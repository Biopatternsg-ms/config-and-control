# Phase 0 Research & Technical Analysis: REST Controllers Purity and Delivery Layer Compliance

**Feature**: `005-rest-controllers-purity`
**Date**: 2026-11-20
**Spec**: [spec.md](./spec.md)

---

## 1. Build Environment & Java 21 Runtime Configuration

- **Decision**: All Maven builds and test runs must run explicitly using Java 21 (`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test`).
- **Rationale**: The host environment default OpenJDK 25 triggers compiler incompatibility with Lombok 1.18.36 (`TypeTag :: UNKNOWN`). Setting `JAVA_HOME` to OpenJDK 21 complies with the project constitution and compiles cleanly in ~5 seconds.
- **Alternatives Considered**: Upgrading Lombok to an unverified version (rejected: might destabilize Quarkus 3.30.4 code generation).

---

## 2. Technical Decisions & Analysis

### 2.1. Domain Model Leakage vs DTO Encapsulation (`NetworkResponse` & `PipelineDetailResponse`)

- **Context**: 
  - In `NetworkController`: `create` wraps `NetworkConfig` (domain model) in `Response.status(CREATED).entity(...)`, while `update` and `findList(id)` return `NetworkConfig` directly.
  - In `PipelineController`: `create` wraps `PipelineConfig` (domain model) in `Response.status(CREATED).entity(...)`, while `updateDescription`, `updateTranscriptionFactor`, `updateExpertObjects`, `updateSearchConfig`, `updateAlignedExpertObjects`, and `findList(id)` return `PipelineConfig` directly.
- **Decision**:
  - `NetworkController`:
    - `create`: return `Response.status(Response.Status.CREATED).entity(NetworkMapper.toNetworkResponse(networkConfig)).build();`
    - `update`: return `NetworkResponse` directly via `NetworkMapper.toNetworkResponse(updateNetwork.execute(...));`
    - `findList(id)`: return `NetworkResponse` directly via `NetworkMapper.toNetworkResponse(findNetwork.byId(id));`
  - `PipelineController`:
    - Create a dedicated record `PipelineDetailResponse` in `com.biopatternsg.infrastructure.dtos` containing all detailed configuration fields (`id`, `networkId`, `name`, `description`, `levels`, `step`, `createdAt`, `status`, `statuses`, `expertObjects`, `alignedExpertObjects`, `transcriptionFactorConfig`, `useOnlyPrincipalName`, `retMax`, `maxComplexes`).
    - Add static mapper method `PipelineMapper.configToDetailResponse(PipelineConfig config)`.
    - `create`: return `Response.status(Response.Status.CREATED).entity(PipelineMapper.configToDetailResponse(pipelineConfig)).build();`
    - All 5 `update*` methods: return `PipelineDetailResponse` directly via `PipelineMapper.configToDetailResponse(updatePipeline.execute(...));`
    - `findList(id)`: return `PipelineDetailResponse` directly via `PipelineMapper.configToDetailResponse(findPipeline.byId(id));`
    - `findList(filters)`: continue returning `ReportFormat<PipelineResponse>` using lightweight summary DTOs.
- **Rationale**:
  - Eliminates domain model leakage into the delivery layer (Constitution Principle V).
  - Guarantees 100% backward compatibility for API consumers querying `GET /pipelines/{id}` and invoking `PUT` updates by keeping all configuration properties in the JSON response.
  - Keeps method signatures strongly typed (`public PipelineDetailResponse updateDescription(...)`), which automatically generates clean OpenAPI documentation and eliminates boilerplate `Response.ok(...).build()`.
- **Alternatives Considered**:
  - *Extending `PipelineResponse` directly*: Rejected in clarification Q3 (Option B chosen) to keep list responses lightweight without transferring complex lists of expert objects and statuses for every search row.
  - *Wrapping all endpoints in `jakarta.ws.rs.core.Response`*: Rejected in clarification Q2 (Option A chosen) because direct return types enforce compiler-checked DTO contracts and simplify controller code to single expressions.

---

### 2.2. Elimination of Imperative Branching in `PipelineController.resume`

- **Context**: 
  `PipelineController.resume` currently contains:
  ```java
  if (request == null || request.microService() == null) {
      return Response.status(Response.Status.BAD_REQUEST)
              .entity(java.util.Map.of("message", "microService is required"))
              .build();
  }
  ```
- **Decision**:
  - Remove the imperative `if` check from `PipelineController.resume`.
  - `ResumePipelineRequest.microService` already has `@NotNull(message = "microService is required")`.
  - Rely on Quarkus Bean Validation (`@Valid ResumePipelineRequest request`) and standard exception mappers (`ConstraintViolationExceptionMapper`) to handle null or missing fields automatically with HTTP 400 Bad Request.
  - Simplify the method body to:
    ```java
    var summary = resumePipeline.execute(request.microService());
    return Response.ok(PipelineMapper.toResponse(summary)).build();
    ```
- **Rationale**:
  - Complies strictly with Constitution Principle V (*REST controllers are purely delivery adapters with zero validation branching or error map building*).
  - Ensures uniform error response structure across all API endpoints in the application.
- **Alternatives Considered**:
  - *Throwing `UnprocessableEntityException`*: Rejected because missing required input fields is a standard HTTP 400 Bad Request client validation concern handled by Bean Validation.

---

### 2.3. Encapsulation of Keycloak Webhook Parsing into `KeycloakEventMapper`

- **Context**:
  `UserController.handleKeycloakEvent(KeycloakEventNotification event)` contains 20 lines of conditional event type checking, details inspection, and fallback ID resolution:
  ```java
  boolean isVerifyEmail = event != null && (
          "VERIFY_EMAIL".equalsIgnoreCase(event.type()) ||
          ("CUSTOM_REQUIRED_ACTION".equalsIgnoreCase(event.type())
                  && event.details() != null
                  && "VERIFY_EMAIL".equalsIgnoreCase(String.valueOf(event.details().get("custom_required_action"))))
  );
  if (isVerifyEmail) {
      String targetId = (event.userId() != null && !event.userId().isBlank())
              ? event.userId()
              : (event.details() != null ? String.valueOf(event.details().get("username")) : null);
      if (targetId != null && !targetId.isBlank() && !"null".equalsIgnoreCase(targetId)) {
          log.info("Processing email verification event for identifier: {}", targetId);
          userManagement.processEmailVerification(targetId);
      }
  }
  return Response.ok().build();
  ```
- **Decision**:
  - Create a stateless static mapper `KeycloakEventMapper` under `com.biopatternsg.infrastructure.adapters.mappers`.
  - Provide a pure static method `Optional<String> extractEmailVerificationTargetId(KeycloakEventNotification event)`.
  - In `UserController.handleKeycloakEvent`, simplify the logic to declarative dispatching:
    ```java
    KeycloakEventMapper.extractEmailVerificationTargetId(event)
            .ifPresent(userManagement::processEmailVerification);
    return Response.ok().build();
    ```
- **Rationale**:
  - Adheres to Clarification Q1 (Option A).
  - Preserves Hexagonal Architecture: `KeycloakEventNotification` remains in infrastructure; domain ports are not polluted with webhook structures.
  - Eliminates all nested `if` statements and log branches from `UserController`, reducing the method to a clean, declarative 3-line delivery action.
- **Alternatives Considered**:
  - *Passing `KeycloakEventNotification` to `UserManagement`*: Rejected; violates Constitution Principle I (domain cannot depend on infrastructure DTOs).
  - *Creating a CDI `@ApplicationScoped` webhook handler*: Rejected; a pure static utility class is simpler, stateless, and has zero runtime overhead or CDI lifecycle dependencies.

---

### 2.4. Encapsulation of Paginated `ReportFormat` Transformation in Static Mappers

- **Context**:
  `AdminController.listUsers`, `NetworkController.findList`, and `PipelineController.findList` currently duplicate stream transformation pipelines:
  ```java
  var responseList = modelFormat.list().stream().map(Mapper::toResponse).toList();
  return new ReportFormat<>(modelFormat.count(), responseList);
  ```
- **Decision**:
  - Add static utility methods:
    - `UserMapper.toReportResponse(ReportFormat<UserConfig> modelFormat)`
    - `NetworkMapper.toReportResponse(ReportFormat<NetworkConfig> modelFormat)`
    - `PipelineMapper.toReportResponse(ReportFormat<PipelineConfig> modelFormat)`
  - Each method safely checks for null/empty lists and constructs `new ReportFormat<>(modelFormat.count(), responseList)`.
  - In controllers, list queries become concise single-line statements:
    ```java
    return NetworkMapper.toReportResponse(findNetwork.findList(NetworkMapper.requestToModel(networkFilters), networkFilters.page(), networkFilters.size()));
    ```
- **Rationale**:
  - Complies with Constitution Principle V (*Static Mappers encapsulate transformation logic; lambdas inside controllers must not contain multi-line mappings*).
  - Centralizes collection mapping and null safety in one place per domain aggregate.
- **Alternatives Considered**:
  - *A generic static utility method `ReportFormatMapper.map(ReportFormat<M>, Function<M, R>)`*: Feasible, but having dedicated static methods on `UserMapper`, `NetworkMapper`, and `PipelineMapper` provides strongly-typed, discoverable methods consistent with existing mapper patterns in the project.

---

### 2.5. ArchUnit Quality Gate for Controller Return Types

- **Context**:
  `ArchitectureLayerTest` validates layer isolation and naming conventions, but currently has no check preventing domain models from leaking into controller public method signatures.
- **Decision**:
  - Add a dedicated ArchUnit test in `src/test/java/archtest/ArchitectureLayerTest.java`:
    ```java
    @DisplayName("Controllers should not expose domain models in public method return types")
    @Test
    void controllerMethodsShouldNotReturnDomainModelsTest() {
        ArchRuleDefinition.methods()
                .that().areDeclaredInClassesThat().resideInAPackage(ROOT + ".infrastructure.adapters.in.restcontrollers..")
                .and().arePublic()
                .should().notHaveRawReturnType(resideInAPackage(ROOT + ".domain.models.."))
                .check(this.javaClasses);
    }
    ```
- **Rationale**:
  - Enforces Constitution Principle VI (*ArchUnit & Quality Gates Enforcement*).
  - Provides an automated build barrier preventing regressions or future developer slip-ups.
