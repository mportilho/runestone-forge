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
 * Cross-module comparison benchmark: expression-evaluator (legacy) vs exp-eval-mk2.
 *
 * <p>Covers six canonical scenarios derived from existing test suites:
 * <ol>
 *   <li><b>literalDense</b>  — 64 compiled-in numeric literals plus one variable (seed).
 *       Tests constant-folding and dense addition chains.</li>
 *   <li><b>variableChurn</b> — 12 variables, arithmetic mix (mul/add/sub).
 *       High binding churn per invocation.</li>
 *   <li><b>userFunction</b>  — 4 calls to a custom {@code weighted(a,b,c)} function.
 *       Exercises function dispatch and argument coercion.</li>
 *   <li><b>conditional</b>   — {@code if a > b then … else … endif} over 12 variables.
 *       Tests branching in the execution plan.</li>
 *   <li><b>logarithmChain</b>— 12 {@code ln()}/{@code lb()} calls over 12 variables.
 *       Exercises math-function dispatch and BigDecimal transcendentals.</li>
 *   <li><b>powerChain</b>    — 12 squarings of variables ({@code a^2 + b^2 − …}).
 *       Tests exponentiation path.</li>
 * </ol>
 *
 * <p><b>Two comparison groups</b>:
 * <ul>
 *   <li><b>Group 1</b> — {@code legacy.evaluate()} vs {@code mk2.compute()}</li>
 *   <li><b>Group 2</b> — {@code legacy.evaluate()} vs {@code mk2.computeWithAudit()}</li>
 * </ul>
 *
 * <p>Run with {@code -prof gc} to capture {@code B/op} (gc.alloc.rate.norm) alongside {@code ns/op}.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class CrossModuleComparisonBenchmark {

    // ── literalDense ──────────────────────────────────────────────────────────

    @Benchmark
    public BigDecimal literalDense_legacy(LiteralDenseState s) {
        CrossModuleExpressionBenchmarkSupport.applyLiteralSeed(
            s.legacy, CrossModuleExpressionBenchmarkSupport.literalSeed(s.legacyIndex++));
        return s.legacy.evaluate();
    }

    @Benchmark
    public BigDecimal literalDense_mk2Compute(LiteralDenseState s) {
        CrossModuleExpressionBenchmarkSupport.applyLiteralSeed(
            s.mk2, CrossModuleExpressionBenchmarkSupport.literalSeed(s.mk2Index++));
        return s.mk2.compute();
    }

    @Benchmark
    public AuditResult<BigDecimal> literalDense_mk2Audit(LiteralDenseState s) {
        CrossModuleExpressionBenchmarkSupport.applyLiteralSeed(
            s.mk2, CrossModuleExpressionBenchmarkSupport.literalSeed(s.mk2Index++));
        return s.mk2.computeWithAudit();
    }

    // ── variableChurn ─────────────────────────────────────────────────────────

    @Benchmark
    public BigDecimal variableChurn_legacy(VariableChurnState s) {
        CrossModuleExpressionBenchmarkSupport.applyFrame(
            s.legacy, CrossModuleExpressionBenchmarkSupport.variableFrame(s.legacyIndex++));
        return s.legacy.evaluate();
    }

    @Benchmark
    public BigDecimal variableChurn_mk2Compute(VariableChurnState s) {
        CrossModuleExpressionBenchmarkSupport.applyFrame(
            s.mk2, CrossModuleExpressionBenchmarkSupport.variableFrame(s.mk2Index++));
        return s.mk2.compute();
    }

    @Benchmark
    public AuditResult<BigDecimal> variableChurn_mk2Audit(VariableChurnState s) {
        CrossModuleExpressionBenchmarkSupport.applyFrame(
            s.mk2, CrossModuleExpressionBenchmarkSupport.variableFrame(s.mk2Index++));
        return s.mk2.computeWithAudit();
    }

    // ── userFunction ──────────────────────────────────────────────────────────

    @Benchmark
    public BigDecimal userFunction_legacy(UserFunctionState s) {
        CrossModuleExpressionBenchmarkSupport.applyFrame(
            s.legacy, CrossModuleExpressionBenchmarkSupport.userFunctionFrame(s.legacyIndex++));
        return s.legacy.evaluate();
    }

    @Benchmark
    public BigDecimal userFunction_mk2Compute(UserFunctionState s) {
        CrossModuleExpressionBenchmarkSupport.applyFrame(
            s.mk2, CrossModuleExpressionBenchmarkSupport.userFunctionFrame(s.mk2Index++));
        return s.mk2.compute();
    }

    @Benchmark
    public AuditResult<BigDecimal> userFunction_mk2Audit(UserFunctionState s) {
        CrossModuleExpressionBenchmarkSupport.applyFrame(
            s.mk2, CrossModuleExpressionBenchmarkSupport.userFunctionFrame(s.mk2Index++));
        return s.mk2.computeWithAudit();
    }

    // ── conditional ───────────────────────────────────────────────────────────

    @Benchmark
    public BigDecimal conditional_legacy(ConditionalState s) {
        CrossModuleExpressionBenchmarkSupport.applyFrame(
            s.legacy, CrossModuleExpressionBenchmarkSupport.variableFrame(s.legacyIndex++));
        return s.legacy.evaluate();
    }

    @Benchmark
    public BigDecimal conditional_mk2Compute(ConditionalState s) {
        CrossModuleExpressionBenchmarkSupport.applyFrame(
            s.mk2, CrossModuleExpressionBenchmarkSupport.variableFrame(s.mk2Index++));
        return s.mk2.compute();
    }

    @Benchmark
    public AuditResult<BigDecimal> conditional_mk2Audit(ConditionalState s) {
        CrossModuleExpressionBenchmarkSupport.applyFrame(
            s.mk2, CrossModuleExpressionBenchmarkSupport.variableFrame(s.mk2Index++));
        return s.mk2.computeWithAudit();
    }

    // ── logarithmChain ────────────────────────────────────────────────────────

    @Benchmark
    public BigDecimal logarithmChain_legacy(LogarithmChainState s) {
        CrossModuleExpressionBenchmarkSupport.applyFrame(
            s.legacy, CrossModuleExpressionBenchmarkSupport.variableFrame(s.legacyIndex++));
        return s.legacy.evaluate();
    }

    @Benchmark
    public BigDecimal logarithmChain_mk2Compute(LogarithmChainState s) {
        CrossModuleExpressionBenchmarkSupport.applyFrame(
            s.mk2, CrossModuleExpressionBenchmarkSupport.variableFrame(s.mk2Index++));
        return s.mk2.compute();
    }

    @Benchmark
    public AuditResult<BigDecimal> logarithmChain_mk2Audit(LogarithmChainState s) {
        CrossModuleExpressionBenchmarkSupport.applyFrame(
            s.mk2, CrossModuleExpressionBenchmarkSupport.variableFrame(s.mk2Index++));
        return s.mk2.computeWithAudit();
    }

    // ── powerChain ────────────────────────────────────────────────────────────

    @Benchmark
    public BigDecimal powerChain_legacy(PowerChainState s) {
        CrossModuleExpressionBenchmarkSupport.applyFrame(
            s.legacy, CrossModuleExpressionBenchmarkSupport.variableFrame(s.legacyIndex++));
        return s.legacy.evaluate();
    }

    @Benchmark
    public BigDecimal powerChain_mk2Compute(PowerChainState s) {
        CrossModuleExpressionBenchmarkSupport.applyFrame(
            s.mk2, CrossModuleExpressionBenchmarkSupport.variableFrame(s.mk2Index++));
        return s.mk2.compute();
    }

    @Benchmark
    public AuditResult<BigDecimal> powerChain_mk2Audit(PowerChainState s) {
        CrossModuleExpressionBenchmarkSupport.applyFrame(
            s.mk2, CrossModuleExpressionBenchmarkSupport.variableFrame(s.mk2Index++));
        return s.mk2.computeWithAudit();
    }

    // ── States ────────────────────────────────────────────────────────────────

    @State(Scope.Thread)
    public static class LiteralDenseState {
        Expression legacy;
        MathExpression mk2;
        int legacyIndex;
        int mk2Index;

        @Setup(Level.Trial)
        public void setUp() {
            legacy = CrossModuleExpressionBenchmarkSupport.newLegacyLiteralDenseExpression();
            mk2 = CrossModuleExpressionBenchmarkSupport.newMk2LiteralDenseExpression();
        }
    }

    @State(Scope.Thread)
    public static class VariableChurnState {
        Expression legacy;
        MathExpression mk2;
        int legacyIndex;
        int mk2Index;

        @Setup(Level.Trial)
        public void setUp() {
            legacy = CrossModuleExpressionBenchmarkSupport.newLegacyVariableChurnExpression();
            mk2 = CrossModuleExpressionBenchmarkSupport.newMk2VariableChurnExpression();
        }
    }

    @State(Scope.Thread)
    public static class UserFunctionState {
        Expression legacy;
        MathExpression mk2;
        int legacyIndex;
        int mk2Index;

        @Setup(Level.Trial)
        public void setUp() {
            legacy = CrossModuleExpressionBenchmarkSupport.newLegacyUserFunctionExpression();
            mk2 = CrossModuleExpressionBenchmarkSupport.newMk2UserFunctionExpression();
        }
    }

    @State(Scope.Thread)
    public static class ConditionalState {
        Expression legacy;
        MathExpression mk2;
        int legacyIndex;
        int mk2Index;

        @Setup(Level.Trial)
        public void setUp() {
            legacy = CrossModuleExpressionBenchmarkSupport.newLegacyConditionalExpression();
            mk2 = CrossModuleExpressionBenchmarkSupport.newMk2ConditionalExpression();
        }
    }

    @State(Scope.Thread)
    public static class LogarithmChainState {
        Expression legacy;
        MathExpression mk2;
        int legacyIndex;
        int mk2Index;

        @Setup(Level.Trial)
        public void setUp() {
            legacy = CrossModuleExpressionBenchmarkSupport.newLegacyLogarithmChainExpression();
            mk2 = CrossModuleExpressionBenchmarkSupport.newMk2LogarithmChainExpression();
        }
    }

    @State(Scope.Thread)
    public static class PowerChainState {
        Expression legacy;
        MathExpression mk2;
        int legacyIndex;
        int mk2Index;

        @Setup(Level.Trial)
        public void setUp() {
            legacy = CrossModuleExpressionBenchmarkSupport.newLegacyPowerChainExpression();
            mk2 = CrossModuleExpressionBenchmarkSupport.newMk2PowerChainExpression();
        }
    }
}
