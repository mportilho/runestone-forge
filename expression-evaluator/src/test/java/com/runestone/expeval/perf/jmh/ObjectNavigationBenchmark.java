package com.runestone.expeval.perf.jmh;

import com.runestone.expeval.api.LogicalExpression;
import com.runestone.expeval.api.MathExpression;
import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.internal.runtime.ExpressionCompiler;
import com.runestone.expeval.perf.ObjectNavigationBenchmarkSupport;
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
import java.util.concurrent.TimeUnit;

/**
 * Isolates the steady-state and compile/startup costs introduced by object navigation.
 *
 * <p>Runtime scenarios reuse prebuilt input maps so the measured cost stays focused on the
 * navigation machinery itself: symbol lookup, property-chain traversal, {@code MethodHandle}
 * invocation and the reflective fallback path. Startup scenarios capture the two non-hot but
 * feature-specific phases added by the implementation: type-hint discovery during
 * {@code ExpressionEnvironment.build()} and compile-time semantic/plan construction.
 *
 * <p>Run with the standard JMH script to collect {@code ns/op} and {@code B/op}:
 * <pre>
 *   run-jmh.sh expression-evaluator ObjectNavigationBenchmark /tmp/performance-benchmark/object-navigation.json
 * </pre>
 *
 * <p>To inspect sampled stack hot methods, rerun the same benchmark with JMH's stack profiler.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class ObjectNavigationBenchmark {

    @Benchmark
    public boolean typedNestedProperty(TypedNestedPropertyState state) {
        return state.expression.compute(
                ObjectNavigationBenchmarkSupport.userOnlyValues(state.index++));
    }

    @Benchmark
    public BigDecimal typedMethodNoArg(TypedMethodNoArgState state) {
        return state.expression.compute(
                ObjectNavigationBenchmarkSupport.userOnlyValues(state.index++));
    }

    @Benchmark
    public BigDecimal typedMethodWithArgument(TypedMethodWithArgumentState state) {
        return state.expression.compute(
                ObjectNavigationBenchmarkSupport.userAndRateValues(state.index++));
    }

    @Benchmark
    public boolean reflectiveNestedProperty(ReflectiveNestedPropertyState state) {
        return state.expression.compute(
                ObjectNavigationBenchmarkSupport.userOnlyValues(state.index++));
    }

    @Benchmark
    public BigDecimal reflectiveMethodWithArgument(ReflectiveMethodWithArgumentState state) {
        return state.expression.compute(
                ObjectNavigationBenchmarkSupport.userAndRateValues(state.index++));
    }

    @Benchmark
    public ExpressionEnvironment buildTypedEnvironment(BuildTypedEnvironmentState state) {
        return ObjectNavigationBenchmarkSupport.buildTypedNavigationEnvironment();
    }

    @Benchmark
    public LogicalExpression compileTypedNestedProperty(CompileTypedNestedPropertyState state) {
        return LogicalExpression.compile(
                ObjectNavigationBenchmarkSupport.NESTED_PROPERTY_EXPRESSION,
                state.environment,
                state.compiler
        );
    }

    @Benchmark
    public MathExpression compileTypedMethodWithArgument(CompileTypedMethodWithArgumentState state) {
        return MathExpression.compile(
                ObjectNavigationBenchmarkSupport.METHOD_WITH_ARG_EXPRESSION,
                state.environment,
                state.compiler
        );
    }

    @Benchmark
    public MathExpression compileReflectiveMethodWithArgument(CompileReflectiveMethodWithArgumentState state) {
        return MathExpression.compile(
                ObjectNavigationBenchmarkSupport.METHOD_WITH_ARG_EXPRESSION,
                state.environment,
                state.compiler
        );
    }

    @State(Scope.Thread)
    public static class TypedNestedPropertyState {
        private LogicalExpression expression;
        private int index;

        @Setup(Level.Trial)
        public void setUp() {
            expression = LogicalExpression.compile(
                    ObjectNavigationBenchmarkSupport.NESTED_PROPERTY_EXPRESSION,
                    ObjectNavigationBenchmarkSupport.typedEnvironment()
            );
        }
    }

    @State(Scope.Thread)
    public static class TypedMethodNoArgState {
        private MathExpression expression;
        private int index;

        @Setup(Level.Trial)
        public void setUp() {
            expression = MathExpression.compile(
                    ObjectNavigationBenchmarkSupport.METHOD_NO_ARG_EXPRESSION,
                    ObjectNavigationBenchmarkSupport.typedEnvironment()
            );
        }
    }

    @State(Scope.Thread)
    public static class TypedMethodWithArgumentState {
        private MathExpression expression;
        private int index;

        @Setup(Level.Trial)
        public void setUp() {
            expression = MathExpression.compile(
                    ObjectNavigationBenchmarkSupport.METHOD_WITH_ARG_EXPRESSION,
                    ObjectNavigationBenchmarkSupport.typedEnvironment()
            );
        }
    }

    @State(Scope.Thread)
    public static class ReflectiveNestedPropertyState {
        private LogicalExpression expression;
        private int index;

        @Setup(Level.Trial)
        public void setUp() {
            expression = LogicalExpression.compile(
                    ObjectNavigationBenchmarkSupport.NESTED_PROPERTY_EXPRESSION,
                    ObjectNavigationBenchmarkSupport.reflectiveEnvironment()
            );
        }
    }

    @State(Scope.Thread)
    public static class ReflectiveMethodWithArgumentState {
        private MathExpression expression;
        private int index;

        @Setup(Level.Trial)
        public void setUp() {
            expression = MathExpression.compile(
                    ObjectNavigationBenchmarkSupport.METHOD_WITH_ARG_EXPRESSION,
                    ObjectNavigationBenchmarkSupport.reflectiveEnvironment()
            );
        }
    }

    @State(Scope.Thread)
    public static class BuildTypedEnvironmentState {
    }

    @State(Scope.Thread)
    public static class CompileTypedNestedPropertyState {
        private ExpressionEnvironment environment;
        private ExpressionCompiler compiler;

        @Setup(Level.Trial)
        public void setUpEnvironment() {
            environment = ObjectNavigationBenchmarkSupport.typedEnvironment();
        }

        @Setup(Level.Invocation)
        public void setUpInvocation() {
            compiler = new ExpressionCompiler();
        }
    }

    @State(Scope.Thread)
    public static class CompileTypedMethodWithArgumentState {
        private ExpressionEnvironment environment;
        private ExpressionCompiler compiler;

        @Setup(Level.Trial)
        public void setUpEnvironment() {
            environment = ObjectNavigationBenchmarkSupport.typedEnvironment();
        }

        @Setup(Level.Invocation)
        public void setUpInvocation() {
            compiler = new ExpressionCompiler();
        }
    }

    @State(Scope.Thread)
    public static class CompileReflectiveMethodWithArgumentState {
        private ExpressionEnvironment environment;
        private ExpressionCompiler compiler;

        @Setup(Level.Trial)
        public void setUpEnvironment() {
            environment = ObjectNavigationBenchmarkSupport.reflectiveEnvironment();
        }

        @Setup(Level.Invocation)
        public void setUpInvocation() {
            compiler = new ExpressionCompiler();
        }
    }
}
