# Quickstart Validation Guide: Domain Layer Purity and Framework Decoupling

## Overview

This guide details the validation steps and commands to verify that the domain layer is completely decoupled from web frameworks (`jakarta.ws.rs.*`), that `domain.services` has been eradicated, and that automated architectural quality gates pass.

---

## Prerequisites

- **Java**: OpenJDK 21 (ensure `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`)
- **Maven**: Version 3.8+ (or project `./mvnw` wrapper)
- **Git Branch**: `002-domain-layer-purity`

---

## Verification Steps & Runnable Commands

### Scenario 1: Clean Compilation with Java 21

Ensure the refactored domain ports and relocated application services compile cleanly without errors:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw clean compile
```

**Expected Outcome**: `BUILD SUCCESS` with zero compilation errors.

---

### Scenario 2: Verify Elimination of Web Framework Imports in Domain

Verify that zero files in the `domain` module import `jakarta.ws.rs.*`:

```bash
grep -rn "jakarta.ws.rs" src/main/java/com/biopatternsg/domain/ || echo "DOMAIN_PURITY_VERIFIED: Zero framework imports found"
```

**Expected Outcome**:
```text
DOMAIN_PURITY_VERIFIED: Zero framework imports found
```

---

### Scenario 3: Verify Removal of `domain/services` Directory

Verify that the `domain/services` directory no longer exists on disk:

```bash
test ! -d src/main/java/com/biopatternsg/domain/services && echo "DIRECTORY_CHECK_PASSED: domain/services does not exist"
```

**Expected Outcome**:
```text
DIRECTORY_CHECK_PASSED: domain/services does not exist
```

---

### Scenario 4: Automated ArchUnit Architecture Layer Isolation Test

Execute the automated architecture test suite:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw test -Dtest=ArchitectureLayerTest
```

**Expected Outcome**:
- `Tests run: 7, Failures: 0, Errors: 0, Skipped: 0`
- `domainLayerIsolationTest` passes with zero violations.
- `BUILD SUCCESS`.

---

### Scenario 5: Unit Tests for User Registration & Pipeline Service

Execute the targeted unit tests to ensure that `UserManagementUseCase` and `KeycloakAdapter` function correctly with the refactored `String` return type:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw test -Dtest=UserManagementUseCaseTest,KeycloakAdapterTest
```

**Expected Outcome**: All tests pass cleanly.

---

## Artifact References

- Data Model & Architecture Boundaries: [data-model.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/config-and-control/specs/002-domain-layer-purity/data-model.md)
- KeycloakRepository Contract: [keycloak-repository-contract.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/config-and-control/specs/002-domain-layer-purity/contracts/keycloak-repository-contract.md)
- PipelineService Contract: [pipeline-service-contract.md](file:///home/yacson/Proyectos/biopatternsg-ms/services/config-and-control/specs/002-domain-layer-purity/contracts/pipeline-service-contract.md)
