package com.runestone.expeval.perf.jmh;

import com.runestone.expeval.expression.calculator.CalculationMemory;
import com.runestone.expeval.expression.calculator.Calculator;
import com.runestone.expeval.expression.calculator.CalculatorInput;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class CalculatorContextPropagationBenchmark {

    private Calculator calculator;
    private List<CalculatorInput> sparseInputs;
    private List<CalculatorInput> denseInputs;
    private Map<String, Object> sparseContext;
    private Map<String, Object> denseContext;

    @Setup(Level.Trial)
    public void setup() {
        calculator = new Calculator();
        sparseInputs = List.of(
                new CalculatorInput("v001 + 1"),
                new CalculatorInput("v777 + 2"),
                new CalculatorInput("v999 + 3"));
        denseInputs = List.of(new CalculatorInput("a + b + c + d + e + f + g + h + i + j"));

        sparseContext = new HashMap<>();
        for (int i = 0; i < 1_000; i++) {
            sparseContext.put(String.format("v%03d", i), BigDecimal.valueOf(i + 1L));
        }

        denseContext = new HashMap<>();
        for (char variable = 'a'; variable <= 'j'; variable++) {
            denseContext.put(String.valueOf(variable), BigDecimal.valueOf(variable - 'a' + 1L));
        }

        calculator.calculate(sparseInputs, sparseContext);
        calculator.calculate(denseInputs, denseContext);
    }

    @Benchmark
    public void calculatorWithLargeContextSparseUsage(Blackhole blackhole) {
        List<CalculationMemory> memory = calculator.calculate(sparseInputs, sparseContext);
        blackhole.consume(memory);
    }

    @Benchmark
    public void calculatorWithSmallContextDenseUsage(Blackhole blackhole) {
        List<CalculationMemory> memory = calculator.calculate(denseInputs, denseContext);
        blackhole.consume(memory);
    }
}
