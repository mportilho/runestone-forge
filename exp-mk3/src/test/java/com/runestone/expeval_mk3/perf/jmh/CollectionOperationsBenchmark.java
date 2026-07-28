package com.runestone.expeval_mk3.perf.jmh;

import com.runestone.expeval_mk3.api.CompiledExpression;
import com.runestone.expeval_mk3.api.ExpressionCompiler;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
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

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Fork(1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class CollectionOperationsBenchmark {

    @Benchmark
    public void map(RuntimePlans plans, Blackhole blackhole) {
        blackhole.consume(plans.map.compute());
    }

    @Benchmark
    public void sum(RuntimePlans plans, Blackhole blackhole) {
        blackhole.consume(plans.sum.compute());
    }

    @Benchmark
    public void mapThenSum(RuntimePlans plans, Blackhole blackhole) {
        blackhole.consume(plans.mapThenSum.compute());
    }

    @Benchmark
    public void allShortCircuit(RuntimePlans plans, Blackhole blackhole) {
        blackhole.consume(plans.allShortCircuit.compute());
    }

    @Benchmark
    public void sortBy(RuntimePlans plans, Blackhole blackhole) {
        blackhole.consume(plans.sortBy.compute());
    }

    @Benchmark
    public void reduce(RuntimePlans plans, Blackhole blackhole) {
        blackhole.consume(plans.reduce.compute());
    }

    @Benchmark
    public void wildcardMaterialization(RuntimePlans plans, Blackhole blackhole) {
        blackhole.consume(plans.wildcardMaterialization.compute());
    }

    @Benchmark
    public void safeCall(RuntimePlans plans, Blackhole blackhole) {
        blackhole.consume(plans.safeCall.compute());
    }

    @State(Scope.Benchmark)
    public static class RuntimePlans {

        private CompiledExpression map;
        private CompiledExpression sum;
        private CompiledExpression mapThenSum;
        private CompiledExpression allShortCircuit;
        private CompiledExpression sortBy;
        private CompiledExpression reduce;
        private CompiledExpression wildcardMaterialization;
        private CompiledExpression safeCall;

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = ExpressionEnvironment.standard();
            map = ExpressionCompiler.compile(
                    "items := [1, 2, 3, 4, 5, 6, 7, 8]; items.map(@ -> @ + 1)", environment);
            sum = ExpressionCompiler.compile("items := [1, 2, 3, 4, 5, 6, 7, 8]; items.sum()", environment);
            mapThenSum = ExpressionCompiler.compile(
                    "items := [1, 2, 3, 4, 5, 6, 7, 8]; items.map(@ -> @ + 1).sum()", environment);
            allShortCircuit = ExpressionCompiler.compile(
                    "items := [0, 1, 2, 3, 4, 5, 6, 7]; items.all(@ -> @ > 0)", environment);
            sortBy = ExpressionCompiler.compile(
                    "items := [8, 3, 5, 1, 7, 2, 6, 4]; items.sortBy(@ -> @, \"asc\")", environment);
            reduce = ExpressionCompiler.compile(
                    "items := [1, 2, 3, 4, 5, 6, 7, 8]; "
                            + "items.reduce(0, @ -> @.accumulator + @.item)",
                    environment);
            wildcardMaterialization = ExpressionCompiler.compile(
                    "items := [1, 2, 3, 4, 5, 6, 7, 8]; items[*]", environment);
            safeCall = ExpressionCompiler.compile(
                    "items := [1, 2, 3, 4, 5, 6, 7, 8]; items?.map(@ -> @ + 1)", environment);
        }
    }
}
