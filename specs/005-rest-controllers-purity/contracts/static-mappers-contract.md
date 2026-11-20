# Contract: Static Mappers

**Package**: `com.biopatternsg.infrastructure.adapters.mappers`

---

## 1. `KeycloakEventMapper`

Stateless utility class extracting actionable webhook events.

```java
package com.biopatternsg.infrastructure.adapters.mappers;

import com.biopatternsg.infrastructure.dtos.keycloak.KeycloakEventNotification;
import java.util.Optional;

public final class KeycloakEventMapper {

    private static final String VERIFY_EMAIL_TYPE = "VERIFY_EMAIL";
    private static final String CUSTOM_REQUIRED_ACTION_TYPE = "CUSTOM_REQUIRED_ACTION";
    private static final String CUSTOM_REQUIRED_ACTION_DETAIL = "custom_required_action";
    private static final String USERNAME_DETAIL = "username";

    private KeycloakEventMapper() {}

    /**
     * Evaluates a Keycloak webhook notification and extracts the verified target
     * user identifier (Keycloak ID or username fallback) if the event represents an email verification.
     *
     * @param event the webhook event payload
     * @return Optional containing the target identifier, or Optional.empty() if not actionable
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

---

## 2. `NetworkMapper` Report Extensions

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

---

## 3. `PipelineMapper` Extensions

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

---

## 4. `UserMapper` Report Extensions

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
