package com.runestone.expeval.perf.jmh.navigation.collection;

import com.runestone.expeval.perf.VectorMapTransformBenchmarkSupport;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * Measures the hot evaluation paths of the {@code ..map(@ -> expr)} feature and the
 * {@code [*].prop..sum()} wildcard-projection baseline.
 *
 * <p>All expressions are pre-compiled in {@link VectorMapTransformBenchmarkSupport}; the
 * benchmark loop only exercises the evaluation phase. Run via
 * {@link VectorMapTransformBenchmarkRunner} to collect {@code ns/op} and {@code B/op}.
 *
 * <h2>Scenarios</h2>
 * <ul>
 *   <li>{@code wildcardProjectionSum} — {@code books[*].price..sum()} (baseline)</li>
 *   <li>{@code mapExtractPropertySum} — {@code books..map(@ -> @.price)..sum()}</li>
 *   <li>{@code mapComputedFieldSum}   — {@code books..map(@ -> @.price * @.qty)..sum()}</li>
 *   <li>{@code mapComputedFieldAvg}   — {@code books..map(@ -> @.price * @.qty)..avg()}</li>
 *   <li>{@code mapChainedSum}         — {@code books..map(@ -> @.price)..map(@ -> @ * 2)..sum()}</li>
 *   <li>{@code mapEntryValuesSum}     — {@code mapObj..map(@ -> @.value * 2)..sum()}</li>
 * </ul>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class VectorMapTransformBenchmark {

    /** Baseline: wildcard projection + aggregation — no map step. */
    @Benchmark
    public BigDecimal wildcardProjectionSum(BenchmarkState state) {
        return VectorMapTransformBenchmarkSupport.evaluateWildcardProjectionSum(state.index++);
    }

    /** Map step that only extracts a property — equivalent work to wildcard projection. */
    @Benchmark
    public BigDecimal mapExtractPropertySum(BenchmarkState state) {
        return VectorMapTransformBenchmarkSupport.evaluateMapExtractPropertySum(state.index++);
    }

    /** Map step with a two-operand multiplication (the motivating example). */
    @Benchmark
    public BigDecimal mapComputedFieldSum(BenchmarkState state) {
        return VectorMapTransformBenchmarkSupport.evaluateMapComputedFieldSum(state.index++);
    }

    /** Same computed field but with avg aggregation. */
    @Benchmark
    public BigDecimal mapComputedFieldAvg(BenchmarkState state) {
        return VectorMapTransformBenchmarkSupport.evaluateMapComputedFieldAvg(state.index++);
    }

    /** Two map steps chained: cost of an extra full-list allocation and iteration. */
    @Benchmark
    public BigDecimal mapChainedSum(BenchmarkState state) {
        return VectorMapTransformBenchmarkSupport.evaluateMapChainedSum(state.index++);
    }

    /** Map-entry transform ({@code @.value}) over a {@code Map<String, BigDecimal>}. */
    @Benchmark
    public BigDecimal mapEntryValuesSum(BenchmarkState state) {
        return VectorMapTransformBenchmarkSupport.evaluateMapEntryValuesSum(state.index++);
    }

    @State(Scope.Thread)
    public static class BenchmarkState {

        private int index;

        @Setup(Level.Trial)
        public void setUp() {
            index = 0;
        }
    }
}
