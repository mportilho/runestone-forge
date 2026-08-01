package com.runestone.expeval_mk3.perf.jmh;

import com.runestone.expeval_mk3.api.ExpressionCompilationResult;
import com.runestone.expeval_mk3.api.ExpressionCompiler;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.LogicalExpression;
import com.runestone.expeval_mk3.api.MathExpression;
import com.runestone.expeval_mk3.api.ResultExpression;
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
import org.openjdk.jmh.infra.Blackhole;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Phase 5 reproducible baseline (issue #105): hot arithmetic compute, hot logical compute, full
 * uncached compilation, and fixed-size Public Materialization. This is a baseline record, not a
 * before/after comparison and not a CI gate.
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class Phase5BaselineBenchmark {

    @Benchmark
    public void arithmeticCompute(HotViews views, Blackhole blackhole) {
        blackhole.consume(views.arithmetic.compute(views.arithmeticOverrides));
    }

    @Benchmark
    public void logicalCompute(HotViews views, Blackhole blackhole) {
        blackhole.consume(views.logical.compute(views.logicalOverrides));
    }

    @Benchmark
    public void fullUncachedCompilation(CompilationInputs inputs, Blackhole blackhole) {
        ExpressionCompilationResult result = ExpressionCompiler.compile(inputs.source, inputs.environment);
        blackhole.consume(result);
    }

    @Benchmark
    public void materializationCompute(HotViews views, Blackhole blackhole) {
        blackhole.consume(views.materialization.compute());
    }

    @State(Scope.Benchmark)
    public static class HotViews {

        private MathExpression arithmetic;
        private Map<String, Object> arithmeticOverrides;
        private LogicalExpression logical;
        private Map<String, Object> logicalOverrides;
        private ResultExpression materialization;

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = ExpressionEnvironment.builder()
                    .externalSymbol("a", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .externalSymbol("b", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .externalSymbol("c", Boolean.TRUE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .build();

            arithmetic = ExpressionCompiler.compileOrThrow("a + b * 2", environment).asMath();
            arithmeticOverrides = Map.of("a", new BigDecimal("3"), "b", new BigDecimal("5"));

            // xor never short-circuits, so every operand is evaluated on every call.
            logical = ExpressionCompiler.compileOrThrow(
                            "(a > 0) xor (b > 0) xor c", environment)
                    .asLogical();
            logicalOverrides = Map.of("a", new BigDecimal("3"), "b", new BigDecimal("-5"), "c", Boolean.FALSE);

            materialization = ExpressionCompiler.compileOrThrow(
                            "[1, 2, 3, 4, 5, 6, 7, 8]", ExpressionEnvironment.standard())
                    .asResult();
        }
    }

    @State(Scope.Benchmark)
    public static class CompilationInputs {

        private String source;
        private ExpressionEnvironment environment;

        @Setup(Level.Trial)
        public void setUp() {
            environment = ExpressionEnvironment.builder()
                    .externalSymbol("a", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .externalSymbol("b", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .build();
            source = "a + b * 2";
        }
    }
}
