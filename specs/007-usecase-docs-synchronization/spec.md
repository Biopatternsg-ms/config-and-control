# Feature Specification: Use Case Documentation Synchronization

**Feature Branch**: `007-usecase-docs-synchronization`

**Created**: 2026-11-20

**Status**: Draft

**Input**: User description: "/speckit-specify basado en /home/yacson/Proyectos/biopatternsg-ms/SDD/config-and-control/06-use-case-documentation-synchronization"

## Clarifications

### Session 2026-11-20

- Q: ¿Dónde debe residir la prueba automatizada que valida que el 100% de los casos de uso cuenten con su archivo de documentación en `.agents/use_cases/`? → A: Opción B (En una clase de prueba de gobernanza dedicada `src/test/java/archtest/DocumentationGovernanceTest.java`, aislando la inspección de archivos markdown del análisis de capas de código de ArchUnit).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Comprehensive Documentation for Undocumented and Modified Use Cases (Priority: P1)

As an AI Agent, Core Developer, or System Architect, I need every concrete Use Case implementation in `com.biopatternsg.application.usecase` to have an authoritative, comprehensive markdown document under `.agents/use_cases/<UseCaseName>.md` detailing its business purpose, inbound/outbound ports, domain services, step-by-step execution flow, error cases, and Mermaid sequence diagrams, so that autonomous agents and human developers can understand, maintain, and safely orchestrate use case workflows without reverse-engineering source code.

**Why this priority**: Constitution Principle IV (*Mandatory Synchronous Use Case Documentation — NON-NEGOTIABLE*) and Section 1.4 of `.agents/rules.md` mandate that code and architectural documentation must be synchronized in lockstep. Undocumented use cases create blind spots for autonomous maintenance, risk architectural drift, and break knowledge continuity.

**Independent Test**: Can be verified by running a 1-to-1 parity audit between all `*UseCase.java` files under `src/main/java/com/biopatternsg/application/usecase/` and `.md` files in `.agents/use_cases/`, confirming that 13 out of 13 Use Cases possess complete documentation matching the constitutional standard.

**Acceptance Scenarios**:

1. **Given** `RegenerateAlignedObjectsUseCase.java`, **When** inspecting `.agents/use_cases/RegenerateAlignedObjectsUseCase.md`, **Then** the document clearly details the business problem (resetting `UPDATE_ALIGNED_OBJECTS` step to `PENDING` and re-triggering generation in `pubmed-integration`), ports involved (`RegenerateAlignedObjects`, `PipelineRepository`, `TriggerPubmedIntegration`), domain services (`PipelineService`), step-by-step flow, error cases (`UnprocessableEntityException`), and a valid Mermaid sequence diagram.
2. **Given** `ResumePipelineUseCase.java`, **When** inspecting `.agents/use_cases/ResumePipelineUseCase.md`, **Then** the document clearly details the self-healing batch resumption flow across candidate steps for `SEARCH_BIOLOGICAL_OBJECTS` and `PUBMED_INTEGRATION`, ports involved (`ResumePipeline`, `PipelineRepository`, `NetworkRepository`), application services (`PipelineStepOrchestrator`), batch processing resilience (`PipelineResumptionBatch`), error handling, and a valid Mermaid sequence diagram.
3. **Given** any of the 13 Use Cases in `src/main/java/com/biopatternsg/application/usecase/`, **When** checking `.agents/use_cases/`, **Then** a corresponding `<UseCaseName>.md` file exists and adheres to the standard 5-part structure (Purpose, Contracts, Step-by-Step Flow, Mermaid Diagram, Error Handling).

---

### User Story 2 - Discrepancy Resolution & Delivery Layer Endpoint Alignment (Priority: P2)

As a Developer or Integration Engineer, I need all existing use case documentation files (specifically `LaunchPipelineUseCase.md` and related pipeline orchestrators) to reflect exact controller endpoints, HTTP methods, payload structures, status codes, and outbound port invocations as implemented in code, eliminating outdated descriptions.

**Why this priority**: Inaccurate documentation (e.g., documenting `POST /api/pipeline/{id}/launch` returning `200 OK` when the actual controller exposes `POST /config-and-control/pipelines/launch` with request body `LaunchPipelineRequest` returning `202 Accepted`) causes integration failures and misleads API consumers and test builders.

**Independent Test**: Can be verified by cross-referencing HTTP endpoints, request bodies, and status codes declared in each `.agents/use_cases/*.md` file against the annotations in corresponding REST controllers (`PipelineController`, `NetworkController`, `UserController`, `AdminController`).

**Acceptance Scenarios**:

1. **Given** `LaunchPipelineUseCase.md`, **When** examining the delivery layer section and Mermaid diagram, **Then** it accurately specifies `POST /config-and-control/pipelines/launch` with `LaunchPipelineRequest` body and `202 Accepted ("Pipeline launched")` response, and specifies outbound invocation to `BiologicalObjectRepository.launch(pipeline)`.
2. **Given** all 13 use case documentation files, **When** validating controller routing paths, **Then** zero references to legacy paths like `/api/pipeline/...` or obsolete status codes remain.

---

### User Story 3 - Central Architecture Index Synchronization & Encoding Normalization (Priority: P3)

As a Software Architect, I need Section 5 (*Casos de Uso Principales*) in `.agents/architecture.md` to index all 13 Use Cases with uniform formatting, concise responsibility summaries, valid relative links, and clean UTF-8 encoding (eliminating mojibake/corrupted characters), providing a reliable catalog of application capabilities.

**Why this priority**: `.agents/architecture.md` serves as the primary system map. Corrupted characters reduce professionalism and readability, while missing table entries prevent agents from locating use case documentation during automated workflows.

**Independent Test**: Can be verified by parsing `.agents/architecture.md`, confirming that all 13 Use Cases appear in Section 5 table, all markdown hyperlinks resolve to existing files under `.agents/use_cases/`, and the file contains zero UTF-8 encoding artifacts (`Ã³`, `â”`, etc.).

**Acceptance Scenarios**:

1. **Given** `.agents/architecture.md`, **When** inspecting the table in Section 5, **Then** exactly 13 rows exist corresponding to the 13 concrete Use Cases, each with backticked names, correct Inbound Port names, concise responsibilities, and functional relative links (`use_cases/<UseCaseName>.md`).
2. **Given** `.agents/architecture.md`, **When** inspecting character encoding, **Then** all Spanish text, tree diagrams, and table headers are cleanly encoded in standard UTF-8 without mojibake artifacts.

---

### User Story 4 - Automated Quality Gate for Use Case Documentation (Priority: P4)

As a Quality Engineer and Governance Lead, I need an automated test in the test suite that inspects all concrete classes in `com.biopatternsg.application.usecase..` and asserts that a corresponding `<UseCaseName>.md` documentation file exists under `.agents/use_cases/`, ensuring that Constitution Principle IV cannot be violated in future pull requests.

**Why this priority**: Manual governance checks are prone to human oversight. An automated test locks in synchronization permanently, failing the build immediately if any new use case is added without its documentation.

**Independent Test**: Can be verified by executing `DocumentationGovernanceTest` and confirming that the test verifies documentation existence for 100% of discovered use case classes.

**Acceptance Scenarios**:

1. **Given** the test suite in `src/test/java/archtest/DocumentationGovernanceTest.java`, **When** the documentation parity rule runs, **Then** it dynamically identifies all top-level classes in `com.biopatternsg.application.usecase..` and asserts that `.agents/use_cases/<SimpleName>.md` exists on disk.
2. **Given** any future use case added to the codebase without a documentation file, **When** running `mvn test`, **Then** the build fails with a clear assertion message indicating the missing documentation file.

---

### Edge Cases

- What happens if a Use Case has multiple overloaded `execute` methods or helper orchestrators?
  - The documentation file must detail all public entry points defined by its Inbound Port and explain their distinct execution paths.
- What happens if a Use Case delegates to another Use Case or application service?
  - The documentation must explicitly illustrate the boundary in the Mermaid sequence diagram, showing whether the call is synchronous or asynchronous.
- How are relative paths in `.agents/architecture.md` resolved across different viewing tools (GitHub, IDE preview, CLI)?
  - Links use relative project paths (`use_cases/<UseCaseName>.md`) which resolve consistently both in GitHub repository views and local markdown renderers.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Every Use Case class in `src/main/java/com/biopatternsg/application/usecase/` MUST have a dedicated markdown documentation file in `.agents/use_cases/<UseCaseName>.md`.
- **FR-002**: Each use case document MUST include:
  1. Class, Inbound Port, Outbound Ports, and Invoking Controller header metadata.
  2. Purpose and business problem solved.
  3. Step-by-step execution flow.
  4. Complete Mermaid sequence diagram (`sequenceDiagram`) detailing interactions between Controller, Use Case, Repositories, Domain/Application Services, and external clients.
  5. Error cases and domain/infrastructure exceptions thrown.
- **FR-003**: `RegenerateAlignedObjectsUseCase.md` MUST accurately document the resetting of `UPDATE_ALIGNED_OBJECTS` step to `PENDING` and delegated re-triggering of `TriggerPubmedIntegration`.
- **FR-004**: `ResumePipelineUseCase.md` MUST accurately document batch resumption across candidate step partitions for `SEARCH_BIOLOGICAL_OBJECTS` and `PUBMED_INTEGRATION`, user resolution via `NetworkRepository`, and delegated execution to `PipelineStepOrchestrator`.
- **FR-005**: `LaunchPipelineUseCase.md` MUST be updated to align with the actual delivery contract: `POST /config-and-control/pipelines/launch` with `LaunchPipelineRequest(pipelineId)` returning `202 Accepted`, and outbound execution via `BiologicalObjectRepository.launch(pipeline)`.
- **FR-006**: Section 5 of `.agents/architecture.md` MUST be updated to list all 13 Use Cases with uniform formatting, correct Inbound Ports, and valid relative links to `.agents/use_cases/<UseCaseName>.md`.
- **FR-007**: All text in `.agents/architecture.md` and `.agents/use_cases/*.md` MUST be formatted in valid UTF-8, repairing any existing corrupted character encoding.
- **FR-008**: An automated architectural test in `DocumentationGovernanceTest.java` MUST enforce that 100% of classes in `com.biopatternsg.application.usecase..` have an existing `.agents/use_cases/<UseCaseName>.md` documentation file.

### Key Entities

- **Use Case Implementations**: 13 concrete classes implementing inbound ports in `com.biopatternsg.application.usecase`.
- **Inbound Ports**: 13 interface contracts in `com.biopatternsg.domain.port.in`.
- **Use Case Documentation**: 13 markdown files in `.agents/use_cases/<UseCaseName>.md`.
- **Architecture Overview**: Master architectural reference in `.agents/architecture.md`.
- **Quality Gate**: Architectural test in `src/test/java/archtest/DocumentationGovernanceTest.java`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Exactly 13 out of 13 Use Cases in `com.biopatternsg.application.usecase` (100%) have a verified, synchronized markdown document in `.agents/use_cases/`.
- **SC-002**: 100% of use case documentation files include a syntactically valid Mermaid sequence diagram rendering without syntax errors.
- **SC-003**: Section 5 of `.agents/architecture.md` indexes all 13 Use Cases with 0 broken hyperlinks.
- **SC-004**: 0 corrupted encoding artifacts (mojibake) exist in `.agents/architecture.md`.
- **SC-005**: 100% of architecture tests pass, including the new automated use case documentation verification gate.

## Assumptions

- No runtime Java code in use cases or application services needs to be altered; this feature is strictly focused on documentation synchronization, diagram accuracy, architecture catalog normalization, and automated quality gates.
- Documentation language remains Spanish, consistent with the existing documentation in `.agents/`.
