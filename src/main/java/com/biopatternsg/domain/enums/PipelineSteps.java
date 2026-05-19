package com.biopatternsg.domain.enums;

import lombok.Getter;

@Getter
public enum PipelineSteps {

    CONFIG("configuration", PipelineStage.INIT),
    LAUNCH("launched", PipelineStage.INIT),
    TRANSCRIPTION_FACTOR("transcription_factor", PipelineStage.BIOLOGICAL_OBJECT),
    EXPERT_OBJECTS("expert_objects", PipelineStage.BIOLOGICAL_OBJECT),
    SEARCH_LEVELS("search_levels", PipelineStage.BIOLOGICAL_OBJECT),
    COMBINATIONS("combinations", PipelineStage.PUBMED_INTEGRATION);

    private final String value;
    private final PipelineStage stage;

    PipelineSteps(String value, PipelineStage stage) {
        this.value = value;
        this.stage = stage;
    }

}
