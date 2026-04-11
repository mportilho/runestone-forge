package com.runestone.expeval.perf.jmh;

import com.runestone.expeval.internal.runtime.CompilePathAllocationBenchmarkSupport;
import com.runestone.expeval.internal.runtime.ExpressionRuntimeSupport;
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
 * Measures allocation in the compile path for three smells:
 *
 * <ul>
 *   <li><b>compileSimpleCacheHit</b> — isolates Smell A: {@code RuntimeValueFactory} +
 *       {@code RuntimeCoercionService} created per {@code compile()} even on cache hit.</li>
 *   <li><b>compileFunctionCacheHit</b> — same smell in the context of an expression with
 *       8 function calls, where {@code MutableBindings.seedDefaults()} does more work.</li>
 *   <li><b>compileFunctionCacheMiss</b> — full pipeline; isolates Smell A + B + C together
 *       (FunctionRef per call, FunctionCatalog.findExact stream per lookup, factory objects).</li>
 * </ul>
 *
 * <p>Run with {@code -prof gc} to capture {@code B/op}. The cache-hit benchmarks should show
 * a fixed 48-B floor (the two factory objects) plus any {@code MutableBindings} overhead.
 * The difference between cache-miss and cache-hit B/op isolates the compile-only allocation
 * (including FunctionRef and stream smells).
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class CompilePathAllocationBenchmark {

    @Benchmark
    public ExpressionRuntimeSupport compileSimpleCacheHit(CompileState state) {
        return state.support.compileSimpleCacheHit();
    }

    @Benchmark
    public ExpressionRuntimeSupport compileFunctionCacheHit(CompileState state) {
        return state.support.compileFunctionCacheHit();
    }

    @Benchmark
    public ExpressionRuntimeSupport compileFunctionCacheMiss(CompileState state) {
        return state.support.compileFunctionCacheMiss();
    }

    @State(Scope.Thread)
    public static class CompileState {

        CompilePathAllocationBenchmarkSupport support;

        @Setup(Level.Trial)
        public void setUp() {
            support = new CompilePathAllocationBenchmarkSupport();
        }
    }
}
