package com.runestone.expeval_mk3.internal.ast;

enum PostfixOperator {
    PERCENT("%"),
    FACTORIAL("!");

    private final String canonicalSymbol;

    PostfixOperator(String canonicalSymbol) {
        this.canonicalSymbol = canonicalSymbol;
    }

    String canonicalSymbol() {
        return canonicalSymbol;
    }
}
