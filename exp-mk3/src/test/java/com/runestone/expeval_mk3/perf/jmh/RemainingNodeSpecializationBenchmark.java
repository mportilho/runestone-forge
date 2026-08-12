package com.runestone.expeval_mk3.perf.jmh;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.MapType;
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
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class RemainingNodeSpecializationBenchmark {

    @Benchmark
    public void optimized(Plans plans, Blackhole blackhole) {
        blackhole.consume(plans.optimized.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void oracle(Plans plans, Blackhole blackhole) {
        blackhole.consume(plans.oracle.compute(plans.overrides, plans.clock));
    }

    @State(Scope.Benchmark)
    public static class Plans {
        @Param
        private Case benchmarkCase;

        private ExecutionPlan optimized;
        private ExecutionPlan oracle;
        private Map<String, Object> overrides;
        private final Clock clock = Clock.systemUTC();

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = benchmarkCase.environment();
            SemanticModel model = OraclePlanFixtures.resolve(benchmarkCase.source, environment);
            optimized = OraclePlanFixtures.buildOptimized(model, environment);
            oracle = OraclePlanFixtures.buildOracle(model, environment);
            overrides = benchmarkCase.overrides();
        }
    }

    public enum Case {
        COALESCE_BINARY("map?.[\"missing\"] ?? n1"),
        BETWEEN_NUMBER("n1 between n0 and n2"),
        BETWEEN_STRING("s2 between s1 and s4"),
        CONCAT_BINARY("s1 || s2"),
        CONCAT_NARY("s1 || s2 || s3 || s4"),
        CONDITIONAL_ONE("if flag1 then n1 else n2 endif"),
        CONDITIONAL_TWO("if flag1 then n1 elsif flag2 then n2 else n3 endif"),
        CONDITIONAL_THREE("if flag1 then n1 elsif flag2 then n2 elsif flag3 then n3 else n4 endif"),
        DECIMAL_ADD("n1 + n2"),
        DECIMAL_SUBTRACT("n1 - n2"),
        DECIMAL_MULTIPLY("n1 * n2"),
        DECIMAL_DIVIDE("n1 / n2"),
        DECIMAL_MODULO("n1 mod n2");

        private final String source;

        Case(String source) {
            this.source = source;
        }

        ExpressionEnvironment environment() {
            ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder();
            return switch (this) {
                case COALESCE_BINARY -> builder
                        .externalSymbol("map", new MapType(ScalarType.NUMBER), Map.of(), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                        .externalSymbol("n1", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                        .build();
                case BETWEEN_NUMBER, DECIMAL_ADD, DECIMAL_SUBTRACT, DECIMAL_MULTIPLY,
                        DECIMAL_DIVIDE, DECIMAL_MODULO -> numbers(builder, 0, 1, 2).build();
                case BETWEEN_STRING -> strings(builder, 1, 2, 4).build();
                case CONCAT_BINARY -> strings(builder, 1, 2).build();
                case CONCAT_NARY -> strings(builder, 1, 2, 3, 4).build();
                case CONDITIONAL_ONE -> numbers(builder
                        .externalSymbol("flag1", ScalarType.BOOLEAN, false, ExternalSymbolOverwritePolicy.OVERRIDABLE), 1, 2).build();
                case CONDITIONAL_TWO -> numbers(builder
                        .externalSymbol("flag1", ScalarType.BOOLEAN, false, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                        .externalSymbol("flag2", ScalarType.BOOLEAN, true, ExternalSymbolOverwritePolicy.OVERRIDABLE), 1, 2, 3).build();
                case CONDITIONAL_THREE -> numbers(builder
                        .externalSymbol("flag1", ScalarType.BOOLEAN, false, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                        .externalSymbol("flag2", ScalarType.BOOLEAN, false, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                        .externalSymbol("flag3", ScalarType.BOOLEAN, true, ExternalSymbolOverwritePolicy.OVERRIDABLE), 1, 2, 3, 4).build();
            };
        }

        Map<String, Object> overrides() {
            return Map.of();
        }

        private static ExpressionEnvironment.Builder numbers(ExpressionEnvironment.Builder builder, int... indexes) {
            for (int index : indexes) {
                builder.externalSymbol(
                        "n" + index, ScalarType.NUMBER, BigDecimal.valueOf(index + 2L),
                        ExternalSymbolOverwritePolicy.OVERRIDABLE);
            }
            return builder;
        }

        private static ExpressionEnvironment.Builder strings(ExpressionEnvironment.Builder builder, int... indexes) {
            for (int index : indexes) {
                builder.externalSymbol(
                        "s" + index, ScalarType.STRING, "text-" + index,
                        ExternalSymbolOverwritePolicy.OVERRIDABLE);
            }
            return builder;
        }
    }
}
