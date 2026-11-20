# Phase 0 Research & Technical Analysis: Domain Layer Purity and Framework Decoupling

## Context & Objectives

This research resolves the technical decisions for executing **REQ-CNC-ARCH-01** (`002-domain-layer-purity`), ensuring strict adherence to Hexagonal Architecture (Constitution Principle I) and eliminating framework leaks from the domain layer.

---

## Technical Decisions & Analysis

### 1. Build Environment & Java 21 Runtime Configuration

- **Decision**: All Maven builds and test executions must use Java 21 explicitly (`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`).
- **Rationale**: The host environment has OpenJDK 25 set as default, which triggers a known compiler fatal error (`java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN`) when javac interacts with Lombok 1.18.36. Setting `JAVA_HOME` to OpenJDK 21 complies with the project constitution and compiles cleanly in ~5 seconds.
- **Alternatives Considered**: Upgrading Lombok to an unverified version (rejected: might introduce regressions with Quarkus 3.30.4).

### 2. Domain Outbound Port Decoupling (`KeycloakRepository`)

- **Decision**: Update `KeycloakRepository.register(UserConfig newUser)` to return a pure `String` representing the generated `identityProviderId`.
- **Rationale**: 
  - `jakarta.ws.rs.core.Response` leaks HTTP transport concepts (`Response`, `Location` header, HTTP status codes) directly into the domain port interface.
  - In `UserManagementUseCase`, the returned value is immediately parsed to extract the trailing ID segment from the URI path (`location.getPath().substring(...)`) and passed to `userConfig.setIdentityProviderId(userId)`.
  - Returning `String` provides a clear, minimal contract between domain and infrastructure.
- **Alternatives Considered**:
  - Returning `UserConfig` with ID populated: Rejected, as `KeycloakRepository` only provisions identity in Keycloak, whereas MongoDB persistence and aggregate population are owned by `UserManagementUseCase` and `UserRepository`.
  - Returning `java.net.URI`: Rejected, as `URI` is still a protocol-level locator and not a domain identifier.

### 3. Encapsulation of Header Extraction in `KeycloakAdapter`

- **Decision**: Move the extraction of `response.getLocation().getPath()` and substring logic into `KeycloakAdapter.register(UserConfig userConfig)`.
- **Rationale**:
  - `KeycloakAdapter` is an outbound infrastructure adapter whose purpose is translating external REST/HTTP responses into domain primitives/models.
  - If Keycloak registration succeeds (HTTP 201), `KeycloakAdapter` extracts the user ID, assigns default roles (`researcher`) via `keycloakHttpClient.setRoles`, and returns the extracted `userId` `String`.
  - If Keycloak returns an error or location is missing, `KeycloakAdapter` throws `KeycloakServiceException` (or appropriate domain exception) instead of letting null references leak into the application layer.
- **Alternatives Considered**:
  - Exposing an adapter response DTO: Rejected, domain port contracts must not reference infrastructure DTOs.

### 4. Elimination of `domain.services` Package

- **Decision**: 
  - Relocate `PipelineService.java` interface from `src/main/java/com/biopatternsg/domain/services/` to `src/main/java/com/biopatternsg/application/services/`.
  - Fix package declaration of `PipelineServiceImpl.java` to `package com.biopatternsg.application.services;`.
  - Delete `src/main/java/com/biopatternsg/domain/services/` directory.
  - Update all imports referencing `com.biopatternsg.domain.services.PipelineService`.
- **Rationale**:
  - Hexagonal architecture defines the domain layer as consisting purely of `enums`, `exceptions`, `models`, and `port` (`in` and `out`).
  - `PipelineService` coordinates biological workflow step transitions, metric persistence, and asynchronous operations, which belongs to the application layer.
- **Alternatives Considered**:
  - Leaving `PipelineService` in domain: Rejected, directly violates Constitution Principle I & III and `.agents/architecture.md`.

### 5. Scope Boundary with Requirement 02 (REQ-CNC-ARCH-02)

- **Finding**: 
  - Several outbound adapters (`InferencesAdapter`, `PubmedIntegrationAdapter`, `BiologicalObjectAdapter`) currently import `PipelineService` to mutate pipeline steps.
  - Decoupling these outbound adapters from `PipelineService` (moving orchestration up to `PipelineStepOrchestrator` and hiding implementations under `application/services/impl`) is explicitly scheduled for **Requirement 02** (`02-application-services-encapsulation-and-layer-boundaries`).
  - Requirement 01 is strictly bounded to domain layer purity: removing `jakarta.ws.rs.*` from `KeycloakRepository`, removing `domain.services`, and ensuring `ArchitectureLayerTest.domainLayerIsolationTest` passes with zero violations.

---

## Conclusion & Readiness for Phase 1

All technical unknowns and clarifications have been resolved. The architecture boundaries are defined, and the implementation steps are prepared for data modeling and contract specification.
