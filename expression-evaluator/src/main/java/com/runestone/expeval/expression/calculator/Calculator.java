package com.runestone.expeval.expression.calculator;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval.expression.ExpressionContext;
import com.runestone.expeval.expression.ExpressionOptions;
import com.runestone.expeval.expression.supplier.DefaultExpressionSupplier;
import com.runestone.expeval.expression.supplier.ExpressionSupplier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A calculator that can evaluate multiple expressions sequentially, reusing the results of the previous expression on the next one, and
 * return a detailed calculation memory for the whole process.
 *
 * @author Marcelo Portilho
 */
public class Calculator {

    private final ExpressionSupplier expressionSupplier;

    /**
     * Creates a calculator with default expression options and an empty expression context
     */
    public Calculator() {
        this(new DefaultExpressionSupplier(ExpressionOptions.defaultOptions(), new ExpressionContext()));
    }

    /**
     * Creates a calculator with default expression options and the provided expression context
     *
     * @param expressionSupplier the expression supplier to be used
     */
    public Calculator(ExpressionSupplier expressionSupplier) {
        this.expressionSupplier = expressionSupplier;
    }

    /**
     * Calculates the results of the provided expressions
     *
     * @param calculatorInputs the expressions to be calculated
     * @return a list of calculation memories for each expression
     */
    public List<CalculationMemory> calculate(List<CalculatorInput> calculatorInputs) {
        return calculate(calculatorInputs, null, null);
    }

    /**
     * Calculates the results of the provided expressions
     *
     * @param calculatorInputs the expressions to be calculated
     * @param contextVariables the variables to be used on the calculation
     * @return a list of calculation memories for each expression
     */
    public List<CalculationMemory> calculate(List<CalculatorInput> calculatorInputs, Map<String, Object> contextVariables) {
        return calculate(calculatorInputs, contextVariables, null);
    }

    /**
     * Calculates the results of the provided expressions
     *
     * @param calculatorInputs  the expressions to be calculated
     * @param expressionContext the expression context to be used on the calculation
     * @return a list of calculation memories for each expression
     */
    public List<CalculationMemory> calculate(List<CalculatorInput> calculatorInputs, ExpressionContext expressionContext) {
        return calculate(calculatorInputs, null, expressionContext);
    }

    /**
     * Calculates the results of the provided expressions
     *
     * @param calculatorInputs  the expressions to be calculated
     * @param contextVariables  the variables to be used on the calculation
     * @param expressionContext the expression context to be used on the calculation
     * @return a list of calculation memories for each expression
     */
    public List<CalculationMemory> calculate(List<CalculatorInput> calculatorInputs, Map<String, Object> contextVariables, ExpressionContext expressionContext) {
        List<CalculationMemory> memoryList = new ArrayList<>();
        Map<String, Object> computationVariables = contextVariables != null ? new HashMap<>(contextVariables) : new HashMap<>();

        for (CalculatorInput input : calculatorInputs) {
            Expression expression = expressionSupplier.createExpression(input.expression());
            expression.setVariables(computationVariables);
            Object result = expression.evaluate(expressionContext);
            var memory = new CalculationMemory(input, expression, result, expression.getVariables(), expression.getAssignedVariables(), new HashMap<>(computationVariables));
            computationVariables.putAll(memory.assignedVariables());
            memoryList.add(memory);
        }
        return memoryList;
    }

}
