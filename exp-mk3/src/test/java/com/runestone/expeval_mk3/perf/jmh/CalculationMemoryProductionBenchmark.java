package com.runestone.expeval_mk3.perf.jmh;

import com.runestone.expeval_mk3.api.CalculationEntry;
import com.runestone.expeval_mk3.api.CalculationKey;
import com.runestone.expeval_mk3.api.CalculationMemory;
import com.runestone.expeval_mk3.api.ComputationWithMemory;
import com.runestone.expeval_mk3.api.ExpressionEngine;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.FunctionPurity;
import com.runestone.expeval_mk3.api.ResultExpression;
import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.api.VariableEntry;
import com.runestone.expeval_mk3.api.VariableKey;
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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Paired production-path gates for calculation-memory capture, publication, and complete consumption. */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class CalculationMemoryProductionBenchmark {

    /** Control arm: executes the marked production plan with calculation capture inactive. */
    @Benchmark
    public Object compute(ProductionState state) {
        return state.expression.compute();
    }

    /** Includes execution, the existing single public materialization, capture, freeze, and envelope. */
    @Benchmark
    public ComputationWithMemory<Object> computeWithMemory(ProductionState state) {
        return state.expression.computeWithMemory();
    }

    /** Complete production flow followed by the allocation-free indexed persistence path. */
    @Benchmark
    public long computeWithMemoryAndConsumeIndexed(ProductionState state) {
        ComputationWithMemory<Object> computation = state.expression.computeWithMemory();
        return consumeValue(1, computation.result()) * 31 + consumeIndexed(computation.memory());
    }

    /** Isolates indexed traversal from execution, capture, public materialization, and freeze. */
    @Benchmark
    public long traverseIndexed(ProductionState state) {
        return consumeIndexed(state.memory);
    }

    /** Isolates allocation of the two immutable convenience projections. */
    @Benchmark
    public void createListProjections(ProductionState state, Blackhole sink) {
        sink.consume(state.memory.variables());
        sink.consume(state.memory.calculations());
    }

    /** Measures projection and transient entry records without an iterator. */
    @Benchmark
    public void traverseListsByIndex(ProductionState state, Blackhole sink) {
        List<VariableEntry> variables = state.memory.variables();
        for (int index = 0; index < variables.size(); index++) {
            VariableEntry entry = variables.get(index);
            sink.consume(entry);
            consume(entry.key(), entry.value(), sink);
        }
        List<CalculationEntry> calculations = state.memory.calculations();
        for (int index = 0; index < calculations.size(); index++) {
            CalculationEntry entry = calculations.get(index);
            sink.consume(entry);
            consume(entry.key(), entry.value(), sink);
        }
    }

    /** Measures projections, AbstractList iterators, and transient entry records together. */
    @Benchmark
    public void traverseListsWithIterators(ProductionState state, Blackhole sink) {
        Iterator<VariableEntry> variables = state.memory.variables().iterator();
        while (variables.hasNext()) {
            VariableEntry entry = variables.next();
            sink.consume(entry);
            consume(entry.key(), entry.value(), sink);
        }
        Iterator<CalculationEntry> calculations = state.memory.calculations().iterator();
        while (calculations.hasNext()) {
            CalculationEntry entry = calculations.next();
            sink.consume(entry);
            consume(entry.key(), entry.value(), sink);
        }
    }

    @State(Scope.Benchmark)
    public static class ProductionState {

        @Param
        private Scenario scenario;
        private ResultExpression expression;
        private CalculationMemory memory;

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = ExpressionEnvironment.builder()
                    .externalSymbol("amount", BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                    .externalSymbol("enabled", false, ExternalSymbolOverwritePolicy.FIXED)
                    .externalSymbol("values", List.of(
                            BigDecimal.ONE, BigDecimal.TWO, BigDecimal.valueOf(3), BigDecimal.valueOf(4),
                            BigDecimal.valueOf(5), BigDecimal.valueOf(6), BigDecimal.valueOf(7), BigDecimal.valueOf(8)),
                            ExternalSymbolOverwritePolicy.FIXED)
                    .functionsFrom(MarkingFunctions.class, FunctionPurity.IMPURE)
                    .build();
            expression = ExpressionEngine.defaultEngine().compileOrThrow(scenario.source, environment).asResult();
            memory = expression.computeWithMemory().memory();
            if (memory.calculationCount() != scenario.reachedPoints) {
                throw new IllegalStateException("scenario " + scenario + " reached "
                        + memory.calculationCount() + " points instead of " + scenario.reachedPoints);
            }
            if (scenario == Scenario.OPAQUE_REPEATED && !expression.compute().equals(List.of(
                    BigDecimal.TWO, BigDecimal.valueOf(3), BigDecimal.valueOf(4), BigDecimal.valueOf(5),
                    BigDecimal.valueOf(6), BigDecimal.valueOf(7), BigDecimal.valueOf(8), BigDecimal.valueOf(9)))) {
                throw new IllegalStateException("opaque descendants were not all executed");
            }
        }
    }

    public enum Scenario {
        EMPTY("amount + 1", 0),
        DENSE("mark(amount) + mark(amount + 1) + mark(amount + 2) + mark(amount + 3)", 4),
        PREFIX("markBoolean(enabled) and markBoolean(enabled) and markBoolean(enabled) and markBoolean(enabled)", 1),
        ALTERNATING("if enabled then mark(amount) else mark(amount + 1) endif"
                + " + if enabled then mark(amount + 2) else mark(amount + 3) endif"
                + " + if enabled then mark(amount + 4) else mark(amount + 5) endif"
                + " + if enabled then mark(amount + 6) else mark(amount + 7) endif", 4),
        SPARSE("if enabled then mark(amount) + mark(amount + 1) + mark(amount + 2)"
                + " + mark(amount + 3) + mark(amount + 4) + mark(amount + 5) + mark(amount + 6)"
                + " else mark(amount + 7) endif", 1),
        OPAQUE_REPEATED("values.map(@ -> opaqueMark(@))", 0);

        private final String source;
        private final int reachedPoints;

        Scenario(String source, int reachedPoints) {
            this.source = source;
            this.reachedPoints = reachedPoints;
        }
    }

    public static final class MarkingFunctions {

        private MarkingFunctions() {
        }

        public static BigDecimal mark(BigDecimal value) {
            return value;
        }

        public static boolean markBoolean(boolean value) {
            return value;
        }

        public static BigDecimal opaqueMark(BigDecimal value) {
            return value.add(BigDecimal.ONE);
        }
    }

    private static long consumeIndexed(CalculationMemory memory) {
        long checksum = 1;
        for (int index = 0; index < memory.variableCount(); index++) {
            checksum = consume(checksum, memory.variableKeyAt(index), memory.variableValueAt(index));
        }
        for (int index = 0; index < memory.calculationCount(); index++) {
            checksum = consume(checksum, memory.calculationKeyAt(index), memory.calculationValueAt(index));
        }
        return checksum;
    }

    private static long consume(long checksum, VariableKey key, Object value) {
        checksum = checksum * 31 + key.name().hashCode();
        checksum = checksum * 31 + key.origin().ordinal();
        return consumeValue(checksum, value);
    }

    private static long consume(long checksum, CalculationKey key, Object value) {
        SourceSpan span = key.sourceSpan();
        checksum = checksum * 31 + key.nodeId();
        checksum = checksum * 31 + span.offset();
        checksum = checksum * 31 + span.endOffset();
        checksum = checksum * 31 + span.line();
        checksum = checksum * 31 + span.column();
        checksum = checksum * 31 + key.kind().ordinal();
        checksum = checksum * 31 + key.name().hashCode();
        return consumeValue(checksum, value);
    }

    private static long consumeValue(long checksum, Object value) {
        if (value instanceof List<?> list) {
            checksum = checksum * 31 + list.size();
            for (int index = 0; index < list.size(); index++) {
                checksum = consumeValue(checksum, list.get(index));
            }
            return checksum;
        }
        return checksum * 31 + (value == null ? 0 : value.hashCode());
    }

    private static void consume(VariableKey key, Object value, Blackhole sink) {
        sink.consume(key.name());
        sink.consume(key.origin().ordinal());
        sink.consume(consumeValue(1, value));
    }

    private static void consume(CalculationKey key, Object value, Blackhole sink) {
        SourceSpan span = key.sourceSpan();
        sink.consume(key.nodeId());
        sink.consume(span.offset());
        sink.consume(span.endOffset());
        sink.consume(span.line());
        sink.consume(span.column());
        sink.consume(key.kind().ordinal());
        sink.consume(key.name());
        sink.consume(consumeValue(1, value));
    }
}
