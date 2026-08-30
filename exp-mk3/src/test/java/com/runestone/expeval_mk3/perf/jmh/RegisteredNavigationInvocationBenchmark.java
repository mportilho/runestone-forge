package com.runestone.expeval_mk3.perf.jmh;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.FunctionPurity;
import com.runestone.expeval_mk3.api.FunctionSignature;
import com.runestone.expeval_mk3.api.JavaMethodDescriptor;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Issue #127 paired gate for environment-prepared registered-property and registered-method entry
 * points. The optimized plans use the descriptors' prepared entry points; the Unoptimized Oracle
 * retains the generic MethodHandle routes. Overridable symbols prevent constant folding.
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class RegisteredNavigationInvocationBenchmark {

    @Benchmark
    public void propertyOptimized(PropertyPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.optimized.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void propertyOracle(PropertyPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.oracle.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void methodOptimized(MethodPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.optimized.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public void methodOracle(MethodPlans plans, Blackhole blackhole) {
        blackhole.consume(plans.oracle.compute(plans.overrides, plans.clock));
    }

    @Benchmark
    public Object arrayEntryPoint(ArrayEntryPointState state) throws Throwable {
        return state.descriptor.invokeArray(state.receiverAndArguments);
    }

    private static ExpressionEnvironment environment() {
        ObjectType accountType = new ObjectType(Account.class.getName());
        return ExpressionEnvironment.builder()
                .registerJavaType(Account.class)
                .registerJavaTypeMethod(Account.class, "add", FunctionPurity.PURE, BigDecimal.class)
                .externalSymbol(
                        "account",
                        accountType,
                        new Account(BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol(
                        "increment",
                        ScalarType.NUMBER,
                        BigDecimal.ONE,
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
    }

    private static void preparePlans(Plans plans, String source) {
        ExpressionEnvironment environment = environment();
        SemanticModel model = OraclePlanFixtures.resolve(source, environment);
        plans.optimized = OraclePlanFixtures.buildOptimized(model, environment);
        plans.oracle = OraclePlanFixtures.buildOracle(model, environment);
        plans.overrides = Map.of(
                "account", new Account(new BigDecimal("41")),
                "increment", BigDecimal.ONE);
    }

    private abstract static class Plans {
        ExecutionPlan optimized;
        ExecutionPlan oracle;
        Map<String, Object> overrides;
        final Clock clock = Clock.systemUTC();
    }

    @State(Scope.Benchmark)
    public static class PropertyPlans extends Plans {
        @Setup(Level.Trial)
        public void setUp() {
            preparePlans(this, "account.balance");
        }
    }

    @State(Scope.Benchmark)
    public static class MethodPlans extends Plans {
        @Setup(Level.Trial)
        public void setUp() {
            preparePlans(this, "account.add(increment)");
        }
    }

    @State(Scope.Benchmark)
    public static class ArrayEntryPointState {
        JavaMethodDescriptor descriptor;
        Object[] receiverAndArguments;

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = ExpressionEnvironment.builder()
                    .registerJavaType(Account.class)
                    .registerJavaTypeMethod(
                            Account.class,
                            "add4",
                            FunctionPurity.PURE,
                            BigDecimal.class,
                            BigDecimal.class,
                            BigDecimal.class,
                            BigDecimal.class)
                    .build();
            descriptor = environment.javaTypes()
                    .find(Account.class)
                    .orElseThrow()
                    .findMethod(new FunctionSignature(
                            "add4", List.of(
                                    ScalarType.NUMBER,
                                    ScalarType.NUMBER,
                                    ScalarType.NUMBER,
                                    ScalarType.NUMBER)))
                    .orElseThrow();
            receiverAndArguments = new Object[] {
                new Account(BigDecimal.ONE),
                BigDecimal.ONE,
                BigDecimal.TWO,
                new BigDecimal("3"),
                new BigDecimal("4")
            };
        }
    }

    public record Account(BigDecimal balance) {
        public BigDecimal add(BigDecimal increment) {
            return balance.add(increment);
        }

        public BigDecimal add4(
                BigDecimal first, BigDecimal second, BigDecimal third, BigDecimal fourth) {
            return balance.add(first).add(second).add(third).add(fourth);
        }
    }
}
