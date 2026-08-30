package com.runestone.expeval_mk3.perf.jmh;

import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ExternalSymbol;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.ScalarType;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class ExternalSymbolDefaultPreparationBenchmark {

    private final BigDecimal canonicalScalar = new BigDecimal("123.45");
    private final List<Integer> explicitlyTypedDefault = List.of(1, 2, 3, 4, 5, 6, 7, 8);
    private final Map<String, List<Integer>> compoundDefault = compoundDefault();

    @Benchmark
    public void canonicalScalar(Blackhole blackhole) {
        blackhole.consume(ExternalSymbol.withDefault(
                "amount", canonicalScalar, ExternalSymbolOverwritePolicy.FIXED));
    }

    @Benchmark
    public void explicitlyTyped(Blackhole blackhole) {
        blackhole.consume(ExternalSymbol.withDefault(
                "scores",
                new CollectionType(ScalarType.NUMBER),
                explicitlyTypedDefault,
                ExternalSymbolOverwritePolicy.FIXED));
    }

    @Benchmark
    public void compoundInferred(Blackhole blackhole) {
        blackhole.consume(ExternalSymbol.withDefault(
                "scoresByGroup", compoundDefault, ExternalSymbolOverwritePolicy.FIXED));
    }

    private static Map<String, List<Integer>> compoundDefault() {
        Map<String, List<Integer>> values = new LinkedHashMap<>();
        values.put("delta", List.of(25, 26, 27, 28, 29, 30, 31, 32));
        values.put("beta", List.of(9, 10, 11, 12, 13, 14, 15, 16));
        values.put("alpha", List.of(1, 2, 3, 4, 5, 6, 7, 8));
        values.put("gamma", List.of(17, 18, 19, 20, 21, 22, 23, 24));
        return values;
    }
}
