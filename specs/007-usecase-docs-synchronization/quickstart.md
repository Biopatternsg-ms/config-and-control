# Quickstart & Validation Guide: Use Case Documentation Synchronization

**Feature**: `007-usecase-docs-synchronization` | **Date**: 2026-11-20

## 1. Prerequisites
- Linux AMD64 environment
- OpenJDK 21 LTS installed (`/usr/lib/jvm/java-21-openjdk-amd64`)
- Maven wrapper executable (`./mvnw`)

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=/usr/lib/jvm/java-21-openjdk-amd64/bin:$PATH
```

---

## 2. Validation Scenarios

### Scenario 1: Documentation Parity Check
Verify that all 13 Use Cases in `src/main/java/com/biopatternsg/application/usecase/` have matching `.md` documentation files:
```bash
# Count Java classes (expected: 13)
ls -1 src/main/java/com/biopatternsg/application/usecase/*UseCase.java | wc -l

# Count Markdown files (expected: 13)
ls -1 .agents/use_cases/*UseCase.md | wc -l
```
*Expected Result*: Both counts equal 13.

---

### Scenario 2: Documentation Governance Automated Test
Execute the automated governance test suite:
```bash
./mvnw test -Dtest=DocumentationGovernanceTest
```
*Expected Result*: All tests pass with 100% success rate:
- `everyUseCaseMustHaveDocumentationFileTest`: PASSED
- `everyUseCaseMustBeIndexedInArchitectureDocTest`: PASSED

---

### Scenario 3: Character Encoding Purity Verification
Verify that `.agents/architecture.md` contains zero mojibake artifacts:
```bash
# Must return zero matches
grep -rnE "(Ã³|Ãº|Ã¡|Ã©|Ã­|Ã±|â”)" .agents/architecture.md || echo "ZERO MOJIBAKE FOUND"
```
*Expected Result*: Zero matches.

---

### Scenario 4: Full Test Suite Regression
Run full test suite:
```bash
./mvnw clean test
```
*Expected Result*: 100% tests pass cleanly.
