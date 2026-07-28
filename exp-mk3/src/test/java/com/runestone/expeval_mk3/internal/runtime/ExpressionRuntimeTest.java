package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.MemberName;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.ast.PropertyNavigationLink;
import com.runestone.expeval_mk3.internal.source.SourceSpan;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionRuntimeTest {

    private static final SourceSpan SPAN = new SourceSpan(2, 13, 1, 3);
    private static final int CURRENT_ITEM_SLOT = 0;
    private static final CollectionType NUMBER_COLLECTION = new CollectionType(ScalarType.NUMBER);

    private static ExecutionScope newScope() {
        return new ExecutionScope(1, ZoneId.of("UTC"));
    }

    @Test
    void mapStringKeySubscriptRejectsNullValuesAtRuntime() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("present", null);

        assertThatThrownBy(() -> ExpressionRuntime.mapKeyValue(values, "present", false, SPAN))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("map value at SourceSpan[offset=2, endOffset=13, line=1, column=3]");
    }

    @Test
    void safeMapStringKeySubscriptOnlyProtectsNullReceivers() {
        assertThat(ExpressionRuntime.mapKeyValue(null, "present", true, SPAN)).isNull();

        assertThatThrownBy(() -> ExpressionRuntime.mapKeyValue(
                Map.of("present", BigDecimal.ONE), "missing", true, SPAN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("map key not found: missing");
    }

    @Test
    void allReturnsTrueWhenEveryElementSatisfiesLambda() {
        ExecutionScope scope = newScope();
        ExecutableLambda isPositive = new ExecutableLambda(
                positiveCheckNode(), CURRENT_ITEM_SLOT);

        boolean result = ExpressionRuntime.all(
                List.of(BigDecimal.ONE, BigDecimal.TEN), isPositive, scope, NUMBER_COLLECTION);

        assertThat(result).isTrue();
    }

    @Test
    void allReturnsFalseWhenAnyElementFailsLambda() {
        ExecutionScope scope = newScope();
        ExecutableLambda isPositive = new ExecutableLambda(
                positiveCheckNode(), CURRENT_ITEM_SLOT);

        boolean result = ExpressionRuntime.all(
                List.of(BigDecimal.ONE, BigDecimal.valueOf(-1)), isPositive, scope, NUMBER_COLLECTION);

        assertThat(result).isFalse();
    }

    @Test
    void anyReturnsTrueWhenSomeElementSatisfiesLambda() {
        ExecutionScope scope = newScope();
        ExecutableLambda isPositive = new ExecutableLambda(
                positiveCheckNode(), CURRENT_ITEM_SLOT);

        boolean result = ExpressionRuntime.any(
                List.of(BigDecimal.valueOf(-1), BigDecimal.TEN), isPositive, scope, NUMBER_COLLECTION);

        assertThat(result).isTrue();
    }

    @Test
    void anyReturnsFalseWhenNoElementSatisfiesLambda() {
        ExecutionScope scope = newScope();
        ExecutableLambda isPositive = new ExecutableLambda(
                positiveCheckNode(), CURRENT_ITEM_SLOT);

        boolean result = ExpressionRuntime.any(
                List.of(BigDecimal.valueOf(-1), BigDecimal.valueOf(-2)), isPositive, scope, NUMBER_COLLECTION);

        assertThat(result).isFalse();
    }

    @Test
    void mapAppliesLambdaToEveryElement() {
        ExecutionScope scope = newScope();
        ExecutableNode doubleValue = executionScope ->
                ExpressionRuntime.number(executionScope.read(CURRENT_ITEM_SLOT)).multiply(BigDecimal.TWO);
        ExecutableLambda lambda = new ExecutableLambda(doubleValue, CURRENT_ITEM_SLOT);

        List<Object> result = ExpressionRuntime.map(
                List.of(BigDecimal.ONE, BigDecimal.TEN), lambda, scope, NUMBER_COLLECTION, 10);

        assertThat(result).containsExactly(BigDecimal.valueOf(2), BigDecimal.valueOf(20));
    }

    @Test
    void reduceAccumulatesAcrossElementsUsingLambda() {
        ExecutionScope scope = newScope();
        PropertyNavigationLink accumulatorLink = new PropertyNavigationLink(
                new NodeId(1), SPAN, new MemberName("accumulator"), false);
        PropertyNavigationLink itemLink = new PropertyNavigationLink(
                new NodeId(2), SPAN, new MemberName("item"), false);
        ExecutableNode sumStep = executionScope -> {
            Object reductionItem = executionScope.read(CURRENT_ITEM_SLOT);
            BigDecimal accumulator = ExpressionRuntime.number(
                    ExpressionRuntime.propertyValue(reductionItem, accumulatorLink));
            BigDecimal item = ExpressionRuntime.number(ExpressionRuntime.propertyValue(reductionItem, itemLink));
            return accumulator.add(item);
        };
        ExecutableLambda lambda = new ExecutableLambda(sumStep, CURRENT_ITEM_SLOT);

        Object result = ExpressionRuntime.reduce(
                List.of(BigDecimal.ONE, BigDecimal.TEN), BigDecimal.ZERO, lambda, scope);

        assertThat(result).isEqualTo(BigDecimal.valueOf(11));
    }

    @Test
    void sortByOrdersAscendingAndDescendingByLambdaKey() {
        ExecutionScope scope = newScope();
        ExecutableNode identity = executionScope -> executionScope.read(CURRENT_ITEM_SLOT);
        ExecutableLambda selector = new ExecutableLambda(identity, CURRENT_ITEM_SLOT);
        List<Object> values = List.of(BigDecimal.TEN, BigDecimal.ONE, BigDecimal.valueOf(5));

        List<Object> ascending = ExpressionRuntime.sortBy(values, "asc", selector, scope, 10, ScalarType.NUMBER);
        List<Object> descending = ExpressionRuntime.sortBy(values, "desc", selector, scope, 10, ScalarType.NUMBER);

        assertThat(ascending).containsExactly(BigDecimal.ONE, BigDecimal.valueOf(5), BigDecimal.TEN);
        assertThat(descending).containsExactly(BigDecimal.TEN, BigDecimal.valueOf(5), BigDecimal.ONE);
    }

    private static ExecutableNode positiveCheckNode() {
        return executionScope -> ExpressionRuntime.number(executionScope.read(CURRENT_ITEM_SLOT))
                .compareTo(BigDecimal.ZERO) > 0;
    }
}
