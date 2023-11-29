package com.runestone.expeval.expression.calculator;

/**
 * Represents the input of a calculation. It contains the expression to be evaluated, and it's id as a generic object.
 *
 * @param id         the id of the expression
 * @param expression the expression to be evaluated
 * @author Marcelo Portilho
 */
public record CalculatorInput(Object id, String expression) {

    public CalculatorInput(String expression) {
        this(null, expression);
    }
}
