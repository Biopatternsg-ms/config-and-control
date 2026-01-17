package com.biopatternsg.domain.enums;

public enum Strand {
    POSITIVE("+"),
    NEGATIVE("-");

    private final String value;

    Strand(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
