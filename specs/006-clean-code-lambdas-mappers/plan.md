# Implementation Plan: Clean Code, Lambda Purity, and Static Mappers Standardization

**Branch**: `006-clean-code-lambdas-mappers` | **Date**: 2026-11-20 | **Spec**: [spec.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/config-and-control/specs/006-clean-code-lambdas-mappers/spec.md)

**Input**: Feature specification from `specs/006-clean-code-lambdas-mappers/spec.md`

## Summary

Standardize functional programming and data transformation patterns across `config-and-control` microservice according to Constitution Principle V and Section 5 of `.agents/rules.md`. The plan accomplishes:
1. Complete elimination of multi-line block lambdas (`-> { ... }`) containing branching or mutations in favor of pure static mappers (`PipelineMapper.symbolsToExpertObjects`) and descriptive private helper methods (`PipelineConfig.updateExistingStatusMetrics`, `UserController.dispatchEmailVerification`).
2. Full standardization of static utility mappers (`NetworkMapper`, `PipelineMapper`, `UserMapper`, `KeycloakEventMapper`) making them `public final`, equipped with private constructors preventing instantiation, pure stateless static methods, null safety, and canonical `<source>To<Target>` nomenclature with delegating backward-compatible aliases.
3. Centralization of paginated database-to-domain collection transformations in outbound repository adapters (`NetworkRepositoryAdapter`, `PipelineRepositoryAdapter`, `UserRepositoryAdapter`) via dedicated `toDomainReport` methods.
4. Automated ArchUnit architecture enforcement in `ArchitectureLayerTest` verifying static mapper constraints.

## Technical Context

**Language/Version**: Java 21 LTS (OpenJDK 21, runtime and compiler target).

**Primary Dependencies**: Quarkus 3.30.4 (REST, MongoDB Panache, SmallRye OpenAPI), Lombok 1.18.36, ArchUnit 1.4.1, JUnit 5.

**Storage**: MongoDB with Panache (`PanacheMongoRepositoryBase`), collections mapped in `com.biopatternsg.infrastructure.mongo_db.collections.*`.

**Testing**: JUnit 5, ArchUnit (`ArchitectureLayerTest`), Mockito, Quarkus Test (`@QuarkusTest` for integration, plain POJO unit tests for pure mappers).

**Target Platform**: Linux / Container (OCI container / JVM mode).

**Project Type**: Hexagonal Microservice (REST API + MongoDB persistence + Keycloak integration).

**Performance Goals**: Zero object allocation overhead from utility classes; sub-millisecond in-memory stream and mapping execution.

**Constraints**:
- JDK toolchain: MUST build using `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`.
- Zero multi-line imperative block lambdas in functional stream/Optional expressions.
- 100% backward-compatibility for existing public API REST JSON contracts and existing mapper callers.

**Scale/Scope**: 4 static mappers, 3 repository adapters, 1 domain aggregate, 1 REST controller, 1 integration adapter, and 1 architecture test suite.

## Constitution Check

*GATE: Evaluated against `.specify/memory/constitution.md`*

| Principle | Check | Status | Notes |
|-----------|-------|--------|-------|
| **I. Hexagonal Isolation** | Domain models have zero dependencies on infrastructure or persistence; adapters reside in `infrastructure.adapters.*`. | PASS | Static mappers remain in `infrastructure.adapters.mappers` and bridge layers without leaking infrastructure details into domain. |
| **II. Use Cases as Orchestrators** | Use cases remain lightweight with max 3 parameters; no business logic placed in delivery or mappers. | PASS | No use cases are altered; orchestration remains in application layer. |
| **III. Application Services Encapsulation** | Interfaces separated from implementations; controllers do not bypass use cases. | PASS | No changes to service interfaces; `PipelineStepOrchestratorImpl` switch expressions remain intact (they are Java 21 switch rules, not functional lambdas). |
| **IV. Use Case Documentation** | Any modified use case has synced docs. | PASS | No use case signatures or orchestration logic are modified. |
| **V. Clean Code & Lambda Purity** | Delivery layer contains zero business logic; lambdas are single-line pure expressions; static mappers are final stateless utility classes. | PASS | Directly targeted and fully satisfied by this feature. |
| **VI. ArchUnit Quality Gates** | Automated architecture tests verify constraints. | PASS | New ArchUnit rules added to `ArchitectureLayerTest` verifying mapper naming, static methods, private constructors, and zero CDI annotations. |

## Project Structure

### Documentation (this feature)

```text
specs/006-clean-code-lambdas-mappers/
├── spec.md              # Feature specification
├── plan.md              # Implementation plan (this file)
├── research.md          # Technical decisions and patterns (Phase 0)
├── data-model.md        # Method signatures, contracts, and data structures (Phase 1)
├── quickstart.md        # Validation commands and test scenarios (Phase 1)
├── contracts/           # Mapping interface specifications (Phase 1)
│   ├── network-mapper-contract.md
│   ├── pipeline-mapper-contract.md
│   ├── user-mapper-contract.md
│   └── repository-report-contract.md
└── tasks.md             # Execution task breakdown (Phase 2 - generated by /speckit-tasks)
```

### Source Code Layout

```text
src/main/java/com/biopatternsg/
├── domain/models/
│   ├── PipelineConfig.java                             # Extract updateExistingStatusMetrics helper
│   ├── NetworkConfig.java
│   ├── UserConfig.java
│   └── ReportFormat.java
├── infrastructure/
│   ├── adapters/
│   │   ├── in/restcontrollers/
│   │   │   └── UserController.java                     # Extract dispatchEmailVerification helper
│   │   ├── mappers/
│   │   │   ├── NetworkMapper.java                      # final, private ctor, canonical names, toDomainReport
│   │   │   ├── PipelineMapper.java                     # final, private ctor, symbolsToExpertObjects, toDomainReport
│   │   │   ├── UserMapper.java                         # final, private ctor, canonical names, toDomainReport
│   │   │   └── KeycloakEventMapper.java                # Already final + private ctor; verify null safety
│   │   └── out/repositories/
│   │       ├── PubmedIntegrationAdapter.java           # Use PipelineMapper.symbolsToExpertObjects
│   │       ├── NetworkRepositoryAdapter.java           # Use NetworkMapper.toDomainReport
│   │       ├── PipelineRepositoryAdapter.java          # Use PipelineMapper.toDomainReport
│   │       └── UserRepositoryAdapter.java              # Use UserMapper.toDomainReport
src/test/java/
├── archtest/
│   └── ArchitectureLayerTest.java                      # Add static mappers ArchUnit verification
└── unit/
    └── mappers/
        ├── NetworkMapperTest.java
        ├── PipelineMapperTest.java
        ├── UserMapperTest.java
        └── KeycloakEventMapperTest.java
```

**Structure Decision**: Standard Quarkus Maven single-module layout. All refactored classes and new unit tests reside within established hexagonal packaging.

## Complexity Tracking

*No constitutional violations identified. No complexity justification required.*
