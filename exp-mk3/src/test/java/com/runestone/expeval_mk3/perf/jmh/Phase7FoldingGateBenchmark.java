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
 * Phase 7 non-regression gate (issue #120): optimized vs Unoptimized Oracle for the four foldable
 * constructs introduced across issues #116-#119, none of which move the existing Symbol-Externo-driven
 * benchmarks (Phase5BaselineBenchmark, Phase6NavigationBenchmark, CollectionOperationsBenchmark). Each
 * case compiles the same expression twice from the same {@code SemanticModel} through
 * {@link OraclePlanFixtures}: once with folding enabled ({@code optimized}) and once through the
 * Unoptimized Oracle ({@code oracle}) that ADR 0019 validates against. `richConstantCompilation*`
 * measures full compilation cost (parse, semantic resolution, plan build) per operation, not hot
 * {@code compute()}; the other three measure hot {@code compute()} over a plan built once in
 * {@code @Setup(Level.Trial)}, with the {@link Clock} and override {@link Map} hoisted into
 * {@code @State} fields so neither is allocated inside the measured body. Unlike Phase5/Phase6, which
 * call the public {@code ResultExpression.compute()} (going through {@code PublicMaterialization}'s
 * defensive copy), the three hot-path cases here call {@link ExecutionPlan#compute} directly so the
 * optimized/oracle pair is measured at the same internal abstraction level; their ns/op is not
 * comparable to Phase5/Phase6 numbers for that reason. The membership pair's gain reflects the
 * combined effect of the download (issue #119) and the eager literal pre-evaluation (issue #115) that
 * the oracle disables along with it, not the download in isolation.
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class Phase7FoldingGateBenchmark {

    private static final String RICH_CONSTANT_SOURCE =
            "((1 + 2) * (3 - 4) mod 5 + 5) > 0 and (asText(10) = \"10\") and !(2 > 5)"
                    + " or (asBool(true) and (7 in [1, 2, 3, 4, 5, 6, 7, 8, 9]))";

    @Benchmark
    public void navigationPrefixOptimized(NavigationPrefixPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.optimized.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void navigationPrefixOracle(NavigationPrefixPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.oracle.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void membershipDownloadOptimized(MembershipPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.optimized.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void membershipDownloadOracle(MembershipPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.oracle.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void assertionElisionOptimized(AssertionElisionPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.optimized.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void assertionElisionOracle(AssertionElisionPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.oracle.compute(plans.overrides, plans.clock));
    }

    /**
     * Overrides the class-level protocol: at the shared 5x500ms warmup this compile-heavy case's
     * error bars exceeded its mean on both sides (see the issue #120 performance-history entry), so
     * it needs a longer warmup to converge. The override keeps that requirement enforced by the
     * benchmark itself rather than only by the documented CLI flags.
     */
    @Benchmark
    @Warmup(iterations = 15, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 15, time = 1, timeUnit = TimeUnit.SECONDS)
    public void richConstantCompilationOptimized(CompilationEnvironment inputs, Blackhole blackhole) {
        blackhole.consume(OraclePlanFixtures.compileOptimized(RICH_CONSTANT_SOURCE, inputs.environment));
    }

    @Benchmark
    @Warmup(iterations = 15, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 15, time = 1, timeUnit = TimeUnit.SECONDS)
    public void richConstantCompilationOracle(CompilationEnvironment inputs, Blackhole blackhole) {
        blackhole.consume(OraclePlanFixtures.compileOracle(RICH_CONSTANT_SOURCE, inputs.environment));
    }

    @State(Scope.Benchmark)
    public static class NavigationPrefixPlans {

        private ExecutionPlan optimized;
        private ExecutionPlan oracle;
        private final Map<String, Object> overrides = Map.of();
        private final Clock clock = Clock.systemUTC();

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = ExpressionEnvironment.builder()
                    .externalSymbol(
                            "account",
                            new ObjectType(Account.class.getName()),
                            new Account("Ana", BigDecimal.TEN),
                            ExternalSymbolOverwritePolicy.FIXED)
                    .registerJavaType(Account.class)
                    .build();
            SemanticModel model = OraclePlanFixtures.resolve("account.name", environment);
            optimized = OraclePlanFixtures.buildOptimized(model, environment);
            oracle = OraclePlanFixtures.buildOracle(model, environment);
        }
    }

    @State(Scope.Benchmark)
    public static class MembershipPlans {

        private ExecutionPlan optimized;
        private ExecutionPlan oracle;
        private final Map<String, Object> overrides = Map.of();
        private final Clock clock = Clock.systemUTC();

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = ExpressionEnvironment.builder().build();
            String source = "32 in [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,"
                    + " 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32]";
            SemanticModel model = OraclePlanFixtures.resolve(source, environment);
            optimized = OraclePlanFixtures.buildOptimized(model, environment);
            oracle = OraclePlanFixtures.buildOracle(model, environment);
        }
    }

    @State(Scope.Benchmark)
    public static class AssertionElisionPlans {

        private ExecutionPlan optimized;
        private ExecutionPlan oracle;
        private Map<String, Object> overrides;
        private final Clock clock = Clock.systemUTC();

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = ExpressionEnvironment.builder()
                    .externalSymbol("x", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .build();
            SemanticModel model = OraclePlanFixtures.resolve("asNumber(x)", environment);
            optimized = OraclePlanFixtures.buildOptimized(model, environment);
            oracle = OraclePlanFixtures.buildOracle(model, environment);
            overrides = Map.of("x", BigDecimal.TEN);
        }
    }

    @State(Scope.Benchmark)
    public static class CompilationEnvironment {

        private ExpressionEnvironment environment;

        @Setup(Level.Trial)
        public void setUp() {
            environment = ExpressionEnvironment.builder().build();
        }
    }

    public record Account(String name, BigDecimal score) {
    }
}
