package com.biopatternsg.domain.port.out.repositories;

import com.biopatternsg.domain.models.PipelineConfig;

public interface BiologicalObjectRepository {

    String launch(PipelineConfig pipelineConfig);
}
