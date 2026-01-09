package com.biopatternsg.domain.models.pipeline_config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class ExpertObjectConfig {

    private String uniprotId;
    private String hgncId;
    private String symbol;
}
