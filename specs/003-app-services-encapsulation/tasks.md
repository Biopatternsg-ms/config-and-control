# Tasks: Application Services Encapsulation and Layer Boundaries

**Input**: Design documents from `specs/003-app-services-encapsulation/`
- Spec: [spec.md](./spec.md)
- Plan: [plan.md](./plan.md)
- Research: [research.md](./research.md)
- Data Model: [data-model.md](./data-model.md)
- Contracts: [pipeline-step-orchestrator-contract.md](./contracts/pipeline-step-orchestrator-contract.md), [outbound-adapter-boundaries.md](./contracts/outbound-adapter-boundaries.md)
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

- [X] T001 Verify project structure and Java 21 environment (`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`) in `pom.xml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Subpackage structure initialization

**⚠️ CRITICAL**: Establish `impl/` subpackage directory before creating implementations

- [X] T002 Create implementation subpackage directory `src/main/java/com/biopatternsg/application/services/impl`

**Checkpoint**: Foundation ready - user story implementation can begin

---

## Phase 3: User Story 1 - Application Services Interface Segregation & Encapsulation (Priority: P1) 🎯 MVP

**Goal**: Segregate `PipelineService` and `PipelineStepOrchestrator` into public interface contracts in `application.services` and hide their implementations in `application.services.impl`.

**Independent Test**: Verify that `com.biopatternsg.application.services` contains only interface files and `impl/` contains `@ApplicationScoped` implementation classes, with use cases injecting only interfaces.

### Implementation for User Story 1

- [X] T003 [P] [US1] Move `PipelineServiceImpl` to `com.biopatternsg.application.services.impl.PipelineServiceImpl` and remove old file in `src/main/java/com/biopatternsg/application/services/impl/PipelineServiceImpl.java`
- [X] T004 [P] [US1] Extract `PipelineStepOrchestrator` public interface contract in `src/main/java/com/biopatternsg/application/services/PipelineStepOrchestrator.java`
- [X] T005 [US1] Implement `PipelineStepOrchestratorImpl` in `src/main/java/com/biopatternsg/application/services/impl/PipelineStepOrchestratorImpl.java`
- [X] T006 [P] [US1] Verify that all use cases inject only interface contracts `PipelineService` and `PipelineStepOrchestrator` in `src/main/java/com/biopatternsg/application/usecase/UpdatePipelineUseCase.java`, `src/main/java/com/biopatternsg/application/usecase/UpdatePipelineStepUseCase.java`, `src/main/java/com/biopatternsg/application/usecase/RegenerateAlignedObjectsUseCase.java`, and `src/main/java/com/biopatternsg/application/usecase/ResumePipelineUseCase.java`

**Checkpoint**: User Story 1 complete — application service contracts and hidden implementations are separated.

---

## Phase 4: User Story 2 - Decoupling Outbound Adapters from Application Services (Priority: P2)

**Goal**: Completely eliminate all dependencies from outbound infrastructure adapters to application services.

**Independent Test**: Verify that zero classes in `infrastructure.adapters.out.repositories` import or inject `PipelineService` or `PipelineStepOrchestrator`.

### Implementation for User Story 2

- [X] T007 [P] [US2] Remove unused `PipelineService` field injection from `src/main/java/com/biopatternsg/infrastructure/adapters/out/repositories/BiologicalObjectAdapter.java`
- [X] T008 [P] [US2] Remove `PipelineService` dependency from `src/main/java/com/biopatternsg/infrastructure/adapters/out/repositories/PubmedIntegrationAdapter.java` and propagate integration exceptions
- [X] T009 [P] [US2] Remove `PipelineService` dependency from `src/main/java/com/biopatternsg/infrastructure/adapters/out/repositories/InferencesAdapter.java` and propagate integration exceptions
- [X] T010 [P] [US2] Refactor unit tests in `src/test/java/unit/adapters/InferencesAdapterTest.java` to verify REST client calls and exception propagation without `PipelineService`

**Checkpoint**: User Story 2 complete — outbound infrastructure adapters are decoupled from application services.

---

## Phase 5: User Story 3 - Centralized Pipeline Step Orchestration & State Transition Management (Priority: P3)

**Goal**: Centralize step lifecycle status management (`IN_PROGRESS`, `FAILED`, `PENDING`) and exception capture in `PipelineStepOrchestratorImpl`.

**Independent Test**: Execute step orchestration tests and verify that `PipelineStepOrchestratorImpl` updates step states around outbound port calls.

### Implementation for User Story 3

- [X] T011 [US3] Implement pre-step `IN_PROGRESS` transitions and exception capture with `FAILED` transitions in `src/main/java/com/biopatternsg/application/services/impl/PipelineStepOrchestratorImpl.java`
- [X] T012 [P] [US3] Update unit tests in `src/test/java/unit/services/PipelineStepOrchestratorTest.java` to test orchestrator step transitions and external port invocations using `PipelineStepOrchestratorImpl`
- [X] T013 [P] [US3] Update `ResumePipelineUseCaseTest` fake orchestrator to implement `PipelineStepOrchestrator` interface in `src/test/java/unit/usecase/ResumePipelineUseCaseTest.java`

**Checkpoint**: User Story 3 complete — workflow progression and step status updates are managed cleanly in application orchestration.

---

## Phase 6: User Story 4 - Automated Hexagonal Layer Boundary Compliance (Priority: P4)

**Goal**: Verify through static inspection and automated ArchUnit tests that the application layer is accessed exclusively by application components.

**Independent Test**: Run `ArchitectureLayerTest` and confirm 7/7 tests pass with zero violations.

### Implementation for User Story 4

- [X] T014 [US4] Run static check verifying zero imports of `com.biopatternsg.application.services` across all classes in `src/main/java/com/biopatternsg/infrastructure/`
- [X] T015 [US4] Execute ArchUnit test suite using `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw test -Dtest=ArchitectureLayerTest` in `src/test/java/archtest/ArchitectureLayerTest.java` verifying 7/7 passing rules with 0 violations

**Checkpoint**: User Story 4 complete — architectural compliance is continuously verified.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Full regression suite validation and code cleanup

- [X] T016 Execute full project test suite (`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw test`) validating end-to-end functionality across all 42 tests
- [X] T017 [P] Verify code style, imports, and packaging consistency across all classes in `src/main/java/`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Can start immediately (T001).
- **Foundational (Phase 2)**: Depends on Phase 1 (T002 creates `impl/`).
- **User Story 1 (Phase 3 - US1)**: Depends on Phase 2.
- **User Story 2 (Phase 4 - US2)**: Can run in parallel with US1 or immediately following.
- **User Story 3 (Phase 5 - US3)**: Depends on US1 and US2 (connects orchestration to decoupled ports).
- **User Story 4 (Phase 6 - US4)**: Depends on US1, US2, and US3 (verifies final layer boundaries).
- **Polish (Phase 7)**: Runs after all user stories complete.

### Parallel Opportunities

- **Within US1**: T003 (`PipelineServiceImpl`), T004 (`PipelineStepOrchestrator` interface), and T006 (use cases) can be verified in parallel.
- **Within US2**: T007 (`BiologicalObjectAdapter`), T008 (`PubmedIntegrationAdapter`), T009 (`InferencesAdapter`), and T010 (`InferencesAdapterTest`) operate on distinct files and can run in parallel.
- **Within US3**: T012 (`PipelineStepOrchestratorTest`) and T013 (`ResumePipelineUseCaseTest`) can run in parallel.

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 & 2 (Setup and directory creation).
2. Complete Phase 3 (US1 - Interface segregation in `application.services`).
3. Validate: Interfaces and implementations cleanly separated.

### Incremental Delivery

1. Foundation ready (Phase 1 & 2).
2. Implement US1 (Interface segregation) → Verified (MVP).
3. Implement US2 (Decouple outbound adapters) → Verified.
4. Implement US3 (Orchestration transitions) → Verified.
5. Implement US4 (ArchUnit verification) → 7/7 passing.
6. Full test regression run (Phase 7).
