package com.runestone.converters.impl.runtime;

import com.runestone.converters.ConversionContext;
import com.runestone.converters.RuntimeDataConversionService;
import com.runestone.converters.RuntimeDataConverter;
import com.runestone.converters.impl.runtime.dates.RuntimeTemporalConverters;
import com.runestone.utils.DateUtils;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.Date;
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
    private ConversionContext context;
    private RuntimeDataConverter<String, LocalDate> stringToLocalDateConverter;
    private RuntimeDataConverter<String, LocalDateTime> stringToLocalDateTimeConverter;
    private RuntimeDataConverter<String, Temporal> stringToTemporalConverter;
    private RuntimeDataConverter<Date, LocalDateTime> dateToLocalDateTimeConverter;
    private String numericText;
    private BigDecimal decimalSource;
    private Integer integerSource;
    private LocalDate localDateSource;
    private String localDateText;
    private String localDateTimeText;
    private String temporalText;
    private Date dateSource;
    private ZoneId zoneId;
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
        context = service.conversionContext();
        stringToLocalDateConverter = temporalConverter(String.class, LocalDate.class);
        stringToLocalDateTimeConverter = temporalConverter(String.class, LocalDateTime.class);
        stringToTemporalConverter = temporalConverter(String.class, Temporal.class);
        dateToLocalDateTimeConverter = temporalConverter(Date.class, LocalDateTime.class);
        numericText = "12345";
        decimalSource = new BigDecimal("12345.67");
        integerSource = 12345;
        localDateSource = LocalDate.of(2026, 7, 12);
        localDateText = "2026-07-12";
        localDateTimeText = "2026-07-12T13:14:15";
        temporalText = localDateTimeText;
        zoneId = context.zoneId();
        dateSource = Date.from(LocalDateTime.of(2026, 7, 12, 13, 14, 15).atZone(zoneId).toInstant());
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
    public boolean canConvertExactStringToIntPrimitive() {
        return service.canConvert(String.class, int.class);
    }

    @Benchmark
    public Integer convertExactStringToInteger() {
        return service.convert(numericText, Integer.class);
    }

    @Benchmark
    public int convertExactStringToIntPrimitive() {
        return service.convert(numericText, int.class);
    }

    @Benchmark
    public boolean canConvertAssignableNumberToLong() {
        return service.canConvert(BigDecimal.class, Long.class);
    }

    @Benchmark
    public boolean canConvertAssignableNumberToLongPrimitive() {
        return service.canConvert(BigDecimal.class, long.class);
    }

    @Benchmark
    public Long convertAssignableNumberToLong() {
        return service.convert(decimalSource, Long.class);
    }

    @Benchmark
    public long convertAssignableNumberToLongPrimitive() {
        return service.convert(decimalSource, long.class);
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
    public Temporal directLocalDateToTemporal() {
        return localDateSource;
    }

    @Benchmark
    public LocalDate convertStringToLocalDate() {
        return service.convert(localDateText, LocalDate.class);
    }

    @Benchmark
    public LocalDate runtimeConverterStringToLocalDate() {
        return stringToLocalDateConverter.convert(localDateText, context);
    }

    @Benchmark
    public LocalDate directStringToLocalDate() {
        return DateUtils.DATETIME_FORMATTER.parse(localDateText, LocalDate::from);
    }

    @Benchmark
    public LocalDateTime convertStringToLocalDateTime() {
        return service.convert(localDateTimeText, LocalDateTime.class);
    }

    @Benchmark
    public LocalDateTime runtimeConverterStringToLocalDateTime() {
        return stringToLocalDateTimeConverter.convert(localDateTimeText, context);
    }

    @Benchmark
    public LocalDateTime directStringToLocalDateTime() {
        return DateUtils.DATETIME_FORMATTER.parse(localDateTimeText, LocalDateTime::from);
    }

    @Benchmark
    public Temporal convertStringToTemporal() {
        return service.convert(temporalText, Temporal.class);
    }

    @Benchmark
    public Temporal runtimeConverterStringToTemporal() {
        return stringToTemporalConverter.convert(temporalText, context);
    }

    @Benchmark
    public Temporal directStringToTemporal() {
        return directToTemporal(temporalText);
    }

    @Benchmark
    public LocalDateTime convertDateToLocalDateTime() {
        return service.convert(dateSource, LocalDateTime.class);
    }

    @Benchmark
    public LocalDateTime runtimeConverterDateToLocalDateTime() {
        return dateToLocalDateTimeConverter.convert(dateSource, context);
    }

    @Benchmark
    public LocalDateTime directDateToLocalDateTime() {
        return Instant.ofEpochMilli(dateSource.getTime()).atZone(zoneId).toLocalDateTime();
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

    private static Temporal directToTemporal(String source) {
        if (source.length() <= 8) {
            return DateUtils.DATETIME_FORMATTER.parse(source, LocalTime::from);
        } else if (source.length() <= 10) {
            return DateUtils.DATETIME_FORMATTER.parse(source, LocalDate::from);
        }
        return DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse(source, LocalDateTime::from);
    }

    @SuppressWarnings("unchecked")
    private static <S, T> RuntimeDataConverter<S, T> temporalConverter(Class<S> sourceType, Class<T> targetType) {
        return (RuntimeDataConverter<S, T>) RuntimeTemporalConverters.all().stream()
                .filter(converter -> converter.sourceType() == sourceType && converter.targetType() == targetType)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing temporal runtime converter"));
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
