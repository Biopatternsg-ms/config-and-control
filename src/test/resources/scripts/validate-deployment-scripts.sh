#!/usr/bin/env bash
# Validation script for Jenkinsfile shell snippets using bash -n / sh -n syntax verification
set -euo pipefail

echo "=== Validating Deployment Script Syntax ==="

# Script 1: config-and-control health check and resume call
cat << 'EOF' > /tmp/validate-cac-deploy.sh
timeout 30 sh -c '
    until curl --silent --fail "http://localhost:${CONFIG_AND_CONTROL_PORT}/q/health/ready" > /dev/null 2>&1; do
        echo "Waiting for config-and-control container to become ready..."
        sleep 2
    done
'
curl -s -X POST -H "x-system-call: true" "http://localhost:${CONFIG_AND_CONTROL_PORT}/config-and-control/pipelines/resume-failed" || true
EOF

sh -n /tmp/validate-cac-deploy.sh
echo "✓ config-and-control deploy script syntax is valid"

# Script 2: ontologies RabbitMQ purge
cat << 'EOF' > /tmp/validate-ontologies-deploy.sh
curl -s -u "${RABBIT_USER}:${RABBIT_PASSWORD}" -X DELETE "http://${RABBIT_HOST}:15672/api/queues/%2f/mesh-queue/contents" || true
EOF

sh -n /tmp/validate-ontologies-deploy.sh
echo "✓ ontologies purge script syntax is valid"

# Script 3: pubmed-integration RabbitMQ purge
cat << 'EOF' > /tmp/validate-pubmed-deploy.sh
curl -s -u "${RABBIT_USER}:${RABBIT_PASSWORD}" -X DELETE "http://${RABBIT_HOST}:15672/api/queues/%2f/ncbi-pubmed-queue/contents" || true
curl -s -u "${RABBIT_USER}:${RABBIT_PASSWORD}" -X DELETE "http://${RABBIT_HOST}:15672/api/queues/%2f/pubtator-queue/contents" || true
EOF

sh -n /tmp/validate-pubmed-deploy.sh
echo "✓ pubmed-integration purge script syntax is valid"

rm -f /tmp/validate-cac-deploy.sh /tmp/validate-ontologies-deploy.sh /tmp/validate-pubmed-deploy.sh
echo "=== All deployment scripts verified successfully ==="
