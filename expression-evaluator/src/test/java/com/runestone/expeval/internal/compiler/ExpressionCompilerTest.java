package com.runestone.expeval.internal.compiler;

import com.runestone.expeval.api.CompilationIssue;
import com.runestone.expeval.api.ExpressionCompilationException;
import com.runestone.expeval.api.IssueCode;
import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.environment.ExpressionEnvironmentBuilder;
import com.runestone.expeval.internal.grammar.ExpressionResultType;
import com.runestone.expeval.internal.runtime.CompiledExpression;
import com.runestone.expeval.compiler.ExpressionCompiler;
import com.runestone.expeval.internal.runtime.ExpressionRuntimeSupport;
import com.runestone.expeval.testing.ExpressionCompilerInspector;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionCompilerTest {

    private final ExpressionCompiler compiler = new ExpressionCompiler();
    private final ExpressionCompilerInspector inspector = new ExpressionCompilerInspector(compiler);

    @Test
    void shouldRejectUnknownFunctionsDuringCompilation() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();

        assertThatThrownBy(() -> ExpressionRuntimeSupport.compile("missing() + 1", ExpressionResultType.MATH, environment))
            .isInstanceOf(ExpressionCompilationException.class)
            .extracting(exception -> ((ExpressionCompilationException) exception).issues())
            .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.list(CompilationIssue.class))
            .extracting(CompilationIssue::code)
            .contains(IssueCode.UNKNOWN_FUNCTION);
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
            .contains(IssueCode.INVALID_FUNCTION_ARITY);
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
            .contains(IssueCode.AMBIGUOUS_FUNCTION);
    }

    @Test
    void shouldReuseCompiledExpressionsFromTheCacheForTheSameEnvironment() {
        ExpressionEnvironment environment = new ExpressionEnvironmentBuilder()
            .registerStaticProvider(ProviderFixture.class)
            .registerExternalSymbol("principal", BigDecimal.ONE, true)
            .build();

        CompiledExpression first = inspector.compileMath("bonus(principal) + 1", environment);
        CompiledExpression second = inspector.compileMath("bonus(principal) + 1", environment);

        assertThat(second).isSameAs(first);
    }

    @Test
    void shouldShareCacheAcrossEquivalentEnvironmentsBuiltIndependently() {
        ExpressionEnvironment env1 = new ExpressionEnvironmentBuilder()
            .registerStaticProvider(ProviderFixture.class)
            .registerExternalSymbol("principal", BigDecimal.ONE, true)
            .build();
        ExpressionEnvironment env2 = new ExpressionEnvironmentBuilder()
            .registerStaticProvider(ProviderFixture.class)
            .registerExternalSymbol("principal", BigDecimal.ONE, true)
            .build();

        CompiledExpression first = inspector.compileMath("bonus(principal) + 1", env1);
        CompiledExpression second = inspector.compileMath("bonus(principal) + 1", env2);

        assertThat(second).isSameAs(first);
    }

    @Test
    void shouldCacheCompiledExpressionWithFoldedPropertyChain() {
        ExpressionEnvironment environment = new ExpressionEnvironmentBuilder()
            .registerExternalSymbol("prices", List.of(BigDecimal.ONE, BigDecimal.TEN), false)
            .build();

        CompiledExpression first = inspector.compileMath("prices[1]", environment);
        CompiledExpression second = inspector.compileMath("prices[1]", environment);

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

        CompiledExpression first = inspector.compileMath("multiply(2)", env1);
        CompiledExpression second = inspector.compileMath("multiply(2)", env2);

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
