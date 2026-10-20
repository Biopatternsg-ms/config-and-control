#!/usr/bin/env bash
# Test script to validate RabbitMQ Management HTTP API purge calls with 404 tolerance
set -euo pipefail

RABBIT_HOST="${RABBIT_HOST:-localhost}"
RABBIT_PORT="${RABBIT_PORT:-15672}"
RABBIT_USER="${RABBIT_USER:-rabbit}"
RABBIT_PASSWORD="${RABBIT_PASSWORD:-rabbit}"

purge_queue() {
    local queue="$1"
    echo "Attempting to purge queue: ${queue}..."
    local status_code
    status_code=$(curl -s -o /dev/null -w "%{http_code}" -u "${RABBIT_USER}:${RABBIT_PASSWORD}" \
        -X DELETE "http://${RABBIT_HOST}:${RABBIT_PORT}/api/queues/%2f/${queue}/contents" || true)

    if [ "$status_code" -eq 204 ] || [ "$status_code" -eq 200 ]; then
        echo "✓ Successfully purged queue: ${queue} (HTTP ${status_code})"
    elif [ "$status_code" -eq 404 ]; then
        echo "✓ Queue does not exist yet: ${queue} (HTTP 404 - tolerated during deployment)"
    else
        echo "⚠ Warning: Queue ${queue} purge returned status ${status_code}"
    fi
}

echo "=== Testing RabbitMQ Queue Purge Operations ==="
purge_queue "mesh-queue"
purge_queue "ncbi-pubmed-queue"
purge_queue "pubtator-queue"
echo "=== Queue purge test complete ==="
