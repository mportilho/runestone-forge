package com.runestone.expeval.perf.jmh.runtime;

import com.runestone.expeval.api.CacheConfig;
import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.environment.ExpressionEnvironmentBuilder;
import com.runestone.expeval.internal.grammar.ExpressionResultType;
import com.runestone.expeval.internal.runtime.ExpressionCompilationCache;
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
 * Measures the read-only {@code ExecutionScope} path with no assignments and no audit trail.
 *
 * <p>Run with {@code -prof gc}; these scenarios isolate scope creation and dynamic instant
 * caching more directly than benchmarks that allocate per-call input maps.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class ReadOnlyExecutionScopeBenchmark {

    @Benchmark
    public boolean foldedLiteral(ReadOnlyState state) {
        return state.foldedLiteral.computeLogical(null);
    }

    @Benchmark
    public boolean defaultOnlySymbol(ReadOnlyState state) {
        return state.defaultOnlySymbol.computeLogical(null);
    }

    @Benchmark
    public boolean dynamicInstant(ReadOnlyState state) {
        return state.dynamicInstant.computeLogical(null);
    }

    @State(Scope.Thread)
    public static class ReadOnlyState {

        ExpressionRuntimeSupport foldedLiteral;
        ExpressionRuntimeSupport defaultOnlySymbol;
        ExpressionRuntimeSupport dynamicInstant;

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionCompilationCache cache = new ExpressionCompilationCache(CacheConfig.defaults());
            ExpressionEnvironment emptyEnvironment = ExpressionEnvironmentBuilder.empty();
            ExpressionEnvironment defaultSymbolEnvironment = new ExpressionEnvironmentBuilder()
                    .registerExternalSymbol("threshold", BigDecimal.TEN, true)
                    .build();

            foldedLiteral = cache.compileRuntime("1 < 2", ExpressionResultType.LOGICAL, emptyEnvironment);
            defaultOnlySymbol = cache.compileRuntime("threshold > 0", ExpressionResultType.LOGICAL, defaultSymbolEnvironment);
            dynamicInstant = cache.compileRuntime("currTime = currTime", ExpressionResultType.LOGICAL, emptyEnvironment);
        }
    }
}
