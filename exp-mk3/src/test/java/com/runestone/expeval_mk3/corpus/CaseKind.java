package com.runestone.expeval_mk3.corpus;

enum CaseKind {
    VALID("valid"),
    INVALID("invalid");

    private final String yamlName;

    CaseKind(String yamlName) {
        this.yamlName = yamlName;
    }

    static CaseKind from(String value) {
        for (CaseKind kind : values()) {
            if (kind.yamlName.equals(value)) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Unknown case kind: " + value);
    }
}
