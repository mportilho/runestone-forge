package com.runestone.expeval.api;

import com.runestone.expeval.environment.ExpressionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies functional correctness of explicit-engine compilation for
 * {@link MathExpression}, {@link LogicalExpression}, and {@link AssignmentExpression}.
 *
 * <p>Cache-sharing and cache-isolation behaviour between an injected engine and the JVM-wide
 * singleton are verified at a lower level in {@code ExpressionRuntimeSupportLifecycleTest}.
 * Tests here focus on the public API contract: correct evaluation results and null-safety.
 */
@DisplayName("Explicit engine injection — public API")
class ExpressionEngineInjectionTest {

    private static final ExpressionEnvironment ENV = ExpressionEnvironment.builder().build();

    // --- MathExpression ---

    @Nested
    @DisplayName("MathExpression with explicit engine")
    class MathExpressionOverload {

        @Test
        @DisplayName("evaluates a simple arithmetic expression correctly")
        void evaluatesArithmeticExpression() {
            var engine = new ExpressionEngine(CacheConfig.defaults());

            BigDecimal result = engine.compileMath("3 + 4", ENV).compute();

            assertThat(result).isEqualByComparingTo("7");
        }

        @Test
        @DisplayName("evaluates an expression with a bound variable")
        void evaluatesExpressionWithBoundVariable() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("x", BigDecimal.TEN, true)
                    .build();
            var engine = new ExpressionEngine(CacheConfig.defaults());

            BigDecimal result = engine.compileMath("x * 2", env)
                    .compute(Map.of("x", 5));

            assertThat(result).isEqualByComparingTo("10");
        }

        @Test
        @DisplayName("rejects null engine")
        void rejectsNullEngine() {
            assertThatThrownBy(() -> MathExpression.compile("1 + 1", ENV, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("produces the same result as the singleton overload")
        void producesSameResultAsSingletonOverload() {
            var engine = new ExpressionEngine(CacheConfig.defaults());
            String source = "12 * 12";

            BigDecimal viaInjected = MathExpression.compile(source, ENV, engine).compute();
            BigDecimal viaSingleton = MathExpression.compile(source, ENV).compute();

            assertThat(viaInjected).isEqualByComparingTo(viaSingleton);
        }

    }

    // --- LogicalExpression ---

    @Nested
    @DisplayName("LogicalExpression with explicit engine")
    class LogicalExpressionOverload {

        @Test
        @DisplayName("evaluates a simple comparison correctly")
        void evaluatesSimpleComparison() {
            var engine = new ExpressionEngine(CacheConfig.defaults());

            boolean result = engine.compileLogical("5 > 3", ENV).compute();

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("evaluates a false comparison correctly")
        void evaluatesFalseComparison() {
            var engine = new ExpressionEngine(CacheConfig.defaults());

            boolean result = engine.compileLogical("5 < 3", ENV).compute();

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("rejects null engine")
        void rejectsNullEngine() {
            assertThatThrownBy(() -> LogicalExpression.compile("1 > 0", ENV, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("produces the same result as the singleton overload")
        void producesSameResultAsSingletonOverload() {
            var engine = new ExpressionEngine(CacheConfig.defaults());
            String source = "10 >= 10";

            boolean viaInjected = LogicalExpression.compile(source, ENV, engine).compute();
            boolean viaSingleton = LogicalExpression.compile(source, ENV).compute();

            assertThat(viaInjected).isEqualTo(viaSingleton);
        }
    }

    // --- AssignmentExpression ---

    @Nested
    @DisplayName("AssignmentExpression with explicit engine")
    class AssignmentExpressionOverload {

        @Test
        @DisplayName("evaluates a single assignment correctly")
        void evaluatesSingleAssignment() {
            var engine = new ExpressionEngine(CacheConfig.defaults());

            Map<String, Object> result = engine.compileAssignments("total = 6 * 7;", ENV).compute();

            assertThat(result).containsKey("total");
            assertThat((BigDecimal) result.get("total")).isEqualByComparingTo("42");
        }

        @Test
        @DisplayName("evaluates multiple assignments correctly")
        void evaluatesMultipleAssignments() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("rate", new BigDecimal("0.10"), true)
                    .build();
            var engine = new ExpressionEngine(CacheConfig.defaults());

            Map<String, Object> result = AssignmentExpression
                    .compile("base = 100; tax = base * rate;", env, engine)
                    .compute(Map.of("rate", new BigDecimal("0.10")));

            assertThat(result).containsKey("base").containsKey("tax");
            assertThat((BigDecimal) result.get("base")).isEqualByComparingTo("100");
            assertThat((BigDecimal) result.get("tax")).isEqualByComparingTo("10.0");
        }

        @Test
        @DisplayName("rejects null engine")
        void rejectsNullEngine() {
            assertThatThrownBy(() -> AssignmentExpression.compile("x = 1", ENV, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("produces the same result as the singleton overload")
        void producesSameResultAsSingletonOverload() {
            var engine = new ExpressionEngine(CacheConfig.defaults());
            String source = "result = 9 * 9;";

            Map<String, Object> viaInjected = AssignmentExpression.compile(source, ENV, engine).compute();
            Map<String, Object> viaSingleton = AssignmentExpression.compile(source, ENV).compute();

            assertThat((BigDecimal) viaInjected.get("result"))
                    .isEqualByComparingTo((BigDecimal) viaSingleton.get("result"));
        }
    }
}
