# Tasks: Domain Layer Purity and Framework Decoupling

**Input**: Design documents from `specs/002-domain-layer-purity/`
- Spec: [spec.md](./spec.md)
- Plan: [plan.md](./plan.md)
- Research: [research.md](./research.md)
- Data Model: [data-model.md](./data-model.md)
- Contracts: [keycloak-repository-contract.md](./contracts/keycloak-repository-contract.md), [pipeline-service-contract.md](./contracts/pipeline-service-contract.md)
- Quickstart: [quickstart.md](./quickstart.md)

**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project build environment configuration

- [X] T001 Verify Java 21 runtime (`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`) and compiler compatibility in `pom.xml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Baseline verification prior to architectural modifications

**⚠️ CRITICAL**: Establish baseline test execution before refactoring begins

- [X] T002 Execute baseline architecture tests using `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw test -Dtest=ArchitectureLayerTest` in `src/test/java/archtest/ArchitectureLayerTest.java`

**Checkpoint**: Build and test infrastructure verified - user story implementation can begin

---

## Phase 3: User Story 1 - Pure Domain Port Contracts for Identity Management (Priority: P1) 🎯 MVP

**Goal**: Decouple `KeycloakRepository` from `jakarta.ws.rs.core.Response`, return pure `String identityProviderId`, encapsulate HTTP header extraction in `KeycloakAdapter`, and adapt `UserManagementUseCase`.

**Independent Test**: Verify that `KeycloakRepository.java` contains zero imports of `jakarta.ws.rs.*`, `KeycloakAdapter.register` extracts the user ID internally, and `UserManagementUseCase.register` receives `String userId` directly.

### Implementation for User Story 1

- [X] T003 [P] [US1] Remove `import jakarta.ws.rs.core.Response;` and update `register(UserConfig newUser)` to return `String` in `src/main/java/com/biopatternsg/domain/port/out/repositories/KeycloakRepository.java`
- [X] T004 [US1] Refactor `KeycloakAdapter.register(UserConfig userConfig)` to return `String`, encapsulate HTTP 201 verification and `response.getLocation().getPath()` parsing, and return the extracted `userId` in `src/main/java/com/biopatternsg/infrastructure/adapters/out/repositories/KeycloakAdapter.java`
- [X] T005 [US1] Refactor `UserManagementUseCase.register(UserConfig userConfig)` to assign `userConfig.setIdentityProviderId(keycloakRepository.register(userConfig))` directly without URI parsing in `src/main/java/com/biopatternsg/application/usecase/UserManagementUseCase.java`
- [X] T006 [P] [US1] Create unit test `UserManagementUseCaseTest` verifying orchestration with the refactored `String` return type in `src/test/java/unit/usecase/UserManagementUseCaseTest.java`

**Checkpoint**: User Story 1 complete — domain port is decoupled from Jakarta REST and independently testable.

---

## Phase 4: User Story 2 - Elimination of Forbidden Domain Services Package (Priority: P2)

**Goal**: Relocate `PipelineService` interface to `com.biopatternsg.application.services`, fix `PipelineServiceImpl` package declaration, eradicate `domain/services` directory, and update all calling sites.

**Independent Test**: Verify that `src/main/java/com/biopatternsg/domain/services` does not exist on disk, `PipelineService` and `PipelineServiceImpl` belong to `com.biopatternsg.application.services`, and all references compile cleanly.

### Implementation for User Story 2

- [X] T007 [P] [US2] Relocate `PipelineService.java` to `src/main/java/com/biopatternsg/application/services/PipelineService.java` with package `package com.biopatternsg.application.services;`
- [X] T008 [P] [US2] Correct package declaration to `package com.biopatternsg.application.services;` in `src/main/java/com/biopatternsg/application/services/PipelineServiceImpl.java`
- [X] T009 [US2] Delete directory `src/main/java/com/biopatternsg/domain/services/` from the filesystem
- [X] T010 [P] [US2] Update `PipelineService` imports in application use cases in `src/main/java/com/biopatternsg/application/services/PipelineStepOrchestrator.java`, `src/main/java/com/biopatternsg/application/usecase/UpdatePipelineStepUseCase.java`, and `src/main/java/com/biopatternsg/application/usecase/RegenerateAlignedObjectsUseCase.java`
- [X] T011 [P] [US2] Update `PipelineService` imports in outbound repository adapters in `src/main/java/com/biopatternsg/infrastructure/adapters/out/repositories/PubmedIntegrationAdapter.java`, `src/main/java/com/biopatternsg/infrastructure/adapters/out/repositories/InferencesAdapter.java`, and `src/main/java/com/biopatternsg/infrastructure/adapters/out/repositories/BiologicalObjectAdapter.java`
- [X] T012 [P] [US2] Update `PipelineService` imports in unit test files `src/test/java/unit/adapters/InferencesAdapterTest.java` and `src/test/java/unit/services/PipelineStepOrchestratorTest.java`

**Checkpoint**: User Story 2 complete — `domain.services` package is eradicated and `PipelineService` lives in `application.services`.

---

## Phase 5: User Story 3 - Automated Hexagonal Architecture Verification Gate (Priority: P3)

**Goal**: Automatically verify that the domain layer has zero framework dependencies and that ArchUnit domain isolation passes with zero violations.

**Independent Test**: Execute `ArchitectureLayerTest.domainLayerIsolationTest` and verify zero occurrences of `jakarta.ws.rs` in `domain`.

### Implementation for User Story 3

- [X] T013 [US3] Run static inspection script to verify zero occurrences of `jakarta.ws.rs` across all files in `src/main/java/com/biopatternsg/domain/`
- [X] T014 [US3] Verify that filesystem directory `src/main/java/com/biopatternsg/domain/services` is completely absent
- [X] T015 [US3] Run ArchUnit test suite using `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw test -Dtest=ArchitectureLayerTest#domainLayerIsolationTest` in `src/test/java/archtest/ArchitectureLayerTest.java` ensuring 0 violations

**Checkpoint**: User Story 3 complete — domain isolation is objectively verified by automated gates.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Documentation synchronization and end-to-end quickstart validation

- [X] T016 [P] Synchronize Use Case documentation and sequence diagram for `UserManagementUseCase` in `.agents/use_cases/UserManagementUseCase.md` per Constitution Principle IV
- [X] T017 Execute full quickstart validation guide (`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw clean test`) per `specs/002-domain-layer-purity/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Can start immediately (T001).
- **Foundational (Phase 2)**: Depends on Phase 1 (T002 verifies baseline build).
- **User Story 1 (Phase 3 - US1)**: Depends on Phase 2; can proceed independently.
- **User Story 2 (Phase 4 - US2)**: Depends on Phase 2; operates on pipeline service files independent of US1.
- **User Story 3 (Phase 5 - US3)**: Depends on completion of US1 and US2 (verifies combined domain purity).
- **Polish (Phase 6)**: Runs after all user stories are complete.

### Parallel Opportunities

- **Within US1**: T003 (`KeycloakRepository`) and T006 (`UserManagementUseCaseTest`) can be prepared in parallel.
- **Within US2**: T007 (`PipelineService`), T008 (`PipelineServiceImpl`), T010 (application imports), T011 (adapter imports), and T012 (test imports) touch distinct files and can run in parallel.
- **Across Stories**: US1 (Keycloak) and US2 (PipelineService) touch completely separate sets of files and can be executed concurrently.

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 & 2 (Setup and baseline verification).
2. Complete Phase 3 (US1 - `KeycloakRepository` decoupling).
3. Validate: `UserManagementUseCase` compiles and functions with pure domain types.

### Incremental Delivery

1. Foundation ready (Phase 1 & 2).
2. Implement US1 (Identity port decoupling) → Verified (MVP).
3. Implement US2 (Eradicate `domain.services`) → Verified.
4. Run US3 (ArchUnit verification gate) → Verified.
5. Polish docs & quickstart validation (Phase 6).
