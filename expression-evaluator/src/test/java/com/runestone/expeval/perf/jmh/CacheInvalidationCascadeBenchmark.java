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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class CacheInvalidationCascadeBenchmark {

    private Expression sequenceExpression;
    private Expression manyVarsExpressionForMap;
    private Expression manyVarsExpressionForSingleSet;
    private Map<String, Object> variableBatch;
    private long sequence;

    @Setup(Level.Trial)
    public void setup() {
        sequenceExpression = new Expression("S[[1,2,3,4,5,6,7,8,9,10,11,12]](S + x)");
        sequenceExpression.setVariable("x", BigDecimal.ONE);

        String plainExpression = "a + b + c + d + e + f + g + h + i + j";
        manyVarsExpressionForMap = new Expression(plainExpression);
        manyVarsExpressionForSingleSet = new Expression(plainExpression);

        variableBatch = new HashMap<>();
        for (char var = 'a'; var <= 'j'; var++) {
            String variableName = String.valueOf(var);
            variableBatch.put(variableName, BigDecimal.ONE);
            manyVarsExpressionForMap.setVariable(variableName, BigDecimal.ONE);
            manyVarsExpressionForSingleSet.setVariable(variableName, BigDecimal.ONE);
        }
    }

    @Benchmark
    public void sequenceWithVariableMutation(Blackhole blackhole) {
        long value = ++sequence;
        sequenceExpression.setVariable("x", BigDecimal.valueOf(value));
        blackhole.consume(sequenceExpression.evaluate());
    }

    @Benchmark
    public void manyVariablesWithMapBatchSet(Blackhole blackhole) {
        long value = ++sequence;
        for (char var = 'a'; var <= 'j'; var++) {
            variableBatch.put(String.valueOf(var), BigDecimal.valueOf(value + var));
        }
        manyVarsExpressionForMap.setVariables(variableBatch);
        blackhole.consume(manyVarsExpressionForMap.evaluate());
    }

    @Benchmark
    public void manyVariablesWithRepeatedSetVariable(Blackhole blackhole) {
        long value = ++sequence;
        for (char var = 'a'; var <= 'j'; var++) {
            manyVarsExpressionForSingleSet.setVariable(String.valueOf(var), BigDecimal.valueOf(value + var));
        }
        blackhole.consume(manyVarsExpressionForSingleSet.evaluate());
    }
}
