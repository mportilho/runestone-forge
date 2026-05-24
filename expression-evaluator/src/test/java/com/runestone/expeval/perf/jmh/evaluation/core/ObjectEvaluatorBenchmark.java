package com.runestone.expeval.perf.jmh.evaluation.core;

import com.runestone.expeval.api.MathExpression;
import com.runestone.expeval.perf.ExpressionBenchmarkSupport;
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
 * standard evaluator ({@link com.runestone.expeval.internal.runtime.AbstractObjectEvaluator}).
 *
 * <p>Run via {@link ObjectEvaluatorBenchmarkRunner} or with the JMH script:
 * <pre>
 *   run-jmh.sh . ObjectEvaluatorBenchmark /tmp/performance-benchmark/perf040.json
 * </pre>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class ObjectEvaluatorBenchmark {

    // ── literalDense ──────────────────────────────────────────────────────────

    @Benchmark
    public BigDecimal literalDense(LiteralDenseState s) {
        return s.expression.compute(ExpressionBenchmarkSupport.literalSeedToMap(
            ExpressionBenchmarkSupport.literalSeed(s.index++)));
    }

    // ── variableChurn ─────────────────────────────────────────────────────────

    @Benchmark
    public BigDecimal variableChurn(VariableChurnState s) {
        return s.expression.compute(ExpressionBenchmarkSupport.frameToMap(
            ExpressionBenchmarkSupport.variableFrame(s.index++)));
    }

    // ── userFunction ──────────────────────────────────────────────────────────

    @Benchmark
    public BigDecimal userFunction(UserFunctionState s) {
        return s.expression.compute(ExpressionBenchmarkSupport.frameToMap(
            ExpressionBenchmarkSupport.userFunctionFrame(s.index++)));
    }

    // ── conditional ───────────────────────────────────────────────────────────

    @Benchmark
    public BigDecimal conditional(ConditionalState s) {
        return s.expression.compute(ExpressionBenchmarkSupport.frameToMap(
            ExpressionBenchmarkSupport.variableFrame(s.index++)));
    }

    // ── logarithmChain ────────────────────────────────────────────────────────

    @Benchmark
    public BigDecimal logarithmChain(LogarithmChainState s) {
        return s.expression.compute(ExpressionBenchmarkSupport.frameToMap(
            ExpressionBenchmarkSupport.variableFrame(s.index++)));
    }

    // ── powerChain ────────────────────────────────────────────────────────────

    @Benchmark
    public BigDecimal powerChain(PowerChainState s) {
        return s.expression.compute(ExpressionBenchmarkSupport.frameToMap(
            ExpressionBenchmarkSupport.variableFrame(s.index++)));
    }

    // ── States ────────────────────────────────────────────────────────────────

    @State(Scope.Thread)
    public static class LiteralDenseState {
        MathExpression expression;
        int index;

        @Setup(Level.Trial)
        public void setUp() {
            expression = MathExpression.compile(
                ExpressionBenchmarkSupport.LITERAL_DENSE_EXPRESSION,
                ExpressionBenchmarkSupport.emptyEnvironment());
        }
    }

    @State(Scope.Thread)
    public static class VariableChurnState {
        MathExpression expression;
        int index;

        @Setup(Level.Trial)
        public void setUp() {
            expression = MathExpression.compile(
                ExpressionBenchmarkSupport.VARIABLE_CHURN_EXPRESSION,
                ExpressionBenchmarkSupport.emptyEnvironment());
        }
    }

    @State(Scope.Thread)
    public static class UserFunctionState {
        MathExpression expression;
        int index;

        @Setup(Level.Trial)
        public void setUp() {
            expression = MathExpression.compile(
                ExpressionBenchmarkSupport.USER_FUNCTION_EXPRESSION,
                ExpressionBenchmarkSupport.userFunctionEnvironment());
        }
    }

    @State(Scope.Thread)
    public static class ConditionalState {
        MathExpression expression;
        int index;

        @Setup(Level.Trial)
        public void setUp() {
            expression = MathExpression.compile(
                ExpressionBenchmarkSupport.CONDITIONAL_EXPRESSION,
                ExpressionBenchmarkSupport.emptyEnvironment());
        }
    }

    @State(Scope.Thread)
    public static class LogarithmChainState {
        MathExpression expression;
        int index;

        @Setup(Level.Trial)
        public void setUp() {
            expression = MathExpression.compile(
                ExpressionBenchmarkSupport.LOGARITHM_CHAIN_EXPRESSION,
                ExpressionBenchmarkSupport.logarithmEnvironment());
        }
    }

    @State(Scope.Thread)
    public static class PowerChainState {
        MathExpression expression;
        int index;

        @Setup(Level.Trial)
        public void setUp() {
            expression = MathExpression.compile(
                ExpressionBenchmarkSupport.POWER_CHAIN_EXPRESSION,
                ExpressionBenchmarkSupport.emptyEnvironment());
        }
    }
}
