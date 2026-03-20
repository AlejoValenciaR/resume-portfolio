#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# shellcheck source=scripts/vps-common.sh
source "${SCRIPT_DIR}/vps-common.sh"

trap cleanup_ssh_context EXIT

require_env APP_NAME
require_env APP_CONTAINER_NAME
require_env DOMAIN_NAME
require_env VPS_HOST
require_env VPS_PORT
require_env REMOTE_APP_DIR
require_env SSH_USER
require_ssh_auth
require_env IMAGE_REF
require_env SSL_FULLCHAIN_FILE
require_env SSL_PRIVKEY_FILE

REMOTE_STAGING_DIR="${REMOTE_STAGING_DIR:-/tmp/${APP_NAME}-bootstrap}"
SSL_CERT_PATH="/etc/nginx/ssl/${DOMAIN_NAME}/fullchain.pem"
SSL_KEY_PATH="/etc/nginx/ssl/${DOMAIN_NAME}/privkey.pem"

init_ssh_context

IMAGE_ARCHIVE_PATH="${TEMP_WORK_DIR}/${APP_NAME}.tar"
APP_ENV_PATH="${TEMP_WORK_DIR}/app.env"
NGINX_CONFIG_PATH="${TEMP_WORK_DIR}/${DOMAIN_NAME}.conf"

log "Rendering runtime configuration files."
cat > "${APP_ENV_PATH}" <<'EOF'
APP_BASE_PATH=
EOF

if [[ -n "${APP_RUNTIME_ENV_FILE:-}" ]]; then
  log "Appending optional application environment file."
  cat "${APP_RUNTIME_ENV_FILE}" >> "${APP_ENV_PATH}"
fi

render_nginx_template "${REPO_ROOT}/deploy/vps/nginx-site.conf.template" "${NGINX_CONFIG_PATH}"

log "Packaging Docker image ${IMAGE_REF}."
build_image_archive

log "Preparing remote staging directory ${REMOTE_STAGING_DIR}."
remote_exec "mkdir -p '${REMOTE_STAGING_DIR}'"

log "Uploading image archive, SSL files, and Nginx configuration to the VPS."
remote_upload "${IMAGE_ARCHIVE_PATH}" "${REMOTE_STAGING_DIR}/image.tar"
remote_upload "${APP_ENV_PATH}" "${REMOTE_STAGING_DIR}/app.env"
remote_upload "${NGINX_CONFIG_PATH}" "${REMOTE_STAGING_DIR}/nginx.conf"
remote_upload "${SSL_FULLCHAIN_FILE}" "${REMOTE_STAGING_DIR}/fullchain.pem"
remote_upload "${SSL_PRIVKEY_FILE}" "${REMOTE_STAGING_DIR}/privkey.pem"

log "Bootstrapping the VPS and deploying the container."
remote_exec env \
  APP_NAME="${APP_NAME}" \
  APP_CONTAINER_NAME="${APP_CONTAINER_NAME}" \
  DOMAIN_NAME="${DOMAIN_NAME}" \
  IMAGE_REF="${IMAGE_REF}" \
  REMOTE_APP_DIR="${REMOTE_APP_DIR}" \
  REMOTE_LOGIN_USER="${SSH_USER}" \
  REMOTE_STAGING_DIR="${REMOTE_STAGING_DIR}" \
  SSL_CERT_PATH="${SSL_CERT_PATH}" \
  SSL_KEY_PATH="${SSL_KEY_PATH}" \
  bash -s <<'EOF'
set -euo pipefail

run_sudo() {
  if [[ "$(id -u)" -eq 0 ]]; then
    "$@"
  else
    sudo "$@"
  fi
}

install_base_packages() {
  if command -v apt-get >/dev/null 2>&1; then
    export DEBIAN_FRONTEND=noninteractive
    run_sudo apt-get update
    run_sudo apt-get install -y ca-certificates curl gnupg nginx ufw
    return
  fi

  if command -v dnf >/dev/null 2>&1; then
    run_sudo dnf install -y ca-certificates curl nginx firewalld
    return
  fi

  if command -v yum >/dev/null 2>&1; then
    run_sudo yum install -y ca-certificates curl nginx firewalld
    return
  fi

  echo "Unsupported Linux distribution. Install Docker and Nginx manually." >&2
  exit 1
}

ensure_docker() {
  if ! command -v docker >/dev/null 2>&1; then
    curl -fsSL https://get.docker.com | run_sudo sh
  fi

  run_sudo systemctl enable --now docker
}

ensure_nginx() {
  run_sudo systemctl enable --now nginx
}

configure_firewall() {
  if command -v ufw >/dev/null 2>&1; then
    run_sudo ufw allow OpenSSH || true
    run_sudo ufw allow 'Nginx Full' || true
    run_sudo ufw --force enable || true
    return
  fi

  if command -v firewall-cmd >/dev/null 2>&1; then
    run_sudo systemctl enable --now firewalld || true
    run_sudo firewall-cmd --permanent --add-service=http || true
    run_sudo firewall-cmd --permanent --add-service=https || true
    run_sudo firewall-cmd --reload || true
  fi
}

configure_nginx() {
  local site_name="${DOMAIN_NAME}.conf"
  local ssl_dir
  ssl_dir="$(dirname "${SSL_CERT_PATH}")"

  run_sudo mkdir -p "${ssl_dir}"
  run_sudo cp "${REMOTE_STAGING_DIR}/fullchain.pem" "${SSL_CERT_PATH}"
  run_sudo cp "${REMOTE_STAGING_DIR}/privkey.pem" "${SSL_KEY_PATH}"
  run_sudo chmod 644 "${SSL_CERT_PATH}"
  run_sudo chmod 600 "${SSL_KEY_PATH}"

  if [[ -d /etc/nginx/sites-available ]]; then
    run_sudo cp "${REMOTE_STAGING_DIR}/nginx.conf" "/etc/nginx/sites-available/${site_name}"
    run_sudo ln -sfn "/etc/nginx/sites-available/${site_name}" "/etc/nginx/sites-enabled/${site_name}"
    run_sudo rm -f /etc/nginx/sites-enabled/default
  else
    run_sudo cp "${REMOTE_STAGING_DIR}/nginx.conf" "/etc/nginx/conf.d/${site_name}"
  fi

  run_sudo nginx -t
  run_sudo systemctl restart nginx
}

deploy_container() {
  run_sudo mkdir -p "${REMOTE_APP_DIR}"
  run_sudo cp "${REMOTE_STAGING_DIR}/app.env" "${REMOTE_APP_DIR}/app.env"

  run_sudo docker load -i "${REMOTE_STAGING_DIR}/image.tar"
  run_sudo docker rm -f "${APP_CONTAINER_NAME}" >/dev/null 2>&1 || true
  run_sudo docker run -d \
    --name "${APP_CONTAINER_NAME}" \
    --restart unless-stopped \
    --env-file "${REMOTE_APP_DIR}/app.env" \
    -p 127.0.0.1:8080:8080 \
    "${IMAGE_REF}"
}

verify_deployment() {
  sleep 5
  curl -fsS "http://127.0.0.1:8080/portfolio/alejandro" >/dev/null
  curl -kfsS --resolve "${DOMAIN_NAME}:443:127.0.0.1" "https://${DOMAIN_NAME}/portfolio/alejandro" >/dev/null

  local root_status
  root_status="$(curl -kIs --resolve "${DOMAIN_NAME}:443:127.0.0.1" "https://${DOMAIN_NAME}/" | awk 'NR==1 {print $2}')"
  if [[ "${root_status}" != "301" && "${root_status}" != "302" ]]; then
    echo "Unexpected root response status: ${root_status}" >&2
    exit 1
  fi
}

cleanup_stage() {
  rm -rf "${REMOTE_STAGING_DIR}"
}

install_base_packages
ensure_docker
ensure_nginx
configure_firewall

if id "${REMOTE_LOGIN_USER}" >/dev/null 2>&1; then
  run_sudo usermod -aG docker "${REMOTE_LOGIN_USER}" || true
fi

deploy_container
configure_nginx
verify_deployment
cleanup_stage
EOF

log "Bootstrap deployment finished successfully."
