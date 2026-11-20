# Data Model and Method Contracts: Clean Code, Lambda Purity, and Static Mappers Standardization

**Feature**: `006-clean-code-lambdas-mappers` | **Date**: 2026-11-20

## 1. Static Mapper Contracts

### 1.1 `NetworkMapper`
Package: `com.biopatternsg.infrastructure.adapters.mappers`
Class Modifiers: `public final class NetworkMapper`
Constructor: `private NetworkMapper()`

| Method Signature | Input | Output | Description |
|------------------|-------|--------|-------------|
| `public static NetworkConfig collectionToModel(NetworkCollection collection)` | `NetworkCollection` (nullable) | `NetworkConfig` (nullable) | Canonical DB collection to domain model mapper |
| `public static NetworkCollection modelToCollection(NetworkConfig model, String userId)` | `NetworkConfig` (nullable), `String userId` | `NetworkCollection` (nullable) | Canonical domain model to DB collection mapper |
| `public static NetworkResponse modelToResponse(NetworkConfig model)` | `NetworkConfig` (nullable) | `NetworkResponse` (nullable) | Canonical domain model to REST response DTO mapper |
| `public static NetworkConfig requestToModel(CreateNetworkRequest request)` | `CreateNetworkRequest` (nullable) | `NetworkConfig` (nullable) | Maps create request DTO to domain model |
| `public static NetworkConfig requestToModel(UpdateNetworkRequest request)` | `UpdateNetworkRequest` (nullable) | `NetworkConfig` (nullable) | Maps update request DTO to domain model |
| `public static NetworkConfig requestToModel(FindNetworkRequest request)` | `FindNetworkRequest` (nullable) | `NetworkConfig` (nullable) | Maps search request DTO to query domain model |
| `public static List<NetworkConfig> collectionToModelList(List<NetworkCollection> list)` | `List<NetworkCollection>` (nullable) | `List<NetworkConfig>` | Null-safe list conversion |
| `public static List<NetworkResponse> modelToResponseList(List<NetworkConfig> list)` | `List<NetworkConfig>` (nullable) | `List<NetworkResponse>` | Null-safe list conversion |
| `public static ReportFormat<NetworkConfig> toDomainReport(ReportFormat<NetworkCollection> report)` | `ReportFormat<NetworkCollection>` (nullable) | `ReportFormat<NetworkConfig>` | Transforms DB paginated report to Domain report envelope |
| `public static ReportFormat<NetworkResponse> toReportResponse(ReportFormat<NetworkConfig> report)` | `ReportFormat<NetworkConfig>` (nullable) | `ReportFormat<NetworkResponse>` | Transforms Domain report to REST response envelope |
| *Backward-compatible aliases:* | | | |
| `public static NetworkConfig toNetworkConfig(NetworkCollection collection)` | `NetworkCollection` | `NetworkConfig` | Delegates to `collectionToModel(collection)` |
| `public static NetworkCollection toNetworkCollection(NetworkConfig model, String userId)` | `NetworkConfig`, `String` | `NetworkCollection` | Delegates to `modelToCollection(model, userId)` |
| `public static NetworkResponse toNetworkResponse(NetworkConfig model)` | `NetworkConfig` | `NetworkResponse` | Delegates to `modelToResponse(model)` |
| `public static List<NetworkConfig> toNetworkConfigList(List<NetworkCollection> list)` | `List<NetworkCollection>` | `List<NetworkConfig>` | Delegates to `collectionToModelList(list)` |
| `public static List<NetworkResponse> toNetworkResponseList(List<NetworkConfig> list)` | `List<NetworkConfig>` | `List<NetworkResponse>` | Delegates to `modelToResponseList(list)` |

---

### 1.2 `PipelineMapper`
Package: `com.biopatternsg.infrastructure.adapters.mappers`
Class Modifiers: `public final class PipelineMapper`
Constructor: `private PipelineMapper()`

| Method Signature | Input | Output | Description |
|------------------|-------|--------|-------------|
| `public static PipelineConfig collectionToModel(PipelineCollection collection)` | `PipelineCollection` (nullable) | `PipelineConfig` (nullable) | Canonical DB collection to domain model mapper |
| `public static PipelineCollection modelToCollection(PipelineConfig model)` | `PipelineConfig` (nullable) | `PipelineCollection` (nullable) | Canonical domain model to DB collection mapper |
| `public static PipelineResponse modelToResponse(PipelineConfig model)` | `PipelineConfig` (nullable) | `PipelineResponse` (nullable) | Canonical domain model to summary REST response DTO |
| `public static PipelineDetailResponse modelToDetailResponse(PipelineConfig model)` | `PipelineConfig` (nullable) | `PipelineDetailResponse` (nullable) | Canonical domain model to detailed REST response DTO |
| `public static PipelineConfig requestToModel(CreatePipelineRequest request)` | `CreatePipelineRequest` (nullable) | `PipelineConfig` (nullable) | Maps create request to domain model |
| `public static PipelineConfig requestToModel(FindPipelineRequest request)` | `FindPipelineRequest` (nullable) | `PipelineConfig` (nullable) | Maps find request to domain model |
| `public static List<PipelineConfig> collectionToModelList(List<PipelineCollection> list)` | `List<PipelineCollection>` (nullable) | `List<PipelineConfig>` | Null-safe list conversion |
| `public static List<PipelineResponse> modelToResponseList(List<PipelineConfig> list)` | `List<PipelineConfig>` (nullable) | `List<PipelineResponse>` | Null-safe list conversion |
| `public static List<ExpertObjectConfig> symbolsToExpertObjects(List<String> symbols)` | `List<String>` (nullable) | `List<ExpertObjectConfig>` | **Pure static function**: transforms string symbols into `ExpertObjectConfig` instances |
| `public static ReportFormat<PipelineConfig> toDomainReport(ReportFormat<PipelineCollection> report)` | `ReportFormat<PipelineCollection>` (nullable) | `ReportFormat<PipelineConfig>` | Transforms DB paginated report to Domain report envelope |
| `public static ReportFormat<PipelineResponse> toReportResponse(ReportFormat<PipelineConfig> report)` | `ReportFormat<PipelineConfig>` (nullable) | `ReportFormat<PipelineResponse>` | Transforms Domain report to REST response envelope |
| *Backward-compatible aliases:* | | | |
| `public static PipelineConfig collectionToConfig(PipelineCollection collection)` | `PipelineCollection` | `PipelineConfig` | Delegates to `collectionToModel(collection)` |
| `public static PipelineCollection configToCollection(PipelineConfig model)` | `PipelineConfig` | `PipelineCollection` | Delegates to `modelToCollection(model)` |
| `public static PipelineResponse configToResponse(PipelineConfig model)` | `PipelineConfig` | `PipelineResponse` | Delegates to `modelToResponse(model)` |
| `public static PipelineDetailResponse configToDetailResponse(PipelineConfig model)` | `PipelineConfig` | `PipelineDetailResponse` | Delegates to `modelToDetailResponse(model)` |
| `public static List<PipelineResponse> configToResponseList(List<PipelineConfig> list)` | `List<PipelineConfig>` | `List<PipelineResponse>` | Delegates to `modelToResponseList(list)` |

---

### 1.3 `UserMapper`
Package: `com.biopatternsg.infrastructure.adapters.mappers`
Class Modifiers: `public final class UserMapper`
Constructor: `private UserMapper()`

| Method Signature | Input | Output | Description |
|------------------|-------|--------|-------------|
| `public static UserConfig collectionToModel(UserCollection collection)` | `UserCollection` (nullable) | `UserConfig` (nullable) | Canonical DB collection to domain model mapper |
| `public static UserCollection modelToCollection(UserConfig model)` | `UserConfig` (nullable) | `UserCollection` (nullable) | Canonical domain model to DB collection mapper |
| `public static UserResponse modelToResponse(UserConfig model)` | `UserConfig` (nullable) | `UserResponse` (nullable) | Canonical domain model to REST response DTO |
| `public static UserConfig requestToModel(UserRequest request)` | `UserRequest` (nullable) | `UserConfig` (nullable) | Maps create user request to domain model |
| `public static UserConfig requestToModel(UserFiltersRequest request)` | `UserFiltersRequest` (nullable) | `UserConfig` (nullable) | Maps filter request to query domain model |
| `public static UserConfig keycloakToModel(UserKeycloakResponse response)` | `UserKeycloakResponse` (nullable) | `UserConfig` (nullable) | Maps Keycloak DTO to domain model |
| `public static List<UserResponse> modelToResponseList(List<UserConfig> list)` | `List<UserConfig>` (nullable) | `List<UserResponse>` | Null-safe list conversion |
| `public static ReportFormat<UserConfig> toDomainReport(ReportFormat<UserCollection> report)` | `ReportFormat<UserCollection>` (nullable) | `ReportFormat<UserConfig>` | Transforms DB paginated report to Domain report envelope |
| `public static ReportFormat<UserResponse> toReportResponse(ReportFormat<UserConfig> report)` | `ReportFormat<UserConfig>` (nullable) | `ReportFormat<UserResponse>` | Transforms Domain report to REST response envelope |

---

### 1.4 `KeycloakEventMapper`
Package: `com.biopatternsg.infrastructure.adapters.mappers`
Class Modifiers: `public final class KeycloakEventMapper`
Constructor: `private KeycloakEventMapper()` (already exists)

| Method Signature | Input | Output | Description |
|------------------|-------|--------|-------------|
| `public static Optional<String> extractEmailVerificationTargetId(KeycloakEventNotification event)` | `KeycloakEventNotification` (nullable) | `Optional<String>` | Pure static extractor for verified email target user ID or username |

---

## 2. Extracted Helper Methods

### 2.1 `PipelineConfig` (Domain Aggregate)
```java
// Extracted to replace inline block lambda in addStatusForStep
private void updateExistingStatusMetrics(PipelineStatus existingStatus, Map<String, String> metrics) {
    existingStatus.setCreatedAt(new Date());
    if (metrics != null) {
        existingStatus.setMetrics(metrics);
    }
}
```

### 2.2 `UserController` (REST Controller)
```java
// Extracted to replace inline block lambda in handleKeycloakEvent
private void dispatchEmailVerification(String targetId) {
    log.info("Processing email verification event for identifier: {}", targetId);
    userManagement.processEmailVerification(targetId);
}
```

### 2.3 `PubmedIntegrationAdapter` (Outbound Integration Adapter)
```java
// Replacement in executeGenerateAlignedObjects:
List<ExpertObjectConfig> expertObjects = (pipelineConfig.getAlignedExpertObjects() != null && !pipelineConfig.getAlignedExpertObjects().isEmpty())
        ? PipelineMapper.symbolsToExpertObjects(pipelineConfig.getAlignedExpertObjects())
        : pipelineConfig.getExpertObjects();
```
