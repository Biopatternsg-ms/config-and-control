# Development Rules and Coding Standards

> **Service:** `config-and-control`  
> **Package Base:** `com.biopatternsg.*`  
> **Framework:** Quarkus (Java 21) | MongoDB Panache  
> **Architecture:** Hexagonal (Ports & Adapters) / Clean Architecture  

This document establishes the mandatory design, architectural, and clean code rules that developers and AI agents **must follow** when creating or modifying code in the `config-and-control` service.

---

## 1. Use Cases (`com.biopatternsg.application.usecase`)

### 1.1. Single Entry-Point Responsibility
Each Use Case represents a single business interaction and implements its dedicated input port interface defined under `com.biopatternsg.domain.port.in.*`.
- Use Cases must implement the contract method defined by their inbound port interface (typically `execute(...)`, or specialized query variants when defined in port contracts like `byId(...)`, `byName(...)`, `byFilters(...)`).
- Do not add arbitrary public methods outside of the port contract.

### 1.2. Use Cases as Lightweight Orchestrators
Implementations (`*UseCase.java`) must remain lean and act primarily as orchestrators:
- Validate input preconditions and existence of parent aggregates (e.g., verifying that a `network` exists before attaching a `pipeline`).
- Check business constraints and throw domain exceptions (`UnprocessableEntityException`, `UnauthorizedServiceException`).
- Coordinate execution by invoking repository ports (`domain.port.out.repositories.*`) or application services (`application.services.*`).
- Complex multi-step lifecycle transitions or asynchronous orchestrations must be delegated to **Application Services** (e.g., `PipelineStepOrchestrator`, `PipelineService`).

### 1.3. Parameter Encapsulation (Domain Commands & Models)
- No method in a Use Case or Service should accept more than **3 individual parameters**.
- Operations requiring multiple inputs must be encapsulated into Domain Models or Command records/classes located under `com.biopatternsg.domain.models.*` (e.g., `NetworkConfig`, `PipelineConfig`, `PipelineStatus`, `UserConfig`).
- Domain models represent logical structures of the business and do not depend on MongoDB or external transport models.

### 1.4. Mandatory Use Case Documentation (`.agents/use_cases/`)
- **New Use Cases:** Whenever a new Use Case is created, a corresponding markdown document must be created under `.agents/use_cases/<UseCaseName>.md` describing:
  1. What business problem it addresses.
  2. Input and output contracts (Inbound port, Outbound ports).
  3. Step-by-step execution flow.
  4. Sequence diagram using Mermaid format (`sequenceDiagram`).
  5. Exceptions and validation failure scenarios.
- **Modifications:** Whenever an existing Use Case signature, flow, or rule is modified, its documentation in `.agents/use_cases/<UseCaseName>.md` must be updated synchronously.
- **Index Reference:** Register or update the reference in Section 5 of `.agents/architecture.md`.

---

## 2. Application Services (`com.biopatternsg.application.services`)

### 2.1. Encapsulating Complex Business Logic and Step Transitions
- Multi-step biological workflow progression, state machine transitions, step metric recalculations, and phase transitions belong in **Application Services**.
- Examples in this service:
  - `PipelineService`: Handles step updates, metric recording, and state persistence.
  - `PipelineStepOrchestrator`: Coordinates which external services (`inferences`, `pubmed-integration`) to invoke based on pipeline step completions.

### 2.2. Strict Encapsulation and Interface-Implementation Separation
Although some legacy services might still be concrete classes, the **mandatory rule for any new or refactored Application Service** is:
1. **Interface Contract:** The public contract must be defined as an interface placed directly at `com.biopatternsg.application.services.*` (or within a domain-specific subpackage if grouped by context, e.g. `services.pipeline.PipelineService`).
2. **Hidden Implementation:** The implementation class must reside in an `impl/` subpackage (e.g. `services.pipeline.impl.PipelineServiceImpl`).
3. **Encapsulation (Package-Private where applicable):** Concrete implementation classes must not expose internal helper methods publicly. Only the interface methods should be visible outside the package.
4. **CDI Injection:** Mark the implementation with `@ApplicationScoped` and inject the **interface** into Use Cases, never the concrete implementation class.

### 2.3. Service Injection and Dependencies
- Application Services can coordinate multiple Outbound Repository Ports (`domain.port.out.repositories.*`).
- Use Cases inject Application Service interfaces via constructor injection (`@RequiredArgsConstructor` from Lombok or explicit CDI constructor).
- **Prohibition:** Application Services must **never** be injected directly into Controllers; all external traffic must enter via Use Cases (`domain.port.in.*`).

---

## 3. Clean Code & Lambda Practices

### 3.1. Zero Business Logic & Zero Complex Builders Inside Lambdas
- Never embed multi-line business calculations, branching `if/else`, or extensive object builders inside lambda expressions (`.map(...)`, `.filter(...)`, `.orElseGet(...)`).
- Lambdas must delegate to descriptive private helper methods.

```java
// ❌ INCORRECT: Inline logic inside lambda
return dataList.stream()
        .map(item -> {
            String id = item.getId();
            // multiple lines of conversion and conditional logic
            return new ConvertedObject(id);
        })
        .toList();

// ✅ CORRECT: Lambda delegates to a descriptive helper method
return dataList.stream()
        .map(this::toDomainEntity)
        .toList();
```

### 3.2. Single Responsibility Private Methods
- Extract multi-step operations into dedicated private methods that reflect business intent (e.g., `updateDescription(...)`, `updateTranscriptionFactor(...)`, `buildExecutionMetrics(...)`).
- Keeps the public methods readable as high-level flows of business intent.

---

## 4. REST Controllers (`com.biopatternsg.infrastructure.adapters.in.restcontrollers`)

### 4.1. Zero Business Logic in Controllers
- Controllers are strictly delivery adapters:
  1. Receive HTTP request / payload DTO (`com.biopatternsg.infrastructure.dtos.*`).
  2. Convert request DTOs to Domain Models using static mappers (`com.biopatternsg.infrastructure.adapters.mappers.*`).
  3. Invoke the Use Case through its Inbound Port interface.
  4. Convert the Domain Model result to a Response DTO using the static mapper and return it.
- **No** validation branching, existence queries, or business rules inside controller methods.

### 4.2. Business Exceptions in Domain/Application Layers
- Existence checks and business rule validations belong in Use Cases / Services.
- Controllers must never manually check entity existence to throw business exceptions.
- Exceptions are defined in `com.biopatternsg.domain.exceptions.*` (e.g., `UnprocessableEntityException`, `UnauthorizedServiceException`) and mapped to HTTP status codes by Quarkus Exception Mappers in `infrastructure.exceptions`.

### 4.3. Naming Conventions
- Must reside in `com.biopatternsg.infrastructure.adapters.in.restcontrollers`.
- Class names must strictly end with `Controller` (e.g., `PipelineController`, `NetworkController`, `UserController`, `AdminController`).

---

## 5. Static Mappers (`com.biopatternsg.infrastructure.adapters.mappers`)

### 5.1. Stateless and Pure Static Methods
- Located in `com.biopatternsg.infrastructure.adapters.mappers.*`.
- Pure static utility classes:
  - Methods: `toModel(...)`, `toEntity(...)`, `toResponse(...)`, `toResponseList(...)`.
  - No CDI injection (`@ApplicationScoped`) or mutable state.
  - No reliance on runtime reflection or bytecode-generation mapping libraries.

---

## 6. Persistence & Outbound Adapters (`com.biopatternsg.infrastructure.adapters.out.*`)

### 6.1. Repository Adapters
- Implement repository interfaces defined in `com.biopatternsg.domain.port.out.repositories.*`.
- Class names must strictly end with `Adapter` (e.g., `NetworkRepositoryAdapter`, `PipelineRepositoryAdapter`).
- Separate concerns: MongoDB collections (`infrastructure.mongo_db.collections.*`) are converted to Domain Models (`domain.models.*`) via Mappers before returning to the application layer.

### 6.2. External REST Clients
- External microservices (`biological-objects`, `inferences`, `pubmed-integration`, `keycloak`) are consumed via MicroProfile REST Client interfaces under `infrastructure.clients.*` and wrapped by outbound adapters under `infrastructure.adapters.out.repositories.*`.

---

## 7. Architectural Integrity and Verification

### 7.1. ArchUnit Rules Enforcement
Before finishing any modification or new feature, architectural tests must pass:
1. **Domain Isolation:** `domain..` must not depend on `application..` or `infrastructure..`.
2. **Application Isolation:** `application..` must not depend on `infrastructure..`.
3. **Use Case Isolation:** `application.usecase..` classes must not be directly invoked or accessed across layers except through Inbound Ports (`domain.port.in..`).
4. **Naming Enforcements:**
   - Classes in `application.usecase..` must end with `UseCase`.
   - Classes in `infrastructure.adapters.in.restcontrollers..` must end with `Controller`.
   - Classes in `infrastructure.adapters.out..` must end with `Adapter`.

### 7.2. Mandatory Verification Commands
```bash
# 1. Run architecture validation tests
./mvnw test -Dtest=ArchitectureLayerTest

# 2. Compile and ensure zero errors
./mvnw clean compile
```
