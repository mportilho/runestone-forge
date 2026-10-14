package com.runestone.converters.impl.stable;

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
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
public class DataConversionServiceBenchmark {

    private DefaultDataConversionService service;
    private String numericText;
    private BigDecimal numericSource;
    private Integer integerSource;
    private LocalDate localDateSource;
    private List<String> stringList;

    @Setup
    public void setup() {
        service = DefaultDataConversionService.standard();
        numericText = "12345";
        numericSource = new BigDecimal("123.45");
        integerSource = 123;
        localDateSource = LocalDate.of(2026, 3, 19);
        stringList = List.of("1", "2", "3", "4", "5", "6", "7", "8");
    }

    @Benchmark
    public boolean canConvertExactStringToInteger() {
        return service.canConvert(String.class, Integer.class);
    }

    @Benchmark
    public Integer convertExactStringToInteger() {
        return service.convert(numericText, Integer.class);
    }

    @Benchmark
    public boolean canConvertAssignableNumberToLong() {
        return service.canConvert(BigDecimal.class, Long.class);
    }

    @Benchmark
    public Long convertAssignableNumberToLong() {
        return service.convert(numericSource, Long.class);
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

    @Benchmark
    public Integer[] convertContainerStringListToIntegerArray() {
        return service.convert(stringList, Integer[].class);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(DataConversionServiceBenchmark.class.getSimpleName())
                .build();
        new Runner(options).run();
    }
}
