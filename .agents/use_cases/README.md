# Catálogo de Casos de Uso (`config-and-control`)

> **Servicio:** `config-and-control`  
> **Paquete Base:** `com.biopatternsg.application.usecase`  
> **Puertos de Entrada:** `com.biopatternsg.domain.port.in`  

Este directorio contiene la documentación técnica y funcional detallada de todos los casos de uso implementados en el microservicio `config-and-control`. Cada documento describe el propósito del caso de uso, sus contratos de entrada y salida, los servicios de aplicación y repositorios involucrados, el flujo paso a paso, escenarios de error y su diagrama de secuencia en formato **Mermaid**.

---

## Índice de Casos de Uso

### 1. Gestión de Redes Biológicas (`Network`)

| Caso de Uso | Puerto de Entrada | Descripción | Documentación |
|---|---|---|---|
| `CreateNetworkUseCase` | `CreateNetwork` | Registra una nueva red biológica validando que no exista duplicidad de nombre por usuario. | [`CreateNetworkUseCase.md`](CreateNetworkUseCase.md) |
| `UpdateNetworkUseCase` | `UpdateNetwork` | Actualiza el nombre y descripción de una red biológica existente. | [`UpdateNetworkUseCase.md`](UpdateNetworkUseCase.md) |
| `FindNetworkUseCase` | `FindNetwork` | Consulta redes biológicas por identificador, nombre exacto o filtros paginados. | [`FindNetworkUseCase.md`](FindNetworkUseCase.md) |

### 2. Configuración, Ejecución y Orquestación de Pipelines (`Pipeline`)

| Caso de Uso | Puerto de Entrada | Descripción | Documentación |
|---|---|---|---|
| `CreatePipelineUseCase` | `CreatePipeline` | Crea una configuración de pipeline asociada a una red e inicializa el estado de todos sus pasos. | [`CreatePipelineUseCase.md`](CreatePipelineUseCase.md) |
| `UpdatePipelineUseCase` | `UpdatePipeline` | Actualiza modularmente secciones del pipeline (descripción, factor de transcripción, objetos expertos, configuración de búsqueda, objetos alineados). | [`UpdatePipelineUseCase.md`](UpdatePipelineUseCase.md) |
| `UpdatePipelineStepUseCase` | `UpdatePipelineStep` | Actualiza el estado y métricas de un paso del pipeline y dispara la orquestación de la siguiente fase cuando se completa. | [`UpdatePipelineStepUseCase.md`](UpdatePipelineStepUseCase.md) |
| `FindPipelineUseCase` | `FindPipeline` | Consulta configuraciones de pipelines por identificador, nombre o filtros paginados. | [`FindPipelineUseCase.md`](FindPipelineUseCase.md) |
| `LaunchPipelineUseCase` | `LaunchPipeline` | Inicia la ejecución de un pipeline configurado disparando el procesamiento hacia el servicio de objetos biológicos. | [`LaunchPipelineUseCase.md`](LaunchPipelineUseCase.md) |
| `GetPipelineExecutionUseCase` | `GetPipelineExecution` | Reconstruye el estado consolidado de ejecución, duraciones por fase, progreso y métricas detalladas del experimento. | [`GetPipelineExecutionUseCase.md`](GetPipelineExecutionUseCase.md) |
| `RegenerateAlignedObjectsUseCase` | `RegenerateAlignedObjects` | Reinicia el paso de alineación manual a `PENDING` y re-dispara la generación de objetos alineados en `pubmed-integration`. | [`RegenerateAlignedObjectsUseCase.md`](RegenerateAlignedObjectsUseCase.md) |
| `ResumePipelineUseCase` | `ResumePipeline` | Reanuda en lote los pipelines que quedaron en estado `IN_PROGRESS` para un microservicio específico (`SEARCH_BIOLOGICAL_OBJECTS` o `PUBMED_INTEGRATION`). | [`ResumePipelineUseCase.md`](ResumePipelineUseCase.md) |

### 3. Autenticación y Administración de Usuarios (`User` / `Keycloak`)

| Caso de Uso | Puerto de Entrada | Descripción | Documentación |
|---|---|---|---|
| `UserAuthenticationUseCase` | `UserAuthentication` | Gestiona el inicio de sesión (`login`) y renovación de tokens (`refreshToken`) contra Keycloak. | [`UserAuthenticationUseCase.md`](UserAuthenticationUseCase.md) |
| `UserManagementUseCase` | `UserManagement` | Orquesta el registro de usuarios, verificación de correo, recuperación de contraseña, activación/desactivación y sincronización entre Keycloak y MongoDB. | [`UserManagementUseCase.md`](UserManagementUseCase.md) |

---

## Regla de Mantenimiento y Sincronización

De acuerdo con [`.agents/architecture-rules.md`](../architecture-rules.md) (Sección 2.3) y la Constitución del proyecto ([`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) — Principio IV):

1. **Creación de un Caso de Uso:** Siempre que se cree un nuevo caso de uso en `com.biopatternsg.application.usecase`, se **DEBE** crear su archivo `<NombreUseCase>.md` en este directorio y **registrar su referencia en las tablas de este `README.md`** (además de `.agents/architecture.md`).
2. **Modificación de un Caso de Uso:** Siempre que cambie el flujo, contratos o reglas de negocio de un caso de uso existente, se **DEBE** actualizar su archivo `<NombreUseCase>.md` y su descripción en este `README.md` en caso de que su responsabilidad haya cambiado.
3. **Verificación Automatizada:** Ejecutar `./mvnw test -Dtest=DocumentationGovernanceTest` para validar que todos los casos de uso cuentan con su archivo `.md` y están referenciados tanto en `.agents/use_cases/README.md` como en `.agents/architecture.md`.
