package com.runestone.expeval2.perf.jmh;

import com.runestone.expeval2.catalog.functions.MathFunctions;
import org.openjdk.jmh.annotations.*;

import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class MathFunctionsBenchmark {

    @Param({"10", "100", "1000"})
    private int size;

    private BigDecimal[] data;

    @Setup
    public void setup() {
        data = new BigDecimal[size];
        Random random = new Random(42);
        for (int i = 0; i < size; i++) {
            data[i] = BigDecimal.valueOf(random.nextDouble() * 1000);
        }
    }

    @Benchmark
    public BigDecimal testMean() {
        return MathFunctions.mean(data);
    }

    @Benchmark
    public BigDecimal testVariance() {
        return MathFunctions.variance(data, 0);
    }

    @Benchmark
    public BigDecimal testMeanDev() {
        return MathFunctions.meanDev(data);
    }
}
