# Feature Specification: Clean Code, Lambda Purity, and Static Mappers Standardization

**Feature Branch**: `006-clean-code-lambdas-mappers`

**Created**: 2026-11-20

**Status**: Draft

**Input**: User description: "/speckit-specify basado en /home/yacson/Proyectos/biopatternsg-ms/SDD/config-and-control/05-clean-code-lambdas-and-static-mappers-standardization"

## Clarifications

### Session 2026-11-20

- Q: ¿Dónde debe residir la transformación de símbolos biológicos (`List<String>`) a configuraciones de objetos expertos (`List<ExpertObjectConfig>`) que actualmente se ejecuta como lambda multilínea en `PubmedIntegrationAdapter`? → A: Opción A (Mapper estático reutilizable `PipelineMapper.symbolsToExpertObjects(List<String> symbols)` en `PipelineMapper`, garantizando funciones puras y reutilizables).
- Q: ¿Cómo debe abordarse la estandarización de nombres de métodos en los mappers estáticos? → A: Opción A (Estandarizar bajo la convención canónica `<origen>To<Destino>` en todos los mappers como `collectionToModel`, `modelToCollection`, `modelToResponse`, `requestToModel`, preservando los nombres contextuales existentes como alias delegados transparentes para máxima compatibilidad).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Lambda Purity and Extraction of Multi-line Helper Methods (Priority: P1)

As a Core Developer and Code Reviewer, I need functional stream expressions and lambda operations (`.map(...)`, `.filter(...)`, `.forEach(...)`, `.ifPresent(...)`, `.ifPresentOrElse(...)`) across domain, application, and infrastructure layers to remain pure, concise, and declarative by extracting multi-statement mutations, builders, and branching conditions into descriptive private helper methods, so that public methods read as clear business workflows rather than low-level procedural code.

**Why this priority**: Constitution Principle V (*Clean Code & Zero Business Logic in Delivery & Lambdas*) and Section 3 of `.agents/rules.md` strictly forbid multi-line lambdas containing branching logic, nested mutations, or complex builders. Procedural statements hidden within lambdas obscure business intent, hinder testability, and decrease code readability.

**Independent Test**: Can be verified by running static analysis and inspecting all functional lambda pipelines across the codebase, ensuring that zero functional pipelines contain block lambdas (`-> { ... }`) with branching statements, multi-line object builders, or imperative state mutations.

**Acceptance Scenarios**:

1. **Given** `PubmedIntegrationAdapter.executeGenerateAlignedObjects`, **When** transforming aligned expert objects into configuration records, **Then** the stream mapping delegates to a descriptive private helper method or pure static mapper function instead of executing an inline multi-line block lambda with setters.
2. **Given** `PipelineConfig.addStatusForStep`, **When** updating an existing step status in the aggregate statuses list, **Then** the `.ifPresentOrElse(...)` action delegates to a private helper method (`updateExistingStatusMetrics`) instead of executing an inline multi-line mutation block.
3. **Given** `UserController.handleKeycloakEvent`, **When** dispatching verified target user identifiers to the application service, **Then** the `.ifPresent(...)` action delegates to a private helper method or direct method reference.
4. **Given** any functional stream or Optional pipeline in the microservice, **When** reviewing lambda bodies, **Then** all lambdas are single-line pure expressions or direct method references (`Class::method`).

---

### User Story 2 - Standardization of Static Utility Mappers (Priority: P2)

As an Application Architect and Maintainer, I need all mapping classes in `com.biopatternsg.infrastructure.adapters.mappers` to adhere to standardized static utility conventions—including `final` class definitions, private constructors preventing instantiation, pure functions with zero side-effects, comprehensive null-safety, and uniform method naming patterns—so that data transformations between database documents, domain models, and transport DTOs are predictable and maintainable.

**Why this priority**: Constitution Principle V and Section 5 of `.agents/rules.md` dictate that mappers must be stateless utility classes with pure static methods. Inconsistent naming, lack of private constructors, or missing null checks create cognitive overhead and increase the risk of unexpected NullPointerExceptions or accidental instantiation.

**Independent Test**: Can be verified by inspecting all classes under `com.biopatternsg.infrastructure.adapters.mappers`, ensuring that all are `public final`, contain private constructors, expose only static methods, and consistently handle null inputs without throwing exceptions.

**Acceptance Scenarios**:

1. **Given** any mapper in `infrastructure.adapters.mappers` (`NetworkMapper`, `PipelineMapper`, `UserMapper`, `KeycloakEventMapper`), **When** inspecting class declarations, **Then** all classes are declared `public final` and define a private constructor to prevent instantiation.
2. **Given** any public mapping method across all mappers, **When** invoking the method with `null` input, **Then** the method safely returns `null` or a safe empty collection/envelope without throwing `NullPointerException`.
3. **Given** the method signatures across mappers, **When** evaluating naming conventions, **Then** all mappers adopt the canonical `<source>To<Target>` nomenclature (`collectionToModel`, `modelToCollection`, `modelToResponse`, `requestToModel`, `toDomainReport`, `toReportResponse`), while preserving existing contextual method names (`toNetworkConfig`, `collectionToConfig`, `configToCollection`) as delegating compatibility aliases.
4. **Given** the component scan or CDI container, **When** analyzing mapper classes, **Then** zero mappers contain CDI bean annotations (`@ApplicationScoped`, `@Singleton`, `@RequestScoped`) or mutable instance variables.

---

### User Story 3 - Repository Adapter Collection Transformation Standardization (Priority: P3)

As a Core Developer, I need outbound repository adapters (`NetworkRepositoryAdapter`, `PipelineRepositoryAdapter`, `UserRepositoryAdapter`) to transform paginated database report envelopes into domain report envelopes via dedicated static mapper methods (`toDomainReport`) rather than repeating manual stream mapping pipelines, ensuring repository adapters remain cohesive data dispatchers.

**Why this priority**: Repository adapters currently duplicate the same pattern of extracting `reportCollection.list().stream().map(...).toList()` and rebuilding `ReportFormat`. Centralizing this transformation within the respective static mappers enforces DRY principles and aligns outbound adapters with the clean delivery patterns established in REST controllers.

**Independent Test**: Can be verified by checking `findByFilters` / `list` methods in `NetworkRepositoryAdapter`, `PipelineRepositoryAdapter`, and `UserRepositoryAdapter`, confirming that collection transformation delegates to `toDomainReport` static methods with zero inline stream mapping.

**Acceptance Scenarios**:

1. **Given** `NetworkRepositoryAdapter.findByFilters`, **When** the database repository returns a paginated report collection, **Then** the adapter delegates report transformation to `NetworkMapper.toDomainReport(reportCollection)`.
2. **Given** `PipelineRepositoryAdapter.findByFilters`, **When** the database repository returns a paginated report collection, **Then** the adapter delegates report transformation to `PipelineMapper.toDomainReport(reportCollection)`.
3. **Given** `UserRepositoryAdapter.list`, **When** the database repository returns a paginated report collection, **Then** the adapter delegates report transformation to `UserMapper.toDomainReport(reportCollection)`.

---

### User Story 4 - Automated Architecture Quality Gates for Static Mappers (Priority: P4)

As a Software Architect, I need automated ArchUnit tests to continuously enforce that all classes in the mappers package are static utility classes, contain only static methods, have private constructors, and contain zero CDI bean annotations.

**Why this priority**: Automated quality gates ensure that clean code standards and static mapper conventions cannot be bypassed or degraded in future pull requests or new features.

**Independent Test**: Can be verified by executing `ArchitectureLayerTest` and confirming that static mapper rules pass cleanly with zero violations.

**Acceptance Scenarios**:

1. **Given** the architecture test suite in `ArchitectureLayerTest`, **When** checking `..infrastructure.adapters.mappers..`, **Then** ArchUnit rules verify that all classes in the package end with `Mapper`.
2. **Given** any class in `..infrastructure.adapters.mappers..`, **When** evaluated by ArchUnit, **Then** the rule confirms that all methods are `static` and no constructors are `public`.
3. **Given** any class in `..infrastructure.adapters.mappers..`, **When** evaluated by ArchUnit, **Then** the rule confirms that no class has CDI bean annotations (`@ApplicationScoped`, `@Singleton`).

---

### Edge Cases

- How do static mappers handle `null` collections or empty lists inside report envelopes?
  - Mappers safely return empty lists or `ReportFormat(0L, List.of())` when either the envelope or the inner list is `null`.
- How do mappers handle partially populated models or DTOs with missing optional fields?
  - Optional fields are mapped with null-safe builders or default values (e.g., boolean flags defaulting to safe false/true as specified by business requirements).
- What happens if reflection or serialization tools attempt to instantiate static mapper classes?
  - Private constructors prevent external instantiation; reflection instantiation throws `UnsupportedOperationException` or AssertionError if invoked.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: All lambda expressions and functional pipelines across the microservice MUST be pure, single-line expressions or method references; multi-statement block lambdas (`-> { ... }`) MUST NOT contain branching logic (`if`, `switch`) or multi-step object mutations.
- **FR-002**: Multi-statement lambdas in `PubmedIntegrationAdapter` MUST be eliminated by delegating the conversion of aligned symbols to `PipelineMapper.symbolsToExpertObjects`, while multi-statement mutations in `PipelineConfig.addStatusForStep` MUST be extracted into a private helper method (`updateExistingStatusMetrics`).
- **FR-003**: All mapper classes in `com.biopatternsg.infrastructure.adapters.mappers` MUST be declared `public final`.
- **FR-004**: All mapper classes in `com.biopatternsg.infrastructure.adapters.mappers` MUST define a private, no-argument constructor preventing instantiation.
- **FR-005**: All mapper methods MUST be pure, static, stateless functions with zero side-effects.
- **FR-006**: All mapper methods MUST provide null-safe conversions, returning `null` or safe empty structures when passed `null` inputs.
- **FR-007**: `NetworkMapper`, `PipelineMapper`, and `UserMapper` MUST implement `toDomainReport` methods to transform database `ReportFormat<Collection>` envelopes into domain `ReportFormat<Model>` envelopes.
- **FR-008**: Outbound repository adapters (`NetworkRepositoryAdapter`, `PipelineRepositoryAdapter`, `UserRepositoryAdapter`) MUST delegate paginated report conversions to their respective static mapper `toDomainReport` methods.
- **FR-009**: Mappers MUST NOT be annotated with CDI annotations (`@ApplicationScoped`, `@Singleton`, `@RequestScoped`).
- **FR-010**: Automated architectural testing in `ArchitectureLayerTest` MUST verify that all mapper classes end with `Mapper`, contain only static methods, have private constructors, and contain zero CDI annotations.

### Key Entities

- **Static Mappers**: Utility classes (`NetworkMapper`, `PipelineMapper`, `UserMapper`, `KeycloakEventMapper`) translating between database documents, domain models, and delivery DTOs.
- **Domain Aggregates & Models**: `PipelineConfig`, `NetworkConfig`, `UserConfig`, `ReportFormat<T>`.
- **Database Collections**: `PipelineCollection`, `NetworkCollection`, `UserCollection`.
- **Repository Adapters**: Outbound infrastructure adapters (`PipelineRepositoryAdapter`, `NetworkRepositoryAdapter`, `UserRepositoryAdapter`) implementing domain repository ports.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 0 multi-line block lambdas with imperative branching logic or multi-step object builders exist across `src/main/java`.
- **SC-002**: 100% of mapper classes in `com.biopatternsg.infrastructure.adapters.mappers` are `final`, have private constructors, and contain only pure static methods.
- **SC-003**: 100% of paginated report conversions in outbound repository adapters delegate to `toDomainReport` static mapper methods.
- **SC-004**: 100% of automated architecture tests in `ArchitectureLayerTest` pass cleanly with 0 violations.
- **SC-005**: Full regression test suite compiles cleanly and passes with 100% success rate.

## Assumptions

- Standardizing mapper method names and encapsulating repository report mappings will not alter database query executions or MongoDB Panache behavior.
- Java 21 LTS toolchain remains mandatory for all builds and tests (`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`).
- Existing public API contracts and JSON wire formats remain 100% backward-compatible.
