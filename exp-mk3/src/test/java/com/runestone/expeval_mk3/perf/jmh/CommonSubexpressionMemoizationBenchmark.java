package com.runestone.expeval_mk3.perf.jmh;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.ObjectType;
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
 * The issue #121 permanence gate for Subexpressao Comum Memoizada: optimized vs Unoptimized Oracle for
 * two shapes of repeated pure subexpression, following the same {@code optimized}/{@code oracle} pair
 * protocol as {@link Phase7FoldingGateBenchmark}. {@code navigationMemo} repeats a registered-property
 * read over an {@code OVERRIDABLE} external (a realistic case the memo is meant for: the receiver never
 * folds, so both occurrences reach the runtime as real {@code RegisteredPropertyExecutableNode} reads
 * without the memo). {@code cheapAddMemo} repeats a single decimal addition, the cheapest possible
 * eligible subtree, deliberately included as a control: if a frame read plus a branch costs more than
 * redoing the addition itself, that is the honest result the permanence decision in
 * {@code docs/perf/performance-history.md} is based on, not a flattering case picked to justify keeping
 * the feature.
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class CommonSubexpressionMemoizationBenchmark {

    @Benchmark
    public void navigationMemoOptimized(NavigationMemoPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.optimized.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void navigationMemoOracle(NavigationMemoPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.oracle.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void cheapAddMemoOptimized(CheapAddMemoPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.optimized.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void cheapAddMemoOracle(CheapAddMemoPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.oracle.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void expensiveFunctionMemoOptimized(ExpensiveFunctionMemoPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.optimized.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void expensiveFunctionMemoOracle(ExpensiveFunctionMemoPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.oracle.compute(plans.overrides, plans.clock));
    }

    @State(Scope.Benchmark)
    public static class ExpensiveFunctionMemoPlans {

        private ExecutionPlan optimized;
        private ExecutionPlan oracle;
        private Map<String, Object> overrides;
        private final Clock clock = Clock.systemUTC();

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = ExpressionEnvironment.builder()
                    .externalSymbol("x", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .build();
            SemanticModel model = OraclePlanFixtures.resolve("sqrt(x) + sqrt(x) * 2", environment);
            optimized = OraclePlanFixtures.buildOptimized(model, environment);
            oracle = OraclePlanFixtures.buildOracle(model, environment);
            overrides = Map.of("x", new BigDecimal("7"));
        }
    }

    @State(Scope.Benchmark)
    public static class NavigationMemoPlans {

        private ExecutionPlan optimized;
        private ExecutionPlan oracle;
        private Map<String, Object> overrides;
        private final Clock clock = Clock.systemUTC();

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = ExpressionEnvironment.builder()
                    .externalSymbol(
                            "account",
                            new ObjectType(Phase7FoldingGateBenchmark.Account.class.getName()),
                            new Phase7FoldingGateBenchmark.Account("Ana", BigDecimal.TEN),
                            ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .registerJavaType(Phase7FoldingGateBenchmark.Account.class)
                    .build();
            SemanticModel model = OraclePlanFixtures.resolve("account.score + account.score * 2", environment);
            optimized = OraclePlanFixtures.buildOptimized(model, environment);
            oracle = OraclePlanFixtures.buildOracle(model, environment);
            overrides = Map.of("account", new Phase7FoldingGateBenchmark.Account("Bea", new BigDecimal("42")));
        }
    }

    @State(Scope.Benchmark)
    public static class CheapAddMemoPlans {

        private ExecutionPlan optimized;
        private ExecutionPlan oracle;
        private Map<String, Object> overrides;
        private final Clock clock = Clock.systemUTC();

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = ExpressionEnvironment.builder()
                    .externalSymbol("x", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .build();
            SemanticModel model = OraclePlanFixtures.resolve("(x + 1) + (x + 1)", environment);
            optimized = OraclePlanFixtures.buildOptimized(model, environment);
            oracle = OraclePlanFixtures.buildOracle(model, environment);
            overrides = Map.of("x", new BigDecimal("7"));
        }
    }
}
