# Feature Specification: Application Services Encapsulation and Layer Boundaries

**Feature Branch**: `003-app-services-encapsulation`

**Created**: 2026-11-19

**Status**: Draft

**Input**: User description: "basado en /home/yacson/Proyectos/biopatternsg-ms/SDD/config-and-control/02-application-services-encapsulation-and-layer-boundaries"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Application Services Interface Segregation & Encapsulation (Priority: P1)

As a Software Architect and Core Developer, I need application services (`PipelineService`, `PipelineStepOrchestrator`) to expose strict public interface contracts while hiding concrete implementations in a dedicated `impl/` subpackage, so that consumers across the application layer depend exclusively on service contracts rather than concrete classes.

**Why this priority**: Direct reliance on concrete service classes violates Constitution Principle III (*Application Services Encapsulation — Strict Interface & Hidden Impl*), breaks encapsulation, and increases coupling across orchestration use cases.

**Independent Test**: Can be verified by confirming that all application services define public interfaces at `com.biopatternsg.application.services` and hide their implementations under `com.biopatternsg.application.services.impl`, with all use cases injecting only interface types.

**Acceptance Scenarios**:

1. **Given** an orchestration use case requiring pipeline step management, **When** the use case declares dependencies, **Then** it injects the public interface contract (`PipelineService` or `PipelineStepOrchestrator`) without importing concrete implementation classes.
2. **Given** the source codebase structure, **When** inspecting `com.biopatternsg.application.services`, **Then** the package root contains exclusively interface contracts, and all concrete classes reside inside `impl/`.

---

### User Story 2 - Decoupling Outbound Adapters from Application Services (Priority: P2)

As a Core Platform Engineer, I need outbound infrastructure adapters (`InferencesAdapter`, `PubmedIntegrationAdapter`, `BiologicalObjectAdapter`) to be completely decoupled from application services, ensuring infrastructure components only communicate outward with external HTTP systems and never mutate internal application state directly.

**Why this priority**: Outbound adapters calling application services creates an illegal backward dependency from Infrastructure to Application, violating Constitution Principle I (*Strict Hexagonal Architecture & Layer Isolation*) and failing `ArchitectureLayerTest.layerApplicationTest`.

**Independent Test**: Can be verified by inspecting all outbound adapter classes to ensure zero imports or injections of application service interfaces/classes, and confirming that adapters only communicate via their external clients and outbound ports.

**Acceptance Scenarios**:

1. **Given** an outbound integration adapter executing an external API request, **When** the external request is dispatched, **Then** the adapter does not call or inject any application service to update step state.
2. **Given** an outbound adapter with unused service injections, **When** the adapter is analyzed, **Then** all unused dependency fields and imports are completely removed.

---

### User Story 3 - Centralized Pipeline Step Orchestration & State Transition Management (Priority: P3)

As a Biological Workflow Orchestrator Maintainer, I need pipeline step lifecycle state transitions (`IN_PROGRESS`, `FAILED`, `COMPLETED`) to be coordinated centrally within the application service layer (`PipelineStepOrchestrator`), ensuring that execution progress and failure states are updated consistently around external service calls.

**Why this priority**: Moving step state updates out of infrastructure adapters requires the application orchestrator to manage the complete lifecycle of each step, guaranteeing that failures in external integrations are intercepted and recorded reliably.

**Independent Test**: Can be verified by executing pipeline orchestration flows and confirming that step states transition to `IN_PROGRESS` prior to triggering external calls, transition to `COMPLETED` upon success, and transition to `FAILED` when external adapters encounter errors.

**Acceptance Scenarios**:

1. **Given** a pipeline step triggered for execution, **When** the application step orchestrator invokes an external integration, **Then** it records the `IN_PROGRESS` status before the call and handles success appropriately.
2. **Given** an external service failure or network error during step execution, **When** the outbound adapter propagates the exception, **Then** the application orchestrator catches the failure and records the `FAILED` status on the pipeline step.

---

### User Story 4 - Automated Hexagonal Layer Boundary Compliance (Priority: P4)

As a Platform Quality Assurance Engineer, I need automated architectural quality gates (`ArchitectureLayerTest`) to verify that the application layer is exclusively accessed by application classes and inbound ports, ensuring zero regressions in architectural isolation.

**Why this priority**: Continuous automated verification ensures that infrastructure adapters never reintroduce illegal dependencies into the application layer, guaranteeing long-term architectural stability.

**Independent Test**: Can be verified by executing the complete architectural test suite, ensuring all 7 tests in `ArchitectureLayerTest` (including `layerApplicationTest`) pass with zero violations.

**Acceptance Scenarios**:

1. **Given** the full codebase compiled classes, **When** the layered architecture rule `.whereLayer("Application").mayOnlyBeAccessedByLayers("Application")` is evaluated, **Then** the check passes with zero violations.
2. **Given** any new or refactored component in infrastructure, **When** dependencies are scanned, **Then** zero classes in `com.biopatternsg.infrastructure..` depend on `com.biopatternsg.application.services..`.

---

### Edge Cases

- What happens when an external microservice call throws a runtime or communication exception?
  - The outbound adapter MUST log the error and propagate the exception, allowing `PipelineStepOrchestrator` to catch it and update the pipeline step state to `FAILED`.
- What happens if a pipeline execution context lacks user identification required by an external service?
  - The outbound adapter or orchestrator MUST detect the missing identification, log the error, and transition the step state to `FAILED` without crashing unhandled.
- What happens if a legacy use case previously injected a concrete service class?
  - The use case constructor and fields MUST be updated to inject only the public interface, allowing the dependency injection container to provide the hidden implementation.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The application services package (`com.biopatternsg.application.services`) MUST contain only public interface contracts for application services (`PipelineService`, `PipelineStepOrchestrator`).
- **FR-002**: All concrete implementations of application services (`PipelineServiceImpl`, `PipelineStepOrchestratorImpl`) MUST reside within the implementation subpackage (`com.biopatternsg.application.services.impl`).
- **FR-003**: The pipeline step orchestrator interface (`PipelineStepOrchestrator`) MUST expose the contract `void orchestrate(PipelineConfig pipelineConfig, PipelineSteps step)`.
- **FR-004**: All use cases (`UpdatePipelineUseCase`, `UpdatePipelineStepUseCase`, `RegenerateAlignedObjectsUseCase`, `ResumePipelineUseCase`) MUST inject only the public interface contracts `PipelineService` and `PipelineStepOrchestrator`.
- **FR-005**: Outbound infrastructure adapters (`InferencesAdapter`, `PubmedIntegrationAdapter`, `BiologicalObjectAdapter`) MUST NOT inject, import, or invoke `PipelineService` or any application service.
- **FR-006**: The unused `PipelineService` injection in `BiologicalObjectAdapter` MUST be eliminated.
- **FR-007**: Step status transitions (`IN_PROGRESS`, `COMPLETED`, `FAILED`) and execution error capture MUST be orchestrated within `PipelineStepOrchestratorImpl` in the application layer.
- **FR-008**: Outbound adapters MUST focus strictly on executing external HTTP requests via REST clients and propagating results or integration exceptions.
- **FR-009**: The automated architecture test suite MUST enforce that the `Application` layer is accessed exclusively by `Application` components, passing all 7 checks in `ArchitectureLayerTest`.

### Key Entities *(include if feature involves data)*

- **PipelineService Interface**: Application service contract defining step status mutation and metrics recording (`updateStep`).
- **PipelineServiceImpl**: Encapsulated application service implementation managing pipeline configuration persistence via repository ports.
- **PipelineStepOrchestrator Interface**: Application service contract defining multi-step workflow progression across biological microservices.
- **PipelineStepOrchestratorImpl**: Encapsulated orchestrator implementation coordinating external integrations and managing step lifecycle states.
- **Outbound Integration Adapters**: Infrastructure components (`InferencesAdapter`, `PubmedIntegrationAdapter`, `BiologicalObjectAdapter`) interacting strictly with external REST services.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of classes directly in `com.biopatternsg.application.services` are public interface contracts (zero concrete service classes in root package).
- **SC-002**: 100% of application service implementations reside inside `com.biopatternsg.application.services.impl`.
- **SC-003**: 0 classes in `com.biopatternsg.infrastructure..` depend on or import classes from `com.biopatternsg.application.services..`.
- **SC-004**: 100% pass rate (7/7 tests passing with 0 failures or violations) in `ArchitectureLayerTest`.
- **SC-005**: 100% test pass rate across the full application test suite (`mvn test`) with zero regressions in pipeline resumption, updates, or external integrations.

## Assumptions

- External REST endpoint contracts, paths, and JSON payloads for PubMed, PubTator, Inferences, and Biological Object services remain unchanged.
- CDI container (Quarkus Arc) resolves interface injections to `@ApplicationScoped` implementations located in `impl/` subpackages seamlessly.
- Exception handling in `PipelineStepOrchestratorImpl` guarantees that external communication errors lead to explicit `FAILED` status updates on the aggregate.
