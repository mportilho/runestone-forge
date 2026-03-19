package com.runestone.converters.impl;

import com.runestone.converters.DataConversionService;
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

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
public class CollectionArrayConversionBenchmark {

    private MeasuredCollectionArrayConversionService service;
    private List<BigDecimal> decimalValues;
    private List<Integer> integerValues;
    private List<String> stringValues;

    @Setup
    public void setup() {
        service = new ProductionCollectionArrayConversionService();
        decimalValues = new ArrayList<>(64);
        integerValues = new ArrayList<>(64);
        stringValues = new ArrayList<>(64);
        for (int i = 0; i < 64; i++) {
            decimalValues.add(BigDecimal.valueOf(i + 1L));
            integerValues.add(i + 1);
            stringValues.add(Integer.toString(i + 1));
        }
    }

    @Benchmark
    public double[] convertBigDecimalListToDoubleArray() {
        return (double[]) service.convert(decimalValues, double[].class);
    }

    @Benchmark
    public int[] convertBigDecimalListToIntArray() {
        return (int[]) service.convert(decimalValues, int[].class);
    }

    @Benchmark
    public long[] convertBigDecimalListToLongArray() {
        return (long[]) service.convert(decimalValues, long[].class);
    }

    @Benchmark
    public BigDecimal[] convertBigDecimalListToBigDecimalArray() {
        return (BigDecimal[]) service.convert(decimalValues, BigDecimal[].class);
    }

    @Benchmark
    public Number[] convertIntegerListToNumberArray() {
        return (Number[]) service.convert(integerValues, Number[].class);
    }

    @Benchmark
    public Integer[] convertStringListToIntegerArray() {
        return (Integer[]) service.convert(stringValues, Integer[].class);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(CollectionArrayConversionBenchmark.class.getSimpleName())
                .build();
        new Runner(options).run();
    }

    private interface MeasuredCollectionArrayConversionService {
        Object convert(Collection<?> source, Class<?> targetType);
    }

    private static final class ReflectiveCollectionArrayConversionService implements MeasuredCollectionArrayConversionService {
        private final DataConversionService delegate = new DefaultDataConversionService();

        @Override
        public Object convert(Collection<?> source, Class<?> targetType) {
            Class<?> componentType = targetType.getComponentType();
            Object array = Array.newInstance(componentType, source.size());
            int i = 0;
            for (Object element : source) {
                Array.set(array, i++, delegate.convert(element, componentType));
            }
            return array;
        }
    }

    private static final class ProductionCollectionArrayConversionService implements MeasuredCollectionArrayConversionService {
        private final DataConversionService delegate = new DefaultDataConversionService();

        @Override
        public Object convert(Collection<?> source, Class<?> targetType) {
            return delegate.convert(source, targetType);
        }
    }
}
