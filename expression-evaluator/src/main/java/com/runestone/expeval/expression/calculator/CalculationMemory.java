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

import java.util.AbstractMap;
import java.util.Collection;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Represents the memory of a calculation. It contains the input expression, it's result and the variables used in the evaluation.
 * <p>
 * The amount of detail retained depends on {@link CalculatorMemoryMode}:
 * <ul>
 *   <li>{@link CalculatorMemoryMode#FULL}: full snapshots are provided for {@code variables} and {@code contextVariables}.</li>
 *   <li>{@link CalculatorMemoryMode#COMPACT}: only assignment deltas are retained; {@code variables} and {@code contextVariables} are empty.</li>
 *   <li>{@link CalculatorMemoryMode#LAZY}: snapshots are reconstructed on first access and cached for subsequent accesses.</li>
 * </ul>
 *
 * @param inputData         the input expression to be calculated
 * @param expression        the evaluated expression
 * @param result            the result of the expression's evaluation
 * @param variables         the variables used in the evaluation
 * @param assignedVariables the variables that were assigned during the evaluation
 * @param contextVariables  the variables that were present as context during each evaluation
 * @author Marcelo Portilho
 */
public record CalculationMemory(

        CalculatorInput inputData,
        Expression expression,
        Object result,
        Map<String, Object> variables,
        Map<String, Object> assignedVariables,
        Map<String, Object> contextVariables

) {

    public CalculationMemory {
        Objects.requireNonNull(expression, "Expression cannot be null");
        Objects.requireNonNull(variables, "Variables cannot be null");
        Objects.requireNonNull(assignedVariables, "Assigned variables cannot be null");
        Objects.requireNonNull(contextVariables, "Context variables cannot be null");
        variables = Collections.unmodifiableMap(variables);
        assignedVariables = Collections.unmodifiableMap(assignedVariables);
        contextVariables = Collections.unmodifiableMap(contextVariables);
    }

    /**
     * Finds a numeric variable in the computation memory by its name. It will first look for assigned variable list and then for variable list.
     * <p>
     * The variable must be of a BigDecimal type or an empty optional will be returned.
     *
     * @param variableName the name of the variable
     * @return the numeric value of the variable or empty if it does not exist
     */
    public Optional<BigDecimal> findNumericVariable(String variableName) {
        if (assignedVariables.get(variableName) instanceof BigDecimal number) {
            return Optional.of(number);
        } else if (variables.get(variableName) instanceof BigDecimal number) {
            return Optional.of(number);
        } else if (contextVariables.get(variableName) instanceof BigDecimal number) {
            return Optional.of(number);
        }
        return Optional.empty();
    }

    /**
     * Finds a boolean variable in the computation memory by its name. It will first look for assigned variable list and then for variable list.
     * <p>
     * The variable must be a boolean value or an empty optional will be returned.
     *
     * @param variableName the name of the variable
     * @return the boolean value of the variable or empty if it does not exist
     */
    public Optional<Boolean> findBooleanVariable(String variableName) {
        if (assignedVariables.get(variableName) instanceof Boolean aBoolean) {
            return Optional.of(aBoolean);
        } else if (variables.get(variableName) instanceof Boolean aBoolean) {
            return Optional.of(aBoolean);
        } else if (contextVariables.get(variableName) instanceof Boolean aBoolean) {
            return Optional.of(aBoolean);
        }
        return Optional.empty();
    }

    /**
     * Creates a compact memory entry preserving only assignment deltas.
     */
    public static CalculationMemory compact(CalculatorInput inputData, Expression expression, Object result, Map<String, Object> assignedVariables) {
        return new CalculationMemory(inputData, expression, result, Map.of(), assignedVariables, Map.of());
    }

    /**
     * Creates a lazy memory entry that materializes snapshots only when accessed.
     */
    public static CalculationMemory lazy(
            CalculatorInput inputData,
            Expression expression,
            Object result,
            Map<String, Object> assignedVariables,
            Supplier<Map<String, Object>> variablesSupplier,
            Supplier<Map<String, Object>> contextVariablesSupplier) {

        return new CalculationMemory(
                inputData,
                expression,
                result,
                new LazyMemoizedMap(variablesSupplier),
                assignedVariables,
                new LazyMemoizedMap(contextVariablesSupplier));
    }

    private static final class LazyMemoizedMap extends AbstractMap<String, Object> {

        private final Supplier<Map<String, Object>> supplier;
        private volatile Map<String, Object> map;

        private LazyMemoizedMap(Supplier<Map<String, Object>> supplier) {
            this.supplier = Objects.requireNonNull(supplier, "Map supplier cannot be null");
        }

        @Override
        public Object get(Object key) {
            return getMap().get(key);
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return getMap().entrySet();
        }

        @Override
        public Set<String> keySet() {
            return getMap().keySet();
        }

        @Override
        public Collection<Object> values() {
            return getMap().values();
        }

        @Override
        public int size() {
            return getMap().size();
        }

        @Override
        public boolean isEmpty() {
            return getMap().isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return getMap().containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return getMap().containsValue(value);
        }

        @Override
        public boolean equals(Object obj) {
            return getMap().equals(obj);
        }

        @Override
        public int hashCode() {
            return getMap().hashCode();
        }

        @Override
        public String toString() {
            return getMap().toString();
        }

        private Map<String, Object> getMap() {
            Map<String, Object> localMap = map;
            if (localMap == null) {
                synchronized (this) {
                    localMap = map;
                    if (localMap == null) {
                        Map<String, Object> suppliedMap = Objects.requireNonNull(supplier.get(), "Map supplier cannot return null");
                        localMap = suppliedMap.isEmpty() ? Map.of() : Collections.unmodifiableMap(new java.util.HashMap<>(suppliedMap));
                        map = localMap;
                    }
                }
            }
            return localMap;
        }
    }

}
