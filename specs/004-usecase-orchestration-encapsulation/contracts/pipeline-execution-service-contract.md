# Contract: PipelineExecutionService

**Service**: `PipelineExecutionService`
**Package**: `com.biopatternsg.application.services`
**Implementation**: `com.biopatternsg.application.services.impl.PipelineExecutionServiceImpl`

---

## 1. Interface Definition

```java
package com.biopatternsg.application.services;

import com.biopatternsg.domain.models.ExperimentExecutionResponse;
import com.biopatternsg.domain.models.PipelineConfig;

public interface PipelineExecutionService {

    /**
     * Constructs a consolidated execution report for the given pipeline,
     * aggregating execution durations, timeline steps, step metrics,
     * and UI icon/description mappings.
     *
     * @param pipeline the pipeline configuration aggregate
     * @return the complete execution response
     */
    ExperimentExecutionResponse buildExecutionReport(PipelineConfig pipeline);
}
```

---

## 2. Behavioral Contract

1. **Input Validation**: `pipeline` must not be null.
2. **Durations**:
   - `totalExecutionTime`: Formatted duration (`"Xm Ys"` or `"Xs"`) from first status `createdAt` to last status `createdAt` (or current time if running).
   - `currentPhaseDuration`: Duration of the current in-progress phase.
3. **Step Sequence**:
   The report must evaluate all standard steps:
   - `CONFIG`
   - `SEARCH_BIOLOGICAL_OBJECTS`
   - `PUBMED_INTEGRATION`
   - `UPDATE_ALIGNED_OBJECTS`
   - `FIND_ROLES`
   - `INFER_PATHWAYS`
4. **Step Formatting**:
   - Each step must contain its designated Lucide `iconName` (`"Sliders"`, `"GitBranch"`, `"Cpu"`, `"BookOpen"`, etc.).
   - Each step must contain its descriptive human-readable label and text summary.
   - Specific step metrics (e.g. `totalAlignedObjects`, `derivedConceptsCount`, `inferenceRunTimeMs`) must be properly aggregated and mapped.
