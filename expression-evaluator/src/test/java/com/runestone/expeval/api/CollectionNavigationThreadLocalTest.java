package com.runestone.expeval.api;

import com.runestone.expeval.environment.ExpressionEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Concurrency tests targeting the ThreadLocal pooling optimizations in AbstractObjectEvaluator:
 * <ul>
 *   <li><b>DEEP_SCAN_CTX (O2)</b> — DeepScanContext reuse (ArrayList, IdentityHashMap, ArrayDeque)</li>
 *   <li><b>FILTER_CTX (O4)</b> — FilterContextStack frame reuse</li>
 * </ul>
 *
 * <p>All tests exercise observable behavior (return values, exception messages).
 * Internal ThreadLocal state is never inspected directly.
 */
@DisplayName("ThreadLocal pooling — concurrency safety")
class CollectionNavigationThreadLocalTest {

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    private static ExpressionEnvironment env(String name, Object value) {
        return ExpressionEnvironment.builder()
                .registerExternalSymbol(name, value, true)
                .build();
    }

    /** Builds a store map with N books each priced at {@code price}. */
    private static Map<String, Object> storeOf(int bookCount, BigDecimal price) {
        List<Map<String, Object>> books = new ArrayList<>(bookCount);
        for (int i = 0; i < bookCount; i++) {
            Map<String, Object> book = new LinkedHashMap<>();
            book.put("title", "Book-" + i);
            book.put("price", price);
            books.add(book);
        }
        Map<String, Object> store = new LinkedHashMap<>();
        store.put("books", books);
        return store;
    }

    /**
     * Builds a store map that contains a self-referencing entry ({@code store.self = store})
     * to exercise the cycle-detection IdentityHashMap in DeepScanContext.
     */
    private static Map<String, Object> cyclicStoreOf(int bookCount, BigDecimal price) {
        Map<String, Object> store = new LinkedHashMap<>(storeOf(bookCount, price));
        store.put("self", store); // cyclic reference
        return store;
    }

    // -------------------------------------------------------------------------
    // DeepScanContext (O2) — thread isolation
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("DeepScanContext (O2) — thread isolation")
    class DeepScanContextIsolation {

        @Test
        @DisplayName("concurrent deep scans on distinct data do not bleed results between threads")
        void shouldIsolateDeepScanResultsPerThread() throws Exception {
            // Two compiled expressions scanning structures with different total prices.
            // Threads in group A should always get sum=30 (3 books × 10), group B sum=60 (3 books × 20).
            var storeA = storeOf(3, new BigDecimal("10"));
            var storeB = storeOf(3, new BigDecimal("20"));
            MathExpression exprA = MathExpression.compile("store..ds(price)..sum()", env("store", storeA));
            MathExpression exprB = MathExpression.compile("store..ds(price)..sum()", env("store", storeB));

            int threads = 8;
            int iterations = 60;
            ExecutorService exec = Executors.newFixedThreadPool(threads);
            try {
                List<Future<BigDecimal>> futuresA = IntStream.range(0, iterations)
                        .mapToObj(i -> exec.submit(() -> exprA.compute(Map.of("store", storeA))))
                        .toList();
                List<Future<BigDecimal>> futuresB = IntStream.range(0, iterations)
                        .mapToObj(i -> exec.submit(() -> exprB.compute(Map.of("store", storeB))))
                        .toList();

                for (Future<BigDecimal> f : futuresA) {
                    assertThat(f.get()).isEqualByComparingTo("30");
                }
                for (Future<BigDecimal> f : futuresB) {
                    assertThat(f.get()).isEqualByComparingTo("60");
                }
            } finally {
                exec.shutdown();
                exec.awaitTermination(10, TimeUnit.SECONDS);
            }
        }

        @Test
        @DisplayName("deep scan results are cleared between sequential invocations on the same thread")
        void shouldClearDeepScanResultsBetweenInvocationsOnSameThread() {
            // Single thread evaluates the same deep scan expression 100 times.
            // If ctx.results is not cleared, it would accumulate and the count would grow.
            var store = storeOf(3, new BigDecimal("5"));
            MathExpression expr = MathExpression.compile("store..ds(price)..count()", env("store", store));

            for (int i = 0; i < 100; i++) {
                BigDecimal result = expr.compute(Map.of("store", store));
                assertThat(result)
                        .as("iteration %d: deep scan count must always equal 3, never accumulate", i)
                        .isEqualByComparingTo("3");
            }
        }

        @Test
        @DisplayName("deep scan terminates on cyclic structures and returns correct count under concurrency")
        void shouldTerminateOnCyclicStructureUnderConcurrency() throws Exception {
            // store.self = store creates a cycle. IdentityHashMap.visited must prevent infinite loop.
            var store = cyclicStoreOf(2, new BigDecimal("7"));
            MathExpression expr = MathExpression.compile("store..ds(price)..count()", env("store", store));

            int threads = 6;
            int iterations = 40;
            ExecutorService exec = Executors.newFixedThreadPool(threads);
            try {
                List<Future<BigDecimal>> futures = IntStream.range(0, threads * iterations)
                        .mapToObj(i -> exec.submit(() -> expr.compute(Map.of("store", store))))
                        .toList();
                for (Future<BigDecimal> f : futures) {
                    assertThat(f.get())
                            .as("deep scan on cyclic structure should collect exactly 2 prices")
                            .isEqualByComparingTo("2");
                }
            } finally {
                exec.shutdown();
                exec.awaitTermination(10, TimeUnit.SECONDS);
            }
        }

        @Test
        @DisplayName("visited set is cleared between sequential deep scans on the same thread")
        void shouldClearVisitedSetBetweenInvocationsOnSameThread() {
            // Cyclic structure: if visited is not cleared, first scan would still work but
            // second scan might skip elements already in the identity set from a prior call.
            var store = cyclicStoreOf(4, new BigDecimal("3"));
            MathExpression expr = MathExpression.compile("store..ds(price)..sum()", env("store", store));

            for (int i = 0; i < 50; i++) {
                BigDecimal result = expr.compute(Map.of("store", store));
                assertThat(result)
                        .as("iteration %d: visited set must be cleared; sum should always be 12 (4 × 3)", i)
                        .isEqualByComparingTo("12");
            }
        }

        @Test
        @DisplayName("deep scan on different thread pool threads all produce correct results")
        void shouldProduceCorrectResultsOnHighConcurrencyDeepScan() throws Exception {
            var store = storeOf(5, new BigDecimal("4"));
            MathExpression expr = MathExpression.compile("store..ds(price)..sum()", env("store", store));

            int threads = 20;
            int iterations = 100;
            ExecutorService exec = Executors.newFixedThreadPool(threads);
            try {
                List<Future<BigDecimal>> futures = IntStream.range(0, threads * iterations)
                        .mapToObj(i -> exec.submit(() -> expr.compute(Map.of("store", store))))
                        .toList();
                for (Future<BigDecimal> f : futures) {
                    assertThat(f.get()).isEqualByComparingTo("20");
                }
            } finally {
                exec.shutdown();
                exec.awaitTermination(15, TimeUnit.SECONDS);
            }
        }
    }

    // -------------------------------------------------------------------------
    // FilterContextStack (O4) — exception safety
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("FilterContextStack (O4) — exception safety")
    class FilterContextStackExceptionSafety {

        @Test
        @DisplayName("filter predicate type error leaves the stack clean for subsequent evaluations on same thread")
        void shouldLeaveFilterStackCleanAfterPredicateException() {
            // Expression that causes a type error inside the filter predicate
            // (comparing a String field to a number triggers TYPE_MISMATCH)
            var books = List.of(
                    Map.of("title", "Alpha", "price", new BigDecimal("5")),
                    Map.of("title", "Beta",  "price", "NOT_A_NUMBER"),  // bad data
                    Map.of("title", "Gamma", "price", new BigDecimal("8"))
            );
            MathExpression badExpr = MathExpression.compile(
                    "books[?(@[\"price\"] < 10)]..count()", env("books", books));

            // First call with bad data should throw
            assertThatThrownBy(() -> badExpr.compute(Map.of("books", books)))
                    .isInstanceOf(Exception.class);

            // Good data on the same virtual-thread-executor should work correctly after the exception.
            var goodBooks = List.of(
                    Map.of("title", "Alpha", "price", new BigDecimal("5")),
                    Map.of("title", "Gamma", "price", new BigDecimal("8"))
            );
            MathExpression goodExpr = MathExpression.compile(
                    "books[?(@[\"price\"] < 10)]..count()", env("books", goodBooks));
            BigDecimal result = goodExpr.compute(Map.of("books", goodBooks));
            assertThat(result)
                    .as("filter stack must be clean after prior exception; both books pass price < 10")
                    .isEqualByComparingTo("2");
        }

        @Test
        @DisplayName("filter stack depth returns to zero after exception in concurrent scenario")
        void shouldRecoverFilterStackDepthAfterConcurrentException() throws Exception {
            var goodBooks = List.of(
                    Map.of("title", "Alpha", "price", new BigDecimal("5")),
                    Map.of("title", "Beta",  "price", new BigDecimal("9"))
            );
            var badBooks = List.of(
                    Map.of("title", "Alpha", "price", new BigDecimal("5")),
                    Map.of("title", "Beta",  "price", "INVALID")  // triggers exception during filter
            );
            MathExpression expr = MathExpression.compile(
                    "books[?(@[\"price\"] < 10)]..count()", env("books", goodBooks));

            // Run a mix of good and bad evaluations concurrently; each thread then re-evaluates
            // with good data to verify its filter stack was cleaned up by the finally block.
            int threads = 4;
            ExecutorService exec = Executors.newFixedThreadPool(threads);
            List<Future<BigDecimal>> results = new ArrayList<>();
            CountDownLatch latch = new CountDownLatch(1);

            try {
                for (int t = 0; t < threads; t++) {
                    results.add(exec.submit(() -> {
                        latch.await();
                        // Trigger exception on this thread
                        try {
                            expr.compute(Map.of("books", badBooks));
                        } catch (Exception ignored) {
                            // expected; now evaluate with good data on same thread
                        }
                        // Must succeed: filter stack must be depth-0 after the exception
                        return expr.compute(Map.of("books", goodBooks));
                    }));
                }
                latch.countDown();
                for (Future<BigDecimal> f : results) {
                    assertThat(f.get())
                            .as("filter stack must be empty after exception; both books pass price < 10")
                            .isEqualByComparingTo("2");
                }
            } finally {
                exec.shutdown();
                exec.awaitTermination(10, TimeUnit.SECONDS);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Map filter concurrency (O3 + O4)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Map filter concurrency (O3 + O4)")
    class MapFilterConcurrency {

        @Test
        @DisplayName("concurrent map filter evaluations produce correct isolated results")
        void shouldProduceCorrectResultsOnConcurrentMapFilter() throws Exception {
            // Map<String, Map> with 4 entries; filter by price < 10 → 2 entries
            Map<String, Object> bookByIsbn = Map.of(
                    "0-001", Map.of("title", "Alpha", "price", new BigDecimal("5")),
                    "0-002", Map.of("title", "Beta",  "price", new BigDecimal("15")),
                    "0-003", Map.of("title", "Gamma", "price", new BigDecimal("8")),
                    "0-004", Map.of("title", "Delta", "price", new BigDecimal("20"))
            );
            MathExpression expr = MathExpression.compile(
                    "catalog[?(@[\"price\"] < 10)]..count()",
                    env("catalog", bookByIsbn));

            int threads = 8;
            int iterations = 50;
            ExecutorService exec = Executors.newFixedThreadPool(threads);
            try {
                List<Future<BigDecimal>> futures = IntStream.range(0, threads * iterations)
                        .mapToObj(i -> exec.submit(() -> expr.compute(Map.of("catalog", bookByIsbn))))
                        .toList();
                for (Future<BigDecimal> f : futures) {
                    assertThat(f.get())
                            .as("map filter concurrent: only Alpha(5) and Gamma(8) pass < 10")
                            .isEqualByComparingTo("2");
                }
            } finally {
                exec.shutdown();
                exec.awaitTermination(10, TimeUnit.SECONDS);
            }
        }

        @Test
        @DisplayName("map filter does not retain entries from previous invocation on same thread")
        void shouldNotRetainMapFilterResultsAcrossInvocationsOnSameThread() {
            Map<String, Object> books = Map.of(
                    "A", Map.of("price", new BigDecimal("5")),
                    "B", Map.of("price", new BigDecimal("15")),
                    "C", Map.of("price", new BigDecimal("8"))
            );
            MathExpression expr = MathExpression.compile(
                    "books[?(@[\"price\"] < 10)]..count()", env("books", books));

            for (int i = 0; i < 50; i++) {
                BigDecimal result = expr.compute(Map.of("books", books));
                assertThat(result)
                        .as("iteration %d: map filter must always return 2 (A and C), never accumulate", i)
                        .isEqualByComparingTo("2");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Mixed deep scan + filter on same thread
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Mixed DeepScanContext and FilterContextStack on same thread")
    class MixedDeepScanAndFilter {

        @Test
        @DisplayName("deep scan followed by filter on same thread returns correct results for both")
        void shouldNotCrossContaminateDeepScanAndFilterContextsOnSameThread() {
            var store = storeOf(4, new BigDecimal("10"));
            var books = List.of(
                    Map.of("title", "Alpha", "price", new BigDecimal("5")),
                    Map.of("title", "Beta",  "price", new BigDecimal("15")),
                    Map.of("title", "Gamma", "price", new BigDecimal("8"))
            );
            MathExpression deepScanExpr = MathExpression.compile(
                    "store..ds(price)..count()", env("store", store));
            MathExpression filterExpr = MathExpression.compile(
                    "books[?(@[\"price\"] < 10)]..count()", env("books", books));

            // Interleave deep scan and filter on the same thread
            for (int i = 0; i < 30; i++) {
                BigDecimal deepScanResult = deepScanExpr.compute(Map.of("store", store));
                BigDecimal filterResult = filterExpr.compute(Map.of("books", books));

                assertThat(deepScanResult)
                        .as("iteration %d: deep scan should count 4 prices", i)
                        .isEqualByComparingTo("4");
                assertThat(filterResult)
                        .as("iteration %d: filter should count 2 books under 10", i)
                        .isEqualByComparingTo("2");
            }
        }

        @Test
        @DisplayName("concurrent threads interleaving deep scan and filter return correct results")
        void shouldNotCrossContaminateWhenMixingDeepScanAndFilterConcurrently() throws Exception {
            var store = storeOf(3, new BigDecimal("10"));
            var books = List.of(
                    Map.of("title", "Alpha", "price", new BigDecimal("5")),
                    Map.of("title", "Beta",  "price", new BigDecimal("15")),
                    Map.of("title", "Gamma", "price", new BigDecimal("8"))
            );
            MathExpression deepScanExpr = MathExpression.compile(
                    "store..ds(price)..sum()", env("store", store));
            MathExpression filterExpr = MathExpression.compile(
                    "books[?(@[\"price\"] < 10)]..count()", env("books", books));

            int threads = 8;
            int iterations = 40;
            ExecutorService exec = Executors.newFixedThreadPool(threads);
            List<Future<BigDecimal>> deepScanFutures = new ArrayList<>();
            List<Future<BigDecimal>> filterFutures = new ArrayList<>();
            try {
                for (int i = 0; i < threads * iterations; i++) {
                    deepScanFutures.add(exec.submit(() -> deepScanExpr.compute(Map.of("store", store))));
                    filterFutures.add(exec.submit(() -> filterExpr.compute(Map.of("books", books))));
                }
                for (Future<BigDecimal> f : deepScanFutures) {
                    assertThat(f.get()).isEqualByComparingTo("30");
                }
                for (Future<BigDecimal> f : filterFutures) {
                    assertThat(f.get()).isEqualByComparingTo("2");
                }
            } finally {
                exec.shutdown();
                exec.awaitTermination(10, TimeUnit.SECONDS);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Virtual threads (Java 21)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Virtual threads (Java 21) — ThreadLocal isolation")
    class VirtualThreadIsolation {

        @Test
        @DisplayName("virtual threads have independent DeepScanContext (O2) and produce correct results")
        void shouldIsolateDeepScanContextPerVirtualThread() throws Exception {
            var store = storeOf(3, new BigDecimal("7"));
            MathExpression expr = MathExpression.compile("store..ds(price)..sum()", env("store", store));

            int taskCount = 200;
            List<Future<BigDecimal>> futures = new ArrayList<>(taskCount);
            try (ExecutorService vExec = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < taskCount; i++) {
                    futures.add(vExec.submit(() -> expr.compute(Map.of("store", store))));
                }
                for (Future<BigDecimal> f : futures) {
                    assertThat(f.get())
                            .as("virtual thread deep scan: 3 books × 7 = 21")
                            .isEqualByComparingTo("21");
                }
            }
        }

        @Test
        @DisplayName("virtual threads have independent FilterContextStack (O4) and produce correct results")
        void shouldIsolateFilterContextStackPerVirtualThread() throws Exception {
            var books = List.of(
                    Map.of("title", "Alpha", "price", new BigDecimal("5")),
                    Map.of("title", "Beta",  "price", new BigDecimal("15")),
                    Map.of("title", "Gamma", "price", new BigDecimal("8")),
                    Map.of("title", "Delta", "price", new BigDecimal("20"))
            );
            MathExpression expr = MathExpression.compile(
                    "books[?(@[\"price\"] < 10)]..count()", env("books", books));

            int taskCount = 200;
            List<Future<BigDecimal>> futures = new ArrayList<>(taskCount);
            try (ExecutorService vExec = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < taskCount; i++) {
                    futures.add(vExec.submit(() -> expr.compute(Map.of("books", books))));
                }
                for (Future<BigDecimal> f : futures) {
                    assertThat(f.get())
                            .as("virtual thread filter: Alpha(5) and Gamma(8) pass < 10")
                            .isEqualByComparingTo("2");
                }
            }
        }

        @Test
        @DisplayName("cyclic deep scan terminates correctly on virtual threads")
        void shouldTerminateOnCyclicStructureOnVirtualThreads() throws Exception {
            var store = cyclicStoreOf(3, new BigDecimal("4"));
            MathExpression expr = MathExpression.compile("store..ds(price)..count()", env("store", store));

            int taskCount = 100;
            List<Future<BigDecimal>> futures = new ArrayList<>(taskCount);
            try (ExecutorService vExec = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < taskCount; i++) {
                    futures.add(vExec.submit(() -> expr.compute(Map.of("store", store))));
                }
                for (Future<BigDecimal> f : futures) {
                    assertThat(f.get())
                            .as("cyclic deep scan on virtual thread: exactly 3 prices")
                            .isEqualByComparingTo("3");
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Stress: deep scan under high concurrency
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Stress tests")
    class StressTests {

        @Test
        @DisplayName("deep scan under high concurrency (20 threads × 100 iterations) is always correct")
        void shouldBeStableUnderHighConcurrencyDeepScan() throws Exception {
            var store = storeOf(5, new BigDecimal("3"));
            MathExpression expr = MathExpression.compile("store..ds(price)..count()", env("store", store));

            int threads = 20;
            int iterations = 100;
            List<String> errors = new CopyOnWriteArrayList<>();
            ExecutorService exec = Executors.newFixedThreadPool(threads);
            try {
                List<Future<Void>> futures = IntStream.range(0, threads * iterations)
                        .mapToObj(i -> exec.<Void>submit(() -> {
                            BigDecimal result = expr.compute(Map.of("store", store));
                            if (result.compareTo(new BigDecimal("5")) != 0) {
                                errors.add("Thread " + Thread.currentThread().getName()
                                        + " iteration " + i + ": expected 5, got " + result);
                            }
                            return null;
                        }))
                        .toList();
                for (Future<Void> f : futures) {
                    f.get();
                }
            } finally {
                exec.shutdown();
                exec.awaitTermination(30, TimeUnit.SECONDS);
            }
            assertThat(errors).as("no thread should produce a wrong deep scan count").isEmpty();
        }

        @Test
        @DisplayName("mixed filter and deep scan under high concurrency produce no cross-contamination")
        void shouldProduceNoContaminationUnderHighMixedConcurrency() throws Exception {
            var store = storeOf(4, new BigDecimal("5"));
            var books = List.of(
                    Map.of("price", new BigDecimal("3")),
                    Map.of("price", new BigDecimal("7")),
                    Map.of("price", new BigDecimal("11")),
                    Map.of("price", new BigDecimal("2"))
            );
            MathExpression deepExpr = MathExpression.compile(
                    "store..ds(price)..count()", env("store", store));
            MathExpression filterExpr = MathExpression.compile(
                    "books[?(@[\"price\"] < 8)]..count()", env("books", books));

            int threads = 16;
            int iterations = 80;
            List<String> errors = new CopyOnWriteArrayList<>();
            ExecutorService exec = Executors.newFixedThreadPool(threads);
            try {
                List<Future<Void>> futures = new ArrayList<>();
                for (int i = 0; i < threads * iterations; i++) {
                    final int idx = i;
                    if (idx % 2 == 0) {
                        futures.add(exec.<Void>submit(() -> {
                            BigDecimal r = deepExpr.compute(Map.of("store", store));
                            if (r.compareTo(new BigDecimal("4")) != 0) {
                                errors.add("deepScan wrong: " + r);
                            }
                            return null;
                        }));
                    } else {
                        futures.add(exec.<Void>submit(() -> {
                            BigDecimal r = filterExpr.compute(Map.of("books", books));
                            // 3, 7, 2 pass < 8 → count = 3
                            if (r.compareTo(new BigDecimal("3")) != 0) {
                                errors.add("filter wrong: " + r);
                            }
                            return null;
                        }));
                    }
                }
                for (Future<Void> f : futures) {
                    f.get();
                }
            } finally {
                exec.shutdown();
                exec.awaitTermination(30, TimeUnit.SECONDS);
            }
            assertThat(errors).as("no contamination between deep scan and filter contexts").isEmpty();
        }
    }
}
