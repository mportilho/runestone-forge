package com.runestone.expeval.perf.jmh.startup.compilation;

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
 *   <li><b>compileSimpleCacheHit</b> — isolates cache-hit overhead after runtime support has
 *       already been built.</li>
 *   <li><b>compileFunctionCacheHit</b> — same cache-hit path in the context of an expression
 *       with 8 function calls.</li>
 *   <li><b>compileFunctionCacheMiss</b> — full pipeline; includes FunctionRef per call,
 *       FunctionCatalog.findExact stream per lookup, and runtime-support construction.</li>
 * </ul>
 *
 * <p>Run with {@code -prof gc} to capture {@code B/op}. The difference between cache-miss and
 * cache-hit B/op isolates compile-only allocation, including FunctionRef, stream, plan, and
 * runtime-support construction costs.
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
