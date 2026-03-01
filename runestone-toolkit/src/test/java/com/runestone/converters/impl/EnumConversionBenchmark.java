package com.runestone.converters.impl;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(0)
public class EnumConversionBenchmark {

    public enum SmallEnum {
        A, B, C, D, E
    }

    public enum LargeEnum {
        V1, V2, V3, V4, V5, V6, V7, V8, V9, V10,
        V11, V12, V13, V14, V15, V16, V17, V18, V19, V20,
        V21, V22, V23, V24, V25, V26, V27, V28, V29, V30,
        V31, V32, V33, V34, V35, V36, V37, V38, V39, V40,
        V41, V42, V43, V44, V45, V46, V47, V48, V49, V50
    }

    private DefaultDataConversionService service;
    private String smallEnumNameUpper;
    private String smallEnumNameLower;
    private String largeEnumNameUpper;
    private String largeEnumNameLower;
    private Integer smallEnumOrdinal;
    private Integer largeEnumOrdinal;

    @Setup
    public void setup() {
        service = new DefaultDataConversionService();
        smallEnumNameUpper = "E";
        smallEnumNameLower = "e";
        largeEnumNameUpper = "V50";
        largeEnumNameLower = "v50";
        smallEnumOrdinal = 4;
        largeEnumOrdinal = 49;
    }

    @Benchmark
    public Object convertSmallEnumByName() {
        return service.convert(smallEnumNameUpper, SmallEnum.class);
    }

    @Benchmark
    public Object convertSmallEnumByNameIgnoreCase() {
        return service.convert(smallEnumNameLower, SmallEnum.class);
    }

    @Benchmark
    public Object convertLargeEnumByName() {
        return service.convert(largeEnumNameUpper, LargeEnum.class);
    }

    @Benchmark
    public Object convertLargeEnumByNameIgnoreCase() {
        return service.convert(largeEnumNameLower, LargeEnum.class);
    }

    @Benchmark
    public Object convertSmallEnumByOrdinal() {
        return service.convert(smallEnumOrdinal, SmallEnum.class);
    }

    @Benchmark
    public Object convertLargeEnumByOrdinal() {
        return service.convert(largeEnumOrdinal, LargeEnum.class);
    }

    // Comparison with manual old logic to show O(N)
    @Benchmark
    public Object convertLargeEnumByNameOldLogic() {
        String value = largeEnumNameUpper;
        Class<LargeEnum> targetType = LargeEnum.class;
        for (LargeEnum enumConstant : targetType.getEnumConstants()) {
            if (enumConstant.name().equalsIgnoreCase(value)) {
                return enumConstant;
            }
        }
        return null;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(EnumConversionBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
