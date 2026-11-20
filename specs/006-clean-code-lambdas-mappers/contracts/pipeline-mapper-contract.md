# Contract: PipelineMapper

**Package**: `com.biopatternsg.infrastructure.adapters.mappers`
**Class**: `public final class PipelineMapper`

## Public API Contract

```java
package com.biopatternsg.infrastructure.adapters.mappers;

import com.biopatternsg.domain.models.ExpertObjectConfig;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.models.PipelineResponse;
import com.biopatternsg.domain.models.ReportFormat;
import com.biopatternsg.infrastructure.dtos.*;
import com.biopatternsg.infrastructure.mongo_db.collections.PipelineCollection;

import java.util.List;

public final class PipelineMapper {

    private PipelineMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // Canonical source-to-target mappers
    public static PipelineConfig collectionToModel(PipelineCollection pipelineCollection);
    public static PipelineResponse modelToResponse(PipelineConfig pipelineConfig);
    public static PipelineDetailResponse modelToDetailResponse(PipelineConfig pipelineConfig);
    public static PipelineCollection modelToCollection(PipelineConfig pipelineConfig);
    public static List<PipelineResponse> modelToResponseList(List<PipelineConfig> pipelineConfigList);
    public static List<PipelineConfig> collectionToModelList(List<PipelineCollection> collectionList);
    public static PipelineConfig requestToModel(CreatePipelineRequest createPipelineRequest);
    public static PipelineConfig requestToModel(FindPipelineRequest findPipelineRequest);

    // Pure functional expert object mapper
    public static List<ExpertObjectConfig> symbolsToExpertObjects(List<String> symbols);

    // Envelopes
    public static ReportFormat<PipelineConfig> toDomainReport(ReportFormat<PipelineCollection> reportCollection);
    public static ReportFormat<PipelineResponse> toReportResponse(ReportFormat<PipelineConfig> modelFormat);

    // Backward-compatibility delegating aliases
    public static PipelineConfig collectionToConfig(PipelineCollection pipelineCollection);
    public static PipelineResponse configToResponse(PipelineConfig pipelineConfig);
    public static PipelineDetailResponse configToDetailResponse(PipelineConfig pipelineConfig);
    public static PipelineCollection configToCollection(PipelineConfig pipelineConfig);
    public static List<PipelineResponse> configToResponseList(List<PipelineConfig> pipelineConfigList);
}
```

## Invariants
1. `PipelineMapper` cannot be instantiated.
2. `symbolsToExpertObjects(null)` safely returns `List.of()`.
3. `symbolsToExpertObjects(symbols)` maps each non-null string into an `ExpertObjectConfig` with its symbol set, returning an immutable or unmodifiable list.
4. `toDomainReport` returns `new ReportFormat<>(0L, List.of())` when `reportCollection` is `null` or its list is `null`.
