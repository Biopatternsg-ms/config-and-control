# Contract: PipelineController

**Controller**: `PipelineController`
**Package**: `com.biopatternsg.infrastructure.adapters.in.restcontrollers`
**Base Path**: `/config-and-control/pipelines`

---

## 1. Endpoints Specification

### 1.1. Create Pipeline
- **Method**: `POST`
- **Path**: `/config-and-control/pipelines`
- **Request Body**: `@Valid CreatePipelineRequest`
- **Response**: HTTP 201 Created with `PipelineDetailResponse` entity
- **Implementation**:
  ```java
  @POST
  public Response create(@Valid CreatePipelineRequest newPipeline) {
      var config = createPipeline.execute(PipelineMapper.requestToCreate(newPipeline));
      return Response.status(Response.Status.CREATED)
              .entity(PipelineMapper.configToDetailResponse(config))
              .build();
  }
  ```

---

### 1.2. Update Description
- **Method**: `PUT`
- **Path**: `/config-and-control/pipelines/description`
- **Request Body**: `@Valid UpdatePipelineDescriptionRequest`
- **Response**: HTTP 200 OK with `PipelineDetailResponse` body
- **Implementation**:
  ```java
  @PUT
  @Path("/description")
  public PipelineDetailResponse updateDescription(@Valid UpdatePipelineDescriptionRequest updatePipeline) {
      var config = updatePipelineUseCase.updateDescription(PipelineMapper.requestToUpdate(updatePipeline));
      return PipelineMapper.configToDetailResponse(config);
  }
  ```

---

### 1.3. Update Transcription Factor
- **Method**: `PUT`
- **Path**: `/config-and-control/pipelines/transcription-factor`
- **Request Body**: `@Valid UpdatePipelineTranscriptionFactorRequest`
- **Response**: HTTP 200 OK with `PipelineDetailResponse` body
- **Implementation**:
  ```java
  @PUT
  @Path("/transcription-factor")
  public PipelineDetailResponse updateTranscriptionFactor(@Valid UpdatePipelineTranscriptionFactorRequest updatePipeline) {
      var config = updatePipelineUseCase.updateTranscriptionFactor(PipelineMapper.requestToUpdate(updatePipeline));
      return PipelineMapper.configToDetailResponse(config);
  }
  ```

---

### 1.4. Update Expert Objects
- **Method**: `PUT`
- **Path**: `/config-and-control/pipelines/expert-objects`
- **Request Body**: `@Valid UpdatePipelineExpertObjectsRequest`
- **Response**: HTTP 200 OK with `PipelineDetailResponse` body
- **Implementation**:
  ```java
  @PUT
  @Path("/expert-objects")
  public PipelineDetailResponse updateExpertObjects(@Valid UpdatePipelineExpertObjectsRequest updatePipeline) {
      var config = updatePipelineUseCase.updateExpertObjects(PipelineMapper.requestToUpdate(updatePipeline));
      return PipelineMapper.configToDetailResponse(config);
  }
  ```

---

### 1.5. Update Search Configuration
- **Method**: `PUT`
- **Path**: `/config-and-control/pipelines/search-config`
- **Request Body**: `@Valid UpdatePipelineSearchConfigRequest`
- **Response**: HTTP 200 OK with `PipelineDetailResponse` body
- **Implementation**:
  ```java
  @PUT
  @Path("/search-config")
  public PipelineDetailResponse updateSearchConfig(@Valid UpdatePipelineSearchConfigRequest updatePipeline) {
      var config = updatePipelineUseCase.updateSearchConfig(PipelineMapper.requestToUpdate(updatePipeline));
      return PipelineMapper.configToDetailResponse(config);
  }
  ```

---

### 1.6. Update Aligned Expert Objects
- **Method**: `PUT`
- **Path**: `/config-and-control/pipelines/aligned-expert-objects`
- **Request Body**: `@Valid UpdateAlignedExpertObjectsRequest`
- **Response**: HTTP 200 OK with `PipelineDetailResponse` body
- **Implementation**:
  ```java
  @PUT
  @Path("/aligned-expert-objects")
  public PipelineDetailResponse updateAlignedExpertObjects(@Valid UpdateAlignedExpertObjectsRequest updatePipeline) {
      var config = updatePipelineUseCase.updateAlignedExpertObjects(PipelineMapper.requestToUpdate(updatePipeline));
      return PipelineMapper.configToDetailResponse(config);
  }
  ```

---

### 1.7. Find Pipeline by ID
- **Method**: `GET`
- **Path**: `/config-and-control/pipelines/{id}`
- **Path Parameter**: `id` (String)
- **Response**: HTTP 200 OK with `PipelineDetailResponse` body
- **Implementation**:
  ```java
  @GET
  @Path("/{id}")
  public PipelineDetailResponse findList(@PathParam("id") String id) {
      var config = findPipeline.byId(id);
      return PipelineMapper.configToDetailResponse(config);
  }
  ```

---

### 1.8. List Pipelines with Filters
- **Method**: `GET`
- **Path**: `/config-and-control/pipelines/list`
- **Query Parameters**: via `FindPipelineRequest`
- **Response**: HTTP 200 OK with `ReportFormat<PipelineResponse>` body
- **Implementation**:
  ```java
  @GET
  @Path("/list")
  public ReportFormat<PipelineResponse> findList(FindPipelineRequest pipelineFilters) {
      var modelFormat = findPipeline.findList(
              PipelineMapper.requestToUpdate(pipelineFilters),
              pipelineFilters.page(),
              pipelineFilters.size()
      );
      return PipelineMapper.toReportResponse(modelFormat);
  }
  ```

---

### 1.9. Resume Pipelines (Declarative Validation)
- **Method**: `POST`
- **Path**: `/config-and-control/pipelines/resume`
- **Request Body**: `@Valid ResumePipelineRequest request`
- **Response**: HTTP 200 OK with `PipelineResumeResponse` body
- **Implementation**:
  ```java
  @POST
  @Path("/resume")
  public Response resume(@Valid ResumePipelineRequest request) {
      var summary = resumePipeline.execute(request.microService());
      var response = PipelineMapper.toResponse(summary);
      return Response.ok(response).build();
  }
  ```
