# Research & Decisions: Automated Pipeline Resumption on Deployment and Fail-Fast Error Truncation

**Feature Branch**: `002-resume-failed-pipelines-on-deploy`  
**Date**: 2026-10-20  
**Spec Reference**: [spec.md](./spec.md)

---

## 1. Context & Architecture Overview

The BioPatternsG platform consists of interacting Quarkus microservices orchestrating computational pipelines for biological object discovery, PubMed literature analysis, and inference discovery:
- **`config-and-control`**: Central orchestrator, pipeline lifecycle manager, MongoDB persistence, Keycloak integration.
- **`search-biological-objects`**: Discovers biological entities and queries ontologies.
- **`ontologies`**: Builds GO and MeSH trees, consumes/produces to RabbitMQ (`mesh-queue`).
- **`pubmed-integration`**: Builds term pairs, searches PubMed and PubTator, consumes/produces to RabbitMQ (`ncbi-pubmed-queue`, `pubtator-queue`).
- **`build-knowledge-base`**: Python FastAPI service building graph relationships.

This research resolves all design questions identified during specification and clarification phases to prepare a robust, constitution-compliant implementation plan.

---

## 2. Research Decisions

### Decision 1: Authentication & Context Resolution for CI/CD Deployment Endpoint

- **Question**: How should `POST /config-and-control/pipelines/resume-failed` authenticate and establish user context when invoked automatically by Jenkins via `curl` after deployment?
- **Decision**: 
  1. Exclude the path `config-and-control/pipelines/resume-failed` in `ContextInterceptor.filter(...)` from mandatory `x-user-id` header validation (or allow an internal header flag `x-system-call: true`).
  2. In `ResumeFailedPipelinesUseCase`, query all pipelines in `FAILED` status across the entire collection (using a new repository method `pipelineRepository.findAllFailed()`).
  3. For each failed pipeline, query its parent `networkId` from `NetworkRepository` (`networkRepository.findById(pipeline.getNetworkId())`) to resolve the owner `userId`.
  4. Set context dynamically in the use case using `jakarta.ws.rs.core.MultivaluedHashMap<String, String>` (`context.putSingle("x-user-id", ownerUserId); sessionUtil.setContext(context);`) before dispatching to `PipelineStepOrchestrator`.
- **Rationale**:
  - `ContextInterceptor` protects user-facing REST endpoints by requiring `x-user-id` provided by Keycloak API Gateway. However, during automated deployment resumption, Jenkins acts as a system agent and does not possess individual user credentials.
  - Pipelines belong to different researchers. Resolving `userId` from `NetworkCollection` ensures that each re-dispatched step runs under the genuine tenant's identity, preventing authorization rejections in downstream worker services.
- **Alternatives Considered**:
  - *Keycloak Client Credentials Token Exchange*: Requires Jenkins to authenticate against Keycloak, retrieve an access token, and pass it in every request. Rejected because it adds substantial CI overhead and still leaves the question of which user identity owns the pipeline data.
  - *Fixed System User ID (`system-ci`)*: Jenkins passes a static system user. Rejected because downstream services filter and associate biological objects with the requesting `userId`; attributing all resumed pipelines to a generic system user would break user data isolation.

---

### Decision 2: Sequential Batch Processing and Error Isolation

- **Question**: When `resumeFailedPipelines` discovers multiple `FAILED` pipelines, how should the execution batch be scheduled?
- **Decision**: 
  - Execute sequentially in a `for` loop within `ResumeFailedPipelinesUseCase`.
  - Wrap each pipeline re-dispatch in a dedicated `try-catch (Exception ex)` block.
  - Return a structured DTO `ResumeFailedPipelinesResponse` containing counts (`totalFailed`, `resumed`, `skipped`, `errors`) and detailed itemized status per pipeline.
- **Rationale**:
  - Downstream services interface with external APIs (NCBI ESearch and PubTator3) that enforce rate limits (3 to 8 requests/second). Launching dozens of pipelines in parallel would cause 429 Too Many Requests errors.
  - Sequential processing consumes minimal memory and avoids worker thread exhaustion on container startup.
  - An exception on one malformed pipeline (e.g. missing network or corrupted configuration) must not interrupt the resumption of remaining valid pipelines.
- **Alternatives Considered**:
  - *Asynchronous Thread Pool / Parallel Streams*: Rejected due to external rate limiting and risk of overwhelming MongoDB connection pools.
  - *Scheduled Quartz Job in Background*: Overcomplicated for post-deployment execution, makes it difficult for the Jenkins step to report the immediate resumption count in build logs.

---

### Decision 3: Fault Tolerance (`@Retry`) and Fail-Fast Error Truncation

- **Question**: How should inter-service communication failures (HTTP 4xx/5xx, timeouts, connection refused) be intercepted, retried, and truncated?
- **Decision**:
  1. Add `quarkus-smallrye-fault-tolerance` to `pubmed-integration/pom.xml` (it is already present in `config-and-control` and `search-biological-objects`).
  2. Annotate outbound REST client interfaces or adapter methods with `@Retry(maxRetries = 2, delay = 1000)`.
  3. In adapter catch blocks (`catch (WebApplicationException | ProcessingException | Exception e)`):
     - Log error details with pipeline and step context.
     - Call `pipelineService.updateStep(pipelineId, currentStep, Status.FAILED)`.
     - Do NOT proceed to subsequent steps in the chain (truncate process).
  4. In `pubmed-integration/PubmedController`, update asynchronous controller endpoints (`build-pairs`, `search-pubmed-ids-by-pairs`, `search-pubtator-by-pmids`) to invoke `configAndControlRepository.updateStep(pipelineId, currentStep, Status.FAILED, userId)` within their `catch (Exception e)` blocks, ensuring alignment with existing handlers in `GenerateAlignedObjectsUseCase` and `GenerateKbForPipelineUseCase`.
- **Rationale**:
  - A brief retry (2 retries with 1s backoff) absorbs transient network blips and micro-service restarts without failing prematurely.
  - If the service remains unavailable after retries, failing immediately prevents zombie workflows, eliminates orphan background threads, and leaves the pipeline in a clean state ready for post-deployment resumption.
- **Alternatives Considered**:
  - *Zero Retries (Immediate Fail-Fast)*: Too fragile in containerized environments where services may take 1-2 seconds to establish socket connections.
  - *Manual Thread.sleep loops in code*: Boilerplate, non-idiomatic in Quarkus, and bypasses SmallRye metrics and CircuitBreaker capabilities.

---

### Decision 4: Readiness Probing in `config-and-control` for Jenkins

- **Question**: How does Jenkins verify that `config-and-control` has completed startup before invoking `resume-failed`?
- **Decision**:
  1. Add `quarkus-smallrye-health` to `config-and-control/pom.xml`.
  2. Quarkus automatically registers `/q/health/ready` (checking database connectivity and application runtime status).
  3. In `config-and-control/jenkins/Jenkinsfile`, immediately following `docker run -d`, execute:
     ```bash
     echo "--- Waiting for application readiness ---"
     timeout 30 sh -c 'until curl --silent --fail http://localhost:${CONFIG_AND_CONTROL_PORT}/q/health/ready; do sleep 2; done'
     echo "--- Triggering resume of failed pipelines ---"
     curl -s -X POST "http://localhost:${CONFIG_AND_CONTROL_PORT}/config-and-control/pipelines/resume-failed"
     ```
- **Rationale**:
  - Standard Quarkus extension providing RFC-compliant health probes.
  - Deterministic polling replaces arbitrary `sleep` statements, minimizing deployment pipeline duration while preventing connection refused errors.
- **Alternatives Considered**:
  - *Arbitrary `sleep 20`*: Flaky on busy build machines and unnecessarily slow on fast runners.
  - *Polling `GET /config-and-control/pipelines`*: Requires headers (`x-user-id`) and triggers database queries that may distort audit logs.

---

### Decision 5: RabbitMQ Queue Purging in Worker Deployment Pipelines

- **Question**: How should `mesh-queue` in `ontologies` and `ncbi-pubmed-queue`/`pubtator-queue` in `pubmed-integration` be purged during deployment?
- **Decision**:
  - In `ontologies/jenkins/Jenkinsfile` and `pubmed-integration/jenkins/Jenkinsfile`, execute an HTTP call to the RabbitMQ Management API (`port 15672`) between `docker rm` and `docker run -d`:
    ```bash
    curl -s -u "${RABBIT_USER}:${RABBIT_PASSWORD}" -X DELETE \
        "http://${RABBIT_HOST}:15672/api/queues/%2f/${QUEUE_NAME}/contents" || true
    ```
  - Handle HTTP 404 (queue not yet created or broker freshly initialized) gracefully with `|| true`, preventing deployment failures.
- **Rationale**:
  - The RabbitMQ container exposes the management HTTP API on port 15672 with existing credentials (`RABBIT_USER` and `RABBIT_PASSWORD`) already present in Jenkins credentials store.
  - Purging queue contents deletes queued and unacknowledged messages without destroying queue declarations, durable bindings, or exchange subscriptions.
- **Alternatives Considered**:
  - *`docker exec rabbitmq rabbitmqctl purge_queue`*: Requires the Jenkins agent to have Docker CLI access to the specific RabbitMQ container name, which creates hardcoupling and fails in remote Docker/Kubernetes runners.
  - *In-app startup hook (`@Observes StartupEvent`)*: Starts consuming messages the moment the SmallRye connector initializes, causing a race condition where messages are consumed before the purge code runs.

---

### Decision 6: Deployment Simulation Test Strategy

- **Question**: How should the multi-repository deployment recovery behavior be tested and verified?
- **Decision**:
  1. **Unit & Use Case Tests in `config-and-control`**:
     - `ResumeFailedPipelinesUseCaseTest`: Test recovery logic across each of the 7 automated steps, ensuring the correct predecessor step is passed to `PipelineStepOrchestrator`.
     - Test that manual steps (`UPDATE_ALIGNED_OBJECTS`, `CONFIGURE_INFERENCES`) are excluded from auto-dispatch.
     - Test error isolation when one pipeline throws an exception.
     - Test dynamic `userId` extraction from `NetworkRepository`.
  2. **Inter-Service Error Truncation Tests**:
     - Test REST client mock failures (HTTP 500, ConnectException) verifying that step status transitions to `FAILED` and no further steps are invoked.
  3. **End-to-End Simulation Test (`DeploymentRecoverySimulationIT`)**:
     - A dedicated test verifying the end-to-end flow: create pipeline, fail at step N, invoke `POST /resume-failed`, assert step N is re-dispatched and transitions to `IN_PROGRESS`.
- **Rationale**:
  - Fast, reliable feedback in local builds and CI without requiring a live multi-container Docker swarm.

---

## 3. Summary of Repository Impact Matrix

| Repository | Files to Create / Modify | Scope | Elevated Permission Needed? |
|---|---|---|---|
| **`config-and-control`** | `pom.xml`, `PipelineController.java`, `domain.port.in.ResumeFailedPipelines.java`, `application.usecase.ResumeFailedPipelinesUseCase.java`, `PipelineRepository.java`, `PipelineRepositoryAdapter.java`, `ContextInterceptor.java`, `PipelineStepOrchestrator.java`, `Jenkinsfile`, unit/integration tests | Core (Active Workspace) | No |
| **`pubmed-integration`** | `pom.xml`, `PubmedController.java`, `Jenkinsfile`, outbound REST client adapters | Worker (Active Workspace) | No |
| **`ontologies`** | `jenkins/Jenkinsfile` | Worker (Active Workspace) | No |
| **`search-biological-objects`** | Outbound REST adapters & error handling in `QueryOntologiesImpl`, `QueryConfigAndControlImp` | External Worker (Read-only Workspace) | **YES** (Outside active workspace) |
