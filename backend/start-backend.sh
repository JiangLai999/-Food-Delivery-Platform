#!/usr/bin/env bash
set -e
ROOT_DIR=$(cd "$(dirname "$0")/.." && pwd)
cd "$ROOT_DIR/backend"
echo "[Backend] Building..."
mvn clean package -DskipTests
echo "[Backend] Starting..."
mvn spring-boot:run
