package com.runestone.expeval.perf.jmh;

import com.runestone.expeval.expression.Expression;
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

import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class DynamicFunctionOverheadBenchmark {

    private static final MethodType ADDER_TYPE = MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class);

    private Expression nativeConstantExpression;
    private Expression dynamicConstantExpression;
    private Expression nativeVariableExpression;
    private Expression dynamicVariableExpression;
    private Expression dynamicFunctionInSequenceExpression;

    private long sequence;

    @Setup(Level.Trial)
    public void setup() {
        nativeConstantExpression = new Expression("1 + 2");
        dynamicConstantExpression = buildDynamicAddExpression("$.add(1, 2)");

        nativeVariableExpression = new Expression("a + b");
        nativeVariableExpression.setVariable("a", BigDecimal.ONE);
        nativeVariableExpression.setVariable("b", BigDecimal.TEN);

        dynamicVariableExpression = buildDynamicAddExpression("$.add(a, b)");
        dynamicVariableExpression.setVariable("a", BigDecimal.ONE);
        dynamicVariableExpression.setVariable("b", BigDecimal.TEN);

        dynamicFunctionInSequenceExpression = buildDynamicAddExpression("S[[1,2,3,4,5,6,7,8,9,10]]($.add(S, 1))");
    }

    @Benchmark
    public void nativeConstantMath(Blackhole blackhole) {
        blackhole.consume(nativeConstantExpression.evaluate());
    }

    @Benchmark
    public void dynamicConstantFunction(Blackhole blackhole) {
        blackhole.consume(dynamicConstantExpression.evaluate());
    }

    @Benchmark
    public void nativeVariableMath(Blackhole blackhole) {
        long value = ++sequence;
        nativeVariableExpression.setVariable("a", BigDecimal.valueOf(value));
        nativeVariableExpression.setVariable("b", BigDecimal.valueOf(value + 1));
        blackhole.consume(nativeVariableExpression.evaluate());
    }

    @Benchmark
    public void dynamicVariableFunction(Blackhole blackhole) {
        long value = ++sequence;
        dynamicVariableExpression.setVariable("a", BigDecimal.valueOf(value));
        dynamicVariableExpression.setVariable("b", BigDecimal.valueOf(value + 1));
        blackhole.consume(dynamicVariableExpression.evaluate());
    }

    @Benchmark
    public void dynamicFunctionInsideSequence(Blackhole blackhole) {
        blackhole.consume(dynamicFunctionInSequenceExpression.evaluate());
    }

    private static Expression buildDynamicAddExpression(String expressionText) {
        Expression expression = new Expression(expressionText);
        expression.addFunction("add", ADDER_TYPE, parameters ->
                ((BigDecimal) parameters[0]).add((BigDecimal) parameters[1]));
        return expression;
    }
}
