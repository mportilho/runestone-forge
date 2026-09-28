package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.runtime.ValueShapeValidator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValueShapeLimitsTest {
    private final ExpressionEngine engine = ExpressionEngine.builder().build();

    @Test
    void textAndNumericLiteralsFailAtCompilationOnlyInLimitedModes() {
        ExpressionResourceLimits limits = ExpressionResourceLimits.builder()
                .maxTextLength(2).maxNumericPrecision(2).maxNumericScaleMagnitude(2).build();
        for (ExpressionTrustMode mode : List.of(ExpressionTrustMode.SAFE, ExpressionTrustMode.TRUSTED)) {
            for (String source : List.of("\"abc\"", "123", "0.001")) {
                assertThat(((ExpressionCompilationResult.Failure) engine.compile(source, environment(mode, limits))).diagnostics())
                        .extracting(ExpressionDiagnostic::code).contains("SEMANTIC_VALUE_SHAPE_EXCEEDED");
            }
        }
        assertThat(engine.compile("\"abc\"", environment(ExpressionTrustMode.UNSAFE, limits)))
                .isInstanceOf(ExpressionCompilationResult.Success.class);
    }

    @Test
    void defaultsAndFoldedConstantsAreRejectedAtCompilation() {
        ExpressionResourceLimits limits = ExpressionResourceLimits.builder().maxTextLength(2).build();
        ExpressionEnvironment defaults = ExpressionEnvironment.builder().resourceLimits(limits)
                .externalSymbol("value", "abc", ExternalSymbolOverwritePolicy.OVERRIDABLE).build();
        assertThat(((ExpressionCompilationResult.Failure) engine.compile("1", defaults)).diagnostics())
                .extracting(ExpressionDiagnostic::code).contains("SEMANTIC_VALUE_SHAPE_EXCEEDED");
        assertThat(((ExpressionCompilationResult.Failure) engine.compile(
                "repeat(\"a\", 3)", environment(ExpressionTrustMode.TRUSTED, limits))).diagnostics())
                .extracting(ExpressionDiagnostic::code).contains("SEMANTIC_VALUE_SHAPE_EXCEEDED");
        ExpressionResourceLimits shallow = ExpressionResourceLimits.builder().maxValueDepth(1).build();
        assertThat(((ExpressionCompilationResult.Failure) engine.compile(
                "[[1]]", environment(ExpressionTrustMode.TRUSTED, shallow))).diagnostics())
                .extracting(ExpressionDiagnostic::code).contains("SEMANTIC_VALUE_SHAPE_EXCEEDED");
    }

    @Test
    void safeOverridesAreRejectedBeforeExecutionButTrustedKeepsRuntimeUnrestricted() {
        ExpressionResourceLimits limits = ExpressionResourceLimits.builder().maxTextLength(2).build();
        for (ExpressionTrustMode mode : List.of(ExpressionTrustMode.SAFE, ExpressionTrustMode.TRUSTED)) {
            ExpressionEnvironment env = ExpressionEnvironment.builder().trustMode(mode).resourceLimits(limits)
                    .externalSymbol("value", "a", ExternalSymbolOverwritePolicy.OVERRIDABLE).build();
            ResultExpression expression = ((ExpressionCompilationResult.Success) engine.compile("value", env))
                    .compiledExpression().asResult();
            if (mode == ExpressionTrustMode.SAFE) {
                assertThatThrownBy(() -> expression.compute(Map.of("value", "abc")))
                        .isInstanceOf(ExpressionExecutionException.class)
                        .satisfies(error -> assertThat(((ExpressionExecutionException) error).diagnostic().code())
                                .isEqualTo("RUNTIME_VALUE_SHAPE_EXCEEDED"));
            } else {
                assertThat(expression.compute(Map.of("value", "abc"))).isEqualTo("abc");
            }
        }
    }

    @Test
    void cyclicContainersTerminateAndSharedAcyclicValuesRemainValid() {
        ExpressionResourceLimits limits = ExpressionResourceLimits.builder().maxValueDepth(3).build();
        List<Object> cycle = new ArrayList<>();
        cycle.add(cycle);
        assertThat(ValueShapeValidator.check(cycle, limits).property()).isEqualTo("maxValueDepth");
        List<Object> leaf = List.of("a");
        assertThat(ValueShapeValidator.check(List.of(leaf, leaf), limits)).isNull();
        Object shared = "a";
        for (int index = 0; index < 40; index++) {
            shared = List.of(shared, shared);
        }
        assertThat(ValueShapeValidator.check(shared,
                ExpressionResourceLimits.builder().maxValueDepth(41).build())).isNull();
    }

    @Test
    void cyclicInferredDefaultsAreRejectedAsInvalidConfigurationBeforeSnapshotting() {
        List<Object> cycle = new ArrayList<>();
        cycle.add(cycle);

        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .externalSymbol("cycle", cycle, ExternalSymbolOverwritePolicy.FIXED)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cycle")
                .hasMessageContaining("cyclic container");
    }

    @Test
    void deepAcyclicDefaultsRemainCompilationResourceDiagnostics() {
        Object deepDefault = List.of(List.of(BigDecimal.ONE));
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .trustMode(ExpressionTrustMode.TRUSTED)
                .resourceLimits(ExpressionResourceLimits.builder().maxValueDepth(1).build())
                .externalSymbol("deep", deepDefault, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        assertThat(((ExpressionCompilationResult.Failure) engine.compile("1", environment)).diagnostics())
                .extracting(ExpressionDiagnostic::code)
                .contains("SEMANTIC_VALUE_SHAPE_EXCEEDED");
    }

    @Test
    void minimumIntegerScaleCannotOverflowMagnitudeCheck() {
        ExpressionResourceLimits limits = ExpressionResourceLimits.defaults();
        assertThat(ValueShapeValidator.check(new BigDecimal(java.math.BigInteger.ONE, Integer.MIN_VALUE), limits)
                .property()).isEqualTo("maxNumericScaleMagnitude");
    }

    @Test
    void literalRegexLengthHasDedicatedDiagnostic() {
        ExpressionResourceLimits limits = ExpressionResourceLimits.builder().maxRegexPatternLength(1).build();
        assertThat(((ExpressionCompilationResult.Failure) engine.compile(
                "\"ab\" =~ \"ab\"", environment(ExpressionTrustMode.SAFE, limits))).diagnostics())
                .extracting(ExpressionDiagnostic::code).contains("SEMANTIC_REGEX_PATTERN_LENGTH_EXCEEDED");
        assertThat(((ExpressionCompilationResult.Failure) engine.compile(
                "split(\"ab\", \"ab\")", environment(ExpressionTrustMode.TRUSTED, limits))).diagnostics())
                .extracting(ExpressionDiagnostic::code).contains("SEMANTIC_REGEX_PATTERN_LENGTH_EXCEEDED");
    }

    @Test
    void repeatRejectsPredictableGrowthBeforeBuildingTheOutput() {
        ExpressionResourceLimits limits = ExpressionResourceLimits.builder().maxTextLength(2).build();
        ExpressionEnvironment safe = ExpressionEnvironment.builder().trustMode(ExpressionTrustMode.SAFE)
                .resourceLimits(limits).externalSymbol("count", BigDecimal.ONE,
                        ExternalSymbolOverwritePolicy.OVERRIDABLE).build();
        ResultExpression expression = ((ExpressionCompilationResult.Success) engine.compile(
                "repeat(\"a\", count)", safe)).compiledExpression().asResult();
        assertThatThrownBy(() -> expression.compute(Map.of("count", BigDecimal.valueOf(100_000_000))))
                .isInstanceOf(ExpressionExecutionException.class)
                .satisfies(error -> assertThat(((ExpressionExecutionException) error).diagnostic().code())
                        .isEqualTo("RUNTIME_VALUE_SHAPE_EXCEEDED"));
    }

    @Test
    void predictableConstantExpansionsFailDuringCompilation() {
        ExpressionResourceLimits limits = ExpressionResourceLimits.builder().maxTextLength(3).build();
        ExpressionEnvironment trusted = environment(ExpressionTrustMode.TRUSTED, limits);
        for (String source : List.of("padLeft(\"a\", 1000000)",
                "concat([\"ab\", \"cd\"])", "join([\"a\", \"b\"], \"---\")",
                "\"ab\" || \"cd\"", "replace(\"aaaa\", \"a\", \"bb\")",
                "replaceAll(\"aaaa\", \"a\", \"bb\")")) {
            assertThat(((ExpressionCompilationResult.Failure) engine.compile(source, trusted)).diagnostics())
                    .as(source).extracting(ExpressionDiagnostic::code).contains("SEMANTIC_VALUE_SHAPE_EXCEEDED");
        }
    }

    @Test
    void dynamicExpansionsFailAtCallWithoutReclassifyingAsProviderFailure() {
        ExpressionResourceLimits limits = ExpressionResourceLimits.builder()
                .maxTextLength(3).maxMaterializedSize(2).build();
        ExpressionEnvironment safe = ExpressionEnvironment.builder().trustMode(ExpressionTrustMode.SAFE)
                .resourceLimits(limits).externalSymbol("text", "a", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("pattern", "a", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("replacement", "x", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("size", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE).build();
        for (String source : List.of("padRight(text, size)", "replace(text, \"a\", replacement)",
                "replaceAll(text, pattern, replacement)", "replaceAll(text, \"a\", replacement)")) {
            ResultExpression expression = ((ExpressionCompilationResult.Success) engine.compile(source, safe))
                    .compiledExpression().asResult();
            assertThatThrownBy(() -> expression.compute(Map.of(
                            "text", "aaa", "size", BigDecimal.valueOf(1_000_000), "replacement", "xx")))
                    .as(source).isInstanceOf(ExpressionExecutionException.class)
                    .satisfies(error -> assertThat(((ExpressionExecutionException) error).diagnostic().code())
                            .isEqualTo("RUNTIME_VALUE_SHAPE_EXCEEDED"));
        }
        for (String source : List.of("split(text, pattern)", "split(text, \"a\")")) {
            ResultExpression expression = ((ExpressionCompilationResult.Success) engine.compile(source, safe))
                    .compiledExpression().asResult();
            assertThatThrownBy(() -> expression.compute(Map.of("text", "aaaa")))
                    .as(source).isInstanceOf(ExpressionExecutionException.class)
                    .satisfies(error -> assertThat(((ExpressionExecutionException) error).diagnostic().code())
                            .isEqualTo("RUNTIME_VALUE_SHAPE_EXCEEDED"));
            assertThatThrownBy(() -> expression.compute(Map.of("text", "aaa")))
                    .as(source).isInstanceOf(ExpressionExecutionException.class)
                    .satisfies(error -> assertThat(((ExpressionExecutionException) error).diagnostic().code())
                            .isEqualTo("RUNTIME_MATERIALIZATION_LIMIT_EXCEEDED"));
        }
    }

    @Test
    void boundedRegexPreservesReplacementGroupsAndSplitEmptySegments() {
        var regex = com.runestone.expeval_mk3.internal.regex.LinearRegex.compile("(a)");
        assertThat(regex.replaceAllBounded("aba", "$1$1", 5, ignored -> { throw new AssertionError(); }))
                .isEqualTo("aabaa");
        assertThat(regex.splitBounded("aba", 4, 3,
                () -> { throw new AssertionError(); }, () -> { throw new AssertionError(); }))
                .containsExactly("", "b", "");
        assertThat(com.runestone.expeval_mk3.internal.regex.LinearRegex.compile("")
                .splitBounded("ab", 4, 3,
                        () -> { throw new AssertionError(); }, () -> { throw new AssertionError(); }))
                .containsExactly("a", "b", "");
        assertThat(com.runestone.expeval_mk3.internal.regex.LinearRegex.compile("(?P<optional>a)?b")
                .replaceAllBounded("b", "${optional}", 4, ignored -> { throw new AssertionError(); }))
                .isEqualTo("null");
    }

    @Test
    void safeConcatenationChecksCombinedLengthAndReplaceUsesActualMatches() {
        ExpressionResourceLimits limits = ExpressionResourceLimits.builder().maxTextLength(10).build();
        ExpressionEnvironment safe = ExpressionEnvironment.builder().trustMode(ExpressionTrustMode.SAFE)
                .resourceLimits(limits).externalSymbol("left", "a", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("right", "b", ExternalSymbolOverwritePolicy.OVERRIDABLE).build();
        ResultExpression concatenation = ((ExpressionCompilationResult.Success) engine.compile("left || right", safe))
                .compiledExpression().asResult();
        assertThatThrownBy(() -> concatenation.compute(Map.of("left", "123456", "right", "789012")))
                .isInstanceOf(ExpressionExecutionException.class);
        ResultExpression replacement = ((ExpressionCompilationResult.Success) engine.compile(
                "replace(left, \"ab\", \"WXYZ\")", safe)).compiledExpression().asResult();
        assertThat(replacement.compute(Map.of("left", "abcdef"))).isEqualTo("WXYZcdef");
    }

    @Test
    void nullContainerMembersRemainContractViolations() {
        List<Object> values = new ArrayList<>();
        values.add(null);
        assertThat(ValueShapeValidator.check(values, ExpressionResourceLimits.defaults()).kind())
                .isEqualTo(ValueShapeValidator.Kind.FORBIDDEN_NULL);
    }

    private static ExpressionEnvironment environment(ExpressionTrustMode mode, ExpressionResourceLimits limits) {
        return ExpressionEnvironment.builder().trustMode(mode).resourceLimits(limits).build();
    }
}
