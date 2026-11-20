# Data Model and Schema Contracts: Use Case Documentation Synchronization

**Feature**: `007-usecase-docs-synchronization` | **Date**: 2026-11-20

## 1. Documentation Structure Standard

Every use case documentation file under `.agents/use_cases/<UseCaseName>.md` MUST conform to this 5-part structure:

```markdown
# Caso de Uso: `<UseCaseName>`

- **Clase:** `com.biopatternsg.application.usecase.<UseCaseName>`
- **Puerto de Entrada:** `com.biopatternsg.domain.port.in.<InboundPortName>`
- **Puertos de Salida:**
  - `com.biopatternsg.domain.port.out.repositories.<RepositoryPort>`
  - ...
- **Servicios Involucrados:** (si aplica: Domain/Application Services)
- **Controlador Invocador:** `com.biopatternsg.infrastructure.adapters.in.restcontrollers.<ControllerName>`

---

## 1. Propósito
[Descripción concisa del problema de negocio que resuelve y el valor que aporta]

---

## 2. Flujo de Ejecución Paso a Paso
1. [Paso 1: Validación de pre-condiciones e inputs]
2. [Paso 2: Consulta o verificación de existencia de entidades]
3. [Paso 3: Mutación o delegación a servicios de aplicación / puertos outbound]
4. [Paso 4: Persistencia y retorno]

---

## 3. Diagrama de Flujo (Mermaid)
```mermaid
sequenceDiagram
    autonumber
    actor Cliente as API Client / Frontend
    participant Controller as <ControllerName>
    participant UseCase as <UseCaseName>
    participant ...
    ...
```

---

## 4. Catálogo de Errores y Excepciones de Dominio
| Escenario de Error | Nivel | Manejo y Respuesta |
|---|---|---|
| ... | ... | ... |
```

---

## 2. Complete Inventory of Use Cases (13/13)

| # | Use Case Class | Inbound Port | Outbound Ports & Services | Controller Endpoint | Documentation File |
|---|----------------|--------------|---------------------------|---------------------|--------------------|
| 1 | `CreateNetworkUseCase` | `CreateNetwork` | `NetworkRepository` | `POST /config-and-control/networks` | `CreateNetworkUseCase.md` |
| 2 | `CreatePipelineUseCase` | `CreatePipeline` | `PipelineRepository`, `NetworkRepository` | `POST /config-and-control/pipelines` | `CreatePipelineUseCase.md` |
| 3 | `FindNetworkUseCase` | `FindNetwork` | `NetworkRepository` | `GET /config-and-control/networks` | `FindNetworkUseCase.md` |
| 4 | `FindPipelineUseCase` | `FindPipeline` | `PipelineRepository` | `GET /config-and-control/pipelines` | `FindPipelineUseCase.md` |
| 5 | `GetPipelineExecutionUseCase` | `GetPipelineExecution` | `PipelineRepository` | `GET /config-and-control/pipelines/{id}/execution` | `GetPipelineExecutionUseCase.md` |
| 6 | `LaunchPipelineUseCase` | `LaunchPipeline` | `PipelineRepository`, `BiologicalObjectRepository` | `POST /config-and-control/pipelines/launch` | `LaunchPipelineUseCase.md` |
| 7 | `RegenerateAlignedObjectsUseCase` | `RegenerateAlignedObjects` | `PipelineRepository`, `TriggerPubmedIntegration`, `PipelineService` | `POST /config-and-control/pipelines/{id}/regenerate-aligned-objects` | `RegenerateAlignedObjectsUseCase.md` |
| 8 | `ResumePipelineUseCase` | `ResumePipeline` | `PipelineRepository`, `NetworkRepository`, `PipelineStepOrchestrator` | `POST /config-and-control/pipelines/resume` | `ResumePipelineUseCase.md` |
| 9 | `UpdateNetworkUseCase` | `UpdateNetwork` | `NetworkRepository` | `PUT /config-and-control/networks` | `UpdateNetworkUseCase.md` |
| 10 | `UpdatePipelineStepUseCase` | `UpdatePipelineStep` | `PipelineRepository`, `PipelineStepOrchestrator` | `PUT /config-and-control/pipelines/step` | `UpdatePipelineStepUseCase.md` |
| 11 | `UpdatePipelineUseCase` | `UpdatePipeline` | `PipelineRepository` | `PATCH /config-and-control/pipelines/*` | `UpdatePipelineUseCase.md` |
| 12 | `UserAuthenticationUseCase` | `UserAuthentication` | `KeycloakPort` | `POST /config-and-control/users/auth/*` | `UserAuthenticationUseCase.md` |
| 13 | `UserManagementUseCase` | `UserManagement` | `UserRepository`, `KeycloakPort` | `POST/GET /config-and-control/users/*` | `UserManagementUseCase.md` |
