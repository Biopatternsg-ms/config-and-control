# Data Model: Resume Pipeline (Reanudación Automática de Pipelines)

**Feature**: `001-resume-pipeline`  
**Date**: 2026-10-27  
**Scope**: `config-and-control`  
**Status**: Completed  

---

## 1. Domain Entities & Value Objects

### 1.1 `MicroService` (Domain Enum)
*Package*: `com.biopatternsg.domain.enums`  
Define los microservicios del ecosistema BioPatternsG autorizados para invocar la reanudación autónoma de pipelines.

```java
public enum MicroService {
    SEARCH_BIOLOGICAL_OBJECTS,
    PUBMED_INTEGRATION
}
```

- **Validation Rules**:
  - No puede ser nulo en peticiones entrantes.
  - Valores no reconocidos deben disparar un error `400 Bad Request` o `422 Unprocessable Entity` durante la deserialización JSON.

---

### 1.2 `PipelineResumeSummary` (Domain Model)
*Package*: `com.biopatternsg.domain.models`  
Encapsula el resultado consolidado del caso de uso tras procesar el lote de pipelines pendientes de reanudación.

| Field | Type | Description | Constraints |
|---|---|---|---|
| `microService` | `MicroService` | Microservicio que originó la solicitud de reanudación | Not Null |
| `totalFound` | `int` | Total de pipelines candidatos identificados en base de datos | >= 0 |
| `resumedCount` | `int` | Cantidad de pipelines relanzados/orquestados exitosamente | >= 0 |
| `failedCount` | `int` | Cantidad de pipelines que fallaron durante la reanudación | >= 0 |
| `resumedPipelineIds` | `List<String>` | Identificadores de los pipelines procesados exitosamente | Not Null (lista vacía si total = 0) |
| `failedPipelineIds` | `List<String>` | Identificadores de los pipelines que presentaron error | Not Null (lista vacía si fallas = 0) |
| `errors` | `Map<String, String>` | Detalle de causas de error asociadas a cada pipeline ID fallido | Not Null |

- **Validation & Business Invariants**:
  - `totalFound == resumedCount + failedCount`.
  - La falla individual de un pipeline añade su ID a `failedPipelineIds` y su motivo a `errors`, sin abortar el procesamiento de los demás elementos del lote.

---

### 1.3 `BiologicalObjectRepository` (Outbound Port Extendido)
*Package*: `com.biopatternsg.domain.port.out.repositories`  
Puerto de salida extendido para soportar lanzamiento determinista pasando explícitamente el `userId`:

```java
public interface BiologicalObjectRepository {
    String launch(PipelineConfig pipelineConfig);
    String launchConsole(PipelineConfig pipelineConfig, String userId);
}
```

---

### 1.4 `ResumePipeline` (Inbound Port)
*Package*: `com.biopatternsg.domain.port.in`  
Contrato del caso de uso de reanudación en la capa de aplicación.

```java
public interface ResumePipeline {
    PipelineResumeSummary execute(MicroService microService);
}
```

---

## 2. Infrastructure DTOs

### 2.1 `ResumePipelineRequest` (REST DTO)
*Package*: `com.biopatternsg.infrastructure.dtos`  
Payload de entrada para el endpoint `POST /config-and-control/pipelines/resume`.

```java
public record ResumePipelineRequest(
    @NotNull(message = "microService is required")
    MicroService microService
) {}
```

### 2.2 `PipelineResumeResponse` (REST DTO)
*Package*: `com.biopatternsg.infrastructure.dtos`  
Representación serializable de `PipelineResumeSummary` para la respuesta HTTP 200/202.

```java
public record PipelineResumeResponse(
    MicroService microService,
    int totalFound,
    int resumedCount,
    int failedCount,
    List<String> resumedPipelineIds,
    List<String> failedPipelineIds,
    Map<String, String> errors
) {}
```

---

## 3. Relaciones con Entidades Existentes

```mermaid
classDiagram
    direction TB

    class MicroService {
        <<enumeration>>
        SEARCH_BIOLOGICAL_OBJECTS
        PUBMED_INTEGRATION
    }

    class ResumePipelineRequest {
        <<record>>
        +MicroService microService
    }

    class PipelineResumeSummary {
        +MicroService microService
        +int totalFound
        +int resumedCount
        +int failedCount
        +List~String~ resumedPipelineIds
        +List~String~ failedPipelineIds
        +Map~String, String~ errors
    }

    class PipelineConfig {
        +String id
        +String name
        +String networkId
        +PipelineSteps step
        +List~PipelineStatus~ statuses
        +Integer levels
        +Integer maxComplexes
        +List~ExpertObjectConfig~ expertObjects
        +TranscriptionFactorConfig transcriptionFactorConfig
        +int retMax
        +boolean useOnlyPrincipalName
    }

    class PipelineStatus {
        +PipelineSteps step
        +Status status
        +Date createdAt
        +Map~String, String~ metrics
    }

    class PipelineSteps {
        <<enumeration>>
        CONFIG (1)
        LAUNCH (1)
        TRANSCRIPTION_FACTOR (2)
        EXPERT_OBJECTS (3)
        SEARCH_LEVELS (4)
        COMBINATIONS (5)
        SEARCH_PUBMED_IDS (6)
        SEARCH_PUBTATOR (7)
        BUILD_KNOWLEDGE_BASE (8)
        GENERATE_ALIGNED_OBJECTS (9)
    }

    class Status {
        <<enumeration>>
        PENDING
        IN_PROGRESS
        COMPLETED
        FAILED
    }

    class NetworkConfig {
        +String id
        +String userId
        +String name
        +String description
    }

    ResumePipelineRequest --> MicroService
    PipelineResumeSummary --> MicroService
    PipelineConfig "1" *-- "many" PipelineStatus
    PipelineConfig --> PipelineSteps
    PipelineStatus --> PipelineSteps
    PipelineStatus --> Status
    PipelineConfig ..> NetworkConfig : relates via networkId
```

---

## 4. Transiciones de Estado y Ciclo de Reanudación

### 4.1 Reanudación en `SEARCH_BIOLOGICAL_OBJECTS` (Pasos 2 al 4)

Los pipelines en pasos 2, 3 o 4 (`TRANSCRIPTION_FACTOR`, `EXPERT_OBJECTS`, `SEARCH_LEVELS`) con su último estado en `IN_PROGRESS` corresponden a ejecuciones biológicas interrumpidas. Su reanudación consiste en relanzar la etapa biológica completa a través de `biologicalObjectRepository.launch(...)`.

```mermaid
stateDiagram-v2
    [*] --> Candidato: step in [2, 3, 4] AND last(statuses) == IN_PROGRESS
    Candidato --> ResolviendoUsuario: networkRepository.findById(pipeline.networkId)
    ResolviendoUsuario --> ErrorDatos: network == null OR userId vacío
    ResolviendoUsuario --> InvocandoLaunch: userId válido
    InvocandoLaunch --> Relanzado: biologicalObjectRepository.launchConsole(pipeline, userId) exitoso
    InvocandoLaunch --> ErrorEjecucion: excepción en llamada HTTP
    Relanzado --> [*]
    ErrorDatos --> [*]
    ErrorEjecucion --> [*]
```

### 4.2 Reanudación en `PUBMED_INTEGRATION` (Pasos 5 al 9)

Los pipelines en pasos 5 al 9 con último estado `IN_PROGRESS` se reactivan llamando a `pipelineStepOrchestrator.orchestrate(pipeline, previousStep)` enviando el paso inmediatamente anterior, permitiendo que la máquina de estados retome la secuencia sin reiniciar desde cero.

```mermaid
stateDiagram-v2
    [*] --> EvaluandoPaso: step in [5..9] AND last(statuses) == IN_PROGRESS
    
    state EvaluandoPaso {
        COMBINATIONS_5 --> Prev_SEARCH_LEVELS_4: Mapear a paso previo
        SEARCH_PUBMED_IDS_6 --> Prev_COMBINATIONS_5: Mapear a paso previo
        SEARCH_PUBTATOR_7 --> Prev_SEARCH_PUBMED_IDS_6: Mapear a paso previo
        BUILD_KNOWLEDGE_BASE_8 --> Prev_SEARCH_PUBTATOR_7: Mapear a paso previo
        GENERATE_ALIGNED_OBJECTS_9 --> Prev_BUILD_KNOWLEDGE_BASE_8: Mapear a paso previo
    }

    EvaluandoPaso --> Orquestando: Inyectar userId en sesión
    Orquestando --> Reanudado: pipelineStepOrchestrator.orchestrate(pipeline, previousStep)
    Reanudado --> [*]
```
