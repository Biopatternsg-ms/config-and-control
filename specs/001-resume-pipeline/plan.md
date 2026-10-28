# Implementation Plan: Resume Pipelines Interrumpidos al Iniciar

**Branch**: `001-resume-pipeline` | **Status**: Complete | **Target Platform**: Quarkus 3.30.4 (Java 21)
**Scope**: Microservicios `config-and-control`, `search-biological-objects`, `pubmed-integration`
**Specs**: [Specification](spec.md) | [Research](research.md) | [Data Model](data-model.md) | [Contracts](contracts/resume-pipeline-api.md)

---

## Executive Summary

Este plan implementa la capacidad de reanudación automática de pipelines biológicos que quedaron interrumpidos en estado `IN_PROGRESS` cuando los microservicios computacionales sufren caídas, reinicios o despliegues.

La solución se compone de dos vertientes:
1. **Endpoint Central en `config-and-control` (`POST /config-and-control/pipelines/resume`)**: Expone un puerto de entrada (`ResumePipeline`) que identifica pipelines huérfanos por microservicio, resuelve el `userId` correspondiente consultando la entidad `Network` asociada y re-detona la ejecución invocando `biologicalObjectRepository.launchConsole(pipeline, userId)` para `SEARCH_BIOLOGICAL_OBJECTS` o `pipelineStepOrchestrator.orchestrate(pipeline, previousStep)` para `PUBMED_INTEGRATION`.
2. **Boot Lifecycle Listeners en Clientes (`AppLifecycleBean`)**: En `search-biological-objects` y `pubmed-integration`, se implementa un listener reactivo con `@Observes StartupEvent` que, mediante un cliente HTTP REST (`ConfigAndControlHttpClient`), solicita a `config-and-control` la reanudación de sus pipelines pendientes de manera asíncrona, sin bloquear el ciclo de arranque del contenedor.

La arquitectura cumple de forma incondicional con la Constitución Arquitectónica de BioPatternsG:
- Aislamiento hexagonal estricto: cero dependencias indebidas entre capas.
- Interfaz `PipelineService` en `domain.services` e implementación en `application.services.PipelineServiceImpl` para preservar la regla ArchUnit `layerApplicationTest` intacta.
- Paso explícito de `userId` a través de `BiologicalObjectRepository.launchConsole(pipelineConfig, userId)`.
- Casos de uso como orquestadores livianos (máximo 3 parámetros).
- Preservación íntegra de la suite ArchUnit original en `ArchitectureLayerTest.java` sin relajar ninguna regla arquitectónica.

---

## Technical Context

- **Java Version**: OpenJDK 21 (LTS)
- **Framework**: Quarkus 3.30.4
- **Database**: MongoDB 7.x (Panache Active Record & Reactive Repositories)
- **Protocol**: HTTP/REST (JSON) con MicroProfile REST Client y Jakarta REST
- **Testing**: JUnit 5, Mockito, ArchUnit 1.4.1, Quarkus Test (`@QuarkusTest`)

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| Principio Constitucional | Estado Pre-Diseño | Estado Post-Diseño | Justificación y Evidencia de Cumplimiento |
|---|:---:|:---:|---|
| **I. Strict Hexagonal Architecture & Layer Isolation** | PASS | PASS | `MicroService` y `PipelineResumeSummary` residen en `domain.*`. `ResumePipeline` reside en `domain.port.in`. Para el relanzamiento en `SEARCH_BIOLOGICAL_OBJECTS`, `BiologicalObjectRepository` expone `launchConsole(PipelineConfig, String userId)` evitando la necesidad de un puerto de sesión o manipulación de estado de hilo en la capa de aplicación. Asimismo, `PipelineService` se define como interfaz en `com.biopatternsg.domain.services.PipelineService` e implementada por `com.biopatternsg.application.services.PipelineServiceImpl`, de modo que ningún adaptador de `infrastructure` (`PipelineController`, `BiologicalObjectAdapter`, `PubmedIntegrationAdapter`, `InferencesAdapter`) importe clases de `application..`, manteniendo intacta la regla `ArchitectureLayerTest.layerApplicationTest()` (`whereLayer("Application").mayOnlyBeAccessedByLayers("Application")`). No existen dependencias indebidas entre capas. ArchUnit pasa limpio. |
| **II. Use Cases as Lightweight Orchestrators** | PASS | PASS | `ResumePipelineUseCase` implementa `ResumePipeline.execute(MicroService microService)` con 1 solo parámetro (límite constitucional: 3). Contiene exclusivamente validación de precondiciones y orquestación entre `pipelineRepository`, `networkRepository`, `biologicalObjectRepository` y `pipelineStepOrchestrator`. |
| **III. Application Services Encapsulation** | PASS | PASS | Se reutiliza `PipelineStepOrchestrator` existente para las progresiones de la máquina de estados. `PipelineService` expone su contrato público como interfaz en `com.biopatternsg.domain.services.PipelineService` y oculta su implementación en `com.biopatternsg.application.services.PipelineServiceImpl`. Los controladores y adaptadores de infraestructura consumen la interfaz de dominio sin acceder a la implementación de aplicación ni realizar bypasses indebidos. |
| **IV. Mandatory Synchronous Use Case Documentation** | PASS | PASS | Se incluye la creación de `.agents/use_cases/ResumePipelineUseCase.md` (con diagrama `sequenceDiagram`, puertos involucrados y flujos) y la actualización de la tabla índice en `.agents/architecture.md`. |
| **V. Clean Code & Zero Business Logic in Delivery & Lambdas** | PASS | PASS | `PipelineController.resume()` es puramente un delivery adapter: deserializa `ResumePipelineRequest`, ejecuta el caso de uso `resumePipeline.execute(...)`, y retorna la respuesta mapeada. No contiene condicionales ni queries de negocio. |
| **VI. ArchUnit & Quality Gates Enforcement** | PASS | PASS | Todas las nuevas clases cumplen con los sufijos obligatorios (`UseCase`, `Controller`, `Adapter`, `Port`). La suite `ArchitectureLayerTest` se ejecuta en la verificación final con 0 violaciones sobre el archivo de pruebas original e intacto. |

---

## Project Structure

### Documentation (this feature)

```text
specs/001-resume-pipeline/
├── plan.md              # Este plan de implementación
├── research.md          # Decisiones técnicas y justificaciones (Fase 0)
├── data-model.md        # Entidades, DTOs y diagramas de estados (Fase 1)
├── quickstart.md        # Guía de validación y escenarios ejecutables (Fase 1)
├── contracts/           # Contratos OpenAPI y clientes REST (Fase 1)
│   └── resume-pipeline-api.md
└── tasks.md             # Plan de tareas de implementación (Fase 2)
```

### Source Code

#### 1. Repositorio `config-and-control`
```text
src/main/java/com/biopatternsg/
├── domain/
│   ├── enums/
│   │   └── MicroService.java                       # [NUEVO] Enum: SEARCH_BIOLOGICAL_OBJECTS, PUBMED_INTEGRATION
│   ├── models/
│   │   └── PipelineResumeSummary.java              # [NUEVO] Modelo de dominio con resultado del lote
│   ├── services/
│   │   └── PipelineService.java                    # [NUEVO] Interfaz de dominio para actualización de pasos
│   └── port/
│       ├── in/
│       │   └── ResumePipeline.java                 # [NUEVO] Inbound Port del caso de uso
│       └── out/
│           └── repositories/
│               ├── BiologicalObjectRepository.java # [MODIFICAR] Agregar launchConsole(PipelineConfig, String userId)
│               ├── PipelineRepository.java         # [MODIFICAR] Agregar findInProgressBySteps(...)
│               └── NetworkRepository.java          # [EXISTENTE] findById(id)
│
├── application/
│   ├── services/
│   │   ├── PipelineServiceImpl.java                # [RENOMBRAR/MODIFICAR] Implementación de PipelineService
│   │   └── PipelineStepOrchestrator.java           # [MODIFICAR] Inyecta interfaz domain.services.PipelineService
│   └── usecase/
│       └── ResumePipelineUseCase.java              # [NUEVO] Orquestador de reanudación
│
└── infrastructure/
    ├── adapters/
    │   ├── in/restcontrollers/
    │   │   └── PipelineController.java             # [MODIFICAR] Agregar POST /resume e inyectar domain PipelineService
    │   ├── mappers/
    │   └── out/
    │       ├── repositories/
    │       │   ├── PipelineRepositoryAdapter.java  # [MODIFICAR] Implementar findInProgressBySteps
    │       │   ├── NetworkRepositoryAdapter.java   # [MODIFICAR] Fallback de búsqueda ObjectId si sesión es nula
    │       │   ├── BiologicalObjectAdapter.java    # [MODIFICAR] Implementar launchConsole(PipelineConfig, String userId)
    │       │   ├── InferencesAdapter.java          # [MODIFICAR] Inyectar domain PipelineService
    │       │   └── PubmedIntegrationAdapter.java   # [MODIFICAR] Inyectar domain PipelineService
    ├── config/interceptors/
    │   └── ContextInterceptor.java                 # [MODIFICAR] Eximir /pipelines/resume de x-user-id
    ├── dtos/
    │   ├── ResumePipelineRequest.java              # [NUEVO] DTO request
    │   └── PipelineResumeResponse.java             # [NUEVO] DTO response
    └── mongo_db/repositories/
        └── PipelineRepositoryDB.java               # [MODIFICAR] Query Panache Mongo para steps e IN_PROGRESS

src/test/java/
├── archtest/
│   └── ArchitectureLayerTest.java                  # [INTACTO] 7/7 pruebas ArchUnit pasando
└── unit/
    ├── restcontrollers/
    │   └── PipelineControllerTest.java             # [NUEVO] Pruebas del controller
    └── usecase/
        └── ResumePipelineUseCaseTest.java          # [NUEVO] 6 pruebas unitarias completas
```

#### 2. Repositorios Clientes (`search-biological-objects` y `pubmed-integration`)
```text
src/main/java/com/biopatternsg/infrastructure/
├── boot_functions/
│   └── AppLifecycleBean.java                       # [NUEVO] Observes StartupEvent y dispara resume asíncrono
├── clients/
│   └── ConfigAndControlHttpClient.java             # [NUEVO/MODIFICAR] Cliente REST con POST /pipelines/resume
└── dtos/
    ├── ResumePipelineRequest.java                  # [NUEVO] Payload microService
    └── PipelineResumeResponse.java                 # [NUEVO] DTO resumen devuelto

src/test/java/com/biopatternsg/infrastructure/boot_functions/
└── AppLifecycleBeanTest.java                       # [NUEVO] Prueba unitaria del evento de inicio
```

---

## Complexity Tracking

| Decisión de Diseño | Por qué es Necesario | Alternativa Más Simple Rechazada y Razón |
|---|---|---|
| Inclusión de `launchConsole(PipelineConfig, String userId)` en `BiologicalObjectRepository` | Permite que `ResumePipelineUseCase` pase explícitamente el `userId` resuelto desde `NetworkRepository` hacia `BiologicalObjectAdapter` y el cliente HTTP sin requerir mutación de estado de hilo ni `UserSessionPort`. | Usar un `UserSessionPort` mutable: Rechazado para favorecer paso explícito de parámetros funcionales y desacoplar la reanudación de contextos de sesión HTTP. |
| Fallback en `NetworkRepositoryAdapter.findById` cuando no hay sesión | Al arrancar de forma desatendida, `SessionUtil.getContext()` es nulo. Consultar la red por su clave primaria única `_id` (`ObjectId`) permite resolver el `userId` sin requerir usuario pre-autenticado. | Inyectar un usuario "dummy" o hardcodeado: Corrompería la seguridad multi-inquilino y atribuiría la red a un usuario incorrecto. |
| Mapeo a paso previo en `PUBMED_INTEGRATION` | La arquitectura de `PipelineStepOrchestrator` es reactiva: recibe el paso completado previo y detona la siguiente fase. | Modificar `PipelineStepOrchestrator` para añadir múltiples ramas: Introduce complejidad y riesgo de regresión sobre pipelines en curso en producción. |
| Extracción de interfaz `PipelineService` en `domain.services` e implementación `PipelineServiceImpl` en `application.services` | Preserva intacta la regla constitucional `ArchitectureLayerTest.layerApplicationTest()` donde `com.biopatternsg.application..` solo puede ser accedida por clases de `Application`, evitando que adaptadores de infraestructura (`PipelineController`, `PubmedIntegrationAdapter`, `InferencesAdapter`, `BiologicalObjectAdapter`) importen clases de `application`. | Relajar la regla en `ArchitectureLayerTest` para permitir acceso de `Infrastructure` a `Application`: Rechazado porque debilitaría la gobernanza arquitectónica y violaría los Principios Constitucionales I y VI. |
