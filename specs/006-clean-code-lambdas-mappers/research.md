# Technical Research: Clean Code, Lambda Purity, and Static Mappers Standardization

**Feature**: `006-clean-code-lambdas-mappers` | **Date**: 2026-11-20

## 1. Lambda Purity & Helper Method Extraction

### Decision
Extract all multi-statement functional block lambdas (`-> { ... }`) in `src/main/java` into pure static functions or private descriptive helper methods:
1. **`PubmedIntegrationAdapter.executeGenerateAlignedObjects`**:
   - Current: Inline block lambda `.map(symbol -> { ExpertObjectConfig config = new ExpertObjectConfig(); config.setSymbol(symbol); return config; })`.
   - Resolution: Move conversion to pure static utility method `PipelineMapper.symbolsToExpertObjects(List<String> symbols)`.
   - Call site becomes: `List<ExpertObjectConfig> expertObjects = PipelineMapper.symbolsToExpertObjects(pipelineConfig.getAlignedExpertObjects());` if non-empty, otherwise `pipelineConfig.getExpertObjects()`.
2. **`PipelineConfig.addStatusForStep`**:
   - Current: Inline block lambda inside `.ifPresentOrElse(ps -> { ps.setCreatedAt(new Date()); if (metrics != null) ps.setMetrics(metrics); }, ...)`.
   - Resolution: Extract action to `private void updateExistingStatusMetrics(PipelineStatus ps, Map<String, String> metrics)`.
   - Call site becomes: `.ifPresentOrElse(ps -> updateExistingStatusMetrics(ps, metrics), () -> this.statuses.add(new PipelineStatus(step, status, new Date(), metrics)));`.
3. **`UserController.handleKeycloakEvent`**:
   - Current: Inline block lambda `.ifPresent(targetId -> { log.info(...); userManagement.processEmailVerification(targetId); });`.
   - Resolution: Extract action to private helper method `private void dispatchEmailVerification(String targetId)`.
   - Call site becomes: `.ifPresent(this::dispatchEmailVerification);`.
4. **`PipelineStepOrchestratorImpl` switch rules**:
   - Evaluated Java 21 switch blocks (`case SEARCH_LEVELS -> { ... }`). These are language switch rules, not functional lambdas or streams. They already adhere to Constitution Principle III and remain untouched.

### Rationale
- Complies strictly with Constitution Principle V and Section 3 of `.agents/rules.md`.
- Functional pipelines read as clear, declarative expressions without nested branching or procedural mutations.
- Pure mapping methods in `PipelineMapper` can be independently tested with unit tests without mocking.

### Alternatives Considered
- *Inline private method in PubmedIntegrationAdapter*: Rejected because transforming string symbols to `ExpertObjectConfig` is a pure data mapping concern that belongs to `PipelineMapper`, enabling reuse and testability.

---

## 2. Static Utility Mappers Standardization

### Decision
Standardize all mapper classes in `com.biopatternsg.infrastructure.adapters.mappers` (`NetworkMapper`, `PipelineMapper`, `UserMapper`, `KeycloakEventMapper`):
1. **Class Declaration**: `public final class <Name>Mapper`.
2. **Constructor**: `private <Name>Mapper() { throw new UnsupportedOperationException("Utility class cannot be instantiated"); }`.
3. **Stateless Purity**: Zero instance variables, zero state mutation, 100% `public static` methods.
4. **No CDI Annotations**: Zero `@ApplicationScoped`, `@Singleton`, `@RequestScoped`.
5. **Null Safety**: Every method must return `null` or a safe empty collection/envelope when passed `null` input without throwing `NullPointerException`.
6. **Canonical Nomenclature with Delegating Aliases**:
   - Adopt canonical `<source>To<Target>` names:
     - `collectionToModel(Collection c)`
     - `modelToCollection(Model m)`
     - `modelToResponse(Model m)`
     - `requestToModel(Request r)`
     - `toDomainReport(ReportFormat<Collection> r)`
     - `toReportResponse(ReportFormat<Model> r)`
   - Maintain legacy contextual names as delegating aliases (`@Deprecated(forRemoval = false)` or clean passthrough):
     - `NetworkMapper.toNetworkConfig(...)` delegates to `NetworkMapper.collectionToModel(...)`
     - `NetworkMapper.toNetworkCollection(...)` delegates to `NetworkMapper.modelToCollection(...)`
     - `NetworkMapper.toNetworkResponse(...)` delegates to `NetworkMapper.modelToResponse(...)`
     - `PipelineMapper.collectionToConfig(...)` delegates to `PipelineMapper.collectionToModel(...)`
     - `PipelineMapper.configToCollection(...)` delegates to `PipelineMapper.modelToCollection(...)`
     - `PipelineMapper.configToResponse(...)` delegates to `PipelineMapper.modelToResponse(...)`
     - `PipelineMapper.configToDetailResponse(...)` remains as specialized canonical detail mapper.

### Rationale
- Predictable and uniform API across all entity contexts.
- Prevents compilation breaks across existing tests and consumers while transitioning codebase to clean canonical standard.
- Enforces effective Java utility class idiom (Joshua Bloch, Item 4).

---

## 3. Repository Adapter Collection Transformation (`toDomainReport`)

### Decision
Introduce `toDomainReport(ReportFormat<Collection> reportCollection)` in `NetworkMapper`, `PipelineMapper`, and `UserMapper`.
Refactor `NetworkRepositoryAdapter.findByFilters`, `PipelineRepositoryAdapter.findByFilters`, and `UserRepositoryAdapter.list` to replace:
```java
var reportCollection = repositoryDB.findByUserAndFilters(...);
var modelList = reportCollection.list().stream().map(Mapper::collectionToModel).toList();
return new ReportFormat<>(reportCollection.count(), modelList);
```
with:
```java
var reportCollection = repositoryDB.findByUserAndFilters(...);
return Mapper.toDomainReport(reportCollection);
```

### Rationale
- Eliminates duplicated boilerplates across repository adapters.
- Guarantees consistent null-safety for report collections (e.g. `reportCollection == null` or `reportCollection.list() == null` safely return `new ReportFormat<>(0L, List.of())`).
- Keeps repository adapters focused strictly on DB query execution and session parameter retrieval.

---

## 4. ArchUnit Quality Gates for Static Mappers

### Decision
Add automated architecture tests to `ArchitectureLayerTest`:
1. `staticMappersNamingAndLocationTest`: All classes in `com.biopatternsg.infrastructure.adapters.mappers..` must have simple names ending with `Mapper`.
2. `staticMappersShouldBeUtilityClassesTest`: All classes in `com.biopatternsg.infrastructure.adapters.mappers..` must be `final`, have no `public` constructors, have all declared methods `static`, and have no fields that are non-`static` or non-`final`.
3. `staticMappersShouldNotHaveCDIAnnotationsTest`: No class in `com.biopatternsg.infrastructure.adapters.mappers..` may be annotated with `@ApplicationScoped`, `@Singleton`, `@RequestScoped`, `@Dependent`.

### Rationale
- Guarantees zero regression on static mapper rules in future PRs.
- Verifiable in seconds via `mvn test -Dtest=ArchitectureLayerTest`.
