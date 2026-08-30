package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Issue #104's basic concurrency gate: one {@link CompiledExpression} and its views, plus the
 * {@code ExecutionPlan} they share, are reused across genuinely in-flight concurrent calls. Every call
 * gets its own {@code ExecutionScope} and result snapshot; nothing about one call's overrides, assigned
 * symbols, or current-temporal reads may leak into another's. This is representative concurrent reuse,
 * not the sustained-contention stress reserved for Etapa 12.
 */
class ConcurrentSharedPlanExecutionTest {

    @Test
    void sharesOneCompiledExpressionAcrossViewsWithDistinctOverridesUnderConcurrentThreads() throws Exception {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("factor", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        CompiledExpression compiled = ExpressionEngine.defaultEngine().compileOrThrow(
                "doubled := factor * 2; exceeds := doubled > 10; exceeds", environment);
        ResultExpression resultView = compiled.asResult();
        LogicalExpression logicalView = compiled.asLogical();
        AssignmentsExpression assignmentsView = compiled.asAssignments();

        int callCount = 64;
        ExecutorService executor = Executors.newFixedThreadPool(callCount);
        try {
            CountDownLatch ready = new CountDownLatch(callCount);
            CountDownLatch release = new CountDownLatch(1);
            List<Callable<CallOutcome>> calls = IntStream.range(0, callCount)
                    .mapToObj(value -> (Callable<CallOutcome>) () -> {
                        BigDecimal factor = BigDecimal.valueOf(value);
                        Map<String, Object> overrides = Map.of("factor", factor);
                        ready.countDown();
                        release.await();
                        Object result = resultView.compute(overrides);
                        boolean logical = logicalView.compute(overrides);
                        Map<String, Object> assignments = assignmentsView.compute(overrides);
                        return new CallOutcome(factor, result, logical, assignments);
                    })
                    .collect(Collectors.toList());

            List<Future<CallOutcome>> futures = calls.stream().map(executor::submit).collect(Collectors.toList());
            assertThat(ready.await(10, TimeUnit.SECONDS)).as("all calls became ready").isTrue();
            release.countDown();

            for (Future<CallOutcome> future : futures) {
                CallOutcome outcome = future.get(10, TimeUnit.SECONDS);
                BigDecimal expectedDoubled = outcome.factor.multiply(BigDecimal.valueOf(2));
                boolean expectedExceeds = expectedDoubled.compareTo(BigDecimal.TEN) > 0;

                assertThat(outcome.result).as("factor=%s", outcome.factor).isEqualTo(expectedExceeds);
                assertThat(outcome.logical).as("factor=%s", outcome.factor).isEqualTo(expectedExceeds);
                assertThat(outcome.assignments)
                        .as("factor=%s", outcome.factor)
                        .containsEntry("exceeds", expectedExceeds);
                assertThat((BigDecimal) outcome.assignments.get("doubled"))
                        .as("factor=%s", outcome.factor)
                        .isEqualByComparingTo(expectedDoubled);
                assertThatThrownBy(() -> outcome.assignments.put("extra", BigDecimal.ZERO))
                        .as("independent immutable snapshot for factor=%s", outcome.factor)
                        .isInstanceOf(UnsupportedOperationException.class);
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void isolatesNestedCurrentItemStateAcrossConcurrentThreadsSharingOnePlan() throws Exception {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol(
                        "outer",
                        new CollectionType(new CollectionType(ScalarType.NUMBER)),
                        List.of(List.of(BigDecimal.ONE)),
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        ResultExpression expression = ExpressionEngine.defaultEngine().compileOrThrow(
                        "outer.map(@ -> @.map(@ -> 10 / @).sum())", environment)
                .asResult();

        List<List<BigDecimal>> failingInput = List.of(List.of(BigDecimal.ONE, BigDecimal.ZERO));
        List<List<BigDecimal>> healthyInput = List.of(
                List.of(BigDecimal.TEN, BigDecimal.valueOf(5)), List.of(BigDecimal.ONE));
        Object expectedHealthyResult = List.of(new BigDecimal("3"), new BigDecimal("10"));

        int callsPerKind = 32;
        ExecutorService executor = Executors.newFixedThreadPool(callsPerKind * 2);
        try {
            CountDownLatch ready = new CountDownLatch(callsPerKind * 2);
            CountDownLatch release = new CountDownLatch(1);
            List<Callable<Object>> healthyCalls = IntStream.range(0, callsPerKind)
                    .mapToObj(value -> (Callable<Object>) () -> {
                        ready.countDown();
                        release.await();
                        return expression.computeWithMemory(Map.of("outer", healthyInput));
                    })
                    .collect(Collectors.toList());
            List<Callable<Object>> failingCalls = IntStream.range(0, callsPerKind)
                    .mapToObj(value -> (Callable<Object>) () -> {
                        ready.countDown();
                        release.await();
                        try {
                            expression.computeWithMemory(Map.of("outer", failingInput));
                            return "did not fail";
                        } catch (ExpressionExecutionException expected) {
                            return expected;
                        }
                    })
                    .collect(Collectors.toList());

            List<Future<Object>> healthyFutures =
                    healthyCalls.stream().map(executor::submit).collect(Collectors.toList());
            List<Future<Object>> failingFutures =
                    failingCalls.stream().map(executor::submit).collect(Collectors.toList());
            assertThat(ready.await(10, TimeUnit.SECONDS)).as("all calls became ready").isTrue();
            release.countDown();

            for (Future<Object> future : healthyFutures) {
                ComputationWithMemory<?> computation =
                        (ComputationWithMemory<?>) future.get(10, TimeUnit.SECONDS);
                assertThat(computation.result()).isEqualTo(expectedHealthyResult);
                assertThat(computation.memory().variables()).containsExactly(
                        new VariableEntry(new VariableKey("outer", VariableOrigin.EXTERNAL), healthyInput));
                assertThat(computation.memory().calculations()).isEmpty();
            }
            for (Future<Object> future : failingFutures) {
                assertThat(future.get(10, TimeUnit.SECONDS)).isInstanceOf(ExpressionExecutionException.class);
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void isolatesCurrentTemporalReadsAcrossConcurrentThreadsSharingOnePlan() throws Exception {
        SequentialSecondClock clock = new SequentialSecondClock(ZoneOffset.UTC);
        ExpressionEngine engine = ExpressionEngine.builder().clock(clock).build();
        CompiledExpression compiled = engine.compileOrThrow(
                "moment := currDateTime; moment = currDateTime", ExpressionEnvironment.standard());
        AssignmentsExpression assignmentsView = compiled.asAssignments();
        LogicalExpression consistencyView = compiled.asLogical();

        int callCount = 48;
        ExecutorService executor = Executors.newFixedThreadPool(callCount);
        try {
            CountDownLatch ready = new CountDownLatch(callCount);
            CountDownLatch release = new CountDownLatch(1);
            List<Callable<LocalDateTime>> calls = IntStream.range(0, callCount)
                    .mapToObj(value -> (Callable<LocalDateTime>) () -> {
                        ready.countDown();
                        release.await();
                        assertThat(consistencyView.compute()).as("repeated reads within one call agree").isTrue();
                        return (LocalDateTime) assignmentsView.compute().get("moment");
                    })
                    .collect(Collectors.toList());

            List<Future<LocalDateTime>> futures = calls.stream().map(executor::submit).collect(Collectors.toList());
            assertThat(ready.await(10, TimeUnit.SECONDS)).as("all calls became ready").isTrue();
            release.countDown();

            List<LocalDateTime> moments = futures.stream().map(this::join).collect(Collectors.toList());
            Set<LocalDateTime> distinctMoments = Set.copyOf(moments);

            assertThat(moments).hasSize(callCount);
            assertThat(distinctMoments)
                    .as("no two concurrent calls observed the same cached instant from another call's scope")
                    .hasSize(callCount);
        } finally {
            executor.shutdownNow();
        }
    }

    private LocalDateTime join(Future<LocalDateTime> future) {
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    private record CallOutcome(BigDecimal factor, Object result, boolean logical, Map<String, Object> assignments) {
    }

    /** Hands out a strictly increasing whole-second instant on every read, thread-safely. */
    private static final class SequentialSecondClock extends Clock {

        private final AtomicLong seconds = new AtomicLong();
        private final ZoneId zone;

        SequentialSecondClock(ZoneId zone) {
            this.zone = zone;
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochSecond(seconds.incrementAndGet());
        }
    }
}
