# Caso de Uso: `GetPipelineExecutionUseCase`

- **Clase:** `com.biopatternsg.application.usecase.GetPipelineExecutionUseCase`
- **Puerto de Entrada:** `com.biopatternsg.domain.port.in.GetPipelineExecution`
- **Puerto de Salida:** `com.biopatternsg.domain.port.out.repositories.PipelineRepository`
- **Servicios de Aplicación Inyectados:** `com.biopatternsg.application.services.PipelineExecutionService`
- **Controlador Invocador:** `com.biopatternsg.infrastructure.adapters.in.restcontrollers.PipelineController`

---

## 1. Propósito
Actúa como un **orquestador liviano** (Principio II de la Constitución) que coordina la consulta del estado de ejecución de un experimento/pipeline. Valida la existencia del pipeline a través del repositorio de persistencia y delega la agregación de métricas, cronología, duraciones y mapeo de iconos/descripciones UI al servicio especializado `PipelineExecutionService`.

---

## 2. Flujo de Ejecución

1. Recupera la entidad `PipelineConfig` mediante `PipelineRepository.findById(pipelineId)`.
2. Si no existe, lanza `UnprocessableEntityException("The pipeline don't exists")`.
3. Delega la construcción del informe métrico y cronológico a `PipelineExecutionService.buildExecutionReport(pipelineConfig)`.
4. Retorna el objeto `ExperimentExecutionResponse` construido por el servicio.

---

## 3. Diagrama de Flujo (Mermaid)

```mermaid
sequenceDiagram
    autonumber
    actor Frontend as Frontend UI / Monitor
    participant Controller as PipelineController
    participant UseCase as GetPipelineExecutionUseCase
    participant Repo as PipelineRepository
    participant Service as PipelineExecutionService

    Frontend->>Controller: GET /api/pipeline/{id}/execution
    Controller->>UseCase: execute(pipelineId)
    UseCase->>Repo: findById(pipelineId)
    alt Pipeline no encontrado
        Repo-->>UseCase: null
        UseCase-->>Controller: throw UnprocessableEntityException
        Controller-->>Frontend: 422 Unprocessable Entity
    else Pipeline encontrado
        Repo-->>UseCase: PipelineConfig (con statuses y configuración)
        UseCase->>Service: buildExecutionReport(pipelineConfig)
        Service->>Service: iterate PipelineSteps.values()
        Service->>Service: calculate step durations & phase metrics
        Service->>Service: resolve Lucide icons & descriptive labels
        Service-->>UseCase: ExperimentExecutionResponse
        UseCase-->>Controller: ExperimentExecutionResponse
        Controller-->>Frontend: 200 OK (Consolidated Execution State)
    end
```

---

## 4. Casos de Error

- **Pipeline Inexistente:** Se lanza `UnprocessableEntityException` con mensaje `"The pipeline don't exists"` si el repositorio retorna `null`.
