package com.runestone.expeval.api;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionTrustModeTest {
    private static final ExpressionResourceLimits LIMITS = ExpressionResourceLimits.builder()
            .maxCurrentItemDepth(0).maxMaterializedSize(1).maxFactorialInput(3).build();

    @ParameterizedTest
    @EnumSource(ExpressionTrustMode.class)
    void compilationLimitsApplyOnlyToSafeAndTrusted(ExpressionTrustMode mode) {
        ExpressionEnvironment environment = environment(mode).build();
        for (String source : List.of("[1, 2]", "4!", "items := [1]; items.map(@ -> @)")) {
            if (mode == ExpressionTrustMode.UNSAFE) {
                assertThat(ExpressionEngine.defaultEngine().compileOrThrow(source, environment)).isNotNull();
            } else {
                assertThatThrownBy(() -> ExpressionEngine.defaultEngine().compileOrThrow(source, environment))
                        .isInstanceOf(ExpressionCompilationException.class);
            }
        }
    }

    @ParameterizedTest
    @EnumSource(ExpressionTrustMode.class)
    void defaultSnapshotsAreCompilationResources(ExpressionTrustMode mode) {
        ExpressionEnvironment.Builder builder = environment(mode)
                .externalSymbol("items", List.of(1, 2), ExternalSymbolOverwritePolicy.OVERRIDABLE);
        if (mode == ExpressionTrustMode.UNSAFE) {
            assertThat(builder.build().externalSymbols().asMap()).containsKey("items");
        } else {
            assertThatThrownBy(builder::build).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("maxMaterializedSize 1");
        }
    }

    @ParameterizedTest
    @EnumSource(ExpressionTrustMode.class)
    void runtimeOverridesAndResultsAreBoundedOnlyInSafe(ExpressionTrustMode mode) {
        ExpressionEnvironment environment = environment(mode)
                .externalSymbol("items", List.of(1), ExternalSymbolOverwritePolicy.OVERRIDABLE).build();
        ResultExpression expression = ExpressionEngine.defaultEngine().compileOrThrow("items", environment).asResult();
        Map<String, Object> inputs = Map.of("items", List.of(1, 2));
        if (mode == ExpressionTrustMode.SAFE) {
            assertThatThrownBy(() -> expression.compute(inputs)).isInstanceOf(ExpressionExecutionException.class);
        } else {
            assertThat(expression.compute(inputs)).isEqualTo(List.of(BigDecimal.ONE, BigDecimal.TWO));
        }
        assertThatThrownBy(() -> expression.compute(Map.of("items", List.of("bad"))))
                .isInstanceOf(ExpressionExecutionException.class);
    }

    @ParameterizedTest
    @EnumSource(ExpressionTrustMode.class)
    void providerResultsAndAssignmentViewsAreBoundedOnlyInSafe(ExpressionTrustMode mode) {
        ExpressionEnvironment environment = environment(mode)
                .functionsFrom(Providers.class, FunctionPurity.IMPURE).build();
        ResultExpression expression = ExpressionEngine.defaultEngine().compileOrThrow("items()", environment).asResult();
        CompiledExpression assignments = ExpressionEngine.defaultEngine().compileOrThrow("a := 1; b := 2;", environment);
        if (mode == ExpressionTrustMode.SAFE) {
            assertThatThrownBy(expression::compute).isInstanceOf(ExpressionExecutionException.class);
            assertThatThrownBy(assignments::asAssignments).isInstanceOf(ExpressionViewException.class);
        } else {
            assertThat(expression.compute()).isEqualTo(List.of(BigDecimal.ONE, BigDecimal.TWO));
            assertThat(assignments.asAssignments().compute()).hasSize(2);
        }
        ResultExpression nullResult = ExpressionEngine.defaultEngine().compileOrThrow("nullResult()", environment).asResult();
        assertThatThrownBy(nullResult::compute).isInstanceOf(ExpressionExecutionException.class);
    }

    @ParameterizedTest
    @EnumSource(ExpressionTrustMode.class)
    void factorialBoundsAreSeparateFromFunctionalContracts(ExpressionTrustMode mode) {
        ExpressionEnvironment environment = environment(mode)
                .externalSymbol("n", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE).build();
        ResultExpression expression = ExpressionEngine.defaultEngine().compileOrThrow("n!", environment).asResult();
        assertThat(expression.compute(Map.of("n", 3))).isEqualTo(new BigDecimal("6"));
        if (mode == ExpressionTrustMode.SAFE) {
            assertThatThrownBy(() -> expression.compute(Map.of("n", 4)))
                    .isInstanceOf(ExpressionExecutionException.class);
        } else {
            assertThat(expression.compute(Map.of("n", 4))).isEqualTo(new BigDecimal("24"));
        }
        for (BigDecimal invalid : List.of(new BigDecimal("-1"), new BigDecimal("1.5"))) {
            assertThatThrownBy(() -> expression.compute(Map.of("n", invalid)))
                    .isInstanceOf(ExpressionExecutionException.class);
        }
        for (String invalid : List.of("(-1)!", "1.5!", "1 + true")) {
            assertThatThrownBy(() -> ExpressionEngine.defaultEngine().compileOrThrow(invalid, environment))
                    .isInstanceOf(ExpressionCompilationException.class);
        }
    }

    @ParameterizedTest
    @EnumSource(ExpressionTrustMode.class)
    void failedFactorialFoldDoesNotImposeCompilationLimitsOnRuntime(ExpressionTrustMode mode) {
        ResultExpression expression = ExpressionEngine.defaultEngine().compileOrThrow("(2 + 2)!", environment(mode).build())
                .asResult();
        if (mode == ExpressionTrustMode.SAFE) {
            assertThatThrownBy(expression::compute).isInstanceOf(ExpressionExecutionException.class);
        } else {
            assertThat(expression.compute()).isEqualTo(new BigDecimal("24"));
        }
    }

    private static ExpressionEnvironment.Builder environment(ExpressionTrustMode mode) {
        return ExpressionEnvironment.builder().trustMode(mode).resourceLimits(LIMITS);
    }

    public static final class Providers {
        public static List<Integer> items() { return List.of(1, 2); }
        public static String nullResult() { return null; }
    }
}
