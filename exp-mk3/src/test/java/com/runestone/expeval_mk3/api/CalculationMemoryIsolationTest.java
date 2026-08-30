package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class CalculationMemoryIsolationTest {

    @Test
    void concurrentSuccessesAndFailuresKeepOverridesMemoAndMemoryExecutionLocal() throws Exception {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("value", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("divisor", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .functionsFrom(Functions.class, FunctionPurity.IMPURE)
                .build();
        ResultExpression expression = ExpressionEngine.defaultEngine().compileOrThrow(
                        "memo := (value + 1) * (value + 1); 10 / divisor + mark(memo)", environment)
                .asResult();

        int callCount = 32;
        CountDownLatch ready = new CountDownLatch(callCount);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(callCount);
        try {
            List<Future<Outcome>> futures = IntStream.range(0, callCount)
                    .mapToObj(index -> (Callable<Outcome>) () -> compute(expression, index, ready, release))
                    .map(executor::submit)
                    .toList();
            assertThat(ready.await(10, TimeUnit.SECONDS)).as("all calls became ready").isTrue();
            release.countDown();

            List<CalculationMemory> memories = new ArrayList<>();
            for (int index = 0; index < futures.size(); index++) {
                Outcome outcome = futures.get(index).get(10, TimeUnit.SECONDS);
                if ((index & 1) == 0) {
                    assertThat(outcome.failure()).isInstanceOf(ExpressionExecutionException.class);
                    assertThat(outcome.computation()).isNull();
                    continue;
                }

                BigDecimal value = BigDecimal.valueOf(index);
                BigDecimal memo = value.add(BigDecimal.ONE).pow(2);
                ComputationWithMemory<Object> computation = outcome.computation();
                assertThat((BigDecimal) computation.result()).isEqualByComparingTo(BigDecimal.TEN.add(memo));
                assertThat((BigDecimal) variableValue(computation.memory(), "value")).isEqualByComparingTo(value);
                assertThat(variableValue(computation.memory(), "memo")).isEqualTo(memo);
                assertThat(computation.memory().calculations())
                        .extracting(CalculationEntry::value)
                        .containsExactly(memo);
                memories.add(computation.memory());
            }
            assertThat(memories).doesNotHaveDuplicates();
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void reentrantComputationUsesAnIndependentFrameRecorderAndMemory() {
        ReentrantFunctions functions = new ReentrantFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("value", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();
        ResultExpression expression = ExpressionEngine.defaultEngine()
                .compileOrThrow("reenter(value)", environment)
                .asResult();
        functions.expression = expression;

        ComputationWithMemory<Object> outer = expression.computeWithMemory(Map.of("value", BigDecimal.TEN));
        ComputationWithMemory<Object> inner = functions.inner;

        assertThat(outer.result()).isEqualTo(new BigDecimal("11"));
        assertThat(inner.result()).isEqualTo(new BigDecimal("11"));
        assertThat(variableValue(outer.memory(), "value")).isEqualTo(BigDecimal.TEN);
        assertThat(variableValue(inner.memory(), "value")).isEqualTo(new BigDecimal("11"));
        assertThat(outer.memory()).isNotSameAs(inner.memory());
        assertThat(outer.memory().calculationKeyAt(0)).isSameAs(inner.memory().calculationKeyAt(0));
    }

    private static Outcome compute(
            ResultExpression expression, int index, CountDownLatch ready, CountDownLatch release) throws InterruptedException {
        BigDecimal value = BigDecimal.valueOf(index);
        BigDecimal divisor = (index & 1) == 0 ? BigDecimal.ZERO : BigDecimal.ONE;
        ready.countDown();
        release.await();
        try {
            return new Outcome(expression.computeWithMemory(Map.of("value", value, "divisor", divisor)), null);
        } catch (ExpressionExecutionException exception) {
            return new Outcome(null, exception);
        }
    }

    private static Object variableValue(CalculationMemory memory, String name) {
        for (int index = 0; index < memory.variableCount(); index++) {
            if (memory.variableKeyAt(index).name().equals(name)) {
                return memory.variableValueAt(index);
            }
        }
        throw new AssertionError("missing variable: " + name);
    }

    private record Outcome(ComputationWithMemory<Object> computation, Throwable failure) {
    }

    public static final class Functions {
        public static BigDecimal mark(BigDecimal value) {
            return value;
        }
    }

    public static final class ReentrantFunctions {
        private ResultExpression expression;
        private ComputationWithMemory<Object> inner;
        private boolean nested;

        public BigDecimal reenter(BigDecimal value) {
            if (nested) {
                return value;
            }
            nested = true;
            try {
                inner = expression.computeWithMemory(Map.of("value", value.add(BigDecimal.ONE)));
                return (BigDecimal) inner.result();
            } finally {
                nested = false;
            }
        }
    }
}
