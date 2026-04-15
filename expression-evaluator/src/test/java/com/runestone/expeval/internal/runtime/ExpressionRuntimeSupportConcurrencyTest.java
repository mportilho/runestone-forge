package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.AuditResult;
import com.runestone.expeval.api.CacheConfig;
import com.runestone.expeval.environment.ExpressionEnvironment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the thread-safety guarantees and documented limitations of the ThreadLocal-based
 * override buffer introduced in {@link ExpressionRuntimeSupport}.
 *
 * <p><strong>What the ThreadLocal slot does:</strong> On the hot read-only, non-audited path
 * (non-null user values, no assignments), each thread owns a private {@code OverrideSlot} that
 * holds a reusable {@code Object[]} override buffer and a pre-wired read-only
 * {@link ExecutionScope} whose {@code layer1} permanently points to that buffer.
 * {@code Arrays.fill(UNBOUND)} plus per-entry writes reset the slot for every call.
 *
 * <p><strong>Documented limitation:</strong> Re-entrant evaluation of the <em>same</em>
 * {@link ExpressionRuntimeSupport} instance on the same thread is not supported. Both the outer
 * and inner call share the same slot, so the inner {@code buildOverrides} overwrites the outer
 * call's buffer mid-evaluation.
 */
@DisplayName("ExpressionRuntimeSupport — ThreadLocal slot concurrency")
class ExpressionRuntimeSupportConcurrencyTest {

    /** Empty environment — symbols used in expressions are implicit external symbols. */
    private static final ExpressionEnvironment ENV = ExpressionEnvironment.builder().build();

    /**
     * Environment with {@code a} and {@code b} declared with default {@code 0}.
     * Required for tests that omit one symbol in a call so the missing slot falls back to the
     * registered default instead of remaining {@code UNBOUND}.
     */
    private static final ExpressionEnvironment ENV_AB = ExpressionEnvironment.builder()
            .registerExternalSymbol("a", BigDecimal.ZERO, true)
            .registerExternalSymbol("b", BigDecimal.ZERO, true)
            .build();

    /** Isolated compiler — keeps these tests from polluting the JVM-wide singleton cache. */
    private static final ExpressionCompiler ISOLATED_COMPILER = new ExpressionCompiler(CacheConfig.defaults());

    // ─── Re-entrancy test fixtures ────────────────────────────────────────────

    /**
     * The target runtime used by {@link ReentrantProbe} during re-entrancy tests.
     * Set before each test that needs it; cleared in {@code @AfterEach}.
     */
    static volatile ExpressionRuntimeSupport reentrantTarget;

    /**
     * Prevents {@link ReentrantProbe#probe()} from recursing infinitely.
     * The probe only re-enters on the first call (depth 0 → 1); on subsequent
     * re-entrant calls it returns {@link BigDecimal#ZERO} immediately.
     */
    static final ThreadLocal<Integer> REENTRY_DEPTH = ThreadLocal.withInitial(() -> 0);

    @AfterEach
    void clearReentrantTarget() {
        reentrantTarget = null;
        REENTRY_DEPTH.set(0);
    }

    /**
     * Static function registered in the expression environment for re-entrancy tests.
     * When invoked by the evaluator, it calls back into the <em>same</em>
     * {@link ExpressionRuntimeSupport} instance on the same thread, exercising the
     * documented re-entrancy limitation.
     */
    public static final class ReentrantProbe {
        private ReentrantProbe() {}

        public static BigDecimal probe() {
            int depth = REENTRY_DEPTH.get();
            if (depth > 0) {
                // Break infinite recursion: inner re-entrant calls return ZERO immediately.
                return BigDecimal.ZERO;
            }
            REENTRY_DEPTH.set(depth + 1);
            try {
                // Re-enter the SAME runtime with a different value for 'a'.
                return reentrantTarget.computeMath(Map.of("a", BigDecimal.valueOf(100)));
            } finally {
                REENTRY_DEPTH.set(depth);
            }
        }
    }

    // ─── Concurrent thread isolation ─────────────────────────────────────────

    @Nested
    @DisplayName("concurrent thread isolation — read-only non-audited hot path")
    class ConcurrentThreadIsolation {

        @Test
        @DisplayName("40 concurrent threads with distinct bindings all produce the correct isolated result")
        void fortyThreadsWithDistinctBindingsProduceCorrectResults() throws Exception {
            ExpressionRuntimeSupport runtime = ExpressionRuntimeSupport.compileMath("a + b", ENV, ISOLATED_COMPILER);
            int threadCount = 40;
            ExecutorService pool = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startGate = new CountDownLatch(1);
            List<Future<BigDecimal>> futures = new ArrayList<>(threadCount);

            for (int i = 0; i < threadCount; i++) {
                final int val = i;
                futures.add(pool.submit(() -> {
                    startGate.await();
                    return runtime.computeMath(Map.of("a", val, "b", val * 2));
                }));
            }

            startGate.countDown();
            pool.shutdown();
            assertThat(pool.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

            for (int i = 0; i < threadCount; i++) {
                BigDecimal expected = BigDecimal.valueOf((long) i + (long) i * 2);
                assertThat(futures.get(i).get(5, TimeUnit.SECONDS))
                        .as("thread %d: a=%d, b=%d", i, i, i * 2)
                        .isEqualByComparingTo(expected);
            }
        }

        @Test
        @DisplayName("two threads whose first calls warm each slot concurrently do not cross-contaminate")
        void twoConcurrentThreadsWithWarmSlotsDoNotCrossContaminate() throws Exception {
            ExpressionRuntimeSupport runtime = ExpressionRuntimeSupport.compileMath("a * 2", ENV, ISOLATED_COMPILER);

            CyclicBarrier barrier = new CyclicBarrier(2);
            AtomicReference<BigDecimal> result1 = new AtomicReference<>();
            AtomicReference<BigDecimal> result2 = new AtomicReference<>();
            AtomicReference<Throwable> failure = new AtomicReference<>();

            Thread t1 = new Thread(() -> {
                try {
                    runtime.computeMath(Map.of("a", BigDecimal.valueOf(999))); // warm slot
                    barrier.await();                                            // synchronize
                    result1.set(runtime.computeMath(Map.of("a", BigDecimal.ONE)));
                } catch (Throwable e) {
                    failure.set(e);
                }
            });
            Thread t2 = new Thread(() -> {
                try {
                    runtime.computeMath(Map.of("a", BigDecimal.valueOf(999))); // warm slot
                    barrier.await();                                            // synchronize
                    result2.set(runtime.computeMath(Map.of("a", BigDecimal.TWO)));
                } catch (Throwable e) {
                    failure.set(e);
                }
            });

            t1.start();
            t2.start();
            t1.join(5_000);
            t2.join(5_000);

            assertThat(failure.get()).as("no unexpected exception in either thread").isNull();
            assertThat(result1.get()).isEqualByComparingTo("2");  // 1 * 2
            assertThat(result2.get()).isEqualByComparingTo("4");  // 2 * 2
        }

        @Test
        @DisplayName("concurrent logical evaluations with user bindings each produce the correct boolean result")
        void concurrentLogicalEvaluationsProduceCorrectResults() throws Exception {
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("threshold", new BigDecimal("50"), true)
                    .build();
            ExpressionRuntimeSupport runtime = ExpressionRuntimeSupport.compileLogical(
                    "a > threshold", env, ISOLATED_COMPILER);

            int threadCount = 30;
            ExecutorService pool = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startGate = new CountDownLatch(1);
            List<Future<Boolean>> futures = new ArrayList<>(threadCount);

            for (int i = 0; i < threadCount; i++) {
                final boolean expectedAbove = (i % 2 == 0);
                final int aVal = expectedAbove ? 100 : 10;
                futures.add(pool.submit(() -> {
                    startGate.await();
                    return runtime.computeLogical(Map.of("a", aVal));
                }));
            }

            startGate.countDown();
            pool.shutdown();
            assertThat(pool.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

            for (int i = 0; i < threadCount; i++) {
                boolean expected = (i % 2 == 0); // even → 100 > 50 = true; odd → 10 > 50 = false
                assertThat(futures.get(i).get(5, TimeUnit.SECONDS))
                        .as("thread %d (a=%d)", i, expected ? 100 : 10)
                        .isEqualTo(expected);
            }
        }
    }

    // ─── Same-thread sequential correctness ──────────────────────────────────

    @Nested
    @DisplayName("same-thread sequential correctness")
    class SameThreadSequentialCorrectness {

        @Test
        @DisplayName("override buffer is reset to UNBOUND between calls — stale values from prior call never leak")
        void overrideBufferIsResetBetweenCallsPreventingStaleValues() {
            ExpressionRuntimeSupport runtime = ExpressionRuntimeSupport.compileMath("a + b", ENV_AB, ISOLATED_COMPILER);

            BigDecimal firstCall = runtime.computeMath(Map.of("a", 10, "b", 20));
            // Second call omits 'b'; if the buffer were not reset, b=20 from the first call would persist.
            BigDecimal secondCall = runtime.computeMath(Map.of("a", 5));
            // Third call omits 'a'; if the buffer were not reset, a=5 from the second call would persist.
            BigDecimal thirdCall = runtime.computeMath(Map.of("b", 7));

            assertThat(firstCall).isEqualByComparingTo("30");
            assertThat(secondCall)
                    .as("b must fall back to default 0, not reuse b=20 from the first call")
                    .isEqualByComparingTo("5");
            assertThat(thirdCall)
                    .as("a must fall back to default 0, not reuse a=5 from the second call")
                    .isEqualByComparingTo("7");
        }

        @Test
        @DisplayName("20 sequential calls on the same thread with distinct values each produce the correct result")
        void sequentialCallsWithRotatingValuesEachProduceCorrectResult() {
            ExpressionRuntimeSupport runtime = ExpressionRuntimeSupport.compileMath("a + b", ENV, ISOLATED_COMPILER);

            for (int i = 0; i < 20; i++) {
                BigDecimal result = runtime.computeMath(Map.of("a", i, "b", i * 3));
                assertThat(result)
                        .as("call %d: a=%d, b=%d", i, i, i * 3)
                        .isEqualByComparingTo(BigDecimal.valueOf((long) i + (long) i * 3));
            }
        }

        @Test
        @DisplayName("dynamic instant cache is cleared before each reuse so currDate stays consistent within each evaluation")
        void dynamicInstantCacheIsClearedBeforeEachReuse() {
            // The expression uses 'a' (user-supplied) to force the ThreadLocal path, plus
            // 'currDate = currDate' to exercise the per-evaluation dynamic cache.
            // Each call reuses the ThreadLocal scope after clearDynamicCache(); the date
            // comparison must remain true (same cached value within one evaluation).
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerExternalSymbol("a", BigDecimal.ONE, true)
                    .build();
            ExpressionRuntimeSupport runtime = ExpressionRuntimeSupport.compileLogical(
                    "a > 0 and currDate = currDate", env, ISOLATED_COMPILER);

            for (int i = 0; i < 5; i++) {
                boolean result = runtime.computeLogical(Map.of("a", BigDecimal.ONE));
                assertThat(result)
                        .as("call %d: currDate must equal itself within a single evaluation", i)
                        .isTrue();
            }
        }
    }

    // ─── Thread-pool thread reuse ─────────────────────────────────────────────

    @Nested
    @DisplayName("thread-pool thread reuse")
    class ThreadPoolThreadReuse {

        @Test
        @DisplayName("single-thread pool reuses the same OS thread and slot across 30 tasks without contamination")
        void singleThreadPoolReusesSameSlotAcrossMultipleTasks() throws Exception {
            ExpressionRuntimeSupport runtime = ExpressionRuntimeSupport.compileMath("a + b", ENV, ISOLATED_COMPILER);
            // A single-thread executor guarantees the same thread (and therefore the same ThreadLocal
            // slot) is reused for every submitted task.
            ExecutorService pool = Executors.newSingleThreadExecutor();
            int iterations = 30;
            List<Future<BigDecimal>> futures = new ArrayList<>(iterations);

            for (int i = 0; i < iterations; i++) {
                final int val = i;
                futures.add(pool.submit(() -> runtime.computeMath(Map.of("a", val, "b", val))));
            }

            pool.shutdown();
            assertThat(pool.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

            for (int i = 0; i < iterations; i++) {
                assertThat(futures.get(i).get(5, TimeUnit.SECONDS))
                        .as("task %d", i)
                        .isEqualByComparingTo(BigDecimal.valueOf(i * 2L));
            }
        }
    }

    // ─── Independent runtime instances ───────────────────────────────────────

    @Nested
    @DisplayName("independent runtime instances — each has its own ThreadLocal slot")
    class IndependentRuntimeInstances {

        @Test
        @DisplayName("two instances compiled from the same source have independent slots on the same thread")
        void twoInstancesCompiledFromSameSourceHaveIndependentSlotsOnSameThread() {
            ExpressionRuntimeSupport runtimeA = ExpressionRuntimeSupport.compileMath("a + b", ENV, ISOLATED_COMPILER);
            ExpressionRuntimeSupport runtimeB = ExpressionRuntimeSupport.compileMath("a + b", ENV, ISOLATED_COMPILER);

            // Warm each slot with different values.
            runtimeA.computeMath(Map.of("a", 1, "b", 1));
            runtimeB.computeMath(Map.of("a", 100, "b", 100));

            // runtimeB's slot must not affect runtimeA's slot and vice versa.
            BigDecimal resultA = runtimeA.computeMath(Map.of("a", 10, "b", 20));
            BigDecimal resultB = runtimeB.computeMath(Map.of("a", 30, "b", 40));

            assertThat(resultA).isEqualByComparingTo("30");
            assertThat(resultB).isEqualByComparingTo("70");
        }

        @Test
        @DisplayName("interleaved evaluation of two instances on the same thread does not corrupt either slot")
        void interleavedEvaluationOfTwoInstancesOnSameThreadPreservesIsolation() {
            ExpressionRuntimeSupport r1 = ExpressionRuntimeSupport.compileMath("a + b", ENV, ISOLATED_COMPILER);
            ExpressionRuntimeSupport r2 = ExpressionRuntimeSupport.compileMath("a + b", ENV, ISOLATED_COMPILER);

            for (int i = 0; i < 10; i++) {
                BigDecimal v1 = r1.computeMath(Map.of("a", i, "b", i));
                BigDecimal v2 = r2.computeMath(Map.of("a", i * 10, "b", i * 10));
                // Re-read r1 to detect if r2's evaluation corrupted r1's slot.
                BigDecimal v1Again = r1.computeMath(Map.of("a", i, "b", i));

                assertThat(v1).as("r1 iteration %d", i).isEqualByComparingTo(BigDecimal.valueOf(i * 2L));
                assertThat(v2).as("r2 iteration %d", i).isEqualByComparingTo(BigDecimal.valueOf(i * 20L));
                assertThat(v1Again).as("r1 after r2 call, iteration %d", i).isEqualByComparingTo(v1);
            }
        }
    }

    // ─── Audited path ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("audited path — creates a fresh scope per call, no ThreadLocal reuse")
    class AuditedPath {

        @Test
        @DisplayName("20 concurrent audited evaluations each produce the correct result and an independent trace")
        void concurrentAuditedEvaluationsAreIsolated() throws Exception {
            ExpressionRuntimeSupport runtime = ExpressionRuntimeSupport.compileMath("a + b", ENV, ISOLATED_COMPILER);
            int threadCount = 20;
            ExecutorService pool = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startGate = new CountDownLatch(1);
            List<Future<AuditResult<BigDecimal>>> futures = new ArrayList<>(threadCount);

            for (int i = 0; i < threadCount; i++) {
                final int val = i;
                futures.add(pool.submit(() -> {
                    startGate.await();
                    return runtime.computeMathWithAudit(Map.of("a", val, "b", val));
                }));
            }

            startGate.countDown();
            pool.shutdown();
            assertThat(pool.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

            for (int i = 0; i < threadCount; i++) {
                AuditResult<BigDecimal> auditResult = futures.get(i).get(5, TimeUnit.SECONDS);
                assertThat(auditResult.value())
                        .as("thread %d audit result", i)
                        .isEqualByComparingTo(BigDecimal.valueOf(i * 2L));
                assertThat(auditResult.trace())
                        .as("thread %d must have a non-null audit trace", i)
                        .isNotNull();
            }
        }

        @Test
        @DisplayName("sequential audited calls on the same thread each produce the correct result")
        void sequentialAuditedCallsOnSameThreadProduceCorrectResults() {
            ExpressionRuntimeSupport runtime = ExpressionRuntimeSupport.compileMath("a + b", ENV, ISOLATED_COMPILER);

            for (int i = 0; i < 10; i++) {
                AuditResult<BigDecimal> result = runtime.computeMathWithAudit(Map.of("a", i, "b", i));
                assertThat(result.value())
                        .as("audited call %d", i)
                        .isEqualByComparingTo(BigDecimal.valueOf(i * 2L));
            }
        }
    }

    // ─── Same-thread re-entrancy ──────────────────────────────────────────────

    @Nested
    @DisplayName("same-thread re-entrant evaluation of the same instance")
    class SameThreadReentrancy {

        @Test
        @DisplayName("inner call on the same instance allocates a fresh buffer — outer binding a=1 is preserved")
        void innerCallOnSameInstancePreservesOuterBindings() {
            // Expression: "probe() + a"
            // Outer call: a=1.
            // probe() re-enters the same runtime with a=100.
            //   Inner: probe() (depth>0 → 0) + 100 = 100
            // Outer reads its own a=1 from the untouched ThreadLocal buffer.
            // Correct result: 100 + 1 = 101.
            ExpressionCompiler dedicatedCompiler = new ExpressionCompiler(CacheConfig.defaults());
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerStaticProvider(ReentrantProbe.class)
                    .build();
            ExpressionRuntimeSupport runtime = ExpressionRuntimeSupport.compileMath(
                    "probe() + a", env, dedicatedCompiler);

            reentrantTarget = runtime;

            BigDecimal result = runtime.computeMath(Map.of("a", BigDecimal.ONE));

            assertThat(result)
                    .as("outer call must read its own a=1 despite the inner call overriding a=100")
                    .isEqualByComparingTo("101");
        }

        @Test
        @DisplayName("audited outer call is also protected — inner non-audited call uses a fresh buffer")
        void auditedOuterCallIsProtectedFromInnerNonAuditedReentry() {
            ExpressionCompiler dedicatedCompiler = new ExpressionCompiler(CacheConfig.defaults());
            ExpressionEnvironment env = ExpressionEnvironment.builder()
                    .registerStaticProvider(ReentrantProbe.class)
                    .build();
            ExpressionRuntimeSupport runtime = ExpressionRuntimeSupport.compileMath(
                    "probe() + a", env, dedicatedCompiler);

            reentrantTarget = runtime;

            // Outer is audited; inner (probe()) calls non-audited computeMath.
            AuditResult<BigDecimal> auditResult = runtime.computeMathWithAudit(Map.of("a", BigDecimal.ONE));

            assertThat(auditResult.value())
                    .as("audited outer call must also preserve its own a=1 binding")
                    .isEqualByComparingTo("101");
        }

        @Test
        @DisplayName("re-entrant call on a DIFFERENT instance is safe — each instance owns its own slot")
        void reentrantCallOnDifferentInstanceDoesNotCorruptOuterSlot() {
            ExpressionCompiler dedicatedCompiler = new ExpressionCompiler(CacheConfig.defaults());
            ExpressionEnvironment envForOuter = ExpressionEnvironment.builder()
                    .registerStaticProvider(CrossInstanceProbe.class)
                    .build();
            ExpressionRuntimeSupport outerRuntime = ExpressionRuntimeSupport.compileMath(
                    "crossCall() + a", envForOuter, dedicatedCompiler);

            ExpressionRuntimeSupport innerRuntime = ExpressionRuntimeSupport.compileMath(
                    "a + b", ENV, dedicatedCompiler);
            CrossInstanceProbe.innerTarget = innerRuntime;

            // crossCall() evaluates innerRuntime(a=100, b=200) → 300.
            // Outer reads its own a=1 → 300 + 1 = 301.
            BigDecimal result = outerRuntime.computeMath(Map.of("a", BigDecimal.ONE));

            assertThat(result)
                    .as("inner call on a different instance must not corrupt the outer slot")
                    .isEqualByComparingTo("301");
        }
    }

    /**
     * Function class for the "safe cross-instance re-entrancy" test.
     * Calls a DIFFERENT {@link ExpressionRuntimeSupport} to demonstrate that cross-instance
     * calls do not suffer from the same-instance re-entrancy limitation.
     */
    public static final class CrossInstanceProbe {
        static volatile ExpressionRuntimeSupport innerTarget;

        private CrossInstanceProbe() {}

        public static BigDecimal crossCall() {
            return innerTarget.computeMath(Map.of("a", BigDecimal.valueOf(100),
                    "b", BigDecimal.valueOf(200)));
        }
    }
}
