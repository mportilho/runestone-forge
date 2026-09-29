package com.runestone.expeval.internal.ast;

enum ConditionalSeparator {
    COMMA(","),
    SEMICOLON(";");

    private final String text;

    ConditionalSeparator(String text) {
        this.text = text;
    }

    String text() {
        return text;
    }
}
