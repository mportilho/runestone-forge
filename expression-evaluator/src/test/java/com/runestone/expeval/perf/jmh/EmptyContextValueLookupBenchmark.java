package com.runestone.expeval.perf.jmh;

import com.runestone.expeval.expression.ExpressionContext;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class EmptyContextValueLookupBenchmark {

    private ExpressionContext emptyContext;
    private ExpressionContext supplierOnlyContext;
    private ExpressionContext dictionaryContext;

    @Setup(Level.Trial)
    public void setup() {
        emptyContext = new ExpressionContext();
        supplierOnlyContext = new ExpressionContext(name -> null, null, null);
        dictionaryContext = new ExpressionContext(null, Map.of("known", 1), null);
    }

    @Benchmark
    public void findMissingValueOnEmptyContext(Blackhole blackhole) {
        blackhole.consume(emptyContext.findValue("missing"));
    }

    @Benchmark
    public void findMissingValueWithSupplierOnlyContext(Blackhole blackhole) {
        blackhole.consume(supplierOnlyContext.findValue("missing"));
    }

    @Benchmark
    public void findMissingValueOnDictionaryContext(Blackhole blackhole) {
        blackhole.consume(dictionaryContext.findValue("missing"));
    }
}
