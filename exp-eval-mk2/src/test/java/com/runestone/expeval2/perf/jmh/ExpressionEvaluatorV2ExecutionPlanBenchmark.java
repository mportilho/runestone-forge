package com.runestone.expeval2.perf.jmh;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.environment.ExpressionEnvironmentBuilder;
import com.runestone.expeval2.internal.grammar.ExpressionResultType;
import com.runestone.expeval2.internal.runtime.ExpressionCompiler;
import com.runestone.expeval2.internal.runtime.ExpressionRuntimeSupport;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ExpressionEvaluatorV2ExecutionPlanBenchmark {

    private static final String MATH_LITERAL_DENSE_EXPRESSION = buildMathLiteralDenseExpression();
    private static final String LOGICAL_MIXED_LITERAL_EXPRESSION = buildLogicalMixedLiteralExpression();

    @Benchmark
    public Object compileMathLiteralDense(CompileState state) {
        return new ExpressionCompiler().compile(
            MATH_LITERAL_DENSE_EXPRESSION,
            ExpressionResultType.MATH,
            state.environment
        );
    }

    @Benchmark
    public Object compileLogicalMixedLiteralDense(CompileState state) {
        return new ExpressionCompiler().compile(
            LOGICAL_MIXED_LITERAL_EXPRESSION,
            ExpressionResultType.LOGICAL,
            state.environment
        );
    }

    @Benchmark
    public Object computeMathLiteralDense(ComputeState state) {
        return state.mathRuntime.computeMath(Map.of());
    }

    @Benchmark
    public boolean computeLogicalMixedLiteralDense(ComputeState state) {
        return state.logicalRuntime.computeLogical(Map.of());
    }

    @State(Scope.Benchmark)
    public static class CompileState {

        private ExpressionEnvironment environment;

        @Setup(Level.Trial)
        public void setUp() {
            environment = ExpressionEnvironmentBuilder.empty();
        }
    }

    @State(Scope.Benchmark)
    public static class ComputeState {

        private ExpressionRuntimeSupport mathRuntime;
        private ExpressionRuntimeSupport logicalRuntime;

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = ExpressionEnvironmentBuilder.empty();
            mathRuntime = ExpressionRuntimeSupport.compileMath(MATH_LITERAL_DENSE_EXPRESSION, environment);
            logicalRuntime = ExpressionRuntimeSupport.compileLogical(LOGICAL_MIXED_LITERAL_EXPRESSION, environment);
        }
    }

    private static String buildMathLiteralDenseExpression() {
        StringJoiner joiner = new StringJoiner(" + ");
        for (int index = 1; index <= 64; index++) {
            joiner.add("100%d.%04d".formatted(index, index));
        }
        return joiner.toString();
    }

    private static String buildLogicalMixedLiteralExpression() {
        return String.join(
            " and ",
            "2024-12-31 > 2024-01-01",
            "12:30 < 13:45",
            "2024-12-31T12:30 > 2024-12-30T08:15",
            "\"zeta\" > \"alpha\"",
            "2025-01-15 >= 2025-01-14",
            "16:45 > 06:30",
            "2025-01-15T23:59 > 2025-01-15T00:00",
            "\"runestone\" != \"forge\"",
            "42 > 7",
            "true"
        );
    }
}
