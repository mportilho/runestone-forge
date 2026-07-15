package com.runestone.expeval_mk3.corpus;

enum CasePhase {
    PARSER("parser"),
    SEMANTIC("semantic"),
    RUNTIME("runtime"),
    MIGRATION("migration"),
    DIFFERENTIAL("differential");

    private final String yamlName;

    CasePhase(String yamlName) {
        this.yamlName = yamlName;
    }

    static CasePhase from(String value) {
        for (CasePhase phase : values()) {
            if (phase.yamlName.equals(value)) {
                return phase;
            }
        }
        throw new IllegalArgumentException("Unknown case phase: " + value);
    }
}
