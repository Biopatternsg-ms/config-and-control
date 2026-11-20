# Contract: UserMapper

**Package**: `com.biopatternsg.infrastructure.adapters.mappers`
**Class**: `public final class UserMapper`

## Public API Contract

```java
package com.biopatternsg.infrastructure.adapters.mappers;

import com.biopatternsg.domain.models.ReportFormat;
import com.biopatternsg.domain.models.UserConfig;
import com.biopatternsg.infrastructure.dtos.UserFiltersRequest;
import com.biopatternsg.infrastructure.dtos.UserRequest;
import com.biopatternsg.infrastructure.dtos.UserResponse;
import com.biopatternsg.infrastructure.dtos.keycloak.UserKeycloakResponse;
import com.biopatternsg.infrastructure.mongo_db.collections.UserCollection;

import java.util.List;

public final class UserMapper {

    private UserMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // Canonical source-to-target mappers
    public static UserConfig collectionToModel(UserCollection userCollection);
    public static UserCollection modelToCollection(UserConfig userConfig);
    public static UserResponse modelToResponse(UserConfig userConfig);
    public static UserConfig requestToModel(UserRequest userRequest);
    public static UserConfig requestToModel(UserFiltersRequest userFiltersRequest);
    public static UserConfig keycloakToModel(UserKeycloakResponse userKeycloakResponse);
    public static List<UserResponse> modelToResponseList(List<UserConfig> userConfigList);
    public static List<UserConfig> collectionToModelList(List<UserCollection> collectionList);

    // Envelopes
    public static ReportFormat<UserConfig> toDomainReport(ReportFormat<UserCollection> reportCollection);
    public static ReportFormat<UserResponse> toReportResponse(ReportFormat<UserConfig> modelFormat);
}
```

## Invariants
1. `UserMapper` cannot be instantiated.
2. Null input safely yields null or empty collections.
3. `toDomainReport` returns `new ReportFormat<>(0L, List.of())` on null or empty input.
