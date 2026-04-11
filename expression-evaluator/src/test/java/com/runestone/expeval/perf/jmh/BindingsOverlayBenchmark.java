package com.runestone.expeval.perf.jmh;

import com.runestone.expeval.api.MathExpression;
import com.runestone.expeval.perf.BindingsBenchmarkSupport;
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

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Isolates the caller-visible cost of external bindings preparation in {@code compute(Map)}.
 *
 * <p>The scenarios intentionally separate three cases:
 * defaults only, defaults plus a small number of overrides, and overrides without defaults.
 * This makes it easier to see whether the runtime is paying to copy catalog defaults on every call.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class BindingsOverlayBenchmark {

    @Benchmark
    public BigDecimal manyDefaultsNoOverrides(ManyDefaultsNoOverridesState state) {
        return state.expression.compute(state.values);
    }

    @Benchmark
    public BigDecimal manyDefaultsOneOverride(ManyDefaultsOneOverrideState state) {
        return state.expression.compute(state.values);
    }

    @Benchmark
    public BigDecimal manyDefaultsTwoOverrides(ManyDefaultsTwoOverridesState state) {
        return state.expression.compute(state.values);
    }

    @Benchmark
    public BigDecimal noDefaultsOneOverride(NoDefaultsOneOverrideState state) {
        return state.expression.compute(state.values);
    }

    @Benchmark
    public BigDecimal noDefaultsTwoOverrides(NoDefaultsTwoOverridesState state) {
        return state.expression.compute(state.values);
    }

    @State(Scope.Thread)
    public static class ManyDefaultsNoOverridesState {
        private MathExpression expression;
        private Map<String, Object> values;

        @Setup(Level.Trial)
        public void setUp() {
            expression = MathExpression.compile(
                    BindingsBenchmarkSupport.MANY_DEFAULTS_EXPRESSION,
                    BindingsBenchmarkSupport.manyDefaultsEnvironment()
            );
            values = BindingsBenchmarkSupport.noOverrides();
        }
    }

    @State(Scope.Thread)
    public static class ManyDefaultsOneOverrideState {
        private MathExpression expression;
        private Map<String, Object> values;

        @Setup(Level.Trial)
        public void setUp() {
            expression = MathExpression.compile(
                    BindingsBenchmarkSupport.MANY_DEFAULTS_EXPRESSION,
                    BindingsBenchmarkSupport.manyDefaultsEnvironment()
            );
            values = BindingsBenchmarkSupport.oneOverride();
        }
    }

    @State(Scope.Thread)
    public static class ManyDefaultsTwoOverridesState {
        private MathExpression expression;
        private Map<String, Object> values;

        @Setup(Level.Trial)
        public void setUp() {
            expression = MathExpression.compile(
                    BindingsBenchmarkSupport.MANY_DEFAULTS_EXPRESSION,
                    BindingsBenchmarkSupport.manyDefaultsEnvironment()
            );
            values = BindingsBenchmarkSupport.twoOverrides();
        }
    }

    @State(Scope.Thread)
    public static class NoDefaultsOneOverrideState {
        private MathExpression expression;
        private Map<String, Object> values;

        @Setup(Level.Trial)
        public void setUp() {
            expression = MathExpression.compile(
                    BindingsBenchmarkSupport.SINGLE_OVERRIDE_EXPRESSION,
                    BindingsBenchmarkSupport.noDefaultsEnvironment()
            );
            values = BindingsBenchmarkSupport.oneOverrideNoDefaults();
        }
    }

    @State(Scope.Thread)
    public static class NoDefaultsTwoOverridesState {
        private MathExpression expression;
        private Map<String, Object> values;

        @Setup(Level.Trial)
        public void setUp() {
            expression = MathExpression.compile(
                    BindingsBenchmarkSupport.DOUBLE_OVERRIDE_EXPRESSION,
                    BindingsBenchmarkSupport.noDefaultsEnvironment()
            );
            values = BindingsBenchmarkSupport.twoOverridesNoDefaults();
        }
    }
}
