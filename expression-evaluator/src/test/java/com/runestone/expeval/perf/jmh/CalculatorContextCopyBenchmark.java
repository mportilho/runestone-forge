package com.runestone.expeval.perf.jmh;

import com.runestone.expeval.expression.calculator.CalculationMemory;
import com.runestone.expeval.expression.calculator.Calculator;
import com.runestone.expeval.expression.calculator.CalculatorInput;
import com.runestone.expeval.expression.calculator.CalculatorMemoryMode;
import com.runestone.expeval.expression.calculator.CalculatorOptions;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class CalculatorContextCopyBenchmark {

    private static final CalculatorOptions FULL_OPTIONS = new CalculatorOptions(CalculatorMemoryMode.FULL, 64);

    private Calculator calculator;
    private List<CalculatorInput> noAssignmentInputs;
    private List<CalculatorInput> sparseAssignmentInputs;
    private Map<String, Object> largeContext;

    @Setup(Level.Trial)
    public void setup() {
        calculator = new Calculator();
        noAssignmentInputs = buildNoAssignmentInputs(32);
        sparseAssignmentInputs = buildSparseAssignmentInputs(32);
        largeContext = buildLargeContext(2_000);

        calculator.calculate(noAssignmentInputs, largeContext, FULL_OPTIONS);
        calculator.calculate(sparseAssignmentInputs, largeContext, FULL_OPTIONS);
    }

    @Benchmark
    public void fullModeLargeContextNoAssignments(Blackhole blackhole) {
        List<CalculationMemory> memory = calculator.calculate(noAssignmentInputs, largeContext, FULL_OPTIONS);
        blackhole.consume(memory);
    }

    @Benchmark
    public void fullModeLargeContextSparseAssignments(Blackhole blackhole) {
        List<CalculationMemory> memory = calculator.calculate(sparseAssignmentInputs, largeContext, FULL_OPTIONS);
        blackhole.consume(memory);
    }

    private static List<CalculatorInput> buildNoAssignmentInputs(int count) {
        List<CalculatorInput> inputs = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int left = i % 100;
            int right = (i + 13) % 100;
            inputs.add(new CalculatorInput("v" + left + " + v" + right));
        }
        return inputs;
    }

    private static List<CalculatorInput> buildSparseAssignmentInputs(int count) {
        List<CalculatorInput> inputs = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int left = i % 100;
            int right = (i + 17) % 100;
            if (i % 8 == 0) {
                inputs.add(new CalculatorInput("tmp" + i + " := v" + left + " + v" + right + ";"));
            } else {
                inputs.add(new CalculatorInput("v" + left + " + v" + right));
            }
        }
        return inputs;
    }

    private static Map<String, Object> buildLargeContext(int size) {
        Map<String, Object> context = new HashMap<>(size * 2);
        for (int i = 0; i < size; i++) {
            context.put("v" + i, BigDecimal.valueOf(i + 1L));
        }
        return context;
    }
}
