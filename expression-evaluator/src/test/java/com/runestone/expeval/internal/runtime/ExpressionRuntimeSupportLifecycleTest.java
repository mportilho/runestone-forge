package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.CacheConfig;
import com.runestone.expeval.api.ExpressionEngine;
import com.runestone.expeval.api.MathExpression;
import com.runestone.expeval.environment.ExpressionEnvironment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the singleton lifecycle of {@link ExpressionRuntimeSupport}:
 * {@code configure}, {@code reconfigure}, and {@code invalidateCache}.
 *
 * <p>Every test that mutates the singleton calls {@link ExpressionRuntimeSupport#reconfigure}
 * in {@code @AfterEach} to restore a known baseline for the rest of the suite.
 */
@DisplayName("ExpressionRuntimeSupport — singleton lifecycle")
class ExpressionRuntimeSupportLifecycleTest {

    private static final ExpressionEnvironment ENV = ExpressionEnvironment.builder().build();

    @AfterEach
    void resetSingletonToDefaults() {
        ExpressionEngine.reconfigureDefault(CacheConfig.defaults());
    }

    // --- configure() ---

    @Nested
    @DisplayName("configure()")
    class Configure {

        @Test
        @DisplayName("is a no-op when the singleton is already initialized")
        void isNoOpWhenAlreadyInitialized() {
            // Warm the singleton and capture a cached compiled expression.
            MathExpression first = MathExpression.compile("50 + 1", ENV);

            // configure() must be ignored because the singleton exists.
            ExpressionEngine.configureDefault(new CacheConfig(256, null));

            // A second compile of the same expression should be a cache hit —
            // proving the runtime (and its cache) was not replaced.
            MathExpression second = MathExpression.compile("50 + 1", ENV);
            assertThat(compiledExpressionOf(second)).isSameAs(compiledExpressionOf(first));
        }

        @Test
        @DisplayName("rejects null CacheConfig")
        void rejectsNullCacheConfig() {
            assertThatThrownBy(() -> ExpressionEngine.configureDefault(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // --- reconfigure() ---

    @Nested
    @DisplayName("reconfigure()")
    class Reconfigure {

        @Test
        @DisplayName("replaces the runtime even after initialization, clearing the cache")
        void replacesRuntimeAndClearsCache() {
            CompiledExpression before =
                    compiledExpressionOf(MathExpression.compile("40 + 2", ENV));

            ExpressionEngine.reconfigureDefault(CacheConfig.defaults());

            CompiledExpression after =
                    compiledExpressionOf(MathExpression.compile("40 + 2", ENV));
            assertThat(after).isNotSameAs(before);
        }

        @Test
        @DisplayName("warms the cache again after reconfiguration")
        void warmsTheCacheAfterReconfiguration() {
            ExpressionEngine.reconfigureDefault(CacheConfig.defaults());

            CompiledExpression first =
                    compiledExpressionOf(MathExpression.compile("40 + 2", ENV));
            CompiledExpression second =
                    compiledExpressionOf(MathExpression.compile("40 + 2", ENV));

            assertThat(second).isSameAs(first);
        }

        @Test
        @DisplayName("rejects null CacheConfig")
        void rejectsNullCacheConfig() {
            assertThatThrownBy(() -> ExpressionEngine.reconfigureDefault(null))
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
                    compiledExpressionOf(MathExpression.compile("30 + 3", ENV));

            ExpressionEngine.invalidateDefaultCache();

            CompiledExpression after =
                    compiledExpressionOf(MathExpression.compile("30 + 3", ENV));
            assertThat(after).isNotSameAs(before);
        }

        @Test
        @DisplayName("warms the cache again after the first post-invalidation compilation")
        void warmsTheCacheAfterFirstPostInvalidationCompile() {
            ExpressionEngine.invalidateDefaultCache();

            CompiledExpression afterInvalidation =
                    compiledExpressionOf(MathExpression.compile("30 + 3", ENV));
            CompiledExpression cachedAgain =
                    compiledExpressionOf(MathExpression.compile("30 + 3", ENV));

            assertThat(cachedAgain).isSameAs(afterInvalidation);
        }
    }

    private static CompiledExpression compiledExpressionOf(MathExpression expression) {
        try {
            Field runtimeField = MathExpression.class.getDeclaredField("runtime");
            runtimeField.setAccessible(true);
            ExpressionRuntimeSupport runtime = (ExpressionRuntimeSupport) runtimeField.get(expression);
            return runtime.getCompiledExpression();
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Unable to inspect compiled expression", e);
        }
    }
}
