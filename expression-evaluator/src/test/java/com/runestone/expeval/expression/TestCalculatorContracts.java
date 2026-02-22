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
import com.runestone.expeval.expression.calculator.CalculatorMemoryMode;
import com.runestone.expeval.expression.calculator.CalculatorOptions;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
    public void testCalculatorOptionsValidation() {
        Assertions.assertThatNullPointerException()
                .isThrownBy(() -> new CalculatorOptions(null, 64));
        Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> new CalculatorOptions(CalculatorMemoryMode.LAZY, 0));
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
    public void testCalculateDoesNotMutateContextVariableMapOnAllMemoryModes() {
        Calculator calculator = new Calculator();
        List<CalculatorInput> inputs = List.of(new CalculatorInput("c := a + b;"));
        Map<String, Object> contextVariables = new HashMap<>();
        contextVariables.put("a", 1);
        contextVariables.put("b", 2);
        Map<String, Object> snapshot = new HashMap<>(contextVariables);

        for (CalculatorMemoryMode mode : CalculatorMemoryMode.values()) {
            calculator.calculate(inputs, contextVariables, new CalculatorOptions(mode, 2));
            Assertions.assertThat(contextVariables).containsExactlyInAnyOrderEntriesOf(snapshot);
        }
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
    public void testDefaultModeMatchesExplicitFullMode() {
        Calculator calculator = new Calculator();
        List<CalculatorInput> inputs = List.of(
                new CalculatorInput("c := a + b;"),
                new CalculatorInput("d := c + 1; d")
        );
        Map<String, Object> context = Map.of("a", 1, "b", 2);

        List<CalculationMemory> defaultMemory = calculator.calculate(inputs, context);
        List<CalculationMemory> fullMemory = calculator.calculate(inputs, context, new CalculatorOptions(CalculatorMemoryMode.FULL, 8));

        Assertions.assertThat(defaultMemory).hasSize(fullMemory.size());
        for (int i = 0; i < defaultMemory.size(); i++) {
            CalculationMemory defaultEntry = defaultMemory.get(i);
            CalculationMemory fullEntry = fullMemory.get(i);
            Assertions.assertThat(defaultEntry.result()).isEqualTo(fullEntry.result());
            Assertions.assertThat(defaultEntry.assignedVariables()).containsExactlyInAnyOrderEntriesOf(fullEntry.assignedVariables());
            Assertions.assertThat(defaultEntry.variables()).containsExactlyInAnyOrderEntriesOf(fullEntry.variables());
            Assertions.assertThat(defaultEntry.contextVariables()).containsExactlyInAnyOrderEntriesOf(fullEntry.contextVariables());
        }
    }

    @Test
    public void testFullModeContextSnapshotRemainsStableAcrossStepsWithoutAssignments() {
        Calculator calculator = new Calculator();
        List<CalculatorInput> inputs = List.of(
                new CalculatorInput("a + 1"),
                new CalculatorInput("a + 2"),
                new CalculatorInput("a + 3")
        );
        Map<String, Object> context = Map.of("a", 10, "unused", 99);

        List<CalculationMemory> memoryList = calculator.calculate(inputs, context, new CalculatorOptions(CalculatorMemoryMode.FULL, 8));

        Assertions.assertThat(memoryList).hasSize(3);
        for (CalculationMemory memory : memoryList) {
            Assertions.assertThat(memory.contextVariables()).containsExactlyInAnyOrderEntriesOf(context);
            Assertions.assertThat(memory.assignedVariables()).isEmpty();
        }
    }

    @Test
    public void testFullModeContextSnapshotAdvancesOnlyAfterAssignmentSteps() {
        Calculator calculator = new Calculator();
        List<CalculatorInput> inputs = List.of(
                new CalculatorInput("x := a + 1;"),
                new CalculatorInput("a + x"),
                new CalculatorInput("y := x + 1;"),
                new CalculatorInput("a + y")
        );
        Map<String, Object> context = Map.of("a", 1);

        List<CalculationMemory> memoryList = calculator.calculate(inputs, context, new CalculatorOptions(CalculatorMemoryMode.FULL, 8));

        Assertions.assertThat(memoryList).hasSize(4);
        Assertions.assertThat(memoryList.get(0).contextVariables()).containsExactlyInAnyOrderEntriesOf(Map.of("a", 1));
        Assertions.assertThat(memoryList.get(1).contextVariables()).containsExactlyInAnyOrderEntriesOf(
                Map.of("a", 1, "x", BigDecimal.valueOf(2)));
        Assertions.assertThat(memoryList.get(2).contextVariables()).containsExactlyInAnyOrderEntriesOf(
                Map.of("a", 1, "x", BigDecimal.valueOf(2)));
        Assertions.assertThat(memoryList.get(3).contextVariables()).containsExactlyInAnyOrderEntriesOf(
                Map.of("a", 1, "x", BigDecimal.valueOf(2), "y", BigDecimal.valueOf(3)));
    }

    @Test
    public void testCompactModeKeepsOnlyAssignedVariables() {
        Calculator calculator = new Calculator();
        List<CalculatorInput> inputs = List.of(
                new CalculatorInput("c := a + b;"),
                new CalculatorInput("d := c + 1; d")
        );
        Map<String, Object> context = Map.of("a", 1, "b", 2, "unused", 999);

        List<CalculationMemory> memoryList = calculator.calculate(inputs, context, new CalculatorOptions(CalculatorMemoryMode.COMPACT, 8));

        Assertions.assertThat(memoryList).hasSize(2);
        Assertions.assertThat(memoryList.get(0).variables()).isEmpty();
        Assertions.assertThat(memoryList.get(0).contextVariables()).isEmpty();
        Assertions.assertThat(memoryList.get(0).assignedVariables()).containsEntry("c", BigDecimal.valueOf(3));
        Assertions.assertThat(memoryList.get(0).findNumericVariable("c")).hasValue(BigDecimal.valueOf(3));

        Assertions.assertThat(memoryList.get(1).variables()).isEmpty();
        Assertions.assertThat(memoryList.get(1).contextVariables()).isEmpty();
        Assertions.assertThat(memoryList.get(1).assignedVariables()).containsEntry("d", BigDecimal.valueOf(4));
    }

    @Test
    public void testLazyModeReconstructsContextByDeltaAndCheckpoint() {
        Calculator calculator = new Calculator();
        List<CalculatorInput> inputs = List.of(
                new CalculatorInput("a := 1;"),
                new CalculatorInput("b := a + 1;"),
                new CalculatorInput("c := b + 1; c")
        );
        Map<String, Object> context = Map.of("seed", 99);

        List<CalculationMemory> memoryList = calculator.calculate(inputs, context, new CalculatorOptions(CalculatorMemoryMode.LAZY, 2));

        Assertions.assertThat(memoryList).hasSize(3);
        Assertions.assertThat(memoryList.get(0).contextVariables()).containsExactlyInAnyOrderEntriesOf(Map.of("seed", 99));
        Assertions.assertThat(memoryList.get(1).contextVariables()).containsExactlyInAnyOrderEntriesOf(
                Map.of("seed", 99, "a", BigDecimal.ONE));
        Assertions.assertThat(memoryList.get(2).contextVariables()).containsExactlyInAnyOrderEntriesOf(
                Map.of("seed", 99, "a", BigDecimal.ONE, "b", BigDecimal.valueOf(2)));
        Assertions.assertThat(memoryList.get(2).findNumericVariable("b")).hasValue(BigDecimal.valueOf(2));
    }

    @Test
    public void testLazyCalculationMemoryMaterializesAndMemoizesOnFirstAccess() {
        AtomicInteger variablesSupplierCalls = new AtomicInteger();
        AtomicInteger contextSupplierCalls = new AtomicInteger();

        CalculationMemory memory = CalculationMemory.lazy(
                new CalculatorInput("x := 1;"),
                new Expression("x := 1;"),
                false,
                new HashMap<>(Map.of("x", BigDecimal.ONE)),
                () -> {
                    variablesSupplierCalls.incrementAndGet();
                    return new HashMap<>(Map.of("x", BigDecimal.ONE, "y", BigDecimal.TEN));
                },
                () -> {
                    contextSupplierCalls.incrementAndGet();
                    return new HashMap<>(Map.of("k", "v"));
                });

        Assertions.assertThat(variablesSupplierCalls).hasValue(0);
        Assertions.assertThat(contextSupplierCalls).hasValue(0);

        Assertions.assertThat(memory.variables().get("y")).isEqualTo(BigDecimal.TEN);
        Assertions.assertThat(memory.variables().containsKey("x")).isTrue();
        Assertions.assertThat(memory.variables().size()).isEqualTo(2);
        Assertions.assertThat(variablesSupplierCalls).hasValue(1);

        Assertions.assertThat(memory.contextVariables().get("k")).isEqualTo("v");
        Assertions.assertThat(memory.contextVariables().containsKey("k")).isTrue();
        Assertions.assertThat(memory.contextVariables().size()).isEqualTo(1);
        Assertions.assertThat(contextSupplierCalls).hasValue(1);
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
