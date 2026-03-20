#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

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

REMOTE_STAGING_DIR="${REMOTE_STAGING_DIR:-/tmp/${APP_NAME}-deploy}"

init_ssh_context

IMAGE_ARCHIVE_PATH="${TEMP_WORK_DIR}/${APP_NAME}.tar"
APP_ENV_PATH="${TEMP_WORK_DIR}/app.env"

log "Rendering runtime environment file."
cat > "${APP_ENV_PATH}" <<'EOF'
APP_BASE_PATH=
EOF

if [[ -n "${APP_RUNTIME_ENV_FILE:-}" ]]; then
  log "Appending optional application environment file."
  cat "${APP_RUNTIME_ENV_FILE}" >> "${APP_ENV_PATH}"
fi

log "Packaging Docker image ${IMAGE_REF}."
build_image_archive

log "Preparing remote staging directory ${REMOTE_STAGING_DIR}."
remote_exec "mkdir -p '${REMOTE_STAGING_DIR}'"

log "Uploading deployment bundle to the VPS."
remote_upload "${IMAGE_ARCHIVE_PATH}" "${REMOTE_STAGING_DIR}/image.tar"
remote_upload "${APP_ENV_PATH}" "${REMOTE_STAGING_DIR}/app.env"

log "Replacing the running application container."
remote_exec env \
  APP_CONTAINER_NAME="${APP_CONTAINER_NAME}" \
  APP_NAME="${APP_NAME}" \
  DOMAIN_NAME="${DOMAIN_NAME}" \
  IMAGE_REF="${IMAGE_REF}" \
  REMOTE_APP_DIR="${REMOTE_APP_DIR}" \
  REMOTE_STAGING_DIR="${REMOTE_STAGING_DIR}" \
  bash -s <<'EOF'
set -euo pipefail

run_sudo() {
  if [[ "$(id -u)" -eq 0 ]]; then
    "$@"
  else
    sudo "$@"
  fi
}

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker is not installed on the VPS. Run the bootstrap pipeline first." >&2
  exit 1
fi

run_sudo systemctl enable --now docker
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

sleep 5
curl -fsS "http://127.0.0.1:8080/portfolio/alejandro" >/dev/null
curl -kfsS --resolve "${DOMAIN_NAME}:443:127.0.0.1" "https://${DOMAIN_NAME}/portfolio/alejandro" >/dev/null

rm -rf "${REMOTE_STAGING_DIR}"
EOF

log "Deploy-only run finished successfully."
