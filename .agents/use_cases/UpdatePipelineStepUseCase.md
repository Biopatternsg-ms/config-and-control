# Use Case: `UpdatePipelineStepUseCase`

- **Class:** `com.biopatternsg.application.usecase.UpdatePipelineStepUseCase`
- **Inbound Port:** `com.biopatternsg.domain.port.in.UpdatePipelineStep`
- **Application Services Involved:**
  - `PipelineService`
  - `com.biopatternsg.application.services.PipelineStepOrchestrator`
- **Invoking Controller:** `com.biopatternsg.infrastructure.adapters.in.restcontrollers.PipelineController`

---

## 1. Purpose
Updates the execution status and metrics of a specific step (`PipelineSteps`) within a pipeline (e.g., `IN_PROGRESS`, `COMPLETED`, `FAILED`). When a step transitions to `COMPLETED`, it automatically invokes `PipelineStepOrchestrator` to advance or trigger the next phase of the biological pipeline.

---

## 2. Execution Flow

1. Receives `pipelineId` and the `PipelineStatus` domain object (containing `step`, `status`, and `metrics`).
2. Calls `PipelineService.updateStep(...)` to record the step status and metrics and persist the updated history.
3. If `status == Status.COMPLETED`, invokes `PipelineStepOrchestrator.orchestrate(pipelineConfig, step)` to trigger the next corresponding phase across external microservices (`inferences`, `pubmed-integration`, etc.).
4. Logs the step update completion.

---

## 3. Sequence Diagram (Mermaid)

```mermaid
sequenceDiagram
    autonumber
    actor Service as External Worker / Microservice
    participant Controller as PipelineController
    participant UseCase as UpdatePipelineStepUseCase
    participant PipeService as PipelineService
    participant Orchestrator as PipelineStepOrchestrator

    Service->>Controller: PATCH /config-and-control/pipelines/update-step (PipelineStepRequest)
    Controller->>UseCase: execute(pipelineId, pipelineStatus)
    UseCase->>PipeService: updateStep(pipelineId, pipelineStatus)
    PipeService-->>UseCase: updated PipelineConfig
    alt Status is COMPLETED
        UseCase->>Orchestrator: orchestrate(pipelineConfig, step)
        Note over Orchestrator: Triggers asynchronous calls / downstream phases
    end
    UseCase-->>Controller: void
    Controller-->>Service: 202 Accepted ("updated")
```

---

## 4. Error Cases

- **Non-existent Pipeline:** `PipelineService` throws `UnprocessableEntityException("The pipeline don't exists")` if `pipelineId` does not exist.
