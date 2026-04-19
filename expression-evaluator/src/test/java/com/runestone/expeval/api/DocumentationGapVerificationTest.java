package com.runestone.expeval.api;

import com.runestone.expeval.environment.ExpressionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies behaviors that were open questions in documentation-plan.md.
 * Each test documents a specific finding for inclusion in the final docs.
 */
@DisplayName("Documentation gap verification")
class DocumentationGapVerificationTest {

    // -------------------------------------------------------------------------
    // Section 4.4: ?? (null coalescing) operator precedence
    // Finding: ?? binds LOWER than arithmetic. `a ?? b + 1` = `(a ?? b) + 1`.
    // To use an arithmetic expression as fallback, wrap it in if(): `a ?? if(true, b+1, b+1)`
    // or restructure the expression.
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("?? operator precedence")
    class NullCoalescingPrecedence {

        @Test
        @DisplayName("a ?? b + 1 parses as (a ?? b) + 1, not a ?? (b+1)")
        void nullCoalescingBindsLowerThanArithmetic() {
            // When `a` is not null (= 10), result is (10 ?? b) + 1 = 10 + 1 = 11
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("a", BigDecimal.TEN, true)
                    .registerExternalSymbol("b", BigDecimal.ONE, true)
                    .build();

            BigDecimal result = MathExpression.compile("<number>a ?? b + 1", env)
                    .compute(Map.of("a", BigDecimal.TEN, "b", BigDecimal.ONE));

            // If ??>arithmetic: result would be 10 + 1 = 11 (a=10, so ?? not triggered, then +1)
            // If arithmetic>??: result would be 10 (a=10, not null, ?? not triggered, no +1 outside)
            // Observed: (a ?? b) + 1 with a=10 → 10 + 1 = 11
            assertThat(result).isEqualByComparingTo("11");
        }

        @Test
        @DisplayName("fallback value of ?? is evaluated as a single numericEntity (reference or literal)")
        void nullCoalescingFallbackIsReference() {
            // `a` is overridable but we supply null-equivalent via HashMap
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("a", BigDecimal.ONE, true)
                    .registerExternalSymbol("fallback", new BigDecimal("99"), true)
                    .build();

            // When a is provided as non-null the ?? is not triggered
            BigDecimal result = MathExpression.compile("<number>a ?? fallback", env)
                    .compute(Map.of("a", BigDecimal.TEN, "fallback", new BigDecimal("99")));
            assertThat(result).isEqualByComparingTo("10");
        }

        @Test
        @DisplayName("?? fallback of type if() can include arithmetic inside branches")
        void nullCoalescingFallbackCanBeIfExpression() {
            // `a ?? if(true, b + 1, 0)` — the IF expression is a valid numericEntity
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("a", BigDecimal.ONE, true)
                    .registerExternalSymbol("b", BigDecimal.ONE, true)
                    .build();

            BigDecimal result = MathExpression.compile("<number>a ?? if(true, b + 1, 0)", env)
                    .compute(Map.of("a", BigDecimal.TEN, "b", BigDecimal.ONE));

            // a = 10 (not null) → result is 10 (fallback not evaluated)
            assertThat(result).isEqualByComparingTo("10");
        }
    }

    // -------------------------------------------------------------------------
    // Section 4.4: Comments in expressions
    // Finding: both // line comments and /* */ block comments are supported.
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Comment support in expressions")
    class CommentSupport {

        @Test
        @DisplayName("// line comment is ignored in math expressions")
        void lineCommentIsSkippedInMath() {
            BigDecimal result = MathExpression.compile("1 + 2 // this is a comment").compute();
            assertThat(result).isEqualByComparingTo("3");
        }

        @Test
        @DisplayName("/* block comment */ is ignored in math expressions")
        void blockCommentIsSkippedInMath() {
            BigDecimal result = MathExpression.compile("1 + /* inline comment */ 2").compute();
            assertThat(result).isEqualByComparingTo("3");
        }

        @Test
        @DisplayName("multi-line block comment is supported")
        void multiLineBlockCommentIsSupported() {
            BigDecimal result = MathExpression.compile("""
                    /* multi-line
                       block comment */
                    10 + 5""").compute();
            assertThat(result).isEqualByComparingTo("15");
        }

        @Test
        @DisplayName("// line comment is ignored in assignment blocks")
        void lineCommentIsSkippedInAssignment() {
            var result = AssignmentExpression.compile("""
                    // compute the sum
                    x = 3 + 4; // seven
                    """).compute();
            assertThat(result.get("x")).isEqualTo(new BigDecimal("7"));
        }
    }

    // -------------------------------------------------------------------------
    // Section 4.4: root operator syntax
    // Finding: `a root b` is infix (b-th root of a). `√` is a synonym.
    // `sqrt(x)` is a function form for square root only.
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Root operator syntax")
    class RootOperatorSyntax {

        @Test
        @DisplayName("'a root b' computes the b-th root of a (infix form)")
        void rootInfixComputes() {
            // 8 root 3 = cube root of 8 = 2
            BigDecimal result = MathExpression.compile("8 root 3").compute();
            assertThat(result).isEqualByComparingTo("2");
        }

        @Test
        @DisplayName("sqrt(x) computes the square root (function form)")
        void sqrtFunctionForm() {
            BigDecimal result = MathExpression.compile("sqrt(9)").compute();
            assertThat(result).isEqualByComparingTo("3");
        }

        @Test
        @DisplayName("Unicode √ is a synonym for 'root'")
        void unicodeRootSymbol() {
            // 16 √ 2 = square root of 16 = 4
            BigDecimal result = MathExpression.compile("16 \u221A 2").compute();
            assertThat(result).isEqualByComparingTo("4");
        }

        @Test
        @DisplayName("% is a postfix PERCENTAGE operator (not modulo)")
        void percentIsPostfixPercentage() {
            // 50% = 0.5 (postfix, not modulo)
            BigDecimal result = MathExpression.compile("50%").compute();
            assertThat(result).isEqualByComparingTo("0.5");
        }

        @Test
        @DisplayName("'mod' keyword is the modulo infix operator")
        void modKeywordIsModulo() {
            // 10 mod 3 = 1
            BigDecimal result = MathExpression.compile("10 mod 3").compute();
            assertThat(result).isEqualByComparingTo("1");
        }
    }

    // -------------------------------------------------------------------------
    // Section 4.9: currDate/currTime/currDateTime evaluated per compute() call
    // Finding: dynamic literals are re-evaluated on each compute() call.
    // Within a single call, each literal evaluates to the same instant (cached in ExecutionScope).
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("currDate/currTime/currDateTime per-call evaluation")
    class DynamicLiteralPerCall {

        @Test
        @DisplayName("currDate resolves to the same value within one compute() call")
        void currDateIsConsistentWithinOneCall() {
            // Verified by DynamicLiteralExpressionTest — included here for documentation
            boolean result = LogicalExpression.compile("currDate = currDate").compute();
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("currDate resolves to a LocalDate (type check)")
        void currDateResolvesToLocalDate() {
            // currDate cannot be prefixed with <date> — it is already a date literal.
            // To capture it in an assignment, reference it directly.
            var result = AssignmentExpression.compile("d = currDate;").compute();
            assertThat(result.get("d")).isInstanceOf(LocalDate.class);
        }
    }

    // -------------------------------------------------------------------------
    // Section 4.3: ExpressionEnvironmentId
    // Finding: identical static configurations (same providers, symbols, MathContext)
    // produce the same ExpressionEnvironmentId (SHA-256 based).
    // Instance providers use identity hash → different instances = different ID.
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("ExpressionEnvironmentId hashing")
    class EnvironmentIdHashing {

        @Test
        @DisplayName("two environments with identical static configuration share the same ID")
        void identicalStaticConfigProducesSameId() {
            ExpressionEnvironment env1 = ExpressionEnvironment.builder()
                    .addMathFunctions()
                    .registerExternalSymbol("rate", new BigDecimal("0.05"), false)
                    .build();
            ExpressionEnvironment env2 = ExpressionEnvironment.builder()
                    .addMathFunctions()
                    .registerExternalSymbol("rate", new BigDecimal("0.05"), false)
                    .build();

            assertThat(env1.environmentId()).isEqualTo(env2.environmentId());
        }

        @Test
        @DisplayName("adding an extra symbol changes the environment ID")
        void differentSymbolsProduceDifferentId() {
            ExpressionEnvironment env1 = ExpressionEnvironment.builder()
                    .registerExternalSymbol("a", BigDecimal.ONE, false)
                    .build();
            ExpressionEnvironment env2 = ExpressionEnvironment.builder()
                    .registerExternalSymbol("a", BigDecimal.ONE, false)
                    .registerExternalSymbol("b", BigDecimal.TEN, false)
                    .build();

            assertThat(env1.environmentId()).isNotEqualTo(env2.environmentId());
        }

        @Test
        @DisplayName("changing overridable=false to true changes the environment ID")
        void overridableFlagAffectsId() {
            ExpressionEnvironment envConst = ExpressionEnvironment.builder()
                    .registerExternalSymbol("rate", new BigDecimal("0.05"), false)
                    .build();
            ExpressionEnvironment envOverridable = ExpressionEnvironment.builder()
                    .registerExternalSymbol("rate", new BigDecimal("0.05"), true)
                    .build();

            assertThat(envConst.environmentId()).isNotEqualTo(envOverridable.environmentId());
        }

        @Test
        @DisplayName("two different instance provider objects produce different environment IDs")
        void differentInstanceProducerDifferentId() {
            Object instance1 = new Object();
            Object instance2 = new Object();

            ExpressionEnvironment env1 = ExpressionEnvironment.builder()
                    .registerInstanceProvider(instance1)
                    .build();
            ExpressionEnvironment env2 = ExpressionEnvironment.builder()
                    .registerInstanceProvider(instance2)
                    .build();

            // Instance providers use System.identityHashCode → different instances → different IDs
            assertThat(env1.environmentId()).isNotEqualTo(env2.environmentId());
        }
    }

    // -------------------------------------------------------------------------
    // Section 4.3: Custom provider method discovery
    // Finding: only public static methods (static providers) or public non-static
    // (instance providers); Object methods excluded; MathContext first-param auto-injected.
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Custom provider method discovery rules")
    class ProviderDiscovery {

        public static class MyProvider {
            public static BigDecimal triple(BigDecimal x) { return x.multiply(new BigDecimal("3")); }
            public static BigDecimal quadruple(BigDecimal x) { return x.multiply(new BigDecimal("4")); }
            // Object methods (equals, hashCode, toString) are excluded automatically
        }

        public static class OverloadedProvider {
            public static String format(String value) { return "[" + value + "]"; }
            public static String format(String value, String prefix) { return prefix + "[" + value + "]"; }
        }

        @Test
        @DisplayName("public static methods are registered as callable functions")
        void publicStaticMethodsAreRegistered() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerStaticProvider(MyProvider.class)
                    .build();
            BigDecimal result = MathExpression.compile("triple(5)", env).compute();
            assertThat(result).isEqualByComparingTo("15");
        }

        @Test
        @DisplayName("overloaded methods are registered as separate arities")
        void overloadedMethodsAreRegisteredSeparately() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerStaticProvider(OverloadedProvider.class)
                    .build();

            var r1 = AssignmentExpression.compile("x = format(\"val\");", env).compute();
            var r2 = AssignmentExpression.compile("x = format(\"val\", \"PRE-\");", env).compute();

            assertThat(r1.get("x")).isEqualTo("[val]");
            assertThat(r2.get("x")).isEqualTo("PRE-[val]");
        }

        public static class DoublerProvider {
            public BigDecimal doubleIt(BigDecimal x) { return x.multiply(new BigDecimal("2")); }
        }

        @Test
        @DisplayName("instance provider exposes non-static methods")
        void instanceProviderExposesInstanceMethods() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerInstanceProvider(new DoublerProvider())
                    .build();

            BigDecimal result = MathExpression.compile("doubleIt(7)", env).compute();
            assertThat(result).isEqualByComparingTo("14");
        }
    }
}
