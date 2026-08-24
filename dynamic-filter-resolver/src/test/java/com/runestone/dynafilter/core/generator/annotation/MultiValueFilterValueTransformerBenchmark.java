package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.operation.ComparisonOperation;
import com.runestone.dynafilter.core.operation.types.Dynamic;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.IsIn;
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

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
public class MultiValueFilterValueTransformerBenchmark {

    @Benchmark
    public void normalBoundChain(ContainerState state, Blackhole blackhole) {
        blackhole.consume(state.normalChain.transformValue(state.values, 0));
    }

    @Benchmark
    public void normalDirectCalls(ContainerState state, Blackhole blackhole) {
        blackhole.consume(state.transformDirectly(state.normalContext));
    }

    @Benchmark
    public void dynamicBoundChain(ContainerState state, Blackhole blackhole) {
        blackhole.consume(state.dynamicChain.transformDynamicPayload(
                state.dynamicValues, 1, ComparisonOperation.IN));
    }

    @Benchmark
    public void dynamicDirectCalls(ContainerState state, Blackhole blackhole) {
        blackhole.consume(state.transformDynamicDirectly(ComparisonOperation.IN));
    }

    @State(Scope.Benchmark)
    public static class ContainerState {

        @Param({"1", "3"})
        int transformerCount;

        final Object[] values = {"one", "two", "three", "four"};
        final Object[] dynamicValues = {"in", "one", "two", "three", "four"};
        final FilterValueContext normalContext = new FilterValueContext(
                "values", 0, List.of("target"), Equals.class, Object.class);
        final FilterValueContext[] dynamicContexts = new FilterValueContext[ComparisonOperation.values().length];
        final FilterValueTransformer[] transformers = {
                new ReplacingTransformer(), new ReplacingTransformer(), new ReplacingTransformer()
        };
        BoundFilterValueTransformerChain normalChain;
        BoundFilterValueTransformerChain dynamicChain;

        @Setup
        @SuppressWarnings("unchecked")
        public void setup() {
            Class<? extends FilterValueTransformer>[] types = new Class[transformerCount];
            for (int i = 0; i < transformerCount; i++) {
                types[i] = ReplacingTransformer.class;
            }
            AtomicInteger normalIndex = new AtomicInteger();
            normalChain = BoundFilterValueTransformerChain.bind(types,
                    type -> transformers[normalIndex.getAndIncrement()], new String[]{"values"},
                    new String[]{"target"}, Equals.class, Object.class);
            AtomicInteger dynamicIndex = new AtomicInteger();
            dynamicChain = BoundFilterValueTransformerChain.bind(types,
                    type -> transformers[dynamicIndex.getAndIncrement()], new String[]{"values"},
                    new String[]{"target"}, Dynamic.class, Object.class);
            dynamicContexts[ComparisonOperation.IN.ordinal()] = new FilterValueContext(
                    "values", 0, List.of("target"), IsIn.class, Object.class);
        }

        Object[] transformDynamicDirectly(ComparisonOperation operation) {
            return transformDirectly(dynamicContexts[operation.ordinal()]);
        }

        Object[] transformDirectly(FilterValueContext context) {
            Object[] result = new Object[values.length];
            for (int valueIndex = 0; valueIndex < values.length; valueIndex++) {
                Object value = values[valueIndex];
                for (int transformerIndex = 0; transformerIndex < transformerCount; transformerIndex++) {
                    value = transformers[transformerIndex].transform(value, context);
                    if (value == null) {
                        throw new IllegalStateException("Unexpected null benchmark result");
                    }
                }
                result[valueIndex] = value;
            }
            return result;
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
