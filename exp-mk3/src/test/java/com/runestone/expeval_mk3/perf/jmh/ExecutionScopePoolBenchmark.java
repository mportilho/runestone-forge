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
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ExecutionScopePoolBenchmark {

    private static final String SOURCE = "taxa := if contrato.indice = \"PRE\" then taxaPre else taxaPos + spread endif; "
            + "fator := (1 + taxa / 100) ^ (prazo / 12); "
            + "saldo * fator + pmt(taxa / 100, prazo, saldo)";

    @Benchmark
    public void canonicalExpression(CanonicalPlan plan, Blackhole blackhole) {
        blackhole.consume(plan.executionPlan.compute(plan.overrides, plan.clock));
    }

    @State(Scope.Benchmark)
    public static class CanonicalPlan {

        private ExecutionPlan executionPlan;
        private Map<String, Object> overrides;
        private final Clock clock = Clock.systemUTC();

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = ExpressionEnvironment.builder()
                    .mathContext(new MathContext(12, RoundingMode.HALF_UP))
                    .registerJavaType(PreIndexedContract.class)
                    .externalSymbol(
                            "contrato",
                            new ObjectType(PreIndexedContract.class.getName()),
                            new PreIndexedContract(),
                            ExternalSymbolOverwritePolicy.FIXED)
                    .externalSymbol("taxaPre", ScalarType.NUMBER, new BigDecimal("12"), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .externalSymbol("taxaPos", ScalarType.NUMBER, new BigDecimal("15"), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .externalSymbol("spread", ScalarType.NUMBER, new BigDecimal("3"), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .externalSymbol("saldo", ScalarType.NUMBER, new BigDecimal("10000"), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .externalSymbol("prazo", ScalarType.NUMBER, new BigDecimal("12"), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .build();
            SemanticModel model = OraclePlanFixtures.resolve(SOURCE, environment);
            executionPlan = OraclePlanFixtures.buildOptimized(model, environment);
            overrides = Map.of(
                    "taxaPre", new BigDecimal("11.75"),
                    "taxaPos", new BigDecimal("14.25"),
                    "spread", new BigDecimal("2.50"),
                    "saldo", new BigDecimal("10000"),
                    "prazo", new BigDecimal("12"));
        }
    }

    public static final class PreIndexedContract {

        public String getIndice() {
            return "PRE";
        }
    }
}
