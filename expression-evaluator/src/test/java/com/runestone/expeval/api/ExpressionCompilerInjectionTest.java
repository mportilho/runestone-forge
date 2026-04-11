package com.runestone.expeval.api;

import com.runestone.expeval.api.CacheConfig;
import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.internal.runtime.ExpressionCompiler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies functional correctness of the explicit-compiler overloads introduced in Option C for
 * {@link MathExpression}, {@link LogicalExpression}, and {@link AssignmentExpression}.
 *
 * <p>Cache-sharing and cache-isolation behaviour between an injected compiler and the JVM-wide
 * singleton are verified at a lower level in {@code ExpressionRuntimeSupportLifecycleTest}.
 * Tests here focus on the public API contract: correct evaluation results and null-safety.
 */
@DisplayName("Explicit compiler injection — public API overloads")
class ExpressionCompilerInjectionTest {

    private static final ExpressionEnvironment ENV = ExpressionEnvironment.builder().build();

    // --- MathExpression ---

    @Nested
    @DisplayName("MathExpression.compile(source, env, compiler)")
    class MathExpressionOverload {

        @Test
        @DisplayName("evaluates a simple arithmetic expression correctly")
        void evaluatesArithmeticExpression() {
            var compiler = new ExpressionCompiler(CacheConfig.defaults());

            BigDecimal result = MathExpression.compile("3 + 4", ENV, compiler).compute();

            assertThat(result).isEqualByComparingTo("7");
        }

        @Test
        @DisplayName("evaluates an expression with a bound variable")
        void evaluatesExpressionWithBoundVariable() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("x", BigDecimal.TEN, true)
                    .build();
            var compiler = new ExpressionCompiler(CacheConfig.defaults());

            BigDecimal result = MathExpression.compile("x * 2", env, compiler)
                    .compute(Map.of("x", 5));

            assertThat(result).isEqualByComparingTo("10");
        }

        @Test
        @DisplayName("rejects null compiler")
        void rejectsNullCompiler() {
            assertThatThrownBy(() -> MathExpression.compile("1 + 1", ENV, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("produces the same result as the singleton overload")
        void producesSameResultAsSingletonOverload() {
            var compiler = new ExpressionCompiler(CacheConfig.defaults());
            String source = "12 * 12";

            BigDecimal viaInjected = MathExpression.compile(source, ENV, compiler).compute();
            BigDecimal viaSingleton = MathExpression.compile(source, ENV).compute();

            assertThat(viaInjected).isEqualByComparingTo(viaSingleton);
        }
    }

    // --- LogicalExpression ---

    @Nested
    @DisplayName("LogicalExpression.compile(source, env, compiler)")
    class LogicalExpressionOverload {

        @Test
        @DisplayName("evaluates a simple comparison correctly")
        void evaluatesSimpleComparison() {
            var compiler = new ExpressionCompiler(CacheConfig.defaults());

            boolean result = LogicalExpression.compile("5 > 3", ENV, compiler).compute();

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("evaluates a false comparison correctly")
        void evaluatesFalseComparison() {
            var compiler = new ExpressionCompiler(CacheConfig.defaults());

            boolean result = LogicalExpression.compile("5 < 3", ENV, compiler).compute();

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("rejects null compiler")
        void rejectsNullCompiler() {
            assertThatThrownBy(() -> LogicalExpression.compile("1 > 0", ENV, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("produces the same result as the singleton overload")
        void producesSameResultAsSingletonOverload() {
            var compiler = new ExpressionCompiler(CacheConfig.defaults());
            String source = "10 >= 10";

            boolean viaInjected = LogicalExpression.compile(source, ENV, compiler).compute();
            boolean viaSingleton = LogicalExpression.compile(source, ENV).compute();

            assertThat(viaInjected).isEqualTo(viaSingleton);
        }
    }

    // --- AssignmentExpression ---

    @Nested
    @DisplayName("AssignmentExpression.compile(source, env, compiler)")
    class AssignmentExpressionOverload {

        @Test
        @DisplayName("evaluates a single assignment correctly")
        void evaluatesSingleAssignment() {
            var compiler = new ExpressionCompiler(CacheConfig.defaults());

            Map<String, Object> result = AssignmentExpression.compile("total = 6 * 7;", ENV, compiler).compute();

            assertThat(result).containsKey("total");
            assertThat((BigDecimal) result.get("total")).isEqualByComparingTo("42");
        }

        @Test
        @DisplayName("evaluates multiple assignments correctly")
        void evaluatesMultipleAssignments() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("rate", new BigDecimal("0.10"), true)
                    .build();
            var compiler = new ExpressionCompiler(CacheConfig.defaults());

            Map<String, Object> result = AssignmentExpression
                    .compile("base = 100; tax = base * rate;", env, compiler)
                    .compute(Map.of("rate", new BigDecimal("0.10")));

            assertThat(result).containsKey("base").containsKey("tax");
            assertThat((BigDecimal) result.get("base")).isEqualByComparingTo("100");
            assertThat((BigDecimal) result.get("tax")).isEqualByComparingTo("10.0");
        }

        @Test
        @DisplayName("rejects null compiler")
        void rejectsNullCompiler() {
            assertThatThrownBy(() -> AssignmentExpression.compile("x = 1", ENV, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("produces the same result as the singleton overload")
        void producesSameResultAsSingletonOverload() {
            var compiler = new ExpressionCompiler(CacheConfig.defaults());
            String source = "result = 9 * 9;";

            Map<String, Object> viaInjected = AssignmentExpression.compile(source, ENV, compiler).compute();
            Map<String, Object> viaSingleton = AssignmentExpression.compile(source, ENV).compute();

            assertThat((BigDecimal) viaInjected.get("result"))
                    .isEqualByComparingTo((BigDecimal) viaSingleton.get("result"));
        }
    }
}
