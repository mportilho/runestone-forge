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
        ResultExpression expression = ExpressionCompiler.compileOrThrow(
                        "outer.map(@ -> @.map(@ -> 10 / @).sum())", environment)
                .asResult();

        List<List<BigDecimal>> failingInput = List.of(List.of(BigDecimal.ONE, BigDecimal.ZERO));
        assertThatThrownBy(() -> expression.compute(Map.of("outer", failingInput)))
                .isInstanceOf(ExpressionExecutionException.class);

        List<List<BigDecimal>> healthyInput = List.of(
                List.of(BigDecimal.TEN, BigDecimal.valueOf(5)), List.of(BigDecimal.ONE));
        assertThat(expression.compute(Map.of("outer", healthyInput)))
                .isEqualTo(List.of(new BigDecimal("3"), new BigDecimal("10")));
    }

    /**
     * Extends the same proof to three levels of nesting at {@code maxCurrentItemDepth}'s exact boundary:
     * each level uses its own frame slot, the computed sums only match if every level's {@code @} bound the
     * correct element, and a mid-iteration failure at the innermost level must still restore all three slots
     * before the shared plan is reused for a healthy call.
     */
    @Test
    void aThreeLevelNestingAtTheDepthLimitRestoresEveryFrameAfterAMidIterationFailure() {
        CollectionType tripleNestedNumberCollection =
                new CollectionType(new CollectionType(new CollectionType(ScalarType.NUMBER)));
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .maxCurrentItemDepth(3)
                .externalSymbol("outer", tripleNestedNumberCollection,
                        List.of(List.of(List.of(BigDecimal.ONE))),
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        ResultExpression expression = ExpressionCompiler.compileOrThrow(
                        "outer.map(@ -> @.map(@ -> @.map(@ -> 100 / @).sum()).sum())", environment)
                .asResult();

        List<List<List<BigDecimal>>> failingInput = List.of(
                List.of(List.of(BigDecimal.TEN, BigDecimal.ZERO)));
        assertThatThrownBy(() -> expression.compute(Map.of("outer", failingInput)))
                .isInstanceOf(ExpressionExecutionException.class);

        List<List<List<BigDecimal>>> healthyInput = List.of(
                List.of(List.of(BigDecimal.TEN, BigDecimal.valueOf(2)), List.of(BigDecimal.valueOf(4))),
                List.of(List.of(BigDecimal.valueOf(5))));
        assertThat(expression.compute(Map.of("outer", healthyInput)))
                .isEqualTo(List.of(new BigDecimal("85"), new BigDecimal("20")));
    }
}
