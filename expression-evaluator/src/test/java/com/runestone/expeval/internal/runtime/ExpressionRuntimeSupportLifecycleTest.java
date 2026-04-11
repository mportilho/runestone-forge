package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.CacheConfig;
import com.runestone.expeval.environment.ExpressionEnvironment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the singleton lifecycle of {@link ExpressionRuntimeSupport}:
 * {@code configure}, {@code reconfigure}, {@code invalidateCache}, and the explicit-compiler
 * overloads introduced in Option C.
 *
 * <p>Every test that mutates the singleton calls {@link ExpressionRuntimeSupport#reconfigure}
 * in {@code @AfterEach} to restore a known baseline for the rest of the suite.
 */
@DisplayName("ExpressionRuntimeSupport — singleton lifecycle")
class ExpressionRuntimeSupportLifecycleTest {

    private static final ExpressionEnvironment ENV = ExpressionEnvironment.builder().build();

    @AfterEach
    void resetSingletonToDefaults() {
        ExpressionRuntimeSupport.reconfigure(CacheConfig.defaults());
    }

    // --- configure() ---

    @Nested
    @DisplayName("configure()")
    class Configure {

        @Test
        @DisplayName("is a no-op when the singleton is already initialized")
        void isNoOpWhenAlreadyInitialized() {
            // Warm the singleton and capture a cached compiled expression.
            ExpressionRuntimeSupport first = ExpressionRuntimeSupport.compileMath("50 + 1", ENV);

            // configure() must be ignored because the singleton exists.
            ExpressionRuntimeSupport.configure(new CacheConfig(256, null));

            // A second compile of the same expression should be a cache hit —
            // proving the compiler (and its cache) was not replaced.
            ExpressionRuntimeSupport second = ExpressionRuntimeSupport.compileMath("50 + 1", ENV);
            assertThat(second.getCompiledExpression()).isSameAs(first.getCompiledExpression());
        }

        @Test
        @DisplayName("rejects null CacheConfig")
        void rejectsNullCacheConfig() {
            assertThatThrownBy(() -> ExpressionRuntimeSupport.configure(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // --- reconfigure() ---

    @Nested
    @DisplayName("reconfigure()")
    class Reconfigure {

        @Test
        @DisplayName("replaces the compiler even after initialization, clearing the cache")
        void replacesCompilerAndClearsCache() {
            CompiledExpression before =
                    ExpressionRuntimeSupport.compileMath("40 + 2", ENV).getCompiledExpression();

            ExpressionRuntimeSupport.reconfigure(CacheConfig.defaults());

            CompiledExpression after =
                    ExpressionRuntimeSupport.compileMath("40 + 2", ENV).getCompiledExpression();
            assertThat(after).isNotSameAs(before);
        }

        @Test
        @DisplayName("warms the cache again after reconfiguration")
        void warmsTheCacheAfterReconfiguration() {
            ExpressionRuntimeSupport.reconfigure(CacheConfig.defaults());

            CompiledExpression first =
                    ExpressionRuntimeSupport.compileMath("40 + 2", ENV).getCompiledExpression();
            CompiledExpression second =
                    ExpressionRuntimeSupport.compileMath("40 + 2", ENV).getCompiledExpression();

            assertThat(second).isSameAs(first);
        }

        @Test
        @DisplayName("rejects null CacheConfig")
        void rejectsNullCacheConfig() {
            assertThatThrownBy(() -> ExpressionRuntimeSupport.reconfigure(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // --- invalidateCache() on singleton ---

    @Nested
    @DisplayName("invalidateCache() on singleton")
    class InvalidateCacheOnSingleton {

        @Test
        @DisplayName("forces a new compilation on the next call")
        void forcesNewCompilationAfterInvalidation() {
            CompiledExpression before =
                    ExpressionRuntimeSupport.compileMath("30 + 3", ENV).getCompiledExpression();

            ExpressionRuntimeSupport.invalidateCache();

            CompiledExpression after =
                    ExpressionRuntimeSupport.compileMath("30 + 3", ENV).getCompiledExpression();
            assertThat(after).isNotSameAs(before);
        }

        @Test
        @DisplayName("warms the cache again after the first post-invalidation compilation")
        void warmsTheCacheAfterFirstPostInvalidationCompile() {
            ExpressionRuntimeSupport.invalidateCache();

            CompiledExpression afterInvalidation =
                    ExpressionRuntimeSupport.compileMath("30 + 3", ENV).getCompiledExpression();
            CompiledExpression cachedAgain =
                    ExpressionRuntimeSupport.compileMath("30 + 3", ENV).getCompiledExpression();

            assertThat(cachedAgain).isSameAs(afterInvalidation);
        }
    }

    // --- explicit compiler overloads (Option C) ---

    @Nested
    @DisplayName("explicit compiler overloads")
    class ExplicitCompilerOverloads {

        @Test
        @DisplayName("compileMath with explicit compiler caches within that compiler")
        void compileMathWithExplicitCompilerCachesWithinThatCompiler() {
            var compiler = new ExpressionCompiler(CacheConfig.defaults());

            CompiledExpression first =
                    ExpressionRuntimeSupport.compileMath("7 * 6", ENV, compiler).getCompiledExpression();
            CompiledExpression second =
                    ExpressionRuntimeSupport.compileMath("7 * 6", ENV, compiler).getCompiledExpression();

            assertThat(second).isSameAs(first);
        }

        @Test
        @DisplayName("compileLogical with explicit compiler caches within that compiler")
        void compileLogicalWithExplicitCompilerCachesWithinThatCompiler() {
            var compiler = new ExpressionCompiler(CacheConfig.defaults());

            CompiledExpression first =
                    ExpressionRuntimeSupport.compileLogical("7 > 6", ENV, compiler).getCompiledExpression();
            CompiledExpression second =
                    ExpressionRuntimeSupport.compileLogical("7 > 6", ENV, compiler).getCompiledExpression();

            assertThat(second).isSameAs(first);
        }

        @Test
        @DisplayName("compileAssignments with explicit compiler caches within that compiler")
        void compileAssignmentsWithExplicitCompilerCachesWithinThatCompiler() {
            var compiler = new ExpressionCompiler(CacheConfig.defaults());

            CompiledExpression first =
                    ExpressionRuntimeSupport.compileAssignments("x = 7 * 6;", ENV, compiler).getCompiledExpression();
            CompiledExpression second =
                    ExpressionRuntimeSupport.compileAssignments("x = 7 * 6;", ENV, compiler).getCompiledExpression();

            assertThat(second).isSameAs(first);
        }

        @Test
        @DisplayName("invalidating the explicit compiler does not affect the singleton cache")
        void invalidatingExplicitCompilerDoesNotAffectSingleton() {
            var explicitCompiler = new ExpressionCompiler(CacheConfig.defaults());

            // Warm both the singleton and the explicit compiler with the same expression.
            CompiledExpression singletonEntry =
                    ExpressionRuntimeSupport.compileMath("8 * 8", ENV).getCompiledExpression();
            ExpressionRuntimeSupport.compileMath("8 * 8", ENV, explicitCompiler);

            // Invalidate only the explicit compiler.
            explicitCompiler.invalidateCache();

            // Explicit compiler must produce a new entry (cache was cleared).
            CompiledExpression explicitAfter =
                    ExpressionRuntimeSupport.compileMath("8 * 8", ENV, explicitCompiler).getCompiledExpression();

            // Singleton cache must be unaffected.
            CompiledExpression singletonAfter =
                    ExpressionRuntimeSupport.compileMath("8 * 8", ENV).getCompiledExpression();

            assertThat(explicitAfter).isNotSameAs(singletonEntry);
            assertThat(singletonAfter).isSameAs(singletonEntry);
        }

        @Test
        @DisplayName("rejects null compiler argument")
        void rejectsNullCompilerArgument() {
            assertThatThrownBy(() -> ExpressionRuntimeSupport.compileMath("1 + 1", ENV, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
