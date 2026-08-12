package com.runestone.expeval_mk3.perf.jmh;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.FunctionPurity;
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
 * Issue #125 pilot gate for Invocacao Sem Reflexao e Elisao de Coercao de Borda on global function
 * calls, one state per representative arity (one, two, four - the arities LambdaMetafactory/direct
 * {@code invokeExact} cover per ADR 0020 - plus five, the smallest arity that falls back to the
 * array-based entry point). Arguments are {@code OVERRIDABLE} external symbols rather than
 * literals so Constant Folding (Etapa 7) cannot fold the call away in the optimized plan.
 *
 * <p>Unlike the other Etapa 8 gates, the {@code optimized}/{@code oracle} pair below does
 * <strong>not</strong> discriminate this specific mechanism: {@code ExecutionPlanBuilder.build} and
 * {@code buildOracle} both route a {@link com.runestone.expeval_mk3.internal.runtime.FunctionCallExecutableNode}
 * through the same {@code FunctionDescriptor}, hence the same generated entry point (ADR 0020's
 * Consequences: "measuring invocation cost requires holding the environment fixed"). The
 * optimized/oracle benchmarks here exist for family consistency and as a permanent regression net,
 * not as the pilot's pass/fail signal. The pass/fail signal is the before/after-on-same-tree
 * comparison recorded in {@code docs/perf/performance-history.md} (same protocol as issue #85),
 * captured by stashing the issue #125 change and re-running this same class.
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class FunctionInvocationBenchmark {

    @Benchmark
    public void arityOneOptimized(ArityOnePlans plans, Blackhole blackhole) {
        blackhole.consume(plans.optimized.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void arityOneOracle(ArityOnePlans plans, Blackhole blackhole) {
        blackhole.consume(plans.oracle.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void arityTwoOptimized(ArityTwoPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.optimized.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void arityTwoOracle(ArityTwoPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.oracle.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void arityFourOptimized(ArityFourPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.optimized.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void arityFourOracle(ArityFourPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.oracle.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void arityFiveOptimized(ArityFivePlans plans, Blackhole blackhole) {
        blackhole.consume(plans.optimized.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void arityFiveOracle(ArityFivePlans plans, Blackhole blackhole) {
        blackhole.consume(plans.oracle.compute(plans.overrides, plans.clock));
    }

    /** Static providers whose parameters and return are already the canonical {@link BigDecimal}
     * NUMBER type, so boundary-coercion elision applies to every argument slot. */
    public static final class Provider {
        public static BigDecimal identity1(BigDecimal a) {
            return a;
        }

        public static BigDecimal sum2(BigDecimal a, BigDecimal b) {
            return a.add(b);
        }

        public static BigDecimal sum4(BigDecimal a, BigDecimal b, BigDecimal c, BigDecimal d) {
            return a.add(b).add(c).add(d);
        }

        public static BigDecimal sum5(BigDecimal a, BigDecimal b, BigDecimal c, BigDecimal d, BigDecimal e) {
            return a.add(b).add(c).add(d).add(e);
        }
    }

    private static ExpressionEnvironment.Builder withArguments(int count) {
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder()
                .functionsFrom(Provider.class, FunctionPurity.PURE);
        for (int index = 0; index < count; index++) {
            builder = builder.externalSymbol(
                    "a" + index, ScalarType.NUMBER, BigDecimal.valueOf(index + 1), ExternalSymbolOverwritePolicy.OVERRIDABLE);
        }
        return builder;
    }

    private static Map<String, Object> overridesFor(int count) {
        Map<String, Object> overrides = new java.util.HashMap<>();
        for (int index = 0; index < count; index++) {
            overrides.put("a" + index, BigDecimal.valueOf(index + 1).add(BigDecimal.TEN));
        }
        return Map.copyOf(overrides);
    }

    @State(Scope.Benchmark)
    public static class ArityOnePlans {
        private ExecutionPlan optimized;
        private ExecutionPlan oracle;
        private Map<String, Object> overrides;
        private final Clock clock = Clock.systemUTC();

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = withArguments(1).build();
            SemanticModel model = OraclePlanFixtures.resolve("identity1(a0)", environment);
            optimized = OraclePlanFixtures.buildOptimized(model, environment);
            oracle = OraclePlanFixtures.buildOracle(model, environment);
            overrides = overridesFor(1);
        }
    }

    @State(Scope.Benchmark)
    public static class ArityTwoPlans {
        private ExecutionPlan optimized;
        private ExecutionPlan oracle;
        private Map<String, Object> overrides;
        private final Clock clock = Clock.systemUTC();

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = withArguments(2).build();
            SemanticModel model = OraclePlanFixtures.resolve("sum2(a0, a1)", environment);
            optimized = OraclePlanFixtures.buildOptimized(model, environment);
            oracle = OraclePlanFixtures.buildOracle(model, environment);
            overrides = overridesFor(2);
        }
    }

    @State(Scope.Benchmark)
    public static class ArityFourPlans {
        private ExecutionPlan optimized;
        private ExecutionPlan oracle;
        private Map<String, Object> overrides;
        private final Clock clock = Clock.systemUTC();

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = withArguments(4).build();
            SemanticModel model = OraclePlanFixtures.resolve("sum4(a0, a1, a2, a3)", environment);
            optimized = OraclePlanFixtures.buildOptimized(model, environment);
            oracle = OraclePlanFixtures.buildOracle(model, environment);
            overrides = overridesFor(4);
        }
    }

    @State(Scope.Benchmark)
    public static class ArityFivePlans {
        private ExecutionPlan optimized;
        private ExecutionPlan oracle;
        private Map<String, Object> overrides;
        private final Clock clock = Clock.systemUTC();

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = withArguments(5).build();
            SemanticModel model = OraclePlanFixtures.resolve("sum5(a0, a1, a2, a3, a4)", environment);
            optimized = OraclePlanFixtures.buildOptimized(model, environment);
            oracle = OraclePlanFixtures.buildOracle(model, environment);
            overrides = overridesFor(5);
        }
    }
}
