package com.runestone.expeval.api;

import com.runestone.expeval.internal.runtime.ValueShapeValidator;
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

/**
 * Essential Stage 12 properties. jqwik prints the generated seed on failure; the minimized source in
 * the assertion message is promoted to a synthetic expression case before a regression is fixed.
 */
final class HardeningPropertyTest {

    private static final ExpressionEngine ENGINE = ExpressionEngine.builder().build();
    private static final ExpressionEnvironment STANDARD_ENVIRONMENT = ExpressionEnvironment.standard();
    private static final ExpressionEnvironment TEXT_ENVIRONMENT = ExpressionEnvironment.builder()
            .externalSymbol("text", "x", ExternalSymbolOverwritePolicy.OVERRIDABLE)
            .build();
    private static final ExpressionEnvironment TRAVERSAL_ENVIRONMENT = ExpressionEnvironment.builder()
            .trustMode(ExpressionTrustMode.SAFE)
            .resourceLimits(ExpressionResourceLimits.builder().maxTraversalSteps(1_000).build())
            .externalSymbol("values", new CollectionType(ScalarType.NUMBER), List.of(BigDecimal.ONE),
                    ExternalSymbolOverwritePolicy.OVERRIDABLE)
            .externalSymbol("factor", ScalarType.NUMBER, BigDecimal.ZERO, ExternalSymbolOverwritePolicy.OVERRIDABLE)
            .build();
    private static final ExpressionResourceLimits VALUE_DEPTH_LIMITS = ExpressionResourceLimits.builder()
            .maxValueDepth(32)
            .build();

    @Property(tries = 1_000, shrinking = ShrinkingMode.FULL)
    @Report(Reporting.FALSIFIED)
    void recursivelyGeneratedArithmeticExpressionsPreserveTheirExactValue(
            @ForAll("recursiveArithmetic") ArithmeticCase expressionCase) {
        ExpressionCompilationResult result = ENGINE.compile(expressionCase.source(), STANDARD_ENVIRONMENT);

        assertThat(result).as(expressionCase.source()).isInstanceOf(ExpressionCompilationResult.Success.class);
        BigDecimal actual = ((ExpressionCompilationResult.Success) result).compiledExpression().asMath().compute();
        assertThat(actual).as(expressionCase.source()).isEqualByComparingTo(expressionCase.expected());
    }

    @Property(tries = 1_000, shrinking = ShrinkingMode.FULL)
    @Report(Reporting.FALSIFIED)
    void generatedSupplementaryUnicodeValuesSurviveThePublicBoundary(@ForAll("unicodeTexts") String text) {
        ExpressionCompilationResult result = ENGINE.compile("text", TEXT_ENVIRONMENT);

        assertThat(result).isInstanceOf(ExpressionCompilationResult.Success.class);
        Object actual = ((ExpressionCompilationResult.Success) result).compiledExpression().asResult()
                .compute(Map.of("text", text));
        assertThat(actual).isEqualTo(text);
    }

    @Property(tries = 1_000, shrinking = ShrinkingMode.FULL)
    @Report(Reporting.FALSIFIED)
    void composedTraversalPathsUseOneSafeExecutionAllowance(@ForAll("traversalCases") TraversalCase traversalCase) {
        ExpressionCompilationResult result = ENGINE.compile(
                "values.map(@ -> @ + factor).map(@ -> @ * 2).sum()", TRAVERSAL_ENVIRONMENT);

        assertThat(result).isInstanceOf(ExpressionCompilationResult.Success.class);
        BigDecimal actual = ((ExpressionCompilationResult.Success) result).compiledExpression().asMath()
                .compute(Map.of("values", traversalCase.values(), "factor", traversalCase.factor()));
        assertThat(actual).isEqualByComparingTo(traversalCase.expected());
    }

    @Property(tries = 1_000, shrinking = ShrinkingMode.FULL)
    @Report(Reporting.FALSIFIED)
    void recursivelyGeneratedDeepValuesTerminateAtTheirConfiguredBoundary(
            @ForAll("nestedValues") NestedValueCase nestedValue) {
        ValueShapeValidator.Violation violation = ValueShapeValidator.check(nestedValue.value(), VALUE_DEPTH_LIMITS);

        if (nestedValue.depth() <= VALUE_DEPTH_LIMITS.maxValueDepth()) {
            assertThat(violation).as("depth=%s", nestedValue.depth()).isNull();
        } else {
            assertThat(violation).as("depth=%s", nestedValue.depth()).isNotNull();
            assertThat(violation.property()).isEqualTo("maxValueDepth");
        }
    }

    @Property(tries = 1_000, shrinking = ShrinkingMode.FULL)
    @Report(Reporting.FALSIFIED)
    void recursivelyGeneratedInvalidSourcesProduceStructuredDiagnostics(
            @ForAll("recursiveArithmetic") ArithmeticCase expressionCase) {
        ExpressionCompilationResult result = ENGINE.compile(expressionCase.source() + " +", STANDARD_ENVIRONMENT);

        assertThat(result).as(expressionCase.source()).isInstanceOf(ExpressionCompilationResult.Failure.class);
        assertThat(((ExpressionCompilationResult.Failure) result).diagnostics()).isNotEmpty();
    }

    @Provide
    Arbitrary<ArithmeticCase> recursiveArithmetic() {
        return Arbitraries.longs().map(HardeningPropertyTest::recursiveArithmeticCase);
    }

    @Provide
    Arbitrary<String> unicodeTexts() {
        return Arbitraries.longs().map(seed -> {
            Random random = new Random(seed);
            StringBuilder text = new StringBuilder();
            int length = random.nextInt(16);
            for (int index = 0; index < length; index++) {
                int codePoint = switch (random.nextInt(4)) {
                    case 0 -> 'a' + random.nextInt(26);
                    case 1 -> 0x03B1 + random.nextInt(24);
                    default -> 0x1F600 + random.nextInt(80);
                };
                text.appendCodePoint(codePoint);
            }
            return text.toString();
        });
    }

    @Provide
    Arbitrary<TraversalCase> traversalCases() {
        return Arbitraries.longs().map(seed -> {
            Random random = new Random(seed);
            BigDecimal factor = BigDecimal.valueOf(random.nextInt(11) - 5L);
            List<BigDecimal> values = new ArrayList<>();
            BigDecimal expected = BigDecimal.ZERO;
            for (int index = 0, size = random.nextInt(8) + 1; index < size; index++) {
                BigDecimal value = BigDecimal.valueOf(random.nextInt(21) - 10L);
                values.add(value);
                expected = expected.add(value.add(factor).multiply(BigDecimal.TWO));
            }
            return new TraversalCase(List.copyOf(values), factor, expected);
        });
    }

    @Provide
    Arbitrary<NestedValueCase> nestedValues() {
        return Arbitraries.longs().map(seed -> nestedValue((int) Math.floorMod(seed, 64) + 1));
    }

    private static ArithmeticCase recursiveArithmeticCase(long seed) {
        return recursiveArithmetic(new Random(seed), 5);
    }

    private static ArithmeticCase recursiveArithmetic(Random random, int remainingDepth) {
        if (remainingDepth == 0 || random.nextBoolean()) {
            BigDecimal value = BigDecimal.valueOf(random.nextInt(21) - 10L);
            return new ArithmeticCase(value.toPlainString(), value);
        }

        ArithmeticCase left = recursiveArithmetic(random, remainingDepth - 1);
        ArithmeticCase right = recursiveArithmetic(random, remainingDepth - 1);
        return switch (random.nextInt(3)) {
            case 0 -> new ArithmeticCase("(" + left.source() + " + " + right.source() + ")",
                    left.expected().add(right.expected()));
            case 1 -> new ArithmeticCase("(" + left.source() + " - " + right.source() + ")",
                    left.expected().subtract(right.expected()));
            default -> new ArithmeticCase("(" + left.source() + " * " + right.source() + ")",
                    left.expected().multiply(right.expected()));
        };
    }

    private static NestedValueCase nestedValue(int depth) {
        return new NestedValueCase(depth, recursivelyNestedValue(depth));
    }

    private static Object recursivelyNestedValue(int remainingDepth) {
        if (remainingDepth == 0) {
            return BigDecimal.ONE;
        }
        return List.of(recursivelyNestedValue(remainingDepth - 1));
    }

    record ArithmeticCase(String source, BigDecimal expected) {
    }

    record TraversalCase(List<BigDecimal> values, BigDecimal factor, BigDecimal expected) {
    }

    record NestedValueCase(int depth, Object value) {
    }
}
