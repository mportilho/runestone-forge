package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.execution.plan.*;

final class ConstantNodeValues {

    private ConstantNodeValues() {
    }

    static boolean isConstant(ExecutableNode node) {
        return switch (node) {
            case ExecutableLiteral ignored -> true;
            case ExecutableFunctionCall functionCall -> functionCall.isFolded();
            case ExecutableVectorLiteral vectorLiteral -> vectorLiteral.isFolded();
            default -> false;
        };
    }

    static Object value(ExecutableNode node) {
        return switch (node) {
            case ExecutableLiteral literal -> literal.precomputed();
            case ExecutableFunctionCall functionCall -> functionCall.foldedResult();
            case ExecutableVectorLiteral vectorLiteral -> vectorLiteral.foldedValue();
            default -> throw new IllegalStateException("not a constant node: " + node);
        };
    }
}
