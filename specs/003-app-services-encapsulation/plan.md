# Implementation Plan: Application Services Encapsulation and Layer Boundaries

**Branch**: `003-app-services-encapsulation` | **Date**: 2026-11-19 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/003-app-services-encapsulation/spec.md`

---

## Summary

This plan executes **REQ-CNC-ARCH-02** to enforce strict interface-implementation segregation in `application.services` and eliminate illegal dependencies from infrastructure adapters to application services:
1. Segregates `PipelineStepOrchestrator` into a public interface contract (`com.biopatternsg.application.services.PipelineStepOrchestrator`) and moves its `@ApplicationScoped` implementation to `com.biopatternsg.application.services.impl.PipelineStepOrchestratorImpl`.
2. Places `PipelineServiceImpl` inside `com.biopatternsg.application.services.impl.PipelineServiceImpl`, keeping `PipelineService` as the public interface contract.
3. Decouples outbound infrastructure adapters (`InferencesAdapter`, `PubmedIntegrationAdapter`, `BiologicalObjectAdapter`) from `PipelineService`, removing all backward dependencies from Infrastructure to Application.
4. Moves pipeline step status transition logic (`IN_PROGRESS`, `FAILED`, `PENDING`) and exception management into `PipelineStepOrchestratorImpl`.
5. Updates all unit tests to reflect the decoupled adapter design and verifies that all 7 tests in `ArchitectureLayerTest` (specifically `layerApplicationTest`) pass with zero violations.

---

## Technical Context

**Language/Version**: Java 21 (OpenJDK 21 at `/usr/lib/jvm/java-21-openjdk-amd64`)

**Primary Dependencies**: Quarkus 3.30.4, ArchUnit 1.4.1 / JUnit 5, Lombok 1.18.36, MicroProfile REST Client

**Storage**: MongoDB (Panache Mongo Repository) - unaffected

**Testing**: JUnit 5, ArchUnit (`ArchitectureLayerTest`), Maven Surefire

**Target Platform**: Linux JVM / Containerized Microservice

**Project Type**: Backend Microservice (Hexagonal Architecture)

**Performance Goals**: Clean build and test execution (<10s).

**Constraints**:
- Strict Hexagonal Architecture & Layer Isolation (Principle I)
- Application Services Encapsulation: Strict Interface & Hidden Impl (Principle III)
- Inbound controllers and use cases must not directly access concrete service implementations
- All 7 tests in `ArchitectureLayerTest` must pass with zero violations (Principle VI)

**Scale/Scope**: Refactoring focused on `PipelineService`, `PipelineStepOrchestrator`, their implementations in `impl/`, and 3 outbound adapters.

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Requirement | Plan Status | Rationale |
| :--- | :--- | :---: | :--- |
| **I. Strict Hexagonal Architecture & Layer Isolation** | Layer dependencies flow inward. Application may only be accessed by Application. | **PASS** | Outbound adapters (`InferencesAdapter`, `PubmedIntegrationAdapter`, `BiologicalObjectAdapter`) are decoupled from `PipelineService`, eliminating all infrastructure -> application dependencies. |
| **II. Use Cases as Lightweight Orchestrators** | Use cases coordinate through ports and application service interfaces. | **PASS** | All use cases inject `PipelineStepOrchestrator` and `PipelineService` interfaces, keeping orchestration lightweight. |
| **III. Application Services Encapsulation** | Public interfaces in `application.services`, implementations hidden in `impl/`. | **PASS** | `PipelineService` and `PipelineStepOrchestrator` reside in `application.services`; `PipelineServiceImpl` and `PipelineStepOrchestratorImpl` reside in `application.services.impl`. |
| **IV. Mandatory Synchronous Use Case Documentation** | Update documentation if flow or interfaces change. | **PASS** | Use cases maintain existing orchestration signatures; no documentation divergence. |
| **V. Clean Code & Zero Business Logic in Delivery & Lambdas** | Zero business logic in controllers. | **PASS** | Controllers remain untouched. |
| **VI. ArchUnit & Quality Gates Enforcement** | `ArchitectureLayerTest` passes with zero violations. | **PASS** | All 7 rules pass cleanly (including `layerApplicationTest`). |

---

## Project Structure

### Documentation (this feature)

```text
specs/003-app-services-encapsulation/
├── spec.md              # Feature specification
├── plan.md              # Implementation plan (this file)
├── research.md          # Phase 0 technical analysis and decisions
├── data-model.md        # Phase 1 data models & layer diagrams
├── quickstart.md        # Phase 1 executable validation guide
├── contracts/           # Phase 1 interface & boundary contracts
│   ├── pipeline-step-orchestrator-contract.md
│   └── outbound-adapter-boundaries.md
├── checklists/
│   └── requirements.md  # Requirements quality checklist
└── tasks.md             # Phase 2 output (/speckit-tasks command)
```

### Source Code Layout

```text
src/main/java/com/biopatternsg/
├── application/
│   ├── services/
│   │   ├── PipelineService.java                 # Public interface contract
│   │   ├── PipelineStepOrchestrator.java         # Public interface contract
│   │   └── impl/
│   │       ├── PipelineServiceImpl.java         # Hidden implementation (@ApplicationScoped)
│   │       └── PipelineStepOrchestratorImpl.java # Hidden implementation (@ApplicationScoped)
│   └── usecase/
│       ├── UpdatePipelineUseCase.java           # Injects service interfaces
│       ├── UpdatePipelineStepUseCase.java       # Injects service interfaces
│       ├── RegenerateAlignedObjectsUseCase.java # Injects service interfaces
│       └── ResumePipelineUseCase.java           # Injects service interfaces
└── infrastructure/
    └── adapters/out/repositories/
        ├── BiologicalObjectAdapter.java         # Unused PipelineService injection removed
        ├── InferencesAdapter.java               # PipelineService dependency removed
        └── PubmedIntegrationAdapter.java        # PipelineService dependency removed
```

---

## Complexity Tracking

*No constitutional violations exist; this feature directly remedies existing architectural violations.*
