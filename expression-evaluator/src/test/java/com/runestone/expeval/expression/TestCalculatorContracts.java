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

package com.runestone.expeval.expression;

import com.runestone.expeval.expression.calculator.CalculationMemory;
import com.runestone.expeval.expression.calculator.Calculator;
import com.runestone.expeval.expression.calculator.CalculatorInput;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestCalculatorContracts {

    @Test
    public void testCalculateWithNullInputListThrowsNullPointerException() {
        Calculator calculator = new Calculator();
        Assertions.assertThatNullPointerException()
                .isThrownBy(() -> calculator.calculate((List<CalculatorInput>) null));
    }

    @Test
    public void testCalculateWithEmptyInputListReturnsEmptyMemory() {
        Calculator calculator = new Calculator();
        List<CalculationMemory> memoryList = calculator.calculate(List.of());
        Assertions.assertThat(memoryList).isEmpty();
    }

    @Test
    public void testCalculateWithNullInputEntryThrowsNullPointerException() {
        Calculator calculator = new Calculator();
        List<CalculatorInput> inputs = new ArrayList<>();
        inputs.add(new CalculatorInput("a := 1 + 2;"));
        inputs.add(null);

        Assertions.assertThatNullPointerException()
                .isThrownBy(() -> calculator.calculate(inputs));
    }

    @Test
    public void testCalculateDoesNotMutateContextVariableMap() {
        Calculator calculator = new Calculator();
        List<CalculatorInput> inputs = List.of(new CalculatorInput("c := a + b;"));
        Map<String, Object> contextVariables = new HashMap<>();
        contextVariables.put("a", 1);
        contextVariables.put("b", 2);

        Map<String, Object> snapshot = new HashMap<>(contextVariables);
        List<CalculationMemory> memoryList = calculator.calculate(inputs, contextVariables);

        Assertions.assertThat(contextVariables).containsExactlyInAnyOrderEntriesOf(snapshot);
        Assertions.assertThat(memoryList.getFirst().assignedVariables())
                .containsEntry("c", BigDecimal.valueOf(3));
    }

    @Test
    public void testCalculationMemoryMapsAreUnmodifiable() {
        Calculator calculator = new Calculator();
        List<CalculatorInput> inputs = List.of(new CalculatorInput("x := 10;"));
        List<CalculationMemory> memoryList = calculator.calculate(inputs, Map.of("a", 1));
        CalculationMemory memory = memoryList.getFirst();

        Assertions.assertThatThrownBy(() -> memory.variables().put("b", BigDecimal.TEN))
                .isInstanceOf(UnsupportedOperationException.class);
        Assertions.assertThatThrownBy(() -> memory.assignedVariables().put("y", BigDecimal.ONE))
                .isInstanceOf(UnsupportedOperationException.class);
        Assertions.assertThatThrownBy(() -> memory.contextVariables().put("z", BigDecimal.ZERO))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void testFindNumericVariablePrecedenceAndTypeFallback() {
        CalculationMemory memory = new CalculationMemory(
                new CalculatorInput("1 = 1"),
                new Expression("1 = 1"),
                true,
                new HashMap<>(Map.of("same", BigDecimal.valueOf(2), "fallback", BigDecimal.valueOf(7), "wrong", true)),
                new HashMap<>(Map.of("same", BigDecimal.valueOf(1), "fallback", "not-a-number", "wrong", false)),
                new HashMap<>(Map.of("same", BigDecimal.valueOf(3), "fallback", BigDecimal.valueOf(9), "wrong", "invalid"))
        );

        Assertions.assertThat(memory.findNumericVariable("same")).hasValue(BigDecimal.valueOf(1));
        Assertions.assertThat(memory.findNumericVariable("fallback")).hasValue(BigDecimal.valueOf(7));
        Assertions.assertThat(memory.findNumericVariable("wrong")).isEmpty();
    }

    @Test
    public void testFindBooleanVariablePrecedenceAndTypeFallback() {
        CalculationMemory memory = new CalculationMemory(
                new CalculatorInput("true = true"),
                new Expression("true = true"),
                true,
                new HashMap<>(Map.of("same", false, "fallback", true, "wrong", BigDecimal.ONE)),
                new HashMap<>(Map.of("same", true, "fallback", BigDecimal.TEN, "wrong", BigDecimal.ZERO)),
                new HashMap<>(Map.of("same", false, "fallback", false, "wrong", "invalid"))
        );

        Assertions.assertThat(memory.findBooleanVariable("same")).hasValue(true);
        Assertions.assertThat(memory.findBooleanVariable("fallback")).hasValue(true);
        Assertions.assertThat(memory.findBooleanVariable("wrong")).isEmpty();
    }

}
