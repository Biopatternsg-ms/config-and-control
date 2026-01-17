package com.biopatternsg.domain.models;

import com.biopatternsg.domain.models.pipeline_config.ExpertObjectConfig;
import com.biopatternsg.domain.models.pipeline_config.TranscriptionFactorConfig;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class PipelineConfig {

    private String id;
    private String name;
    private String description;
    private String networkId;
    private Integer levels;
    private List<ExpertObjectConfig> expertObjects;
    private TranscriptionFactorConfig transcriptionFactorConfig;
}
