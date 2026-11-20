# Quickstart: Verifying Use Cases Lightweight Orchestration and Parameter Encapsulation

**Feature**: `004-usecase-orchestration-encapsulation`
**Date**: 2026-11-19
**Spec**: [spec.md](./spec.md)

---

## 1. Prerequisites

- Java 21 OpenJDK (`/usr/lib/jvm/java-21-openjdk-amd64`)
- Maven 3.9+ or `./mvnw`
- Working directory: `/home/yacson/Proyectos/biopatternsg-ms/services/config-and-control`

---

## 2. Verification Scenarios

### Scenario 1: Parameter Limit Verification (Rule 1.3 Static Check)
Verify that zero methods in `application.usecase` and `application.services` accept more than 3 parameters.

```bash
# Grep check ensuring no methods with >= 4 comma-separated parameters in application interfaces/use cases
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test-compile
```

### Scenario 2: Pipeline Execution Reporting Verification (`GetPipelineExecutionUseCase`)
Run unit tests verifying that `GetPipelineExecutionUseCase` verifies pipeline existence and delegates report creation to `PipelineExecutionService`.

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -Dtest=GetPipelineExecutionUseCaseTest,PipelineExecutionServiceTest
```
**Expected Outcome**:
- `ExperimentExecutionResponse` is generated with 6 steps, accurate duration metrics, Lucide icons, and zero formatting logic in `GetPipelineExecutionUseCase`.

### Scenario 3: Pipeline Resumption Flow Verification (`ResumePipelineUseCase`)
Run unit tests verifying resumption of candidate pipelines with no method signatures exceeding 3 parameters.

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -Dtest=ResumePipelineUseCaseTest,PipelineStepOrchestratorTest
```
**Expected Outcome**:
- Pipelines for `SEARCH_BIOLOGICAL_OBJECTS` and `PUBMED_INTEGRATION` are dispatched via `PipelineStepOrchestrator.resumePipeline(...)`.
- Resumption batch accurately tallies successes, failures, and error reasons.

### Scenario 4: Step Update & Configuration Invariants Verification (`UpdatePipelineUseCase`)
Run unit tests verifying that `PipelineConfig` evaluates completion for `CONFIG` and formats metrics for `UPDATE_ALIGNED_OBJECTS`.

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -Dtest=UpdatePipelineUseCaseTest,PipelineConfigTest
```

### Scenario 5: Architecture and Layer Boundary Integrity
Run ArchUnit tests verifying that all layer boundaries and naming conventions remain intact.

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -Dtest=ArchitectureLayerTest
```
**Expected Outcome**:
- All 7 architecture tests pass with 0 violations.

### Scenario 6: Full Regression Suite
Execute the entire test suite to ensure zero regressions across the service.

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test
```
**Expected Outcome**:
- All tests pass (BUILD SUCCESS).
