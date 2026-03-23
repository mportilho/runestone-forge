package com.runestone.expeval2.perf.jmh;

import com.runestone.expeval2.internal.runtime.ExpressionRuntimeSupport;
import com.runestone.expeval2.perf.CrossModuleExpressionBenchmarkSupport;
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
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks the six canonical evaluation scenarios (PERF-039 / PERF-040) using the
 * standard evaluator ({@link com.runestone.expeval2.internal.runtime.AbstractRawObjectEvaluator}).
 *
 * <p>Run via {@link RawObjectEvaluatorBenchmarkRunner} or with the JMH script:
 * <pre>
 *   run-jmh.sh . RawObjectEvaluatorBenchmark /tmp/performance-benchmark/perf040.json
 * </pre>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class RawObjectEvaluatorBenchmark {

    // ── literalDense ──────────────────────────────────────────────────────────

    @Benchmark
    public BigDecimal literalDense(LiteralDenseState s) {
        return s.runtime.computeMath(CrossModuleExpressionBenchmarkSupport.literalSeedToMap(
            CrossModuleExpressionBenchmarkSupport.literalSeed(s.index++)));
    }

    // ── variableChurn ─────────────────────────────────────────────────────────

    @Benchmark
    public BigDecimal variableChurn(VariableChurnState s) {
        return s.runtime.computeMath(CrossModuleExpressionBenchmarkSupport.frameToMap(
            CrossModuleExpressionBenchmarkSupport.variableFrame(s.index++)));
    }

    // ── userFunction ──────────────────────────────────────────────────────────

    @Benchmark
    public BigDecimal userFunction(UserFunctionState s) {
        return s.runtime.computeMath(CrossModuleExpressionBenchmarkSupport.frameToMap(
            CrossModuleExpressionBenchmarkSupport.userFunctionFrame(s.index++)));
    }

    // ── conditional ───────────────────────────────────────────────────────────

    @Benchmark
    public BigDecimal conditional(ConditionalState s) {
        return s.runtime.computeMath(CrossModuleExpressionBenchmarkSupport.frameToMap(
            CrossModuleExpressionBenchmarkSupport.variableFrame(s.index++)));
    }

    // ── logarithmChain ────────────────────────────────────────────────────────

    @Benchmark
    public BigDecimal logarithmChain(LogarithmChainState s) {
        return s.runtime.computeMath(CrossModuleExpressionBenchmarkSupport.frameToMap(
            CrossModuleExpressionBenchmarkSupport.variableFrame(s.index++)));
    }

    // ── powerChain ────────────────────────────────────────────────────────────

    @Benchmark
    public BigDecimal powerChain(PowerChainState s) {
        return s.runtime.computeMath(CrossModuleExpressionBenchmarkSupport.frameToMap(
            CrossModuleExpressionBenchmarkSupport.variableFrame(s.index++)));
    }

    // ── States ────────────────────────────────────────────────────────────────

    @State(Scope.Thread)
    public static class LiteralDenseState {
        ExpressionRuntimeSupport runtime;
        int index;

        @Setup(Level.Trial)
        public void setUp() {
            runtime = ExpressionRuntimeSupport.compileMath(
                CrossModuleExpressionBenchmarkSupport.LITERAL_DENSE_EXPRESSION,
                CrossModuleExpressionBenchmarkSupport.emptyEnvironment());
        }
    }

    @State(Scope.Thread)
    public static class VariableChurnState {
        ExpressionRuntimeSupport runtime;
        int index;

        @Setup(Level.Trial)
        public void setUp() {
            runtime = ExpressionRuntimeSupport.compileMath(
                CrossModuleExpressionBenchmarkSupport.VARIABLE_CHURN_EXPRESSION,
                CrossModuleExpressionBenchmarkSupport.emptyEnvironment());
        }
    }

    @State(Scope.Thread)
    public static class UserFunctionState {
        ExpressionRuntimeSupport runtime;
        int index;

        @Setup(Level.Trial)
        public void setUp() {
            runtime = ExpressionRuntimeSupport.compileMath(
                CrossModuleExpressionBenchmarkSupport.USER_FUNCTION_EXPRESSION,
                CrossModuleExpressionBenchmarkSupport.userFunctionEnvironment());
        }
    }

    @State(Scope.Thread)
    public static class ConditionalState {
        ExpressionRuntimeSupport runtime;
        int index;

        @Setup(Level.Trial)
        public void setUp() {
            runtime = ExpressionRuntimeSupport.compileMath(
                CrossModuleExpressionBenchmarkSupport.CONDITIONAL_EXPRESSION,
                CrossModuleExpressionBenchmarkSupport.emptyEnvironment());
        }
    }

    @State(Scope.Thread)
    public static class LogarithmChainState {
        ExpressionRuntimeSupport runtime;
        int index;

        @Setup(Level.Trial)
        public void setUp() {
            runtime = ExpressionRuntimeSupport.compileMath(
                CrossModuleExpressionBenchmarkSupport.LOGARITHM_CHAIN_EXPRESSION,
                CrossModuleExpressionBenchmarkSupport.logarithmEnvironment());
        }
    }

    @State(Scope.Thread)
    public static class PowerChainState {
        ExpressionRuntimeSupport runtime;
        int index;

        @Setup(Level.Trial)
        public void setUp() {
            runtime = ExpressionRuntimeSupport.compileMath(
                CrossModuleExpressionBenchmarkSupport.POWER_CHAIN_EXPRESSION,
                CrossModuleExpressionBenchmarkSupport.emptyEnvironment());
        }
    }
}
