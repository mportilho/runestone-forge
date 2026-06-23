package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.CompilationIssue;
import com.runestone.expeval.api.IssueCode;
import com.runestone.expeval.api.SemanticResolutionException;
import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.environment.ExpressionEnvironmentBuilder;
import com.runestone.expeval.internal.grammar.ExpressionResultType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionCompilerTest {

    private final ExpressionCompiler compiler = new ExpressionCompiler();

    @Test
    void shouldRejectUnknownFunctionsDuringCompilation() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();

        assertThatThrownBy(() -> compiler.compile("missing() + 1", ExpressionResultType.MATH, environment))
            .isInstanceOf(SemanticResolutionException.class)
            .extracting(exception -> ((SemanticResolutionException) exception).issues())
            .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.list(CompilationIssue.class))
            .extracting(CompilationIssue::code)
            .contains(IssueCode.UNKNOWN_FUNCTION);
    }

    @Test
    void shouldRejectInvalidFunctionArityDuringCompilation() {
        ExpressionEnvironment environment = new ExpressionEnvironmentBuilder()
            .registerStaticProvider(ProviderFixture.class)
            .build();

        assertThatThrownBy(() -> compiler.compile("bonus(1, 2)", ExpressionResultType.MATH, environment))
            .isInstanceOf(SemanticResolutionException.class)
            .extracting(exception -> ((SemanticResolutionException) exception).issues())
            .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.list(CompilationIssue.class))
            .extracting(CompilationIssue::code)
            .contains(IssueCode.INVALID_FUNCTION_ARITY);
    }

    @Test
    void shouldRejectAmbiguousOverloadsDuringCompilation() {
        ExpressionEnvironment environment = new ExpressionEnvironmentBuilder()
            .registerStaticProvider(ProviderFixture.class)
            .build();

        assertThatThrownBy(() -> compiler.compile("pick(value) + 1", ExpressionResultType.MATH, environment))
            .isInstanceOf(SemanticResolutionException.class)
            .extracting(exception -> ((SemanticResolutionException) exception).issues())
            .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.list(CompilationIssue.class))
            .extracting(CompilationIssue::code)
            .contains(IssueCode.AMBIGUOUS_FUNCTION);
    }

    @Test
    void shouldCompileRepeatedlyForTheSameEnvironment() {
        ExpressionEnvironment environment = new ExpressionEnvironmentBuilder()
            .registerStaticProvider(ProviderFixture.class)
            .registerExternalSymbol("principal", BigDecimal.ONE, true)
            .build();

        CompiledExpression first = compiler.compile("bonus(principal) + 1", ExpressionResultType.MATH, environment);
        CompiledExpression second = compiler.compile("bonus(principal) + 1", ExpressionResultType.MATH, environment);

        assertThat(first).isNotNull();
        assertThat(second).isNotSameAs(first);
    }

    @Test
    void shouldCompileEquivalentEnvironmentsBuiltIndependently() {
        ExpressionEnvironment env1 = new ExpressionEnvironmentBuilder()
            .registerStaticProvider(ProviderFixture.class)
            .registerExternalSymbol("principal", BigDecimal.ONE, true)
            .build();
        ExpressionEnvironment env2 = new ExpressionEnvironmentBuilder()
            .registerStaticProvider(ProviderFixture.class)
            .registerExternalSymbol("principal", BigDecimal.ONE, true)
            .build();

        CompiledExpression first = compiler.compile("bonus(principal) + 1", ExpressionResultType.MATH, env1);
        CompiledExpression second = compiler.compile("bonus(principal) + 1", ExpressionResultType.MATH, env2);

        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
    }

    @Test
    void shouldCompileExpressionWithFoldedPropertyChain() {
        ExpressionEnvironment environment = new ExpressionEnvironmentBuilder()
            .registerExternalSymbol("prices", List.of(BigDecimal.ONE, BigDecimal.TEN), false)
            .build();

        CompiledExpression result = compiler.compile("prices[1]", ExpressionResultType.MATH, environment);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldCompileDifferentInstanceProviderInstances() {
        ExpressionEnvironment env1 = new ExpressionEnvironmentBuilder()
            .registerInstanceProvider(new InstanceProviderFixture(BigDecimal.ONE))
            .build();
        ExpressionEnvironment env2 = new ExpressionEnvironmentBuilder()
            .registerInstanceProvider(new InstanceProviderFixture(BigDecimal.TEN))
            .build();

        CompiledExpression first = compiler.compile("multiply(2)", ExpressionResultType.MATH, env1);
        CompiledExpression second = compiler.compile("multiply(2)", ExpressionResultType.MATH, env2);

        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
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
