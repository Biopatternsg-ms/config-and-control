# Feature Specification: Automated Pipeline Resumption on Deployment and Fail-Fast Error Truncation

**Feature Branch**: `002-resume-failed-pipelines-on-deploy`

**Created**: 2026-10-20

**Status**: Draft

**Input**: User description: "Necesito realizar 3 tareas independientes que se relacionan en el escenario que estamos considerando cubrir en el desarrollo. - Crear una función nueva llamada resumeFailedPipelines en PipelineController que consultará todos los pipelines que tengan status FAILED y a cada uno de ellos llamará al orquestador (PipelineStepOrchestrator) enviando el step anterior. - En el archivo JenkinsFile de config-and-control dentro de docker run -d, pedir que ejecute la función resumeFailedPipelines y en los JenkinsFile de ontologies y pubmed-integration antes de ejecutar docker run -d eliminar o limpiar las colas que cada uno de ellos usan. - En el código de pipelines/launch y dentro de las funciones que llama el orquestador (también considerando el código de los repositorios que llama), validar cuando realizamos una petición a otro repositorio y este devuelve error, actualizar el status del pipeline a failed y truncar el proceso. Desarrollar un set de pruebas para simular un despliegue en cada unos de los repositorios y verificar que se retoman correctamente los pipelines que fallaron en algunos de sus pasos y retomarlo a partir de este."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Automatic Pipeline Resumption on Deployment (Priority: P1)

As a biological researcher or system operator, when a new version of the central orchestration service (`config-and-control`) is deployed, I need all pipelines that were interrupted or previously marked as `FAILED` to be automatically detected and resumed from their exact step of failure, so that extensive computational workflows complete without requiring manual administrative intervention or data reprocessing from scratch.

**Why this priority**: High business impact. Prevents loss of progress in lengthy biological analysis workflows (which may take hours or days) when services restart, containers update, or temporary faults occur.

**Independent Test**: Can be tested by marking one or more pipelines in the database with status `FAILED` at an automated workflow step (e.g. `COMBINATIONS`, `SEARCH_PUBMED_IDS`, or `FIND_ROLES`), invoking the resumption operation, and observing that each pipeline transitions back to `IN_PROGRESS` at the target step and dispatches to the corresponding worker service without re-running earlier completed steps.

**Acceptance Scenarios**:

1. **Given** an existing pipeline has status `FAILED` at step `SEARCH_PUBMED_IDS` while predecessor steps (`LAUNCH`, `SEARCH_LEVELS`, `COMBINATIONS`) are `COMPLETED`, **When** the resumption operation is triggered following a service deployment (after `/q/health/ready` confirms container readiness), **Then** the system identifies `SEARCH_PUBMED_IDS` as the failed step, determines `COMBINATIONS` as the predecessor step, resolves the pipeline's owner `userId` from its associated network, invokes the step orchestrator with `COMBINATIONS`, updates the pipeline status to `IN_PROGRESS` at `SEARCH_PUBMED_IDS`, and re-dispatches the search task to the worker service.
2. **Given** an existing pipeline has status `FAILED` at the initial step (`LAUNCH` / `SEARCH_LEVELS`), **When** the resumption operation is triggered, **Then** the system identifies the failure at the initial step and invokes the biological object discovery launch process to restart the initial pipeline execution.
3. **Given** an existing pipeline has status `FAILED` at step `FIND_ROLES`, **When** the resumption operation is triggered, **Then** the system identifies `CONFIGURE_INFERENCES` as the predecessor step and invokes `triggerInferences.executeFindRoles` with the resolved owner `userId`.
4. **Given** multiple pipelines exist in the database with status `FAILED` across different user accounts, **When** the batch resumption operation runs, **Then** the system processes them sequentially, isolating errors per pipeline, dynamically binding each user's context, and returning a summary detailing counts of resumed, skipped, and error pipelines.
5. **Given** a pipeline is already in status `COMPLETED` or actively `IN_PROGRESS`, **When** the resumption operation is triggered, **Then** that pipeline is not modified or re-dispatched.
6. **Given** a downstream worker service is unavailable when resumption runs, **When** the orchestrator calls the worker and encounters persistent communication failure after 2 retries, **Then** the pipeline remains in `FAILED` status, is counted as an error in the resumption summary, and can be resumed cleanly on a subsequent deployment.

---

### User Story 2 - Fail-Fast Error Truncation Across Inter-Service Calls (Priority: P1)

As a workflow orchestrator, when an inter-service communication request between microservices (e.g., `config-and-control` communicating with `search-biological-objects`, `pubmed-integration`, or `inferences`; or worker services querying `ontologies`, `build-knowledge-base`, or updating `config-and-control`) encounters a communication failure (HTTP 4xx/5xx status code, connection refusal, or request timeout), I need the system to immediately mark the affected pipeline step and overall pipeline as `FAILED` and truncate subsequent execution so that no further downstream processing is triggered.

**Why this priority**: Critical data integrity and fault isolation. Prevents silent errors, corrupt partial datasets, and runaway asynchronous execution chains when downstream dependencies are unavailable or failing.

**Independent Test**: Can be tested by simulating an HTTP 500 error or connection failure on a downstream endpoint during step execution, and verifying that after 2 retries (1s delay) the current step transitions to `FAILED`, the overall pipeline status is updated to `FAILED`, and subsequent steps in the pipeline lifecycle are never invoked.

**Acceptance Scenarios**:

1. **Given** a pipeline is executing step `SEARCH_PUBTATOR`, **When** the orchestrator calls the external worker service and receives an HTTP 500 error or network timeout that persists after 2 retries, **Then** the system records status `FAILED` for step `SEARCH_PUBTATOR`, logs the error context, and halts the workflow without triggering `BUILD_KNOWLEDGE_BASE`.
2. **Given** an asynchronous worker task in `pubmed-integration` (`build-pairs`, `search-pubmed-ids-by-pairs`, `search-pubtator-by-pmids`, `generate-kb`, or `generate-aligned-objects`) encounters a remote HTTP error or unhandled exception, **When** the failure occurs, **Then** the worker catches the error, sends a status update request to `config-and-control` marking the active step as `FAILED`, and terminates its processing loop immediately.
3. **Given** an inter-service request in `pipelines/launch` fails during initial biological object discovery, **When** the exception occurs, **Then** the pipeline status is updated to `FAILED`, the error is recorded, and no subsequent discovery or combination steps are initiated.

---

### User Story 3 - Clean Message Queues Prior to Worker Deployment (Priority: P2)

As a DevOps engineer or system administrator, when deploying worker microservices that consume asynchronous queues (`ontologies` consuming `mesh-queue`, and `pubmed-integration` consuming `ncbi-pubmed-queue` and `pubtator-queue`), I need the continuous delivery pipeline to purge all pending or orphaned messages from these queues immediately before launching the new service container, so that workers do not process stale, uncoordinated, or obsolete messages from previous terminated sessions.

**Why this priority**: High reliability. Eliminates race conditions, duplicate processing of cancelled or crashed requests, and deadlocks caused by stale backlog messages upon worker container startup.

**Independent Test**: Can be tested by inserting dummy messages into the target queues while service containers are stopped, executing the deployment preparation step, and confirming that the queue message count is 0 before the new container starts.

**Acceptance Scenarios**:

1. **Given** the deployment pipeline for `ontologies` reaches the deployment stage, **When** the old container is stopped and before the new container is launched, **Then** the deployment script purges `mesh-queue` in the message broker via the RabbitMQ Management HTTP API, verifying 0 remaining messages before starting the new container.
2. **Given** the deployment pipeline for `pubmed-integration` reaches the deployment stage, **When** the old container is stopped and before the new container is launched, **Then** the deployment script purges both `ncbi-pubmed-queue` and `pubtator-queue` in the message broker via the RabbitMQ Management HTTP API, verifying 0 remaining messages before starting the new container.
3. **Given** a target queue is already empty or has not yet been declared in the broker (HTTP 404), **When** the purge operation executes, **Then** the operation completes successfully without throwing deployment-blocking errors.

---

### User Story 4 - Automated Deployment and Recovery Simulation Suite (Priority: P3)

As a quality assurance engineer, I need an automated test suite that simulates unexpected service restarts and container redeployments across all relevant repositories while pipelines are in various execution states, verifying that pipelines that failed at any automated stage are accurately resumed from that specific stage upon deployment.

**Why this priority**: Essential for regression testing and continuous verification of system resilience across the multi-repository ecosystem.

**Independent Test**: Can be tested by running the automated test suite against simulated mock services or integration containers, stopping services mid-execution, asserting failure transition, triggering deployment recovery, and verifying end-to-end completion of the pipeline from the exact point of interruption.

**Acceptance Scenarios**:

1. **Given** an automated test suite running against a test environment, **When** a service container is abruptly terminated during step `COMBINATIONS` and restarted via simulated deployment, **Then** the test suite verifies that the pipeline status is reconciled to `FAILED`, the post-deployment resumption triggers, and execution resumes from step `COMBINATIONS` to completion.
2. **Given** a pipeline stopped at a manual user interaction step (`UPDATE_ALIGNED_OBJECTS` or `CONFIGURE_INFERENCES`), **When** deployment resumption executes, **Then** the test suite verifies that manual steps remain in their expected pending state and are not inappropriately re-dispatched to automated workers.

---

### Edge Cases

- **Failure at Manual Review Steps**: If a pipeline was paused or marked failed at a manual review step (`UPDATE_ALIGNED_OBJECTS` where the user must review aligned biological objects, or `CONFIGURE_INFERENCES` where the user selects inference models), automated deployment resumption MUST NOT trigger automated downstream processing; it must preserve the pipeline in its expected user-action-required state.
- **Service Readiness at Deployment Resumption**: To avoid calling the application before it is fully initialized, the deployment script MUST poll `http://localhost:${CONFIG_AND_CONTROL_PORT}/q/health/ready` (provided via `quarkus-smallrye-health`) with a retry loop (max 30 seconds) before invoking `POST /config-and-control/pipelines/resume-failed`.
- **Concurrent Active Pipelines**: If a pipeline is already in status `IN_PROGRESS` when `resumeFailedPipelines` is executed (e.g., from an independent user launch), the system MUST ignore it to avoid duplicate concurrent worker executions.
- **Downstream Worker Not Ready at Resumption**: When `resumeFailedPipelines` re-dispatches a step to a downstream worker service that is still restarting or unhealthy, the fail-fast mechanism (User Story 2) MUST catch the communication error after 2 retries, keep the pipeline in `FAILED` status, record it in the error count of the summary response, and prevent inconsistent pipeline state.
- **Multi-Tenant User Context Resolution**: Because `resumeFailedPipelines` is triggered as an administrative CI/CD task without an active user session, the system MUST resolve the owner `userId` for each pipeline from its parent `NetworkCollection` and establish this context during dispatch so that downstream worker services receive the correct tenant identity.
- **Empty or Undeclared Queues During Deployment Purge**: The queue purge script MUST handle empty queues (0 messages) and non-existent queues (HTTP 404 from RabbitMQ Management API) gracefully, allowing the deployment to continue without failure.
- **Broker Unreachable During Deployment**: If the message broker is unreachable during the queue purge stage, the deployment script should report a clear error rather than hanging indefinitely.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST provide an endpoint (`POST /config-and-control/pipelines/resume-failed`) and underlying use case (`resumeFailedPipelines`) accessible in `PipelineController` that queries all pipeline records currently in `FAILED` status across all networks. This endpoint MUST be exempted from strict `x-user-id` enforcement in `ContextInterceptor` (or accept an internal system indicator such as `x-system-call: true`), and the use case MUST resolve the owner `userId` for each pipeline from its associated `NetworkCollection` before dispatching.
- **FR-002**: For each identified `FAILED` pipeline, the system MUST inspect the pipeline's execution history to determine the specific automated step where the failure occurred.
- **FR-003**: The system MUST determine the predecessor step immediately prior to the failed step in the pipeline lifecycle sequence (`LAUNCH` → `SEARCH_LEVELS` → `COMBINATIONS` → `SEARCH_PUBMED_IDS` → `SEARCH_PUBTATOR` → `BUILD_KNOWLEDGE_BASE` → `GENERATE_ALIGNED_OBJECTS` → `UPDATE_ALIGNED_OBJECTS` → `CONFIGURE_INFERENCES` → `FIND_ROLES` → `UPDATE_BIOLOGICAL_OBJECTS`). If the failed step is `FIND_ROLES`, the system MUST identify `CONFIGURE_INFERENCES` as the predecessor step.
- **FR-004**: If the failed step is the initial step (`LAUNCH` or `SEARCH_LEVELS`), the system MUST re-dispatch the initial biological object discovery process (`biologicalObjectRepository.launch`).
- **FR-005**: For any subsequent failed automated step, the system MUST invoke `PipelineStepOrchestrator.orchestrate` passing the pipeline configuration and the predecessor step, thereby instructing the orchestrator to advance to and re-execute the failed step.
- **FR-006**: The system MUST isolate errors during batch resumption such that a failure or exception encountered while resuming an individual pipeline (including if a destination worker is unavailable) does not prevent the evaluation and resumption of other failed pipelines. In such cases, the affected pipeline MUST remain in `FAILED` status and be reported in the error count of the summary response.
- **FR-007**: The system MUST NOT automatically re-dispatch pipelines whose failure or pending status corresponds to manual user action steps (`UPDATE_ALIGNED_OBJECTS` and `CONFIGURE_INFERENCES`), leaving them in their pending state for user action in the web UI.
- **FR-008**: The deployment configuration for `config-and-control` MUST include `quarkus-smallrye-health` in `pom.xml`, and the deployment script (`Jenkinsfile`) MUST verify container readiness by polling `http://localhost:${CONFIG_AND_CONTROL_PORT}/q/health/ready` with `curl` in a retry loop (maximum 30 seconds) after `docker run -d` before invoking `POST /config-and-control/pipelines/resume-failed`.
- **FR-009**: The deployment script (`Jenkinsfile`) of `ontologies` MUST purge all messages from `mesh-queue` in the message broker prior to executing `docker run -d`.
- **FR-010**: The deployment script (`Jenkinsfile`) of `pubmed-integration` MUST purge all messages from `ncbi-pubmed-queue` and `pubtator-queue` in the message broker prior to executing `docker run -d`.
- **FR-011**: All outbound REST clients and adapters across `config-and-control`, `search-biological-objects`, and `pubmed-integration` that dispatch pipeline execution steps MUST validate HTTP response status codes.
- **FR-012**: When an outbound REST client receives an HTTP error status (4xx or 5xx), a connection failure, or a request timeout, the system MUST catch the error, update the pipeline step status and overall pipeline status to `FAILED`, and truncate the execution flow by halting any further step transitions or asynchronous dispatching.
- **FR-013**: In asynchronous worker tasks and controller catch blocks within `pubmed-integration` (including `buildTreeMesh`, `searchPairs`, `searchPubtatorByPmids`, and preserving existing failure notifications in `GenerateAlignedObjectsUseCase` and `GenerateKbForPipelineUseCase`) and `search-biological-objects`, any unhandled exception or failed HTTP request MUST trigger an explicit step status update call to `config-and-control` setting the step status to `FAILED`, rather than silently logging the error and abandoning the pipeline.
- **FR-014**: The repository MUST include automated test cases simulating deployment restarts across services, verifying that pipelines interrupted at distinct steps (`SEARCH_LEVELS`, `COMBINATIONS`, `SEARCH_PUBMED_IDS`, `SEARCH_PUBTATOR`, `BUILD_KNOWLEDGE_BASE`, `GENERATE_ALIGNED_OBJECTS`, `FIND_ROLES`) are correctly detected and resumed from their exact failed step.
- **FR-015**: The deployment script MUST authenticate and purge queues (`mesh-queue` in `ontologies`; `ncbi-pubmed-queue` and `pubtator-queue` in `pubmed-integration`) via the RabbitMQ Management HTTP API with `curl` (`DELETE /api/queues/%2f/{queue}/contents`) using existing credentials (`RABBIT_USER`/`RABBIT_PASSWORD` at `http://${RABBIT_HOST}:15672`) between `docker rm` and `docker run -d`. If a target queue returns HTTP 404 (does not exist yet), the script MUST NOT fail the deployment.
- **FR-016**: The `resumeFailedPipelines` operation MUST process all `FAILED` pipelines sequentially, isolating exceptions per pipeline so that a failure in one does not halt the others, and MUST return a summary response detailing counts of resumed, skipped, and error pipelines.
- **FR-017**: Outbound inter-service REST calls across services (including `pubmed-integration`, with `quarkus-smallrye-fault-tolerance` added to its `pom.xml`) MUST apply brief retry attempts (maximum 2 retries with a 1-second delay via MicroProfile Fault Tolerance `@Retry(maxRetries = 2, delay = 1000)`) for transient failures, and if the error persists (HTTP 4xx/5xx, timeout, connection refused), the system MUST mark the active step as `FAILED`, update overall pipeline status to `FAILED`, and truncate the workflow chain.

### Key Entities *(include if feature involves data)*

- **Pipeline Configuration (`PipelineConfig`)**: Represents the central aggregate root of an analysis pipeline, containing pipeline metadata (`id`, `name`, `networkId`), current step indicator (`step`), and execution status history (`statuses`).
- **Network Aggregate (`NetworkCollection`)**: Represents the parent network associated with one or more pipelines, holding the owner `userId` used to re-establish tenant security context during background automated resumption.
- **Pipeline Step Execution**: Represents an individual lifecycle step within a pipeline, including its step identifier (`PipelineSteps`), operational state (`Status`: `CONFIG`, `PENDING`, `IN_PROGRESS`, `COMPLETED`, `FAILED`), timestamps, and contextual metrics.
- **Pipeline Step Lifecycle Sequence**: The deterministic ordered progression of pipeline stages:
  1. `LAUNCH` (Initial discovery launch)
  2. `SEARCH_LEVELS` (Biological entity tree search)
  3. `COMBINATIONS` (Term pair generation)
  4. `SEARCH_PUBMED_IDS` (Literature identifier search)
  5. `SEARCH_PUBTATOR` (PubTator annotation extraction)
  6. `BUILD_KNOWLEDGE_BASE` (Knowledge graph / base generation)
  7. `GENERATE_ALIGNED_OBJECTS` (Candidate aligned biological objects generation)
  8. `UPDATE_ALIGNED_OBJECTS` (Manual user validation and confirmation of symbols)
  9. `CONFIGURE_INFERENCES` (Manual user configuration of inference parameters)
  10. `FIND_ROLES` (Inference execution and role discovery)
  11. `UPDATE_BIOLOGICAL_OBJECTS` (Final biological object persistence)
- **Message Queue Definition**: The target RabbitMQ messaging queues subject to deployment purging:
  - `mesh-queue` (in `ontologies`)
  - `ncbi-pubmed-queue` (in `pubmed-integration`)
  - `pubtator-queue` (in `pubmed-integration`)

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of pipelines in `FAILED` status at automated steps are successfully evaluated and re-dispatched when `resumeFailedPipelines` is triggered.
- **SC-002**: 100% of inter-service REST failures (HTTP 4xx/5xx, connection timeouts) result in an immediate `FAILED` status update and complete truncation of subsequent workflow steps (0% runaway orphan executions).
- **SC-003**: Message queue depth in `mesh-queue`, `ncbi-pubmed-queue`, and `pubtator-queue` is verified to be exactly 0 prior to starting new worker containers during deployment.
- **SC-004**: Following container startup in `config-and-control`, the deployment pipeline triggers the resumption workflow within 30 seconds of passing container readiness health checks (`/q/health/ready`).
- **SC-005**: Automated deployment simulation tests pass with 100% success rate, verifying recovery from failure across each of the 7 automated pipeline steps.
- **SC-006**: In an error scenario where one pipeline fails to resume (e.g. downstream worker unavailable), 100% of remaining valid failed pipelines in the batch are still resumed successfully without process termination.

## Assumptions

- **Existing Status Reconciliation**: The existing `PipelineLifecycleReconciliationListener` (`@Observes StartupEvent/ShutdownEvent`) will continue to transition orphan `IN_PROGRESS` automated steps to `FAILED` when the container stops/starts, allowing `resumeFailedPipelines` to pick them up seamlessly.
- **Health Extension Availability**: Adding `quarkus-smallrye-health` to `config-and-control/pom.xml` enables `/q/health/ready` without requiring custom health check implementations.
- **Internal Network Security**: The endpoint `POST /config-and-control/pipelines/resume-failed` is an administrative operational endpoint intended for internal container network access during CI/CD execution and is not exposed to public untrusted ingress.
- **Purge Semantics**: Purging a queue deletes existing unacknowledged or queued messages but preserves the queue declaration, queue bindings, and exchange configuration in the RabbitMQ broker.
- **Idempotency**: Invoking `resumeFailedPipelines` when zero pipelines are in `FAILED` status is a safe no-op that returns a successful empty summary response without error.
- **Manual Steps Exemption**: Steps `UPDATE_ALIGNED_OBJECTS` and `CONFIGURE_INFERENCES` require human input via the web user interface and must never be automatically dispatched to worker microservices by background batch processes.
- **Network Connectivity**: During CI/CD deployment execution, the Jenkins agent has network reachability to the RabbitMQ broker (e.g., on the shared Docker network or host port) and to the newly deployed application container.

## Clarifications

### Session 2026-10-20

- - Q: ¿Cómo debe autenticarse y purgarse las colas de mensajería en CI/CD? (FR-015) → A: Opción A - Purgado vía RabbitMQ Management HTTP API con `curl` (`DELETE /api/queues/%2f/{queue}/contents`) usando las credenciales existentes `RABBIT_USER`/`RABBIT_PASSWORD` y `http://${RABBIT_HOST}:15672` entre `docker rm` y `docker run -d` (`mesh-queue` en `ontologies`; `ncbi-pubmed-queue` y `pubtator-queue` en `pubmed-integration`). Respuestas HTTP 404 (cola no declarada aún) se ignoran sin fallar el despliegue.
- - Q: ¿Cuál es la estrategia de concurrencia y control de tasa en `resumeFailedPipelines`? (FR-016) → A: Opción A - Procesamiento estrictamente secuencial con aislamiento de excepciones por pipeline (un fallo no detiene a los demás) para respetar los rate-limits de servicios biológicos externos (NCBI/PubTator), devolviendo un resumen consolidado con contadores de reanudados, omitidos y con error.
- - Q: ¿Cuál es la política de reintentos antes de declarar `FAILED` en llamadas REST? (FR-017) → A: Opción A - Aplicar hasta 2 reintentos con 1 segundo de intervalo vía MicroProfile Fault Tolerance (`@Retry(maxRetries = 2, delay = 1000)`). Si el fallo persiste (4xx, 5xx, timeout, conexión rechazada), transicionar inmediatamente el paso y el pipeline a `FAILED` y truncar la cadena de ejecución.
- - Q: ¿Cómo debe manejar el endpoint `resumeFailedPipelines` la autenticación y el contexto de usuario (`x-user-id`) al ser invocado por Jenkins? (FR-001) → A: Opción A - El endpoint `POST /config-and-control/pipelines/resume-failed` se excluye de la exigencia estricta de `x-user-id` en `ContextInterceptor` (o acepta `x-system-call: true`), y el caso de uso resuelve dinámicamente el `userId` dueño de cada pipeline desde su `NetworkCollection` asociada antes de despachar cada paso.
- - Q: ¿Qué mecanismo debe utilizar el `Jenkinsfile` de `config-and-control` para verificar que el contenedor está listo antes de invocar la reanudación? (FR-008) → A: Opción A - Incorporar la extensión estándar `quarkus-smallrye-health` en `pom.xml` de `config-and-control` y hacer que Jenkins sondee `http://localhost:${CONFIG_AND_CONTROL_PORT}/q/health/ready` mediante `curl` en un bucle con reintentos (máximo 30 segundos) antes de llamar a `resume-failed`.
- - Q: ¿Cómo debe implementarse el soporte de reintentos (Fault Tolerance) en `pubmed-integration`? (FR-011, FR-013, FR-017) → A: Opción A - Agregar `quarkus-smallrye-fault-tolerance` a `pubmed-integration/pom.xml` para aplicar `@Retry(maxRetries=2, delay=1000)` en clientes REST salientes, y en los bloques `catch` de sus controladores y workers asíncronos invocar `updateStep(pipelineId, step, Status.FAILED, userId)`, preservando el comportamiento de fallo existente en `GenerateAlignedObjectsUseCase` y `GenerateKbForPipelineUseCase`.
- - Q: Si un servicio worker destino aún no ha completado su despliegue al momento de la reanudación, ¿cómo debe reaccionar el sistema? (FR-006) → A: Opción A - Fail-fast idempotente: si la llamada REST al worker falla tras 2 reintentos, el pipeline se mantiene en `FAILED` en ese paso, se contabiliza como con error en el resumen del endpoint y queda disponible para una reanudación posterior sin bloquear el despliegue.
- - Q: ¿Cómo debe comportarse `resumeFailedPipelines` ante pipelines que fallaron en pasos de interacción manual o en `FIND_ROLES`? (FR-003, FR-007) → A: Opción A - Los pipelines fallidos en pasos manuales (`UPDATE_ALIGNED_OBJECTS`, `CONFIGURE_INFERENCES`) no se re-despachan a workers y quedan en espera de acción del usuario en la UI; si el fallo ocurrió en `FIND_ROLES`, se determina como predecesor `CONFIGURE_INFERENCES` y se re-invoca `triggerInferences.executeFindRoles`.
