package com.runestone.expeval2.perf.jmh;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval2.api.MathExpression;
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
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class CrossModuleExpressionEngineBenchmark {

    @Benchmark
    public BigDecimal legacyLiteralDense(LiteralDenseState state) {
        return state.evaluateLegacy();
    }

    @Benchmark
    public BigDecimal mk2LiteralDense(LiteralDenseState state) {
        return state.evaluateMk2();
    }

    @Benchmark
    public BigDecimal legacyVariableChurn(VariableChurnState state) {
        return state.evaluateLegacy();
    }

    @Benchmark
    public BigDecimal mk2VariableChurn(VariableChurnState state) {
        return state.evaluateMk2();
    }

    @Benchmark
    public BigDecimal legacyUserFunction(UserFunctionState state) {
        return state.evaluateLegacy();
    }

    @Benchmark
    public BigDecimal mk2UserFunction(UserFunctionState state) {
        return state.evaluateMk2();
    }

    @State(Scope.Thread)
    public static class LiteralDenseState {

        private Expression legacy;
        private MathExpression mk2;
        private int legacyIndex;
        private int mk2Index;

        @Setup(Level.Trial)
        public void setUp() {
            legacy = CrossModuleExpressionBenchmarkSupport.newLegacyLiteralDenseExpression();
            mk2 = CrossModuleExpressionBenchmarkSupport.newMk2LiteralDenseExpression();
        }

        private BigDecimal evaluateLegacy() {
            CrossModuleExpressionBenchmarkSupport.applyLiteralSeed(
                legacy,
                CrossModuleExpressionBenchmarkSupport.literalSeed(legacyIndex++)
            );
            return legacy.evaluate();
        }

        private BigDecimal evaluateMk2() {
            CrossModuleExpressionBenchmarkSupport.applyLiteralSeed(
                mk2,
                CrossModuleExpressionBenchmarkSupport.literalSeed(mk2Index++)
            );
            return mk2.compute();
        }
    }

    @State(Scope.Thread)
    public static class VariableChurnState {

        private Expression legacy;
        private MathExpression mk2;
        private int legacyIndex;
        private int mk2Index;

        @Setup(Level.Trial)
        public void setUp() {
            legacy = CrossModuleExpressionBenchmarkSupport.newLegacyVariableChurnExpression();
            mk2 = CrossModuleExpressionBenchmarkSupport.newMk2VariableChurnExpression();
        }

        private BigDecimal evaluateLegacy() {
            CrossModuleExpressionBenchmarkSupport.applyFrame(
                legacy,
                CrossModuleExpressionBenchmarkSupport.variableFrame(legacyIndex++)
            );
            return legacy.evaluate();
        }

        private BigDecimal evaluateMk2() {
            CrossModuleExpressionBenchmarkSupport.applyFrame(
                mk2,
                CrossModuleExpressionBenchmarkSupport.variableFrame(mk2Index++)
            );
            return mk2.compute();
        }
    }

    @State(Scope.Thread)
    public static class UserFunctionState {

        private Expression legacy;
        private MathExpression mk2;
        private int legacyIndex;
        private int mk2Index;

        @Setup(Level.Trial)
        public void setUp() {
            legacy = CrossModuleExpressionBenchmarkSupport.newLegacyUserFunctionExpression();
            mk2 = CrossModuleExpressionBenchmarkSupport.newMk2UserFunctionExpression();
        }

        private BigDecimal evaluateLegacy() {
            CrossModuleExpressionBenchmarkSupport.applyFrame(
                legacy,
                CrossModuleExpressionBenchmarkSupport.userFunctionFrame(legacyIndex++)
            );
            return legacy.evaluate();
        }

        private BigDecimal evaluateMk2() {
            CrossModuleExpressionBenchmarkSupport.applyFrame(
                mk2,
                CrossModuleExpressionBenchmarkSupport.userFunctionFrame(mk2Index++)
            );
            return mk2.compute();
        }
    }
}
