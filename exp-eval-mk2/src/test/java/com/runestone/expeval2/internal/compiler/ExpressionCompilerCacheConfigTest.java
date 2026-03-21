package com.runestone.expeval2.internal.compiler;

import com.runestone.expeval2.api.CacheConfig;
import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.internal.grammar.ExpressionResultType;
import com.runestone.expeval2.internal.runtime.CompiledExpression;
import com.runestone.expeval2.internal.runtime.ExpressionCompiler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ExpressionCompiler — CacheConfig and invalidateCache")
class ExpressionCompilerCacheConfigTest {

    private static final ExpressionEnvironment ENV = ExpressionEnvironment.builder().build();

    // --- Construction ---

    @Nested
    @DisplayName("Construction with CacheConfig")
    class Construction {

        @Test
        @DisplayName("no-arg constructor uses system-property defaults")
        void noArgConstructorUsesDefaults() {
            // Should not throw; actual size values are tested in CacheConfigTest.
            var compiler = new ExpressionCompiler();
            assertThat(compiler).isNotNull();
        }

        @Test
        @DisplayName("CacheConfig constructor compiles expressions correctly")
        void cacheConfigConstructorCompilesMathExpression() {
            var compiler = new ExpressionCompiler(new CacheConfig(64, null));

            CompiledExpression result = compiler.compile("1 + 2", ExpressionResultType.MATH, ENV);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("CacheConfig constructor with TTL compiles expressions correctly")
        void cacheConfigConstructorWithTtlCompiles() {
            var compiler = new ExpressionCompiler(new CacheConfig(64, Duration.ofMinutes(30)));

            CompiledExpression result = compiler.compile("3 * 4", ExpressionResultType.MATH, ENV);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("rejects null CacheConfig")
        void rejectsNullCacheConfig() {
            assertThatThrownBy(() -> new ExpressionCompiler(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // --- Cache hit / miss ---

    @Nested
    @DisplayName("Cache behaviour")
    class CacheBehaviour {

        @Test
        @DisplayName("returns the same CompiledExpression on repeated compile calls")
        void returnsSameInstanceOnCacheHit() {
            var compiler = new ExpressionCompiler(CacheConfig.defaults());

            CompiledExpression first = compiler.compile("10 + 5", ExpressionResultType.MATH, ENV);
            CompiledExpression second = compiler.compile("10 + 5", ExpressionResultType.MATH, ENV);

            assertThat(second).isSameAs(first);
        }

        @Test
        @DisplayName("produces distinct CompiledExpressions for different sources")
        void returnsDistinctInstancesForDifferentSources() {
            var compiler = new ExpressionCompiler(CacheConfig.defaults());

            CompiledExpression first = compiler.compile("10 + 5", ExpressionResultType.MATH, ENV);
            CompiledExpression second = compiler.compile("10 + 6", ExpressionResultType.MATH, ENV);

            assertThat(second).isNotSameAs(first);
        }
    }

    // --- invalidateCache ---

    @Nested
    @DisplayName("invalidateCache()")
    class InvalidateCache {

        @Test
        @DisplayName("forces a new compilation after the cache is cleared")
        void forcesNewCompilationAfterInvalidation() {
            var compiler = new ExpressionCompiler(CacheConfig.defaults());
            CompiledExpression before = compiler.compile("20 + 1", ExpressionResultType.MATH, ENV);

            compiler.invalidateCache();

            CompiledExpression after = compiler.compile("20 + 1", ExpressionResultType.MATH, ENV);
            assertThat(after).isNotSameAs(before);
        }

        @Test
        @DisplayName("warms the cache again after the first compilation post-invalidation")
        void warmsTheCacheAfterFirstPostInvalidationCompile() {
            var compiler = new ExpressionCompiler(CacheConfig.defaults());
            compiler.compile("20 + 1", ExpressionResultType.MATH, ENV);
            compiler.invalidateCache();

            CompiledExpression afterInvalidation = compiler.compile("20 + 1", ExpressionResultType.MATH, ENV);
            CompiledExpression cachedAgain = compiler.compile("20 + 1", ExpressionResultType.MATH, ENV);

            assertThat(cachedAgain).isSameAs(afterInvalidation);
        }

        @Test
        @DisplayName("only clears entries of this instance — a second compiler is unaffected")
        void doesNotAffectOtherCompilerInstances() {
            var compilerA = new ExpressionCompiler(CacheConfig.defaults());
            var compilerB = new ExpressionCompiler(CacheConfig.defaults());

            CompiledExpression fromB = compilerB.compile("99 + 1", ExpressionResultType.MATH, ENV);
            compilerA.invalidateCache();

            CompiledExpression fromBAfter = compilerB.compile("99 + 1", ExpressionResultType.MATH, ENV);
            assertThat(fromBAfter).isSameAs(fromB);
        }
    }
}
