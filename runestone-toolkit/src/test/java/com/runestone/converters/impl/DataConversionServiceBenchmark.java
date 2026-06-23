package com.runestone.converters.impl;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.Temporal;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
public class DataConversionServiceBenchmark {

    private DefaultDataConversionService service;
    private BigDecimal numericSource;
    private Integer integerSource;
    private LocalDate localDateSource;

    @Setup
    public void setup() {
        service = new DefaultDataConversionService();
        numericSource = new BigDecimal("123.45");
        integerSource = 123;
        localDateSource = LocalDate.of(2026, 3, 19);
    }

    @Benchmark
    public Integer convertNumberToIntegerWrapper() {
        return service.convert(numericSource, Integer.class);
    }

    @Benchmark
    public int convertNumberToIntegerPrimitive() {
        return service.convert(numericSource, int.class);
    }

    @Benchmark
    public Long convertNumberToLongWrapper() {
        return service.convert(numericSource, Long.class);
    }

    @Benchmark
    public long convertNumberToLongPrimitive() {
        return service.convert(numericSource, long.class);
    }

    @Benchmark
    public Double convertNumberToDoubleWrapper() {
        return service.convert(numericSource, Double.class);
    }

    @Benchmark
    public double convertNumberToDoublePrimitive() {
        return service.convert(numericSource, double.class);
    }

    @Benchmark
    public Number convertAssignableNumber() {
        return service.convert(integerSource, Number.class);
    }

    @Benchmark
    public Temporal convertAssignableTemporal() {
        return service.convert(localDateSource, Temporal.class);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(DataConversionServiceBenchmark.class.getSimpleName())
                .build();
        new Runner(options).run();
    }
}
