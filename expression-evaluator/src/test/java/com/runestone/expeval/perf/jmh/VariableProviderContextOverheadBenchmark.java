package com.runestone.expeval.perf.jmh;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval.operation.AbstractOperation;
import com.runestone.expeval.operation.values.AbstractVariableValueOperation;
import com.runestone.expeval.tools.OperationCollectorVisitor;
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

import java.util.Set;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class VariableProviderContextOverheadBenchmark {

    private Expression expression;

    @Setup(Level.Trial)
    public void setup() {
        expression = new Expression("a = b and b = c and c = d");
        expression.setVariableProvider("a", context -> context.currentDateTime().get());
        expression.setVariableProvider("b", context -> context.currentDateTime().get());
        expression.setVariableProvider("c", context -> context.currentDateTime().get());
        expression.setVariableProvider("d", context -> context.currentDateTime().get());

        disableVariableCaching(expression);
    }

    @Benchmark
    public void variableProvidersUsingCurrentDateTime(Blackhole blackhole) {
        blackhole.consume(expression.evaluate());
    }

    private static void disableVariableCaching(Expression expression) {
        Set<AbstractOperation> operations = expression.visitOperations(new OperationCollectorVisitor());
        for (AbstractOperation operation : operations) {
            if (operation instanceof AbstractVariableValueOperation variableOperation) {
                variableOperation.configureCaching(false);
            }
        }
    }
}
