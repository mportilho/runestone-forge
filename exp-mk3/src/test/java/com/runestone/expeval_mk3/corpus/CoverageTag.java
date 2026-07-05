package com.runestone.expeval_mk3.corpus;

enum CoverageTag {
    ASSIGNMENT("assignment"),
    DESTRUCTURING("destructuring"),
    IDENTIFIER("identifier"),
    CURRENT_TEMPORAL("current-temporal"),
    INT_LITERAL("int-literal"),
    FLOAT_LITERAL("float-literal"),
    STRING_LITERAL("string-literal"),
    BOOLEAN_LITERAL("boolean-literal"),
    DATE_LITERAL("date-literal"),
    TIME_LITERAL("time-literal"),
    DATETIME_LITERAL("datetime-literal"),
    NULL_LITERAL("null-literal"),
    VECTOR_LITERAL("vector-literal"),
    EMPTY_VECTOR("empty-vector"),
    COALESCE("coalesce"),
    LOGICAL_OR("logical-or"),
    LOGICAL_AND("logical-and"),
    COMPARISON("comparison"),
    IN("in"),
    NOT_IN("not-in"),
    BETWEEN("between"),
    REGEX("regex"),
    LOGICAL_BITWISE("logical-bitwise"),
    CONCAT("concat"),
    ADDITIVE("additive"),
    MULTIPLICATIVE("multiplicative"),
    UNARY("unary"),
    ROOT("root"),
    EXPONENTIATION("exponentiation"),
    POSTFIX_PERCENT("postfix-percent"),
    POSTFIX_FACTORIAL("postfix-factorial"),
    PARENTHESES("parentheses"),
    IF_CLASSIC("if-classic"),
    IF_FUNCTIONAL("if-functional"),
    FUNCTION_CALL("function-call"),
    PROPERTY_ACCESS("property-access"),
    METHOD_CALL("method-call"),
    SAFE_NAVIGATION("safe-navigation"),
    SUBSCRIPT_INDEX("subscript-index"),
    SUBSCRIPT_SLICE("subscript-slice"),
    SUBSCRIPT_KEY("subscript-key"),
    WILDCARD("wildcard"),
    FILTER("filter"),
    COLLECTION_FUNCTION("collection-function"),
    LAMBDA("lambda"),
    AT_SYMBOL("at-symbol"),
    COMMENTS("comments"),
    PRECEDENCE("precedence"),
    MIGRATION("migration"),
    DIAGNOSTIC("diagnostic");

    private final String yamlName;

    CoverageTag(String yamlName) {
        this.yamlName = yamlName;
    }

    static CoverageTag from(String value) {
        for (CoverageTag tag : values()) {
            if (tag.yamlName.equals(value)) {
                return tag;
            }
        }
        throw new IllegalArgumentException("Unknown coverage tag: " + value);
    }
}
