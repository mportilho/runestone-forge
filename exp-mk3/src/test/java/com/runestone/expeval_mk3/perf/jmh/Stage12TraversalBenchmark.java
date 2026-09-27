package com.runestone.expeval_mk3.perf.jmh;

import com.runestone.expeval_mk3.api.ExpressionEngine;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionTrustMode;
import com.runestone.expeval_mk3.api.MathExpression;
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

import java.util.concurrent.TimeUnit;

/** Paired scalar and collection measurements for the SAFE traversal-step guard rail. */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class Stage12TraversalBenchmark {

    @Benchmark
    public Object scalar(Plans plans) {
        return plans.scalar.compute();
    }

    @Benchmark
    public Object collection(Plans plans) {
        return plans.collection.compute();
    }

    @State(Scope.Benchmark)
    public static class Plans {
        @Param({"UNSAFE", "TRUSTED", "SAFE"})
        private ExpressionTrustMode mode;

        private MathExpression scalar;
        private MathExpression collection;

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = ExpressionEnvironment.builder().trustMode(mode).build();
            ExpressionEngine engine = ExpressionEngine.builder().build();
            scalar = engine.compileOrThrow("1 + 2 * 3", environment).asMath();
            collection = engine.compileOrThrow(
                    "items := [1, 2, 3, 4, 5, 6, 7, 8]; items.map(@ -> @ + 1).sum()", environment)
                    .asMath();
        }
    }
}
