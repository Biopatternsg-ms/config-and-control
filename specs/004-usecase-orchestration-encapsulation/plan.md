# Implementation Plan: Use Cases Lightweight Orchestration and Parameter Encapsulation

**Branch**: `004-usecase-orchestration-encapsulation` | **Date**: 2026-11-19 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/004-usecase-orchestration-encapsulation/spec.md`

## Summary

Enforce Principle II (*Use Cases as Lightweight Orchestrators*), Rule 1.3 (*Maximum 3 Parameters Rule*), and Principle III (*Application Services Encapsulation*) across the application layer of `services/config-and-control`:
1. Refactor `PipelineService.updateStep` to eliminate the 4-parameter method, replacing it with `updateStep(String id, PipelineStatus statusUpdate)` (2 parameters).
2. Extract heavy timeline building, duration computation, metric derivation, and UI icon/label resolution from `GetPipelineExecutionUseCase` into a dedicated Application Service (`PipelineExecutionService` / `impl/PipelineExecutionServiceImpl`), reducing the use case to ~50 lines of pure orchestration.
3. Centralize pipeline resumption dispatching and predecessor step resolution in `PipelineStepOrchestrator.resumePipeline(...)`, eliminating the 5-parameter method in `ResumePipelineUseCase` using an internal batch accumulator (`PipelineResumptionBatch`).
4. Encapsulate step completion rules (`CONFIG`) and metric generation (`UPDATE_ALIGNED_OBJECTS`) directly as rich domain invariants on `PipelineConfig`, eliminating procedural validation in `UpdatePipelineUseCase`.
5. Synchronize use case documentation in `.agents/use_cases/` for all modified use cases per Constitution Principle IV.

---

## Technical Context

**Language/Version**: Java 21 LTS (OpenJDK 21.0.13)
**Primary Dependencies**: Quarkus 3.30.4, SmallRye MicroProfile REST Client, Lombok 1.18.36, ArchUnit 1.4.1, JUnit 5, Mockito
**Storage**: In-memory / MongoDB Repository adapters (via domain ports)
**Testing**: Maven Surefire, JUnit 5, ArchUnit (`ArchitectureLayerTest`)
**Target Platform**: Linux container / Kubernetes microservice
**Project Type**: Microservice (Hexagonal Architecture)
**Performance Goals**: Sub-50ms execution report generation, sub-100ms resumption batch dispatching
**Constraints**:
- Max 3 parameters per method across all use cases and application services.
- Zero business logic / formatting in REST controllers.
- Strict interface separation in `application.services` with implementations in `impl/`.
- 100% backward compatibility on JSON responses (`ExperimentExecutionResponse`, `PipelineResumeResponse`).

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle / Rule | Compliance Status | Justification / Implementation Note |
| :--- | :---: | :--- |
| **Principle I: Hexagonal Layer Isolation** | **PASS** | No infrastructure dependencies in domain or application layers. Domain models remain pure. |
| **Principle II: Lightweight Orchestrators** | **PASS** | `GetPipelineExecutionUseCase`, `ResumePipelineUseCase`, and `UpdatePipelineUseCase` stripped of heavy computation, delegating to `PipelineExecutionService`, `PipelineStepOrchestrator`, and `PipelineConfig`. |
| **Principle II / Rule 1.3: Max 3 Parameters** | **PASS** | `PipelineService.updateStep` (4 params) and `ResumePipelineUseCase.processPipeline` (5 params) refactored into <=3 params using `PipelineStatus` and `PipelineResumptionBatch`. |
| **Principle III: Application Services Encapsulation** | **PASS** | New `PipelineExecutionService` defines public interface in `application.services` and hidden implementation in `application.services.impl`. |
| **Principle IV: Mandatory Use Case Documentation** | **PASS** | Documentation for `GetPipelineExecutionUseCase.md`, `ResumePipelineUseCase.md`, and `UpdatePipelineUseCase.md` synchronized in the same change set. |
| **Principle VI: ArchUnit & Quality Gates** | **PASS** | `ArchitectureLayerTest` (7/7 tests) and full test suite validated. |

---

## Project Structure

### Documentation (this feature)

```text
specs/004-usecase-orchestration-encapsulation/
├── plan.md              # Implementation plan (this file)
├── research.md          # Technical decisions and rationale
├── data-model.md        # Enriched domain model and context structures
├── quickstart.md        # Verification scenarios and test execution guide
├── contracts/           # Service and orchestrator interface contracts
│   ├── pipeline-execution-service-contract.md
│   ├── pipeline-service-parameter-contract.md
│   └── resumption-orchestration-contract.md
└── checklists/
    └── requirements.md  # Spec quality checklist (16/16 PASS)
```

### Source Code (repository root)

```text
src/main/java/com/biopatternsg/
├── domain/
│   └── models/
│       ├── PipelineConfig.java             # Enriched with isConfigComplete(), applyAlignedObjectsConfirmation()
│       └── PipelineStatus.java             # Encapsulates step, status, and metrics
├── application/
│   ├── services/
│   │   ├── PipelineService.java            # Refactored updateStep (2 & 3 params only)
│   │   ├── PipelineStepOrchestrator.java   # Extended with resumePipeline(...)
│   │   ├── PipelineExecutionService.java   # Extracted report generation interface
│   │   └── impl/
│   │       ├── PipelineServiceImpl.java
│   │       ├── PipelineStepOrchestratorImpl.java # Resumption dispatch implementation
│   │       └── PipelineExecutionServiceImpl.java # Timeline, duration, and icon resolution
│   └── usecase/
│       ├── GetPipelineExecutionUseCase.java # Lightweight report orchestrator
│       ├── ResumePipelineUseCase.java       # Lightweight batch resumption orchestrator
│       └── UpdatePipelineUseCase.java       # Lightweight update orchestrator delegating to domain
src/test/java/
├── archtest/
│   └── ArchitectureLayerTest.java          # 7/7 automated architecture tests
└── unit/
    ├── domain/
    │   └── PipelineConfigTest.java
    ├── services/
    │   ├── PipelineExecutionServiceTest.java
    │   └── PipelineStepOrchestratorTest.java
    └── usecase/
        ├── GetPipelineExecutionUseCaseTest.java
        ├── ResumePipelineUseCaseTest.java
        └── UpdatePipelineUseCaseTest.java
.agents/
└── use_cases/
    ├── GetPipelineExecutionUseCase.md       # Synchronized documentation
    ├── ResumePipelineUseCase.md             # Synchronized documentation
    └── UpdatePipelineUseCase.md             # Synchronized documentation
```

---

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
| :--- | :--- | :--- |
| *None* | No constitutional exemptions or complexity compromises required. | All solutions directly simplify existing code and enforce core principles. |
