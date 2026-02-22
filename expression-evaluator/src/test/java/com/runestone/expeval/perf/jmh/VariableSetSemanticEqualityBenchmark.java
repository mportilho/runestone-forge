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

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class VariableSetSemanticEqualityBenchmark {

    private static final BigDecimal ONE_SCALE_1 = new BigDecimal("1.0");
    private static final BigDecimal ONE_SCALE_2 = new BigDecimal("1.00");
    private static final BigDecimal TWO_SCALE_1 = new BigDecimal("2.0");

    private Expression equivalentScaleExpression;
    private Expression nonEquivalentExpression;
    private boolean equivalentToggle;
    private boolean nonEquivalentToggle;

    @Setup(Level.Trial)
    public void setup() {
        equivalentScaleExpression = new Expression("a + 1");
        equivalentScaleExpression.setVariable("a", ONE_SCALE_1);
        equivalentScaleExpression.evaluate();

        nonEquivalentExpression = new Expression("a + 1");
        nonEquivalentExpression.setVariable("a", ONE_SCALE_1);
        nonEquivalentExpression.evaluate();
    }

    @Benchmark
    public void setVariableWithEquivalentComparableValue(Blackhole blackhole) {
        equivalentToggle = !equivalentToggle;
        equivalentScaleExpression.setVariable("a", equivalentToggle ? ONE_SCALE_1 : ONE_SCALE_2);
        blackhole.consume(equivalentScaleExpression.evaluate());
    }

    @Benchmark
    public void setVariableWithDifferentComparableValue(Blackhole blackhole) {
        nonEquivalentToggle = !nonEquivalentToggle;
        nonEquivalentExpression.setVariable("a", nonEquivalentToggle ? ONE_SCALE_1 : TWO_SCALE_1);
        blackhole.consume(nonEquivalentExpression.evaluate());
    }
}
