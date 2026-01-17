package com.biopatternsg.domain.enums;

public enum TranscriptionFactorSource {
    TFBIND("TFBIND"),
    JASPAR("JASPAR");

    private final String value;

    TranscriptionFactorSource(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
