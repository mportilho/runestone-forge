package com.runestone.expeval.api;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class TrustModeResourceDiagnosticMatrixTest {

    private final ExpressionEngine engine = ExpressionEngine.builder().build();

    @Test
    void resourceDiagnosticsFollowTheUnsafeTrustedSafeEnforcementMatrix() {
        for (ExpressionTrustMode mode : ExpressionTrustMode.values()) {
            assertCompilationPhase(mode);
            assertRuntimeValueShapePhase(mode);
            assertRuntimeMaterializationPhase(mode);
            assertRuntimeTraversalPhase(mode);
        }
    }

    private void assertCompilationPhase(ExpressionTrustMode mode) {
        for (CompilationResourceScenario scenario : List.of(
                new CompilationResourceScenario(ExpressionResourceLimits.builder().maxSourceLength(1).build(), "12",
                        "COMPILE_SOURCE_LENGTH_EXCEEDED"),
                new CompilationResourceScenario(ExpressionResourceLimits.builder().maxTokenCount(2).build(), "1+1",
                        "COMPILE_TOKEN_COUNT_EXCEEDED"),
                new CompilationResourceScenario(ExpressionResourceLimits.builder().maxSyntaxDepth(1).build(), "-1",
                        "COMPILE_SYNTAX_DEPTH_EXCEEDED"),
                new CompilationResourceScenario(ExpressionResourceLimits.builder().maxAstNodeCount(1).build(), "1",
                        "COMPILE_AST_NODE_COUNT_EXCEEDED"),
                new CompilationResourceScenario(ExpressionResourceLimits.builder().maxTextLength(1).build(), "\"xx\"",
                        "SEMANTIC_VALUE_SHAPE_EXCEEDED"),
                new CompilationResourceScenario(ExpressionResourceLimits.builder().maxRegexPatternLength(1).build(),
                        "\"a\" =~ \"aa\"", "SEMANTIC_REGEX_PATTERN_LENGTH_EXCEEDED"))) {
            ExpressionEnvironment environment = ExpressionEnvironment.builder()
                    .trustMode(mode)
                    .resourceLimits(scenario.limits())
                    .build();
            ExpressionCompilationResult result = engine.compile(scenario.source(), environment);
            if (mode == ExpressionTrustMode.UNSAFE) {
                assertThat(result).as(scenario.code()).isInstanceOf(ExpressionCompilationResult.Success.class);
                continue;
            }

            assertThat(result).as(scenario.code()).isInstanceOf(ExpressionCompilationResult.Failure.class);
            assertThat(((ExpressionCompilationResult.Failure) result).diagnostics())
                    .extracting(ExpressionDiagnostic::code)
                    .containsExactly(scenario.code());
        }
    }

    private void assertRuntimeValueShapePhase(ExpressionTrustMode mode) {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .trustMode(mode)
                .resourceLimits(ExpressionResourceLimits.builder().maxTextLength(1).build())
                .externalSymbol("text", "x", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        ResultExpression expression = ((ExpressionCompilationResult.Success) engine.compile("text", environment))
                .compiledExpression()
                .asResult();

        if (mode == ExpressionTrustMode.SAFE) {
            assertThatThrownBy(() -> expression.compute(Map.of("text", "xx")))
                    .isInstanceOf(ExpressionExecutionException.class)
                    .satisfies(error -> assertThat(((ExpressionExecutionException) error).diagnostic().code())
                            .isEqualTo("RUNTIME_VALUE_SHAPE_EXCEEDED"));
            return;
        }

        assertThat(expression.compute(Map.of("text", "xx"))).isEqualTo("xx");
    }

    private void assertRuntimeMaterializationPhase(ExpressionTrustMode mode) {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .trustMode(mode)
                .resourceLimits(ExpressionResourceLimits.builder().maxMaterializedSize(1).build())
                .externalSymbol("text", "a", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("pattern", ",", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        ResultExpression expression = ((ExpressionCompilationResult.Success) engine.compile("split(text, pattern)", environment))
                .compiledExpression()
                .asResult();

        if (mode == ExpressionTrustMode.SAFE) {
            assertResourceFailure(() -> expression.compute(Map.of("text", "a,b")),
                    "RUNTIME_MATERIALIZATION_LIMIT_EXCEEDED");
            return;
        }

        assertThat(expression.compute(Map.of("text", "a,b"))).isEqualTo(List.of("a", "b"));
    }

    private void assertRuntimeTraversalPhase(ExpressionTrustMode mode) {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .trustMode(mode)
                .resourceLimits(ExpressionResourceLimits.builder().maxTraversalSteps(0).build())
                .externalSymbol("values", new CollectionType(ScalarType.NUMBER), List.of(BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        MathExpression expression = ((ExpressionCompilationResult.Success) engine.compile("values.sum()", environment))
                .compiledExpression()
                .asMath();

        if (mode == ExpressionTrustMode.SAFE) {
            assertResourceFailure(expression::compute, "RUNTIME_TRAVERSAL_STEP_LIMIT_EXCEEDED");
            return;
        }

        assertThat(expression.compute()).isEqualByComparingTo(BigDecimal.ONE);
    }

    private static void assertResourceFailure(org.assertj.core.api.ThrowableAssert.ThrowingCallable action, String code) {
        assertThatThrownBy(action)
                .isInstanceOf(ExpressionExecutionException.class)
                .satisfies(error -> assertThat(((ExpressionExecutionException) error).diagnostic().code()).isEqualTo(code));
    }

    private record CompilationResourceScenario(ExpressionResourceLimits limits, String source, String code) {
    }
}
