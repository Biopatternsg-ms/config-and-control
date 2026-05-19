package com.biopatternsg.domain.models;

import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.enums.Status;
import lombok.*;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PipelineStatus {
    private PipelineSteps step;
    private Status status;
    private Date createdAt;
}
