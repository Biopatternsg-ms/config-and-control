# Implementation Plan: Domain Layer Purity and Framework Decoupling

**Branch**: `002-domain-layer-purity` | **Date**: 2026-11-19 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/002-domain-layer-purity/spec.md`

---

## Summary

This plan executes **REQ-CNC-ARCH-01** to restore architectural purity to the `domain` layer in `config-and-control`. The technical approach:
1. Decouples `KeycloakRepository` from Jakarta REST `Response` by modifying `register(UserConfig newUser)` to return a pure `String` (`identityProviderId`).
2. Encapsulates HTTP status code verification and `Location` header URI parsing inside `KeycloakAdapter` in the infrastructure layer.
3. Updates `UserManagementUseCase` to consume `String identityProviderId` directly without parsing HTTP headers.
4. Relocates the `PipelineService` interface from `com.biopatternsg.domain.services` to `com.biopatternsg.application.services` and corrects `PipelineServiceImpl` package declaration.
5. Deletes the `src/main/java/com/biopatternsg/domain/services/` directory completely.
6. Updates all references to `PipelineService` across the codebase and verifies that `ArchitectureLayerTest.domainLayerIsolationTest` passes with zero violations.

---

## Technical Context

**Language/Version**: Java 21 (OpenJDK 21 at `/usr/lib/jvm/java-21-openjdk-amd64`)

**Primary Dependencies**: Quarkus 3.30.4, MicroProfile REST Client, Lombok 1.18.36, ArchUnit 1.4.1 / JUnit 5

**Storage**: MongoDB (Panache Mongo Repository) - unaffected by this refactoring

**Testing**: JUnit 5, ArchUnit (`ArchitectureLayerTest`), Maven Surefire

**Target Platform**: Linux JVM / Containerized Microservice

**Project Type**: Backend Microservice (Hexagonal Architecture)

**Performance Goals**: Zero runtime overhead; fast build and test cycle (<10s).

**Constraints**:
- Strict Hexagonal Architecture & Layer Isolation (Principle I)
- Zero web/framework dependencies (`jakarta.ws.rs.*`) in `com.biopatternsg.domain.*`
- Zero domain service packages (`com.biopatternsg.domain.services` removed)
- Application Service interface in `com.biopatternsg.application.services` and implementation package alignment (Principle III)
- Automated ArchUnit tests passing cleanly with 0 violations (Principle VI)

**Scale/Scope**: Refactoring focused on 5 key classes and their imports across 6 calling sites.

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Requirement | Plan Status | Rationale |
| :--- | :--- | :---: | :--- |
| **I. Strict Hexagonal Architecture & Layer Isolation** | Domain layer MUST NOT depend on application, infrastructure, or external frameworks. Outbound ports must return domain models/primitives. | **PASS** | `KeycloakRepository` is decoupled from `jakarta.ws.rs.core.Response`, returning `String`. `domain.services` is removed. |
| **II. Use Cases as Lightweight Orchestrators** | <= 3 parameters per method. Orchestration logic only. | **PASS** | `register(UserConfig)` takes 1 parameter; orchestration simplified by receiving `userId` directly. |
| **III. Application Services Encapsulation** | Public interfaces in `application.services`, implementations package-aligned. | **PASS** | `PipelineService` interface placed in `application.services`, package declaration aligned in `PipelineServiceImpl`. |
| **IV. Mandatory Synchronous Use Case Documentation** | Update `.agents/use_cases/<UseCaseName>.md` if flow/signature changes. | **PASS** | `UserManagementUseCase.md` to be reviewed/synchronized. |
| **V. Clean Code & Zero Business Logic in Delivery & Lambdas** | Pure static mappers, controllers as delivery adapters only. | **PASS** | No changes to controllers or lambda expressions required. |
| **VI. ArchUnit & Quality Gates Enforcement** | `ArchitectureLayerTest` passes with zero violations. | **PASS** | `domainLayerIsolationTest` explicitly verified to pass. |

---

## Project Structure

### Documentation (this feature)

```text
specs/002-domain-layer-purity/
├── spec.md              # Feature specification
├── plan.md              # Implementation plan (this file)
├── research.md          # Phase 0 technical research and decisions
├── data-model.md        # Phase 1 data models & boundary diagrams
├── quickstart.md        # Phase 1 validation & run guide
├── contracts/           # Phase 1 interface contracts
│   ├── keycloak-repository-contract.md
│   └── pipeline-service-contract.md
├── checklists/
│   └── requirements.md  # Requirements quality checklist
└── tasks.md             # Phase 2 output (/speckit-tasks command)
```

### Source Code Layout

```text
src/
├── main/java/com/biopatternsg/
│   ├── domain/
│   │   ├── models/
│   │   │   ├── UserConfig.java
│   │   │   └── PipelineConfig.java
│   │   ├── port/
│   │   │   └── out/repositories/
│   │   │       └── KeycloakRepository.java  # Refactored: returns String, removes JAX-RS
│   │   └── services/                        # DELETED (Entire directory removed)
│   ├── application/
│   │   ├── services/
│   │   │   ├── PipelineService.java         # Relocated from domain.services
│   │   │   ├── PipelineServiceImpl.java     # Package declaration corrected
│   │   │   └── PipelineStepOrchestrator.java
│   │   └── usecase/
│   │       └── UserManagementUseCase.java   # Refactored: consumes String userId directly
│   └── infrastructure/
│       └── adapters/out/repositories/
│           ├── KeycloakAdapter.java         # Refactored: parses Location & returns String userId
│           ├── BiologicalObjectAdapter.java # Import updated to application.services
│           ├── InferencesAdapter.java       # Import updated to application.services
│           └── PubmedIntegrationAdapter.java # Import updated to application.services
└── test/java/
    ├── archtest/
    │   └── ArchitectureLayerTest.java       # Verified against domainLayerIsolationTest
    └── unit/
```

**Structure Decision**: Standard Maven/Quarkus single project with Hexagonal package hierarchy (`domain`, `application`, `infrastructure`). All changes conform strictly to established package conventions.

---

## Complexity Tracking

*No constitutional violations exist; this feature directly remedies existing architectural non-compliance.*
