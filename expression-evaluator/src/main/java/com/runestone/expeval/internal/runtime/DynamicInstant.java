package com.runestone.expeval.internal.runtime;

enum DynamicInstant {
    CURR_DATE("currDate"),
    CURR_TIME("currTime"),
    CURR_DATETIME("currDateTime");

    private final String canonicalName;

    DynamicInstant(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    /** Returns the identifier name as written in expression syntax (e.g. {@code "currDate"}). */
    String canonicalName() {
        return canonicalName;
    }
}
