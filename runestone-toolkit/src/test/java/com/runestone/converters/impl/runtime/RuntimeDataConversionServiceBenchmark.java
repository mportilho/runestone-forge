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
    private String smallEnumExactName;
    private String smallEnumLowercaseName;
    private String smallEnumMixedCaseName;
    private Integer smallEnumOrdinal;
    private String largeEnumExactName;
    private String largeEnumLowercaseName;
    private String largeEnumMixedCaseName;
    private Integer largeEnumOrdinal;
    private List<BigDecimal> decimalList;
    private List<String> stringList;
    private int[] primitiveIntArray;
    private String[] stringArray;

    @Setup
    public void setup() {
        service = DefaultRuntimeDataConversionService.standard();
        numericText = "12345";
        decimalSource = new BigDecimal("12345.67");
        integerSource = 12345;
        localDateSource = LocalDate.of(2026, 7, 12);
        smallEnumExactName = "ACTIVE";
        smallEnumLowercaseName = "active";
        smallEnumMixedCaseName = "AcTiVe";
        smallEnumOrdinal = 1;
        largeEnumExactName = "VALUE_31";
        largeEnumLowercaseName = "value_31";
        largeEnumMixedCaseName = "VaLuE_31";
        largeEnumOrdinal = 31;
        decimalList = List.of(
                BigDecimal.ONE,
                BigDecimal.TWO,
                BigDecimal.valueOf(3),
                BigDecimal.valueOf(4),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(6),
                BigDecimal.valueOf(7),
                BigDecimal.valueOf(8));
        stringList = List.of("1", "2", "3", "4", "5", "6", "7", "8");
        primitiveIntArray = new int[] { 1, 2, 3, 4, 5, 6, 7, 8 };
        stringArray = new String[] { "1", "2", "3", "4", "5", "6", "7", "8" };
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
    public SmallStatus convertSmallEnumExactName() {
        return service.convert(smallEnumExactName, SmallStatus.class);
    }

    @Benchmark
    public SmallStatus convertSmallEnumLowercaseName() {
        return service.convert(smallEnumLowercaseName, SmallStatus.class);
    }

    @Benchmark
    public SmallStatus convertSmallEnumMixedCaseName() {
        return service.convert(smallEnumMixedCaseName, SmallStatus.class);
    }

    @Benchmark
    public SmallStatus convertSmallEnumOrdinal() {
        return service.convert(smallEnumOrdinal, SmallStatus.class);
    }

    @Benchmark
    public LargeStatus convertLargeEnumExactName() {
        return service.convert(largeEnumExactName, LargeStatus.class);
    }

    @Benchmark
    public LargeStatus convertLargeEnumLowercaseName() {
        return service.convert(largeEnumLowercaseName, LargeStatus.class);
    }

    @Benchmark
    public LargeStatus convertLargeEnumMixedCaseName() {
        return service.convert(largeEnumMixedCaseName, LargeStatus.class);
    }

    @Benchmark
    public LargeStatus convertLargeEnumOrdinal() {
        return service.convert(largeEnumOrdinal, LargeStatus.class);
    }

    @Benchmark
    public Integer[] convertContainerStringListToIntegerArray() {
        return service.convert(stringList, Integer[].class);
    }

    @Benchmark
    public int[] convertContainerBigDecimalListToIntArray() {
        return service.convert(decimalList, int[].class);
    }

    @Benchmark
    public long[] convertContainerBigDecimalListToLongArray() {
        return service.convert(decimalList, long[].class);
    }

    @Benchmark
    public double[] convertContainerBigDecimalListToDoubleArray() {
        return service.convert(decimalList, double[].class);
    }

    @Benchmark
    public long[] convertArrayIntToLongArray() {
        return service.convert(primitiveIntArray, long[].class);
    }

    @Benchmark
    public Integer[] convertArrayStringToIntegerArray() {
        return service.convert(stringArray, Integer[].class);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(RuntimeDataConversionServiceBenchmark.class.getSimpleName())
                .build();
        new Runner(options).run();
    }

    private enum SmallStatus {
        ACTIVE,
        INACTIVE
    }

    private enum LargeStatus {
        VALUE_00,
        VALUE_01,
        VALUE_02,
        VALUE_03,
        VALUE_04,
        VALUE_05,
        VALUE_06,
        VALUE_07,
        VALUE_08,
        VALUE_09,
        VALUE_10,
        VALUE_11,
        VALUE_12,
        VALUE_13,
        VALUE_14,
        VALUE_15,
        VALUE_16,
        VALUE_17,
        VALUE_18,
        VALUE_19,
        VALUE_20,
        VALUE_21,
        VALUE_22,
        VALUE_23,
        VALUE_24,
        VALUE_25,
        VALUE_26,
        VALUE_27,
        VALUE_28,
        VALUE_29,
        VALUE_30,
        VALUE_31
    }
}
