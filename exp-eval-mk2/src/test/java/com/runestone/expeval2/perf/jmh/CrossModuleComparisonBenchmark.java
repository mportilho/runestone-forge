package com.runestone.expeval2.perf.jmh;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval2.api.AuditResult;
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

/**
 * Cross-module benchmark for two comparison groups over the same six scenarios:
 *
 * <ul>
 *   <li>Group 1: legacy {@link Expression#evaluate()} vs mk2 {@link MathExpression#compute(java.util.Map)}</li>
 *   <li>Group 2: legacy {@link Expression#evaluate()} vs mk2 {@link MathExpression#computeWithAudit(java.util.Map)}</li>
 * </ul>
 *
 * <p>Run with {@code GCProfiler} to capture {@code B/op} ({@code gc.alloc.rate.norm}) in addition
 * to the primary {@code ns/op} metric.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class CrossModuleComparisonBenchmark {

    @Benchmark
    public BigDecimal literalDense_legacy(LiteralDenseState state) {
        return state.evaluateLegacy();
    }

    @Benchmark
    public BigDecimal literalDense_mk2Compute(LiteralDenseState state) {
        return state.evaluateMk2Compute();
    }

    @Benchmark
    public AuditResult<BigDecimal> literalDense_mk2Audit(LiteralDenseState state) {
        return state.evaluateMk2Audit();
    }

    @Benchmark
    public BigDecimal variableChurn_legacy(VariableChurnState state) {
        return state.evaluateLegacy();
    }

    @Benchmark
    public BigDecimal variableChurn_mk2Compute(VariableChurnState state) {
        return state.evaluateMk2Compute();
    }

    @Benchmark
    public AuditResult<BigDecimal> variableChurn_mk2Audit(VariableChurnState state) {
        return state.evaluateMk2Audit();
    }

    @Benchmark
    public BigDecimal userFunction_legacy(UserFunctionState state) {
        return state.evaluateLegacy();
    }

    @Benchmark
    public BigDecimal userFunction_mk2Compute(UserFunctionState state) {
        return state.evaluateMk2Compute();
    }

    @Benchmark
    public AuditResult<BigDecimal> userFunction_mk2Audit(UserFunctionState state) {
        return state.evaluateMk2Audit();
    }

    @Benchmark
    public BigDecimal conditional_legacy(ConditionalState state) {
        return state.evaluateLegacy();
    }

    @Benchmark
    public BigDecimal conditional_mk2Compute(ConditionalState state) {
        return state.evaluateMk2Compute();
    }

    @Benchmark
    public AuditResult<BigDecimal> conditional_mk2Audit(ConditionalState state) {
        return state.evaluateMk2Audit();
    }

    @Benchmark
    public BigDecimal powerChain_legacy(PowerChainState state) {
        return state.evaluateLegacy();
    }

    @Benchmark
    public BigDecimal powerChain_mk2Compute(PowerChainState state) {
        return state.evaluateMk2Compute();
    }

    @Benchmark
    public AuditResult<BigDecimal> powerChain_mk2Audit(PowerChainState state) {
        return state.evaluateMk2Audit();
    }

    @Benchmark
    public BigDecimal logarithmChain_legacy(LogarithmChainState state) {
        return state.evaluateLegacy();
    }

    @Benchmark
    public BigDecimal logarithmChain_mk2Compute(LogarithmChainState state) {
        return state.evaluateMk2Compute();
    }

    @Benchmark
    public AuditResult<BigDecimal> logarithmChain_mk2Audit(LogarithmChainState state) {
        return state.evaluateMk2Audit();
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

        private BigDecimal evaluateMk2Compute() {
            return mk2.compute(
                CrossModuleExpressionBenchmarkSupport.literalSeedToMap(
                    CrossModuleExpressionBenchmarkSupport.literalSeed(mk2Index++)
                )
            );
        }

        private AuditResult<BigDecimal> evaluateMk2Audit() {
            return mk2.computeWithAudit(
                CrossModuleExpressionBenchmarkSupport.literalSeedToMap(
                    CrossModuleExpressionBenchmarkSupport.literalSeed(mk2Index++)
                )
            );
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

        private BigDecimal evaluateMk2Compute() {
            return mk2.compute(
                CrossModuleExpressionBenchmarkSupport.frameToMap(
                    CrossModuleExpressionBenchmarkSupport.variableFrame(mk2Index++)
                )
            );
        }

        private AuditResult<BigDecimal> evaluateMk2Audit() {
            return mk2.computeWithAudit(
                CrossModuleExpressionBenchmarkSupport.frameToMap(
                    CrossModuleExpressionBenchmarkSupport.variableFrame(mk2Index++)
                )
            );
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

        private BigDecimal evaluateMk2Compute() {
            return mk2.compute(
                CrossModuleExpressionBenchmarkSupport.frameToMap(
                    CrossModuleExpressionBenchmarkSupport.userFunctionFrame(mk2Index++)
                )
            );
        }

        private AuditResult<BigDecimal> evaluateMk2Audit() {
            return mk2.computeWithAudit(
                CrossModuleExpressionBenchmarkSupport.frameToMap(
                    CrossModuleExpressionBenchmarkSupport.userFunctionFrame(mk2Index++)
                )
            );
        }
    }

    @State(Scope.Thread)
    public static class ConditionalState {

        private Expression legacy;
        private MathExpression mk2;
        private int legacyIndex;
        private int mk2Index;

        @Setup(Level.Trial)
        public void setUp() {
            legacy = CrossModuleExpressionBenchmarkSupport.newLegacyConditionalExpression();
            mk2 = CrossModuleExpressionBenchmarkSupport.newMk2ConditionalExpression();
        }

        private BigDecimal evaluateLegacy() {
            CrossModuleExpressionBenchmarkSupport.applyFrame(
                legacy,
                CrossModuleExpressionBenchmarkSupport.variableFrame(legacyIndex++)
            );
            return legacy.evaluate();
        }

        private BigDecimal evaluateMk2Compute() {
            return mk2.compute(
                CrossModuleExpressionBenchmarkSupport.frameToMap(
                    CrossModuleExpressionBenchmarkSupport.variableFrame(mk2Index++)
                )
            );
        }

        private AuditResult<BigDecimal> evaluateMk2Audit() {
            return mk2.computeWithAudit(
                CrossModuleExpressionBenchmarkSupport.frameToMap(
                    CrossModuleExpressionBenchmarkSupport.variableFrame(mk2Index++)
                )
            );
        }
    }

    @State(Scope.Thread)
    public static class PowerChainState {

        private Expression legacy;
        private MathExpression mk2;
        private int legacyIndex;
        private int mk2Index;

        @Setup(Level.Trial)
        public void setUp() {
            legacy = CrossModuleExpressionBenchmarkSupport.newLegacyPowerChainExpression();
            mk2 = CrossModuleExpressionBenchmarkSupport.newMk2PowerChainExpression();
        }

        private BigDecimal evaluateLegacy() {
            CrossModuleExpressionBenchmarkSupport.applyFrame(
                legacy,
                CrossModuleExpressionBenchmarkSupport.variableFrame(legacyIndex++)
            );
            return legacy.evaluate();
        }

        private BigDecimal evaluateMk2Compute() {
            return mk2.compute(
                CrossModuleExpressionBenchmarkSupport.frameToMap(
                    CrossModuleExpressionBenchmarkSupport.variableFrame(mk2Index++)
                )
            );
        }

        private AuditResult<BigDecimal> evaluateMk2Audit() {
            return mk2.computeWithAudit(
                CrossModuleExpressionBenchmarkSupport.frameToMap(
                    CrossModuleExpressionBenchmarkSupport.variableFrame(mk2Index++)
                )
            );
        }
    }

    @State(Scope.Thread)
    public static class LogarithmChainState {

        private Expression legacy;
        private MathExpression mk2;
        private int legacyIndex;
        private int mk2Index;

        @Setup(Level.Trial)
        public void setUp() {
            legacy = CrossModuleExpressionBenchmarkSupport.newLegacyLogarithmChainExpression();
            mk2 = CrossModuleExpressionBenchmarkSupport.newMk2LogarithmChainExpression();
        }

        private BigDecimal evaluateLegacy() {
            CrossModuleExpressionBenchmarkSupport.applyFrame(
                legacy,
                CrossModuleExpressionBenchmarkSupport.variableFrame(legacyIndex++)
            );
            return legacy.evaluate();
        }

        private BigDecimal evaluateMk2Compute() {
            return mk2.compute(
                CrossModuleExpressionBenchmarkSupport.frameToMap(
                    CrossModuleExpressionBenchmarkSupport.variableFrame(mk2Index++)
                )
            );
        }

        private AuditResult<BigDecimal> evaluateMk2Audit() {
            return mk2.computeWithAudit(
                CrossModuleExpressionBenchmarkSupport.frameToMap(
                    CrossModuleExpressionBenchmarkSupport.variableFrame(mk2Index++)
                )
            );
        }
    }
}
