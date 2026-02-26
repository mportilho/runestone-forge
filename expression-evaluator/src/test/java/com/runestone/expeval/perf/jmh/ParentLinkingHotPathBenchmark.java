package com.runestone.expeval.perf.jmh;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval.operation.AbstractOperation;
import com.runestone.expeval.operation.CloningContext;
import com.runestone.expeval.operation.OperationContext;
import com.runestone.expeval.operation.OperationVisitor;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
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
public class ParentLinkingHotPathBenchmark {

    @Param({"64", "256", "1024"})
    private int parentCount;

    private NoOpOperation[] parentPool;
    private String repeatedVariableExpression;

    @Setup(Level.Trial)
    public void setup() {
        parentPool = new NoOpOperation[parentCount];
        for (int i = 0; i < parentCount; i++) {
            parentPool[i] = new NoOpOperation();
        }
        repeatedVariableExpression = buildRepeatedAdditionExpression("a", parentCount);
    }

    @Benchmark
    public void addParentBurst(Blackhole blackhole) {
        NoOpOperation child = new NoOpOperation();
        for (NoOpOperation parent : parentPool) {
            child.addParent(parent);
        }
        blackhole.consume(child);
    }

    @Benchmark
    public void parseWarmUpAndEvaluateRepeatedVariableExpression(Blackhole blackhole) {
        Expression expression = new Expression(repeatedVariableExpression);
        expression.setVariable("a", BigDecimal.ONE);
        blackhole.consume(expression.warmUp().evaluate());
    }

    private static String buildRepeatedAdditionExpression(String variableName, int occurrences) {
        StringBuilder builder = new StringBuilder(variableName);
        for (int i = 1; i < occurrences; i++) {
            builder.append(" + ").append(variableName);
        }
        return builder.toString();
    }

    private static final class NoOpOperation extends AbstractOperation {

        @Override
        protected Object resolve(OperationContext context) {
            return null;
        }

        @Override
        protected AbstractOperation createClone(CloningContext context) {
            return new NoOpOperation();
        }

        @Override
        public void accept(OperationVisitor<?> visitor) {
            throw new UnsupportedOperationException("Not used in benchmark");
        }

        @Override
        protected void formatRepresentation(StringBuilder builder) {
            builder.append("noop");
        }

        @Override
        protected String getOperationToken() {
            return "";
        }
    }
}
