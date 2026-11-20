# Feature Specification: REST Controllers Purity and Delivery Layer Compliance

**Feature Branch**: `005-rest-controllers-purity`

**Created**: 2026-11-20

**Status**: Draft

**Input**: User description: "/speckit-specify basado en /home/yacson/Proyectos/biopatternsg-ms/SDD/config-and-control/04-rest-controllers-purity-and-delivery-layer-compliance"

## Clarifications

### Session 2026-11-20

- Q: ¿Dónde debe encapsularse la lógica de análisis del webhook de Keycloak, verificación del tipo de evento y resolución del identificador de usuario para mantener a `UserController` libre de ramificaciones de negocio? → A: Opción A (Mapper estático de infraestructura `KeycloakEventMapper` que encapsula la verificación del evento y extracción del ID de usuario objetivo, permitiendo que `UserController` despache de forma declarativa hacia `UserManagement.processEmailVerification`).
- Q: ¿Cuál debe ser la firma y tipo de retorno estándar para los métodos de actualización y consulta individual (`update*`, `findList(id)`) en `NetworkController` y `PipelineController`? → A: Opción A (Retornar directamente los DTOs de respuesta `NetworkResponse` y `PipelineResponse` / `PipelineDetailResponse` con HTTP 200 OK implícito, manteniendo tipado fuerte consistente con `ReportFormat<T>` y `getExecutionProgress`).
- Q: ¿Debe `PipelineResponse` incorporar los atributos completos de configuración o debe crearse un DTO específico (`PipelineDetailResponse`)? → A: Opción B (Crear `PipelineDetailResponse` con todos los atributos detallados para `GET /{id}` y endpoints `PUT` de pipelines, manteniendo `PipelineResponse` como DTO ligero exclusivo para listados paginados).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Delivery Layer DTO Encapsulation and Domain Model Isolation (Priority: P1)

As an API Consumer and Client Application, I need REST endpoints to strictly exchange Delivery Layer Transfer Objects (DTOs) rather than exposing internal domain models directly in HTTP responses or method signatures, so that internal domain entity evolution does not accidentally break external HTTP contracts and the domain layer remains fully isolated from the external presentation tier.

**Why this priority**: Directly exposing domain entities (`NetworkConfig`, `PipelineConfig`) in REST controllers breaks Hexagonal Architecture and violates Constitution Principle V (*Clean Code & Zero Business Logic in Delivery & Lambdas*). It tightly couples HTTP clients to domain aggregates, potentially leaking internal state and domain structures into external APIs.

**Independent Test**: Can be verified by inspecting all controller method signatures and endpoint response bodies across `NetworkController` and `PipelineController`, ensuring that only response DTOs (`NetworkResponse`, `PipelineResponse`) are returned, and no domain model types are exposed as endpoint return types.

**Acceptance Scenarios**:

1. **Given** a client requests creation of a network configuration via `POST /config-and-control/networks`, **When** the network is created, **Then** the controller maps the resulting domain model into a `NetworkResponse` DTO and returns HTTP 201 Created with the response DTO payload.
2. **Given** a client updates an existing network configuration via `PUT /config-and-control/networks`, **When** the operation finishes, **Then** the controller returns a `NetworkResponse` DTO (not `NetworkConfig`) with HTTP 200 OK.
3. **Given** a client queries a single network configuration via `GET /config-and-control/networks/{id}`, **When** the entity is found, **Then** the controller returns a `NetworkResponse` DTO with HTTP 200 OK.
4. **Given** a client invokes pipeline creation or update operations (`POST /config-and-control/pipelines`, `PUT /config-and-control/pipelines/description`, `PUT /config-and-control/pipelines/transcription-factor`, `PUT /config-and-control/pipelines/expert-objects`, `PUT /config-and-control/pipelines/search-config`, `PUT /config-and-control/pipelines/aligned-expert-objects`), **When** the operation completes, **Then** the controller returns a `PipelineDetailResponse` DTO containing the updated configuration details (not the internal `PipelineConfig` domain model).
5. **Given** a client queries a single pipeline configuration via `GET /config-and-control/pipelines/{id}`, **When** the pipeline is retrieved, **Then** the controller returns a `PipelineDetailResponse` DTO with all configuration attributes.

---

### User Story 2 - Elimination of Business Branching and Conditional Logic in Delivery Adapters (Priority: P2)

As a Core Developer and Platform Maintainer, I need REST controllers to act purely as declarative, stateless delivery adapters that delegate all request validation, webhook payload interpretation, and conditional business branching to use cases, domain services, or validation frameworks, ensuring zero business logic resides in controllers.

**Why this priority**: Controllers that perform manual payload inspection, conditional event branching, or explicit error-response constructing (such as manual `BAD_REQUEST` JSON building or webhook event filtering) violate the Single Responsibility Principle and Constitution Principle V. Business decisions in delivery adapters bypass domain rules and cannot be reused or verified in core unit tests.

**Independent Test**: Can be verified by inspecting controller methods for branching keywords (`if`, `switch`, ternary operators) and verifying that request constraints are validated declaratively (via bean validation / exception mappers) and external webhook routing is encapsulated outside the controller body.

**Acceptance Scenarios**:

1. **Given** a client requests pipeline resumption via `POST /config-and-control/pipelines/resume`, **When** the request payload is evaluated, **Then** input constraints (such as non-null microservice target) are enforced declaratively via validation constraints rather than imperative conditional checks inside the controller method.
2. **Given** an external identity provider dispatches an event notification webhook via `POST /config-and-control/users/keycloak-events`, **When** the controller receives the notification, **Then** the controller delegates the event handling and identifier resolution directly to the application layer without inline conditional evaluation of event types or manual fallback parsing.
3. **Given** an invalid or unprocessable input across any controller endpoint, **When** validation fails or a domain exception is thrown, **Then** the error is handled uniformly by standard exception mappers rather than manually synthesized response objects in controller methods.

---

### User Story 3 - Static Mapper Encapsulation for Collection and Report Serialization (Priority: P3)

As a Platform Maintainer, I need collection transformation and pagination wrapping logic (such as converting paginated `ReportFormat<DomainModel>` to `ReportFormat<ResponseDTO>`) to be encapsulated within pure static mappers, so that controller methods remain concise 3-line dispatchers (Receive DTO -> Execute Use Case -> Map to Response DTO).

**Why this priority**: Repeated stream mapping (`modelFormat.list().stream().map(...).toList()`) and container reconstruction across `AdminController`, `NetworkController`, and `PipelineController` clutters controllers and introduces redundant transformation logic that belongs in static mappers (Constitution Principle V).

**Independent Test**: Can be verified by checking that `ReportFormat` transformations across all list endpoints delegate to dedicated static mapper methods (e.g. `UserMapper.toReportResponse(...)`, `NetworkMapper.toReportResponse(...)`, `PipelineMapper.toReportResponse(...)`) with zero stream pipelines inside controller bodies.

**Acceptance Scenarios**:

1. **Given** an administrator requests the user list via `GET /config-and-control/admin/users`, **When** the use case returns a paginated domain report, **Then** the controller converts the report using a single static mapper call (`UserMapper.toReportResponse`) and returns the resulting `ReportFormat<UserResponse>`.
2. **Given** a client searches network configurations via `GET /config-and-control/networks/list`, **When** paginated network models are returned, **Then** the controller transforms the report through `NetworkMapper.toReportResponse` without inline stream processing.
3. **Given** a client searches pipeline configurations via `GET /config-and-control/pipelines/list`, **When** paginated pipeline models are returned, **Then** the controller transforms the report through `PipelineMapper.toReportResponse` without inline stream processing.

---

### User Story 4 - Architectural Enforcement and Delivery Layer Quality Gates (Priority: P4)

As a Software Architect, I need automated architectural tests and compiler quality gates to continuously verify that no REST controller directly returns domain models, no controller bypasses Inbound Ports, and all controllers strictly comply with Hexagonal Architecture and Constitution Principle V.

**Why this priority**: Architectural compliance must be self-enforcing in CI/CD. Without automated quality gates, delivery layer impurities can easily re-enter the codebase over time.

**Independent Test**: Can be verified by running the project ArchUnit test suite and confirming that rules specifically check that controllers reside in `infrastructure.adapters.in.restcontrollers`, only depend on inbound ports, and do not expose domain model return types.

**Acceptance Scenarios**:

1. **Given** the test suite is executed, **When** `ArchitectureLayerTest` runs, **Then** all architectural rules pass with zero violations.
2. **Given** any new or refactored controller method, **When** static analysis or architecture tests evaluate public method signatures, **Then** zero methods in `infrastructure.adapters.in.restcontrollers..` expose return types located in `com.biopatternsg.domain.models..`.

---

### Edge Cases

- What happens when a query returns an empty collection or null result?
  - Static mappers safely handle empty collections returning empty lists in `ReportFormat`, preserving total count of 0.
- How does the system handle webhook notifications with unrecognized or unhandled event types?
  - The application layer ignores non-actionable events silently and returns an acknowledgment (HTTP 200 OK) without throwing unhandled exceptions to the webhook sender.
- What happens when an entity requested by ID does not exist?
  - The use case or domain layer throws `NotFoundException` or `UnprocessableEntityException`, which is automatically intercepted by Quarkus exception mappers and translated into standard HTTP 404 or 422 JSON error responses.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: REST controllers in `com.biopatternsg.infrastructure.adapters.in.restcontrollers` MUST NOT expose domain models (`com.biopatternsg.domain.models.*`) as public method return types or response entity payloads.
- **FR-002**: Endpoints in `NetworkController` returning network configurations MUST NOT expose domain models: `create` MUST return `Response` with HTTP 201 Created and `NetworkResponse` entity, while `update` and `findList(id)` MUST return strongly-typed `NetworkResponse` DTOs directly (with HTTP 200 OK), and `findList(filters)` MUST return `ReportFormat<NetworkResponse>`.
- **FR-003**: Endpoints in `PipelineController` returning pipeline configurations MUST NOT expose domain models: `create` MUST return `Response` with HTTP 201 Created and `PipelineDetailResponse` entity, while all update endpoints (`updateDescription`, `updateTranscriptionFactor`, `updateExpertObjects`, `updateSearchConfig`, `updateAlignedExpertObjects`) and `findList(id)` MUST return strongly-typed `PipelineDetailResponse` DTOs directly (with HTTP 200 OK) containing all pipeline configuration attributes (`levels`, `expertObjects`, `alignedExpertObjects`, `transcriptionFactorConfig`, `retMax`, `maxComplexes`, `statuses`), and `findList(filters)` MUST return `ReportFormat<PipelineResponse>` using lightweight summary DTOs.
- **FR-004**: REST controllers MUST NOT contain imperative conditional branching (`if`, `switch`) or manual validation checks to construct error responses; all validations MUST be declared via standard annotations or delegated to use cases.
- **FR-005**: The `resume` endpoint in `PipelineController` MUST validate request payloads declaratively (via `@Valid` / bean validation) rather than using imperative null checking and ad-hoc error map creation.
- **FR-006**: Webhook receivers (specifically `UserController.handleKeycloakEvent`) MUST NOT contain inline parsing or event branching logic; event evaluation and target identifier resolution MUST be encapsulated in a dedicated stateless infrastructure mapper (`KeycloakEventMapper`) returning an optional identifier, and `UserController` MUST dispatch directly to `UserManagement.processEmailVerification` when present.
- **FR-007**: Paginated collection mappings (`ReportFormat<DomainModel>` to `ReportFormat<ResponseDTO>`) across `AdminController`, `NetworkController`, and `PipelineController` MUST be encapsulated in static mapper utility methods (`UserMapper`, `NetworkMapper`, `PipelineMapper`).
- **FR-008**: Static mappers MUST remain stateless utility classes with pure static methods, containing no CDI bean annotations and no runtime state.
- **FR-009**: OpenAPI documentation annotations (`@APIResponse`, `@Content`, `@Schema`) on all controller methods MUST accurately reflect the corresponding Response DTO classes (`NetworkResponse`, `PipelineResponse`, `PipelineDetailResponse`, `UserResponse`) instead of domain models.
- **FR-010**: Automated architectural testing in `ArchitectureLayerTest` MUST enforce controller purity, confirming that controllers do not expose domain model return types and adhere to Hexagonal Architecture layer boundaries.

### Key Entities

- **Delivery Layer DTOs**: Data transfer objects (`NetworkResponse`, `PipelineResponse`, `PipelineDetailResponse`, `UserResponse`, `ReportFormat<T>`, `PipelineResumeResponse`, `KeycloakEventNotification`) defining the external API and webhook contracts.
- **Domain Models**: Core enterprise business aggregates (`NetworkConfig`, `PipelineConfig`, `UserConfig`, `PipelineStatus`) that must remain encapsulated within the domain and application layers.
- **Static Mappers**: Stateless transformation utilities (`NetworkMapper`, `PipelineMapper`, `UserMapper`, `KeycloakEventMapper`) translating between transport DTOs and domain models.
- **Inbound Ports**: Interface contracts (`CreateNetwork`, `UpdateNetwork`, `FindNetwork`, `CreatePipeline`, `UpdatePipeline`, `FindPipeline`, `ResumePipeline`, `UserManagement`) that controllers invoke exclusively.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of public endpoints in `infrastructure.adapters.in.restcontrollers` return delivery DTOs (`Response` wrapping DTO or direct DTO types), with 0 domain model types exposed in method signatures.
- **SC-002**: 0 imperative conditional branching statements (`if`, `switch`) for business logic or manual validation error construction exist inside REST controller classes.
- **SC-003**: 100% of paginated report transformations in controllers delegate to reusable static mapper methods, eliminating inline stream pipelines across controller classes.
- **SC-004**: 100% of automated architecture rules in `ArchitectureLayerTest` and all unit test suites pass cleanly with 0 violations.
- **SC-005**: All existing REST API client contracts remain backward-compatible in JSON structure while achieving complete internal decoupling from domain models.

## Assumptions

- The JSON serialization of `NetworkResponse` and `PipelineResponse` provides all fields required by existing frontend and external API consumers.
- Quarkus bean validation and exception mappers (`NotFoundExceptionMapper`, `ConstraintViolationExceptionMapper`, etc.) handle request validation failures and domain exceptions uniformly across the microservice.
- Webhook event handling for Keycloak events continues to process email verifications reliably while moving event evaluation into the application/domain layer.
- Existing unit tests for controllers (`UserControllerTest`, `PipelineControllerTest`) will be updated or expanded to reflect the pure delivery adapter behavior.
