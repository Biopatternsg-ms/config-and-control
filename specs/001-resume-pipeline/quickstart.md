# Quickstart & Validation Guide: Resume Pipeline

**Feature**: `001-resume-pipeline`  
**Date**: 2026-10-27  
**Scope**: `config-and-control`, `search-biological-objects`, `pubmed-integration`  

Este documento proporciona los escenarios de prueba ejecutables y comandos de validación para verificar el correcto funcionamiento de la funcionalidad de reanudación automática de pipelines sin duplicar detalles de implementación (consultar [data-model.md](data-model.md) y [contracts/resume-pipeline-api.md](contracts/resume-pipeline-api.md)).

---

## 1. Prerrequisitos del Entorno

1. **Servicios en ejecución:**
   - Instancia de MongoDB accesible según `application.properties` (por defecto `localhost:27017/config_and_control`).
   - Servicio `config-and-control` iniciado en modo desarrollo o pruebas (`mvn quarkus:dev`).
2. **Datos iniciales de prueba en MongoDB:**
   - Una red válida (`network`) asociada a un `userId` conocido.
   - Pipelines de prueba configurados vinculados a dicho `networkId` con diferentes estados iniciales.

---

## 2. Escenarios de Validación Ejecutables

### Escenario 1: Reanudación manual de Pipelines en Etapa Biológica (`SEARCH_BIOLOGICAL_OBJECTS`)

**Objetivo:** Verificar que pipelines con `step` en {`TRANSCRIPTION_FACTOR`, `EXPERT_OBJECTS`, `SEARCH_LEVELS`} y último estado `IN_PROGRESS` son detectados, resuelven su `userId` desde la red, y detonan el lanzamiento hacia el servicio de objetos biológicos.

1. **Preparación en MongoDB:**
   ```javascript
   // Insertar red de prueba
   const networkId = ObjectId();
   db.network.insertOne({
     _id: networkId,
     userId: "test-user-bio-01",
     name: "Red Validacion Bio",
     description: "Red de pruebas para reanudacion"
   });

   // Insertar pipeline interrumpido en paso EXPERT_OBJECTS (3)
   db.pipeline.insertOne({
     name: "Pipeline Test Interrumpido Bio",
     networkId: networkId.str,
     levels: 2,
     maxComplexes: 5,
     step: "EXPERT_OBJECTS",
     statuses: [
       { step: "LAUNCH", status: "COMPLETED", createdAt: new Date() },
       { step: "TRANSCRIPTION_FACTOR", status: "COMPLETED", createdAt: new Date() },
       { step: "EXPERT_OBJECTS", status: "IN_PROGRESS", createdAt: new Date() }
     ],
     expertObjects: [],
     transcriptionFactorConfig: {}
   });
   ```

2. **Comando de Ejecución (cURL):**
   ```bash
   curl -X POST http://localhost:8080/config-and-control/pipelines/resume \
     -H "Content-Type: application/json" \
     -d '{"microService": "SEARCH_BIOLOGICAL_OBJECTS"}'
   ```

3. **Resultado Esperado:**
   - Código HTTP `200 OK`.
   - Payload JSON acorde al contrato [contracts/resume-pipeline-api.md](contracts/resume-pipeline-api.md):
     - `microService`: `"SEARCH_BIOLOGICAL_OBJECTS"`
     - `totalFound`: 1
     - `resumedCount`: 1
     - `failedCount`: 0
     - `resumedPipelineIds`: Contiene el ID del pipeline insertado.
   - Logs de `config-and-control` evidencian la asignación de `userId = "test-user-bio-01"` en `SessionUtil` y la llamada a `biologicalObjectHttpClient.launch`.

---

### Escenario 2: Reanudación de Pipelines en Etapa PubMed (`PUBMED_INTEGRATION`)

**Objetivo:** Verificar que un pipeline con `step` en `BUILD_KNOWLEDGE_BASE` (8) con último estado `IN_PROGRESS` invoca a `pipelineStepOrchestrator.orchestrate` con el paso anterior `SEARCH_PUBTATOR` (7).

1. **Preparación en MongoDB:**
   ```javascript
   const networkId = ObjectId();
   db.network.insertOne({
     _id: networkId,
     userId: "test-user-pubmed-02",
     name: "Red PubMed",
     description: "Prueba PubMed"
   });

   db.pipeline.insertOne({
     name: "Pipeline Test Interrumpido PubMed",
     networkId: networkId.str,
     step: "BUILD_KNOWLEDGE_BASE",
     statuses: [
       { step: "SEARCH_PUBTATOR", status: "COMPLETED", createdAt: new Date() },
       { step: "BUILD_KNOWLEDGE_BASE", status: "IN_PROGRESS", createdAt: new Date() }
     ]
   });
   ```

2. **Comando de Ejecución (cURL):**
   ```bash
   curl -X POST http://localhost:8080/config-and-control/pipelines/resume \
     -H "Content-Type: application/json" \
     -d '{"microService": "PUBMED_INTEGRATION"}'
   ```

3. **Resultado Esperado:**
   - Código HTTP `200 OK`.
   - `resumedCount`: 1.
   - En logs se comprueba que `PipelineStepOrchestrator` recibe `SEARCH_PUBTATOR` y detona la generación de base de conocimiento o alineación de objetos.

---

### Escenario 3: Consulta sin pipelines pendientes

**Objetivo:** Comprobar que si no existen pipelines en progreso, el servicio responde limpiamente con conteo 0 sin fallar.

1. **Comando de Ejecución:**
   ```bash
   curl -X POST http://localhost:8080/config-and-control/pipelines/resume \
     -H "Content-Type: application/json" \
     -d '{"microService": "SEARCH_BIOLOGICAL_OBJECTS"}'
   ```

2. **Resultado Esperado:**
   - Código HTTP `200 OK`.
   - `totalFound`: 0, `resumedCount`: 0, `failedCount`: 0, `resumedPipelineIds`: [].

---

### Escenario 4: Validación de entrada con microservicio inválido

**Objetivo:** Rechazar peticiones con valores no permitidos en el enum.

1. **Comando de Ejecución:**
   ```bash
   curl -X POST http://localhost:8080/config-and-control/pipelines/resume \
     -H "Content-Type: application/json" \
     -d '{"microService": "INVALID_MICROSERVICE"}'
   ```

2. **Resultado Esperado:**
   - Código HTTP `400 Bad Request`.

---

### Escenario 5: Validación del arranque autónomo (`AppLifecycleBean`)

**Objetivo:** Comprobar que al arrancar `search-biological-objects` o `pubmed-integration`, el bean `AppLifecycleBean` emite la petición de reanudación automáticamente.

1. **Comando de Ejecución en `search-biological-objects`:**
   ```bash
   mvn test -Dtest=AppLifecycleBeanTest
   ```
2. **Comando de Ejecución en `pubmed-integration`:**
   ```bash
   mvn test -Dtest=AppLifecycleBeanTest
   ```
3. **Resultado Esperado:**
   - Las pruebas unitarias confirman la llamada a `configAndControlHttpClient.resumePipelines(...)`.
   - En caso de simular falla de red hacia `config-and-control`, la prueba confirma que la excepción es capturada sin abortar el arranque de la aplicación.

---

## 3. Pruebas de Integración y Reglas de Arquitectura

Para validar que la implementación no rompe los estándares constitucionales ni las capas de arquitectura hexagonal:

```bash
# En el directorio raíz de config-and-control:
mvn test -Dtest=ArchitectureLayerTest
```

**Criterio de Aceptación:**
- 100% de las pruebas de capas (`ArchitectureLayerTest`) deben pasar exitosamente con 0 violaciones de aislamiento de capas o convenciones de nomenclatura.
