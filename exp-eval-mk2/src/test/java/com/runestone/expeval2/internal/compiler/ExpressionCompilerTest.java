package com.runestone.expeval2.internal.compiler;

import com.runestone.expeval2.api.CompilationIssue;
import com.runestone.expeval2.api.ExpressionCompilationException;
import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.environment.ExpressionEnvironmentBuilder;
import com.runestone.expeval2.internal.grammar.ExpressionResultType;
import com.runestone.expeval2.internal.runtime.CompiledExpression;
import com.runestone.expeval2.internal.runtime.ExpressionCompiler;
import com.runestone.expeval2.internal.runtime.ExpressionRuntimeSupport;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionCompilerTest {

    private final ExpressionCompiler compiler = new ExpressionCompiler();

    @Test
    void shouldRejectUnknownFunctionsDuringCompilation() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();

        assertThatThrownBy(() -> ExpressionRuntimeSupport.compile("missing() + 1", ExpressionResultType.MATH, environment))
            .isInstanceOf(ExpressionCompilationException.class)
            .extracting(exception -> ((ExpressionCompilationException) exception).issues())
            .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.list(CompilationIssue.class))
            .extracting(CompilationIssue::code)
            .contains("UNKNOWN_FUNCTION");
    }

    @Test
    void shouldRejectInvalidFunctionArityDuringCompilation() {
        ExpressionEnvironment environment = new ExpressionEnvironmentBuilder()
            .registerStaticProvider(ProviderFixture.class)
            .build();

        assertThatThrownBy(() -> ExpressionRuntimeSupport.compile("bonus(1, 2)", ExpressionResultType.MATH, environment))
            .isInstanceOf(ExpressionCompilationException.class)
            .extracting(exception -> ((ExpressionCompilationException) exception).issues())
            .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.list(CompilationIssue.class))
            .extracting(CompilationIssue::code)
            .contains("INVALID_FUNCTION_ARITY");
    }

    @Test
    void shouldRejectAmbiguousOverloadsDuringCompilation() {
        ExpressionEnvironment environment = new ExpressionEnvironmentBuilder()
            .registerStaticProvider(ProviderFixture.class)
            .build();

        assertThatThrownBy(() -> ExpressionRuntimeSupport.compile("pick(value) + 1", ExpressionResultType.MATH, environment))
            .isInstanceOf(ExpressionCompilationException.class)
            .extracting(exception -> ((ExpressionCompilationException) exception).issues())
            .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.list(CompilationIssue.class))
            .extracting(CompilationIssue::code)
            .contains("AMBIGUOUS_FUNCTION");
    }

    @Test
    void shouldReuseCompiledExpressionsFromTheCacheForTheSameEnvironment() {
        ExpressionEnvironment environment = new ExpressionEnvironmentBuilder()
            .registerStaticProvider(ProviderFixture.class)
            .registerExternalSymbol("principal", com.runestone.expeval2.types.ScalarType.NUMBER, BigDecimal.ONE, true)
            .build();

        CompiledExpression first = compiler.compile("bonus(principal) + 1", ExpressionResultType.MATH, environment);
        CompiledExpression second = compiler.compile("bonus(principal) + 1", ExpressionResultType.MATH, environment);

        assertThat(second).isSameAs(first);
    }

    @Test
    void shouldShareCacheAcrossEquivalentEnvironmentsBuiltIndependently() {
        ExpressionEnvironment env1 = new ExpressionEnvironmentBuilder()
            .registerStaticProvider(ProviderFixture.class)
            .registerExternalSymbol("principal", com.runestone.expeval2.types.ScalarType.NUMBER, BigDecimal.ONE, true)
            .build();
        ExpressionEnvironment env2 = new ExpressionEnvironmentBuilder()
            .registerStaticProvider(ProviderFixture.class)
            .registerExternalSymbol("principal", com.runestone.expeval2.types.ScalarType.NUMBER, BigDecimal.ONE, true)
            .build();

        CompiledExpression first = compiler.compile("bonus(principal) + 1", ExpressionResultType.MATH, env1);
        CompiledExpression second = compiler.compile("bonus(principal) + 1", ExpressionResultType.MATH, env2);

        assertThat(second).isSameAs(first);
    }

    @Test
    void shouldNotShareCacheAcrossDifferentInstanceProviderInstances() {
        ExpressionEnvironment env1 = new ExpressionEnvironmentBuilder()
            .registerInstanceProvider(new InstanceProviderFixture(BigDecimal.ONE))
            .build();
        ExpressionEnvironment env2 = new ExpressionEnvironmentBuilder()
            .registerInstanceProvider(new InstanceProviderFixture(BigDecimal.TEN))
            .build();

        CompiledExpression first = compiler.compile("multiply(2)", ExpressionResultType.MATH, env1);
        CompiledExpression second = compiler.compile("multiply(2)", ExpressionResultType.MATH, env2);

        assertThat(second).isNotSameAs(first);
    }

    public static final class ProviderFixture {

        private ProviderFixture() {
        }

        public static BigDecimal bonus(BigDecimal principal) {
            return principal.multiply(BigDecimal.valueOf(0.10));
        }

        public static BigDecimal pick(BigDecimal value) {
            return value;
        }

        public static BigDecimal pick(String value) {
            return BigDecimal.valueOf(value.length());
        }
    }

    public static final class InstanceProviderFixture {

        private final BigDecimal factor;

        InstanceProviderFixture(BigDecimal factor) {
            this.factor = factor;
        }

        public BigDecimal multiply(BigDecimal x) {
            return x.multiply(factor);
        }
    }
}
