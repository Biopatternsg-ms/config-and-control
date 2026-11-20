# Contract: NetworkController

**Controller**: `NetworkController`
**Package**: `com.biopatternsg.infrastructure.adapters.in.restcontrollers`
**Base Path**: `/config-and-control/networks`

---

## 1. Endpoints Specification

### 1.1. Create Network Configuration
- **Method**: `POST`
- **Path**: `/config-and-control/networks`
- **Request Body**: `@Valid CreateNetworkRequest`
- **Method Signature**: `public Response create(@Valid CreateNetworkRequest newNetwork)`
- **Response**: HTTP 201 Created with `NetworkResponse` entity
- **Implementation**:
  ```java
  @POST
  @Operation(
      summary = "Create network configuration",
      description = "Creates a new network configuration for biological data processing."
  )
  @APIResponses({
      @APIResponse(
          responseCode = "201",
          description = "Network configuration successfully created",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = NetworkResponse.class)
          )
      )
  })
  public Response create(@Valid CreateNetworkRequest newNetwork) {
      var config = createNetwork.execute(NetworkMapper.requestToModel(newNetwork));
      return Response.status(Response.Status.CREATED)
              .entity(NetworkMapper.toNetworkResponse(config))
              .build();
  }
  ```

---

### 1.2. Update Network Configuration
- **Method**: `PUT`
- **Path**: `/config-and-control/networks`
- **Request Body**: `@Valid UpdateNetworkRequest`
- **Method Signature**: `public NetworkResponse update(@Valid UpdateNetworkRequest updateNetworkRequest)`
- **Response**: HTTP 200 OK with `NetworkResponse` body (strongly typed, no domain model return)
- **Implementation**:
  ```java
  @PUT
  @Operation(
      summary = "Update network configuration",
      description = "Updates an existing network configuration for biological data processing."
  )
  @APIResponses({
      @APIResponse(
          responseCode = "200",
          description = "Network configuration successfully updated",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = NetworkResponse.class)
          )
      )
  })
  public NetworkResponse update(@Valid UpdateNetworkRequest updateNetworkRequest) {
      var config = updateNetwork.execute(NetworkMapper.requestToModel(updateNetworkRequest));
      return NetworkMapper.toNetworkResponse(config);
  }
  ```

---

### 1.3. Find Network by ID
- **Method**: `GET`
- **Path**: `/config-and-control/networks/{id}`
- **Path Parameter**: `id` (String)
- **Method Signature**: `public NetworkResponse findList(@PathParam("id") String id)`
- **Response**: HTTP 200 OK with `NetworkResponse` body
- **Implementation**:
  ```java
  @GET
  @Path("/{id}")
  @Operation(
      summary = "Get network configuration by id",
      description = "Obtains the network configuration details by id"
  )
  @APIResponses({
      @APIResponse(
          responseCode = "200",
          description = "Network configuration successfully obtained",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = NetworkResponse.class)
          )
      )
  })
  public NetworkResponse findList(@PathParam("id") String id) {
      return NetworkMapper.toNetworkResponse(findNetwork.byId(id));
  }
  ```

---

### 1.4. List Networks with Filters
- **Method**: `GET`
- **Path**: `/config-and-control/networks/list`
- **Query Parameters**: via `FindNetworkRequest`
- **Method Signature**: `public ReportFormat<NetworkResponse> findList(FindNetworkRequest networkFilters)`
- **Response**: HTTP 200 OK with `ReportFormat<NetworkResponse>`
- **Implementation**:
  ```java
  @GET
  @Path("/list")
  @Operation(
      summary = "List user networks",
      description = "List networks associated for an user with option to apply same filters"
  )
  @APIResponses({
      @APIResponse(
          responseCode = "200",
          description = "User networks list successfully obtained",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ReportFormat.class)
          )
      )
  })
  public ReportFormat<NetworkResponse> findList(FindNetworkRequest networkFilters) {
      var modelFormat = findNetwork.findList(
              NetworkMapper.requestToModel(networkFilters),
              networkFilters.page(),
              networkFilters.size()
      );
      return NetworkMapper.toReportResponse(modelFormat);
  }
  ```
