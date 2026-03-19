#!/usr/bin/env bash
set -euo pipefail

command_exists() {
  command -v "$1" >/dev/null 2>&1
}

require_command() {
  if ! command_exists "$1"; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

require_command docker

WORK_DIR="${WORKSPACE:-$(pwd)}"
WORKSPACE_MOUNT_PATH="/workspace"
KUBECONFIG_FILE="${KUBECONFIG:-${WORK_DIR}/.kube/config}"
KUBECONFIG_CONTAINER_PATH="${WORKSPACE_MOUNT_PATH}/.kube/config"
K8S_NAMESPACE="${K8S_NAMESPACE:-hello-spring}"
APP_NAME="${APP_NAME:-hello-spring}"
KUBECTL_DOCKER_IMAGE="${KUBECTL_DOCKER_IMAGE:-bitnami/kubectl:1.32.2}"

export KUBECONFIG="${KUBECONFIG_FILE}"

kubectl_cli() {
  if command_exists kubectl; then
    kubectl "$@"
    return
  fi

  docker run --rm \
    -e KUBECONFIG="${KUBECONFIG_CONTAINER_PATH}" \
    -v "${WORK_DIR}:${WORKSPACE_MOUNT_PATH}" \
    -w "${WORKSPACE_MOUNT_PATH}" \
    "${KUBECTL_DOCKER_IMAGE}" "$@"
}

kubectl_cli -n "${K8S_NAMESPACE}" get pods
kubectl_cli -n "${K8S_NAMESPACE}" get service "${APP_NAME}"
kubectl_cli -n "${K8S_NAMESPACE}" get ingress "${APP_NAME}"
