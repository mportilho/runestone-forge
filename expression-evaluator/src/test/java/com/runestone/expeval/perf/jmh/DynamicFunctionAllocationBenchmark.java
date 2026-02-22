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
public class DynamicFunctionAllocationBenchmark {

    private static final MethodType ADDER_TYPE = MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class);
    private static final MethodType ARRAY_SUM_TYPE = MethodType.methodType(BigDecimal.class, BigDecimal[].class);

    private Expression nativeVariableExpression;
    private Expression dynamicFixedArityExpression;
    private Expression dynamicVarArgFallbackExpression;

    private long sequence;

    @Setup(Level.Trial)
    public void setup() {
        nativeVariableExpression = new Expression("a + b");
        nativeVariableExpression.setVariable("a", BigDecimal.ONE);
        nativeVariableExpression.setVariable("b", BigDecimal.TEN);

        dynamicFixedArityExpression = new Expression("$.add(a, b)");
        dynamicFixedArityExpression.addFunction("add", ADDER_TYPE, parameters ->
                ((BigDecimal) parameters[0]).add((BigDecimal) parameters[1]));
        dynamicFixedArityExpression.setVariable("a", BigDecimal.ONE);
        dynamicFixedArityExpression.setVariable("b", BigDecimal.TEN);

        dynamicVarArgFallbackExpression = new Expression("$.sumArr(a, b, c, d, e, f, g, h)");
        dynamicVarArgFallbackExpression.addFunction("sumArr", ARRAY_SUM_TYPE, parameters -> {
            BigDecimal total = BigDecimal.ZERO;
            BigDecimal[] values = (BigDecimal[]) parameters[0];
            for (BigDecimal value : values) {
                total = total.add(value);
            }
            return total;
        });
        dynamicVarArgFallbackExpression.setVariable("a", BigDecimal.ONE);
        dynamicVarArgFallbackExpression.setVariable("b", BigDecimal.TEN);
        dynamicVarArgFallbackExpression.setVariable("c", BigDecimal.valueOf(3));
        dynamicVarArgFallbackExpression.setVariable("d", BigDecimal.valueOf(4));
        dynamicVarArgFallbackExpression.setVariable("e", BigDecimal.valueOf(5));
        dynamicVarArgFallbackExpression.setVariable("f", BigDecimal.valueOf(6));
        dynamicVarArgFallbackExpression.setVariable("g", BigDecimal.valueOf(7));
        dynamicVarArgFallbackExpression.setVariable("h", BigDecimal.valueOf(8));
    }

    @Benchmark
    public void nativeVariableMath(Blackhole blackhole) {
        long value = ++sequence;
        nativeVariableExpression.setVariable("a", BigDecimal.valueOf(value));
        nativeVariableExpression.setVariable("b", BigDecimal.valueOf(value + 1));
        blackhole.consume(nativeVariableExpression.evaluate());
    }

    @Benchmark
    public void dynamicFixedArityFunction(Blackhole blackhole) {
        long value = ++sequence;
        dynamicFixedArityExpression.setVariable("a", BigDecimal.valueOf(value));
        dynamicFixedArityExpression.setVariable("b", BigDecimal.valueOf(value + 1));
        blackhole.consume(dynamicFixedArityExpression.evaluate());
    }

    @Benchmark
    public void dynamicVarArgFallbackFunction(Blackhole blackhole) {
        long value = ++sequence;
        dynamicVarArgFallbackExpression.setVariable("a", BigDecimal.valueOf(value));
        dynamicVarArgFallbackExpression.setVariable("b", BigDecimal.valueOf(value + 1));
        blackhole.consume(dynamicVarArgFallbackExpression.evaluate());
    }
}
