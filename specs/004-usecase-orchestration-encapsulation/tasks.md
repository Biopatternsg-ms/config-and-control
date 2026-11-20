# Tasks: Use Cases Lightweight Orchestration and Parameter Encapsulation

**Input**: Design documents from `specs/004-usecase-orchestration-encapsulation/`
- Spec: [spec.md](./spec.md)
- Plan: [plan.md](./plan.md)
- Research: [research.md](./research.md)
- Data Model: [data-model.md](./data-model.md)
- Contracts: [pipeline-execution-service-contract.md](./contracts/pipeline-execution-service-contract.md), [pipeline-service-parameter-contract.md](./contracts/pipeline-service-parameter-contract.md), [resumption-orchestration-contract.md](./contracts/resumption-orchestration-contract.md)
- Quickstart: [quickstart.md](./quickstart.md)

**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3, US4)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Build environment verification

- [X] T001 Verify Java 21 environment and project build sanity using `mvn test-compile` in `pom.xml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish rich domain invariants in `PipelineConfig` that multiple user stories and services rely on

**⚠️ CRITICAL**: Must be completed before User Story 4 and service refactoring

- [X] T002 [P] Enrich `PipelineConfig` domain aggregate with `isConfigComplete()`, `applyConfigCompletionIfEligible()`, and `applyAlignedObjectsConfirmation(List<String> objects)` in `src/main/java/com/biopatternsg/domain/models/PipelineConfig.java`
- [X] T003 [P] Create unit test verifying configuration completeness rules and aligned objects metrics evaluation in `src/test/java/unit/domain/PipelineConfigTest.java`

**Checkpoint**: Foundation ready - user story implementation can begin

---

## Phase 3: User Story 1 - Encapsulación de Parámetros en Operaciones de Pipeline (Priority: P1) 🎯 MVP

**Goal**: Eliminate methods accepting more than 3 parameters across `PipelineService`, refactoring `updateStep` to accept `PipelineStatus`.

**Independent Test**: Verify that `PipelineService` has no methods accepting >3 parameters, successfully updating pipeline steps with metrics encapsulated in `PipelineStatus`.

### Implementation for User Story 1

- [X] T004 [P] [US1] Refactor `PipelineService` interface in `src/main/java/com/biopatternsg/application/services/PipelineService.java` eliminating the 4-parameter `updateStep` method and providing `updateStep(String id, PipelineStatus statusUpdate)` alongside `updateStep(String id, PipelineSteps step, Status status)`
- [X] T005 [US1] Implement the 2-parameter `updateStep(String id, PipelineStatus statusUpdate)` in `src/main/java/com/biopatternsg/application/services/impl/PipelineServiceImpl.java` delegating to repository save
- [X] T006 [P] [US1] Update `PipelineStepOrchestratorImpl` in `src/main/java/com/biopatternsg/application/services/impl/PipelineStepOrchestratorImpl.java` to invoke the 2-parameter `pipelineService.updateStep(pipelineId, pipelineStatus)` with encapsulated metrics
- [X] T007 [P] [US1] Create unit test in `src/test/java/unit/services/PipelineServiceTest.java` verifying `updateStep(id, pipelineStatus)` persists encapsulated metrics and status correctly

**Checkpoint**: User Story 1 complete — parameter encapsulation enforced on `PipelineService`.

---

## Phase 4: User Story 2 - Orquestación Liviana en Consultas de Ejecución y Métricas (Priority: P2)

**Goal**: Extract timeline computation, duration math, metric aggregation, and UI presentation icons from `GetPipelineExecutionUseCase` into `PipelineExecutionService`.

**Independent Test**: Request pipeline execution report and verify that `GetPipelineExecutionUseCase` verifies pipeline existence and delegates report creation to `PipelineExecutionService`, returning the complete `ExperimentExecutionResponse` with 6 steps.

### Implementation for User Story 2

- [X] T008 [P] [US2] Create public interface contract `PipelineExecutionService` in `src/main/java/com/biopatternsg/application/services/PipelineExecutionService.java` defining `ExperimentExecutionResponse buildExecutionReport(PipelineConfig pipeline)`
- [X] T009 [US2] Implement `PipelineExecutionServiceImpl` in `src/main/java/com/biopatternsg/application/services/impl/PipelineExecutionServiceImpl.java` encapsulating duration calculations, step metric derivations, and Lucide icon/label mappings
- [X] T010 [US2] Refactor `GetPipelineExecutionUseCase` in `src/main/java/com/biopatternsg/application/usecase/GetPipelineExecutionUseCase.java` to act as a lightweight orchestrator (~50 lines) that validates pipeline existence and delegates report generation to `PipelineExecutionService`
- [X] T011 [P] [US2] Create unit test `PipelineExecutionServiceTest` in `src/test/java/unit/services/PipelineExecutionServiceTest.java` verifying step timeline building, metric extraction, and duration formatting
- [X] T012 [P] [US2] Update unit test `GetPipelineExecutionUseCaseTest` in `src/test/java/unit/usecase/GetPipelineExecutionUseCaseTest.java` verifying pipeline existence check, exception on missing pipeline, and delegation to `PipelineExecutionService`

**Checkpoint**: User Story 2 complete — `GetPipelineExecutionUseCase` is a lightweight orchestrator.

---

## Phase 5: User Story 3 - Orquestación Liviana en Reanudación de Pipelines por Lote (Priority: P3)

**Goal**: Centralize pipeline resumption dispatching and predecessor step resolution in `PipelineStepOrchestrator`, eliminating the 5-parameter method in `ResumePipelineUseCase`.

**Independent Test**: Execute batch resumption and verify candidate pipelines are dispatched via `PipelineStepOrchestrator.resumePipeline(...)` with results accumulated in a batch context of <=3 parameters.

### Implementation for User Story 3

- [X] T013 [P] [US3] Extend `PipelineStepOrchestrator` interface in `src/main/java/com/biopatternsg/application/services/PipelineStepOrchestrator.java` declaring `void resumePipeline(PipelineConfig pipeline, String userId, MicroService microService)`
- [X] T014 [US3] Implement `resumePipeline` in `src/main/java/com/biopatternsg/application/services/impl/PipelineStepOrchestratorImpl.java` encapsulating predecessor step checks and external port invocations for `SEARCH_BIOLOGICAL_OBJECTS` and `PUBMED_INTEGRATION`
- [X] T015 [US3] Refactor `ResumePipelineUseCase` in `src/main/java/com/biopatternsg/application/usecase/ResumePipelineUseCase.java` to eliminate the 5-parameter `processPipeline` method using an internal `PipelineResumptionBatch` accumulator and delegating single-pipeline resumption to `PipelineStepOrchestrator`
- [X] T016 [P] [US3] Update unit tests in `src/test/java/unit/services/PipelineStepOrchestratorTest.java` verifying `resumePipeline` execution and exception handling for biological and PubMed steps
- [X] T017 [P] [US3] Update unit tests in `src/test/java/unit/usecase/ResumePipelineUseCaseTest.java` validating batch coordination, user resolution, and summary tallies without methods exceeding 3 parameters

**Checkpoint**: User Story 3 complete — batch resumption orchestration is lightweight and parameter-compliant.

---

## Phase 6: User Story 4 - Delegación de Reglas de Transición y Métricas en Actualización de Pipelines (Priority: P4)

**Goal**: Delegate configuration completion evaluation and aligned expert objects metric construction from `UpdatePipelineUseCase` to `PipelineConfig` domain invariants.

**Independent Test**: Update pipeline configuration and aligned objects, verifying that `UpdatePipelineUseCase` delegates to `PipelineConfig` and persists via `PipelineRepository`.

### Implementation for User Story 4

- [X] T018 [US4] Refactor `UpdatePipelineUseCase` in `src/main/java/com/biopatternsg/application/usecase/UpdatePipelineUseCase.java` to delegate CONFIG completeness checking and aligned expert objects confirmation to `PipelineConfig` domain methods, saving via `PipelineRepository`
- [X] T019 [P] [US4] Create unit test `UpdatePipelineUseCaseTest` in `src/test/java/unit/usecase/UpdatePipelineUseCaseTest.java` verifying lightweight delegation across description, transcription factors, aligned expert objects, and search config updates

**Checkpoint**: User Story 4 complete — `UpdatePipelineUseCase` is a lightweight orchestrator delegating to rich domain invariants.

---

## Phase 7: Polish, Documentation Synchronization & Architecture Compliance

**Purpose**: Mandatory use case documentation synchronization (Principle IV) and full regression verification

- [X] T020 [P] Synchronize documentation for `GetPipelineExecutionUseCase` in `.agents/use_cases/GetPipelineExecutionUseCase.md` including intent, ports, updated flow, and sequence diagram
- [X] T021 [P] Synchronize documentation for `ResumePipelineUseCase` in `.agents/use_cases/ResumePipelineUseCase.md` including intent, ports, updated flow, and sequence diagram
- [X] T022 [P] Synchronize documentation for `UpdatePipelineUseCase` in `.agents/use_cases/UpdatePipelineUseCase.md` including intent, ports, updated flow, and sequence diagram
- [X] T023 Run static parameter check verifying zero methods in `application.usecase.*` and `application.services.*` accept more than 3 parameters
- [X] T024 Execute ArchUnit test suite using `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -Dtest=ArchitectureLayerTest` in `src/test/java/archtest/ArchitectureLayerTest.java` verifying 7/7 rules passing with 0 violations
- [X] T025 Execute full project test suite using `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test` validating end-to-end functionality across all tests

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Can start immediately (T001).
- **Foundational (Phase 2)**: Depends on Phase 1 (T002, T003 establish domain invariants).
- **User Story 1 (Phase 3 - US1)**: Depends on Phase 1; establishes `PipelineService` parameter encapsulation.
- **User Story 2 (Phase 4 - US2)**: Depends on Phase 1 and Phase 2; extracts `PipelineExecutionService`.
- **User Story 3 (Phase 5 - US3)**: Depends on Phase 1 and Phase 3; extends `PipelineStepOrchestrator` with resumption.
- **User Story 4 (Phase 6 - US4)**: Depends on Phase 2 (`PipelineConfig` domain invariants).
- **Polish (Phase 7)**: Depends on all user stories (US1–US4) being complete.

### Parallel Opportunities

- **Within Foundational**: T002 (`PipelineConfig`) and T003 (`PipelineConfigTest`) can run in parallel.
- **Within US1**: T004 (`PipelineService` interface), T006 (`PipelineStepOrchestratorImpl`), and T007 (`PipelineServiceTest`) can run in parallel once T004 is defined.
- **Within US2**: T008 (`PipelineExecutionService` interface), T011 (`PipelineExecutionServiceTest`), and T012 (`GetPipelineExecutionUseCaseTest`) can run in parallel.
- **Within US3**: T013 (`PipelineStepOrchestrator` interface), T016 (`PipelineStepOrchestratorTest`), and T017 (`ResumePipelineUseCaseTest`) can run in parallel.
- **Within US4**: T018 (`UpdatePipelineUseCase`) and T019 (`UpdatePipelineUseCaseTest`) can be authored together.
- **Within Polish**: T020, T021, and T022 (documentation synchronization) can run completely in parallel.

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 & 2 (Setup and Domain Invariants).
2. Complete Phase 3 (US1: Parameter Encapsulation in `PipelineService`).
3. Validate: Zero methods in `PipelineService` exceed 3 parameters, unit tests pass.

### Incremental Delivery

1. Foundation ready (Phase 1 & 2).
2. Deliver US1 (Parameter Encapsulation) → Verified (MVP).
3. Deliver US2 (Lightweight `GetPipelineExecutionUseCase` & `PipelineExecutionService`) → Verified.
4. Deliver US3 (Lightweight `ResumePipelineUseCase` & `PipelineStepOrchestrator.resumePipeline`) → Verified.
5. Deliver US4 (Lightweight `UpdatePipelineUseCase` & Domain Invariants) → Verified.
6. Documentation Synchronization and Full Architecture Regression (Phase 7).
