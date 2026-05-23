package com.runestone.expeval.internal.compiler;

import com.runestone.expeval.api.CacheConfig;
import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.internal.runtime.CompiledExpression;
import com.runestone.expeval.compiler.ExpressionCompiler;
import com.runestone.expeval.testing.ExpressionCompilerInspector;
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
            var inspector = new ExpressionCompilerInspector(compiler);

            CompiledExpression result = inspector.compileMath("1 + 2", ENV);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("CacheConfig constructor with TTL compiles expressions correctly")
        void cacheConfigConstructorWithTtlCompiles() {
            var compiler = new ExpressionCompiler(new CacheConfig(64, Duration.ofMinutes(30)));
            var inspector = new ExpressionCompilerInspector(compiler);

            CompiledExpression result = inspector.compileMath("3 * 4", ENV);

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
            var inspector = new ExpressionCompilerInspector(compiler);

            CompiledExpression first = inspector.compileMath("10 + 5", ENV);
            CompiledExpression second = inspector.compileMath("10 + 5", ENV);

            assertThat(second).isSameAs(first);
        }

        @Test
        @DisplayName("produces distinct CompiledExpressions for different sources")
        void returnsDistinctInstancesForDifferentSources() {
            var compiler = new ExpressionCompiler(CacheConfig.defaults());
            var inspector = new ExpressionCompilerInspector(compiler);

            CompiledExpression first = inspector.compileMath("10 + 5", ENV);
            CompiledExpression second = inspector.compileMath("10 + 6", ENV);

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
            var inspector = new ExpressionCompilerInspector(compiler);
            CompiledExpression before = inspector.compileMath("20 + 1", ENV);

            compiler.invalidateCache();

            CompiledExpression after = inspector.compileMath("20 + 1", ENV);
            assertThat(after).isNotSameAs(before);
        }

        @Test
        @DisplayName("warms the cache again after the first compilation post-invalidation")
        void warmsTheCacheAfterFirstPostInvalidationCompile() {
            var compiler = new ExpressionCompiler(CacheConfig.defaults());
            var inspector = new ExpressionCompilerInspector(compiler);
            inspector.compileMath("20 + 1", ENV);
            compiler.invalidateCache();

            CompiledExpression afterInvalidation = inspector.compileMath("20 + 1", ENV);
            CompiledExpression cachedAgain = inspector.compileMath("20 + 1", ENV);

            assertThat(cachedAgain).isSameAs(afterInvalidation);
        }

        @Test
        @DisplayName("only clears entries of this instance — a second compiler is unaffected")
        void doesNotAffectOtherCompilerInstances() {
            var compilerA = new ExpressionCompiler(CacheConfig.defaults());
            var compilerB = new ExpressionCompiler(CacheConfig.defaults());
            var inspectorB = new ExpressionCompilerInspector(compilerB);

            CompiledExpression fromB = inspectorB.compileMath("99 + 1", ENV);
            compilerA.invalidateCache();

            CompiledExpression fromBAfter = inspectorB.compileMath("99 + 1", ENV);
            assertThat(fromBAfter).isSameAs(fromB);
        }
    }
}
