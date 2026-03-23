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
 * Measures the cost of {@code AssignmentExpression.compute(Map)} as the number of external symbols grows.
 *
 * <p>Variable values are supplied per call via a pre-allocated {@code Map<String, Object>} held in
 * the JMH state. Each {@code compute(Map)} call copies the defaults map and merges the user values,
 * so copy size is proportional to the number of external symbols.
 *
 * <p>Scenarios:
 * <ul>
 *   <li><b>noExternal</b> — 5 chained assignments, no external symbols. An empty map is passed;
 *       only the internal defaults copy occurs.</li>
 *   <li><b>threeExternal</b> — 5 assignments that depend on 3 external variables.</li>
 *   <li><b>twelveExternal</b> — 12 assignments that depend on 12 external variables.</li>
 * </ul>
 *
 * <p>Run with {@code -prof gc} to capture {@code B/op} and isolate allocation cost.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class AssignmentExpressionBindingsBenchmark {

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

    // ── States ────────────────────────────────────────────────────────────────

    /**
     * 5 chained assignments, no external symbols.
     * Empty values map — no symbol merging overhead.
     */
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

    /**
     * 5 assignments that reference 3 external symbols (a, b, c).
     * values map holds 3 entries — merged on every compute().
     */
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

    /**
     * 12 chained assignments that each reference one of 12 external symbols (a–l).
     * values map holds 12 entries — largest realistic merge at the standard variable set.
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
        Map<String, Object> values;

        @Setup(Level.Trial)
        public void setUp() {
            expression = AssignmentExpression.compile(EXPRESSION);
            Map<String, Object> v = new java.util.HashMap<>();
            for (int i = 0; i < NAMES.length; i++) {
                v.put(NAMES[i], new BigDecimal(i + 1));
            }
            values = java.util.Collections.unmodifiableMap(v);
        }
    }
}
