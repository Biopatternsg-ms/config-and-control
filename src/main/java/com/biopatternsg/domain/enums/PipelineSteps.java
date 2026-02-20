package com.biopatternsg.domain.enums;

import lombok.Getter;

public enum PipelineSteps {

    CONFIG("configuration"),
    LAUNCH("launched"),
    TRANSCRIPTION_FACTOR("transcription_factor"),
    EXPERT_OBJECTS("expert_objects"),
    SEARCH_LEVELS("search_levels");

    private final String value;

    PipelineSteps(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
