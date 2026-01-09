package com.biopatternsg.domain.enums;

public enum Genome {
    HG38("hg38"),
    HG19("hg19");

    private final String value;

    Genome(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
