package com.runestone.expeval_mk3.perf.jmh;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.plan.ExecutionPlan;
import com.runestone.expeval_mk3.internal.plan.OraclePlanFixtures;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
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
import org.openjdk.jmh.infra.Blackhole;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Issue #126 pilot gate (Etapa 8, increment 3): isolates the comparison/equality mechanism the pilot
 * removes — {@code ExpressionRuntime.compareValues}/{@code structuralEquals} re-dispatching by type on
 * every evaluation, even though the operand type was already resolved at plan-build time. Each state
 * chains sixteen order comparisons or equality checks over the same two operands, rather than one, so
 * the per-dispatch delta clears measurement noise instead of being swamped by fixed plan overhead: a
 * single comparison's {@code compute()} call is dominated by frame setup, not by the one type check
 * this pilot removes. Every operand is an {@code OVERRIDABLE} external symbol, never a literal, so
 * Constant Folding (Etapa 7) cannot fold a chain away in the optimized plan — unlike
 * {@code FunctionInvocationBenchmark} (issue #125), the {@code optimized}/{@code oracle} pair here
 * <strong>does</strong> discriminate the mechanism: {@code buildOracle} always builds the generic
 * {@link com.runestone.expeval_mk3.internal.runtime.BinaryExecutableNode}, and {@code build} builds
 * the type-specialized node whenever the operand type is not {@code COLLECTION}/{@code MAP}. This
 * pairing is therefore the pilot's pass/fail signal directly, with no git-stash before/after protocol
 * needed (contrast issue #125's entry in {@code performance-history.md}).
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ComparisonAndEqualityBenchmark {

    private static final String NUMBER_COMPARISON_SOURCE = chain("(a > b)", "and");
    private static final String STRING_COMPARISON_SOURCE = chain("(s > t)", "and");
    private static final String NUMBER_EQUALITY_SOURCE = chain("(a = b)", "and");
    private static final String NUMBER_EQUALITY_SCALE_MISMATCH_SOURCE = chain("(a = c)", "and");
    private static final String STRING_EQUALITY_SOURCE = chain("(s = t)", "and");

    private static String chain(String term, String connective) {
        return String.join(" " + connective + " ", java.util.Collections.nCopies(16, term));
    }

    @Benchmark
    public void numberComparisonOptimized(NumberComparisonPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.optimized.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void numberComparisonOracle(NumberComparisonPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.oracle.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void stringComparisonOptimized(StringComparisonPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.optimized.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void stringComparisonOracle(StringComparisonPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.oracle.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void numberEqualityOptimized(NumberEqualityPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.optimized.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void numberEqualityOracle(NumberEqualityPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.oracle.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void numberEqualityScaleMismatchOptimized(NumberEqualityScaleMismatchPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.optimized.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void numberEqualityScaleMismatchOracle(NumberEqualityScaleMismatchPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.oracle.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void stringEqualityOptimized(StringEqualityPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.optimized.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void stringEqualityOracle(StringEqualityPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.oracle.compute(plans.overrides, plans.clock));
    }

    private static ExpressionEnvironment.Builder numberEnvironment() {
        return ExpressionEnvironment.builder()
                .externalSymbol("a", ScalarType.NUMBER, BigDecimal.valueOf(3), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("b", ScalarType.NUMBER, BigDecimal.valueOf(5), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("c", ScalarType.NUMBER, BigDecimal.valueOf(3), ExternalSymbolOverwritePolicy.OVERRIDABLE);
    }

    private static ExpressionEnvironment.Builder stringEnvironment() {
        return ExpressionEnvironment.builder()
                .externalSymbol("s", ScalarType.STRING, "alpha", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("t", ScalarType.STRING, "beta", ExternalSymbolOverwritePolicy.OVERRIDABLE);
    }

    @State(Scope.Benchmark)
    public static class NumberComparisonPlans {
        private ExecutionPlan optimized;
        private ExecutionPlan oracle;
        private Map<String, Object> overrides;
        private final Clock clock = Clock.systemUTC();

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = numberEnvironment().build();
            SemanticModel model = OraclePlanFixtures.resolve(NUMBER_COMPARISON_SOURCE, environment);
            optimized = OraclePlanFixtures.buildOptimized(model, environment);
            oracle = OraclePlanFixtures.buildOracle(model, environment);
            overrides = Map.of("a", BigDecimal.valueOf(7), "b", BigDecimal.valueOf(2), "c", BigDecimal.valueOf(7));
        }
    }

    @State(Scope.Benchmark)
    public static class StringComparisonPlans {
        private ExecutionPlan optimized;
        private ExecutionPlan oracle;
        private Map<String, Object> overrides;
        private final Clock clock = Clock.systemUTC();

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = stringEnvironment().build();
            SemanticModel model = OraclePlanFixtures.resolve(STRING_COMPARISON_SOURCE, environment);
            optimized = OraclePlanFixtures.buildOptimized(model, environment);
            oracle = OraclePlanFixtures.buildOracle(model, environment);
            overrides = Map.of("s", "zeta", "t", "yankee");
        }
    }

    @State(Scope.Benchmark)
    public static class NumberEqualityPlans {
        private ExecutionPlan optimized;
        private ExecutionPlan oracle;
        private Map<String, Object> overrides;
        private final Clock clock = Clock.systemUTC();

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = numberEnvironment().build();
            SemanticModel model = OraclePlanFixtures.resolve(NUMBER_EQUALITY_SOURCE, environment);
            optimized = OraclePlanFixtures.buildOptimized(model, environment);
            oracle = OraclePlanFixtures.buildOracle(model, environment);
            overrides = Map.of("a", BigDecimal.valueOf(4), "b", BigDecimal.valueOf(4), "c", BigDecimal.valueOf(9));
        }
    }

    /** {@code a} and {@code c} carry different scales so the specialized path's {@code compareTo} keeps agreeing with the Oracle. */
    @State(Scope.Benchmark)
    public static class NumberEqualityScaleMismatchPlans {
        private ExecutionPlan optimized;
        private ExecutionPlan oracle;
        private Map<String, Object> overrides;
        private final Clock clock = Clock.systemUTC();

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = numberEnvironment().build();
            SemanticModel model = OraclePlanFixtures.resolve(NUMBER_EQUALITY_SCALE_MISMATCH_SOURCE, environment);
            optimized = OraclePlanFixtures.buildOptimized(model, environment);
            oracle = OraclePlanFixtures.buildOracle(model, environment);
            overrides = Map.of("a", new BigDecimal("4.00"), "b", BigDecimal.valueOf(9), "c", new BigDecimal("4.0"));
        }
    }

    @State(Scope.Benchmark)
    public static class StringEqualityPlans {
        private ExecutionPlan optimized;
        private ExecutionPlan oracle;
        private Map<String, Object> overrides;
        private final Clock clock = Clock.systemUTC();

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = stringEnvironment().build();
            SemanticModel model = OraclePlanFixtures.resolve(STRING_EQUALITY_SOURCE, environment);
            optimized = OraclePlanFixtures.buildOptimized(model, environment);
            oracle = OraclePlanFixtures.buildOracle(model, environment);
            overrides = Map.of("s", "same", "t", "same");
        }
    }
}
