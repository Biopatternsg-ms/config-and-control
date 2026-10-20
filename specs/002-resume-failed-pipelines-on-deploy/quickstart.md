# Quickstart & Verification Guide: Deployment Resumption & Fail-Fast Truncation

**Feature Branch**: `002-resume-failed-pipelines-on-deploy`  
**Date**: 2026-10-20  
**Spec Reference**: [spec.md](./spec.md)

---

## 1. Prerequisites

- **Java**: OpenJDK 21
- **Build Tool**: Maven 3.9+ (`./mvnw`)
- **Docker**: Docker Engine 24+ & Docker Compose
- **Databases & Middleware**:
  - MongoDB listening on `localhost:27017`
  - RabbitMQ with management plugin listening on `localhost:5672` (AMQP) and `localhost:15672` (Management HTTP)

---

## 2. Verification Scenarios

### Scenario 1: Automated Pipeline Resumption (`config-and-control`)

**Objective**: Verify that a pipeline marked `FAILED` at step `SEARCH_PUBMED_IDS` resumes execution from predecessor step `COMBINATIONS`.

1. **Setup test data in MongoDB (`config-and-control`)**:
   Insert a mock network and a failed pipeline:
   ```javascript
   // Insert parent network
   const networkId = ObjectId();
   db.network.insertOne({
       _id: networkId,
       userId: "user-test-uuid-1234",
       name: "Test Network",
       description: "Network for testing resumption"
   });

   // Insert pipeline failed at SEARCH_PUBMED_IDS
   db.pipeline.insertOne({
       name: "Interrupted Pipeline",
       networkId: networkId.toString(),
       step: "SEARCH_PUBMED_IDS",
       statuses: [
           { step: "LAUNCH", status: "COMPLETED", createdAt: new Date() },
           { step: "SEARCH_LEVELS", status: "COMPLETED", createdAt: new Date() },
           { step: "COMBINATIONS", status: "COMPLETED", createdAt: new Date() },
           { step: "SEARCH_PUBMED_IDS", status: "FAILED", createdAt: new Date(), metrics: { error: "Simulated container crash" } }
       ]
   });
   ```

2. **Invoke the resumption endpoint**:
   ```bash
   curl -i -X POST \
       -H "Content-Type: application/json" \
       -H "x-system-call: true" \
       http://localhost:8080/config-and-control/pipelines/resume-failed
   ```

3. **Expected Output**:
   - HTTP Status: `200 OK`
   - Response Body:
     ```json
     {
       "totalFailed": 1,
       "resumed": 1,
       "skipped": 0,
       "errors": 0,
       "details": [
         {
           "pipelineId": "<pipelineId>",
           "networkId": "<networkId>",
           "userId": "user-test-uuid-1234",
           "failedStep": "SEARCH_PUBMED_IDS",
           "resumedFromStep": "COMBINATIONS",
           "resultStatus": "RESUMED",
           "message": "Successfully re-dispatched to orchestrator"
         }
       ]
     }
     ```
   - MongoDB Assertion: Pipeline's step `SEARCH_PUBMED_IDS` has a new status entry `IN_PROGRESS`.

---

### Scenario 2: Exemption of Manual Review Steps (`config-and-control`)

**Objective**: Verify that pipelines in `FAILED` or waiting state at manual review steps (`UPDATE_ALIGNED_OBJECTS` or `CONFIGURE_INFERENCES`) are NOT automatically dispatched to workers.

1. **Setup test data**:
   Insert a pipeline marked `FAILED` or stopped at `UPDATE_ALIGNED_OBJECTS`.
2. **Invoke resumption**:
   ```bash
   curl -i -X POST http://localhost:8080/config-and-control/pipelines/resume-failed
   ```
3. **Expected Output**:
   - `resumed`: 0
   - `skipped`: 1
   - Detail `resultStatus`: `"SKIPPED_MANUAL_STEP"`
   - Pipeline remains in waiting state without worker HTTP dispatch.

---

### Scenario 3: Pre-Deployment Queue Purging (`ontologies` & `pubmed-integration`)

**Objective**: Verify that RabbitMQ queues are completely purged before container startup.

1. **Publish dummy message into `mesh-queue`**:
   ```bash
   curl -i -u rabbit:rabbit -H "content-type:application/json" \
       -X POST http://localhost:15672/api/exchanges/%2f/exchange-ncbi/publish \
       -d'{"properties":{},"routing_key":"mesh-queue","payload":"{\"test\":\"dummy-orphan-message\"}","payload_encoding":"string"}'
   ```
2. **Verify queue depth > 0**:
   ```bash
   curl -s -u rabbit:rabbit http://localhost:15672/api/queues/%2f/mesh-queue | grep -o '"messages":[0-9]*'
   ```
3. **Execute deployment purge script**:
   ```bash
   curl -s -u rabbit:rabbit -X DELETE "http://localhost:15672/api/queues/%2f/mesh-queue/contents"
   ```
4. **Verify queue depth == 0**:
   ```bash
   curl -s -u rabbit:rabbit http://localhost:15672/api/queues/%2f/mesh-queue | grep -o '"messages":0'
   ```

---

### Scenario 4: Inter-Service Fail-Fast Truncation

**Objective**: Verify that when a worker service returns HTTP 500, the caller marks the step as `FAILED` and stops execution.

1. **Mock downstream endpoint failure** (e.g., mock worker returns 500 Internal Server Error).
2. **Trigger step execution**:
   Observe adapter logs:
   - Attempt 1 fails.
   - Retry 1 (after 1000ms) fails.
   - Retry 2 (after 1000ms) fails.
3. **Verify outcome**:
   - `pipelineService.updateStep(pipelineId, step, Status.FAILED)` is invoked.
   - Subsequent step in the sequence is NOT invoked.
   - Overall pipeline status remains `FAILED`.

---

### Scenario 5: ArchUnit Quality Gate Verification

**Objective**: Guarantee that all new and modified classes in `config-and-control` comply with hexagonal architecture principles.

```bash
cd c:\Users\USUARIO\Documents\IdeaProjects\config-and-control
./mvnw test -Dtest=ArchitectureLayerTest
```

**Expected Outcome**:
- `BUILD SUCCESS` with 0 failures:
  - `layerApplicationTest`: PASSED
  - `layerApplicationUsesCasesTest`: PASSED
  - `domainLayerIsolationTest`: PASSED
  - `applicationLayerIsolationTest`: PASSED
  - `useCasesNamingConventionTest`: PASSED
  - `controllersNamingConventionTest`: PASSED
  - `adaptersNamingConventionTest`: PASSED
