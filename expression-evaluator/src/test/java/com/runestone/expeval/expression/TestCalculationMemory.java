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
import com.runestone.expeval.expression.supplier.DefaultExpressionSupplier;
import com.runestone.expeval.tools.VerifyExpressionsTools;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestCalculationMemory {

    @Test
    public void testWithDefaultParameters() {
        Calculator calculator = new Calculator();
        List<CalculatorInput> calculatorInputs = List.of(
                new CalculatorInput("""
                        a := 1 + 2;
                        b := 2 * 2;"""),
                new CalculatorInput("c := a ^ 3;"),
                new CalculatorInput("""
                        d := b ^ 2;
                        a + b + c + d""")
        );

        List<CalculationMemory> memoryList = calculator.calculate(calculatorInputs);
        Assertions.assertThat(memoryList).hasSize(3);
        CalculationMemory calculationMemory01 = memoryList.get(0);
        Assertions.assertThat(calculationMemory01.inputData()).isEqualTo(calculatorInputs.get(0));
        Assertions.assertThat((Boolean) calculationMemory01.result()).isFalse();
        Assertions.assertThat(calculationMemory01.variables()).isEmpty();
        Assertions.assertThat(calculationMemory01.assignedVariables()).containsExactlyInAnyOrderEntriesOf(
                Map.of("a", BigDecimal.valueOf(3), "b", BigDecimal.valueOf(4))
        );
        Assertions.assertThat(calculationMemory01.contextVariables()).isEmpty();
        VerifyExpressionsTools.commonVerifications(calculationMemory01.expression());
        Assertions.assertThat(calculationMemory01.findNumericVariable("a")).hasValue(BigDecimal.valueOf(3));
        Assertions.assertThat(calculationMemory01.findNumericVariable("b")).hasValue(BigDecimal.valueOf(4));
        Assertions.assertThat(calculationMemory01.findNumericVariable("c")).isEmpty();
        Assertions.assertThat(calculationMemory01.findNumericVariable("d")).isEmpty();

        CalculationMemory calculationMemory02 = memoryList.get(1);
        Assertions.assertThat(calculationMemory02.inputData()).isEqualTo(calculatorInputs.get(1));
        Assertions.assertThat((Boolean) calculationMemory02.result()).isFalse();
        Assertions.assertThat(calculationMemory02.variables()).containsExactlyInAnyOrderEntriesOf(
                Map.of("a", BigDecimal.valueOf(3))
        );
        Assertions.assertThat(calculationMemory02.assignedVariables()).containsExactlyInAnyOrderEntriesOf(
                Map.of("c", BigDecimal.valueOf(27))
        );
        Assertions.assertThat(calculationMemory02.contextVariables()).containsExactlyInAnyOrderEntriesOf(
                Map.of("a", BigDecimal.valueOf(3), "b", BigDecimal.valueOf(4))
        );
        VerifyExpressionsTools.commonVerifications(calculationMemory02.expression());
        Assertions.assertThat(calculationMemory02.findNumericVariable("a")).hasValue(BigDecimal.valueOf(3));
        Assertions.assertThat(calculationMemory02.findNumericVariable("b")).hasValue(BigDecimal.valueOf(4));
        Assertions.assertThat(calculationMemory02.findNumericVariable("c")).hasValue(BigDecimal.valueOf(27));
        Assertions.assertThat(calculationMemory02.findNumericVariable("d")).isEmpty();

        CalculationMemory calculationMemory03 = memoryList.get(2);
        Assertions.assertThat(calculationMemory03.inputData()).isEqualTo(calculatorInputs.get(2));
        Assertions.assertThat((BigDecimal) calculationMemory03.result()).isEqualByComparingTo(BigDecimal.valueOf(50));
        Assertions.assertThat(calculationMemory03.variables()).containsExactlyInAnyOrderEntriesOf(
                Map.of("a", BigDecimal.valueOf(3), "b", BigDecimal.valueOf(4), "c", BigDecimal.valueOf(27))
        );
        Assertions.assertThat(calculationMemory03.assignedVariables()).containsExactlyInAnyOrderEntriesOf(
                Map.of("d", BigDecimal.valueOf(16))
        );
        Assertions.assertThat(calculationMemory03.contextVariables()).containsExactlyInAnyOrderEntriesOf(
                Map.of("a", BigDecimal.valueOf(3), "b", BigDecimal.valueOf(4), "c", BigDecimal.valueOf(27))
        );
        VerifyExpressionsTools.commonVerifications(calculationMemory03.expression());
        Assertions.assertThat(calculationMemory03.findNumericVariable("a")).hasValue(BigDecimal.valueOf(3));
        Assertions.assertThat(calculationMemory03.findNumericVariable("b")).hasValue(BigDecimal.valueOf(4));
        Assertions.assertThat(calculationMemory03.findNumericVariable("c")).hasValue(BigDecimal.valueOf(27));
        Assertions.assertThat(calculationMemory03.findNumericVariable("d")).hasValue(BigDecimal.valueOf(16));
    }

    @Test
    public void testWithDefaultContextVariables() {
        Calculator calculator = new Calculator(new DefaultExpressionSupplier(ExpressionOptions.defaultOptions(), new ExpressionContext()));
        List<CalculatorInput> calculatorInputs = List.of(
                new CalculatorInput("c := a and !b;"),
                new CalculatorInput("x := y xor false; x or c")
        );
        Map<String, Object> contextVariables = Map.of(
                "a", true,
                "b", false,
                "y", true
        );

        List<CalculationMemory> memoryList = calculator.calculate(calculatorInputs, contextVariables);
        Assertions.assertThat(memoryList).hasSize(2);

        CalculationMemory calculationMemory01 = memoryList.get(0);
        Assertions.assertThat(calculationMemory01.inputData()).isEqualTo(calculatorInputs.get(0));
        Assertions.assertThat((Boolean) calculationMemory01.result()).isFalse();
        Assertions.assertThat(calculationMemory01.variables()).containsExactlyInAnyOrderEntriesOf(
                Map.of("a", true, "b", false)
        );
        Assertions.assertThat(calculationMemory01.assignedVariables()).containsExactlyInAnyOrderEntriesOf(
                Map.of("c", true)
        );
        Assertions.assertThat(calculationMemory01.contextVariables()).containsExactlyInAnyOrderEntriesOf(
                Map.of("a", true, "b", false, "y", true)
        );
        VerifyExpressionsTools.commonVerifications(calculationMemory01.expression());
        Assertions.assertThat(calculationMemory01.findBooleanVariable("a")).hasValue(true);
        Assertions.assertThat(calculationMemory01.findBooleanVariable("b")).hasValue(false);
        Assertions.assertThat(calculationMemory01.findBooleanVariable("c")).hasValue(true);
        Assertions.assertThat(calculationMemory01.findBooleanVariable("y")).hasValue(true);
        Assertions.assertThat(calculationMemory01.findBooleanVariable("x")).isEmpty();

        CalculationMemory calculationMemory02 = memoryList.get(1);
        Assertions.assertThat(calculationMemory02.inputData()).isEqualTo(calculatorInputs.get(1));
        Assertions.assertThat((Boolean) calculationMemory02.result()).isTrue();

        HashMap<String, Object> a = new HashMap<>();
        a.put("c", null); // correct because variable c is not evaluated on second expression as 'x' is true on 'x or c'
        a.put("y", true);
        Assertions.assertThat(calculationMemory02.variables()).containsExactlyInAnyOrderEntriesOf(a);
        Assertions.assertThat(calculationMemory02.assignedVariables()).containsExactlyInAnyOrderEntriesOf(
                Map.of("x", true)
        );
        Assertions.assertThat(calculationMemory02.contextVariables()).containsExactlyInAnyOrderEntriesOf(
                Map.of("a", true, "b", false, "y", true, "c", true)
        );
        VerifyExpressionsTools.commonVerifications(calculationMemory02.expression());
        Assertions.assertThat(calculationMemory02.findBooleanVariable("a")).hasValue(true);
        Assertions.assertThat(calculationMemory02.findBooleanVariable("b")).hasValue(false);
        Assertions.assertThat(calculationMemory02.findBooleanVariable("c")).hasValue(true);
        Assertions.assertThat(calculationMemory02.findBooleanVariable("y")).hasValue(true);
        Assertions.assertThat(calculationMemory02.findBooleanVariable("x")).hasValue(true);
    }

    @Test
    public void testWithExpressionContext() {
        ExpressionContext fixedContext = new ExpressionContext();
        fixedContext.putDictionaryEntry("z", 2);
        Calculator calculator = new Calculator(new DefaultExpressionSupplier(ExpressionOptions.defaultOptions(), fixedContext));
        List<CalculatorInput> calculatorInputs = List.of(
                new CalculatorInput("""
                        a := 1 + z;
                        b := 2 * 2;"""),
                new CalculatorInput("""
                        c := a ^ 3;
                        c * x""")
        );
        ExpressionContext expressionContext = new ExpressionContext();
        expressionContext.putDictionaryEntry("x", 2);
        List<CalculationMemory> memoryList = calculator.calculate(calculatorInputs, expressionContext);

        Assertions.assertThat(memoryList).hasSize(2);
        CalculationMemory calculationMemory01 = memoryList.get(0);
        Assertions.assertThat(calculationMemory01.inputData()).isEqualTo(calculatorInputs.get(0));
        Assertions.assertThat((Boolean) calculationMemory01.result()).isFalse();
        Assertions.assertThat(calculationMemory01.variables()).containsExactlyInAnyOrderEntriesOf(
                Map.of("z", BigDecimal.valueOf(2))
        );
        Assertions.assertThat(calculationMemory01.assignedVariables()).containsExactlyInAnyOrderEntriesOf(
                Map.of("a", BigDecimal.valueOf(3), "b", BigDecimal.valueOf(4))
        );
        Assertions.assertThat(calculationMemory01.contextVariables()).isEmpty();
        VerifyExpressionsTools.commonVerifications(calculationMemory01.expression());
        Assertions.assertThat(calculationMemory01.findNumericVariable("a")).hasValue(BigDecimal.valueOf(3));
        Assertions.assertThat(calculationMemory01.findNumericVariable("b")).hasValue(BigDecimal.valueOf(4));
        Assertions.assertThat(calculationMemory01.findNumericVariable("c")).isEmpty();

        CalculationMemory calculationMemory02 = memoryList.get(1);
        Assertions.assertThat(calculationMemory02.inputData()).isEqualTo(calculatorInputs.get(1));
        Assertions.assertThat((BigDecimal) calculationMemory02.result()).isEqualByComparingTo("54");
        Assertions.assertThat(calculationMemory02.variables()).containsExactlyInAnyOrderEntriesOf(
                Map.of(
                        "a", BigDecimal.valueOf(3),
                        "x", BigDecimal.valueOf(2)
                ));
        Assertions.assertThat(calculationMemory02.assignedVariables()).containsExactlyInAnyOrderEntriesOf(
                Map.of("c", BigDecimal.valueOf(27))
        );
        Assertions.assertThat(calculationMemory02.contextVariables()).containsExactlyInAnyOrderEntriesOf(
                Map.of("a", BigDecimal.valueOf(3), "b", BigDecimal.valueOf(4))
        );
        VerifyExpressionsTools.commonVerifications(calculationMemory02.expression());
        Assertions.assertThat(calculationMemory02.findNumericVariable("a")).hasValue(BigDecimal.valueOf(3));
        Assertions.assertThat(calculationMemory02.findNumericVariable("b")).hasValue(BigDecimal.valueOf(4));
        Assertions.assertThat(calculationMemory02.findNumericVariable("c")).hasValue(BigDecimal.valueOf(27));
        Assertions.assertThat(calculationMemory02.findNumericVariable("x")).hasValue(BigDecimal.valueOf(2));
    }

}
