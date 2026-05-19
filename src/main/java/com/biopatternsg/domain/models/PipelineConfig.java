package com.biopatternsg.domain.models;

import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.enums.Status;
import com.biopatternsg.domain.models.pipeline_config.ExpertObjectConfig;
import com.biopatternsg.domain.models.pipeline_config.TranscriptionFactorConfig;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
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
    private PipelineSteps step;
    private List<PipelineStatus> statuses;
    private List<ExpertObjectConfig> expertObjects;
    private TranscriptionFactorConfig transcriptionFactorConfig;
    private boolean useOnlyPrincipalName;
    private int createdAt;

    public void addStatus(Status status) {
        if(this.statuses == null) {
            this.statuses = new ArrayList<>();
        }
        this.statuses.stream()
                .filter(ps -> ps.getStep() == this.step && ps.getStatus() == status)
                .findFirst()
                .ifPresentOrElse(
                        ps -> ps.setCreatedAt(new Date()),
                        () -> this.statuses.add(new PipelineStatus(this.step, status, new Date()))
                );
    }
}
