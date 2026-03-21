package com.runestone.expeval2.environment;

import com.runestone.expeval2.api.AuditEvent;
import com.runestone.expeval2.api.AuditResult;
import com.runestone.expeval2.api.MathExpression;
import com.runestone.expeval2.internal.runtime.ExpressionCompiler;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link ExpressionEnvironmentBuilder#registerInstanceProvider(Object, boolean)}
 * with {@code foldable=true}.
 *
 * <p>Each test that needs to verify fold-time invocation uses a fresh {@link ExpressionCompiler}
 * to guarantee a cache miss and ensure the function is actually invoked during compilation.
 *
 * <p>Skipped categories:
 * <ul>
 *   <li>Concurrency — fold and evaluation are stateless per expression scope.
 *   <li>Security / Authorization — not applicable to the function registration API.
 * </ul>
 */
@DisplayName("ExpressionEnvironmentBuilder — registerInstanceProvider with foldable=true")
class ExpressionEnvironmentBuilderFoldableInstanceProviderTest {

    private static final Offset<BigDecimal> EPSILON = within(new BigDecimal("0.00000001"));

    // -----------------------------------------------------------------------
    // Fixtures
    // -----------------------------------------------------------------------

    /** Stateless instance provider — safe to fold. */
    static final class DoubleFixture {
        public BigDecimal twice(BigDecimal x) {
            return x.multiply(BigDecimal.TWO);
        }
    }

    /** Instance provider that captures construction-time state — fold uses the state at build time. */
    static final class MultiplierFixture {
        private final BigDecimal factor;

        MultiplierFixture(BigDecimal factor) {
            this.factor = factor;
        }

        public BigDecimal multiply(BigDecimal x) {
            return x.multiply(factor);
        }
    }

    /** Counted instance provider to verify exact invocation count. */
    static final class CountedInstanceFixture {
        final AtomicInteger callCount = new AtomicInteger(0);

        public BigDecimal counted(BigDecimal x) {
            callCount.incrementAndGet();
            return x;
        }
    }

    // -----------------------------------------------------------------------
    // API contract — registration
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Registration API")
    class RegistrationApi {

        @Test
        @DisplayName("rejects null instance with NullPointerException")
        void rejectsNullInstance() {
            assertThatThrownBy(() -> ExpressionEnvironment.builder().registerInstanceProvider(null, true))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("returns the builder for fluent chaining")
        void returnsBuilderForChaining() {
            ExpressionEnvironmentBuilder builder = ExpressionEnvironment.builder();

            ExpressionEnvironmentBuilder returned = builder.registerInstanceProvider(new DoubleFixture(), true);

            assertThat(returned).isSameAs(builder);
        }

        @Test
        @DisplayName("registerInstanceProvider(instance) without flag defaults to foldable=false")
        void noFlagDefaultsToNonFoldable() {
            CountedInstanceFixture fixture = new CountedInstanceFixture();
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerInstanceProvider(fixture)   // no foldable flag
                    .build();

            MathExpression.compile("counted(5)", env, new ExpressionCompiler());

            assertThat(fixture.callCount.get())
                    .as("non-foldable instance provider must not be called at build time")
                    .isEqualTo(0);
        }
    }

    // -----------------------------------------------------------------------
    // Folding behaviour — instance state captured at build time
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Folding — literal arguments")
    class FoldingWithLiteralArgs {

        @Test
        @DisplayName("compute() returns the correct value when the instance method is folded")
        void computeReturnsCorrectValueWhenFolded() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerInstanceProvider(new DoubleFixture(), true)
                    .build();

            BigDecimal result = MathExpression.compile("twice(7)", env).compute();

            assertThat(result).isCloseTo(new BigDecimal("14"), EPSILON);
        }

        @Test
        @DisplayName("instance state at construction time is used for the folded result")
        void instanceStateAtConstructionTimeIsUsedForFoldedResult() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerInstanceProvider(new MultiplierFixture(new BigDecimal("3")), true)
                    .build();

            BigDecimal result = MathExpression.compile("multiply(4)", env).compute();

            assertThat(result).isCloseTo(new BigDecimal("12"), EPSILON);
        }

        @Test
        @DisplayName("folded result is identical across multiple compute() calls")
        void resultIsStableAcrossMultipleComputeCalls() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerInstanceProvider(new MultiplierFixture(new BigDecimal("5")), true)
                    .build();
            MathExpression expr = MathExpression.compile("multiply(3)", env);

            BigDecimal first = expr.compute();
            BigDecimal second = expr.compute();
            BigDecimal third = expr.compute();

            assertThat(first).isEqualByComparingTo(second);
            assertThat(second).isEqualByComparingTo(third);
        }
    }

    // -----------------------------------------------------------------------
    // Invocation count — the core folding contract for instance providers
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Invocation count — instance method must not be called during compute()")
    class InvocationCount {

        private CountedInstanceFixture fixture;
        private ExpressionCompiler freshCompiler;

        @BeforeEach
        void setUp() {
            fixture = new CountedInstanceFixture();
            freshCompiler = new ExpressionCompiler();
        }

        @Test
        @DisplayName("foldable instance method with literal arg is invoked once at build time, never during compute()")
        void foldableInstanceMethodInvokedOnceAtBuildTimeNeverDuringCompute() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerInstanceProvider(fixture, true)
                    .build();

            MathExpression expr = MathExpression.compile("counted(9)", env, freshCompiler);
            assertThat(fixture.callCount.get())
                    .as("must be invoked exactly once at fold time")
                    .isEqualTo(1);

            expr.compute();
            expr.compute();
            expr.compute();
            assertThat(fixture.callCount.get())
                    .as("must not be invoked again during compute()")
                    .isEqualTo(1);
        }

        @Test
        @DisplayName("non-foldable instance method with literal arg is invoked on every compute() call")
        void nonFoldableInstanceMethodInvokedOnEachCompute() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerInstanceProvider(fixture, false)
                    .build();

            MathExpression expr = MathExpression.compile("counted(9)", env, freshCompiler);
            assertThat(fixture.callCount.get())
                    .as("must not be invoked at build time")
                    .isEqualTo(0);

            expr.compute();
            expr.compute();
            expr.compute();
            assertThat(fixture.callCount.get())
                    .as("must be invoked once per compute()")
                    .isEqualTo(3);
        }

        @Test
        @DisplayName("foldable instance method with variable arg is NOT folded — invoked on every compute()")
        void foldableInstanceMethodWithVariableArgIsNotFolded() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerInstanceProvider(fixture, true)
                    .build();

            MathExpression expr = MathExpression.compile("counted(x)", env, freshCompiler)
                    .setValue("x", new BigDecimal("5"));
            assertThat(fixture.callCount.get())
                    .as("must not be invoked at build time when arg is a variable")
                    .isEqualTo(0);

            expr.compute();
            expr.compute();
            assertThat(fixture.callCount.get())
                    .as("must be invoked on each compute() when arg is a variable")
                    .isEqualTo(2);
        }
    }

    // -----------------------------------------------------------------------
    // Audit trail — folded instance calls emit FunctionCall events
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Audit trail — folded instance method emits FunctionCall event")
    class AuditTrail {

        @Test
        @DisplayName("computeWithAudit() emits one FunctionCall event for the folded instance method")
        void emitsOneFunctionCallEventForFoldedInstanceMethod() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerInstanceProvider(new DoubleFixture(), true)
                    .build();

            AuditResult<BigDecimal> result = MathExpression.compile("twice(6)", env).computeWithAudit();

            assertThat(result.trace().functionCalls()).hasSize(1);
            AuditEvent.FunctionCall call = result.trace().functionCalls().getFirst();
            assertThat(call.functionName()).isEqualTo("twice");
            assertThat((BigDecimal) call.result()).isCloseTo(new BigDecimal("12"), EPSILON);
        }

        @Test
        @DisplayName("audit result value matches compute() value for a folded instance method")
        void auditValueMatchesComputeValue() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerInstanceProvider(new MultiplierFixture(new BigDecimal("4")), true)
                    .build();
            MathExpression expr = MathExpression.compile("multiply(3)", env);

            assertThat(expr.computeWithAudit().value()).isCloseTo(expr.compute(), EPSILON);
        }
    }

    // -----------------------------------------------------------------------
    // Coexistence — foldable instance + other providers
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Coexistence — foldable instance provider alongside other registrations")
    class Coexistence {

        @Test
        @DisplayName("foldable instance provider coexists with a non-foldable instance provider")
        void foldableAndNonFoldableInstanceProviderCoexist() {
            CountedInstanceFixture foldable = new CountedInstanceFixture();
            CountedInstanceFixture nonFoldable = new CountedInstanceFixture();

            // Both fixtures expose 'counted(BigDecimal)', so rename one via a subclass to avoid name clash
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerInstanceProvider(new DoubleFixture(), true)
                    .registerInstanceProvider(new MultiplierFixture(new BigDecimal("3")), false)
                    .build();
            ExpressionCompiler freshCompiler = new ExpressionCompiler();

            BigDecimal twiceResult = MathExpression.compile("twice(5)", env, freshCompiler).compute();
            BigDecimal multiplyResult = MathExpression.compile("multiply(5)", env, freshCompiler).compute();

            assertThat(twiceResult).isCloseTo(new BigDecimal("10"), EPSILON);
            assertThat(multiplyResult).isCloseTo(new BigDecimal("15"), EPSILON);
        }

        @Test
        @DisplayName("foldable instance provider coexists with a foldable static provider")
        void foldableInstanceAndFoldableStaticProviderCoexist() {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerInstanceProvider(new DoubleFixture(), true)
                    .addMathFunctions()
                    .build();

            // ln(1) is from the static math catalog (folded), twice(3) is from the instance (folded)
            BigDecimal lnResult = MathExpression.compile("ln(1)", env).compute();
            BigDecimal twiceResult = MathExpression.compile("twice(3)", env).compute();

            assertThat(lnResult).isCloseTo(BigDecimal.ZERO, EPSILON);
            assertThat(twiceResult).isCloseTo(new BigDecimal("6"), EPSILON);
        }
    }
}
