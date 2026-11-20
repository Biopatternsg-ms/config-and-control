# Use Case: `RegenerateAlignedObjectsUseCase`

- **Class:** `com.biopatternsg.application.usecase.RegenerateAlignedObjectsUseCase`
- **Inbound Port:** `com.biopatternsg.domain.port.in.RegenerateAlignedObjects`
- **Outbound Ports:**
  - `com.biopatternsg.domain.port.out.repositories.PipelineRepository`
  - `com.biopatternsg.domain.port.out.TriggerPubmedIntegration`
- **Application / Domain Services Involved:**
  - `PipelineService`
- **Invoking Controller:** `com.biopatternsg.infrastructure.adapters.in.restcontrollers.PipelineController`

---

## 1. Purpose
Resets the manual expert object alignment confirmation step (`UPDATE_ALIGNED_OBJECTS`) to `PENDING` and re-triggers aligned object generation in the external `pubmed-integration` microservice.

---

## 2. Execution Flow

1. Receives `pipelineId`.
2. Looks up the pipeline via `PipelineRepository.findById(pipelineId)`. If not found, throws `UnprocessableEntityException("The pipeline don't exists")`.
3. Resets the `PipelineSteps.UPDATE_ALIGNED_OBJECTS` step to `Status.PENDING` via `PipelineService.updateStep(...)`.
4. Triggers aligned objects generation by calling `TriggerPubmedIntegration.executeGenerateAlignedObjects(pipeline)`.

---

## 3. Sequence Diagram (Mermaid)

```mermaid
sequenceDiagram
    autonumber
    actor Client as API Client / Frontend
    participant Controller as PipelineController
    participant UseCase as RegenerateAlignedObjectsUseCase
    participant PipeRepo as PipelineRepository
    participant PipeService as PipelineService
    participant PubmedAdapter as TriggerPubmedIntegration

    Client->>Controller: POST /config-and-control/pipelines/{id}/regenerate-aligned-objects
    Controller->>UseCase: execute(pipelineId)
    UseCase->>PipeRepo: findById(pipelineId)
    alt Pipeline does not exist
        PipeRepo-->>UseCase: null
        UseCase-->>Controller: throw UnprocessableEntityException
        Controller-->>Client: 422 Unprocessable Entity
    end
    PipeRepo-->>UseCase: PipelineConfig
    UseCase->>PipeService: updateStep(id, UPDATE_ALIGNED_OBJECTS, PENDING)
    UseCase->>PubmedAdapter: executeGenerateAlignedObjects(pipeline)
    UseCase-->>Controller: void
    Controller-->>Client: 202 Accepted ("Regeneration triggered")
```

---

## 4. Error Cases

- **Non-existent Pipeline:** Throws `UnprocessableEntityException("The pipeline don't exists")` if `pipelineId` is not found in the repository.
