package com.runestone.expeval_mk3.internal.ast;

enum UnaryOperator {
    NEGATE("-"),
    LOGICAL_NOT("!");

    private final String canonicalSymbol;

    UnaryOperator(String canonicalSymbol) {
        this.canonicalSymbol = canonicalSymbol;
    }

    String canonicalSymbol() {
        return canonicalSymbol;
    }
}
