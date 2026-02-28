package com.runestone.expeval.perf.jmh;

import com.runestone.expeval.expression.Expression;
import org.junit.jupiter.api.Test;
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

/**
 * Benchmarks targeting Section 3.1 of the performance analysis:
 * "Contextos de Avaliação e Invocação".
 *
 * <p>Measures allocation overhead from three sources per evaluation cycle:
 * <ol>
 *   <li>New {@code OperationContext} (record + CurrentDateTimeSupplier) on every evaluate()</li>
 *   <li>New {@code CallSiteContext} in FunctionOperation (identity cache always misses)</li>
 *   <li>New {@code Object[]} parameter array on every function invocation</li>
 * </ol>
 *
 * <p>Scenarios:
 * <ul>
 *   <li>{@code noFunction} – baseline: no functions, only OperationContext allocation</li>
 *   <li>{@code singleFunction_2params} – 1 function call: adds CallSiteContext + Object[2]</li>
 *   <li>{@code twoFunctions_2params} – 2 function calls: doubles CallSiteContext + Object[2]</li>
 *   <li>{@code singleFunction_6params} – 1 function, 6 params: larger Object[] allocation</li>
 * </ul>
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class ContextAllocationReuseBenchmark {

    private static final MethodType ADDER_2_TYPE = MethodType.methodType(BigDecimal.class, BigDecimal.class, BigDecimal.class);
    private static final MethodType ADDER_6_TYPE = MethodType.methodType(BigDecimal.class,
            BigDecimal.class, BigDecimal.class, BigDecimal.class,
            BigDecimal.class, BigDecimal.class, BigDecimal.class);

    // Baseline: no function calls — only OperationContext + CurrentDateTimeSupplier allocation
    private Expression noFunctionExpr;

    // 1 function call (2 params): OperationContext + CallSiteContext + Object[2]
    private Expression singleFunction2ParamsExpr;

    // 2 function calls (2 params each): OperationContext + 2xCallSiteContext + 2xObject[2]
    private Expression twoFunctions2ParamsExpr;

    // 1 function call (6 params): OperationContext + CallSiteContext + Object[6]
    private Expression singleFunction6ParamsExpr;

    private long sequence;

    @Setup(Level.Trial)
    public void setup() {
        // Baseline: simple arithmetic, no function overhead
        noFunctionExpr = new Expression("a + b");
        noFunctionExpr.setVariable("a", BigDecimal.ONE);
        noFunctionExpr.setVariable("b", BigDecimal.TEN);

        // Single binary function
        singleFunction2ParamsExpr = new Expression("$.add2(a, b)");
        singleFunction2ParamsExpr.addFunction("add2", ADDER_2_TYPE, params ->
                ((BigDecimal) params[0]).add((BigDecimal) params[1]));
        singleFunction2ParamsExpr.setVariable("a", BigDecimal.ONE);
        singleFunction2ParamsExpr.setVariable("b", BigDecimal.TEN);

        // Two independent binary functions combined
        twoFunctions2ParamsExpr = new Expression("$.add2(a, b) + $.add2(c, d)");
        twoFunctions2ParamsExpr.addFunction("add2", ADDER_2_TYPE, params ->
                ((BigDecimal) params[0]).add((BigDecimal) params[1]));
        twoFunctions2ParamsExpr.setVariable("a", BigDecimal.ONE);
        twoFunctions2ParamsExpr.setVariable("b", BigDecimal.TEN);
        twoFunctions2ParamsExpr.setVariable("c", BigDecimal.valueOf(3));
        twoFunctions2ParamsExpr.setVariable("d", BigDecimal.valueOf(4));

        // Single function with 6 parameters (measures Object[] allocation by arity)
        singleFunction6ParamsExpr = new Expression("$.sum6(a, b, c, d, e, f)");
        singleFunction6ParamsExpr.addFunction("sum6", ADDER_6_TYPE, params -> {
            BigDecimal total = BigDecimal.ZERO;
            for (Object p : params) total = total.add((BigDecimal) p);
            return total;
        });
        singleFunction6ParamsExpr.setVariable("a", BigDecimal.ONE);
        singleFunction6ParamsExpr.setVariable("b", BigDecimal.valueOf(2));
        singleFunction6ParamsExpr.setVariable("c", BigDecimal.valueOf(3));
        singleFunction6ParamsExpr.setVariable("d", BigDecimal.valueOf(4));
        singleFunction6ParamsExpr.setVariable("e", BigDecimal.valueOf(5));
        singleFunction6ParamsExpr.setVariable("f", BigDecimal.valueOf(6));
    }

    /**
     * Baseline: measures the cost of OperationContext + CurrentDateTimeSupplier allocation only.
     * No functions → no CallSiteContext or Object[] allocation.
     */
    @Benchmark
    public void noFunction(Blackhole blackhole) {
        long v = ++sequence;
        noFunctionExpr.setVariable("a", BigDecimal.valueOf(v));
        noFunctionExpr.setVariable("b", BigDecimal.valueOf(v + 1));
        blackhole.consume(noFunctionExpr.evaluate());
    }

    /**
     * One function, 2 params: adds CallSiteContext (1x) + Object[2] (1x) to the baseline.
     */
    @Benchmark
    public void singleFunction_2params(Blackhole blackhole) {
        long v = ++sequence;
        singleFunction2ParamsExpr.setVariable("a", BigDecimal.valueOf(v));
        singleFunction2ParamsExpr.setVariable("b", BigDecimal.valueOf(v + 1));
        blackhole.consume(singleFunction2ParamsExpr.evaluate());
    }

    /**
     * Two functions, 2 params each: adds 2x CallSiteContext + 2x Object[2].
     * Demonstrates cumulative allocation cost when multiple functions are present.
     */
    @Benchmark
    public void twoFunctions_2params(Blackhole blackhole) {
        long v = ++sequence;
        twoFunctions2ParamsExpr.setVariable("a", BigDecimal.valueOf(v));
        twoFunctions2ParamsExpr.setVariable("b", BigDecimal.valueOf(v + 1));
        blackhole.consume(twoFunctions2ParamsExpr.evaluate());
    }

    /**
     * One function, 6 params: isolates the effect of Object[] arity on allocation cost.
     */
    @Benchmark
    public void singleFunction_6params(Blackhole blackhole) {
        long v = ++sequence;
        singleFunction6ParamsExpr.setVariable("a", BigDecimal.valueOf(v));
        blackhole.consume(singleFunction6ParamsExpr.evaluate());
    }

    @Test
    public void runBenchmark() throws Exception {
        String[] argv = {this.getClass().getName()};
        org.openjdk.jmh.Main.main(argv);
    }
}
