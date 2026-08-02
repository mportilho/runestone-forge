package com.runestone.expeval_mk3.perf.jmh;

import com.runestone.expeval_mk3.api.ExpressionCompiler;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.ObjectType;
import com.runestone.expeval_mk3.api.ResultExpression;
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
import java.util.concurrent.TimeUnit;

/**
 * Phase 6 characterization baseline (issue #111): filter, subscript/slice, property-and-method
 * chain over a registered Java type, and two-level nested lambda. This is a characterization
 * record, not a before/after comparison and not a CI gate.
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class Phase6NavigationBenchmark {

    @Benchmark
    public void filter(RuntimePlans plans, Blackhole blackhole) {
        blackhole.consume(plans.filter.compute());
    }

    @Benchmark
    public void subscript(RuntimePlans plans, Blackhole blackhole) {
        blackhole.consume(plans.subscript.compute());
    }

    @Benchmark
    public void slice(RuntimePlans plans, Blackhole blackhole) {
        blackhole.consume(plans.slice.compute());
    }

    @Benchmark
    public void propertyChain(RuntimePlans plans, Blackhole blackhole) {
        blackhole.consume(plans.propertyChain.compute());
    }

    @Benchmark
    public void methodChain(RuntimePlans plans, Blackhole blackhole) {
        blackhole.consume(plans.methodChain.compute());
    }

    @Benchmark
    public void nestedLambda(RuntimePlans plans, Blackhole blackhole) {
        blackhole.consume(plans.nestedLambda.compute());
    }

    @State(Scope.Benchmark)
    public static class RuntimePlans {

        private ResultExpression filter;
        private ResultExpression subscript;
        private ResultExpression slice;
        private ResultExpression propertyChain;
        private ResultExpression methodChain;
        private ResultExpression nestedLambda;

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment collectionEnvironment = ExpressionEnvironment.standard();
            filter = ExpressionCompiler.compileOrThrow(
                            "items := [1, 2, 3, 4, 5, 6, 7, 8]; items[?(@ > 4)]", collectionEnvironment)
                    .asResult();
            subscript = ExpressionCompiler.compileOrThrow(
                            "items := [1, 2, 3, 4, 5, 6, 7, 8]; items[4]", collectionEnvironment)
                    .asResult();
            slice = ExpressionCompiler.compileOrThrow(
                            "items := [1, 2, 3, 4, 5, 6, 7, 8]; items[2:6]", collectionEnvironment)
                    .asResult();

            ExpressionEnvironment chainEnvironment = ExpressionEnvironment.builder()
                    .externalSymbol(
                            "customer",
                            new ObjectType(CustomerProfile.class.getName()),
                            new CustomerProfile("Ana", BigDecimal.TEN, new Address("Manaus", "BR")),
                            ExternalSymbolOverwritePolicy.FIXED)
                    .registerJavaTypeWithPublicMethods(CustomerProfile.class)
                    .registerJavaType(Address.class)
                    .build();
            propertyChain = ExpressionCompiler.compileOrThrow("customer.address.city", chainEnvironment)
                    .asResult();
            methodChain = ExpressionCompiler.compileOrThrow(
                            "customer.scorePlus(customer.score)", chainEnvironment)
                    .asResult();

            ExpressionEnvironment lambdaEnvironment = ExpressionEnvironment.builder()
                    .maxCurrentItemDepth(3)
                    .build();
            nestedLambda = ExpressionCompiler.compileOrThrow(
                            "outer := [[1, 2], [3, 4]]; outer.map(@ -> @.map(@ -> @ + 1))", lambdaEnvironment)
                    .asResult();
        }
    }

    public record Address(String city, String country) {
    }

    public record CustomerProfile(String name, BigDecimal score, Address address) {

        public BigDecimal scorePlus(BigDecimal increment) {
            return score.add(increment);
        }
    }
}
