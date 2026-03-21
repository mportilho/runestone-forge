package com.runestone.expeval2.perf.jmh;

import com.runestone.expeval2.api.AssignmentExpression;
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
 * Measures the cost of {@code AssignmentExpression.compute()} as the number of external symbols grows.
 *
 * <p>The key path under investigation is {@code MutableBindings.copyValues()}, which is called
 * on every {@code compute()} invocation because {@code AssignmentExpression} always uses a mutable
 * {@link com.runestone.expeval2.internal.runtime.ExecutionScope}. The copy size is proportional to
 * the number of external symbols bound in the expression.
 *
 * <p>Scenarios:
 * <ul>
 *   <li><b>noExternal</b> — 5 chained assignments, no external symbols. The bindings map is
 *       empty; {@code new HashMap<>(emptyMap)} is essentially a no-op.</li>
 *   <li><b>threeExternal</b> — 5 assignments that depend on 3 external variables. The bindings
 *       map holds 3 entries on every copy.</li>
 *   <li><b>twelveExternal</b> — 12 assignments that depend on 12 external variables. Maximum
 *       realistic copy size for the standard variable set.</li>
 * </ul>
 *
 * <p>Run with {@code -prof gc} to capture {@code B/op} and isolate allocation from the HashMap copy.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class AssignmentExpressionBindingsBenchmark {

    @Benchmark
    public Map<String, Object> computeNoExternal(NoExternalState state) {
        return state.expression.compute();
    }

    @Benchmark
    public Map<String, Object> computeThreeExternal(ThreeExternalState state) {
        return state.expression.compute();
    }

    @Benchmark
    public Map<String, Object> computeTwelveExternal(TwelveExternalState state) {
        return state.expression.compute();
    }

    // ── States ────────────────────────────────────────────────────────────────

    /**
     * 5 chained assignments, no external symbols.
     * bindings.values is empty → HashMap copy is trivially cheap.
     */
    @State(Scope.Thread)
    public static class NoExternalState {

        AssignmentExpression expression;

        @Setup(Level.Trial)
        public void setUp() {
            expression = AssignmentExpression.compile(
                "x1 = 1; x2 = x1 + 2; x3 = x2 + 3; x4 = x3 + 4; x5 = x4 + 5;"
            );
        }
    }

    /**
     * 5 assignments that reference 3 external symbols (a, b, c).
     * bindings.values holds 3 entries — copied on every compute().
     */
    @State(Scope.Thread)
    public static class ThreeExternalState {

        AssignmentExpression expression;

        @Setup(Level.Trial)
        public void setUp() {
            expression = AssignmentExpression.compile(
                "r1 = a * b; r2 = r1 + c; r3 = r2 * 2; r4 = r3 - b; r5 = r4 + a;"
            );
            expression.setValue("a", new BigDecimal("3.500"));
            expression.setValue("b", new BigDecimal("2.100"));
            expression.setValue("c", new BigDecimal("7.700"));
        }
    }

    /**
     * 12 chained assignments that each reference one of 12 external symbols (a–l).
     * bindings.values holds 12 entries — largest realistic copy at the standard variable set.
     */
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

        @Setup(Level.Trial)
        public void setUp() {
            expression = AssignmentExpression.compile(EXPRESSION);
            for (int i = 0; i < NAMES.length; i++) {
                expression.setValue(NAMES[i], new BigDecimal(i + 1));
            }
        }
    }
}
