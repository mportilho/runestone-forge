package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.transformer.FilterValueContext;
import com.runestone.dynafilter.core.transformer.FilterValueTransformer;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
public class ScalarFilterValueTransformerBenchmark {

    @Benchmark
    public void boundChain(TransformerState state, Blackhole blackhole) {
        blackhole.consume(state.chain.transformScalar("value", 0));
    }

    @Benchmark
    public void directCalls(TransformerState state, Blackhole blackhole) {
        Object value = "value";
        for (int i = 0; i < state.transformerCount; i++) {
            Object transformed = state.transformers[i].transform(value, state.context);
            if (transformed == null) {
                throw new IllegalStateException("Unexpected null benchmark result");
            }
            value = transformed;
        }
        blackhole.consume(value);
    }

    @Benchmark
    public void boundReplacingChain(ReplacingTransformerState state, Blackhole blackhole) {
        blackhole.consume(state.chain.transformScalar("value", 0));
    }

    @Benchmark
    public void directReplacingCalls(ReplacingTransformerState state, Blackhole blackhole) {
        Object value = "value";
        for (int i = 0; i < state.transformerCount; i++) {
            Object transformed = state.transformers[i].transform(value, state.context);
            if (transformed == null) {
                throw new IllegalStateException("Unexpected null benchmark result");
            }
            value = transformed;
        }
        blackhole.consume(value);
    }

    @State(Scope.Benchmark)
    public static class TransformerState {

        @Param({"0", "1", "3"})
        int transformerCount;

        final FilterValueContext context = new FilterValueContext("value", 0, java.util.List.of("target"),
                Equals.class, Object.class);
        final FilterValueTransformer[] transformers = {
                new IdentityTransformer(), new IdentityTransformer(), new IdentityTransformer()
        };
        BoundFilterValueTransformerChain chain;

        @Setup
        @SuppressWarnings("unchecked")
        public void setup() {
            Class<? extends FilterValueTransformer>[] types = new Class[transformerCount];
            for (int i = 0; i < transformerCount; i++) {
                types[i] = IdentityTransformer.class;
            }
            AtomicInteger resolvedIndex = new AtomicInteger();
            chain = BoundFilterValueTransformerChain.bind(types, type -> transformers[resolvedIndex.getAndIncrement()],
                    new String[]{"value"}, new String[]{"target"}, Equals.class, Object.class);
        }
    }

    @State(Scope.Benchmark)
    public static class ReplacingTransformerState {

        @Param({"1", "3"})
        int transformerCount;

        final FilterValueContext context = new FilterValueContext("value", 0, java.util.List.of("target"),
                Equals.class, Object.class);
        final FilterValueTransformer[] transformers = {
                new ReplacingTransformer(), new ReplacingTransformer(), new ReplacingTransformer()
        };
        BoundFilterValueTransformerChain chain;

        @Setup
        @SuppressWarnings("unchecked")
        public void setup() {
            Class<? extends FilterValueTransformer>[] types = new Class[transformerCount];
            for (int i = 0; i < transformerCount; i++) {
                types[i] = ReplacingTransformer.class;
            }
            AtomicInteger resolvedIndex = new AtomicInteger();
            chain = BoundFilterValueTransformerChain.bind(types, type -> transformers[resolvedIndex.getAndIncrement()],
                    new String[]{"value"}, new String[]{"target"}, Equals.class, Object.class);
        }
    }

    public static final class IdentityTransformer implements FilterValueTransformer {
        @Override
        public Object transform(Object value, FilterValueContext context) {
            return value;
        }
    }

    public static final class ReplacingTransformer implements FilterValueTransformer {
        @Override
        public Object transform(Object value, FilterValueContext context) {
            return new TransformedValue(value);
        }
    }

    record TransformedValue(Object value) {
    }
}
