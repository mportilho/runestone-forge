package com.runestone.expeval.perf.jmh;

import com.runestone.expeval.api.AuditResult;
import com.runestone.expeval.api.MathExpression;
import com.runestone.expeval.environment.ExpressionEnvironment;
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
 * Measures the evaluation cost of the destructuring assignment operation across three scenarios:
 *
 * <ul>
 *   <li><b>vectorLiteral</b> — {@code [a,b,c] = [10,20,30]; a+b+c}.
 *       Baseline: pure slot assignment from a pre-evaluated vector, no function call overhead.</li>
 *   <li><b>spread3Slots</b> — {@code [a,b,c] = spread(100,-1,[100,10,40]); a+b+c}.
 *       Typical production scenario: spread function call + coercion + 3 slot assignments.</li>
 *   <li><b>spread5Slots</b> — {@code [a,b,c,d,e] = spread(100,1,[100,10,40,20,30]); a+b+c+d+e}.
 *       Larger destructuring: spread over 5 slots to surface slot-count scaling.</li>
 * </ul>
 *
 * <p>Each scenario is measured with and without {@code computeWithAudit()} to isolate audit overhead.
 * Run via {@link DestructuringAssignmentBenchmarkRunner} to include the GC allocation profiler.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms512m", "-Xmx512m"})
public class DestructuringAssignmentBenchmark {

    // ── vectorLiteral ──────────────────────────────────────────────────────────

    @Benchmark
    public BigDecimal vectorLiteralNoAudit(VectorLiteralState state) {
        return state.expression.compute();
    }

    @Benchmark
    public AuditResult<BigDecimal> vectorLiteralWithAudit(VectorLiteralState state) {
        return state.expression.computeWithAudit();
    }

    // ── spread — 3 slots ───────────────────────────────────────────────────────

    @Benchmark
    public BigDecimal spread3SlotsNoAudit(Spread3SlotsState state) {
        return state.expression.compute();
    }

    @Benchmark
    public AuditResult<BigDecimal> spread3SlotsWithAudit(Spread3SlotsState state) {
        return state.expression.computeWithAudit();
    }

    // ── spread — 5 slots ───────────────────────────────────────────────────────

    @Benchmark
    public BigDecimal spread5SlotsNoAudit(Spread5SlotsState state) {
        return state.expression.compute();
    }

    @Benchmark
    public AuditResult<BigDecimal> spread5SlotsWithAudit(Spread5SlotsState state) {
        return state.expression.computeWithAudit();
    }

    // ── States ─────────────────────────────────────────────────────────────────

    @State(Scope.Thread)
    public static class VectorLiteralState {

        // Baseline: destructuring from a literal vector; slots are then summed.
        // No function call — isolates the slot-assignment and VariableRead paths.
        static final String EXPRESSION = "[a, b, c] = [10, 20, 30]; a + b + c";

        MathExpression expression;

        @Setup(Level.Trial)
        public void setUp() {
            expression = MathExpression.compile(EXPRESSION);
        }
    }

    @State(Scope.Thread)
    public static class Spread3SlotsState {

        // Canonical production scenario: spread distributes 100 over 3 proportional buckets
        // in backward direction, then the 3 slots are summed.
        static final String EXPRESSION =
            "[pagtoPrincipal, pagtoCorrecao, pagtoJuros] = spread(100, -1, [100, 10, 40]); " +
            "pagtoPrincipal + pagtoCorrecao + pagtoJuros";

        MathExpression expression;

        @Setup(Level.Trial)
        public void setUp() {
            expression = MathExpression.compile(EXPRESSION,
                ExpressionEnvironment.builder().addMathFunctions().build());
        }
    }

    @State(Scope.Thread)
    public static class Spread5SlotsState {

        // Larger destructuring: 5 reference buckets to surface slot-count scaling.
        static final String EXPRESSION =
            "[a, b, c, d, e] = spread(100, 1, [100, 10, 40, 20, 30]); a + b + c + d + e";

        MathExpression expression;

        @Setup(Level.Trial)
        public void setUp() {
            expression = MathExpression.compile(EXPRESSION,
                ExpressionEnvironment.builder().addMathFunctions().build());
        }
    }
}
