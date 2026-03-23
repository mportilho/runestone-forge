package com.runestone.expeval2.perf.jmh;

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
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Measures the overhead of {@code computeWithAudit()} versus {@code compute()} across three
 * scenarios that exercise different audit code-paths:
 *
 * <ul>
 *   <li><b>variableChurn</b> — 12 variables, no assignments, no function calls.
 *       Exercises {@code VariableRead} events on the read-only scope path.</li>
 *   <li><b>assignedVariable</b> — 1 simple assignment followed by variable reads.
 *       Forces the mutable-scope (HashMap copy) path and exercises {@code AssignmentEvent}.</li>
 *   <li><b>userFunction</b> — 4 calls to {@code weighted(a,b,c)}.
 *       Exercises {@code FunctionCall} events including {@code List.of(args)} per call.</li>
 * </ul>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class AuditOverheadBenchmark {

    // ── variableChurn ──────────────────────────────────────────────────────────

    @Benchmark
    public BigDecimal variableChurnNoAudit(VariableChurnAuditState state) {
        return state.expression.compute(CrossModuleExpressionBenchmarkSupport.frameToMap(CrossModuleExpressionBenchmarkSupport.variableFrame(state.index++)));
    }

    @Benchmark
    public AuditResult<BigDecimal> variableChurnWithAudit(VariableChurnAuditState state) {
        return state.expression.computeWithAudit(CrossModuleExpressionBenchmarkSupport.frameToMap(CrossModuleExpressionBenchmarkSupport.variableFrame(state.index++)));
    }

    // ── assignedVariable ──────────────────────────────────────────────────────

    @Benchmark
    public BigDecimal assignedVariableNoAudit(AssignedVariableAuditState state) {
        return state.expression.compute(CrossModuleExpressionBenchmarkSupport.frameToMap(CrossModuleExpressionBenchmarkSupport.variableFrame(state.index++)));
    }

    @Benchmark
    public AuditResult<BigDecimal> assignedVariableWithAudit(AssignedVariableAuditState state) {
        return state.expression.computeWithAudit(CrossModuleExpressionBenchmarkSupport.frameToMap(CrossModuleExpressionBenchmarkSupport.variableFrame(state.index++)));
    }

    // ── userFunction ──────────────────────────────────────────────────────────

    @Benchmark
    public BigDecimal userFunctionNoAudit(UserFunctionAuditState state) {
        return state.expression.compute(CrossModuleExpressionBenchmarkSupport.frameToMap(CrossModuleExpressionBenchmarkSupport.userFunctionFrame(state.index++)));
    }

    @Benchmark
    public AuditResult<BigDecimal> userFunctionWithAudit(UserFunctionAuditState state) {
        return state.expression.computeWithAudit(CrossModuleExpressionBenchmarkSupport.frameToMap(CrossModuleExpressionBenchmarkSupport.userFunctionFrame(state.index++)));
    }

    // ── States ────────────────────────────────────────────────────────────────

    @State(Scope.Thread)
    public static class VariableChurnAuditState {
        MathExpression expression;
        int index;

        @Setup(Level.Trial)
        public void setUp() {
            expression = CrossModuleExpressionBenchmarkSupport.newMk2VariableChurnExpression();
        }
    }

    @State(Scope.Thread)
    public static class AssignedVariableAuditState {
        // 1 simple assignment + uses the result alongside 9 more variable reads.
        // Forces mutable-scope (HashMap copy) path in ExpressionRuntimeSupport.
        // Expression reuses the 12 standard variable names from CrossModuleExpressionBenchmarkSupport.
        static final String EXPRESSION =
            "r = a + b * c - d; r * e + f - g * h + i - j * k + l";

        MathExpression expression;
        int index;

        @Setup(Level.Trial)
        public void setUp() {
            expression = MathExpression.compile(EXPRESSION);
        }
    }

    @State(Scope.Thread)
    public static class UserFunctionAuditState {
        MathExpression expression;
        int index;

        @Setup(Level.Trial)
        public void setUp() {
            expression = CrossModuleExpressionBenchmarkSupport.newMk2UserFunctionExpression();
        }
    }
}
