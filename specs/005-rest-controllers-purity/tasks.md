# Tasks: REST Controllers Purity and Delivery Layer Compliance

**Input**: Design documents from `specs/005-rest-controllers-purity/`
**Prerequisites**: [plan.md](./plan.md) (required), [spec.md](./spec.md) (required), [research.md](./research.md), [data-model.md](./data-model.md), [contracts/](./contracts/), [quickstart.md](./quickstart.md)

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `- [ ] [ID] [P?] [Story] Description with file path`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., `[US1]`, `[US2]`, `[US3]`, `[US4]`)
- Clear action and exact file path in each description

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Verify runtime environment and inspect current controller contracts

- [X] T001 Verify runtime environment and Java 21 LTS toolchain in `pom.xml` and `./mvnw`
- [X] T002 [P] Inspect existing REST controllers and OpenAPI annotations across `src/main/java/com/biopatternsg/infrastructure/adapters/in/restcontrollers/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Foundational DTOs and static mappers required across multiple user stories

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T003 [P] Create detailed pipeline response DTO `PipelineDetailResponse` in `src/main/java/com/biopatternsg/infrastructure/dtos/PipelineDetailResponse.java` with fields `id`, `name`, `description`, `networkId`, `levels`, `step`, `statuses`, `status`, `expertObjects`, `alignedExpertObjects`, `transcriptionFactorConfig`, `useOnlyPrincipalName`, `createdAt`, `retMax`, `maxComplexes`
- [X] T004 [P] Create stateless Keycloak webhook event mapper `KeycloakEventMapper` in `src/main/java/com/biopatternsg/infrastructure/adapters/mappers/KeycloakEventMapper.java` with static method `extractEmailVerificationTargetId(KeycloakEventNotification event)`

**Checkpoint**: Foundational DTO and mapper ready — user story implementation can now proceed.

---

## Phase 3: User Story 1 - Delivery Layer DTO Encapsulation and Domain Model Isolation (Priority: P1) 🎯 MVP

**Goal**: Prevent domain model leakage (`NetworkConfig`, `PipelineConfig`) from REST controllers by returning strongly-typed `NetworkResponse`, `PipelineResponse`, and `PipelineDetailResponse` DTOs.

**Independent Test**: Verify via unit tests that all endpoints in `NetworkController` and `PipelineController` return DTO types, and zero controller methods expose `com.biopatternsg.domain.models..` in their public signatures.

### Implementation for User Story 1

- [X] T005 [P] [US1] Implement static mapper method `configToDetailResponse` in `src/main/java/com/biopatternsg/infrastructure/adapters/mappers/PipelineMapper.java` converting `PipelineConfig` domain model to `PipelineDetailResponse` DTO
- [X] T006 [P] [US1] Create unit tests in `src/test/java/unit/restcontrollers/NetworkControllerTest.java` verifying `create`, `update`, and `findList(id)` return `NetworkResponse` instead of `NetworkConfig`
- [X] T007 [US1] Refactor `NetworkController` in `src/main/java/com/biopatternsg/infrastructure/adapters/in/restcontrollers/NetworkController.java` to return `NetworkResponse` in `update` and `findList(id)`, wrap `NetworkResponse` in `create`, and update OpenAPI `@APIResponse` schemas
- [X] T008 [US1] Refactor `PipelineController` in `src/main/java/com/biopatternsg/infrastructure/adapters/in/restcontrollers/PipelineController.java` to return `PipelineDetailResponse` in `create`, `updateDescription`, `updateTranscriptionFactor`, `updateExpertObjects`, `updateSearchConfig`, `updateAlignedExpertObjects`, and `findList(id)`, updating OpenAPI `@APIResponse` schemas
- [X] T009 [US1] Update `PipelineControllerTest` in `src/test/java/unit/restcontrollers/PipelineControllerTest.java` to assert `PipelineDetailResponse` entity and return types across `create`, update, and `findList(id)` endpoints

**Checkpoint**: User Story 1 complete (MVP) — all network and pipeline configuration endpoints return pure delivery DTOs without domain model leakage.

---

## Phase 4: User Story 2 - Elimination of Business Branching and Conditional Logic in Delivery Adapters (Priority: P2)

**Goal**: Eliminate imperative validation branching and webhook payload interpretation from REST controllers, keeping them as declarative 3-line delivery dispatchers.

**Independent Test**: Verify via unit tests that `UserController.handleKeycloakEvent` delegates event parsing to `KeycloakEventMapper`, and `PipelineController.resume` executes cleanly without imperative `if` checks.

### Implementation for User Story 2

- [X] T010 [P] [US2] Create unit test `KeycloakEventMapperTest` in `src/test/java/unit/mappers/KeycloakEventMapperTest.java` validating extraction of `targetId` for `VERIFY_EMAIL` and `CUSTOM_REQUIRED_ACTION` events, and empty optional for non-matching events
- [X] T011 [US2] Refactor `UserController` in `src/main/java/com/biopatternsg/infrastructure/adapters/in/restcontrollers/UserController.java` to delegate webhook event processing to `KeycloakEventMapper.extractEmailVerificationTargetId(event).ifPresent(userManagement::processEmailVerification)` without imperative branching
- [X] T012 [US2] Update `UserControllerTest` in `src/test/java/unit/controllers/UserControllerTest.java` verifying declarative delegation to `UserManagement` and handling of unhandled webhook events
- [X] T013 [US2] Refactor `PipelineController.resume` in `src/main/java/com/biopatternsg/infrastructure/adapters/in/restcontrollers/PipelineController.java` removing the manual `if (request == null || request.microService() == null)` check in favor of declarative `@Valid` bean validation
- [X] T014 [US2] Update `PipelineControllerTest` in `src/test/java/unit/restcontrollers/PipelineControllerTest.java` to verify `resume` execution without manual branching

**Checkpoint**: User Story 2 complete — zero imperative branching logic in `UserController` and `PipelineController.resume`.

---

## Phase 5: User Story 3 - Static Mapper Encapsulation for Collection and Report Serialization (Priority: P3)

**Goal**: Encapsulate paginated `ReportFormat` stream transformations inside static mappers, removing inline stream pipelines from controllers.

**Independent Test**: Verify via unit tests that `AdminController.listUsers`, `NetworkController.findList`, and `PipelineController.findList` delegate report mapping to `UserMapper`, `NetworkMapper`, and `PipelineMapper`.

### Implementation for User Story 3

- [X] T015 [P] [US3] Add static method `toReportResponse(ReportFormat<NetworkConfig> modelFormat)` in `src/main/java/com/biopatternsg/infrastructure/adapters/mappers/NetworkMapper.java`
- [X] T016 [P] [US3] Add static method `toReportResponse(ReportFormat<PipelineConfig> modelFormat)` in `src/main/java/com/biopatternsg/infrastructure/adapters/mappers/PipelineMapper.java`
- [X] T017 [P] [US3] Add static method `toReportResponse(ReportFormat<UserConfig> modelFormat)` in `src/main/java/com/biopatternsg/infrastructure/adapters/mappers/UserMapper.java`
- [X] T018 [US3] Refactor `NetworkController.findList(FindNetworkRequest)` in `src/main/java/com/biopatternsg/infrastructure/adapters/in/restcontrollers/NetworkController.java` to delegate collection transformation to `NetworkMapper.toReportResponse`
- [X] T019 [US3] Refactor `PipelineController.findList(FindPipelineRequest)` in `src/main/java/com/biopatternsg/infrastructure/adapters/in/restcontrollers/PipelineController.java` to delegate collection transformation to `PipelineMapper.toReportResponse`
- [X] T020 [US3] Refactor `AdminController.listUsers(UserFiltersRequest)` in `src/main/java/com/biopatternsg/infrastructure/adapters/in/restcontrollers/AdminController.java` to delegate collection transformation to `UserMapper.toReportResponse`
- [X] T021 [P] [US3] Add unit tests in `src/test/java/unit/restcontrollers/NetworkControllerTest.java` and `src/test/java/unit/restcontrollers/PipelineControllerTest.java` verifying report pagination mapping

**Checkpoint**: User Story 3 complete — collection mappings cleanly encapsulated in static mappers, controllers remain concise single-line dispatchers.

---

## Phase 6: User Story 4 - Architectural Enforcement and Delivery Layer Quality Gates (Priority: P4)

**Goal**: Enforce architectural compliance via automated ArchUnit tests verifying that no controller public method exposes domain models as return types.

**Independent Test**: Run `ArchitectureLayerTest` and confirm that all 8 rules pass with 0 violations.

### Implementation for User Story 4

- [X] T022 [P] [US4] Add ArchUnit test `controllerMethodsShouldNotReturnDomainModelsTest` in `src/test/java/archtest/ArchitectureLayerTest.java` verifying that zero public methods in `infrastructure.adapters.in.restcontrollers..` return types residing in `com.biopatternsg.domain.models..`
- [X] T023 [US4] Execute ArchUnit test suite using `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -Dtest=ArchitectureLayerTest` verifying 8/8 rules pass with 0 violations

**Checkpoint**: User Story 4 complete — quality gate permanently active in CI/CD test suite.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Final verification, OpenAPI synchronization, and regression testing

- [X] T024 Verify OpenAPI annotations across all 4 controllers match DTO classes in `src/main/java/com/biopatternsg/infrastructure/adapters/in/restcontrollers/`
- [X] T025 Execute full regression test suite using `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn clean test` validating zero failures across all tests

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately (T001, T002).
- **Foundational (Phase 2)**: Depends on Phase 1; establishes `PipelineDetailResponse` and `KeycloakEventMapper` (T003, T004).
- **User Story 1 (Phase 3 - US1)**: Depends on Phase 2 (T003 establishes `PipelineDetailResponse`).
- **User Story 2 (Phase 4 - US2)**: Depends on Phase 2 (T004 establishes `KeycloakEventMapper`).
- **User Story 3 (Phase 5 - US3)**: Depends on Phase 1; can proceed in parallel with US1/US2.
- **User Story 4 (Phase 6 - US4)**: Depends on US1 completion (controller return types must be refactored before ArchUnit check passes).
- **Polish (Phase 7)**: Depends on all user stories (US1–US4) being complete.

### Parallel Opportunities

- **Within Foundational**: T003 (`PipelineDetailResponse`) and T004 (`KeycloakEventMapper`) can run completely in parallel.
- **Within US1**: T005 (`PipelineMapper.configToDetailResponse`) and T006 (`NetworkControllerTest`) can run in parallel.
- **Within US2**: T010 (`KeycloakEventMapperTest`) and T013 (`PipelineController.resume`) can run in parallel.
- **Within US3**: T015 (`NetworkMapper`), T016 (`PipelineMapper`), and T017 (`UserMapper`) can run in parallel.
- **Across Stories**: US2 (Keycloak webhook refactor) and US3 (report static mappers) can be executed in parallel with US1.

---

## Parallel Example: User Story 1 & 3

```bash
# Launch DTO and Mapper extensions in parallel:
Task: "Implement static mapper method configToDetailResponse in src/main/java/com/biopatternsg/infrastructure/adapters/mappers/PipelineMapper.java"
Task: "Create unit tests in src/test/java/unit/restcontrollers/NetworkControllerTest.java"
Task: "Add static method toReportResponse in src/main/java/com/biopatternsg/infrastructure/adapters/mappers/NetworkMapper.java"
Task: "Add static method toReportResponse in src/main/java/com/biopatternsg/infrastructure/adapters/mappers/UserMapper.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 & 2 (Setup and Foundational DTOs: `PipelineDetailResponse`, `KeycloakEventMapper`).
2. Complete Phase 3 (US1: DTO encapsulation in `NetworkController` and `PipelineController`).
3. Validate: Zero controller endpoints expose `NetworkConfig` or `PipelineConfig`, unit tests pass.

### Incremental Delivery

1. Foundation ready (Phase 1 & 2).
2. Deliver US1 (DTO Encapsulation & Domain Model Isolation) → Verified (MVP).
3. Deliver US2 (Elimination of Imperative Branching & Webhook Parsing) → Verified.
4. Deliver US3 (Encapsulated Paginated Report Mappers) → Verified.
5. Deliver US4 (Automated ArchUnit Quality Gate) → Verified.
6. Polish & Full Regression Testing (Phase 7).
