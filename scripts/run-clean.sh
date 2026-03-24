#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$REPO_ROOT"

MAVEN_ARGS=(clean package)
APP_ARGS=()

if [[ "${1:-}" == "--skip-tests" ]]; then
  MAVEN_ARGS+=(-DskipTests)
  shift
fi

if [[ "$#" -gt 0 ]]; then
  APP_ARGS=("$@")
fi

echo "Cleaning and packaging the Spring Boot app..."
./mvnw "${MAVEN_ARGS[@]}"

JAR_PATH="$(find "$REPO_ROOT/target" -maxdepth 1 -type f -name '*.jar' ! -name 'original-*' | sort | tail -n 1)"

if [[ -z "$JAR_PATH" ]]; then
  echo "No runnable jar was found in target/ after the build." >&2
  exit 1
fi

echo "Starting Spring Boot from $JAR_PATH..."
echo "Open http://localhost:8080 once the app is ready."

exec java -jar "$JAR_PATH" "${APP_ARGS[@]}"
