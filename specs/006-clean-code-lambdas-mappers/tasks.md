# Tasks: Clean Code, Lambda Purity, and Static Mappers Standardization

**Input**: Design documents from `/specs/006-clean-code-lambdas-mappers/`
**Prerequisites**: [plan.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/config-and-control/specs/006-clean-code-lambdas-mappers/plan.md), [spec.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/config-and-control/specs/006-clean-code-lambdas-mappers/spec.md), [research.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/config-and-control/specs/006-clean-code-lambdas-mappers/research.md), [data-model.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/config-and-control/specs/006-clean-code-lambdas-mappers/data-model.md), [contracts/](file:///home/yacson/Proyectos/biopatternsg-ms/services/config-and-control/specs/006-clean-code-lambdas-mappers/contracts/), [quickstart.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/config-and-control/specs/006-clean-code-lambdas-mappers/quickstart.md)

**Tests**: Unit tests for mappers and ArchUnit architecture tests are included to enforce quality gates and prevent regression.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3, US4)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Verification of build environment and toolchain prerequisites

- [X] T001 Verify Java 21 LTS environment and compile cleanly using `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 PATH=/usr/lib/jvm/java-21-openjdk-amd64/bin:$PATH ./mvnw compile`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establishing unit tests and test fixtures for static utility mappers

**⚠️ CRITICAL**: Test fixtures must be established prior to mapper refactoring

- [X] T002 Implement unit tests in `src/test/java/unit/mappers/PipelineMapperTest.java` verifying `symbolsToExpertObjects` contract (empty list on null, symbol mapping) and reflection constructor check
- [X] T003 [P] Implement unit tests in `src/test/java/unit/mappers/NetworkMapperTest.java` for static utility enforcement (reflection private constructor throws `UnsupportedOperationException`), null-safety, and canonical aliases
- [X] T004 [P] Implement unit tests in `src/test/java/unit/mappers/UserMapperTest.java` for static utility enforcement (reflection private constructor throws `UnsupportedOperationException`), null-safety, and canonical mapping methods

**Checkpoint**: Foundational test scaffolding ready - user story implementation can begin.

---

## Phase 3: User Story 1 - Lambda Purity and Extraction of Multi-line Helper Methods (Priority: P1) 🎯 MVP

**Goal**: Eliminate all imperative multi-statement block lambdas (`-> { ... }`) in functional streams and Optionals across domain, application, and infrastructure layers.

**Independent Test**: Static grep check `grep -rn -- "-> {" src/main/java | grep -v "case "` returns zero results; all unit tests pass cleanly.

### Implementation for User Story 1

- [X] T005 [US1] Implement pure static method `symbolsToExpertObjects(List<String> symbols)` with null-safety in `src/main/java/com/biopatternsg/infrastructure/adapters/mappers/PipelineMapper.java`
- [X] T006 [P] [US1] Refactor `PubmedIntegrationAdapter.executeGenerateAlignedObjects` in `src/main/java/com/biopatternsg/infrastructure/adapters/out/repositories/PubmedIntegrationAdapter.java` to use `PipelineMapper.symbolsToExpertObjects` instead of inline block lambda
- [X] T007 [P] [US1] Extract private helper method `updateExistingStatusMetrics(PipelineStatus ps, Map<String, String> metrics)` in `src/main/java/com/biopatternsg/domain/models/PipelineConfig.java` to replace block lambda in `addStatusForStep`
- [X] T008 [P] [US1] Extract private helper method `dispatchEmailVerification(String targetId)` in `src/main/java/com/biopatternsg/infrastructure/adapters/in/restcontrollers/UserController.java` to replace block lambda in `handleKeycloakEvent`
- [X] T009 [US1] Run command `grep -rn -- "-> {" src/main/java | grep -v "case "` and verify zero functional block lambdas remain across `src/main/java`

**Checkpoint**: User Story 1 (MVP) is fully functional, pure, and independently validated.

---

## Phase 4: User Story 2 - Standardization of Static Utility Mappers (Priority: P2)

**Goal**: Standardize all mapper classes in `com.biopatternsg.infrastructure.adapters.mappers` to be `public final`, contain private constructors preventing instantiation, provide canonical `<source>To<Target>` nomenclature with delegating aliases, and guarantee null-safety.

**Independent Test**: Execute `NetworkMapperTest`, `PipelineMapperTest`, `UserMapperTest`, and `KeycloakEventMapperTest` validating reflection instantiation prevention, null input handling, and canonical method outputs.

### Implementation for User Story 2

- [X] T010 [P] [US2] Standardize `NetworkMapper` in `src/main/java/com/biopatternsg/infrastructure/adapters/mappers/NetworkMapper.java` with `public final class`, private constructor throwing `UnsupportedOperationException`, canonical methods (`collectionToModel`, `modelToCollection`, `modelToResponse`, `collectionToModelList`, `modelToResponseList`), delegating backward-compatible aliases, and null safety
- [X] T011 [P] [US2] Standardize `PipelineMapper` in `src/main/java/com/biopatternsg/infrastructure/adapters/mappers/PipelineMapper.java` with `public final class`, private constructor throwing `UnsupportedOperationException`, canonical methods (`collectionToModel`, `modelToCollection`, `modelToResponse`, `modelToDetailResponse`, `collectionToModelList`, `modelToResponseList`), delegating backward-compatible aliases, and null safety
- [X] T012 [P] [US2] Standardize `UserMapper` in `src/main/java/com/biopatternsg/infrastructure/adapters/mappers/UserMapper.java` with `public final class`, private constructor throwing `UnsupportedOperationException`, canonical methods (`collectionToModel`, `modelToCollection`, `modelToResponse`, `collectionToModelList`, `modelToResponseList`), and null safety
- [X] T013 [P] [US2] Standardize `KeycloakEventMapper` in `src/main/java/com/biopatternsg/infrastructure/adapters/mappers/KeycloakEventMapper.java` ensuring private constructor throws `UnsupportedOperationException` and methods remain pure static
- [X] T014 [US2] Run mapper unit test suite via `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 PATH=/usr/lib/jvm/java-21-openjdk-amd64/bin:$PATH ./mvnw test -Dtest=NetworkMapperTest,PipelineMapperTest,UserMapperTest,KeycloakEventMapperTest`

**Checkpoint**: User Story 2 is fully implemented and passes all unit tests.

---

## Phase 5: User Story 3 - Repository Adapter Collection Transformation Standardization (Priority: P3)

**Goal**: Centralize database-to-domain report transformations into dedicated `toDomainReport` static methods in mappers and eliminate duplicated inline stream mapping in repository adapters.

**Independent Test**: Invoke `findByFilters` / `list` in repository adapters and confirm paginated domain `ReportFormat<T>` returns expected counts and converted lists.

### Implementation for User Story 3

- [X] T015 [US3] Implement `public static ReportFormat<NetworkConfig> toDomainReport(ReportFormat<NetworkCollection> reportCollection)` with null-safety in `src/main/java/com/biopatternsg/infrastructure/adapters/mappers/NetworkMapper.java`
- [X] T016 [P] [US3] Implement `public static ReportFormat<PipelineConfig> toDomainReport(ReportFormat<PipelineCollection> reportCollection)` with null-safety in `src/main/java/com/biopatternsg/infrastructure/adapters/mappers/PipelineMapper.java`
- [X] T017 [P] [US3] Implement `public static ReportFormat<UserConfig> toDomainReport(ReportFormat<UserCollection> reportCollection)` with null-safety in `src/main/java/com/biopatternsg/infrastructure/adapters/mappers/UserMapper.java`
- [X] T018 [US3] Refactor `findByFilters` in `src/main/java/com/biopatternsg/infrastructure/adapters/out/repositories/NetworkRepositoryAdapter.java` to delegate report conversion to `NetworkMapper.toDomainReport`
- [X] T019 [P] [US3] Refactor `findByFilters` in `src/main/java/com/biopatternsg/infrastructure/adapters/out/repositories/PipelineRepositoryAdapter.java` to delegate report conversion to `PipelineMapper.toDomainReport`
- [X] T020 [P] [US3] Refactor `list` in `src/main/java/com/biopatternsg/infrastructure/adapters/out/repositories/UserRepositoryAdapter.java` to delegate report conversion to `UserMapper.toDomainReport`
- [X] T021 [US3] Add unit tests for `toDomainReport` in `NetworkMapperTest`, `PipelineMapperTest`, and `UserMapperTest` validating null collection handling and report count preservation

**Checkpoint**: User Story 3 is complete and repository adapters have zero inline report stream mapping.

---

## Phase 6: User Story 4 - Automated Architecture Quality Gates for Static Mappers (Priority: P4)

**Goal**: Implement ArchUnit rules verifying that all mapper classes are static utilities with private constructors, static methods, and zero CDI bean annotations.

**Independent Test**: Run `ArchitectureLayerTest` and confirm 100% of architectural rules pass with zero violations.

### Implementation for User Story 4

- [X] T022 [US4] Add `staticMappersNamingAndLocationTest` in `src/test/java/archtest/ArchitectureLayerTest.java` verifying that all classes in `..infrastructure.adapters.mappers..` end with `Mapper`
- [X] T023 [US4] Add `staticMappersShouldBeUtilityClassesTest` in `src/test/java/archtest/ArchitectureLayerTest.java` verifying that classes in `..infrastructure.adapters.mappers..` are `final`, have no public constructors, and have only `static` methods
- [X] T024 [US4] Add `staticMappersShouldNotHaveCDIAnnotationsTest` in `src/test/java/archtest/ArchitectureLayerTest.java` verifying that no class in `..infrastructure.adapters.mappers..` has `@ApplicationScoped`, `@Singleton`, `@RequestScoped`, or `@Dependent`
- [X] T025 [US4] Execute ArchUnit tests via `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 PATH=/usr/lib/jvm/java-21-openjdk-amd64/bin:$PATH ./mvnw test -Dtest=ArchitectureLayerTest` verifying all new and existing architecture rules pass

**Checkpoint**: User Story 4 is complete and quality gates automatically protect the mappers layer.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Full regression testing, documentation review, and final quality checks

- [X] T026 [P] Verify no use case contracts or orchestration signatures were modified, confirming no updates needed in `.agents/use_cases/`
- [X] T027 Execute full test suite via `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 PATH=/usr/lib/jvm/java-21-openjdk-amd64/bin:$PATH ./mvnw clean test` verifying 100% passing tests
- [X] T028 Validate all scenarios from [quickstart.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/config-and-control/specs/006-clean-code-lambdas-mappers/quickstart.md)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - executes immediately.
- **Foundational (Phase 2)**: Depends on Setup (T001) - establishes unit tests prior to refactoring.
- **User Story 1 (Phase 3)**: Depends on Foundational - delivers MVP lambda purity.
- **User Story 2 (Phase 4)**: Depends on Foundational - standardizes mapper class declarations and canonical methods.
- **User Story 3 (Phase 5)**: Depends on US2 (mapper methods) - refactors repository adapters to use `toDomainReport`.
- **User Story 4 (Phase 6)**: Depends on US2 - validates static mapper purity with ArchUnit.
- **Polish (Phase 7)**: Depends on all user stories (US1-US4) completing.

### Parallel Opportunities

- **Phase 2 (Foundational)**: T003 (`NetworkMapperTest`) and T004 (`UserMapperTest`) can run in parallel with T002 (`PipelineMapperTest`).
- **Phase 3 (US1)**: T006 (`PubmedIntegrationAdapter`), T007 (`PipelineConfig`), and T008 (`UserController`) can run in parallel once T005 (`PipelineMapper.symbolsToExpertObjects`) is implemented.
- **Phase 4 (US2)**: T010 (`NetworkMapper`), T011 (`PipelineMapper`), T012 (`UserMapper`), and T013 (`KeycloakEventMapper`) touch distinct files and can run in parallel.
- **Phase 5 (US3)**: T016 (`PipelineMapper.toDomainReport`) and T017 (`UserMapper.toDomainReport`) can run in parallel with T015 (`NetworkMapper.toDomainReport`). Likewise, T019 (`PipelineRepositoryAdapter`) and T020 (`UserRepositoryAdapter`) can run in parallel with T018 (`NetworkRepositoryAdapter`).

---

## Implementation Strategy

### MVP First (User Story 1 Only)
1. Complete Phase 1 (Setup) and Phase 2 (Foundational tests).
2. Complete Phase 3 (User Story 1 - Lambda Purity).
3. Validate with `grep -rn -- "-> {" src/main/java | grep -v "case "` and run unit tests.
4. MVP achieved: Zero dirty block lambdas in the microservice!

### Incremental Delivery
1. Foundation & Test scaffolding ready (Phases 1 & 2).
2. Deliver US1 (Lambda Purity & Helper extraction) -> Validate cleanly.
3. Deliver US2 (Standardization of static mappers & canonical naming) -> Validate with mapper unit tests.
4. Deliver US3 (Centralize repository report transformations in `toDomainReport`) -> Validate repository adapters.
5. Deliver US4 (ArchUnit Quality Gates) -> Lock in architectural purity.
6. Full regression check (Phase 7).
