package com.runestone.expeval2.perf.jmh;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval2.api.AssignmentExpression;
import com.runestone.expeval2.api.AuditResult;
import com.runestone.expeval2.api.MathExpression;
import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.environment.ExpressionEnvironmentBuilder;
import com.runestone.expeval2.internal.grammar.ExpressionResultType;
import com.runestone.expeval2.internal.runtime.ExpressionCompiler;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
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

    private static final String MATH_LITERAL_DENSE_EXPRESSION = buildMathLiteralDenseExpression();
    private static final String LOGICAL_MIXED_LITERAL_EXPRESSION = buildLogicalMixedLiteralExpression();

    // ── literalDense ──────────────────────────────────────────────────────────

    @Benchmark
    public BigDecimal literalDense_legacy(LiteralDenseState s) {
        CrossModuleExpressionBenchmarkSupport.applyLiteralSeed(
            s.legacy, CrossModuleExpressionBenchmarkSupport.literalSeed(s.legacyIndex++));
        return s.legacy.evaluate();
    }

    @Benchmark
    public BigDecimal literalDense_mk2Compute(LiteralDenseState s) {
        return s.mk2.compute(CrossModuleExpressionBenchmarkSupport.literalSeedToMap(CrossModuleExpressionBenchmarkSupport.literalSeed(s.mk2Index++)));
    }

    @Benchmark
    public AuditResult<BigDecimal> literalDense_mk2Audit(LiteralDenseState s) {
        return s.mk2.computeWithAudit(CrossModuleExpressionBenchmarkSupport.literalSeedToMap(CrossModuleExpressionBenchmarkSupport.literalSeed(s.mk2Index++)));
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
        return s.mk2.compute(CrossModuleExpressionBenchmarkSupport.frameToMap(CrossModuleExpressionBenchmarkSupport.variableFrame(s.mk2Index++)));
    }

    @Benchmark
    public AuditResult<BigDecimal> variableChurn_mk2Audit(VariableChurnState s) {
        return s.mk2.computeWithAudit(CrossModuleExpressionBenchmarkSupport.frameToMap(CrossModuleExpressionBenchmarkSupport.variableFrame(s.mk2Index++)));
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
        return s.mk2.compute(CrossModuleExpressionBenchmarkSupport.frameToMap(CrossModuleExpressionBenchmarkSupport.userFunctionFrame(s.mk2Index++)));
    }

    @Benchmark
    public AuditResult<BigDecimal> userFunction_mk2Audit(UserFunctionState s) {
        return s.mk2.computeWithAudit(CrossModuleExpressionBenchmarkSupport.frameToMap(CrossModuleExpressionBenchmarkSupport.userFunctionFrame(s.mk2Index++)));
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
        return s.mk2.compute(CrossModuleExpressionBenchmarkSupport.frameToMap(CrossModuleExpressionBenchmarkSupport.variableFrame(s.mk2Index++)));
    }

    @Benchmark
    public AuditResult<BigDecimal> conditional_mk2Audit(ConditionalState s) {
        return s.mk2.computeWithAudit(CrossModuleExpressionBenchmarkSupport.frameToMap(CrossModuleExpressionBenchmarkSupport.variableFrame(s.mk2Index++)));
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
        return s.mk2.compute(CrossModuleExpressionBenchmarkSupport.frameToMap(CrossModuleExpressionBenchmarkSupport.variableFrame(s.mk2Index++)));
    }

    @Benchmark
    public AuditResult<BigDecimal> logarithmChain_mk2Audit(LogarithmChainState s) {
        return s.mk2.computeWithAudit(CrossModuleExpressionBenchmarkSupport.frameToMap(CrossModuleExpressionBenchmarkSupport.variableFrame(s.mk2Index++)));
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
        return s.mk2.compute(CrossModuleExpressionBenchmarkSupport.frameToMap(CrossModuleExpressionBenchmarkSupport.variableFrame(s.mk2Index++)));
    }

    @Benchmark
    public AuditResult<BigDecimal> powerChain_mk2Audit(PowerChainState s) {
        return s.mk2.computeWithAudit(CrossModuleExpressionBenchmarkSupport.frameToMap(CrossModuleExpressionBenchmarkSupport.variableFrame(s.mk2Index++)));
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

    // ── compile (mk2 only) ────────────────────────────────────────────────────

    @Benchmark
    public Object compileMathLiteralDense(CompileOnlyState state) {
        return new ExpressionCompiler().compile(
            MATH_LITERAL_DENSE_EXPRESSION,
            ExpressionResultType.MATH,
            state.environment
        );
    }

    @Benchmark
    public Object compileLogicalMixedLiteralDense(CompileOnlyState state) {
        return new ExpressionCompiler().compile(
            LOGICAL_MIXED_LITERAL_EXPRESSION,
            ExpressionResultType.LOGICAL,
            state.environment
        );
    }

    // ── compute literal-only (mk2 only) ───────────────────────────────────────

    @Benchmark
    public Object computeMathLiteralDense(RuntimeOnlyState state) {
        return state.mathRuntime.computeMath(Map.of());
    }

    @Benchmark
    public boolean computeLogicalMixedLiteralDense(RuntimeOnlyState state) {
        return state.logicalRuntime.computeLogical(Map.of());
    }

    // ── assignment bindings (mk2 only) ────────────────────────────────────────

    @Benchmark
    public Map<String, Object> computeNoExternal(NoExternalState state) {
        return state.expression.compute(state.values);
    }

    @Benchmark
    public Map<String, Object> computeThreeExternal(ThreeExternalState state) {
        return state.expression.compute(state.values);
    }

    @Benchmark
    public Map<String, Object> computeTwelveExternal(TwelveExternalState state) {
        return state.expression.compute(state.values);
    }

    // ── additional states ─────────────────────────────────────────────────────

    @State(Scope.Benchmark)
    public static class CompileOnlyState {
        ExpressionEnvironment environment;

        @Setup(Level.Trial)
        public void setUp() {
            environment = ExpressionEnvironmentBuilder.empty();
        }
    }

    @State(Scope.Benchmark)
    public static class RuntimeOnlyState {
        ExpressionRuntimeSupport mathRuntime;
        ExpressionRuntimeSupport logicalRuntime;

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = ExpressionEnvironmentBuilder.empty();
            mathRuntime = ExpressionRuntimeSupport.compileMath(MATH_LITERAL_DENSE_EXPRESSION, environment);
            logicalRuntime = ExpressionRuntimeSupport.compileLogical(LOGICAL_MIXED_LITERAL_EXPRESSION, environment);
        }
    }

    @State(Scope.Thread)
    public static class NoExternalState {
        AssignmentExpression expression;
        Map<String, Object> values = Map.of();

        @Setup(Level.Trial)
        public void setUp() {
            expression = AssignmentExpression.compile(
                "x1 = 1; x2 = x1 + 2; x3 = x2 + 3; x4 = x3 + 4; x5 = x4 + 5;"
            );
        }
    }

    @State(Scope.Thread)
    public static class ThreeExternalState {
        AssignmentExpression expression;
        Map<String, Object> values;

        @Setup(Level.Trial)
        public void setUp() {
            expression = AssignmentExpression.compile(
                "r1 = a * b; r2 = r1 + c; r3 = r2 * 2; r4 = r3 - b; r5 = r4 + a;"
            );
            values = Map.of(
                "a", new BigDecimal("3.500"),
                "b", new BigDecimal("2.100"),
                "c", new BigDecimal("7.700")
            );
        }
    }

    @State(Scope.Thread)
    public static class TwelveExternalState {
        private static final String[] NAMES = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l"};
        private static final String EXPRESSION = """
                r1  = a + b;
                r2  = r1  + c;
                r3  = r2  + d;
                r4  = r3  + e;
                r5  = r4  + f;
                r6  = r5  + g;
                r7  = r6  + h;
                r8  = r7  + i;
                r9  = r8  + j;
                r10 = r9  + k;
                r11 = r10 + l;
                r12 = r11 + 1;
                """;

        AssignmentExpression expression;
        Map<String, Object> values;

        @Setup(Level.Trial)
        public void setUp() {
            expression = AssignmentExpression.compile(EXPRESSION);
            Map<String, Object> v = new HashMap<>();
            for (int i = 0; i < NAMES.length; i++) {
                v.put(NAMES[i], new BigDecimal(i + 1));
            }
            values = Collections.unmodifiableMap(v);
        }
    }

    // ── expression builders ───────────────────────────────────────────────────

    private static String buildMathLiteralDenseExpression() {
        StringJoiner joiner = new StringJoiner(" + ");
        for (int index = 1; index <= 64; index++) {
            joiner.add("100%d.%04d".formatted(index, index));
        }
        return joiner.toString();
    }

    private static String buildLogicalMixedLiteralExpression() {
        return String.join(
            " and ",
            "2024-12-31 > 2024-01-01",
            "12:30 < 13:45",
            "2024-12-31T12:30 > 2024-12-30T08:15",
            "\"zeta\" > \"alpha\"",
            "2025-01-15 >= 2025-01-14",
            "16:45 > 06:30",
            "2025-01-15T23:59 > 2025-01-15T00:00",
            "\"runestone\" != \"forge\"",
            "42 > 7",
            "true"
        );
    }
}
