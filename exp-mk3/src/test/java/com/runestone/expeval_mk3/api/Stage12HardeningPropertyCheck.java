package com.runestone.expeval_mk3.api;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.Report;
import net.jqwik.api.Reporting;
import net.jqwik.api.ShrinkingMode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The opt-in ten-thousand-trial property selected by {@link Stage12HardeningProperties}' child launcher. */
final class Stage12HardeningPropertyCheck {

    private static final ExpressionEngine ENGINE = ExpressionEngine.builder().build();
    private static final ExpressionEnvironment SUFFICIENT_ALLOWANCE_ENVIRONMENT = environment(100_000);
    private static final ExpressionEnvironment EXHAUSTED_ALLOWANCE_ENVIRONMENT = environment(0);

    @Property(tries = 10_000, shrinking = ShrinkingMode.FULL)
    @Report(Reporting.FALSIFIED)
    void recursiveTraversalCompositionsRespectTheirSafeAllowance(
            @ForAll("recursiveTraversalCases") RecursiveTraversalCase traversalCase) {
        ExpressionEnvironment environment = traversalCase.exhaustsAllowance()
                ? EXHAUSTED_ALLOWANCE_ENVIRONMENT : SUFFICIENT_ALLOWANCE_ENVIRONMENT;
        ExpressionCompilationResult result = ENGINE.compile(traversalCase.source(), environment);

        assertThat(result).as(traversalCase.source()).isInstanceOf(ExpressionCompilationResult.Success.class);
        MathExpression expression = ((ExpressionCompilationResult.Success) result).compiledExpression().asMath();
        if (traversalCase.exhaustsAllowance()) {
            assertThatThrownBy(() -> expression.compute(Map.of(
                            "values", traversalCase.values(), "factor", traversalCase.factor())))
                    .isInstanceOf(ExpressionExecutionException.class)
                    .satisfies(error -> assertThat(((ExpressionExecutionException) error).diagnostic().code())
                            .isEqualTo("RUNTIME_TRAVERSAL_STEP_LIMIT_EXCEEDED"));
            return;
        }

        BigDecimal actual = expression.compute(Map.of(
                "values", traversalCase.values(), "factor", traversalCase.factor()));
        assertThat(actual).as(traversalCase.source()).isEqualByComparingTo(traversalCase.expected());
    }

    @Provide
    Arbitrary<RecursiveTraversalCase> recursiveTraversalCases() {
        return Arbitraries.longs().map(Stage12HardeningPropertyCheck::recursiveTraversalCase);
    }

    private static ExpressionEnvironment environment(int maxTraversalSteps) {
        return ExpressionEnvironment.builder()
                .trustMode(ExpressionTrustMode.SAFE)
                .resourceLimits(ExpressionResourceLimits.builder().maxTraversalSteps(maxTraversalSteps).build())
                .externalSymbol("values", new CollectionType(ScalarType.NUMBER), List.of(BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("factor", ScalarType.NUMBER, BigDecimal.ZERO, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
    }

    private static RecursiveTraversalCase recursiveTraversalCase(long seed) {
        Random random = new Random(seed);
        int depth = random.nextInt(16) + 1;
        BigDecimal factor = BigDecimal.valueOf(random.nextInt(11) - 5L);
        List<BigDecimal> values = new ArrayList<>();
        for (int index = 0, size = random.nextInt(64) + 1; index < size; index++) {
            values.add(BigDecimal.valueOf(random.nextInt(31) - 15L));
        }

        String source = recursiveTraversalSource("values", depth);
        List<BigDecimal> expectedValues = values;
        for (int index = 0; index < depth; index++) {
            expectedValues = expectedValues.stream().map(value -> value.add(factor)).toList();
        }
        BigDecimal expected = expectedValues.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return new RecursiveTraversalCase(source + ".sum()", List.copyOf(values), factor, expected, random.nextBoolean());
    }

    private static String recursiveTraversalSource(String receiver, int remainingDepth) {
        if (remainingDepth == 0) {
            return receiver;
        }
        return recursiveTraversalSource(receiver, remainingDepth - 1) + ".map(@ -> @ + factor)";
    }

    record RecursiveTraversalCase(
            String source, List<BigDecimal> values, BigDecimal factor, BigDecimal expected, boolean exhaustsAllowance) {
    }
}
