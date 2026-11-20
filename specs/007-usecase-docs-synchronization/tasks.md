# Tasks: Use Case Documentation Synchronization

**Input**: Design documents from `/specs/007-usecase-docs-synchronization/`
**Prerequisites**: [plan.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/config-and-control/specs/007-usecase-docs-synchronization/plan.md), [spec.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/config-and-control/specs/007-usecase-docs-synchronization/spec.md), [research.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/config-and-control/specs/007-usecase-docs-synchronization/research.md), [data-model.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/config-and-control/specs/007-usecase-docs-synchronization/data-model.md), [contracts/](file:///home/yacson/Proyectos/biopatternsg-ms/services/config-and-control/specs/007-usecase-docs-synchronization/contracts/), [quickstart.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/config-and-control/specs/007-usecase-docs-synchronization/quickstart.md)

**Tests**: Dedicated JUnit 5 governance test suite `DocumentationGovernanceTest.java` enforces documentation existence and indexing permanently.

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

**Purpose**: Inventory and baseline verification of use cases

**⚠️ CRITICAL**: Must confirm full list of use cases in code prior to documentation updates

- [X] T002 Inventory all 13 Use Case classes in `src/main/java/com/biopatternsg/application/usecase/` and cross-check against `.agents/use_cases/`

**Checkpoint**: Foundational inventory complete - user story execution can begin.

---

## Phase 3: User Story 1 - Comprehensive Documentation for Undocumented and Modified Use Cases (Priority: P1) 🎯 MVP

**Goal**: Ensure all 13 concrete Use Cases have authoritative, complete markdown documentation files conforming to Constitution Principle IV.

**Independent Test**: Verify that 13 out of 13 Use Case classes have corresponding non-empty `<UseCaseName>.md` files in `.agents/use_cases/`.

### Implementation for User Story 1

- [X] T003 [US1] Verify and refine `RegenerateAlignedObjectsUseCase.md` in `.agents/use_cases/RegenerateAlignedObjectsUseCase.md` with complete 5-part structure and Mermaid sequence diagram
- [X] T004 [P] [US1] Verify and refine `ResumePipelineUseCase.md` in `.agents/use_cases/ResumePipelineUseCase.md` with complete 5-part structure, batch resilience details, and Mermaid sequence diagram
- [X] T005 [P] [US1] Audit the remaining 11 use case documentation files in `.agents/use_cases/` to ensure all 13 adhere to the standard 5-part template
- [X] T006 [US1] Run parity count verification confirming exactly 13 `.java` use case classes match 13 `.md` files in `.agents/use_cases/`

**Checkpoint**: User Story 1 (MVP) is fully functional and all 13 Use Cases are documented.

---

## Phase 4: User Story 2 - Discrepancy Resolution & Delivery Layer Endpoint Alignment (Priority: P2)

**Goal**: Align delivery endpoints, payloads, HTTP status codes, and outbound calls in use case documentation with actual controller implementations.

**Independent Test**: Cross-reference `LaunchPipelineUseCase.md` with `PipelineController.java` confirming endpoint is `POST /config-and-control/pipelines/launch`, request body is `LaunchPipelineRequest`, and response is `202 Accepted`.

### Implementation for User Story 2

- [X] T007 [US2] Update `LaunchPipelineUseCase.md` in `.agents/use_cases/LaunchPipelineUseCase.md` aligning controller endpoint `POST /config-and-control/pipelines/launch`, request body `LaunchPipelineRequest`, HTTP `202 Accepted ("Pipeline launched")`, and outbound call `BiologicalObjectRepository.launch(pipeline)`
- [X] T008 [P] [US2] Cross-reference controller endpoints in all other 12 `.agents/use_cases/*.md` files against their corresponding REST controllers in `src/main/java/com/biopatternsg/infrastructure/adapters/in/restcontrollers/`

**Checkpoint**: User Story 2 is complete with zero delivery layer discrepancies.

---

## Phase 5: User Story 3 - Central Architecture Index Synchronization & Encoding Normalization (Priority: P3)

**Goal**: Normalize `.agents/architecture.md` Section 5 table indexing all 13 Use Cases and repair all corrupted UTF-8 mojibake characters.

**Independent Test**: Run `grep -rnE "(Ã³|Ãº|Ã¡|Ã©|Ã­|Ã±|â”)" .agents/architecture.md` returning 0 matches, and verify all 13 use cases are indexed in Section 5 with valid relative links.

### Implementation for User Story 3

- [X] T009 [US3] Re-encode `.agents/architecture.md` in `.agents/architecture.md` into valid UTF-8, eliminating all mojibake characters and corrupted box-drawing symbols
- [X] T010 [US3] Update Section 5 (*Casos de Uso Principales*) table in `.agents/architecture.md` indexing all 13 Use Cases with uniform backtick formatting and valid relative links to `use_cases/<UseCaseName>.md`
- [X] T011 [US3] Verify zero mojibake artifacts remain in `.agents/architecture.md` using `grep -rnE "(Ã³|Ãº|Ã¡|Ã©|Ã­|Ã±|â”)" .agents/architecture.md`

**Checkpoint**: User Story 3 is complete with pristine markdown and synchronized architecture table.

---

## Phase 6: User Story 4 - Automated Quality Gate for Use Case Documentation (Priority: P4)

**Goal**: Implement an automated JUnit 5 / ArchUnit governance test class ensuring every use case class has an existing markdown file in `.agents/use_cases/` and is indexed in `.agents/architecture.md`.

**Independent Test**: Execute `DocumentationGovernanceTest` and confirm 100% of tests pass.

### Implementation for User Story 4

- [X] T012 [US4] Create `DocumentationGovernanceTest.java` in `src/test/java/archtest/DocumentationGovernanceTest.java` importing `com.biopatternsg.application.usecase..` using ArchUnit `ClassFileImporter`
- [X] T013 [US4] Implement `everyUseCaseMustHaveDocumentationFileTest` in `src/test/java/archtest/DocumentationGovernanceTest.java` asserting `.agents/use_cases/<SimpleName>.md` exists and is non-empty
- [X] T014 [US4] Implement `everyUseCaseMustBeIndexedInArchitectureDocTest` in `src/test/java/archtest/DocumentationGovernanceTest.java` asserting `.agents/architecture.md` indexes every use case
- [X] T015 [US4] Execute governance test via `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 PATH=/usr/lib/jvm/java-21-openjdk-amd64/bin:$PATH ./mvnw test -Dtest=DocumentationGovernanceTest`

**Checkpoint**: User Story 4 is complete and quality gates automatically enforce Constitution Principle IV.

---

## Phase 7: Polish & Regression Testing

**Purpose**: Full regression testing and final verification

- [X] T016 Execute full test suite via `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 PATH=/usr/lib/jvm/java-21-openjdk-amd64/bin:$PATH ./mvnw clean test` verifying 100% passing tests across all layers
- [X] T017 Validate all scenarios in [quickstart.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/config-and-control/specs/007-usecase-docs-synchronization/quickstart.md)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - executes immediately.
- **Foundational (Phase 2)**: Depends on Setup (T001) - validates baseline use case inventory.
- **User Story 1 (Phase 3)**: Depends on Foundational - delivers MVP documentation parity (13/13).
- **User Story 2 (Phase 4)**: Depends on US1 - aligns endpoints and diagrams with controller code.
- **User Story 3 (Phase 5)**: Depends on US1/US2 - cleans encoding and updates Section 5 table in `architecture.md`.
- **User Story 4 (Phase 6)**: Depends on US1-US3 - executes automated test asserting all 13 docs and index links exist.
- **Polish (Phase 7)**: Depends on US1-US4 completing.

### Parallel Opportunities

- **Phase 3 (US1)**: T004 (`ResumePipelineUseCase.md`) and T005 (Audit remaining 11 docs) can run in parallel with T003 (`RegenerateAlignedObjectsUseCase.md`).
- **Phase 4 (US2)**: T008 (cross-reference other 12 docs) can run in parallel with T007 (`LaunchPipelineUseCase.md`).
- **Phase 6 (US4)**: Test method implementations (T013, T014) can be authored together in `DocumentationGovernanceTest.java`.

---

## Implementation Strategy

### MVP First (User Story 1 Only)
1. Complete Phase 1 (Setup) and Phase 2 (Foundational inventory).
2. Complete Phase 3 (User Story 1 - Documentation parity 13/13).
3. Validate parity via file count and inspection.
4. MVP achieved: 100% of use cases documented!

### Incremental Delivery
1. Foundation & Inventory baseline (Phases 1 & 2).
2. Deliver US1 (Full parity for 13 use cases).
3. Deliver US2 (Endpoint and payload alignment in flow diagrams).
4. Deliver US3 (Architecture index table synchronization & UTF-8 mojibake repair).
5. Deliver US4 (Automated quality gate with `DocumentationGovernanceTest`).
6. Full regression verification (Phase 7).
