package com.runestone.expeval_mk3.perf.jmh;

import com.runestone.expeval_mk3.api.CalculationMemory;
import com.runestone.expeval_mk3.api.ComputationWithMemory;
import com.runestone.expeval_mk3.api.ExpressionEngine;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.FunctionPurity;
import com.runestone.expeval_mk3.api.ResultExpression;
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

/** Paired production-path check for the normal and calculation-memory execution modes. */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class CalculationMemoryProductionBenchmark {

    @Benchmark
    public Object compute(ProductionState state) {
        return state.expression.compute();
    }

    @Benchmark
    public Object computeWithMemory(ProductionState state, Blackhole sink) {
        ComputationWithMemory<Object> computation = state.expression.computeWithMemory();
        CalculationMemory memory = computation.memory();
        sink.consume(computation.result());
        for (int index = 0; index < memory.calculationCount(); index++) {
            sink.consume(memory.calculationKeyAt(index));
            sink.consume(memory.calculationValueAt(index));
        }
        return computation;
    }

    @State(Scope.Benchmark)
    public static class ProductionState {

        @Param
        private Scenario scenario;
        private ResultExpression expression;

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = ExpressionEnvironment.builder()
                    .externalSymbol("amount", BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                    .externalSymbol("enabled", false, ExternalSymbolOverwritePolicy.FIXED)
                    .functionsFrom(MarkingFunctions.class, FunctionPurity.IMPURE)
                    .build();
            expression = ExpressionEngine.defaultEngine().compileOrThrow(scenario.source, environment).asResult();
        }
    }

    public enum Scenario {
        NO_POINTS("amount + 1"),
        ONE_POINT("mark(amount)"),
        LEADING_GAP("if enabled then mark(amount) else mark(amount + 1) endif");

        private final String source;

        Scenario(String source) {
            this.source = source;
        }
    }

    public static final class MarkingFunctions {

        private MarkingFunctions() {
        }

        public static BigDecimal mark(BigDecimal value) {
            return value;
        }
    }
}
