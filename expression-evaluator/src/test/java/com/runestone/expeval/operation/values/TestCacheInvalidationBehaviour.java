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

package com.runestone.expeval.operation.values;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval.operation.AbstractOperation;
import com.runestone.expeval.operation.math.AbstractSequencialMathOperation;
import com.runestone.expeval.operation.values.variable.SequenceVariableValueOperation;
import com.runestone.expeval.tools.OperationCollectorVisitor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

class TestCacheInvalidationBehaviour {

    @Test
    void testSequenceVariableAndOperationShouldNotCacheEvaluationState() {
        Expression expression = new Expression("S[[1,2,3,4,5]](S + 1)");

        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("20");

        Set<AbstractOperation> operations = expression.visitOperations(new OperationCollectorVisitor());
        SequenceVariableValueOperation sequenceVariable = operations.stream()
                .filter(SequenceVariableValueOperation.class::isInstance)
                .map(SequenceVariableValueOperation.class::cast)
                .findFirst()
                .orElseThrow();
        AbstractSequencialMathOperation sequenceOperation = operations.stream()
                .filter(AbstractSequencialMathOperation.class::isInstance)
                .map(AbstractSequencialMathOperation.class::cast)
                .findFirst()
                .orElseThrow();

        Assertions.assertThat(sequenceVariable.getCache()).isNull();
        Assertions.assertThat(sequenceOperation.getCache()).isNull();
    }

    @Test
    void testSetVariablesShouldUpdatePlainValuesAndProvidersTogether() {
        Expression expression = new Expression("a + b + c + d");
        Map<String, Object> firstBatch = new HashMap<>();
        firstBatch.put("a", 1);
        firstBatch.put("b", 2);
        firstBatch.put("c", (Supplier<Object>) () -> 3);
        firstBatch.put("d", (VariableProvider) context -> 4);

        expression.setVariables(firstBatch);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("10");

        Map<String, Object> secondBatch = new HashMap<>();
        secondBatch.put("a", 10);
        secondBatch.put("b", 20);
        secondBatch.put("c", (Supplier<Object>) () -> 30);
        secondBatch.put("d", (VariableProvider) context -> 40);

        expression.setVariables(secondBatch);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("100");
    }

    @Test
    void testSemanticallyEquivalentComparableValueShouldKeepCache() {
        Expression expression = new Expression("a + 1");
        expression.setVariable("a", new BigDecimal("1.0"));

        BigDecimal firstEvaluation = expression.evaluate();
        expression.setVariable("a", new BigDecimal("1.00"));
        BigDecimal secondEvaluation = expression.evaluate();

        Assertions.assertThat(secondEvaluation).isSameAs(firstEvaluation);
        Assertions.assertThat(secondEvaluation).isEqualByComparingTo("2.0");
    }

    @Test
    void testVariableWithManyParentsShouldInvalidateAllCachedBranchesOnUpdate() {
        int occurrences = 64;
        Expression expression = new Expression(buildRepeatedAdditionExpression("a", occurrences));

        expression.setVariable("a", BigDecimal.ONE);
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("64");

        expression.setVariable("a", BigDecimal.valueOf(2));
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("128");
    }

    @Test
    void testVariableWithManyParentsShouldSupportCachingToggleWithoutErrors() {
        int occurrences = 64;
        Expression expression = new Expression(buildRepeatedAdditionExpression("a", occurrences));
        expression.setVariable("a", BigDecimal.ONE);
        expression.evaluate();

        Set<AbstractOperation> operations = expression.visitOperations(new OperationCollectorVisitor());
        AbstractVariableValueOperation variableOperation = operations.stream()
                .filter(AbstractVariableValueOperation.class::isInstance)
                .map(AbstractVariableValueOperation.class::cast)
                .filter(operation -> "a".equals(operation.getVariableName()))
                .findFirst()
                .orElseThrow();

        variableOperation.configureCaching(false);
        expression.setVariable("a", BigDecimal.valueOf(3));
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("192");

        variableOperation.configureCaching(true);
        expression.setVariable("a", BigDecimal.valueOf(4));
        Assertions.assertThat(expression.<BigDecimal>evaluate()).isEqualByComparingTo("256");
    }

    private static String buildRepeatedAdditionExpression(String variableName, int occurrences) {
        if (occurrences <= 0) {
            throw new IllegalArgumentException("Occurrences must be greater than zero");
        }
        StringBuilder builder = new StringBuilder(variableName);
        for (int i = 1; i < occurrences; i++) {
            builder.append(" + ").append(variableName);
        }
        return builder.toString();
    }
}
