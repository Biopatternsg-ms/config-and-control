# Feature Specification: Resume Pipeline (Reanudación Automática de Pipelines)

**Feature Branch**: `001-resume-pipeline`

**Created**: 2026-10-27

**Status**: Draft

**Input**: User description: "necesito crear una función en config and control llamada resume pipeline, que recibirá como único parámetro el nombre o siglas del microservicio en la variable microService, para esto podemos usar un enum, definiremos un nuevo caso de uso llamada resumen pipeline. Si el servicio que le consulta es search biological objectos buscará todos los pipeline que aún tienen uno de los pasos 2, 3 y 4 en IN_PROGRESS y ejecutar el servicio biologicalObjectHttpClient.launch armando el LaunchPipelineInternalRequest y userId con los datos de cada uno de los pipeline que consigue. Si el servicio que le consulta es pubmed-integration buscará todos los pipeline que aún tienen uno de los pasos 5 al 9 en IN_PROGRESS y llamar a pipelineStepOrchestrator.orchestrate enviando el paso anterior al que se encuentra en progreso. Desde search biological objects y pubmed integration crear una carpeta dentro de infrastructure llamada boot_functions y dentro de una clase con la siguiente lógica AppLifecycleBean onStart(@Observes StartupEvent ev). Adaptando el código a cada repo, la idea es llamar la función resumen-pipelines de config-and-control enviando el nombre del repositorio en la variable microService. Importante delimitar, se trata de crear solo la función y caso de uso en config-and-control, en los otros 2 repositorios la pequeña clase que se ejecutará cuando se inicia el servicio."

## Clarifications

### Session 2026-10-27

- Q: ¿Cómo debe determinarse en base de datos que un pipeline tiene "aún" uno de los pasos objetivo (2 al 4 o 5 al 9) en estado `IN_PROGRESS`? (FR-004) → A: Evaluar únicamente el campo raíz `pipeline.step` (que esté entre los pasos 2–4 o 5–9 según el microservicio) y verificar que su último estado registrado en `statuses` sea `IN_PROGRESS`.
- Q: ¿De dónde debe obtener `ResumePipelineUseCase` el `userId` requerido por `biologicalObjectHttpClient.launch` y `pipelineStepOrchestrator.orchestrate` cuando la llamada proviene del arranque automático (`StartupEvent`) sin sesión de usuario activa? (FR-005) → A: Consultar el `userId` desde la entidad `Network` usando `pipeline.getNetworkId()` (`networkRepository.findById(pipeline.getNetworkId()).getUserId()`) y asignarlo en `SessionUtil` (contexto `x-user-id`) y en la llamada a `biologicalObjectHttpClient.launch`.
- Q: ¿Cuál debe ser la firma HTTP del endpoint en `PipelineController` y los valores exactos del enum `MicroService` que enviarán `search-biological-objects` y `pubmed-integration`? (FR-001) → A: `POST /config-and-control/pipelines/resume` recibiendo un body JSON `{"microService": "..."}` (DTO `ResumePipelineRequest(MicroService microService)`) con los valores de enum `SEARCH_BIOLOGICAL_OBJECTS` y `PUBMED_INTEGRATION`.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Reanudación Automática en Etapa de Objetos Biológicos (Priority: P1)

Al arrancar o reiniciar el microservicio `search-biological-objects` (tras un despliegue, escalado de pods o caída inesperada del nodo de cómputo), todos los pipelines de análisis biológico que hayan quedado interrumpidos en la fase de descubrimiento de objetos biológicos (pasos 2, 3 o 4: `TRANSCRIPTION_FACTOR`, `EXPERT_OBJECTS`, `SEARCH_LEVELS`) con su último estado en `IN_PROGRESS` deben ser identificados y relanzados de forma transparente y consistente, garantizando que el pipeline continúe su ejecución sin pérdida de datos ni bloqueo manual.

**Why this priority**: Es el punto de inicio de la computación pesada de la plataforma. Si un contenedor de objetos biológicos se reinicia durante estos pasos, los pipelines quedan permanentemente estancados, impidiendo que el usuario obtenga resultados y requiriendo intervención manual en la base de datos.

**Independent Test**:
Puede probarse de manera aislada preparando en la base de datos un pipeline cuyo campo raíz `step` sea `TRANSCRIPTION_FACTOR` (o `EXPERT_OBJECTS` / `SEARCH_LEVELS`) y cuyo último estado registrado en `statuses` sea `IN_PROGRESS`, invocando el endpoint `POST /config-and-control/pipelines/resume` con payload JSON `{"microService": "SEARCH_BIOLOGICAL_OBJECTS"}`, y verificando que el caso de uso recupera el pipeline, resuelve el `userId` desde la entidad `Network` asociada vía `networkId`, lo asigna en `SessionUtil` (contexto `x-user-id`), reconstruye el `LaunchPipelineInternalRequest`, e invoca el servicio de lanzamiento de objetos biológicos.

**Acceptance Scenarios**:

1. **Given** existen uno o más pipelines en base de datos cuyo campo raíz `step` se encuentra en {`TRANSCRIPTION_FACTOR`, `EXPERT_OBJECTS`, `SEARCH_LEVELS`} y su último estado en `statuses` es `IN_PROGRESS`,
   **When** se invoca la reanudación de pipelines enviando una petición `POST /config-and-control/pipelines/resume` con cuerpo JSON `{"microService": "SEARCH_BIOLOGICAL_OBJECTS"}`,
   **Then** el sistema localiza todos los pipelines coincidentes, resuelve el `userId` desde la entidad `Network` a través de `pipeline.getNetworkId()`, lo establece en `SessionUtil` (`x-user-id`), obtiene los parámetros biológicos, despacha la solicitud de lanzamiento a `biologicalObjectHttpClient.launch(pipelineRequest, userId)`, y retorna el número de pipelines relanzados satisfactoriamente.
2. **Given** ningún pipeline en la base de datos cumple que su campo `step` esté en los pasos 2, 3 o 4 con último estado `IN_PROGRESS`,
   **When** el microservicio `search-biological-objects` solicita la reanudación de pipelines en su arranque enviando `POST /config-and-control/pipelines/resume` con `{"microService": "SEARCH_BIOLOGICAL_OBJECTS"}`,
   **Then** el sistema completa la consulta sin modificaciones y responde con un resultado exitoso indicando 0 pipelines para reanudar.
3. **Given** un pipeline en paso 3 (`EXPERT_OBJECTS`) con último estado en `statuses` igual a `IN_PROGRESS` asociado a una red (`networkId`),
   **When** se ejecuta la reanudación para `SEARCH_BIOLOGICAL_OBJECTS`,
   **Then** el sistema consulta la entidad `Network` mediante `networkRepository.findById(pipeline.getNetworkId()).getUserId()`, establece dicho `userId` en `SessionUtil` (`x-user-id`) y en la llamada a `biologicalObjectHttpClient.launch`.

---

### User Story 2 - Reanudación Automática en Etapa de Integración PubMed (Priority: P2)

Al iniciar el microservicio `pubmed-integration`, cualquier pipeline que haya quedado estancado en cualquiera de los pasos correspondientes al ciclo de literatura y conocimiento (pasos 5 al 9: `COMBINATIONS`, `SEARCH_PUBMED_IDS`, `SEARCH_PUBTATOR`, `BUILD_KNOWLEDGE_BASE`, `GENERATE_ALIGNED_OBJECTS`) en estado `IN_PROGRESS` debe ser retomado invocando el orquestador de pasos (`PipelineStepOrchestrator`), enviando como paso de referencia el paso inmediatamente anterior al que quedó en progreso.

**Why this priority**: La extracción bibliográfica y construcción de base de conocimiento toma tiempos prolongados y puede verse afectada por reinicios de red o límites de cuota externa. Reanudar desde el paso previo exacto previene la necesidad de reiniciar el pipeline completo desde cero (ahorrando tiempo computacional y llamadas a APIs de PubMed).

**Independent Test**:
Puede probarse de forma independiente registrando pipelines en cada uno de los pasos 5 a 9 en estado `IN_PROGRESS`, disparando la reanudación para `PUBMED_INTEGRATION`, y verificando mediante mocks o espías que `pipelineStepOrchestrator.orchestrate` es llamado con el paso predecesor respectivo (por ejemplo, enviando `SEARCH_LEVELS` si el paso en progreso es `COMBINATIONS`).

**Acceptance Scenarios**:

1. **Given** un pipeline con paso actual `COMBINATIONS` (Paso 5) en `IN_PROGRESS`,
   **When** se invoca la reanudación para `PUBMED_INTEGRATION`,
   **Then** el sistema ejecuta `pipelineStepOrchestrator.orchestrate` enviando dicho pipeline y el paso `SEARCH_LEVELS` (Paso 4).
2. **Given** un pipeline con paso actual `SEARCH_PUBMED_IDS` (Paso 6) en `IN_PROGRESS`,
   **When** se invoca la reanudación para `PUBMED_INTEGRATION`,
   **Then** el sistema ejecuta `pipelineStepOrchestrator.orchestrate` enviando dicho pipeline y el paso `COMBINATIONS` (Paso 5).
3. **Given** un pipeline con paso actual `SEARCH_PUBTATOR` (Paso 7) en `IN_PROGRESS`,
   **When** se invoca la reanudación para `PUBMED_INTEGRATION`,
   **Then** el sistema ejecuta `pipelineStepOrchestrator.orchestrate` enviando dicho pipeline y el paso `SEARCH_PUBMED_IDS` (Paso 6).
4. **Given** un pipeline con paso actual `BUILD_KNOWLEDGE_BASE` (Paso 8) en `IN_PROGRESS`,
   **When** se invoca la reanudación para `PUBMED_INTEGRATION`,
   **Then** el sistema ejecuta `pipelineStepOrchestrator.orchestrate` enviando dicho pipeline y el paso `SEARCH_PUBTATOR` (Paso 7).
5. **Given** un pipeline con paso actual `GENERATE_ALIGNED_OBJECTS` (Paso 9) en `IN_PROGRESS`,
   **When** se invoca la reanudación para `PUBMED_INTEGRATION`,
   **Then** el sistema ejecuta `pipelineStepOrchestrator.orchestrate` enviando dicho pipeline y el paso `BUILD_KNOWLEDGE_BASE` (Paso 8).

---

### User Story 3 - Inicialización Autónoma al Arranque de los Microservicios (Priority: P3)

Los microservicios `search-biological-objects` y `pubmed-integration` deben activar la reanudación de forma completamente desatendida mediante un listener de ciclo de vida de aplicación (`StartupEvent` de Quarkus), notificando a `config-and-control` sobre su disponibilidad e iniciando el procesamiento de pipelines pendientes sin intervención operativa humana.

**Why this priority**: Cierra el ciclo de automatización de auto-recuperación (self-healing) de la arquitectura distribuida durante despliegues continuos o recuperaciones tras fallos de infraestructura.

**Independent Test**:
Puede probarse levantando una instancia de prueba de la aplicación (o invocando el método listener anotado con `@Observes StartupEvent`) y comprobando que se emite la petición HTTP hacia `config-and-control` invocando `POST /config-and-control/pipelines/resume` con el body JSON correspondiente (`{"microService": "SEARCH_BIOLOGICAL_OBJECTS"}` o `{"microService": "PUBMED_INTEGRATION"}`).

**Acceptance Scenarios**:

1. **Given** la aplicación `search-biological-objects` inicia su contexto de aplicación,
   **When** se dispara el evento `StartupEvent`,
   **Then** el componente `AppLifecycleBean` (en `com.biopatternsg.infrastructure.boot_functions`) registra en logs el arranque y despacha la petición `POST /config-and-control/pipelines/resume` con body JSON `{"microService": "SEARCH_BIOLOGICAL_OBJECTS"}`.
2. **Given** la aplicación `pubmed-integration` inicia su contexto de aplicación,
   **When** se dispara el evento `StartupEvent`,
   **Then** el componente `AppLifecycleBean` (en `com.biopatternsg.infrastructure.boot_functions`) registra en logs el arranque y despacha la petición `POST /config-and-control/pipelines/resume` con body JSON `{"microService": "PUBMED_INTEGRATION"}`.
3. **Given** `config-and-control` se encuentra temporalmente no disponible o responde con error durante el arranque de un microservicio dependiente,
   **When** el `AppLifecycleBean` captura la excepción de red,
   **Then** el error es registrado como advertencia (`WARN`/`ERROR`) en el log de inicio y la aplicación cliente continúa su proceso de arranque normal sin abortar el inicio del contenedor.

---

### Edge Cases

- **EC-001 - Microservicio no reconocido o nulo**: Si la petición de reanudación recibe un valor nulo, vacío o que no corresponde a ningún microservicio registrado en el enum (`MicroService`), el sistema debe rechazar la solicitud con una excepción de entidad no procesable o validación de entrada (`400 Bad Request` o `422 Unprocessable Entity`), indicando claramente los valores aceptados.
- **EC-002 - Fallo individual en un pipeline del lote**: Si durante la reanudación de una lista de pipelines uno de ellos falla (por ejemplo, error de comunicación remota oタイムout al invocar `biologicalObjectHttpClient.launch`), el sistema debe registrar el fallo individual de ese pipeline y continuar ejecutando la reanudación de los pipelines restantes del lote, retornando un consolidado con los exitosos y los fallidos.
- **EC-003 - Ausencia de pipelines en progreso**: Si no se encuentran pipelines con los pasos o estados requeridos para el microservicio solicitado, la función debe responder exitosamente con un conteo de 0 pipelines reanudados sin emitir advertencias de error.
- **EC-004 - Inconsistencia de datos de usuario en pipeline**: Al no existir sesión de usuario activa en el contexto de arranque desatendido (`StartupEvent`), `ResumePipelineUseCase` consulta la entidad `Network` vinculada mediante `networkRepository.findById(pipeline.getNetworkId()).getUserId()` para resolver el `userId` y asignarlo en `SessionUtil` (contexto `x-user-id`). Si la red no existe o carece de `userId`, se registra el incidente específico para ese pipeline y se continúa con los siguientes sin interrumpir el lote.
- **EC-005 - Concurrencia de arranque múltiple**: Si dos réplicas o instancias del mismo microservicio inician simultáneamente y ambas solicitan la reanudación, la operación debe ser idempotente y verificar el estado actual del pipeline antes de despachar la orquestación para evitar doble procesamiento.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El microservicio `config-and-control` DEBE exponer un punto de entrada Inbound Port denominado `ResumePipeline` y un endpoint REST `POST /config-and-control/pipelines/resume` en `PipelineController` que reciba en el cuerpo de la petición un payload JSON `{"microService": "..."}` deserializado en el DTO `ResumePipelineRequest(MicroService microService)`.
- **FR-002**: El parámetro `microService` DEBE estar fuertemente tipado mediante el enumerador de dominio `MicroService` ubicado en `com.biopatternsg.domain.enums`, con los valores exactos `SEARCH_BIOLOGICAL_OBJECTS` y `PUBMED_INTEGRATION`.
- **FR-003**: Se DEBE implementar el caso de uso `ResumePipelineUseCase` en `com.biopatternsg.application.usecase`, implementando el puerto de entrada `ResumePipeline` y actuando como orquestador ligero conforme a la constitución de arquitectura hexagonal del proyecto.
- **FR-004**: Cuando el valor de `microService` corresponda a `SEARCH_BIOLOGICAL_OBJECTS`, el caso de uso DEBE consultar en el repositorio de pipelines todos los registros evaluando únicamente el campo raíz `pipeline.step` para que pertenezca a los pasos 2, 3 o 4:
  - Paso 2: `TRANSCRIPTION_FACTOR`
  - Paso 3: `EXPERT_OBJECTS`
  - Paso 4: `SEARCH_LEVELS`  
  y verificando que el último estado registrado en su lista de estados (`statuses`) sea `IN_PROGRESS`.
- **FR-005**: Para cada pipeline obtenido para `SEARCH_BIOLOGICAL_OBJECTS`, `ResumePipelineUseCase` DEBE consultar el `userId` desde la entidad `Network` asociada usando `pipeline.getNetworkId()` (`networkRepository.findById(pipeline.getNetworkId()).getUserId()`), asignarlo en `SessionUtil` (contexto `x-user-id`), construir la instancia de `LaunchPipelineInternalRequest` con los datos del pipeline (`pipelineId`, `levels`, `maxComplexes`, `expertObjects`, `transcriptionFactorConfig`) y despachar la ejecución llamando a `biologicalObjectHttpClient.launch(pipelineRequest, userId)`.
- **FR-006**: Cuando el valor de `microService` corresponda a `PUBMED_INTEGRATION`, el caso de uso DEBE consultar en el repositorio de pipelines todos los registros evaluando únicamente el campo raíz `pipeline.step` para que pertenezca a los pasos 5 al 9:
  - Paso 5: `COMBINATIONS`
  - Paso 6: `SEARCH_PUBMED_IDS`
  - Paso 7: `SEARCH_PUBTATOR`
  - Paso 8: `BUILD_KNOWLEDGE_BASE`
  - Paso 9: `GENERATE_ALIGNED_OBJECTS`  
  y verificando que el último estado registrado en su lista de estados (`statuses`) sea `IN_PROGRESS`.
- **FR-007**: Para cada pipeline obtenido para `PUBMED_INTEGRATION`, `ResumePipelineUseCase` DEBE resolver el `userId` desde la entidad `Network` usando `pipeline.getNetworkId()` (`networkRepository.findById(pipeline.getNetworkId()).getUserId()`), asignarlo en `SessionUtil` (contexto `x-user-id`), e invocar el método `orchestrate(pipelineConfig, previousStep)` de `PipelineStepOrchestrator`, mapeando el paso en progreso (`pipeline.step`) al paso inmediatamente anterior de la siguiente manera:
  - Si el paso en progreso es `COMBINATIONS` (5) -> el paso enviado es `SEARCH_LEVELS` (4).
  - Si el paso en progreso es `SEARCH_PUBMED_IDS` (6) -> el paso enviado es `COMBINATIONS` (5).
  - Si el paso en progreso es `SEARCH_PUBTATOR` (7) -> el paso enviado es `SEARCH_PUBMED_IDS` (6).
  - Si el paso en progreso es `BUILD_KNOWLEDGE_BASE` (8) -> el paso enviado es `SEARCH_PUBTATOR` (7).
  - Si el paso en progreso es `GENERATE_ALIGNED_OBJECTS` (9) -> el paso enviado es `BUILD_KNOWLEDGE_BASE` (8).
- **FR-008**: El caso de uso DEBE retornar un modelo de dominio consolidado (`PipelineResumeSummary` o similar) con la cantidad de pipelines evaluados, reanudados exitosamente, omitidos y fallidos.
- **FR-009**: En el repositorio `search-biological-objects`, se DEBE crear la clase `AppLifecycleBean` dentro del paquete `com.biopatternsg.infrastructure.boot_functions`, la cual observará el evento `io.quarkus.runtime.StartupEvent` e invocará el endpoint `POST /config-and-control/pipelines/resume` de `config-and-control` enviando el body JSON `{"microService": "SEARCH_BIOLOGICAL_OBJECTS"}`.
- **FR-010**: En el repositorio `pubmed-integration`, se DEBE crear la clase `AppLifecycleBean` dentro del paquete `com.biopatternsg.infrastructure.boot_functions`, la cual observará el evento `io.quarkus.runtime.StartupEvent` e invocará el endpoint `POST /config-and-control/pipelines/resume` de `config-and-control` enviando el body JSON `{"microService": "PUBMED_INTEGRATION"}`.
- **FR-011 (Delimitación de Alcance)**: La implementación queda estrictamente acotada a:
  - En `config-and-control`: Creación de la función/endpoint REST, Inbound Port `ResumePipeline`, caso de uso `ResumePipelineUseCase`, enum `MicroService`, DTO `ResumePipelineRequest`, consulta en repositorio de base de datos y orquestación con los clientes/servicios existentes.
  - En `search-biological-objects` y `pubmed-integration`: Exclusivamente la creación de la clase de ciclo de vida (`AppLifecycleBean`) en `infrastructure.boot_functions` y la configuración de su invocación HTTP hacia `config-and-control`.

### Key Entities *(include if feature involves data)*

- **MicroService (Enum)**: Enumeración de dominio en `config-and-control` que tipifica los microservicios del ecosistema que pueden solicitar reanudación (`SEARCH_BIOLOGICAL_OBJECTS`, `PUBMED_INTEGRATION`).
- **ResumePipelineRequest**: DTO de entrada HTTP en `config-and-control` que encapsula el payload JSON recibido en la petición de reanudación con el atributo `microService` de tipo `MicroService`.
- **PipelineResumeSummary**: Estructura de dominio que sintetiza el resultado de la operación de reanudación (`microService`, `totalFound`, `resumedCount`, `failedCount`, lista de identificadores o mensajes de error).
- **PipelineConfig & PipelineSteps**: Entidades del dominio existentes que representan la configuración del pipeline y la máquina de estados de pasos secuenciales.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El 100% de los pipelines con pasos 2 a 4 en estado `IN_PROGRESS` son detectados y relanzados automáticamente tras el arranque de `search-biological-objects`.
- **SC-002**: El 100% de los pipelines con pasos 5 a 9 en estado `IN_PROGRESS` son detectados y re-orquestados al paso previo tras el arranque de `pubmed-integration`.
- **SC-003**: El tiempo total de ejecución de la consulta y despacho de reanudación para lotes típicos (hasta 20 pipelines) es inferior a 3 segundos.
- **SC-004**: Tasa de resiliencia de lote del 100%: el fallo de red o error de datos en un pipeline individual nunca interrumpe o cancela la reanudación del resto de los pipelines encontrados en el lote.
- **SC-005**: Cero interrupciones de arranque en los microservicios clientes: fallos temporales en `config-and-control` no impiden que `search-biological-objects` o `pubmed-integration` culminen su inicio.

## Assumptions

- **A-001**: Los números de pasos corresponden unívocamente a los valores del enum `PipelineSteps`: 2 = `TRANSCRIPTION_FACTOR`, 3 = `EXPERT_OBJECTS`, 4 = `SEARCH_LEVELS`, 5 = `COMBINATIONS`, 6 = `SEARCH_PUBMED_IDS`, 7 = `SEARCH_PUBTATOR`, 8 = `BUILD_KNOWLEDGE_BASE`, 9 = `GENERATE_ALIGNED_OBJECTS`.
- **A-002**: La reanudación para `search-biological-objects` reinicia el flujo biológico completo invocando `biologicalObjectHttpClient.launch` tal como lo realiza `LaunchPipelineUseCase`, lo cual es el comportamiento requerido cuando se interrumpe en cualquiera de los pasos 2, 3 o 4.
- **A-003**: La reanudación para `pubmed-integration` se apoya en el comportamiento reactivo de `PipelineStepOrchestrator.orchestrate`, el cual espera recibir el paso previo concluido para detonar el paso subsecuente.
- **A-004**: En `search-biological-objects` y `pubmed-integration`, la clase `AppLifecycleBean` utiliza MicroProfile REST Client o `HttpClient` estándar configurado con timeout defensivo para contactar con `config-and-control`.
- **A-005**: Al ejecutarse la reanudación desde un arranque desatendido (`StartupEvent`) sin token JWT ni sesión de usuario activa en el contexto HTTP entrante, `ResumePipelineUseCase` obtiene siempre el `userId` consultando la entidad `Network` a través de `pipeline.getNetworkId()` (`networkRepository.findById(pipeline.getNetworkId()).getUserId()`) y lo establece programáticamente en `SessionUtil` (contexto `x-user-id`) para garantizar la propagación requerida por los clientes y orquestadores downstream.
