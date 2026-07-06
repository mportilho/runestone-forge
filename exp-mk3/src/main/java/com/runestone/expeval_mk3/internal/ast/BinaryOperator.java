package com.runestone.expeval_mk3.internal.ast;

enum BinaryOperator {
    LOGICAL_OR("or"),
    LOGICAL_AND("and"),
    LOGICAL_NAND("nand"),
    LOGICAL_NOR("nor"),
    LOGICAL_XOR("xor"),
    LOGICAL_XNOR("xnor"),
    CONCAT("||"),
    ADD("+"),
    SUBTRACT("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    MODULO("mod"),
    ROOT("root"),
    EXPONENTIATE("^"),
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUAL(">="),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL("<="),
    EQUAL("="),
    NOT_EQUAL("<>"),
    REGEX_MATCH("=~"),
    REGEX_NOT_MATCH("!~");

    private final String canonicalSymbol;

    BinaryOperator(String canonicalSymbol) {
        this.canonicalSymbol = canonicalSymbol;
    }

    String canonicalSymbol() {
        return canonicalSymbol;
    }
}
