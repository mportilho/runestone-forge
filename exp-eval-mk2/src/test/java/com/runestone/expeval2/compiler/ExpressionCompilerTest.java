package com.runestone.expeval2.compiler;

import com.runestone.expeval2.api.ExpressionEnvironment;
import com.runestone.expeval2.api.ExpressionEnvironmentBuilder;
import com.runestone.expeval2.grammar.language.ExpressionResultType;
import com.runestone.expeval2.semantic.SemanticIssue;
import com.runestone.expeval2.semantic.SemanticResolutionException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

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
            .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.list(SemanticIssue.class))
            .extracting(SemanticIssue::code)
            .contains("UNKNOWN_FUNCTION");
    }

    @Test
    void shouldRejectInvalidFunctionArityDuringCompilation() {
        ExpressionEnvironment environment = new ExpressionEnvironmentBuilder()
            .registerStaticProvider(ProviderFixture.class)
            .build();

        assertThatThrownBy(() -> compiler.compile("bonus(1, 2)", ExpressionResultType.MATH, environment))
            .isInstanceOf(SemanticResolutionException.class)
            .extracting(exception -> ((SemanticResolutionException) exception).issues())
            .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.list(SemanticIssue.class))
            .extracting(SemanticIssue::code)
            .contains("INVALID_FUNCTION_ARITY");
    }

    @Test
    void shouldRejectAmbiguousOverloadsDuringCompilation() {
        ExpressionEnvironment environment = new ExpressionEnvironmentBuilder()
            .registerStaticProvider(ProviderFixture.class)
            .build();

        assertThatThrownBy(() -> compiler.compile("pick(value) + 1", ExpressionResultType.MATH, environment))
            .isInstanceOf(SemanticResolutionException.class)
            .extracting(exception -> ((SemanticResolutionException) exception).issues())
            .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.list(SemanticIssue.class))
            .extracting(SemanticIssue::code)
            .contains("AMBIGUOUS_FUNCTION");
    }

    @Test
    void shouldReuseCompiledExpressionsFromTheCacheForTheSameEnvironment() {
        ExpressionEnvironment environment = new ExpressionEnvironmentBuilder()
            .registerStaticProvider(ProviderFixture.class)
            .registerExternalSymbol("principal", com.runestone.expeval2.semantic.ScalarType.NUMBER, BigDecimal.ONE, true)
            .build();

        CompiledExpression first = compiler.compile("bonus(principal) + 1", ExpressionResultType.MATH, environment);
        CompiledExpression second = compiler.compile("bonus(principal) + 1", ExpressionResultType.MATH, environment);

        assertThat(second).isSameAs(first);
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
}
