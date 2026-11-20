# Quickstart & Validation Guide: Clean Code, Lambda Purity, and Static Mappers Standardization

**Feature**: `006-clean-code-lambdas-mappers` | **Date**: 2026-11-20

## 1. Prerequisites
- Linux AMD64 environment
- OpenJDK 21 LTS installed (`/usr/lib/jvm/java-21-openjdk-amd64`)
- Maven wrapper executable (`./mvnw`)

Always configure the Java 21 environment before running any Maven build:
```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=/usr/lib/jvm/java-21-openjdk-amd64/bin:$PATH
```

---

## 2. Validation Scenarios

### Scenario 1: Zero Multi-line Block Lambdas Verification
Ensure that no functional expressions (`.map`, `.filter`, `.ifPresent`, etc.) contain imperative block lambdas (`-> {`):
```bash
# Must return ZERO matches inside src/main/java (excluding language constructs like switch expressions in PipelineStepOrchestratorImpl)
grep -rn -- "-> {" src/main/java | grep -v "case "
```
*Expected Result*: Zero matches.

---

### Scenario 2: Static Mappers Unit Tests
Execute unit tests for all standardized static utility mappers:
```bash
./mvnw test -Dtest=NetworkMapperTest,PipelineMapperTest,UserMapperTest,KeycloakEventMapperTest
```
*Expected Result*: All tests pass with 100% success rate, validating:
- Private constructors throw `UnsupportedOperationException` on reflection.
- Null-safety across all inputs.
- `symbolsToExpertObjects` transformation.
- `toDomainReport` paginated envelope transformations.

---

### Scenario 3: Automated ArchUnit Quality Gates
Run architectural rules in `ArchitectureLayerTest`:
```bash
./mvnw test -Dtest=ArchitectureLayerTest
```
*Expected Result*: All architecture tests pass, including:
- `staticMappersNamingAndLocationTest`
- `staticMappersShouldBeUtilityClassesTest` (classes are final, constructors private, all methods static)
- `staticMappersShouldNotHaveCDIAnnotationsTest`
- Existing layer isolation tests.

---

### Scenario 4: Full Microservice Regression Suite
Execute the entire test suite:
```bash
./mvnw clean test
```
*Expected Result*: 100% tests passing, zero compilation warnings or failures.
