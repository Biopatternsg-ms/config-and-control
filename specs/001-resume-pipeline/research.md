# Technical Research: Resume Pipeline (Reanudación Automática de Pipelines)

**Feature**: `001-resume-pipeline`  
**Date**: 2026-10-27  
**Scope**: `config-and-control`, `search-biological-objects`, `pubmed-integration`  
**Status**: Completed  

---

## Decision 1: Consulta de pipelines candidatos en MongoDB y verificación de estado en progreso

### Decision
Consultar en MongoDB los pipelines cuyo campo raíz `step` pertenezca al conjunto de pasos asociados al microservicio solicitante:
- Para `SEARCH_BIOLOGICAL_OBJECTS`: `[TRANSCRIPTION_FACTOR, EXPERT_OBJECTS, SEARCH_LEVELS]` (pasos 2, 3, 4).
- Para `PUBMED_INTEGRATION`: `[COMBINATIONS, SEARCH_PUBMED_IDS, SEARCH_PUBTATOR, BUILD_KNOWLEDGE_BASE, GENERATE_ALIGNED_OBJECTS]` (pasos 5, 6, 7, 8, 9).

La consulta en base de datos filtrará `step: { $in: [...] }`. Luego, la verificación de que el pipeline permanece activo en dicho paso se realiza comprobando que el último elemento de la lista histórica `statuses` tenga `status == Status.IN_PROGRESS`. Esta verificación se apoya en un método dedicado en `PipelineRepositoryDB` implementando `$expr: { $eq: [{ $arrayElemAt: ["$statuses.status", -1] }, "IN_PROGRESS"] }`, con una salvaguarda defensiva en Java en el caso de uso (`pipeline.getStatuses() != null && !pipeline.getStatuses().isEmpty() && pipeline.getStatuses().get(pipeline.getStatuses().size() - 1).getStatus() == Status.IN_PROGRESS`).

### Rationale
- **Consistencia con el modelo de datos existente:** En `PipelineCollection`, el campo raíz `step` indica la etapa conceptual en la que se encuentra el pipeline, mientras que la lista `statuses` es un registro cronológico de transiciones (`PipelineStatus(step, status, createdAt, metrics)`).
- **Rendimiento:** Indexar o consultar por `step` reduce drásticamente el conjunto de documentos evaluados en MongoDB. La evaluación del último estado mediante `$arrayElemAt` en Mongo evita transferir pipelines que ya hayan concluido (`COMPLETED`) o fallado (`FAILED`), manteniendo el tiempo de respuesta muy por debajo de los 3 segundos para lotes típicos (criterio SC-003).
- **Independencia de sesión de usuario:** A diferencia de `PipelineRepositoryDB.findByIdAndUser` o `findByUserAndFilters`, la búsqueda de pipelines para reanudación es una operación a nivel de sistema (arranque de infraestructura), por lo que no debe filtrarse por un `networkIdList` dependiente de una sesión HTTP activa.

### Alternatives considered
1. *Filtrar únicamente en memoria Java tras cargar todos los pipelines:* Ineficiente a medida que la base de datos crece con cientos de ejecuciones históricas; descartado por penalización de latencia y uso innecesario de memoria.
2. *Depender exclusivamente del array `statuses` sin filtrar por el campo raíz `step`:* Requiere búsquedas complejas sobre arrays embebidos y no aprovecha la estructura de máquina de estados ya normalizada en `PipelineCollection.step`.
3. *Modificar el esquema de MongoDB agregando un flag booleano `isResumable`:* Innecesario y propenso a desincronizaciones de estado; la información actual en `step` y `statuses` es completa y suficiente.

---

## Decision 2: Resolución del `userId` y paso explícito vía `BiologicalObjectRepository.launchConsole(pipelineConfig, userId)`

### Decision
Para cada pipeline identificado, el caso de uso `ResumePipelineUseCase` resolverá el identificador del usuario propietario (`userId`) consultando la entidad de dominio `Network` mediante `networkRepository.findById(pipeline.getNetworkId()).getUserId()`.

Para invocar la ejecución del microservicio biológico sin recurrir a mutación de estado de hilo ni depender de sesión HTTP:
1. Se extiende el puerto de salida de dominio `BiologicalObjectRepository` agregando la sobrecarga:
   ```java
   String launchConsole(PipelineConfig pipelineConfig, String userId);
   ```
2. En `BiologicalObjectAdapter`, se implementa `launchConsole(pipelineConfig, userId)` inyectando directamente el `userId` en la llamada a `biologicalObjectHttpClient.launch(pipelineBiologicalObject, userId)` con reintentos `@Retry`. El método existente `launch(pipelineConfig)` se preserva delegando a `launchConsole(pipelineConfig, sessionUtil.getUserId())`.
3. `ResumePipelineUseCase` invoca directamente `biologicalObjectRepository.launchConsole(pipeline, userId)` para `SEARCH_BIOLOGICAL_OBJECTS`, eliminando por completo la necesidad de un puerto de sesión (`UserSessionPort`) o adaptador de sesión mutable.
4. Adicionalmente, se actualiza `NetworkRepositoryAdapter.findById(String id)` para que, si el contexto de sesión es nulo (como ocurre en el arranque desatendido del sistema), consulte directamente la colección de MongoDB por `_id` (`ObjectId`) utilizando `networkRepositoryDB.findById(new ObjectId(id))`, garantizando que la red y su correspondiente `userId` sean recuperados sin lanzar `NullPointerException`.

### Rationale
- **Aislamiento de capas (ArchUnit) y Diseño Libre de Efectos Secundarios:** Al pasar `userId` explícitamente en el puerto de salida de dominio `BiologicalObjectRepository.launchConsole`, `ResumePipelineUseCase` opera de forma completamente determinista y funcional, sin requerir estado mutable en el contexto de hilo ni acoplarse con la sesión de infraestructura.
- **Robustez y Desacoplamiento:** Se evita la proliferación de clases accesorias (`UserSessionPort`, `UserSessionAdapter`). Los adaptadores de infraestructura resuelven si obtienen el usuario del contexto de sesión (`launch`) o de forma explícita (`launchConsole`).
- **Robustez en inicialización:** Al consultar `networkRepository.findById(pipeline.getNetworkId())` en modo desatendido, permitir la resolución por `ObjectId` sin exigir una sesión previa evita condiciones de carrera o bloqueos en el arranque.

### Alternatives considered
1. *Usar un puerto de salida `UserSessionPort` e inyectar `userId` en `SessionUtil`:* Inicialmente evaluado, pero descartado porque introduce estado mutable en el hilo y genera acoplamiento innecesario con la infraestructura de sesiones HTTP cuando el caso de uso ya conoce el `userId` del pipeline.
2. *Importar directamente `SessionUtil` en el caso de uso:* Descartado categóricamente porque viola la Constitución (Principio I) y rompe la prueba ArchUnit `applicationLayerIsolationTest`.
3. *Almacenar `userId` de forma redundante en `PipelineCollection`:* Requeriría migración de datos y alteraría el modelo de persistencia consolidado; consultar `Network` es una operación de clave primaria única (`_id`) de costo despreciable (< 1 ms en MongoDB).

---

## Decision 3: Mapeo del paso predecesor (`previousStep`) para la orquestación en `PUBMED_INTEGRATION`

### Decision
Cuando el microservicio solicitante sea `PUBMED_INTEGRATION`, `ResumePipelineUseCase` reactivará el flujo invocando `pipelineStepOrchestrator.orchestrate(pipeline, previousStep)`. El mapeo del paso actual en progreso (`pipeline.getStep()`) hacia su paso predecesor (`previousStep`) se define de forma determinista mediante la siguiente tabla:

| Paso actual en progreso (`pipeline.step`) | Valor numérico | Paso anterior enviado (`previousStep`) | Acción detonada en `PipelineStepOrchestrator` |
|---|:---:|---|---|
| `COMBINATIONS` | 5 | `SEARCH_LEVELS` (4) | `triggerPubmedIntegration.executeBuildsPairs(pipeline)` |
| `SEARCH_PUBMED_IDS` | 6 | `COMBINATIONS` (5) | `triggerPubmedIntegration.executeSearchPubmedIds(pipeline)` |
| `SEARCH_PUBTATOR` | 7 | `SEARCH_PUBMED_IDS` (6) | `triggerPubmedIntegration.executeSearchPubtator(pipeline)` |
| `BUILD_KNOWLEDGE_BASE` | 8 | `SEARCH_PUBTATOR` (7) | `triggerPubmedIntegration.executeBuildKnowledgeBase(pipeline)` |
| `GENERATE_ALIGNED_OBJECTS` | 9 | `BUILD_KNOWLEDGE_BASE` (8) | `triggerPubmedIntegration.executeGenerateAlignedObjects(pipeline)` |

### Rationale
- **Diseño del orquestador existente:** `PipelineStepOrchestrator.orchestrate(PipelineConfig config, PipelineSteps step)` fue diseñado según una semántica reactiva: asume que el paso provisto como argumento `step` acaba de concluir con éxito y detona la transición hacia el paso subsiguiente.
  - Por ejemplo, recibir `SEARCH_LEVELS` hace que el orquestador ejecute `executeBuildsPairs`, el cual lanza el procesamiento de `COMBINATIONS` (paso 5).
  - Recibir `BUILD_KNOWLEDGE_BASE` hace que ejecute `executeGenerateAlignedObjects`, detonando el paso 9.
- **Idempotencia y continuidad sin reinicio total:** Al enviar el paso anterior exacto, el pipeline no necesita retroceder al inicio del flujo biológico ni repetir cálculos costosos de minería o pares de entidades ya almacenados.

### Alternatives considered
1. *Invocar directamente los métodos de `TriggerPubmedIntegration` desde `ResumePipelineUseCase`:* Duplica la lógica de transición de estados y acopla el caso de uso a detalles de bajo nivel que ya están encapsulados en `PipelineStepOrchestrator`.
2. *Añadir un nuevo método `resumeStep` en `PipelineStepOrchestrator`:* Innecesario dado que enviar el paso predecesor encaja perfectamente con el comportamiento de la máquina de estados existente sin alterar código sensible.

---

## Decision 4: Diseño del componente autónomo `AppLifecycleBean` en microservicios clientes

### Decision
En los repositorios `search-biological-objects` y `pubmed-integration`:
1. Se crea la clase `AppLifecycleBean` en el paquete `com.biopatternsg.infrastructure.boot_functions`.
2. La clase se anota con `@ApplicationScoped` y contiene el método observador `void onStart(@Observes StartupEvent ev)`.
3. Inyecta el cliente REST de `config-and-control` (`ConfigAndControlHttpClient`) configurado con `@RestClient`.
4. Al dispararse el evento `StartupEvent`, despacha una petición HTTP `POST /config-and-control/pipelines/resume` con un cuerpo JSON `{"microService": "<NOMBRE_ENUM>"}`:
   - En `search-biological-objects`: `{"microService": "SEARCH_BIOLOGICAL_OBJECTS"}`.
   - En `pubmed-integration`: `{"microService": "PUBMED_INTEGRATION"}`.
5. Toda la invocación está contenida dentro de un bloque `try-catch (Exception e)` que registra con `log.error` o `log.warn` cualquier fallo de red o respuesta de error, sin propagar la excepción hacia el runtime de Quarkus.

### Rationale
- **Auto-recuperación desatendida (Self-Healing):** Al levantarse un nuevo pod o instancia de contenedor tras un reinicio inesperado o despliegue en Kubernetes, el propio servicio notifica proactivamente su disponibilidad a `config-and-control`, reanudando de inmediato cualquier trabajo pendiente sin intervención de operadores humanos.
- **Tolerancia a fallos y no bloqueo de arranque (SC-005):** Si `config-and-control` se encuentra iniciando simultáneamente o experimenta un reinicio breve, la captura defensiva de la excepción garantiza que el contenedor de `search-biological-objects` o `pubmed-integration` culmine su arranque normalmente y esté listo para recibir peticiones regulares.
- **Extensión natural de los clientes existentes:** Ambos microservicios ya poseen interfaces `ConfigAndControlHttpClient` anotadas con `@RegisterRestClient(configKey = "config-and-control-api")`. Agregar el método `resumePipelines` a estas interfaces reutiliza las configuraciones de host, puerto y timeouts ya parametrizadas en `application.properties`.

### Alternatives considered
1. *Programar un Cron/Scheduler periódico (`@Scheduled`):* Generaría sobrecarga innecesaria consultando continuamente la base de datos aun cuando no hay pipelines pendientes o el servicio ha estado operando sin reinicios.
2. *Realizar la reanudación mediante un script externo de CI/CD o Kubernetes Job:* Agrega complejidad operativa y dependencias externas de despliegue, desacoplándose del ciclo de vida natural de los microservicios.

---

## Decision 5: Exención de autorización en `ContextInterceptor` para el endpoint de reanudación

### Decision
Actualizar la regla de filtrado en `com.biopatternsg.infrastructure.config.interceptors.ContextInterceptor` para eximir de la verificación de la cabecera `x-user-id` las peticiones entrantes dirigidas a la ruta `config-and-control/pipelines/resume`:

```java
if (path.contains("config-and-control/users") || path.contains("config-and-control/pipelines/resume")) {
    log.info("userId not needed for path: {}", path);
    return;
}
```

### Rationale
- **Naturaleza de la llamada de arranque:** Las solicitudes emitidas por `AppLifecycleBean` ocurren durante el ciclo de vida de arranque del contenedor (`StartupEvent`), momento en el cual no existe una sesión de usuario humano, token JWT ni cabecera `x-user-id` presente en el hilo de ejecución.
- **Prevención de `UnauthorizedServiceException`:** Actualmente, `ContextInterceptor` rechaza con `401 UnauthorizedServiceException` cualquier petición que carezca de `x-user-id`, salvo aquellas en `config-and-control/users` y `config-and-control/admin`. Sin esta exención, las llamadas legítimas de auto-recuperación de los microservicios fallarían invariablemente.
- **Seguridad perimetral:** Las redes de microservicios de BioPatternsG se comunican en una red privada de Kubernetes/Docker interna. El endpoint solo acepta reanudar pipelines ya existentes y validados previamente en el sistema.

### Alternatives considered
1. *Generar un `x-user-id` sintético o de "sistema" desde los clientes:* Confuso y propenso a corromper la trazabilidad de auditoría, ya que los pipelines pertenecen a usuarios específicos reales.
2. *Desactivar `ContextInterceptor` globalmente:* Inaceptable porque desprotegería todos los endpoints de gestión de usuarios y pipelines frente a accesos sin contexto.

---

## Decision 6: Extracción de la interfaz PipelineService hacia domain.services para preservar intacta ArchitectureLayerTest

### Decision
Extraer el contrato del servicio de actualización de pasos de pipeline como una interfaz pura de dominio en `com.biopatternsg.domain.services.PipelineService` y renombrar su implementación en la capa de aplicación a `com.biopatternsg.application.services.PipelineServiceImpl`:

1. `PipelineService` (interfaz): Reside en `com.biopatternsg.domain.services` y declara los métodos:
   - `PipelineConfig updateStep(String id, PipelineSteps step, Status status);`
   - `PipelineConfig updateStep(String id, PipelineSteps step, Status status, Map<String, String> metrics);`
2. `PipelineServiceImpl` (clase concreta): Reside en `com.biopatternsg.application.services`, se anota con `@ApplicationScoped` y `@RequiredArgsConstructor`, e implementa `PipelineService` inyectando `PipelineRepository`.
3. Todos los consumidores externos e internos (`PipelineController`, `BiologicalObjectAdapter`, `PubmedIntegrationAdapter`, `InferencesAdapter`, `PipelineStepOrchestrator`, `UpdatePipelineStepUseCase`, y los tests unitarios `InferencesAdapterTest` y `PipelineStepOrchestratorTest`) inyectan e importan exclusivamente la interfaz `com.biopatternsg.domain.services.PipelineService`.
4. La regla de ArchUnit `ArchitectureLayerTest.layerApplicationTest()` se mantiene completamente intacta y sin modificaciones:
   ```java
   @DisplayName("The class in the layer applications should only used for others class by the same layer")
   @Test
   void layerApplicationTest() {
       Architectures.LayeredArchitecture architecture = layeredArchitecture()
               .consideringAllDependencies()
               .layer("Application").definedBy(ROOT + ".application..")
               .whereLayer("Application").mayOnlyBeAccessedByLayers("Application");
       architecture.check(this.javaClasses);
   }
   ```

### Rationale
- **Preservación estricta de la regla de ArchUnit:** La regla `layerApplicationTest()` exige que las clases bajo `com.biopatternsg.application..` solo puedan ser accedidas por otras clases dentro de la misma capa `Application`. Cuando `PipelineService` era una clase concreta en `application.services`, cualquier adaptador de infraestructura que la importara provocaba 21 violaciones inmediatas en ArchUnit.
- **Inversión de dependencias (DIP) y pureza de dominio:** En Arquitectura Hexagonal y Clean Architecture, la capa de infraestructura solo debe conocer y acoplarse a contratos declarados en el dominio. Al ubicar la interfaz en `domain.services`, la capa de infraestructura depende legítimamente del dominio, mientras que la implementación concreta reside protegida dentro de `application.services`.
- **Compatibilidad con inyección de dependencias (CDI de Quarkus):** Quarkus resuelve automáticamente la inyección de `PipelineService` mapeándola a su única implementación `@ApplicationScoped` `PipelineServiceImpl` en tiempo de compilación/ejecución, manteniendo transparencia absoluta en los puntos de inyección.

### Alternatives considered
1. *Relajar la regla en ArchitectureLayerTest.java para permitir que Infrastructure acceda a Application:* Rechazado terminantemente porque degrada la gobernanza arquitectónica y abre la puerta a que futuros adaptadores se acoplen directamente a servicios y casos de uso internos.
2. *Mover toda la clase concreta PipelineService al dominio:* Viola el principio de pureza del dominio (Constitución Principio I), ya que PipelineService orquesta repositorios y lógica de aplicación que deben residir en application.
3. *Exponer un Inbound Port UpdatePipelineStep para cada uso en adaptadores:* Innecesario y sobre-ingeniería para los adaptadores de integración que únicamente necesitan registrar la transición de paso de una entidad de pipeline.