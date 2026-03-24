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
 * Two-group cross-module comparison between the legacy {@code expression-evaluator} and
 * the {@code exp-eval-mk2} engine:
 *
 * <ul>
 *   <li><b>Group A (compute)</b> — {@code legacy.evaluate()} vs {@code mk2.compute()}.
 *       Baseline comparison: how much faster is the mk2 engine for plain evaluation?</li>
 *   <li><b>Group B (computeWithAudit)</b> — {@code legacy.evaluate()} vs
 *       {@code mk2.computeWithAudit()}.
 *       Answers: does the audit overhead erode mk2's advantage over the legacy engine?</li>
 * </ul>
 *
 * <p>Scenarios covered: {@code variableChurn} (12 variable reads),
 * {@code userFunction} (4 function calls), {@code conditional} (branch + variable reads).
 * The {@code logarithmChain} and {@code powerChain} scenarios are excluded because their
 * cost is dominated by BigDecimalMath precision, masking evaluator overhead.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class CrossModuleAuditComparisonBenchmark {

    // ── variableChurn — 12 variable reads ─────────────────────────────────────

    @Benchmark
    public BigDecimal groupA_legacy_variableChurn(VariableChurnState state) {
        CrossModuleExpressionBenchmarkSupport.applyFrame(
            state.legacy,
            CrossModuleExpressionBenchmarkSupport.variableFrame(state.index++));
        return state.legacy.evaluate();
    }

    @Benchmark
    public BigDecimal groupA_mk2_variableChurn(VariableChurnState state) {
        return state.mk2.compute(
            CrossModuleExpressionBenchmarkSupport.frameToMap(
                CrossModuleExpressionBenchmarkSupport.variableFrame(state.index++)));
    }

    @Benchmark
    public AuditResult<BigDecimal> groupB_mk2Audit_variableChurn(VariableChurnState state) {
        return state.mk2.computeWithAudit(
            CrossModuleExpressionBenchmarkSupport.frameToMap(
                CrossModuleExpressionBenchmarkSupport.variableFrame(state.index++)));
    }

    // ── userFunction — 4 function calls ───────────────────────────────────────

    @Benchmark
    public BigDecimal groupA_legacy_userFunction(UserFunctionState state) {
        CrossModuleExpressionBenchmarkSupport.applyFrame(
            state.legacy,
            CrossModuleExpressionBenchmarkSupport.userFunctionFrame(state.index++));
        return state.legacy.evaluate();
    }

    @Benchmark
    public BigDecimal groupA_mk2_userFunction(UserFunctionState state) {
        return state.mk2.compute(
            CrossModuleExpressionBenchmarkSupport.frameToMap(
                CrossModuleExpressionBenchmarkSupport.userFunctionFrame(state.index++)));
    }

    @Benchmark
    public AuditResult<BigDecimal> groupB_mk2Audit_userFunction(UserFunctionState state) {
        return state.mk2.computeWithAudit(
            CrossModuleExpressionBenchmarkSupport.frameToMap(
                CrossModuleExpressionBenchmarkSupport.userFunctionFrame(state.index++)));
    }

    // ── conditional — branch evaluation ───────────────────────────────────────

    @Benchmark
    public BigDecimal groupA_legacy_conditional(ConditionalState state) {
        CrossModuleExpressionBenchmarkSupport.applyFrame(
            state.legacy,
            CrossModuleExpressionBenchmarkSupport.variableFrame(state.index++));
        return state.legacy.evaluate();
    }

    @Benchmark
    public BigDecimal groupA_mk2_conditional(ConditionalState state) {
        return state.mk2.compute(
            CrossModuleExpressionBenchmarkSupport.frameToMap(
                CrossModuleExpressionBenchmarkSupport.variableFrame(state.index++)));
    }

    @Benchmark
    public AuditResult<BigDecimal> groupB_mk2Audit_conditional(ConditionalState state) {
        return state.mk2.computeWithAudit(
            CrossModuleExpressionBenchmarkSupport.frameToMap(
                CrossModuleExpressionBenchmarkSupport.variableFrame(state.index++)));
    }

    // ── States ────────────────────────────────────────────────────────────────

    @State(Scope.Thread)
    public static class VariableChurnState {
        Expression legacy;
        MathExpression mk2;
        int index;

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
        int index;

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
        int index;

        @Setup(Level.Trial)
        public void setUp() {
            legacy = CrossModuleExpressionBenchmarkSupport.newLegacyConditionalExpression();
            mk2 = CrossModuleExpressionBenchmarkSupport.newMk2ConditionalExpression();
        }
    }
}
