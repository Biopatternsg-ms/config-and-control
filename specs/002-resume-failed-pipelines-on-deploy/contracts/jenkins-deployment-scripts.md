# Contract: CI/CD Deployment Script Modifications

**Feature Branch**: `002-resume-failed-pipelines-on-deploy`  
**Date**: 2026-10-20  
**Spec Reference**: [spec.md](../spec.md)

---

## 1. `config-and-control/jenkins/Jenkinsfile`

### Context
In stage `'Deploy Application'`, immediately following the `docker run -d` invocation:

### Script Snippet to Insert
```groovy
                        // Post-deployment readiness probe and pipeline resumption
                        echo "--- Awaiting application readiness on port ${CONFIG_AND_CONTROL_PORT} ---"
                        sh """
                        timeout 30 sh -c '
                            until curl --silent --fail http://localhost:${CONFIG_AND_CONTROL_PORT}/q/health/ready > /dev/null 2>&1; do
                                echo "Waiting for config-and-control container to become ready..."
                                sleep 2
                            done
                        '
                        """

                        echo "--- Resuming failed pipelines ---"
                        sh """
                        curl -s -X POST \
                            -H "x-system-call: true" \
                            http://localhost:${CONFIG_AND_CONTROL_PORT}/config-and-control/pipelines/resume-failed || true
                        """
```

---

## 2. `ontologies/jenkins/Jenkinsfile`

### Context
In stage `'Deploy Application'`, immediately following `docker rm ${CONTAINER_NAME} || true` and preceding `docker run -d`:

### Script Snippet to Insert
```groovy
                        // Purge RabbitMQ processing queue before container launch
                        echo "--- Purging RabbitMQ mesh-queue ---"
                        sh """
                        curl -s -u "${RABBIT_USER}:${RABBIT_PASSWORD}" -X DELETE \
                            "http://${RABBIT_HOST}:15672/api/queues/%2f/mesh-queue/contents" || true
                        """
```

---

## 3. `pubmed-integration/jenkins/Jenkinsfile`

### Context
In stage `'Deploy Application'`, immediately following `docker rm ${CONTAINER_NAME} || true` and preceding `docker run -d`:

### Script Snippet to Insert
```groovy
                        // Purge RabbitMQ processing queues before container launch
                        echo "--- Purging RabbitMQ pubmed-integration queues ---"
                        sh """
                        curl -s -u "${RABBIT_USER}:${RABBIT_PASSWORD}" -X DELETE \
                            "http://${RABBIT_HOST}:15672/api/queues/%2f/ncbi-pubmed-queue/contents" || true
                        curl -s -u "${RABBIT_USER}:${RABBIT_PASSWORD}" -X DELETE \
                            "http://${RABBIT_HOST}:15672/api/queues/%2f/pubtator-queue/contents" || true
                        """
```
