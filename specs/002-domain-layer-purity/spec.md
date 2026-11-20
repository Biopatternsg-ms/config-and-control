# Feature Specification: Domain Layer Purity and Framework Decoupling

**Feature Branch**: `002-domain-layer-purity`

**Created**: 2026-11-19

**Status**: Draft

**Input**: User description: "basado en /home/yacson/Proyectos/biopatternsg-ms/SDD/config-and-control/01-domain-layer-purity-and-framework-decoupling"

## Clarifications

### Session 2026-11-19

- Q: ¿Cuál debería ser el tipo de retorno del método del puerto de salida de dominio `KeycloakRepository.register(UserConfig newUser)`? → A: Opción A (`String` retornando directamente el `identityProviderId` generado).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Pure Domain Port Contracts for Identity Management (Priority: P1)

As a Core Application Developer, I need domain outbound port contracts for identity management to return domain-level representations (specifically a `String` containing the generated `identityProviderId`) instead of transport-specific protocol response objects, so that the domain and application layers remain completely decoupled from external web frameworks and delivery protocols.

**Why this priority**: Exposing transport-specific response objects across domain ports violates Hexagonal Architecture and constitutional layer isolation, forcing application orchestration logic to parse transport protocol headers directly.

**Independent Test**: Can be verified by inspecting domain port interfaces to ensure no transport or web library types are exposed, and validating that application use cases receive pure domain representations upon user creation.

**Acceptance Scenarios**:

1. **Given** a new user configuration requiring registration, **When** the application orchestrates user creation via the identity management outbound port, **Then** the port returns a domain identifier string (`identityProviderId`) representing the registered identity without exposing transport response wrappers.
2. **Given** an external identity provider returning a protocol response with location metadata, **When** the outbound adapter processes the response, **Then** the adapter encapsulates the extraction of the identifier and returns the `String` user identifier directly, masking all transport status and header details from the domain and application layers.

---

### User Story 2 - Elimination of Forbidden Domain Services Package (Priority: P2)

As a System Maintainer and Architect, I need pipeline coordination and orchestration service contracts to reside exclusively in the application layer, ensuring the domain layer contains only core models, enums, exceptions, and ports.

**Why this priority**: Placing service abstractions within a domain services package and maintaining mismatched package declarations violates the established architectural specification and constitutional service encapsulation principles, creating structural confusion around service boundaries.

**Independent Test**: Can be verified by confirming that pipeline service contracts and implementations are located in the application services namespace and verifying that no service package exists within the domain module.

**Acceptance Scenarios**:

1. **Given** pipeline execution workflows requiring coordination, **When** developers reference the pipeline coordination contracts, **Then** the contract interface and its implementation are imported exclusively from the application service package.
2. **Given** the source codebase structure, **When** checking the directory hierarchy and package declarations of the domain layer, **Then** zero service packages or service classes exist under the domain root namespace.

---

### User Story 3 - Automated Hexagonal Layer Isolation Verification (Priority: P3)

As a Platform Quality Assurance Engineer, I need automated architectural tests that enforce strict isolation of the domain layer, so that any accidental future introduction of framework dependencies or invalid layer couplings is prevented at build time.

**Why this priority**: Architectural compliance must be continuously and objectively verifiable through automated quality gates to prevent architecture erosion over time.

**Independent Test**: Can be verified by executing the automated architectural test suite, ensuring that the domain layer isolation checks pass with zero violations.

**Acceptance Scenarios**:

1. **Given** the domain layer source files, **When** the automated architectural layer isolation rule is evaluated, **Then** the verification passes with zero violations, proving no domain class depends on application, infrastructure, or external web transport packages.
2. **Given** any static analysis or compilation check, **When** inspecting imports across all domain packages, **Then** zero references to external web or transport frameworks are present.

---

### Edge Cases

- What happens when an external identity provider returns an error status (e.g., conflict or unauthorized) during registration?
  - The outbound infrastructure adapter MUST intercept the transport error and map it into a standardized domain exception before propagating to the application layer.
- What happens if the external response location metadata is missing or malformed?
  - The outbound infrastructure adapter MUST handle the missing identifier as an integration failure and raise a domain exception rather than propagating a null pointer to the application use case.
- What happens if other classes previously imported the service from the old domain package?
  - All existing usages across the codebase MUST be updated to import the service interface from its new location in the application services package without disrupting existing orchestration flows.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Domain outbound ports for identity management MUST NOT declare or expose transport-specific or web framework response types.
- **FR-002**: Outbound identity management registration operation `KeycloakRepository.register(UserConfig newUser)` MUST return a `String` containing the newly created `identityProviderId` (Keycloak user identifier) directly.
- **FR-003**: Outbound infrastructure adapters implementing identity management ports MUST encapsulate all transport-specific response parsing, status verification, and location header extraction internally, returning the user identifier string.
- **FR-004**: Application use cases (`UserManagementUseCase`) MUST interact with outbound ports strictly through domain models and primitives, receiving the `identityProviderId` directly, and MUST NOT parse or inspect transport-level protocol headers.
- **FR-005**: The pipeline orchestration service contract (`PipelineService`) MUST reside within the application services package, complying with microservice package architecture.
- **FR-006**: The pipeline orchestration implementation (`PipelineServiceImpl`) MUST declare a package matching its physical location within the application services namespace.
- **FR-007**: The subpackage namespace for domain services (`com.biopatternsg.domain.services`) MUST be completely removed from the codebase.
- **FR-008**: The domain layer MUST NOT import or depend upon external web frameworks or transport libraries.
- **FR-009**: The microservice architecture verification test suite MUST enforce domain layer isolation and pass with zero rule violations.

### Key Entities *(include if feature involves data)*

- **UserConfig**: Domain model representing user identity configuration and attributes needed for user provisioning.
- **Identity Provider Identifier**: Domain primitive (`String`) representing the unique external subject ID (`identityProviderId`) generated upon successful user registration.
- **PipelineService Contract**: Application-level service interface defining biological pipeline orchestration, step transitions, and execution flow.
- **KeycloakRepository Port**: Domain outbound port defining the contract for user provisioning and identity management decoupled from transport details.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of methods in domain outbound ports return pure domain models or primitives, with 0 transport protocol wrappers exposed.
- **SC-002**: 0 files under the domain layer package contain imports from web transport libraries or external delivery frameworks.
- **SC-003**: 0 classes or directory paths exist under the domain services package, achieving 100% removal of forbidden domain service namespaces.
- **SC-004**: 100% pass rate in automated architecture verification rules during build and test execution.
- **SC-005**: 100% backward compatibility of business orchestration behavior in user registration and pipeline execution flows.

## Assumptions

- The external Keycloak HTTP endpoints, schemas, and credentials remain unchanged; only the internal adapter boundary and contract signatures are refactored.
- Moving `PipelineService` to application services maintains the exact same method signatures required by existing use cases.
- Automated architecture test suite serves as the definitive gate for continuous verification of Hexagonal Architecture boundaries.
- No other domain outbound ports currently expose transport-specific response types beyond the identity management repository.
