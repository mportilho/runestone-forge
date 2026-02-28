package com.runestone.expeval.perf.jmh;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval.expression.ExpressionOptions;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class GlobalCacheParsingBenchmark {

    private String expression;
    private ExpressionOptions options;

    @Setup(Level.Trial)
    public void setup() {
        this.expression = "a + b * (c - d) / 2";
        this.options = ExpressionOptions.defaultOptions();
    }

    @Benchmark
    public Object testNewEvaluatorInstantiationAndParsing() {
        Expression expr = new Expression(expression, options);
        // We trigger parsing by evaluating the expression
        try {
            return expr.evaluate();
        } catch (Exception e) {
            // Expected since variables a, b, c, d are not set, but parsing will have
            // occurred
            return null;
        }
    }
}
