package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.runtime.RuntimeServices;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class CompilationPipelineConcurrencyTest {

    private static final int CONCURRENT_THREADS = 16;
    private static final int COMPILATIONS_PER_THREAD = 8;
    private static final ExpressionEnvironment ENVIRONMENT = ExpressionEnvironment.standard();
    private static final RuntimeServices RUNTIME_SERVICES = RuntimeServices.systemDefault();

    @Test
    void distinctSourcesRemainIsolatedUnderPlatformThreadContention() throws Exception {
        try (ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS)) {
            assertConcurrentCompilation(executor);
        }
    }

    @Test
    void distinctSourcesRemainIsolatedUnderVirtualThreadContention() throws Exception {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            assertConcurrentCompilation(executor);
        }
    }

    private static void assertConcurrentCompilation(ExecutorService executor) throws Exception {
        CountDownLatch ready = new CountDownLatch(CONCURRENT_THREADS);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> results = new ArrayList<>(CONCURRENT_THREADS);

        for (int threadIndex = 0; threadIndex < CONCURRENT_THREADS; threadIndex++) {
            int taskIndex = threadIndex;
            results.add(executor.submit(() -> {
                ready.countDown();
                assertThat(start.await(10, TimeUnit.SECONDS)).isTrue();
                for (int round = 0; round < COMPILATIONS_PER_THREAD; round++) {
                    assertCompilationOutcome(taskIndex, round);
                }
                return null;
            }));
        }

        assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
        start.countDown();
        for (Future<?> result : results) {
            result.get(30, TimeUnit.SECONDS);
        }
    }

    private static void assertCompilationOutcome(int taskIndex, int round) {
        int sourceMarker = taskIndex * COMPILATIONS_PER_THREAD + round;
        switch (round % 4) {
            case 0 -> assertSllSuccess(sourceMarker);
            case 1 -> assertLlSyntaxFailure(sourceMarker);
            case 2 -> assertLexicalFailure(sourceMarker);
            case 3 -> assertSemanticFailure(sourceMarker);
            default -> throw new AssertionError("unreachable scenario");
        }
    }

    private static void assertSllSuccess(int sourceMarker) {
        ExpressionCompilationResult.Success success = (ExpressionCompilationResult.Success) compile(
                sourceMarker + " + 1");

        assertThat(success.compiledExpression().asMath().compute())
                .isEqualByComparingTo(BigDecimal.valueOf(sourceMarker + 1L));
    }

    private static void assertLlSyntaxFailure(int sourceMarker) {
        String source = "1 +" + " ".repeat(sourceMarker + 1);
        ExpressionCompilationResult.Failure failure = (ExpressionCompilationResult.Failure) compile(source);

        assertThat(failure.diagnostics())
                .allSatisfy(diagnostic -> assertThat(diagnostic.category()).isEqualTo(DiagnosticCategory.PARSE));
        assertThat(failure.diagnostics())
                .anySatisfy(diagnostic -> assertThat(diagnostic.primarySpan())
                        .contains(new SourceSpan(source.length(), source.length(), 1, source.length() + 1)));
    }

    private static void assertLexicalFailure(int sourceMarker) {
        String source = "1" + " ".repeat(sourceMarker + 1) + "#";
        ExpressionCompilationResult.Failure failure = (ExpressionCompilationResult.Failure) compile(source);

        assertThat(failure.diagnostics())
                .anySatisfy(diagnostic -> {
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.PARSE_UNRECOGNIZED_CHARACTER.code());
                    assertThat(diagnostic.primarySpan()).contains(new SourceSpan(
                            source.length() - 1, source.length(), 1, source.length()));
                });
    }

    private static void assertSemanticFailure(int sourceMarker) {
        String source = "missing" + sourceMarker;
        ExpressionCompilationResult.Failure failure = (ExpressionCompilationResult.Failure) compile(source);

        assertThat(failure.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> {
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_UNKNOWN_SYMBOL.code());
                    assertThat(diagnostic.primarySpan()).contains(new SourceSpan(0, source.length(), 1, 1));
                });
    }

    private static ExpressionCompilationResult compile(String source) {
        return CompilationPipeline.compile(source, ENVIRONMENT, RUNTIME_SERVICES);
    }
}
