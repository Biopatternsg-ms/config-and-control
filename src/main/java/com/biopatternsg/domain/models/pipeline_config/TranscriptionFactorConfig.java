package com.biopatternsg.domain.models.pipeline_config;

import com.biopatternsg.domain.enums.Genome;
import com.biopatternsg.domain.enums.Strand;
import com.biopatternsg.domain.enums.TranscriptionFactorSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class TranscriptionFactorConfig {

    private String promoterRegion;
    private int reliability;
    private BigDecimal start;
    private BigDecimal end;
    private Genome genome;
    private String track;
    private Float identity;
    private String chromosome;
    private Strand strand;
    private List<TranscriptionFactorSource> sources;
}
