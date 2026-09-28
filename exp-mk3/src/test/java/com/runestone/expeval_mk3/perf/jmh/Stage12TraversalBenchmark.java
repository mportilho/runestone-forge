package com.runestone.expeval_mk3.perf.jmh;

import com.runestone.expeval_mk3.api.ExpressionEngine;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionTrustMode;
import com.runestone.expeval_mk3.api.MathExpression;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy.OVERRIDABLE;

/** Paired scalar and collection measurements for the SAFE traversal-step guard rail. */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class Stage12TraversalBenchmark {

    @Benchmark
    public Object scalar(ScalarPlans plans) {
        return plans.scalar.compute();
    }

    /** Disables scalar replacement so B/op comparisons reflect source-level execution objects deterministically. */
    @Benchmark
    @Fork(value = 3, jvmArgsAppend = "-XX:-EliminateAllocations")
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public Object scalarAllocation(ScalarPlans plans) {
        return plans.scalar.compute();
    }

    @Benchmark
    public Object collection(CollectionPlans plans) {
        return plans.collection.compute();
    }

    /** Keeps the fixed scope cost visible while proving that traversal debits allocate nothing per item. */
    @Benchmark
    @Fork(value = 3, jvmArgsAppend = "-XX:-EliminateAllocations")
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public Object collectionAllocation(CollectionPlans plans) {
        return plans.collection.compute();
    }

    @State(Scope.Benchmark)
    public static class ScalarPlans {
        @Param({"UNSAFE", "TRUSTED", "SAFE"})
        private ExpressionTrustMode mode;

        private MathExpression scalar;

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = ExpressionEnvironment.builder()
                    .trustMode(mode)
                    .externalSymbol("a", BigDecimal.ONE, OVERRIDABLE)
                    .externalSymbol("b", BigDecimal.TWO, OVERRIDABLE)
                    .build();
            ExpressionEngine engine = ExpressionEngine.builder().build();
            scalar = engine.compileOrThrow("a + b * 3", environment).asMath();
        }
    }

    @State(Scope.Benchmark)
    public static class CollectionPlans {
        @Param({"UNSAFE", "TRUSTED", "SAFE"})
        private ExpressionTrustMode mode;

        @Param({"8", "128"})
        private int itemCount;

        private MathExpression collection;

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = ExpressionEnvironment.builder()
                    .trustMode(mode)
                    .externalSymbol("items", items(itemCount), OVERRIDABLE)
                    .build();
            ExpressionEngine engine = ExpressionEngine.builder().build();
            collection = engine.compileOrThrow("items.map(@ -> @ + 1).sum()", environment)
                    .asMath();
        }

        private static List<BigDecimal> items(int size) {
            return java.util.stream.IntStream.rangeClosed(1, size)
                    .mapToObj(BigDecimal::valueOf)
                    .toList();
        }
    }
}
