package com.runestone.expeval_mk3.api;

import java.util.Set;

/**
 * Official Funcao Embutida groups that make up the standard Ambiente de Expressao.
 */
public enum StandardFunctionGroup {

    MATH,
    TRANSCENDENTAL,
    STRING,
    DATE_TIME,
    COMPARABLE,
    FINANCIAL,
    ASSERTION;

    public Set<String> requiredFunctionNames() {
        return StandardBuiltIns.requiredFunctionNames(this);
    }
}
