# Contract: PipelineService Parameter Encapsulation

**Service**: `PipelineService`
**Package**: `com.biopatternsg.application.services`
**Implementation**: `com.biopatternsg.application.services.impl.PipelineServiceImpl`

---

## 1. Interface Definition (Strict Rule 1.3 Compliance)

```java
package com.biopatternsg.application.services;

import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.enums.Status;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.models.PipelineStatus;

public interface PipelineService {

    /**
     * Updates pipeline step and status without additional metrics.
     * (3 parameters - Compliant with Rule 1.3)
     */
    PipelineConfig updateStep(String id, PipelineSteps step, Status status);

    /**
     * Updates pipeline step with encapsulated status information including metrics.
     * Replaces the former 4-parameter method.
     * (2 parameters - Compliant with Rule 1.3)
     */
    PipelineConfig updateStep(String id, PipelineStatus statusUpdate);
}
```

---

## 2. Deprecation / Removal of 4-Parameter Method

The signature:
```java
// REMOVED / FORBIDDEN:
PipelineConfig updateStep(String id, PipelineSteps step, Status status, Map<String, String> metrics);
```
Violates Rule 1.3 (max 3 parameters) and is completely eliminated. Callers construct `PipelineStatus` with `builder().step(step).status(status).metrics(metrics).build()` and pass it to `updateStep(id, statusUpdate)`.
