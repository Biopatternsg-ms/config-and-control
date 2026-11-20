# Technical Research: Use Case Documentation Synchronization

**Feature**: `007-usecase-docs-synchronization` | **Date**: 2026-11-20

## 1. Automated Governance Test Design

### Decision
Implement `DocumentationGovernanceTest.java` under `src/test/java/archtest/` as a dedicated JUnit 5 test class using ArchUnit's `ClassFileImporter` to scan `com.biopatternsg.application.usecase`.

### Rationale
- **Decoupled from code architecture**: Isolating file inspection from bytecode layer rules in `ArchitectureLayerTest` keeps architecture tests fast and focused while giving documentation governance a clear identity.
- **Dynamic detection**: By using `ClassFileImporter`, any new top-level class ending in `UseCase` is automatically detected without hardcoding class names in tests.
- **Immediate feedback**: Fails during `./mvnw test` if a developer or agent introduces a use case without creating its `.md` file in `.agents/use_cases/` or omitting its link from `.agents/architecture.md`.

### Alternatives Considered
- *Custom Maven plugin or bash verification script*: Rejected because shell scripts are easily skipped in CI or IDE runs, whereas JUnit 5 tests execute in every standard Maven test lifecycle (`mvn test`).
- *Placing in ArchitectureLayerTest*: Considered (Option A in clarify), but user opted for Option B to cleanly separate file-system documentation inspection from ArchUnit bytecode layer rules.

---

## 2. Use Case Delivery Contract Synchronization (`LaunchPipelineUseCase.md`)

### Decision
Update `LaunchPipelineUseCase.md`:
1. Change endpoint URL in Mermaid diagram and text from `POST /api/pipeline/{id}/launch` to `POST /config-and-control/pipelines/launch`.
2. Document request payload: `LaunchPipelineRequest(String pipelineId)`.
3. Document response: HTTP `202 Accepted` with text entity `"Pipeline launched"`.
4. Document outbound repository call: `BiologicalObjectRepository.launch(pipeline)`.

### Rationale
- Matches the exact code in `PipelineController.java:238-259` and `LaunchPipelineUseCase.java:29-56`.
- Ensures accurate reference for API consumers and test fixtures.

---

## 3. Architecture Overview (`.agents/architecture.md`) UTF-8 Encoding Repair

### Decision
Re-encode `.agents/architecture.md` into valid UTF-8:
- Replace broken characters (`PropÃ³sito` → `Propósito`, `GestiÃ³n` → `Gestión`, `orquestaciÃ³n` → `orquestación`, `Redes BiolÃ³gicas` → `Redes Biológicas`, etc.).
- Clean tree directory characters (`├──`, `└──`, `│`).
- Format Section 5 table with uniform backticks and verified relative paths (`[<Name>.md](use_cases/<Name>.md)`).

### Rationale
- Restores professional markdown rendering across all tools (VS Code, GitHub web, terminals).
- Eliminates visual noise and ensures hyperlink integrity.
