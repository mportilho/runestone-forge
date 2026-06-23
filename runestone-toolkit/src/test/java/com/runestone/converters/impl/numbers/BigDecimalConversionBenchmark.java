package com.runestone.converters.impl.numbers;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.concurrent.atomic.DoubleAdder;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(0)
public class BigDecimalConversionBenchmark {

    private NumberToBigDecimalConverter converter;
    private Float floatValue;
    private Double doubleValue;
    private DoubleAccumulator accumulator;
    private DoubleAdder adder;

    @Setup
    public void setup() {
        converter = new NumberToBigDecimalConverter();
        floatValue = 0.1f;
        doubleValue = 0.1d;
        accumulator = new DoubleAccumulator(Double::sum, 0.1);
        adder = new DoubleAdder();
        adder.add(0.1);
    }

    @Benchmark
    public BigDecimal convertFloat() {
        return converter.convert(floatValue);
    }

    @Benchmark
    public BigDecimal convertDouble() {
        return converter.convert(doubleValue);
    }

    @Benchmark
    public BigDecimal convertDoubleAccumulator() {
        return converter.convert(accumulator);
    }

    @Benchmark
    public BigDecimal convertDoubleAdder() {
        return converter.convert(adder);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BigDecimalConversionBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
