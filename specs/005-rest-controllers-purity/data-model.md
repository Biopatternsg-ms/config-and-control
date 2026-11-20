# Data Model & Delivery DTO Specifications: REST Controllers Purity

**Feature**: `005-rest-controllers-purity`
**Date**: 2026-11-20
**Spec**: [spec.md](./spec.md)

---

## 1. Delivery Layer Transfer Objects (DTOs)

### 1.1. `PipelineDetailResponse` (New Record)
Represents the comprehensive configuration and state details of a pipeline, returned by single-entity query (`GET /{id}`) and update endpoints (`PUT /description`, `PUT /transcription-factor`, `PUT /expert-objects`, `PUT /search-config`, `PUT /aligned-expert-objects`, `POST /pipelines`).

- **Package**: `com.biopatternsg.infrastructure.dtos`
- **Class Structure**:
  ```java
  package com.biopatternsg.infrastructure.dtos;

  import com.biopatternsg.domain.enums.PipelineSteps;
  import com.biopatternsg.domain.models.PipelineStatus;
  import com.biopatternsg.domain.models.pipeline_config.ExpertObjectConfig;
  import com.biopatternsg.domain.models.pipeline_config.TranscriptionFactorConfig;
  import lombok.Builder;

  import java.util.List;

  @Builder
  public record PipelineDetailResponse(
          String id,
          String name,
          String description,
          String networkId,
          Integer levels,
          PipelineSteps step,
          List<PipelineStatus> statuses,
          PipelineStatus status,
          List<ExpertObjectConfig> expertObjects,
          List<String> alignedExpertObjects,
          TranscriptionFactorConfig transcriptionFactorConfig,
          boolean useOnlyPrincipalName,
          int createdAt,
          int retMax,
          Integer maxComplexes
  ) {
  }
  ```

### 1.2. `PipelineResponse` (Existing Lightweight Summary)
Represents a concise summary of a pipeline configuration, optimized for search and pagination lists (`GET /config-and-control/pipelines/list`).

- **Package**: `com.biopatternsg.infrastructure.dtos`
- **Fields**:
  - `id` (String): Unique identifier
  - `networkId` (String): Associated network ID
  - `name` (String): Name of the pipeline
  - `description` (String): High-level description
  - `step` (PipelineSteps): Current workflow step
  - `createdAt` (int): Creation timestamp
  - `status` (PipelineStatus): Most recent execution status

### 1.3. `NetworkResponse` (Existing Delivery Record)
Represents a network configuration, returned by `POST /networks`, `PUT /networks`, and `GET /networks/{id}`.

- **Package**: `com.biopatternsg.infrastructure.dtos`
- **Fields**:
  - `id` (String): Network unique identifier
  - `userId` (String): ID of the owner user
  - `name` (String): Network configuration name
  - `description` (String): Network description
  - `createdAt` (int): Creation timestamp

### 1.4. `ReportFormat<T>` (Domain Container)
Represents a paginated collection response envelope.
- **Fields**:
  - `count` (Long): Total count of matching records across all pages
  - `list` (List<T>): Current page elements

---

## 2. Static Mapper Contracts

### 2.1. `KeycloakEventMapper` (New Static Utility)
- **Package**: `com.biopatternsg.infrastructure.adapters.mappers`
- **Specification**:
  ```java
  package com.biopatternsg.infrastructure.adapters.mappers;

  import com.biopatternsg.infrastructure.dtos.keycloak.KeycloakEventNotification;
  import java.util.Optional;

  public class KeycloakEventMapper {

      private static final String VERIFY_EMAIL_TYPE = "VERIFY_EMAIL";
      private static final String CUSTOM_REQUIRED_ACTION_TYPE = "CUSTOM_REQUIRED_ACTION";
      private static final String CUSTOM_REQUIRED_ACTION_DETAIL = "custom_required_action";
      private static final String USERNAME_DETAIL = "username";

      private KeycloakEventMapper() {
          // Utility class
      }

      /**
       * Evaluates a Keycloak webhook notification and extracts the verified target
       * user identifier (Keycloak ID or username fallback) if the event represents an email verification.
       *
       * @param event the webhook event payload
       * @return an Optional containing the target identifier, or Optional.empty() if not actionable
       */
      public static Optional<String> extractEmailVerificationTargetId(KeycloakEventNotification event) {
          if (event == null || event.type() == null) {
              return Optional.empty();
          }

          boolean isVerifyEmail = VERIFY_EMAIL_TYPE.equalsIgnoreCase(event.type()) ||
                  (CUSTOM_REQUIRED_ACTION_TYPE.equalsIgnoreCase(event.type())
                          && event.details() != null
                          && VERIFY_EMAIL_TYPE.equalsIgnoreCase(String.valueOf(event.details().get(CUSTOM_REQUIRED_ACTION_DETAIL))));

          if (!isVerifyEmail) {
              return Optional.empty();
          }

          String targetId = (event.userId() != null && !event.userId().isBlank())
                  ? event.userId().trim()
                  : (event.details() != null ? String.valueOf(event.details().get(USERNAME_DETAIL)).trim() : null);

          if (targetId != null && !targetId.isBlank() && !"null".equalsIgnoreCase(targetId)) {
              return Optional.of(targetId);
          }

          return Optional.empty();
      }
  }
  ```

### 2.2. Added Methods on Existing Static Mappers

#### In `NetworkMapper`:
```java
public static ReportFormat<NetworkResponse> toReportResponse(ReportFormat<NetworkConfig> modelFormat) {
    if (modelFormat == null) {
        return new ReportFormat<>(0L, List.of());
    }
    List<NetworkResponse> responseList = modelFormat.list() != null
            ? modelFormat.list().stream().map(NetworkMapper::toNetworkResponse).toList()
            : List.of();
    return new ReportFormat<>(modelFormat.count(), responseList);
}
```

#### In `PipelineMapper`:
```java
public static PipelineDetailResponse configToDetailResponse(PipelineConfig pipelineConfig) {
    if (pipelineConfig == null) {
        return null;
    }
    return PipelineDetailResponse.builder()
            .id(pipelineConfig.getId())
            .name(pipelineConfig.getName())
            .description(pipelineConfig.getDescription())
            .networkId(pipelineConfig.getNetworkId())
            .levels(pipelineConfig.getLevels())
            .step(pipelineConfig.getStep())
            .statuses(pipelineConfig.getStatuses() != null ? List.copyOf(pipelineConfig.getStatuses()) : List.of())
            .status(getMostRecentStatus(pipelineConfig.getStatuses()))
            .expertObjects(pipelineConfig.getExpertObjects() != null ? List.copyOf(pipelineConfig.getExpertObjects()) : List.of())
            .alignedExpertObjects(pipelineConfig.getAlignedExpertObjects() != null ? List.copyOf(pipelineConfig.getAlignedExpertObjects()) : List.of())
            .transcriptionFactorConfig(pipelineConfig.getTranscriptionFactorConfig())
            .useOnlyPrincipalName(pipelineConfig.isUseOnlyPrincipalName())
            .createdAt(pipelineConfig.getCreatedAt())
            .retMax(pipelineConfig.getRetMax())
            .maxComplexes(pipelineConfig.getMaxComplexes())
            .build();
}

public static ReportFormat<PipelineResponse> toReportResponse(ReportFormat<PipelineConfig> modelFormat) {
    if (modelFormat == null) {
        return new ReportFormat<>(0L, List.of());
    }
    List<PipelineResponse> responseList = modelFormat.list() != null
            ? modelFormat.list().stream().map(PipelineMapper::configToResponse).toList()
            : List.of();
    return new ReportFormat<>(modelFormat.count(), responseList);
}
```

#### In `UserMapper`:
```java
public static ReportFormat<UserResponse> toReportResponse(ReportFormat<UserConfig> modelFormat) {
    if (modelFormat == null) {
        return new ReportFormat<>(0L, List.of());
    }
    List<UserResponse> responseList = modelFormat.list() != null
            ? modelFormat.list().stream().map(UserMapper::modelToResponse).toList()
            : List.of();
    return new ReportFormat<>(modelFormat.count(), responseList);
}
```
