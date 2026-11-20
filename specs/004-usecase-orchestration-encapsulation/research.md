# Research & Technical Decisions: Use Cases Lightweight Orchestration and Parameter Encapsulation

**Feature**: `004-usecase-orchestration-encapsulation`
**Date**: 2026-11-19
**Spec**: [spec.md](./spec.md)

---

## 1. Parameter Encapsulation in `PipelineService.updateStep` (Rule 1.3 Compliance)

### Problem
`PipelineService.updateStep(String id, PipelineSteps step, Status status, Map<String, String> metrics)` in `com.biopatternsg.application.services` accepts 4 parameters, violating Rule 1.3 (*Parameter Encapsulation — Maximum 3 Parameters Rule*).

### Decision
Deprecate/remove the 4-parameter method and provide:
1. `PipelineConfig updateStep(String pipelineId, PipelineStatus pipelineStatus)` (2 parameters).
2. `PipelineConfig updateStep(String pipelineId, PipelineSteps step, Status status)` (3 parameters).

### Rationale
- `com.biopatternsg.domain.models.PipelineStatus` is already an existing domain entity containing `step`, `status`, and `metrics` (along with timestamps).
- Encapsulating status transitions in `PipelineStatus` eliminates argument sprawl, increases type safety, and ensures that method signatures in `PipelineService` do not exceed 3 parameters.

### Alternatives Considered
- *Creating a new Parameter DTO (e.g. `UpdateStepCommand`)*: Rejected because `PipelineStatus` already exists in the domain layer and represents the exact state tuple required.

---

## 2. Extraction of `PipelineExecutionService` from `GetPipelineExecutionUseCase`

### Problem
`GetPipelineExecutionUseCase` (332 lines) combines:
1. Pipeline existence verification.
2. Step metrics derivation and aggregation across 6 distinct pipeline steps (`CONFIG`, `SEARCH_BIOLOGICAL_OBJECTS`, `PUBMED_INTEGRATION`, `UPDATE_ALIGNED_OBJECTS`, `FIND_ROLES`, `INFER_PATHWAYS`).
3. Timestamp math and duration calculation (`formatDuration`, `calculateDuration`).
4. UI presentation formatting (Lucide icon names `"Sliders"`, `"GitBranch"`, `"Cpu"`, `"BookOpen"`, human-readable descriptions).

This severely violates Constitution Principle II (*Use Cases as Lightweight Orchestrators*).

### Decision
Extract all metric aggregation, duration calculations, and timeline step construction into a dedicated Application Service:
- Interface: `com.biopatternsg.application.services.PipelineExecutionService`
- Implementation: `com.biopatternsg.application.services.impl.PipelineExecutionServiceImpl`
`GetPipelineExecutionUseCase` will only:
1. Inbound port execution: `execute(String id)`.
2. Query `PipelineRepository.findById(id)`.
3. Throw `UnprocessableEntityException` if missing.
4. Delegate to `pipelineExecutionService.buildExecutionReport(pipelineConfig)`.
5. Return `ExperimentExecutionResponse`.
Total use case length: ~45–55 lines.

### Rationale
- Adheres to Constitution Principle II (Lightweight Orchestrators) and Principle III (Mandatory Interface Separation).
- Allows independent unit testing of timeline construction and duration formatting without mocking repository calls.

### Alternatives Considered
- *Moving logic to REST Controller*: Forbidden by Constitution Principle V (Zero Business Logic in Delivery Layer).
- *Putting logic in Domain Aggregate `PipelineConfig`*: Rejected because formatting human-readable strings, durations, and UI icons is an application reporting concern, not a pure domain invariant.

---

## 3. Resumption Flow Delegation to `PipelineStepOrchestrator`

### Problem
`ResumePipelineUseCase.processPipeline` accepted 5 parameters:
`processPipeline(PipelineConfig pipeline, MicroService microService, List<String> resumedPipelineIds, List<String> failedPipelineIds, Map<String, String> errors)`.
Additionally, `ResumePipelineUseCase` contained internal switch tables and predecessor step resolution rules for dispatching to `BiologicalObjectAdapter` and `PubmedIntegrationAdapter`.

### Decision
1. Extend `PipelineStepOrchestrator` interface and its implementation `PipelineStepOrchestratorImpl`:
   ```java
   void resumePipeline(PipelineConfig pipeline, String userId, MicroService microService);
   ```
   (3 parameters max).
2. Inside `ResumePipelineUseCase`, introduce a domain batch accumulator or result value object (`PipelineResumptionBatch`) so that all helper methods accept at most 2–3 parameters.
3. The use case orchestrates:
   - Identify candidate pipelines via `pipelineRepository.findInProgressByMicroService(microService)`.
   - Iterate candidates, resolve user id via `userResolver`.
   - Dispatch single pipeline resumption via `pipelineStepOrchestrator.resumePipeline(pipeline, userId, microService)`.
   - Record success or exception in the batch accumulator.
   - Return `PipelineResumeResponse` (built from the accumulator).

### Rationale
- Centralizes all microservice step triggering in `PipelineStepOrchestrator`, preventing duplicate invocation logic across use cases and orchestrators.
- Eliminates the 5-parameter method, fulfilling Rule 1.3 and FR-005.

### Alternatives Considered
- *Creating a separate `PipelineResumptionService`*: Evaluated in Clarification Q1 (Option B) and rejected in favor of Option A to consolidate biological step orchestration in `PipelineStepOrchestrator`.

---

## 4. Rich Domain Invariants in `PipelineConfig` for Step Completion and Metrics

### Problem
`UpdatePipelineUseCase` (lines 83–113) directly evaluated multi-field completion rules:
- Step 1 (`name`, `description`) + Step 4 (`levels`, `retMax`, `maxComplexes`) → transition `CONFIG` status to `COMPLETED`.
- Step 3 (`alignedExpertObjects`) → manual metrics dictionary building and step status transition.

### Decision
Encapsulate these invariants directly on the `PipelineConfig` domain aggregate:
1. `void updateAlignedExpertObjects(List<String> alignedExpertObjects)`:
   - Sets aligned expert objects.
   - Sets current step to `UPDATE_ALIGNED_OBJECTS`.
   - Builds metric map (`totalAlignedObjects`, `alignedSymbols`, `statusMessage`).
   - Appends status for `UPDATE_ALIGNED_OBJECTS` as `COMPLETED`.
2. `void updateSearchConfig(PipelineConfig request)` and `boolean isConfigComplete()`:
   - Applies search config parameters.
   - Evaluates if name, description, levels, retMax, and maxComplexes are complete, transitioning `Status.COMPLETED` if satisfied.

### Rationale
- Encourages a Rich Domain Model (Principle I), removing anemic domain models and procedural logic from use cases.
- `UpdatePipelineUseCase` delegates directly to domain methods and then invokes `pipelineRepository.save(pipelineCurrent)`.

---

## 5. Documentation Synchronization (Principle IV)

### Requirement
Every modified use case must have its markdown documentation synchronized in `.agents/use_cases/<UseCaseName>.md` with:
1. Business intent.
2. Inbound and Outbound ports involved.
3. Step-by-step execution flow.
4. Mermaid sequence diagram.
5. Error cases.

The three impacted use cases:
- `.agents/use_cases/GetPipelineExecutionUseCase.md`
- `.agents/use_cases/ResumePipelineUseCase.md`
- `.agents/use_cases/UpdatePipelineUseCase.md`
will be synchronized during implementation.
