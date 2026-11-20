# Contract: Resumption Flow and PipelineStepOrchestrator Extension

**Service**: `PipelineStepOrchestrator`
**Package**: `com.biopatternsg.application.services`
**Implementation**: `com.biopatternsg.application.services.impl.PipelineStepOrchestratorImpl`

---

## 1. Extended Interface Contract

```java
package com.biopatternsg.application.services;

import com.biopatternsg.domain.enums.MicroService;
import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.models.PipelineConfig;

public interface PipelineStepOrchestrator {

    void executeStep(PipelineConfig pipelineConfig, PipelineSteps step);

    /**
     * Resumes an interrupted pipeline for the specified biological microservice.
     * Evaluates predecessor step prerequisites and dispatches to the appropriate outbound adapter.
     * (3 parameters - Compliant with Rule 1.3)
     *
     * @param pipeline     the pipeline configuration to resume
     * @param userId       the resolved user identifier
     * @param microService the target microservice being resumed
     * @throws RuntimeException if predecessor prerequisites are unmet or external call fails
     */
    void resumePipeline(PipelineConfig pipeline, String userId, MicroService microService);
}
```

---

## 2. Caller Contract (`ResumePipelineUseCase`)

`ResumePipelineUseCase` delegates execution per candidate pipeline:
```java
pipelineStepOrchestrator.resumePipeline(pipeline, userId, microService);
```
All helper methods within `ResumePipelineUseCase` operate on `(PipelineConfig pipeline, MicroService microService, PipelineResumptionBatch batch)` with a maximum of 3 parameters.
