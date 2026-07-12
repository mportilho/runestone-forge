package com.runestone.converters.impl.runtime;

import com.runestone.converters.RuntimeDataConversionService;
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
public class RuntimeDataConversionServiceBenchmark {

    private RuntimeDataConversionService service;
    private String numericText;
    private BigDecimal decimalSource;
    private Integer integerSource;
    private LocalDate localDateSource;
    private List<String> stringList;

    @Setup
    public void setup() {
        service = DefaultRuntimeDataConversionService.standard();
        numericText = "12345";
        decimalSource = new BigDecimal("12345.67");
        integerSource = 12345;
        localDateSource = LocalDate.of(2026, 7, 12);
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
        return service.convert(decimalSource, Long.class);
    }

    @Benchmark
    public String convertAssignableNumberToString() {
        return service.convert(integerSource, String.class);
    }

    @Benchmark
    public Temporal convertExactLocalDateToTemporal() {
        return service.convert(localDateSource, Temporal.class);
    }

    @Benchmark
    public Integer[] convertContainerStringListToIntegerArray() {
        return service.convert(stringList, Integer[].class);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(RuntimeDataConversionServiceBenchmark.class.getSimpleName())
                .build();
        new Runner(options).run();
    }
}
