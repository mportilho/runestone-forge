package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionDiagnosticListContractTest {

    @Test
    void apiReturnsTheCompleteCanonicalListAndSuppressesOnlyDependentCascades() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("a", ScalarType.STRING, "external", ExternalSymbolOverwritePolicy.FIXED)
                .build();

        ExpressionCompilationResult independent = ExpressionEngine.defaultEngine().compile(
                "first := missingOne + 1; second := missingTwo + 1; 0", environment);
        assertThat(independent).isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                assertThat(failure.diagnostics()).extracting(ExpressionDiagnostic::code)
                        .containsExactly("SEMANTIC_UNKNOWN_SYMBOL", "SEMANTIC_UNKNOWN_SYMBOL"));

        ExpressionCompilationResult dependent = ExpressionEngine.defaultEngine().compile(
                "a := missing + 1; a + 2", environment);
        assertThat(dependent).isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                assertThat(failure.diagnostics()).extracting(ExpressionDiagnostic::code)
                        .containsExactly("SEMANTIC_UNKNOWN_SYMBOL"));
    }

    @Test
    void apiEmitsTheRemainingRegisteredSemanticFamilies() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();
        ExpressionEnvironment typedEnvironment = ExpressionEnvironment.builder()
                .externalSymbol("number", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("text", ScalarType.STRING, "value", ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertSingleCode("1 + [2]", environment, "SEMANTIC_COLLECTION_ELEMENT_TYPE_MISMATCH");
        assertSingleCode("number = text", typedEnvironment, "SEMANTIC_EQUALITY_TYPE_MISMATCH");
        assertSingleCode(
                "if true then number else text endif", typedEnvironment, "SEMANTIC_CONDITIONAL_TYPE_MISMATCH");
        assertSingleCode("a := 1; a := true; a", environment, "SEMANTIC_ASSIGNMENT_TARGET_MISMATCH");
    }

    @Test
    void apiEmitsDeferredFactorialDiagnostics() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol(
                        "value", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        MathExpression expression = ExpressionEngine.defaultEngine().compileOrThrow("value!", environment).asMath();

        assertExecutionCode(expression, new BigDecimal("1.5"), "RUNTIME_FACTORIAL_NOT_INTEGRAL");
        assertExecutionCode(expression, BigDecimal.ONE.negate(), "RUNTIME_FACTORIAL_NEGATIVE");
    }

    private static void assertSingleCode(String source, ExpressionEnvironment environment, String expectedCode) {
        ExpressionCompilationResult result = ExpressionEngine.defaultEngine().compile(source, environment);
        assertThat(result).isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                assertThat(failure.diagnostics()).extracting(ExpressionDiagnostic::code)
                        .containsExactly(expectedCode));
    }

    private static void assertExecutionCode(MathExpression expression, BigDecimal value, String expectedCode) {
        assertThatThrownBy(() -> expression.compute(Map.of("value", value)))
                .isInstanceOf(ExpressionExecutionException.class)
                .satisfies(thrown -> assertThat(((ExpressionExecutionException) thrown).diagnostic().code())
                        .isEqualTo(expectedCode));
    }
}
