package com.runestone.expeval.perf.jmh;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval.expression.ExpressionContext;
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

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = { "-Xms1g", "-Xmx1g" })
public class MissingVariableLookupBenchmark {

    private Expression expression;
    private ExpressionContext context;

    @Setup(Level.Trial)
    public void setup() {
        context = new ExpressionContext();
        // A supplier that just returns null, to trigger the fallback lookup
        context.setVariablesSupplier(name -> null);

        // Expression with multiple missing variables to amplify the effect
        expression = new Expression("missing1 + missing2 + missing3 + missing4", context);
    }

    @Benchmark
    public void missingVariablesLookup(Blackhole blackhole) {
        try {
            blackhole.consume(expression.evaluate());
        } catch (Exception e) {
            // Expected failure as variables are null
        }
    }

    @Test
    public void runBenchmark() throws Exception {
        String[] argv = { this.getClass().getName() };
        org.openjdk.jmh.Main.main(argv);
    }
}
