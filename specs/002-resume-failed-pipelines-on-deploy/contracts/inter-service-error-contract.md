# Contract: Inter-Service Communication Error Handling & Fail-Fast Propagation

**Feature Branch**: `002-resume-failed-pipelines-on-deploy`  
**Date**: 2026-10-20  
**Spec Reference**: [spec.md](../spec.md)

---

## 1. Scope & Objective

This contract specifies the mandatory error handling, retry policies, and fail-fast truncation rules governing synchronous REST communications between the microservices in the BioPatternsG ecosystem:
- `config-and-control` → `search-biological-objects`
- `config-and-control` → `pubmed-integration`
- `config-and-control` → `inferences`
- `pubmed-integration` → `config-and-control`
- `pubmed-integration` → `build-knowledge-base`
- `search-biological-objects` → `config-and-control`
- `search-biological-objects` → `ontologies`

---

## 2. MicroProfile Fault Tolerance Policy

All outbound MicroProfile REST Clients or their wrapping repository adapters MUST enforce:

```java
@Retry(maxRetries = 2, delay = 1000)
```

### 2.1 Retryable Conditions
- Network connection failures (`java.net.ConnectException`)
- Request timeouts (`jakarta.ws.rs.ProcessingException`, `java.net.SocketTimeoutException`)
- HTTP 503 (Service Unavailable)
- HTTP 504 (Gateway Timeout)

### 2.2 Non-Retryable Conditions (Immediate Fail-Fast)
- HTTP 4xx Client Errors (400 Bad Request, 401 Unauthorized, 404 Not Found, 422 Unprocessable Entity)
- HTTP 500 Internal Server Error (indicates deterministic business logic or data failure on worker)

---

## 3. Exception Handling & Truncation Pattern

### 3.1 Pattern for `config-and-control` Outbound Adapters
Implemented in:
- `PubmedIntegrationAdapter.java`
- `InferencesAdapter.java`
- `BiologicalObjectAdapter.java`

```java
try {
    // 1. Establish tenant user context
    String userId = sessionUtil.getUserId();
    
    // 2. Invoke remote REST endpoint (subject to @Retry)
    remoteRestClient.dispatchStep(request, userId);
    
    // 3. On success, transition step to IN_PROGRESS
    pipelineService.updateStep(pipelineConfig.getId(), currentStep, Status.IN_PROGRESS);
    log.info("Step {} dispatched successfully for pipeline {}", currentStep, pipelineConfig.getId());
} catch (WebApplicationException | ProcessingException e) {
    // 4. On failure after retries, transition step to FAILED
    pipelineService.updateStep(pipelineConfig.getId(), currentStep, Status.FAILED);
    log.error("Failed to dispatch step {} for pipeline {}: {}", currentStep, pipelineConfig.getId(), e.getMessage(), e);
    
    // 5. Truncate execution: do NOT invoke PipelineStepOrchestrator for subsequent steps
}
```

### 3.2 Pattern for Asynchronous Workers (`pubmed-integration`)
Implemented in:
- `PubmedController.java` (`buildTreeMesh`, `searchPairs`, `searchPubtatorByPmids`, `generateKb`, `generateAlignedObjects`)
- `BuildPubmedPairsUseCase.java`
- `SearchPubmedByPairsUseCase.java`

```java
CompletableFuture.runAsync(() -> {
    try {
        useCase.execute(request.pipelineId(), userId);
    } catch (Throwable e) {
        log.error("Error executing asynchronous task for pipeline {}", request.pipelineId(), e);
        // Explicit fail-fast notification back to central orchestrator
        configAndControlRepository.updateStep(
            request.pipelineId(), 
            targetStep, 
            Status.FAILED, 
            userId, 
            Map.of("errorMessage", e.getMessage() != null ? e.getMessage() : "Unknown error")
        );
    }
}, executor);
```

---

## 4. Status Synchronization Contract (`PATCH /config-and-control/pipelines/update-step`)

Worker services communicate step failures to `config-and-control` using the existing status update contract:

- **Method**: `PATCH`
- **Path**: `/config-and-control/pipelines/update-step`
- **Headers**:
  - `Content-Type`: `application/json`
  - `x-user-id`: `{userId}`
- **Payload Schema (`PipelineStepRequest`)**:
```json
{
  "id": "670d8a1b2f9a7c34d5e8f123",
  "step": "COMBINATIONS",
  "status": "FAILED",
  "metrics": {
    "errorReason": "Connection to NCBI ESearch timed out after 2 retries"
  }
}
```
- **Expected Response**: `202 Accepted` (`"updated"`)
- **Orchestrator Behavior on Receiving `FAILED`**:
  `PipelineController.updateStep` updates the step status to `Status.FAILED` and does NOT invoke `PipelineStepOrchestrator.orchestrate(...)`.
