# Interface Contract: PipelineService (Application Service Contract)

## Contract Definition

- **Class Name**: `com.biopatternsg.application.services.PipelineService`
- **Architectural Role**: Application Service Interface (`com.biopatternsg.application.services`)
- **Implementer**: `PipelineServiceImpl` (`com.biopatternsg.application.services.PipelineServiceImpl`)
- **Package Location**: `src/main/java/com/biopatternsg/application/services/PipelineService.java`

---

## Method Specification

```java
package com.biopatternsg.application.services;

import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.enums.Status;
import java.util.Map;

public interface PipelineService {

    /**
     * Updates the status of a specific step in the given pipeline execution.
     *
     * @param pipelineId the ID of the pipeline execution
     * @param step the biological pipeline step being updated
     * @param status the new status (IN_PROGRESS, COMPLETED, FAILED)
     */
    void updateStep(String pipelineId, PipelineSteps step, Status status);

    /**
     * Updates the status of a specific step along with execution metrics.
     *
     * @param pipelineId the ID of the pipeline execution
     * @param step the biological pipeline step being updated
     * @param status the new status (IN_PROGRESS, COMPLETED, FAILED)
     * @param metrics key-value execution metrics recorded during the step
     */
    void updateStep(String pipelineId, PipelineSteps step, Status status, Map<String, String> metrics);
}
```

---

## Architectural Rules

1. **Package Integrity**: The interface must reside strictly under `com.biopatternsg.application.services`.
2. **Directory Removal**: The directory `src/main/java/com/biopatternsg/domain/services/` must not exist.
3. **Implementation Alignment**: The implementation class `PipelineServiceImpl` must declare package `com.biopatternsg.application.services`.
