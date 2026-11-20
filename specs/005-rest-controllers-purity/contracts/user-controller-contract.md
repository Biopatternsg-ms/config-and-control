# Contract: UserController & AdminController

**Controllers**: `UserController`, `AdminController`
**Package**: `com.biopatternsg.infrastructure.adapters.in.restcontrollers`

---

## 1. UserController Keycloak Webhook Contract

### 1.1. Keycloak Webhook Receiver
- **Method**: `POST`
- **Path**: `/config-and-control/users/keycloak-events`
- **Request Body**: `KeycloakEventNotification event`
- **Response**: HTTP 200 OK
- **Implementation**:
  ```java
  @POST
  @Path("/keycloak-events")
  @Operation(
          summary = "Keycloak event webhook receiver",
          description = "Receives events from Keycloak webhook plugin (e.g., VERIFY_EMAIL or CUSTOM_REQUIRED_ACTION)"
  )
  public Response handleKeycloakEvent(KeycloakEventNotification event) {
      log.info("Keycloak webhook event received: {}", event != null ? event.type() : "null");

      KeycloakEventMapper.extractEmailVerificationTargetId(event)
              .ifPresent(targetId -> {
                  log.info("Dispatching email verification for identifier: {}", targetId);
                  userManagement.processEmailVerification(targetId);
              });

      return Response.ok().build();
  }
  ```

---

## 2. AdminController List Users Contract

### 2.1. List Users with Filters
- **Method**: `GET`
- **Path**: `/config-and-control/admin/users`
- **Query Parameters**: via `UserFiltersRequest`
- **Response**: HTTP 200 OK with `ReportFormat<UserResponse>`
- **Implementation**:
  ```java
  @GET
  @Path("/users")
  public ReportFormat<UserResponse> listUsers(UserFiltersRequest userFilters) {
      var modelFormat = userManagement.listUsers(
              UserMapper.filtersToModel(userFilters),
              userFilters.page(),
              userFilters.size()
      );
      return UserMapper.toReportResponse(modelFormat);
  }
  ```
