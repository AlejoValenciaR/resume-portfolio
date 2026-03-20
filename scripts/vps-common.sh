#!/usr/bin/env bash
set -euo pipefail

log() {
  printf '\n[%s] %s\n' "$(date -u +'%Y-%m-%dT%H:%M:%SZ')" "$*"
}

fail() {
  echo "ERROR: $*" >&2
  exit 1
}

require_env() {
  local name="$1"
  if [[ -z "${!name:-}" ]]; then
    fail "Environment variable ${name} is required."
  fi
}

require_ssh_auth() {
  if [[ -z "${SSH_KEY_FILE:-}" && -z "${SSH_PASSWORD:-}" ]]; then
    fail "Provide either SSH_PASSWORD or SSH_KEY_FILE for SSH authentication."
  fi
}

init_ssh_context() {
  require_env VPS_HOST
  require_env VPS_PORT
  require_env SSH_USER
  require_ssh_auth

  TEMP_WORK_DIR="$(mktemp -d)"
  KNOWN_HOSTS_FILE="${TEMP_WORK_DIR}/known_hosts"

  ssh-keyscan -p "${VPS_PORT}" "${VPS_HOST}" > "${KNOWN_HOSTS_FILE}" 2>/dev/null

  SSH_BASE_ARGS=(
    -p "${VPS_PORT}"
    -o StrictHostKeyChecking=yes
    -o UserKnownHostsFile="${KNOWN_HOSTS_FILE}"
  )

  if [[ -n "${SSH_PASSWORD:-}" ]]; then
    command -v sshpass >/dev/null 2>&1 || fail "sshpass is required on the Jenkins agent for password-based SSH."
    export SSHPASS="${SSH_PASSWORD}"
    SSH_AUTH_MODE="password"
    SSH_BASE_ARGS+=(
      -o PreferredAuthentications=password,keyboard-interactive
      -o PubkeyAuthentication=no
    )
  else
    chmod 600 "${SSH_KEY_FILE}"
    SSH_AUTH_MODE="key"
    SSH_BASE_ARGS+=(
      -i "${SSH_KEY_FILE}"
      -o BatchMode=yes
      -o IdentitiesOnly=yes
    )
  fi

  export TEMP_WORK_DIR
  export KNOWN_HOSTS_FILE
  export SSH_AUTH_MODE
}

cleanup_ssh_context() {
  if [[ -n "${TEMP_WORK_DIR:-}" && -d "${TEMP_WORK_DIR}" ]]; then
    rm -rf "${TEMP_WORK_DIR}"
  fi

  unset SSHPASS || true
}

remote_exec() {
  if [[ "${SSH_AUTH_MODE:-}" == "password" ]]; then
    sshpass -e ssh "${SSH_BASE_ARGS[@]}" "${SSH_USER}@${VPS_HOST}" "$@"
    return
  fi

  ssh "${SSH_BASE_ARGS[@]}" "${SSH_USER}@${VPS_HOST}" "$@"
}

remote_upload() {
  local source_path="$1"
  local target_path="$2"
  if [[ "${SSH_AUTH_MODE:-}" == "password" ]]; then
    sshpass -e scp "${SSH_BASE_ARGS[@]}" "${source_path}" "${SSH_USER}@${VPS_HOST}:${target_path}"
    return
  fi

  scp "${SSH_BASE_ARGS[@]}" "${source_path}" "${SSH_USER}@${VPS_HOST}:${target_path}"
}

build_image_archive() {
  require_env IMAGE_REF
  require_env IMAGE_ARCHIVE_PATH

  docker image inspect "${IMAGE_REF}" >/dev/null 2>&1 || fail "Docker image ${IMAGE_REF} was not built."
  docker save -o "${IMAGE_ARCHIVE_PATH}" "${IMAGE_REF}"
}

render_nginx_template() {
  require_env DOMAIN_NAME
  require_env SSL_CERT_PATH
  require_env SSL_KEY_PATH

  local template_path="$1"
  local output_path="$2"

  sed \
    -e "s|__DOMAIN_NAME__|${DOMAIN_NAME}|g" \
    -e "s|__SSL_CERT_PATH__|${SSL_CERT_PATH}|g" \
    -e "s|__SSL_KEY_PATH__|${SSL_KEY_PATH}|g" \
    "${template_path}" > "${output_path}"
}
