# Feature Specification: Use Cases Lightweight Orchestration and Parameter Encapsulation

**Feature Branch**: `004-usecase-orchestration-encapsulation`

**Created**: 2026-11-19

**Status**: Draft

**Input**: User description: "/speckit-specify basado en /home/yacson/Proyectos/biopatternsg-ms/SDD/config-and-control/03-use-cases-lightweight-orchestration-and-parameter-encapsulation"

## Clarifications

### Session 2026-11-19

- Q: ¿Dónde debe residir la lógica de resolución de pasos predecesores y despacho hacia servicios biológicos para la reanudación de pipelines? → A: Delegar a `PipelineStepOrchestrator` extendiendo su contrato para centralizar las invocaciones biológicas y de PubMed, manteniendo a `ResumePipelineUseCase` enfocado puramente en la coordinación del lote y el acumulador.
- Q: ¿Dónde debe residir la lógica para evaluar la completitud del paso de configuración (`CONFIG`) y la generación de métricas de objetos alineados? → A: Encapsular las invariantes de completitud y construcción de métricas en el modelo de dominio `PipelineConfig` (ej. métodos de dominio para verificar completitud del paso CONFIG y confirmación de objetos alineados), mientras `PipelineService` coordina la persistencia y actualización del estado.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Encapsulación de Parámetros en Operaciones de Pipeline (Priority: P1)

Como desarrollador del sistema y mantenedor de la arquitectura, quiero que todas las operaciones de orquestación y servicios de pipeline acepten un máximo de 3 parámetros individuales, agrupando atributos relacionados en modelos de contexto y estado, para asegurar una API interna cohesiva, legible, mantenible y libre de listas largas de argumentos.

**Why this priority**: Es la regla fundamental de encapsulación de parámetros (Principio II de la Constitución y Regla 1.3). Previene el acoplamiento y asegura firmas consistentes en toda la capa de aplicación antes de reorganizar la lógica de orquestación.

**Independent Test**: Puede ser verificado independientemente validando que ninguna operación de actualización de estado de pipeline ni de procesamiento por lote requiera más de 3 parámetros individuales, operando exitosamente a través de modelos contextuales o acumuladores.

**Acceptance Scenarios**:

1. **Given** una solicitud para registrar el progreso de un paso de pipeline con estado y métricas detalladas, **When** el orquestador invoca la actualización del paso, **Then** la operación recibe la identidad del pipeline y el objeto de estado encapsulado (2 parámetros en total), registrando exitosamente las métricas y el nuevo estado sin requerir argumentos individuales dispersos.
2. **Given** una operación de procesamiento de reanudación sobre un pipeline individual dentro de un lote, **When** se evalúa y ejecuta la reanudación del pipeline, **Then** la operación recibe como máximo 3 parámetros (encapsulando el contexto del pipeline, servicio de destino y receptores de resultado), ejecutando la reanudación y registrando el resultado sin sobrepasar el límite de parámetros.

---

### User Story 2 - Orquestación Liviana en Consultas de Ejecución y Métricas (Priority: P2)

Como cliente o analista del sistema, quiero consultar el estado de ejecución y el progreso temporal de un pipeline obteniendo un informe consolidado con métricas y cronología, donde el caso de uso actúe únicamente como orquestador liviano que valida existencia y delega la agregación y cálculo métrico al servicio especializado.

**Why this priority**: `GetPipelineExecutionUseCase` concentra actualmente más de 300 líneas de lógica de negocio pesada (cálculo de duraciones, agregación de métricas de pasos biológicos y mapeo visual), violando el principio de orquestador liviano.

**Independent Test**: Puede ser verificado de manera independiente solicitando la ejecución de un pipeline existente y comprobando que el caso de uso solo valida la existencia del pipeline y delega la construcción del informe métrico, retornando la estructura cronológica completa idéntica a la esperada.

**Acceptance Scenarios**:

1. **Given** un identificador de pipeline existente con múltiples pasos ejecutados, **When** se solicita el detalle de ejecución del pipeline, **Then** el caso de uso valida la existencia del pipeline, delega la agregación métrica y el cálculo de duraciones al servicio de ejecución, y retorna el informe cronológico completo con el estado y las métricas de cada paso.
2. **Given** un identificador de pipeline inexistente, **When** se solicita su detalle de ejecución, **Then** el caso de uso detecta la inexistencia de forma inmediata y reporta el error de entidad no encontrada sin iniciar cálculos innecesarios.

---

### User Story 3 - Orquestación Liviana en Reanudación de Pipelines por Lote (Priority: P3)

Como operador del sistema, quiero reanudar pipelines interrumpidos para un servicio biológico específico a través de un caso de uso liviano que coordine el flujo general por lotes, delegando las reglas de resolución de usuarios, dependencias entre pasos y despacho de reanudación a los componentes de servicio correspondientes.

**Why this priority**: La reanudación de pipelines contiene actualmente algoritmos de mapeo de pasos predecesores y acumulación de resultados distribuidos que deben estar encapsulados, dejando al caso de uso centrado en la coordinación del proceso por lote.

**Independent Test**: Puede ser probado independientemente enviando una solicitud de reanudación para un servicio y verificando que los pipelines elegibles se reanuden en el orden correcto, registrando los pipelines reanudados y fallidos mediante acumuladores cohesivos sin lógica de despacho dispersa.

**Acceptance Scenarios**:

1. **Given** un conjunto de pipelines en estado de progreso para un servicio biológico con identidades de usuario válidas, **When** se ejecuta la orden de reanudación, **Then** el caso de uso orquesta la identificación de candidatos, delega la reanudación y devuelve un resumen consolidado con el total de procesados, reanudados y fallidos.
2. **Given** un pipeline candidato cuya resolución de usuario no es válida, **When** se procesa dentro del lote de reanudación, **Then** el sistema registra el fallo en el acumulador con el mensaje descriptivo correspondiente y continúa con los demás pipelines sin interrumpir el lote completo.

---

### User Story 4 - Delegación de Reglas de Transición y Métricas en Actualización de Pipelines (Priority: P4)

Como usuario del sistema, quiero actualizar la configuración de un pipeline (como objetos alineados o parámetros de búsqueda) y que las reglas de completitud del paso y el cálculo de métricas biológicas sean evaluadas por el servicio de pipeline correspondiente y no embebidas directamente en el caso de uso.

**Why this priority**: Mantiene la responsabilidad única en el caso de uso de actualización, transfiriendo la validación de condiciones de completitud (ej. presencia de campos obligatorios en conjunto para dar por completado un paso de configuración) al servicio de dominio/aplicación.

**Independent Test**: Puede ser verificado ejecutando una actualización parcial de configuración y comprobando que la transición a estado completado y la construcción de métricas se realice según las reglas de negocio delegadas al servicio.

**Acceptance Scenarios**:

1. **Given** un pipeline en configuración donde se actualizan los objetos alineados confirmados, **When** se procesa la actualización, **Then** el servicio construye las métricas del paso (conteo de símbolos y mensaje de confirmación) y avanza el estado del paso biológico según las reglas establecidas.
2. **Given** un pipeline donde se actualizan parámetros de búsqueda y se satisfacen todas las condiciones requeridas (nombre, descripción, niveles, retMax y complejos máximos), **When** se procesa la actualización, **Then** el servicio evalúa la completitud del paso de configuración y lo marca como completado.

---

### Edge Cases

- **Pipeline sin métricas en pasos previos**: Cuando se consulta la ejecución de un pipeline que no tiene métricas registradas en alguno de sus pasos, el servicio debe generar duraciones por defecto y mensajes consistentes sin generar excepciones ni bloquear el informe.
- **Lote de reanudación vacío**: Cuando no existen pipelines en estado de progreso para el servicio consultado, el caso de uso debe retornar de inmediato un resumen de reanudación con conteo cero sin invocar operaciones de despacho.
- **Fallo total de red o de cliente externo durante la reanudación**: Si el despacho hacia un servicio externo falla para un pipeline, el error debe ser capturado en el acumulador del lote y la ejecución debe proceder con el siguiente elemento sin abortar el proceso.
- **Actualización concurrente de configuración**: Si se intenta actualizar un pipeline mientras su estado está en transición, el servicio de pipeline debe aplicar las reglas de consistencia asegurando que las métricas finales representen el último estado confirmado.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Ninguna operación pública o interna en casos de uso o servicios de aplicación debe aceptar más de 3 parámetros individuales. Toda operación que requiera 4 o más datos de entrada debe encapsularlos en un modelo de dominio o comando contextual.
- **FR-002**: La actualización de pasos de pipeline con métricas debe operar mediante un modelo de estado de pipeline encapsulado (recibiendo únicamente la identidad del pipeline y el estado encapsulado).
- **FR-003**: El caso de uso de consulta de ejecución de pipeline (`GetPipelineExecutionUseCase`) debe comportarse estrictamente como orquestador liviano: verificar la existencia del pipeline en el repositorio y delegar la agregación de métricas, cronología y cálculo de duraciones a un servicio especializado (`PipelineExecutionService`).
- **FR-004**: El cálculo de líneas temporales, derivación de métricas por paso biológico y asignación de identificadores visuales o etiquetas de estado debe residir exclusivamente en el servicio de ejecución y no en el caso de uso.
- **FR-005**: El caso de uso de reanudación de pipelines (`ResumePipelineUseCase`) debe coordinar el flujo por lotes sin contener algoritmos de despacho dispersos ni firmas de método que excedan 3 parámetros, delegando la resolución de pasos predecesores y despacho hacia microservicios a `PipelineStepOrchestrator`, y utilizando un objeto de contexto o acumulador para registrar resultados del lote.
- **FR-006**: La evaluación de reglas de completitud para el paso de configuración y la construcción de métricas de objetos alineados deben encapsularse como invariantes de negocio en el modelo de dominio `PipelineConfig`, liberando a `UpdatePipelineUseCase` de lógica procedural imperativa y permitiendo que `PipelineService` coordine la actualización de estado resultante.
- **FR-007**: Los contratos de respuesta hacia los clientes externos (tales como la respuesta de ejecución del experimento o el resumen de reanudación) deben mantener total compatibilidad estructural.
- **FR-008**: Toda modificación realizada sobre la orquestación y firmas de los casos de uso debe ser sincronizada inmediatamente en la documentación correspondiente bajo `.agents/use_cases/` conforme al Principio IV de la Constitución.

---

### Key Entities *(include if feature involves data)*

- **PipelineStatusUpdate**: Modelo conceptual que encapsula la transición de un paso de pipeline, incluyendo el paso biológico, el estado de ejecución y el mapa de métricas asociadas.
- **PipelineExecutionReport**: Modelo que representa el informe completo de ejecución de un pipeline, incluyendo identificadores, duraciones totales, estado global y la secuencia cronológica de pasos con sus métricas y atributos descriptivos.
- **PipelineResumptionContext / BatchAccumulator**: Estructura de contexto que transporta los parámetros y acumula los resultados (identificadores exitosos, fallidos y causas de error) durante el procesamiento de reanudación de pipelines por lote.
- **PipelineResumptionSummary**: Modelo de resultado que consolida los totales encontrados, reanudados, fallidos y detalle de errores tras procesar un lote de reanudación.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Cero métodos en casos de uso (`com.biopatternsg.application.usecase.*`) y servicios de aplicación (`com.biopatternsg.application.services.*`) aceptan más de 3 parámetros individuales.
- **SC-002**: Reducción de la complejidad ciclomática y del tamaño de `GetPipelineExecutionUseCase` a menos de 70 líneas de código, delegando el cálculo métrico y cronológico al servicio de ejecución.
- **SC-003**: Reducción del tamaño y responsabilidades de `ResumePipelineUseCase`, manteniendo una orquestación lineal y limpia sin métodos con 5 parámetros.
- **SC-004**: 100% de compatibilidad en las respuestas entregadas a los clientes consumidores, validada mediante la suite completa de pruebas unitarias y de integración.
- **SC-005**: 100% de cumplimiento de las pruebas arquitectónicas automatizadas (ArchUnit), garantizando que los casos de uso cumplan su rol de orquestadores livianos y respeten las fronteras de capa.
- **SC-006**: 100% de sincronización de la documentación de casos de uso en `.agents/use_cases/` para todos los casos de uso impactados.

---

## Assumptions

- Las definiciones de modelos de dominio existentes (`PipelineConfig`, `PipelineStatus`, `PipelineSteps`, `Status`) ofrecen los campos necesarios para encapsular las métricas y estados requeridos.
- Las reglas de negocio sobre cuándo un paso de configuración se considera completado no sufren cambios de comportamiento, únicamente se reubican en el componente responsable.
- La estructura de los mensajes de error y campos JSON expuestos a través de los adaptadores de entrega externa (REST) se preserva intacta.
- No se requiere introducir nuevos frameworks ni dependencias externas para implementar la encapsulación de parámetros.
