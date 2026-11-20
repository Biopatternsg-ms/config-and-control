# Interface Contract: PipelineStepOrchestrator

## Contract Definition

- **Class Name**: `com.biopatternsg.application.services.PipelineStepOrchestrator`
- **Architectural Role**: Application Service Public Contract (`com.biopatternsg.application.services`)
- **Implementer**: `PipelineStepOrchestratorImpl` (`com.biopatternsg.application.services.impl.PipelineStepOrchestratorImpl`)
- **Primary Invocators**:
  - `UpdatePipelineUseCase` (`com.biopatternsg.application.usecase.UpdatePipelineUseCase`)
  - `UpdatePipelineStepUseCase` (`com.biopatternsg.application.usecase.UpdatePipelineStepUseCase`)
  - `ResumePipelineUseCase` (`com.biopatternsg.application.usecase.ResumePipelineUseCase`)

---

## Method Specification

```java
package com.biopatternsg.application.services;

import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.models.PipelineConfig;

public interface PipelineStepOrchestrator {

    /**
     * Orchestrates the advancement and external service integration for a given pipeline step.
     *
     * @param pipelineConfig the pipeline execution aggregate
     * @param step the current step that completed or triggered progression
     */
    void orchestrate(PipelineConfig pipelineConfig, PipelineSteps step);
}
```

---

## Architectural Rules

1. **Encapsulation**: The interface contract is placed in `application.services`. The implementation MUST reside in `application.services.impl`.
2. **Interface Injection**: All use cases and services MUST inject `PipelineStepOrchestrator` (the interface), NEVER `PipelineStepOrchestratorImpl`.
3. **CDI**: Marked with `@ApplicationScoped` on the implementation class.
