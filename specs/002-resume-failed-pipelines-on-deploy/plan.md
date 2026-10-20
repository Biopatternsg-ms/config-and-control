# Implementation Plan: Automated Pipeline Resumption on Deployment and Fail-Fast Error Truncation

**Branch**: `002-resume-failed-pipelines-on-deploy` | **Date**: 2026-10-20 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/002-resume-failed-pipelines-on-deploy/spec.md` (validated with 8 clarified decisions).

---

## Summary

This feature implements an end-to-end automated recovery and resilience system across the BioPatternsG microservice ecosystem:
1. **Automated Deployment Resumption (`config-and-control`)**: An administrative endpoint (`POST /config-and-control/pipelines/resume-failed`) and use case that detects all pipelines in `FAILED` status, resolves the owner tenant `userId` from its associated network, determines the exact step where failure occurred, and triggers `PipelineStepOrchestrator` with the predecessor step to re-dispatch execution without repeating completed steps. Excludes manual review steps (`UPDATE_ALIGNED_OBJECTS`, `CONFIGURE_INFERENCES`) to preserve human-in-the-loop validation.
2. **Pre-Deployment Clean Slate Queues (`ontologies` & `pubmed-integration`)**: In CI/CD deployment stages, RabbitMQ message queues (`mesh-queue`, `ncbi-pubmed-queue`, `pubtator-queue`) are purged via the RabbitMQ Management HTTP API (`port 15672`) before starting new containers, eliminating stale and orphaned messages.
3. **Fail-Fast Error Truncation across Inter-Service Calls**: Outbound REST clients enforce `@Retry(maxRetries = 2, delay = 1000)`. If communication errors persist (4xx/5xx, timeouts, connection refused), adapters catch exceptions, immediately update the pipeline step to `FAILED`, and truncate the workflow chain. In `pubmed-integration`, async worker tasks explicitly report `FAILED` back to `config-and-control`.
4. **Health Readiness & Simulation Testing**: Incorporates `quarkus-smallrye-health` in `config-and-control` for deterministic readiness polling (`/q/health/ready`) in Jenkins, and provides a multi-repository test suite verifying recovery across all 7 automated steps.

---

## Technical Context

**Language/Version**: Java 21 LTS (Quarkus 3.x Supersonic Subatomic Java) across backend microservices; Groovy / Declarative Pipeline for Jenkinsfiles.

**Primary Dependencies**:
- `io.quarkus:quarkus-smallrye-health` (new in `config-and-control/pom.xml`)
- `io.quarkus:quarkus-smallrye-fault-tolerance` (existing in `config-and-control` & `search-biological-objects`; new in `pubmed-integration/pom.xml`)
- `io.quarkus:quarkus-mongodb-panache` (existing MongoDB Panache entities and repositories)
- `io.quarkus:quarkus-rest-client-reactive` / MicroProfile REST Client
- `io.smallrye.reactive:smallrye-reactive-messaging-rabbitmq`

**Storage**: MongoDB (collections `pipeline`, `network`, `user` in `config-and-control`; collections in worker microservices).

**Testing**: JUnit 5, Mockito, ArchUnit (`ArchitectureLayerTest`), `@QuarkusTest` / WireMock integration tests.

**Target Platform**: Linux containers running in Docker / Docker Compose / Kubernetes, orchestrated via Jenkins CI/CD.

**Project Type**: Multi-repository distributed microservice architecture (Hexagonal / Ports & Adapters).

**Performance Goals**:
- Resumption batch dispatch initiates within 5 seconds of container readiness.
- Sequential rate-limited re-dispatch protecting external NCBI (<= 8 req/s) and PubTator (<= 3 req/s) rate limits.

**Constraints**:
- Strict Hexagonal Architecture isolation (Constitution Principles I-VI).
- Zero downtime impact on active pipelines; isolated error handling ensuring one bad pipeline cannot abort the batch.
- Repositories outside the active workspace (`search-biological-objects`) are read-only and require elevated permissions if modified.

**Scale/Scope**: 4 repositories (`config-and-control`, `ontologies`, `pubmed-integration`, `search-biological-objects`), 11 pipeline steps, 3 RabbitMQ queues.

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Check Item | Status | Verification Detail |
|---|---|---|---|
| **I. Hexagonal Isolation** | Domain layer purity & Port interfaces | **PASS** | New Inbound Port `domain.port.in.ResumeFailedPipelines` defined without infrastructure dependencies. Concrete use case resides in `application.usecase`. |
| **I. Hexagonal Isolation** | Inbound access control | **PASS** | `PipelineController` injects `ResumeFailedPipelines` (inbound port), never the concrete use case class. |
| **I. Hexagonal Isolation** | Naming conventions | **PASS** | `ResumeFailedPipelinesUseCase`, `PipelineController`, `PipelineRepositoryAdapter`. |
| **II. Use Cases as Orchestrators** | Parameter count <= 3 | **PASS** | `execute()` accepts zero parameters (operates globally on failed pipelines). |
| **III. Service Encapsulation** | Interface in `services`, impl in `impl/` | **PASS** | `PipelineStepOrchestrator` and `PipelineService` encapsulate workflow progression and step state changes. |
| **IV. Use Case Documentation** | Synchronous documentation in `.agents/` | **PASS** | Plan mandates creation of `.agents/use_cases/ResumeFailedPipelinesUseCase.md` and updating `.agents/architecture.md`. |
| **V. Zero Logic in Delivery** | REST Controller purity | **PASS** | `PipelineController.resumeFailedPipelines()` only delegates to `ResumeFailedPipelines.execute()` and returns the DTO response. |
| **VI. ArchUnit Quality Gates** | `ArchitectureLayerTest` passes cleanly | **PASS** | Verified against existing `ArchitectureLayerTest.java`. Unit tests placed in `unit.*` package. |

---

## Project Structure & Multi-Repo Scope

### Documentation Artifacts (Active Feature)

```text
specs/002-resume-failed-pipelines-on-deploy/
├── spec.md              # Requirements and 8 clarified decisions
├── plan.md              # Master implementation plan (this file)
├── research.md          # Architectural research & technical decisions
├── data-model.md        # Domain entities, DTOs, and state machine transitions
├── quickstart.md        # Step-by-step verification and simulation test scenarios
├── contracts/
│   ├── resume-failed-pipelines.json      # OpenAPI 3.0 specification
│   ├── inter-service-error-contract.md   # Error propagation & fault tolerance contract
│   └── jenkins-deployment-scripts.md     # Deployment scripts for Jenkinsfiles
└── checklists/
    └── requirements.md  # 16/16 requirements checklist items passing
```

### Source Code Impact by Repository

#### 1. `config-and-control` (Active Workspace)
```text
pom.xml                                                                       # Add quarkus-smallrye-health
jenkins/Jenkinsfile                                                           # Add readiness probe loop and curl resume-failed
src/main/java/com/biopatternsg/
├── domain/
│   ├── port/in/ResumeFailedPipelines.java                                    # Inbound Port interface
│   ├── port/out/repositories/PipelineRepository.java                         # Add findAllFailed() method
│   └── models/ResumeFailedPipelinesResponse.java                             # Result DTO / Domain Model
├── application/
│   ├── usecase/ResumeFailedPipelinesUseCase.java                             # Core orchestration use case
│   └── services/PipelineStepOrchestrator.java                                # Support predecessor resolution
├── infrastructure/
│   ├── adapters/in/restcontrollers/PipelineController.java                   # Add POST /resume-failed endpoint
│   ├── adapters/out/repositories/PipelineRepositoryAdapter.java              # Implement findAllFailed()
│   ├── config/interceptors/ContextInterceptor.java                           # Exclude /resume-failed or check x-system-call
│   └── dtos/PipelineResumeDetail.java                                        # Itemized detail DTO
src/test/java/
├── unit/usecase/ResumeFailedPipelinesUseCaseTest.java                        # Unit test for resumption logic
├── unit/restcontrollers/PipelineControllerTest.java                          # Unit test for controller delegation
└── integration/DeploymentRecoverySimulationIT.java                           # Integration test for simulated restart recovery
.agents/
├── use_cases/ResumeFailedPipelinesUseCase.md                                 # Mandatory use case documentation
└── architecture.md                                                           # Update use case registry
```

#### 2. `pubmed-integration` (Active Workspace)
```text
pom.xml                                                                       # Add quarkus-smallrye-fault-tolerance
jenkins/Jenkinsfile                                                           # Add RabbitMQ purge for ncbi-pubmed-queue & pubtator-queue
src/main/java/com/biopatternsg/
├── infrastructure/
│   ├── adapters/in/restcontrollers/PubmedController.java                     # Add FAILED status notification in async catch blocks
│   └── adapters/out/external_repositories/                                   # Add @Retry on outbound REST clients
└── application/usecase/
    ├── BuildPubmedPairsUseCase.java                                          # Ensure catch triggers FAILED update
    └── SearchPubmedByPairsUseCase.java                                       # Ensure catch triggers FAILED update
```

#### 3. `ontologies` (Active Workspace)
```text
jenkins/Jenkinsfile                                                           # Add RabbitMQ purge for mesh-queue before docker run -d
```

#### 4. `search-biological-objects` (**OUTSIDE ACTIVE WORKSPACE - READ-ONLY**)
> [!WARNING]
> This repository is at `c:\Users\USUARIO\Documents\IdeaProjects\search-biological-objects` and is NOT an active workspace.
> In the implementation phase, modifying files in this repository will require elevated user permission (or explicit bypass).
```text
src/main/java/com/biopatternsg/
└── infrastructure/internal_services/impl/
    ├── QueryOntologiesImpl.java                                              # Validate HTTP status, don't silently ignore 500
    └── QueryConfigAndControlImp.java                                         # Handle retry and propagation
```

---

## Detailed Implementation Breakdown

### Phase 0: Foundations & Dependencies
- **Task 0.1 (`config-and-control`)**: Add `quarkus-smallrye-health` to `config-and-control/pom.xml`. Verify `./mvnw clean compile` succeeds.
- **Task 0.2 (`pubmed-integration`)**: Add `quarkus-smallrye-fault-tolerance` to `pubmed-integration/pom.xml`. Verify `./mvnw clean compile` succeeds.

### Phase 1: Core Resumption Architecture (`config-and-control`)
- **Task 1.1**: Define domain contract `ResumeFailedPipelinesResponse` and `PipelineResumeDetail` in `com.biopatternsg.domain.models` (or `infrastructure.dtos`).
- **Task 1.2**: Define Inbound Port `com.biopatternsg.domain.port.in.ResumeFailedPipelines` with method `ResumeFailedPipelinesResponse execute()`.
- **Task 1.3**: Add `List<PipelineConfig> findAllFailed()` to `PipelineRepository` port and implement in `PipelineRepositoryAdapter` querying `PipelineCollection.find("statuses.status", Status.FAILED)` (or latest status equals `FAILED`).
- **Task 1.4**: Implement `ResumeFailedPipelinesUseCase`:
  - Query all `FAILED` pipelines.
  - Sequentially process each pipeline.
  - Resolve owner `userId` via `networkRepository.findById(pipeline.getNetworkId()).getUserId()`.
  - Exclude manual steps (`UPDATE_ALIGNED_OBJECTS`, `CONFIGURE_INFERENCES`) -> record `SKIPPED_MANUAL_STEP`.
  - Determine predecessor step:
    - If `SEARCH_LEVELS` or `LAUNCH` -> `biologicalObjectRepository.launch(pipeline)`.
    - If `FIND_ROLES` -> `orchestrator.orchestrate(pipeline, PipelineSteps.CONFIGURE_INFERENCES)`.
    - Other automated steps -> `orchestrator.orchestrate(pipeline, getPredecessor(step))`.
  - Isolate exceptions per pipeline and record in response summary.
- **Task 1.5**: Update `ContextInterceptor` in `config-and-control`:
  - Bypass mandatory `x-user-id` header when `path.contains("pipelines/resume-failed")` or when `x-system-call: true` is present.
- **Task 1.6**: Expose `POST /config-and-control/pipelines/resume-failed` in `PipelineController` delegating to `ResumeFailedPipelines.execute()`.

### Phase 2: Fail-Fast & Truncation Across Services
- **Task 2.1 (`config-and-control`)**: Ensure all outbound adapters (`PubmedIntegrationAdapter`, `InferencesAdapter`, `BiologicalObjectAdapter`) enforce `@Retry(maxRetries = 2, delay = 1000)`, catch `WebApplicationException` / `ProcessingException`, transition step to `FAILED`, and truncate execution.
- **Task 2.2 (`pubmed-integration`)**: In `PubmedController.java`, update asynchronous catch blocks in `buildTreeMesh`, `searchPairs`, and `searchPubtatorByPmids` to call `configAndControlRepository.updateStep(pipelineId, step, Status.FAILED, userId)` to prevent silent unmonitored failure.
- **Task 2.3 (`search-biological-objects` - REQUIRES PERMISSION)**: In `QueryOntologiesImpl.java` and `PipelineServiceImpl.java`, ensure HTTP 500 / connection errors from `ontologies` or `config-and-control` are not swallowed, but bubble up to set `Status.FAILED`.

### Phase 3: CI/CD Deployment Scripts (Jenkinsfiles)
- **Task 3.1 (`config-and-control/jenkins/Jenkinsfile`)**:
  - In `'Deploy Application'`, add loop polling `http://localhost:${CONFIG_AND_CONTROL_PORT}/q/health/ready` (timeout 30s).
  - Add `curl -s -X POST -H "x-system-call: true" http://localhost:${CONFIG_AND_CONTROL_PORT}/config-and-control/pipelines/resume-failed || true`.
- **Task 3.2 (`ontologies/jenkins/Jenkinsfile`)**:
  - In `'Deploy Application'`, before `docker run -d`, add `curl` call to purge `mesh-queue` via RabbitMQ Management API (`port 15672`).
- **Task 3.3 (`pubmed-integration/jenkins/Jenkinsfile`)**:
  - In `'Deploy Application'`, before `docker run -d`, add `curl` calls to purge `ncbi-pubmed-queue` and `pubtator-queue` via RabbitMQ Management API.

### Phase 4: Testing & Documentation Synchronization
- **Task 4.1**: Unit tests in `config-and-control/src/test/java/unit/usecase/ResumeFailedPipelinesUseCaseTest.java`.
- **Task 4.2**: Controller test in `config-and-control/src/test/java/unit/restcontrollers/PipelineControllerTest.java`.
- **Task 4.3**: Deployment simulation test in `config-and-control/src/test/java/integration/DeploymentRecoverySimulationIT.java` covering simulated restart and recovery across all 7 automated steps.
- **Task 4.4**: Verify ArchUnit tests (`./mvnw test -Dtest=ArchitectureLayerTest`).
- **Task 4.5**: Create `.agents/use_cases/ResumeFailedPipelinesUseCase.md` and update `.agents/architecture.md`.

---

## Complexity Tracking

| Item | Why Needed | Simpler Alternative Rejected Because |
|---|---|---|
| Dynamic `userId` resolution via `NetworkCollection` | Pipelines belong to different users; downstream workers validate and associate biological entities with the caller's `userId`. | A static system user (`system-ci`) breaks user data segregation and causes tenant isolation errors in worker services. |
| RabbitMQ Management HTTP API for queue purging | Must purge messages before the worker container launches. | In-container hooks race against SmallRye message consumers; `docker exec rabbitmq` couples CI agents to local Docker socket naming conventions. |
| Exemption in `ContextInterceptor` | Jenkins deployment agent runs as a system process without interactive Keycloak login. | Requiring OAuth2 token exchange in Jenkins adds fragile credential pipelines and does not provide tenant `userId`. |
