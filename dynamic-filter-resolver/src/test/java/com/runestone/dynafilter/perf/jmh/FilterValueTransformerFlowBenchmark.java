package com.runestone.dynafilter.perf.jmh;

import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.Conjunction;
import com.runestone.dynafilter.core.generator.annotation.Filter;
import com.runestone.dynafilter.core.operation.types.Dynamic;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.IsIn;
import com.runestone.dynafilter.core.transformer.FilterValueContext;
import com.runestone.dynafilter.core.transformer.FilterValueTransformer;
import com.runestone.dynafilter.core.transformer.FilterValueTransformerRegistry;
import com.runestone.dynafilter.core.transformer.FilterValueTransformerResolver;
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

import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
public class FilterValueTransformerFlowBenchmark {

    @Benchmark
    public void warmCache(FlowState state, Blackhole blackhole) {
        blackhole.consume(state.generator.generateStatements(state.input, state.parameters));
    }

    @Benchmark
    public void coldCache(FlowState state, Blackhole blackhole) {
        AnnotationStatementGenerator generator = new AnnotationStatementGenerator(null, state.transformerResolver);
        blackhole.consume(generator.generateStatements(state.input, state.parameters));
    }

    @State(Scope.Benchmark)
    public static class FlowState {

        @Param({"normalScalar", "normalContainer", "dynamicScalar", "dynamicContainer"})
        String flow;

        @Param({"0", "1", "3"})
        int transformerCount;

        @Param({"identity", "replacement"})
        String behavior;

        AnnotationStatementGenerator generator;
        AnnotationStatementInput input;
        Map<String, Object> parameters;
        FilterValueTransformerResolver transformerResolver;

        @Setup
        public void setup() {
            FilterValueTransformerRegistry registry = new FilterValueTransformerRegistry();
            registry.register(IdentityTransformer.class, new IdentityTransformer(behavior.equals("replacement")));
            transformerResolver = registry.toResolver();
            generator = new AnnotationStatementGenerator(null, transformerResolver);
            Class<?> filterType = filterType();
            input = new AnnotationStatementInput(filterType, null);
            parameters = parameters();
            generator.warmup(input);
        }

        private Class<?> filterType() {
            return switch (flow) {
                case "normalScalar" -> switch (transformerCount) {
                    case 0 -> NormalScalarZero.class;
                    case 1 -> NormalScalarOne.class;
                    default -> NormalScalarThree.class;
                };
                case "normalContainer" -> switch (transformerCount) {
                    case 0 -> NormalContainerZero.class;
                    case 1 -> NormalContainerOne.class;
                    default -> NormalContainerThree.class;
                };
                case "dynamicScalar", "dynamicContainer" -> switch (transformerCount) {
                    case 0 -> DynamicZero.class;
                    case 1 -> DynamicOne.class;
                    default -> DynamicThree.class;
                };
                default -> throw new IllegalArgumentException("Unknown flow " + flow);
            };
        }

        private Map<String, Object> parameters() {
            return switch (flow) {
                case "normalScalar" -> Map.of("value", "one");
                case "normalContainer" -> Map.of("value", new Object[]{"one", "two", "three", "four"});
                case "dynamicScalar" -> Map.of("value", new Object[]{"eq", "one"});
                case "dynamicContainer" -> Map.of("value", new Object[]{"in", "one", "two", "three", "four"});
                default -> throw new IllegalArgumentException("Unknown flow " + flow);
            };
        }
    }

    @Conjunction(@Filter(path = "target", parameters = "value", operation = Equals.class))
    private interface NormalScalarZero {
    }

    @Conjunction(@Filter(path = "target", parameters = "value", operation = IsIn.class))
    private interface NormalContainerZero {
    }

    @Conjunction(@Filter(path = "target", parameters = "value", operation = Equals.class,
            transformers = IdentityTransformer.class))
    private interface NormalScalarOne {
    }

    @Conjunction(@Filter(path = "target", parameters = "value", operation = IsIn.class,
            transformers = IdentityTransformer.class))
    private interface NormalContainerOne {
    }

    @Conjunction(@Filter(path = "target", parameters = "value", operation = Equals.class,
            transformers = {IdentityTransformer.class, IdentityTransformer.class, IdentityTransformer.class}))
    private interface NormalScalarThree {
    }

    @Conjunction(@Filter(path = "target", parameters = "value", operation = IsIn.class,
            transformers = {IdentityTransformer.class, IdentityTransformer.class, IdentityTransformer.class}))
    private interface NormalContainerThree {
    }

    @Conjunction(@Filter(path = "target", parameters = "value", operation = Dynamic.class))
    private interface DynamicZero {
    }

    @Conjunction(@Filter(path = "target", parameters = "value", operation = Dynamic.class,
            transformers = IdentityTransformer.class))
    private interface DynamicOne {
    }

    @Conjunction(@Filter(path = "target", parameters = "value", operation = Dynamic.class,
            transformers = {IdentityTransformer.class, IdentityTransformer.class, IdentityTransformer.class}))
    private interface DynamicThree {
    }

    public static final class IdentityTransformer implements FilterValueTransformer {
        private final boolean replacing;

        public IdentityTransformer() {
            this(false);
        }

        IdentityTransformer(boolean replacing) {
            this.replacing = replacing;
        }

        @Override
        public Object transform(Object value, FilterValueContext context) {
            return replacing ? new TransformedValue(value) : value;
        }
    }

    record TransformedValue(Object value) {
    }
}
