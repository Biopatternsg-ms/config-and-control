# Contract: NetworkMapper

**Package**: `com.biopatternsg.infrastructure.adapters.mappers`
**Class**: `public final class NetworkMapper`

## Public API Contract

```java
package com.biopatternsg.infrastructure.adapters.mappers;

import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.domain.models.ReportFormat;
import com.biopatternsg.infrastructure.dtos.CreateNetworkRequest;
import com.biopatternsg.infrastructure.dtos.FindNetworkRequest;
import com.biopatternsg.infrastructure.dtos.NetworkResponse;
import com.biopatternsg.infrastructure.dtos.UpdateNetworkRequest;
import com.biopatternsg.infrastructure.mongo_db.collections.NetworkCollection;

import java.util.List;

public final class NetworkMapper {

    private NetworkMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // Canonical source-to-target mappers
    public static NetworkConfig collectionToModel(NetworkCollection networkCollection);
    public static NetworkResponse modelToResponse(NetworkConfig networkConfig);
    public static NetworkCollection modelToCollection(NetworkConfig networkConfig, String userId);
    public static List<NetworkConfig> collectionToModelList(List<NetworkCollection> collectionList);
    public static List<NetworkResponse> modelToResponseList(List<NetworkConfig> networkConfigList);
    public static NetworkConfig requestToModel(CreateNetworkRequest createNetwork);
    public static NetworkConfig requestToModel(UpdateNetworkRequest updateNetwork);
    public static NetworkConfig requestToModel(FindNetworkRequest findNetwork);

    // Envelopes
    public static ReportFormat<NetworkConfig> toDomainReport(ReportFormat<NetworkCollection> reportCollection);
    public static ReportFormat<NetworkResponse> toReportResponse(ReportFormat<NetworkConfig> modelFormat);

    // Backward-compatibility delegating aliases
    public static NetworkConfig toNetworkConfig(NetworkCollection networkCollection);
    public static NetworkResponse toNetworkResponse(NetworkConfig networkConfig);
    public static NetworkCollection toNetworkCollection(NetworkConfig networkConfig, String userId);
    public static List<NetworkConfig> toNetworkConfigList(List<NetworkCollection> collectionList);
    public static List<NetworkResponse> toNetworkResponseList(List<NetworkConfig> networkConfigList);
}
```

## Invariants
1. `NetworkMapper` cannot be instantiated via constructor or reflection.
2. Every method returns `null` or an empty object safely on `null` input.
3. `toDomainReport` returns `new ReportFormat<>(0L, List.of())` when `reportCollection` is `null` or its list is `null`.
