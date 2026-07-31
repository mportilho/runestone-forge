package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Proves issue #95's Item Atual restoration rule through the public compile/compute path: a two-level
 * nested lambda ({@code outer.map(@ -> @.map(@ -> ...).sum())}) uses one frame slot per nesting depth,
 * and a failure partway through the inner collection operation must still restore both slots before the
 * exception propagates. The shared {@code CompiledExpression} plan is reused across independent calls
 * with isolated {@code ExecutionScope}s, so a leaked/unsaved current-item slot from a failing call would
 * only surface as a wrong result on the next call against the same plan — which this test checks for.
 */
class NestedCurrentItemRestorationTest {

    private static final CollectionType NESTED_NUMBER_COLLECTION =
            new CollectionType(new CollectionType(ScalarType.NUMBER));

    @Test
    void aMidIterationFailureInTheInnerLambdaLeavesTheSharedPlanUsableForTheNextCall() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("outer", NESTED_NUMBER_COLLECTION, List.of(List.of(BigDecimal.ONE)),
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow(
                "outer.map(@ -> @.map(@ -> 10 / @).sum())", environment);

        List<List<BigDecimal>> failingInput = List.of(List.of(BigDecimal.ONE, BigDecimal.ZERO));
        assertThatThrownBy(() -> expression.compute(Map.of("outer", failingInput)))
                .isInstanceOf(ArithmeticException.class);

        List<List<BigDecimal>> healthyInput = List.of(
                List.of(BigDecimal.TEN, BigDecimal.valueOf(5)), List.of(BigDecimal.ONE));
        assertThat(expression.compute(Map.of("outer", healthyInput)))
                .isEqualTo(List.of(new BigDecimal("3"), new BigDecimal("10")));
    }
}
