<!--
Sync Impact Report:
- Version change: Uninitialized Template -> 1.0.0
- Added sections:
  * Core Principles (Hexagonal Architecture Isolation, Use Case as Lightweight Orchestrators, Application Services Encapsulation, Mandatory Use Case Documentation, Zero Business Logic in Delivery & Lambdas, ArchUnit & Quality Gates)
  * Technology Stack & Constraints
  * Development Workflow & Synchronization
  * Governance
-->

# config-and-control Constitution

This constitution establishes the mandatory architectural principles, design constraints, and clean code standards for the `config-and-control` microservice (BioPatternsG ecosystem). All developers, tools, and AI agents operating via Spec-Kit or any agentic workflow **MUST** strictly adhere to these principles.

---

## Core Principles

### I. Strict Hexagonal Architecture & Layer Isolation (NON-NEGOTIABLE)
- **Domain Purity:** The domain layer (`com.biopatternsg.domain.*`) MUST NOT depend on application, infrastructure, or external frameworks. Domain models represent business realities, not database schemas.
- **Application Independence:** The application layer (`com.biopatternsg.application.*`) MUST depend only on the domain layer and MUST NOT have any direct dependency on infrastructure or framework-specific persistence/delivery mechanisms.
- **Inbound Access Control:** Concrete Use Case implementations (`application.usecase.*`) MUST NOT be directly instantiated or accessed across layers; they MUST be invoked exclusively through their Inbound Port interfaces (`domain.port.in.*`).
- **Naming Conventions:**
  - Classes in `application.usecase..` MUST end with `UseCase`.
  - Classes in `infrastructure.adapters.in.restcontrollers..` MUST end with `Controller`.
  - Classes in `infrastructure.adapters.out..` MUST end with `Adapter`.

### II. Use Cases as Lightweight Orchestrators
- Every Use Case represents a single business interaction implementing an Inbound Port contract.
- Use Cases MUST contain only orchestration logic: input pre-condition validation, parent aggregate existence verification, invoking application services or repository ports, and returning domain models.
- Parameter Limit: No method in a Use Case or Service should accept more than **3 individual parameters**. Any operation requiring more inputs MUST encapsulate them into a Domain Model / Command class under `domain.models.*` (e.g., `NetworkConfig`, `PipelineConfig`, `UserConfig`).
- Any complex, multi-entity, or asynchronous orchestration MUST be delegated to Application Services.

### III. Application Services Encapsulation (Strict Interface & Hidden Impl)
- Complex operational workflows, biological state machine progressions (`PipelineSteps`), metrics calculation, and cross-service orchestration MUST reside in Application Services (`com.biopatternsg.application.services.*`).
- **Mandatory Interface Separation:** Every Application Service MUST expose a public interface contract at `application.services.<context>.*` and hide its implementation class in an `impl/` subpackage (e.g., `PipelineServiceImpl`).
- **No Controller Bypass:** Controllers MUST NEVER inject or invoke Application Services directly; all external interactions must enter strictly through Inbound Ports and Use Cases.

### IV. Mandatory Synchronous Use Case Documentation (NON-NEGOTIABLE)
- **Documentation Synchronization Rule:**
  - Whenever an existing Use Case is modified (signature, orchestration flow, rules, or inputs/outputs), its corresponding markdown documentation in `.agents/use_cases/<UseCaseName>.md` MUST be updated immediately in the same change set.
  - Whenever a new Use Case is created, a dedicated documentation file MUST be created under `.agents/use_cases/<UseCaseName>.md` including:
    1. Business problem and intent.
    2. Inbound port and Outbound ports involved.
    3. Step-by-step execution flow.
    4. Mermaid sequence diagram (`sequenceDiagram`).
    5. Error cases and domain exceptions thrown.
  - The new or modified Use Case MUST be registered in the index table of `.agents/architecture.md`.

### V. Clean Code & Zero Business Logic in Delivery & Lambdas
- **REST Controllers:** Controllers (`infrastructure.adapters.in.restcontrollers.*`) are purely delivery adapters. They MUST NOT contain business logic, entity existence queries, or conditional business branching. Their responsibility is strictly: Receive DTO -> Map to Domain via static mapper -> Execute Use Case -> Map Domain result to Response DTO.
- **Lambda Purity:** Multi-line business logic, branching conditions, and extensive builders inside lambda expressions (`.map(...)`, `.filter(...)`, `.orElseGet(...)`) are FORBIDDEN. Lambdas must delegate to descriptive private helper methods.
- **Static Mappers:** Mappers in `infrastructure.adapters.mappers.*` MUST be stateless utility classes with pure static methods (`toModel`, `toEntity`, `toResponse`). No runtime reflection or CDI beans.

### VI. ArchUnit & Quality Gates Enforcement
- The automated architecture test suite in `src/test/java/archtest/ArchitectureLayerTest.java` MUST pass without violations on every feature or change:
  - Domain isolation check.
  - Application layer isolation check.
  - Use case layer access isolation check.
  - Naming conventions check for UseCases, Controllers, and Adapters.
- The build must succeed cleanly with zero test failures (`./mvnw clean compile && ./mvnw test`).

---

## Technology Stack & Constraints

- **Language & Runtime:** Java 21, Quarkus framework (Supersonic Subatomic Java).
- **Database:** MongoDB with Quarkus Panache (`PanacheMongoRepositoryBase`). Collections mapped under `infrastructure.mongo_db.collections.*`.
- **Identity & Security:** Keycloak OIDC (via MicroProfile REST client and Keycloak Admin API).
- **External Communications:** MicroProfile REST Client (`@RegisterRestClient`) with fault tolerance (`@Retry`).

---

## Development Workflow & Synchronization

1. **Spec & Plan:** Define requirements and verify architectural boundaries prior to code changes.
2. **Implement:**
   - Add/modify domain models and port interfaces first.
   - Implement use case / service orchestration following clean code rules.
   - Implement infrastructure adapters (controller / repository adapter / mappers).
3. **Synchronize Docs:** Update or create `.agents/use_cases/<UseCaseName>.md` and `.agents/architecture.md`.
4. **Verify:** Run ArchUnit and unit tests (`./mvnw test -Dtest=ArchitectureLayerTest`).

---

## Governance

- This constitution supersedes conflicting guidelines and informal conventions.
- All Pull Requests, code reviews, and Spec-Kit implementations MUST verify compliance with these principles.
- Amendments to this constitution require explicit review, version bumping, and recording in the sync impact header.

**Version**: 1.0.0 | **Ratified**: 2026-10-16 | **Last Amended**: 2026-10-16
