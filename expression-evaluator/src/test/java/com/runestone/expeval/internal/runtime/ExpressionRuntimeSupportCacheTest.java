package com.runestone.expeval.internal.runtime;

import com.runestone.converters.DataConversionService;
import com.runestone.expeval.api.CacheConfig;
import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.internal.grammar.ExpressionResultType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExpressionRuntimeSupportCacheTest {

    @Test
    void shouldReuseRuntimeSupportAcrossCallsForSameSourceAndEnvironment() throws Exception {
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();

        ExpressionCompilationCache cache = new ExpressionCompilationCache(CacheConfig.defaults());
        ExpressionRuntimeSupport first = cache.compileRuntime("1 + 2", ExpressionResultType.MATH, environment);
        ExpressionRuntimeSupport second = cache.compileRuntime("1 + 2", ExpressionResultType.MATH, environment);

        assertThat(second).isSameAs(first);
        assertThat(compiledExpressionOf(second)).isSameAs(compiledExpressionOf(first));
    }

    @Test
    void shouldNotReuseCompiledExpressionForDifferentSources() throws Exception {
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();

        ExpressionCompilationCache cache = new ExpressionCompilationCache(CacheConfig.defaults());
        ExpressionRuntimeSupport first = cache.compileRuntime("1 + 2", ExpressionResultType.MATH, environment);
        ExpressionRuntimeSupport second = cache.compileRuntime("3 + 4", ExpressionResultType.MATH, environment);

        assertThat(second).isNotSameAs(first);
        assertThat(compiledExpressionOf(second)).isNotSameAs(compiledExpressionOf(first));
    }

    @Test
    void shouldNotReuseCompiledExpressionForDifferentEnvironments() throws Exception {
        ExpressionEnvironment env1 = ExpressionEnvironment.builder().build();
        ExpressionEnvironment env2 = ExpressionEnvironment.builder().withMathContext(MathContext.DECIMAL64).build();

        ExpressionCompilationCache cache = new ExpressionCompilationCache(CacheConfig.defaults());
        ExpressionRuntimeSupport first = cache.compileRuntime("1 + 2", ExpressionResultType.MATH, env1);
        ExpressionRuntimeSupport second = cache.compileRuntime("1 + 2", ExpressionResultType.MATH, env2);

        assertThat(second).isNotSameAs(first);
        assertThat(compiledExpressionOf(second)).isNotSameAs(compiledExpressionOf(first));
    }

    @Test
    void shouldCompileAgainAfterInvalidation() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();
        ExpressionCompilationCache cache = new ExpressionCompilationCache(CacheConfig.defaults());

        ExpressionRuntimeSupport first = cache.compileRuntime("1 + 2", ExpressionResultType.MATH, environment);
        cache.invalidateCache();
        ExpressionRuntimeSupport second = cache.compileRuntime("1 + 2", ExpressionResultType.MATH, environment);

        assertThat(second).isNotSameAs(first);
    }

    @Test
    void shouldNotReuseCompiledExpressionForDifferentExternalDefaultValues() throws Exception {
        ExpressionEnvironment env1 = ExpressionEnvironment.builder()
                .registerExternalSymbol("rate", BigDecimal.ONE, false)
                .build();
        ExpressionEnvironment env2 = ExpressionEnvironment.builder()
                .registerExternalSymbol("rate", BigDecimal.TEN, false)
                .build();
        ExpressionCompilationCache cache = new ExpressionCompilationCache(CacheConfig.defaults());

        ExpressionRuntimeSupport first = cache.compileRuntime("rate + 1", ExpressionResultType.MATH, env1);
        ExpressionRuntimeSupport second = cache.compileRuntime("rate + 1", ExpressionResultType.MATH, env2);

        assertThat(second).isNotSameAs(first);
        assertThat(compiledExpressionOf(second)).isNotSameAs(compiledExpressionOf(first));
        assertThat(first.computeMath(Map.of())).isEqualByComparingTo("2");
        assertThat(second.computeMath(Map.of())).isEqualByComparingTo("11");
    }

    @Test
    void shouldNotReuseCompiledExpressionForDifferentConversionServiceInstances() throws Exception {
        ExpressionEnvironment env1 = ExpressionEnvironment.builder()
                .conversionService(new FixtureConversionService())
                .build();
        ExpressionEnvironment env2 = ExpressionEnvironment.builder()
                .conversionService(new FixtureConversionService())
                .build();
        ExpressionCompilationCache cache = new ExpressionCompilationCache(CacheConfig.defaults());

        ExpressionRuntimeSupport first = cache.compileRuntime("1 + 2", ExpressionResultType.MATH, env1);
        ExpressionRuntimeSupport second = cache.compileRuntime("1 + 2", ExpressionResultType.MATH, env2);

        assertThat(second).isNotSameAs(first);
        assertThat(compiledExpressionOf(second)).isNotSameAs(compiledExpressionOf(first));
    }

    private static Object compiledExpressionOf(ExpressionRuntimeSupport runtime) throws Exception {
        Field field = ExpressionRuntimeSupport.class.getDeclaredField("compiledExpression");
        field.setAccessible(true);
        return field.get(runtime);
    }

    private static final class FixtureConversionService implements DataConversionService {

        @Override
        public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
            return false;
        }

        @Override
        public <S, T> T convert(S source, Class<T> targetType) {
            throw new UnsupportedOperationException("test fixture");
        }
    }
}
