# Architectural Rules and Design Standards

> **Service:** `config-and-control`  
> **Package Base:** `com.biopatternsg.*`  
> **Framework:** Quarkus (Java 21) | MongoDB Panache  
> **Architecture:** Hexagonal (Ports & Adapters) / Clean Architecture  

This document establishes the mandatory **architectural rules, layer boundaries, component responsibilities, and documentation governance** that developers and AI agents must follow when creating or modifying features in the `config-and-control` service.

*(For Java language, clean code, lambdas, and static mapper coding rules, see [`java-rules.md`](java-rules.md)).*

---

## 1. Hexagonal Layer Boundaries & Isolation

1. **Domain Layer (`com.biopatternsg.domain.*`):**
   - Pure business core (`enums`, `exceptions`, `models`, `port/in`, `port/out`).
   - **MUST NOT** depend on `application..`, `infrastructure..`, or external delivery/persistence frameworks (e.g., `jakarta.ws.rs.*`, MongoDB Panache).
2. **Application Layer (`com.biopatternsg.application.*`):**
   - Contains Use Cases (`application.usecase.*`) and Application Services (`application.services.*`).
   - **MUST** depend only on `domain..` and **MUST NOT** depend on `infrastructure..`.
   - Can only be accessed from outer layers through Inbound Ports (`domain.port.in.*`) or Domain service contracts.
3. **Infrastructure Layer (`com.biopatternsg.infrastructure.*`):**
   - Contains inbound delivery adapters (`adapters.in.restcontrollers.*`), outbound persistence/client adapters (`adapters.out.repositories.*`), static mappers (`adapters.mappers.*`), DTOs (`dtos.*`), and database collections (`mongo_db.*`).

---

## 2. Use Cases (`com.biopatternsg.application.usecase`)

### 2.1. Single Entry-Point Responsibility
- Each Use Case represents a single business interaction and implements its dedicated Inbound Port interface defined under `com.biopatternsg.domain.port.in.*`.
- Use Cases must implement only the contract methods defined by their inbound port interface (typically `execute(...)`, or query variants like `byId(...)`, `byName(...)`, `byFilters(...)`).
- Concrete Use Case classes (`*UseCase.java`) must never be directly injected or instantiated across layers; callers must always depend on the Inbound Port interface.

### 2.2. Use Cases as Lightweight Orchestrators
Implementations (`*UseCase.java`) must remain lean and act purely as orchestrators:
- Validate input preconditions and existence of parent aggregates (e.g., verifying that a `network` exists before attaching a `pipeline`).
- Check business constraints and throw domain exceptions (`UnprocessableEntityException`, `UnauthorizedServiceException`).
- Coordinate execution by invoking outbound repository ports (`domain.port.out.repositories.*`) or application services (`application.services.*`).
- Delegate complex multi-step lifecycle transitions, metric aggregations, or asynchronous orchestrations to **Application Services** (e.g., `PipelineStepOrchestrator`, `PipelineService`, `PipelineExecutionService`).

### 2.3. Mandatory Use Case Documentation (`.agents/use_cases/`)
- **Mandatory English Language:** All Use Case documentation files under `.agents/use_cases/` (including `README.md` and every `<UseCaseName>.md` file) **must be written strictly in English**.
- **New Use Cases:** Whenever a new Use Case is created, a corresponding markdown document must be created in English under `.agents/use_cases/<UseCaseName>.md` describing:
  1. Business problem and purpose.
  2. Input and output contracts (Inbound port, Outbound ports, Application Services).
  3. Step-by-step execution flow.
  4. Sequence diagram using Mermaid format (`sequenceDiagram`).
  5. Exceptions and validation failure scenarios.
- **Modifications:** Whenever an existing Use Case signature, flow, or rule is modified, its documentation in `.agents/use_cases/<UseCaseName>.md` must be updated synchronously in English.
- **Mandatory Index References (`README.md` & `architecture.md`):** Whenever a new Use Case documentation file is added (or an existing one is renamed/repurposed), its reference and link **must** be added/updated in both:
  1. `.agents/use_cases/README.md` (the Use Cases catalog index).
  2. Section 5 of `.agents/architecture.md`.

---

## 3. Application Services (`com.biopatternsg.application.services`)

### 3.1. Encapsulating Complex Business Logic and Step Transitions
- Multi-step biological workflow progression, state machine transitions, step metric recalculations, and phase transitions belong in **Application Services**.
- Examples:
  - `PipelineService`: Handles step updates, metric recording, and state persistence.
  - `PipelineStepOrchestrator`: Coordinates which external services (`inferences`, `pubmed-integration`) to invoke based on pipeline step completions.

### 3.2. Strict Encapsulation and Interface-Implementation Separation
For any new or refactored Application Service:
1. **Interface Contract:** The public contract must be defined as an interface (e.g., `com.biopatternsg.application.services.*` or domain contract).
2. **Hidden Implementation:** The implementation class must reside in an `impl/` subpackage (or dedicated service implementation class) annotated with `@ApplicationScoped`.
3. **Encapsulation:** Concrete implementation classes must not expose internal helper methods publicly. Only the interface contract methods should be visible outside the package.
4. **CDI Injection:** Always inject the **interface** into Use Cases, never the concrete implementation class.

### 3.3. Service Injection and Dependencies
- Application Services may coordinate multiple Outbound Ports (`domain.port.out.*`).
- **Prohibition:** Application Services must **never** be injected directly into REST Controllers; all external traffic must enter via Inbound Ports and Use Cases (`domain.port.in.*`).

---

## 4. REST Controllers (`com.biopatternsg.infrastructure.adapters.in.restcontrollers`)

### 4.1. Zero Business Logic in Controllers
- Controllers are strictly delivery adapters:
  1. Receive HTTP request / payload DTO (`com.biopatternsg.infrastructure.dtos.*`).
  2. Convert request DTOs to Domain Models using static mappers (`com.biopatternsg.infrastructure.adapters.mappers.*`).
  3. Invoke the Use Case through its Inbound Port interface.
  4. Convert the Domain Model result to a Response DTO using the static mapper and return it.
- **No** business branching (`if/else`), existence queries, or domain calculations are allowed inside controller methods.

### 4.2. Business Exceptions in Domain/Application Layers
- Existence checks and business rule validations belong in Use Cases or Application Services.
- Controllers must never manually query repositories or throw operational business exceptions.
- Domain exceptions (`com.biopatternsg.domain.exceptions.*`) are mapped centrally to HTTP status codes by Quarkus Exception Mappers in `infrastructure.exceptions`.

---

## 5. Persistence & Outbound Adapters (`com.biopatternsg.infrastructure.adapters.out.*`)

### 5.1. Repository Adapters
- Implement repository interfaces defined in `com.biopatternsg.domain.port.out.repositories.*`.
- Separate concerns: MongoDB collections (`infrastructure.mongo_db.collections.*`) must be converted to Domain Models (`domain.models.*`) via Static Mappers before returning to the application layer.

### 5.2. External REST Clients
- External microservices (`biological-objects`, `inferences`, `pubmed-integration`, `keycloak`) are consumed via MicroProfile REST Client interfaces under `infrastructure.clients.*` and wrapped by outbound adapters under `infrastructure.adapters.out.repositories.*`.

---

## 6. Architectural Naming & Verification Gates

### 6.1. Mandatory Naming Conventions (Enforced by ArchUnit)
- Classes in `application.usecase..` must end with `UseCase` (e.g., `CreatePipelineUseCase`).
- Classes in `infrastructure.adapters.in.restcontrollers..` must end with `Controller` (e.g., `PipelineController`).
- Classes in `infrastructure.adapters.out..` must end with `Adapter` (e.g., `PipelineRepositoryAdapter`).

### 6.2. Mandatory Verification Commands
Before completing any task, verify architectural and documentation compliance:
```bash
# 1. Run Hexagonal Architecture validation tests
./mvnw test -Dtest=ArchitectureLayerTest

# 2. Run Use Case Documentation governance tests
./mvnw test -Dtest=DocumentationGovernanceTest

# 3. Compile and ensure zero errors
./mvnw clean compile
```
