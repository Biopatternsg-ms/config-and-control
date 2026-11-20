# Quickstart & Verification Guide: REST Controllers Purity

**Feature**: `005-rest-controllers-purity`
**Date**: 2026-11-20
**Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md)

---

## 1. Prerequisites & Environment

All compilation and tests must be executed with Java 21 LTS:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
java -version # Must output openjdk version "21.x.x"
```

---

## 2. Automated Architecture Quality Gate

Verify that all ArchUnit rules pass, specifically verifying that zero public controller methods expose domain models in their return types:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw test -Dtest=ArchitectureLayerTest
```

Expected result:
- `layerApplicationTest`: PASS
- `layerApplicationUsesCasesTest`: PASS
- `domainLayerIsolationTest`: PASS
- `applicationLayerIsolationTest`: PASS
- `useCasesNamingConventionTest`: PASS
- `controllersNamingConventionTest`: PASS
- `adaptersNamingConventionTest`: PASS
- `controllerMethodsShouldNotReturnDomainModelsTest`: PASS (8/8 rules passing, 0 violations)

---

## 3. Targeted Unit Test Execution

Run unit tests covering controllers, mappers, and webhook resolution:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw test -Dtest=KeycloakEventMapperTest,UserControllerTest,NetworkControllerTest,PipelineControllerTest
```

Validation assertions:
1. `KeycloakEventMapperTest`:
   - Extracts ID for `VERIFY_EMAIL` event.
   - Extracts ID for `CUSTOM_REQUIRED_ACTION` event with `VERIFY_EMAIL` in details.
   - Returns `Optional.empty()` for unrelated events or null/blank IDs.
2. `UserControllerTest`:
   - `handleKeycloakEvent` delegates extracted target ID to `userManagement.processEmailVerification(id)` and returns HTTP 200.
   - Ignores unhandled events without exceptions and returns HTTP 200.
3. `NetworkControllerTest`:
   - `create` returns HTTP 201 with `NetworkResponse` entity (not `NetworkConfig`).
   - `update` returns `NetworkResponse` directly.
   - `findList(id)` returns `NetworkResponse` directly.
   - `findList(filters)` returns `ReportFormat<NetworkResponse>`.
4. `PipelineControllerTest`:
   - `create` returns HTTP 201 with `PipelineDetailResponse` entity.
   - All `update*` methods return `PipelineDetailResponse` directly with all configuration fields.
   - `findList(id)` returns `PipelineDetailResponse` directly.
   - `findList(filters)` returns `ReportFormat<PipelineResponse>`.
   - `resume` validates missing microService declaratively via bean validation.

---

## 4. Full Regression Verification

Execute the complete test suite to guarantee zero regressions across the service:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw clean test
```

Expected result:
- `BUILD SUCCESS`
- Total tests run: all passing, 0 failures, 0 errors, 0 skipped.
