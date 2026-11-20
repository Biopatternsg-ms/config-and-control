# Quickstart Validation Guide: Application Services Encapsulation

## Overview

This guide details the executable verification scenarios to ensure that application services are strictly encapsulated behind interface contracts and that all layered architecture constraints are met.

---

## Prerequisites

- **Java**: OpenJDK 21 (`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`)
- **Maven**: Version 3.8+ (or `./mvnw`)
- **Branch**: `003-app-services-encapsulation`

---

## Verification Scenarios

### Scenario 1: Clean Compilation & Interface Segregation

Ensure the microservice compiles cleanly with all service interfaces in `services` and implementations in `services/impl`:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw clean compile test-compile
```

**Expected Outcome**: `BUILD SUCCESS` with zero compilation errors.

---

### Scenario 2: Static Verification of Infrastructure Decoupling

Verify that zero classes in `infrastructure` import classes from `application.services`:

```bash
grep -rn "com.biopatternsg.application.services" src/main/java/com/biopatternsg/infrastructure/ || echo "INFRASTRUCTURE_ISOLATION_VERIFIED: Zero application service dependencies found"
```

**Expected Outcome**:
```text
INFRASTRUCTURE_ISOLATION_VERIFIED: Zero application service dependencies found
```

---

### Scenario 3: Verify Encapsulation in `application.services`

Verify that `src/main/java/com/biopatternsg/application/services/` contains only interface files:

```bash
ls src/main/java/com/biopatternsg/application/services/*.java
```

**Expected Outcome**:
```text
PipelineService.java
PipelineStepOrchestrator.java
```

And implementations reside strictly in `impl/`:
```bash
ls src/main/java/com/biopatternsg/application/services/impl/*.java
```

**Expected Outcome**:
```text
PipelineServiceImpl.java
PipelineStepOrchestratorImpl.java
```

---

### Scenario 4: Full Architecture Layer Test Suite

Run the automated ArchUnit test suite to confirm all 7 rules pass without violations:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw test -Dtest=ArchitectureLayerTest
```

**Expected Outcome**:
```text
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

### Scenario 5: Targeted Unit Tests for Orchestrator and Adapters

Verify unit tests for the decoupled adapters and the step orchestrator:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw test -Dtest="InferencesAdapterTest,PipelineStepOrchestratorTest,ResumePipelineUseCaseTest"
```

**Expected Outcome**: All 23 tests pass cleanly.

---

## Artifact References

- Data Model & Architecture Boundaries: [data-model.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/config-and-control/specs/003-app-services-encapsulation/data-model.md)
- PipelineStepOrchestrator Contract: [pipeline-step-orchestrator-contract.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/config-and-control/specs/003-app-services-encapsulation/contracts/pipeline-step-orchestrator-contract.md)
- Outbound Adapter Boundaries: [outbound-adapter-boundaries.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/config-and-control/specs/003-app-services-encapsulation/contracts/outbound-adapter-boundaries.md)
