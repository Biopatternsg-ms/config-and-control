# Caso de Uso: `UpdatePipelineStepUseCase`

- **Clase:** `com.biopatternsg.application.usecase.UpdatePipelineStepUseCase`
- **Puerto de Entrada:** `com.biopatternsg.domain.port.in.UpdatePipelineStep`
- **Servicios de Aplicación Involucrados:**
  - `com.biopatternsg.application.services.PipelineService`
  - `com.biopatternsg.application.services.PipelineStepOrchestrator`
- **Controlador Invocador:** `com.biopatternsg.infrastructure.adapters.in.restcontrollers.PipelineController`

---

## 1. Propósito
Actualiza el estado de ejecución y métricas de un paso particular (`PipelineSteps`) dentro del pipeline (ej. `IN_PROGRESS`, `COMPLETED`, `FAILED`). Si el estado pasa a `COMPLETED`, dispara automáticamente el orquestador (`PipelineStepOrchestrator`) para transicionar o disparar la siguiente fase del pipeline biológico.

---

## 2. Flujo de Ejecución

1. Recibe el `pipelineId` y el objeto `PipelineStatus` (con `step`, `status` y `metrics`).
2. Llama a `PipelineService.updateStep(pipelineId, step, status, metrics)` para registrar el progreso y persistir el nuevo estado en el histórico.
3. Si `status == Status.COMPLETED`, invoca a `PipelineStepOrchestrator.orchestrate(pipelineConfig, step)` para desencadenar el siguiente paso correspondiente en los servicios externos (`inferences`, `pubmed-integration`, etc.).
4. Registra en el log la actualización del paso.

---

## 3. Diagrama de Flujo (Mermaid)

```mermaid
sequenceDiagram
    autonumber
    actor Service as External Worker / Service
    participant Controller as PipelineController
    participant UseCase as UpdatePipelineStepUseCase
    participant PipeService as PipelineService
    participant Orchestrator as PipelineStepOrchestrator

    Service->>Controller: PATCH /api/pipeline/{id}/step (PipelineStatus)
    Controller->>UseCase: execute(pipelineId, pipelineStatus)
    UseCase->>PipeService: updateStep(pipelineId, step, status, metrics)
    PipeService-->>UseCase: updated PipelineConfig
    alt Status es COMPLETED
        UseCase->>Orchestrator: orchestrate(pipelineConfig, step)
        Note over Orchestrator: Dispara llamadas asíncronas / siguientes fases
    end
    UseCase-->>Controller: void
    Controller-->>Service: 200 OK / 204 No Content
```
