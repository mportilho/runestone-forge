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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Objects;

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
 *
 * // Optional memory strategy
 * CalculatorOptions options = new CalculatorOptions(CalculatorMemoryMode.LAZY, 64);
 * List<CalculationMemory> lazyMemory = calculator.calculate(calculatorInputs, contextVariables, options);
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
        return calculate(calculatorInputs, null, null, CalculatorOptions.defaultOptions());
    }

    /**
     * Calculates the results of the provided expressions
     *
     * @param calculatorInputs the expressions to be calculated
     * @param contextVariables the variables to be used on the calculation
     * @return a list of calculation memories for each expression
     */
    public List<CalculationMemory> calculate(List<CalculatorInput> calculatorInputs, Map<String, Object> contextVariables) {
        return calculate(calculatorInputs, contextVariables, null, CalculatorOptions.defaultOptions());
    }

    /**
     * Calculates the results of the provided expressions
     *
     * @param calculatorInputs  the expressions to be calculated
     * @param expressionContext the expression context to be used on the calculation
     * @return a list of calculation memories for each expression
     */
    public List<CalculationMemory> calculate(List<CalculatorInput> calculatorInputs, ExpressionContext expressionContext) {
        return calculate(calculatorInputs, null, expressionContext, CalculatorOptions.defaultOptions());
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
        return calculate(calculatorInputs, contextVariables, expressionContext, CalculatorOptions.defaultOptions());
    }

    public List<CalculationMemory> calculate(List<CalculatorInput> calculatorInputs, CalculatorOptions options) {
        return calculate(calculatorInputs, null, null, options);
    }

    public List<CalculationMemory> calculate(List<CalculatorInput> calculatorInputs, Map<String, Object> contextVariables, CalculatorOptions options) {
        return calculate(calculatorInputs, contextVariables, null, options);
    }

    public List<CalculationMemory> calculate(List<CalculatorInput> calculatorInputs, ExpressionContext expressionContext, CalculatorOptions options) {
        return calculate(calculatorInputs, null, expressionContext, options);
    }

    /**
     * Calculates the results of the provided expressions with explicit calculator options.
     *
     * @param calculatorInputs  the expressions to be calculated
     * @param contextVariables  the variables to be used on the calculation
     * @param expressionContext high precedence expression context to be used on the calculation
     * @param options           calculator options (memory mode and lazy checkpoint interval)
     * @return a list of calculation memories for each expression
     */
    public List<CalculationMemory> calculate(
            List<CalculatorInput> calculatorInputs,
            Map<String, Object> contextVariables,
            ExpressionContext expressionContext,
            CalculatorOptions options) {

        Objects.requireNonNull(calculatorInputs, "Calculator inputs cannot be null");
        CalculatorOptions calculationOptions = options == null ? CalculatorOptions.defaultOptions() : options;
        CalculatorMemoryMode memoryMode = calculationOptions.memoryMode();

        List<CalculationMemory> memoryList = new ArrayList<>(calculatorInputs.size());
        Map<String, Object> computationVariables = contextVariables != null ? new HashMap<>(contextVariables) : new HashMap<>();
        FullContextSnapshot fullContextSnapshot = memoryMode == CalculatorMemoryMode.FULL
                ? new FullContextSnapshot(computationVariables)
                : null;
        LazyContextHistory lazyContextHistory = memoryMode == CalculatorMemoryMode.LAZY
                ? new LazyContextHistory(computationVariables, calculationOptions.checkpointInterval())
                : null;

        int step = 0;
        for (CalculatorInput input : calculatorInputs) {
            step++;
            Map<String, Object> contextSnapshotBeforeStep = fullContextSnapshot != null ? fullContextSnapshot.current() : null;
            Expression expression = expressionSupplier.createExpression(input.expression());
            expression.setVariables(computationVariables);
            Object result = expression.evaluate(expressionContext);
            Map<String, Object> assignedVariables = expression.getAssignedVariables();
            CalculationMemory memory = createMemory(memoryMode, input, expression, result, assignedVariables,
                    contextSnapshotBeforeStep, lazyContextHistory, step);
            computationVariables.putAll(assignedVariables);
            if (fullContextSnapshot != null) {
                fullContextSnapshot.advance(assignedVariables);
            }
            if (lazyContextHistory != null) {
                lazyContextHistory.registerStep(step, assignedVariables, computationVariables);
            }
            memoryList.add(memory);
        }
        return memoryList;
    }

    private static CalculationMemory createMemory(
            CalculatorMemoryMode memoryMode,
            CalculatorInput input,
            Expression expression,
            Object result,
            Map<String, Object> assignedVariables,
            Map<String, Object> fullContextSnapshotBeforeStep,
            LazyContextHistory lazyContextHistory,
            int step) {

        return switch (memoryMode) {
            case FULL -> new CalculationMemory(input, expression, result, expression.getVariables(),
                    assignedVariables, fullContextSnapshotBeforeStep == null ? Map.of() : fullContextSnapshotBeforeStep);
            case COMPACT -> CalculationMemory.compact(input, expression, result, assignedVariables);
            case LAZY -> {
                Objects.requireNonNull(lazyContextHistory, "Lazy context history cannot be null");
                yield CalculationMemory.lazy(
                        input,
                        expression,
                        result,
                        assignedVariables,
                        expression::getVariables,
                        () -> lazyContextHistory.snapshotBefore(step));
            }
        };
    }

    private static final class FullContextSnapshot {

        private Map<String, Object> current;

        private FullContextSnapshot(Map<String, Object> initialState) {
            this.current = toUnmodifiableCopy(initialState);
        }

        private Map<String, Object> current() {
            return current;
        }

        private void advance(Map<String, Object> assignedVariables) {
            if (assignedVariables.isEmpty() || !hasEffectiveChanges(assignedVariables)) {
                return;
            }
            Map<String, Object> next = new HashMap<>(current);
            next.putAll(assignedVariables);
            current = Collections.unmodifiableMap(next);
        }

        private boolean hasEffectiveChanges(Map<String, Object> assignedVariables) {
            for (Entry<String, Object> entry : assignedVariables.entrySet()) {
                String key = entry.getKey();
                Object updatedValue = entry.getValue();
                Object currentValue = current.get(key);
                if (!Objects.equals(currentValue, updatedValue) || (currentValue == null && !current.containsKey(key))) {
                    return true;
                }
            }
            return false;
        }

        private static Map<String, Object> toUnmodifiableCopy(Map<String, Object> source) {
            if (source.isEmpty()) {
                return Map.of();
            }
            return Collections.unmodifiableMap(new HashMap<>(source));
        }
    }

    private static final class LazyContextHistory {

        private final int checkpointInterval;
        private final Map<Integer, Map<String, Object>> snapshotsAfterStep;
        private final List<Map<String, Object>> deltasByStep;

        private LazyContextHistory(Map<String, Object> initialState, int checkpointInterval) {
            this.checkpointInterval = checkpointInterval;
            this.snapshotsAfterStep = new HashMap<>();
            this.deltasByStep = new ArrayList<>();
            this.snapshotsAfterStep.put(0, initialState.isEmpty() ? Map.of() : new HashMap<>(initialState));
        }

        private void registerStep(int step, Map<String, Object> assignedVariables, Map<String, Object> currentState) {
            deltasByStep.add(assignedVariables.isEmpty() ? Map.of() : assignedVariables);
            if (step % checkpointInterval == 0) {
                snapshotsAfterStep.put(step, currentState.isEmpty() ? Map.of() : new HashMap<>(currentState));
            }
        }

        private Map<String, Object> snapshotBefore(int currentStep) {
            if (currentStep <= 1) {
                return copySnapshot(snapshotsAfterStep.get(0));
            }

            int stepBeforeCurrent = currentStep - 1;
            int baseCheckpoint = (stepBeforeCurrent / checkpointInterval) * checkpointInterval;
            Map<String, Object> baseSnapshot = snapshotsAfterStep.get(baseCheckpoint);
            if (baseSnapshot == null) {
                throw new IllegalStateException("No context checkpoint found for step " + baseCheckpoint);
            }

            Map<String, Object> snapshot = copySnapshot(baseSnapshot);
            for (int deltaIndex = baseCheckpoint; deltaIndex < stepBeforeCurrent; deltaIndex++) {
                snapshot.putAll(deltasByStep.get(deltaIndex));
            }
            return snapshot;
        }

        private static Map<String, Object> copySnapshot(Map<String, Object> source) {
            if (source == null || source.isEmpty()) {
                return new HashMap<>();
            }
            return new HashMap<>(source);
        }
    }

}
