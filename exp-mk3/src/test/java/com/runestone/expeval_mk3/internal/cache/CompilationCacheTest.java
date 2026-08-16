package com.runestone.expeval_mk3.internal.cache;

import com.runestone.expeval_mk3.api.CacheConfig;
import com.runestone.expeval_mk3.api.DiagnosticCategory;
import com.runestone.expeval_mk3.api.ExpressionCompilationResult;
import com.runestone.expeval_mk3.api.ExpressionDiagnostic;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Issue #134's cache-loader contract in isolation from {@code ExpressionEngine}: a hit returns the
 * resident {@link ExpressionCompilationResult}, and an unexpected exception from the compiler escapes
 * without installing an entry, so a later call for the same key can retry.
 */
class CompilationCacheTest {

    @Test
    void anUnexpectedCompilerFailureInstallsNoEntryAndALaterCallCanRetry() {
        AtomicInteger calls = new AtomicInteger();
        CompilationCache cache = new CompilationCache(CacheConfig.defaults(), (source, environment) -> {
            if (calls.incrementAndGet() == 1) {
                throw new IllegalStateException("transient bug");
            }
            return new ExpressionCompilationResult.Failure(List.of(ExpressionDiagnostic.error(
                    DiagnosticCategory.SEMANTIC, "TEST_CODE", "test failure", null)));
        });
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();

        assertThatThrownBy(() -> cache.get("source", environment)).isInstanceOf(IllegalStateException.class);
        ExpressionCompilationResult retried = cache.get("source", environment);

        assertThat(retried).isInstanceOf(ExpressionCompilationResult.Failure.class);
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    void theCacheKeyRetainsTheExactSourceReferenceItWasBuiltFrom() {
        String source = new String("1 + 2".toCharArray());
        CompilationCacheKey key = new CompilationCacheKey(source, "environment-id");

        assertThat(key.source()).as("the key intentionally retains the caller's exact source reference")
                .isSameAs(source);
    }

    @Test
    void aHitReturnsTheResidentResultWithoutCallingTheCompilerAgain() {
        AtomicInteger calls = new AtomicInteger();
        CompilationCache cache = new CompilationCache(CacheConfig.defaults(), (source, environment) -> {
            calls.incrementAndGet();
            return new ExpressionCompilationResult.Failure(List.of(ExpressionDiagnostic.error(
                    DiagnosticCategory.SEMANTIC, "TEST_CODE", "test failure", null)));
        });
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();

        ExpressionCompilationResult first = cache.get("source", environment);
        ExpressionCompilationResult second = cache.get("source", environment);

        assertThat(second).isSameAs(first);
        assertThat(calls.get()).isEqualTo(1);
    }
}
