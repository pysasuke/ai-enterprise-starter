#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
docker compose -f docker/docker-compose.yml up -d postgres redis
echo "PostgreSQL and Redis started. Run app locally with: mvn -pl starter-demo spring-boot:run"
