package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.internal.grammar.ExpressionResultType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.MathContext;

import static org.assertj.core.api.Assertions.assertThat;

class ExpressionRuntimeSupportCacheTest {

    @Test
    void shouldReuseCompiledExpressionAcrossCallsForSameSourceAndEnvironment() throws Exception {
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();

        ExpressionRuntimeSupport first = ExpressionRuntimeSupport.compile("1 + 2", ExpressionResultType.MATH, environment);
        ExpressionRuntimeSupport second = ExpressionRuntimeSupport.compile("1 + 2", ExpressionResultType.MATH, environment);

        assertThat(first).isNotSameAs(second);
        assertThat(compiledExpressionOf(second)).isSameAs(compiledExpressionOf(first));
    }

    @Test
    void shouldNotReuseCompiledExpressionForDifferentSources() throws Exception {
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();

        ExpressionRuntimeSupport first = ExpressionRuntimeSupport.compile("1 + 2", ExpressionResultType.MATH, environment);
        ExpressionRuntimeSupport second = ExpressionRuntimeSupport.compile("3 + 4", ExpressionResultType.MATH, environment);

        assertThat(compiledExpressionOf(second)).isNotSameAs(compiledExpressionOf(first));
    }

    @Test
    void shouldNotReuseCompiledExpressionForDifferentEnvironments() throws Exception {
        ExpressionEnvironment env1 = ExpressionEnvironment.builder().build();
        ExpressionEnvironment env2 = ExpressionEnvironment.builder().withMathContext(MathContext.DECIMAL64).build();

        ExpressionRuntimeSupport first = ExpressionRuntimeSupport.compile("1 + 2", ExpressionResultType.MATH, env1);
        ExpressionRuntimeSupport second = ExpressionRuntimeSupport.compile("1 + 2", ExpressionResultType.MATH, env2);

        assertThat(compiledExpressionOf(second)).isNotSameAs(compiledExpressionOf(first));
    }

    private static Object compiledExpressionOf(ExpressionRuntimeSupport runtime) throws Exception {
        Field field = ExpressionRuntimeSupport.class.getDeclaredField("compiledExpression");
        field.setAccessible(true);
        return field.get(runtime);
    }
}
