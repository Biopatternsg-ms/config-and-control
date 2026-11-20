# Phase 0 Research & Technical Analysis: Application Services Encapsulation and Layer Boundaries

## Context & Objectives

This research analyzes the technical approach for implementing **REQ-CNC-ARCH-02** (`003-app-services-encapsulation`), enforcing Constitution Principle I (*Strict Hexagonal Architecture & Layer Isolation*) and Principle III (*Application Services Encapsulation — Strict Interface & Hidden Impl*).

---

## Technical Decisions & Analysis

### 1. Interface & Implementation Separation in `application.services`

- **Decision**: Define public interfaces `PipelineService` and `PipelineStepOrchestrator` in `com.biopatternsg.application.services` and place their `@ApplicationScoped` implementations (`PipelineServiceImpl`, `PipelineStepOrchestratorImpl`) inside `com.biopatternsg.application.services.impl`.
- **Rationale**:
  - Complies with `.agents/rules.md` (Section 2.2) and Constitution Principle III, which mandates that all application services expose public interfaces and hide concrete classes in `impl/`.
  - Use cases (`UpdatePipelineUseCase`, `UpdatePipelineStepUseCase`, `RegenerateAlignedObjectsUseCase`, `ResumePipelineUseCase`) inject interface contracts, which Quarkus Arc CDI resolves to their implementations at build/runtime.
- **Alternatives Considered**:
  - Leaving implementations in `application.services`: Rejected, directly violates Constitution Principle III and rules.md.
  - Subpackaging by context (`application.services.pipeline.impl`): Kept under `application.services.impl` for consistency with current microservice size, while maintaining full interface isolation.

### 2. Decoupling Outbound Adapters from `PipelineService`

- **Decision**:
  - Remove unused `@Inject private PipelineService pipelineService;` from `BiologicalObjectAdapter`.
  - Remove `PipelineService` constructor parameter and field from `InferencesAdapter` and `PubmedIntegrationAdapter`.
- **Rationale**:
  - Hexagonal Architecture dictates that dependencies flow inward. Outbound adapters in `infrastructure.adapters.out.repositories` implement Outbound Ports (`domain.port.out.*`) and communicate with external web/database systems.
  - Having an outbound adapter directly invoke an Application Service creates an illegal backward dependency from Infrastructure to Application, violating `ArchitectureLayerTest.layerApplicationTest` (`whereLayer("Application").mayOnlyBeAccessedByLayers("Application")`).
- **Alternatives Considered**:
  - Defining a domain port for step updates called by adapters: Rejected, step progression and workflow orchestration belong to the application layer (`PipelineStepOrchestrator`), not infrastructure adapters.

### 3. Step Lifecycle Orchestration in `PipelineStepOrchestratorImpl`

- **Decision**: Move step status transitions (`IN_PROGRESS`, `FAILED`, `PENDING`) and exception interception into `PipelineStepOrchestratorImpl`.
- **Rationale**:
  - When a workflow step triggers an external adapter, `PipelineStepOrchestratorImpl` updates the aggregate step status to `IN_PROGRESS` via `PipelineService.updateStep(...)`.
  - If the outbound adapter completes without error, the step continues or is marked appropriately.
  - If the outbound adapter throws an integration exception, `PipelineStepOrchestratorImpl` intercepts it and transitions the pipeline step status to `FAILED`.
- **Alternatives Considered**:
  - Event-driven reactive bus: Overkill for current synchronous REST clients; standard orchestration wrapping provides clean error handling and immediate traceability.

### 4. ArchUnit Compliance Gate

- **Decision**: Ensure all 7 tests in `ArchitectureLayerTest` pass cleanly.
- **Rationale**:
  - Decoupling infrastructure from `application.services` resolves all 21 violations in `layerApplicationTest`.
  - The combination of REQ-01 (domain purity) and REQ-02 (application encapsulation) establishes full constitutional compliance.

---

## Conclusion & Readiness for Phase 1

All decisions are architecturally sound, verified against Quarkus CDI injection mechanisms, and ready for contract and data modeling.
