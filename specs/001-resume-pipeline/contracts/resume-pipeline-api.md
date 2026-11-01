# API Contract: Resume Pipeline

**Feature**: `001-resume-pipeline`  
**Endpoint**: `POST /config-and-control/pipelines/resume`  
**Consumes**: `application/json`  
**Produces**: `application/json`  
**Authentication**: Unauthenticated / Public internal endpoint (exemption in `ContextInterceptor` for bootstrap automation)

---

## 1. OpenAPI 3.0 Specification

```yaml
openapi: 3.0.3
info:
  title: Config and Control - Resume Pipeline API
  version: 1.0.0
  description: Endpoint para detonar la reanudación autónoma de pipelines en progreso según el microservicio solicitante.
paths:
  /config-and-control/pipelines/resume:
    post:
      summary: Resume pending pipelines for a specific microservice
      description: |
        Busca todos los pipelines en estado IN_PROGRESS que corresponden al rango de pasos
        del microservicio especificado y detona su reanudación u orquestación hacia adelante.
      operationId: resumePipelines
      tags:
        - Pipelines
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ResumePipelineRequest'
            example:
              microService: "SEARCH_BIOLOGICAL_OBJECTS"
      responses:
        '200':
          description: Pipelines evaluados y reanudados exitosamente (o 0 si no había pendientes)
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PipelineResumeResponse'
              examples:
                successWithResumed:
                  summary: Lote procesado con pipelines reactivados
                  value:
                    microService: "SEARCH_BIOLOGICAL_OBJECTS"
                    totalFound: 2
                    resumedCount: 2
                    failedCount: 0
                    resumedPipelineIds:
                      - "671e18d6a89c8a45e0d19f12"
                      - "671e18dfa89c8a45e0d19f15"
                    failedPipelineIds: []
                    errors: {}
                nonePending:
                  summary: No se encontraron pipelines pendientes
                  value:
                    microService: "PUBMED_INTEGRATION"
                    totalFound: 0
                    resumedCount: 0
                    failedCount: 0
                    resumedPipelineIds: []
                    failedPipelineIds: []
                    errors: {}
        '400':
          description: Parámetro microService inválido, nulo o malformado
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
              example:
                status: 400
                message: "Invalid microService value. Accepted values: [SEARCH_BIOLOGICAL_OBJECTS, PUBMED_INTEGRATION]"
        '500':
          description: Error interno no controlado durante la ejecución del lote
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
              example:
                status: 500
                message: "Unexpected error during pipeline resumption batch"

components:
  schemas:
    MicroService:
      type: string
      enum:
        - SEARCH_BIOLOGICAL_OBJECTS
        - PUBMED_INTEGRATION
      description: Identificador del microservicio que solicita la reanudación

    ResumePipelineRequest:
      type: object
      required:
        - microService
      properties:
        microService:
          $ref: '#/components/schemas/MicroService'

    PipelineResumeResponse:
      type: object
      properties:
        microService:
          $ref: '#/components/schemas/MicroService'
        totalFound:
          type: integer
          description: Total de pipelines encontrados en progreso para el microservicio
          example: 2
        resumedCount:
          type: integer
          description: Cantidad de pipelines relanzados satisfactoriamente
          example: 2
        failedCount:
          type: integer
          description: Cantidad de pipelines que fallaron en su reanudación
          example: 0
        resumedPipelineIds:
          type: array
          items:
            type: string
          description: Identificadores de pipelines reanudados
        failedPipelineIds:
          type: array
          items:
            type: string
          description: Identificadores de pipelines fallidos
        errors:
          type: object
          additionalProperties:
            type: string
          description: Mapa de pipelineId -> causa del fallo

    ErrorResponse:
      type: object
      properties:
        status:
          type: integer
        message:
          type: string
```

---

## 2. MicroProfile REST Client Contract (Clientes en Microservicios)

En los microservicios clientes (`search-biological-objects` y `pubmed-integration`), se actualiza la interfaz `ConfigAndControlHttpClient`:

### 2.1 En `search-biological-objects`
*Path*: `src/main/java/com.biopatternsg.infrastructure.clients.internal_clients.ConfigAndControlHttpClient.java`

```java
package com.biopatternsg.infrastructure.clients.internal_clients;

import com.biopatternsg.infrastructure.dtos.PipelineStepInternalRequest;
import com.biopatternsg.infrastructure.dtos.ResumePipelineRequest;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "config-and-control-api")
public interface ConfigAndControlHttpClient {

    @PATCH
    @Retry
    @Path("/config-and-control/pipelines/update-step")
    void updatePipelineStep(PipelineStepInternalRequest updatePipelineStep, @HeaderParam("x-user-id") String userId);

    @POST
    @Path("/config-and-control/pipelines/resume")
    Response resumePipelines(ResumePipelineRequest request);
}
```

### 2.2 En `pubmed-integration`
*Path*: `src/main/java/com.biopatternsg.infrastructure.clients.internal.ConfigAndControlHttpClient.java`

```java
package com.biopatternsg.infrastructure.clients.internal;

import com.biopatternsg.infrastructure.adapters.dtos.PipelineStepRequest;
import com.biopatternsg.infrastructure.adapters.dtos.ResumePipelineRequest;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "config-and-control-api")
public interface ConfigAndControlHttpClient {

    @PATCH
    @Path("config-and-control/pipelines/update-step")
    void updateStep(PipelineStepRequest stepRequest, @HeaderParam("x-user-id") String userId);

    @POST
    @Path("config-and-control/pipelines/resume")
    Response resumePipelines(ResumePipelineRequest request);
}
```
