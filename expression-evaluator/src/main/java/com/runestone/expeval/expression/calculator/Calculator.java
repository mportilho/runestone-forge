/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2023 Marcelo Silva Portilho
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
 * <p>
 * It's recommended to use a calculator for long-running processes that require multiple expressions to be evaluated sequentially, as it
 * will reuse the Expression Supplier, that contains an Expression Options and Context, avoiding the need to recreate them for each expression.
 * <p>
 * The code below shows how to use the calculator:
 *
 * <pre>{@code
 * Calculator calculator = new Calculator();
 * List<CalculatorInput> calculatorInputs = List.of(
 *         new CalculatorInput("c := a and !b;"),
 *         new CalculatorInput("x := y xor false; x or c")
 * );
 * Map<String, Object> contextVariables = Map.of("a", true, "b", false, "y", true);
 * List<CalculationMemory> memoryList = calculator.calculate(calculatorInputs, contextVariables);
 * }</pre>
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
     * @param expressionContext high precedence expression context to be used on the calculation
     * @return a list of calculation memories for each expression
     */
    public List<CalculationMemory> calculate(List<CalculatorInput> calculatorInputs, Map<String, Object> contextVariables, ExpressionContext expressionContext) {
        List<CalculationMemory> memoryList = new ArrayList<>(calculatorInputs.size());
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
