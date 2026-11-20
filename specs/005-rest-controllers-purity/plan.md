# Implementation Plan: REST Controllers Purity and Delivery Layer Compliance

**Branch**: `005-rest-controllers-purity` | **Date**: 2026-11-20 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/005-rest-controllers-purity/spec.md`

## Summary

Enforce Constitution Principle V (*Clean Code & Zero Business Logic in Delivery & Lambdas*) and Principle I (*Strict Hexagonal Architecture & Layer Isolation*) across all REST controllers in `services/config-and-control`:
1. **Eliminate Domain Model Leakage**: Refactor `NetworkController` and `PipelineController` to eliminate `NetworkConfig` and `PipelineConfig` domain models from public method return types and `@APIResponse` OpenAPI annotations, returning strongly-typed `NetworkResponse`, `PipelineResponse` (for lists), and a newly introduced `PipelineDetailResponse` (for full pipeline configuration queries and updates).
2. **Remove Business Branching from Delivery Adapters**: Eliminate imperative null checks and ad-hoc error map construction in `PipelineController.resume` by relying on declarative bean validation (`@NotNull`, `@Valid`) and Quarkus exception mappers.
3. **Encapsulate Webhook Interpretation**: Extract Keycloak webhook payload parsing, event type verification, and target user identifier extraction from `UserController.handleKeycloakEvent` into a stateless infrastructure mapper (`KeycloakEventMapper`), allowing `UserController` to dispatch directly to `UserManagement.processEmailVerification` without conditional branching.
4. **Encapsulate Collection & Report Mappings**: Replace inline `.stream().map(...).toList()` pipelines in `AdminController.listUsers`, `NetworkController.findList`, and `PipelineController.findList` with dedicated static mapper methods (`toReportResponse`) on `UserMapper`, `NetworkMapper`, and `PipelineMapper`.
5. **Quality Gate Enforcement**: Add an automated ArchUnit rule in `ArchitectureLayerTest` to strictly verify that no public method in `infrastructure.adapters.in.restcontrollers..` exposes return types residing in `domain.models..`.

---

## Technical Context

**Language/Version**: Java 21 LTS (OpenJDK 21.0.13)
**Primary Dependencies**: Quarkus 3.30.4 (Arc CDI, RESTEasy Reactive, Hibernate Validator), SmallRye OpenAPI, Lombok 1.18.36, ArchUnit 1.4.1, JUnit 5, Mockito
**Storage**: N/A (Delivery layer focus; domain and repository boundaries remain unchanged)
**Testing**: Maven Surefire, JUnit 5, ArchUnit (`ArchitectureLayerTest`), Mockito
**Target Platform**: Linux container / Kubernetes microservice
**Project Type**: Microservice (Hexagonal Architecture)
**Performance Goals**: Sub-5ms serialization latency for REST endpoint responses, zero allocation overhead beyond DTO records
**Constraints**:
- Controllers are strictly delivery adapters: Receive DTO -> Map to Domain -> Invoke Port -> Map to Response DTO.
- Zero business logic, existence checks, or imperative branching (`if`, `switch`) in controller methods.
- Zero domain model types exposed in controller public method return types.
- Static mappers in `infrastructure.adapters.mappers.*` remain stateless utility classes with pure static methods.
- 100% backward compatibility for all external JSON API responses and webhook consumers.

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle / Rule | Compliance Status | Justification / Implementation Note |
| :--- | :---: | :--- |
| **Principle I: Hexagonal Layer Isolation** | **PASS** | Eliminates domain aggregate leakage (`NetworkConfig`, `PipelineConfig`) into the delivery layer. Controllers interact solely through Inbound Ports and transfer DTOs. |
| **Principle II: Lightweight Orchestrators** | **PASS** | Use cases continue to act as single-business-interaction orchestrators; no orchestration logic is moved to controllers. |
| **Principle III: Application Services Encapsulation** | **PASS** | Controllers inject only Inbound Port interfaces (`domain.port.in.*`), never Application Services directly. |
| **Principle IV: Mandatory Use Case Documentation** | **PASS** | No use case signatures or execution flows are modified; all refactoring is contained within the delivery layer (controllers, mappers, DTOs). |
| **Principle V: Clean Code & Zero Business Logic in Delivery & Lambdas** | **PASS** | **Core focus of this feature**: Eliminates all branching in controllers, encapsulates collection streams into static mappers, introduces `KeycloakEventMapper`, and replaces domain models with dedicated response DTOs. |
| **Principle VI: ArchUnit & Quality Gates** | **PASS** | Introduces automated ArchUnit rule verifying that zero public controller methods expose domain model return types, maintaining 100% test pass rate. |

---

## Project Structure

### Documentation (this feature)

```text
specs/005-rest-controllers-purity/
├── plan.md              # Implementation plan (this file)
├── research.md          # Technical analysis, decisions, and rationale
├── data-model.md        # DTO definitions, static mapper contracts, and serialization models
├── quickstart.md        # Verification guide, testing commands, and regression assertions
├── contracts/           # Delivery layer contracts and OpenAPI schemas
│   ├── network-controller-contract.md
│   ├── pipeline-controller-contract.md
│   ├── user-controller-contract.md
│   └── static-mappers-contract.md
└── checklists/
    └── requirements.md  # Spec quality checklist (16/16 PASS)
```

### Source Code (repository root)

```text
src/main/java/com/biopatternsg/
├── infrastructure/
│   ├── dtos/
│   │   ├── PipelineDetailResponse.java      # New DTO: detailed pipeline configuration
│   │   ├── PipelineResponse.java            # Existing lightweight summary DTO
│   │   └── NetworkResponse.java             # Existing network response DTO
│   ├── adapters/
│   │   ├── in/
│   │   │   └── restcontrollers/
│   │   │       ├── AdminController.java     # Cleaned: delegates report mapping to UserMapper
│   │   │       ├── NetworkController.java   # Cleaned: returns NetworkResponse, delegates report mapping
│   │   │       ├── PipelineController.java  # Cleaned: returns PipelineDetailResponse, zero if branching
│   │   │       └── UserController.java      # Cleaned: delegates webhook resolution to KeycloakEventMapper
│   │   └── mappers/
│   │       ├── KeycloakEventMapper.java     # New: static mapper for webhook event parsing
│   │       ├── NetworkMapper.java           # Added: toReportResponse(ReportFormat<NetworkConfig>)
│   │       ├── PipelineMapper.java          # Added: configToDetailResponse, toReportResponse
│   │       └── UserMapper.java              # Added: toReportResponse(ReportFormat<UserConfig>)
src/test/java/
├── archtest/
│   └── ArchitectureLayerTest.java          # New test: controllerMethodsShouldNotReturnDomainModelsTest
└── unit/
    ├── controllers/
    │   └── UserControllerTest.java         # Updated to verify KeycloakEventMapper delegation
    ├── restcontrollers/
    │   ├── NetworkControllerTest.java      # New: unit tests verifying NetworkResponse returns
    │   └── PipelineControllerTest.java     # Updated: unit tests verifying PipelineDetailResponse & resume
    └── mappers/
        └── KeycloakEventMapperTest.java    # New: unit tests verifying event parsing & extraction
```

---

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
| :--- | :--- | :--- |
| *None* | No constitutional exemptions or complexity compromises required. | The design strictly reduces delivery layer complexity, cleans controllers into 3-line dispatchers, and enforces purity. |
