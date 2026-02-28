package com.runestone.expeval.operation.values.variable;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval.expression.ExpressionContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class VariableValueOperationOptimizationTest {

    @Test
    public void testNoRedundantCallsWhenContextsAreIdentical() {
        AtomicInteger callCount = new AtomicInteger(0);
        Function<String, Object> variablesSupplier = name -> {
            callCount.incrementAndGet();
            return null;
        };

        ExpressionContext context = new ExpressionContext();
        context.setVariablesSupplier(variablesSupplier);

        Expression expression = new Expression("a + 1", context);

        try {
            expression.evaluate();
        } catch (Exception e) {
            // Expected to fail as 'a' is null and allowingNull is false by default
        }

        // With the optimization, it should call the supplier only once
        Assertions.assertThat(callCount.get()).isEqualTo(1);
    }

    @Test
    public void testRedundantCallsWhenContextsAreDifferent() {
        AtomicInteger userCallCount = new AtomicInteger(0);
        AtomicInteger expressionCallCount = new AtomicInteger(0);

        Function<String, Object> userSupplier = name -> {
            userCallCount.incrementAndGet();
            return null;
        };
        Function<String, Object> expressionSupplier = name -> {
            expressionCallCount.incrementAndGet();
            return 10;
        };

        ExpressionContext userContext = new ExpressionContext();
        userContext.setVariablesSupplier(userSupplier);

        ExpressionContext expressionContext = new ExpressionContext();
        expressionContext.setVariablesSupplier(expressionSupplier);

        Expression expression = new Expression("a + 1", expressionContext);

        BigDecimal result = expression.evaluate(userContext);

        Assertions.assertThat(result).isEqualByComparingTo("11");
        Assertions.assertThat(userCallCount.get()).isEqualTo(1);
        Assertions.assertThat(expressionCallCount.get()).isEqualTo(1);
    }
}
