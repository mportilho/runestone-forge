package com.runestone.expeval.expression.supplier;

import com.runestone.expeval.expression.Expression;

/**
 * A functional interface for creating expressions
 *
 * @author Marcelo Portilho
 */
@FunctionalInterface
public interface ExpressionSupplier {

    /**
     * Creates an expression from the given expression string
     *
     * @param expression the expression string
     * @return the expression object
     */
    Expression createExpression(String expression);

}
