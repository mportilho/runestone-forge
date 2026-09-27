package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TraversalStepLimitTest {
    private final ExpressionEngine engine = ExpressionEngine.builder().build();

    @Test
    void exposesTraversalPolicyAndZeroDisablesTraversalCapacity() {
        ExpressionResourceLimits limits = ExpressionResourceLimits.builder().maxTraversalSteps(0).build();
        ResultExpression expression = compile("values.sum()", environment(ExpressionTrustMode.SAFE, limits,
                List.of(BigDecimal.ONE), null));

        assertTraversalFailure(expression::compute);
    }

    @Test
    void safeTraversalFailsBeforeTheNextItemCallbackAndPreservesEarlierEffects() {
        CountingProvider provider = new CountingProvider(99);
        ResultExpression expression = compile("values.map(@ -> record(@)).sum()",
                environment(ExpressionTrustMode.SAFE, limits(2), numbers(1, 2, 3), provider));

        assertTraversalFailure(expression::compute);
        assertThat(provider.calls()).isEqualTo(2);
    }

    @Test
    void nestedTraversalsShareOneAllowanceAndLazyOperationsChargeOnlyReachedItems() {
        CountingProvider provider = new CountingProvider(2);
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .trustMode(ExpressionTrustMode.SAFE).resourceLimits(limits(4))
                .externalSymbol("outer", numbers(1, 2), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("inner", numbers(1, 2), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .functionsFrom(provider, FunctionPurity.IMPURE).build();
        ResultExpression expression = compile("outer.map(@ -> inner.any(@ -> mark(@))).count()", environment);

        assertTraversalFailure(expression::compute);
        assertThat(provider.calls()).isEqualTo(2);
    }

    @Test
    void shortCircuitStopsChargingAndEachExecutionGetsFreshAllowance() {
        CountingProvider provider = new CountingProvider(2);
        ResultExpression expression = compile("values.any(@ -> mark(@))",
                environment(ExpressionTrustMode.SAFE, limits(2), numbers(1, 2, 3), provider));

        assertThat(expression.compute()).isEqualTo(true);
        assertThat(expression.compute()).isEqualTo(true);
        assertThat(provider.calls()).isEqualTo(4);
    }

    @Test
    void trustedAndUnsafeDoNotEnforceRuntimeTraversalSteps() {
        for (ExpressionTrustMode mode : List.of(ExpressionTrustMode.TRUSTED, ExpressionTrustMode.UNSAFE)) {
            CountingProvider provider = new CountingProvider(99);
            ResultExpression expression = compile("values.map(@ -> record(@)).sum()",
                    environment(mode, limits(0), numbers(1, 2, 3), provider));

            assertThat(expression.compute()).isEqualTo(new BigDecimal("6"));
            assertThat(provider.calls()).isEqualTo(3);
        }
    }

    @Test
    void validationStructuralEqualityAndPublicMaterializationUseTheExecutionAllowance() {
        ExpressionResourceLimits limits = limits(2);
        ExpressionEnvironment equalityEnvironment = ExpressionEnvironment.builder()
                .trustMode(ExpressionTrustMode.SAFE).resourceLimits(limits)
                .externalSymbol("left", numbers(1, 2, 3), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("right", numbers(1, 2, 3), ExternalSymbolOverwritePolicy.OVERRIDABLE).build();
        assertTraversalFailure(compile("left = right", equalityEnvironment)::compute);

        ResultExpression publicResult = compile("values",
                environment(ExpressionTrustMode.SAFE, limits, numbers(1, 2, 3), null));
        assertTraversalFailure(publicResult::compute);

        ResultExpression override = compile("values.count()",
                environment(ExpressionTrustMode.SAFE, limits, numbers(1), null));
        assertTraversalFailure(() -> override.compute(Map.of("values", numbers(1, 2, 3))));
    }

    @Test
    void providerReentrancyStartsAnIndependentExecutionAllowance() {
        ReentrantProvider provider = new ReentrantProvider();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .trustMode(ExpressionTrustMode.SAFE).resourceLimits(limits(1))
                .externalSymbol("values", List.of(BigDecimal.ONE), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .functionsFrom(provider, FunctionPurity.IMPURE).build();
        provider.nested = compile("values.any(@ -> true)", environment);
        ResultExpression outer = compile("values.any(@ -> reenter(@))", environment);

        assertThat(outer.compute()).isEqualTo(true);
    }

    @Test
    void assignmentMapsUnusedOverridesAndMapKeyComparisonCannotBypassZeroCapacity() {
        ExpressionResourceLimits zero = limits(0);
        ExpressionEnvironment assignmentsEnvironment = ExpressionEnvironment.builder()
                .trustMode(ExpressionTrustMode.SAFE).resourceLimits(zero).build();
        CompiledExpression assignmentsCompiled = ((ExpressionCompilationResult.Success) engine.compile(
                "answer := 1; answer", assignmentsEnvironment)).compiledExpression();
        assertTraversalFailure(assignmentsCompiled.asAssignments()::compute);
        assertTraversalFailure(assignmentsCompiled.asAssignments()::computeWithMemory);

        ExpressionEnvironment unusedOverrideEnvironment = ExpressionEnvironment.builder()
                .trustMode(ExpressionTrustMode.SAFE).resourceLimits(zero)
                .externalSymbol("unused", numbers(1), ExternalSymbolOverwritePolicy.OVERRIDABLE).build();
        ResultExpression scalar = compile("1", unusedOverrideEnvironment);
        assertTraversalFailure(() -> scalar.compute(Map.of("unused", numbers(1))));

        ExpressionEnvironment scalarOverrideEnvironment = ExpressionEnvironment.builder()
                .trustMode(ExpressionTrustMode.SAFE).resourceLimits(zero)
                .externalSymbol("unused", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE).build();
        assertTraversalFailure(() -> compile("1", scalarOverrideEnvironment)
                .compute(Map.of("unused", BigDecimal.TEN)));

        ExpressionEnvironment maps = ExpressionEnvironment.builder()
                .trustMode(ExpressionTrustMode.SAFE).resourceLimits(zero)
                .externalSymbol("left", Map.of("a", BigDecimal.ONE), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("right", Map.of("b", BigDecimal.ONE), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        assertTraversalFailure(compile("left = right", maps)::compute);
    }

    @Test
    void safeProviderAdaptersFailBeforeAdvancingAContainerResult() {
        AdvancingIterableProvider provider = new AdvancingIterableProvider();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .trustMode(ExpressionTrustMode.SAFE).resourceLimits(limits(0))
                .functionsFrom(provider, FunctionPurity.IMPURE).build();
        ResultExpression expression = compile("generated().count()", environment);

        assertTraversalFailure(expression::compute);
        assertThat(provider.nextCalls()).isZero();
    }

    @Test
    void safeOverrideConversionFailsBeforeAdvancingAnIterable() {
        AdvancingIterable values = new AdvancingIterable();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .trustMode(ExpressionTrustMode.SAFE).resourceLimits(limits(1))
                .externalSymbol("values", numbers(1), ExternalSymbolOverwritePolicy.OVERRIDABLE).build();
        ResultExpression expression = compile("values.count()", environment);

        assertTraversalFailure(() -> expression.compute(Map.of("values", values)));
        assertThat(values.nextCalls()).isZero();
    }

    @Test
    void safeOfficialBuiltInsChargeEveryReachedCollectionPass() {
        ResultExpression expression = compile("meanDev(values)",
                environment(ExpressionTrustMode.SAFE, limits(8), numbers(1, 2), null));

        assertTraversalFailure(expression::compute);
    }

    private ResultExpression compile(String source, ExpressionEnvironment environment) {
        return ((ExpressionCompilationResult.Success) engine.compile(source, environment))
                .compiledExpression().asResult();
    }

    private static ExpressionEnvironment environment(ExpressionTrustMode mode, ExpressionResourceLimits limits,
                                                       List<BigDecimal> values, Object provider) {
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder().trustMode(mode).resourceLimits(limits)
                .externalSymbol("values", values, ExternalSymbolOverwritePolicy.OVERRIDABLE);
        if (provider != null) {
            builder.functionsFrom(provider, FunctionPurity.IMPURE);
        }
        return builder.build();
    }

    private static ExpressionResourceLimits limits(int steps) {
        return ExpressionResourceLimits.builder().maxTraversalSteps(steps).build();
    }

    private static List<BigDecimal> numbers(int... values) {
        return java.util.Arrays.stream(values).mapToObj(BigDecimal::valueOf).toList();
    }

    private static void assertTraversalFailure(org.assertj.core.api.ThrowableAssert.ThrowingCallable callable) {
        assertThatThrownBy(callable).isInstanceOf(ExpressionExecutionException.class)
                .satisfies(error -> assertThat(((ExpressionExecutionException) error).diagnostic().code())
                        .isEqualTo("RUNTIME_TRAVERSAL_STEP_LIMIT_EXCEEDED"));
    }

    public static final class CountingProvider {
        private final AtomicInteger calls = new AtomicInteger();
        private final int trueAt;

        CountingProvider(int trueAt) {
            this.trueAt = trueAt;
        }

        public Boolean mark(BigDecimal value) {
            calls.incrementAndGet();
            return value.compareTo(BigDecimal.valueOf(trueAt)) == 0;
        }

        public BigDecimal record(BigDecimal value) {
            calls.incrementAndGet();
            return value;
        }

        int calls() {
            return calls.get();
        }
    }

    public static final class ReentrantProvider {
        private ResultExpression nested;

        public Boolean reenter(BigDecimal ignored) {
            return (Boolean) nested.compute();
        }
    }

    public static final class AdvancingIterableProvider {
        private final AdvancingIterable values = new AdvancingIterable();

        public Iterable<BigDecimal> generated() {
            return values;
        }

        int nextCalls() {
            return values.nextCalls();
        }
    }

    private static final class AdvancingIterable implements Iterable<BigDecimal> {
        private final AtomicInteger nextCalls = new AtomicInteger();

        @Override
        public Iterator<BigDecimal> iterator() {
            return new Iterator<>() {
                private boolean available = true;

                @Override
                public boolean hasNext() {
                    return available;
                }

                @Override
                public BigDecimal next() {
                    available = false;
                    nextCalls.incrementAndGet();
                    return BigDecimal.ONE;
                }
            };
        }

        private int nextCalls() {
            return nextCalls.get();
        }
    }
}
