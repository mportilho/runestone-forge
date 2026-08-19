package com.runestone.expeval_mk3.perf.jmh;

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
import java.time.LocalDateTime;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Binding Etapa 10 prototype. It models the persistable contract without using production runtime
 * classes, so its verdict can authorize or reopen the production representation before it is built.
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class CalculationMemoryStoragePrototypeBenchmark {

    @Benchmark
    public FrameCapture captureFrameTail(BindingState state) {
        return state.captureFrameTail();
    }

    @Benchmark
    public AppendCapture captureAppend(BindingState state) {
        return state.captureAppend();
    }

    @Benchmark
    public Object materializePublicResult(BindingState state) {
        return state.materializePublicResult();
    }

    @Benchmark
    public ColumnarMemory freezeFrameTail(BindingState state) {
        return state.freezeFrameTail(state.frameCapture, state.fullSchema);
    }

    @Benchmark
    public ColumnarMemory freezeAppend(BindingState state) {
        return state.freezeAppend(state.appendCapture, state.fullSchema);
    }

    @Benchmark
    public EagerMemory freezeEagerEntries(BindingState state) {
        return state.freezeEager(state.frameCapture, state.fullSchema);
    }

    @Benchmark
    public Computation computeWithMemoryFrameColumnar(BindingState state) {
        FrameCapture capture = state.captureFrameTail();
        Object result = state.materializePublicResult();
        return new Computation(result, state.freezeFrameTail(capture, state.fullSchema));
    }

    @Benchmark
    public Computation computeWithMemoryAppendColumnar(BindingState state) {
        AppendCapture capture = state.captureAppend();
        Object result = state.materializePublicResult();
        return new Computation(result, state.freezeAppend(capture, state.fullSchema));
    }

    @Benchmark
    public EagerComputation computeWithMemoryFrameEager(BindingState state) {
        FrameCapture capture = state.captureFrameTail();
        Object result = state.materializePublicResult();
        return new EagerComputation(result, state.freezeEager(capture, state.fullSchema));
    }

    @Benchmark
    public Computation computeWithAssignmentsSchema(BindingState state) {
        FrameCapture capture = state.captureFrameTail();
        Object result = state.materializeAssignments(capture.frame);
        return new Computation(result, state.freezeFrameTail(capture, state.assignmentsSchema));
    }

    @Benchmark
    public long traverseIndexed(BindingState state) {
        return state.columnarMemory.indexedChecksum();
    }

    @Benchmark
    public long traverseLists(BindingState state) {
        return state.columnarMemory.listChecksum();
    }

    @Benchmark
    public long traverseEagerEntries(BindingState state) {
        return state.eagerMemory.checksum();
    }

    @Benchmark
    public void consumeColumnarSequentially(BindingState state, Blackhole sink) {
        state.columnarMemory.consume(sink);
    }

    @Benchmark
    public void consumeEagerSequentially(BindingState state, Blackhole sink) {
        state.eagerMemory.consume(sink);
    }

    @Benchmark
    public void computeAndConsumeFrameColumnar(BindingState state, Blackhole sink) {
        FrameCapture capture = state.captureFrameTail();
        Computation computation = new Computation(
                state.materializePublicResult(),
                state.freezeFrameTail(capture, state.fullSchema));
        sink.consume(computation.result);
        computation.memory.consume(sink);
    }

    @Benchmark
    public void computeAndConsumeAppendColumnar(BindingState state, Blackhole sink) {
        AppendCapture capture = state.captureAppend();
        Computation computation = new Computation(
                state.materializePublicResult(),
                state.freezeAppend(capture, state.fullSchema));
        sink.consume(computation.result);
        computation.memory.consume(sink);
    }

    @Benchmark
    public void computeAndConsumeFrameEager(BindingState state, Blackhole sink) {
        FrameCapture capture = state.captureFrameTail();
        EagerComputation computation = new EagerComputation(
                state.materializePublicResult(),
                state.freezeEager(capture, state.fullSchema));
        sink.consume(computation.result);
        computation.memory.consume(sink);
    }

    @Benchmark
    public long branchSlotFirst(BranchState state) {
        return state.slotFirst();
    }

    @Benchmark
    public long branchModeFirst(BranchState state) {
        return state.modeFirst();
    }

    @Benchmark
    public long branchFusedAbsoluteSlot(BranchState state) {
        return state.fusedAbsoluteSlot();
    }

    @Benchmark
    public FrameCapture countDuringCapture(BindingState state) {
        return state.captureFrameTail();
    }

    @Benchmark
    public FrameCapture countDuringFreeze(BindingState state) {
        FrameCapture capture = state.captureFrameTailWithoutCount();
        return new FrameCapture(capture.frame, state.countReached(capture.frame));
    }

    @State(Scope.Benchmark)
    public static class BindingState {

        private static final Object UNBOUND = new Object();
        private static final Object CAPTURED_NULL = new Object();
        private static final int NORMAL_FRAME_SIZE = 24;
        private static final int[] FULL_VARIABLE_SLOTS = {0, 3, 5, 9, 12, 15, 20, 23};
        private static final int[] ASSIGNMENT_VARIABLE_SLOTS = {0, 5, 12, 20};

        @Param({"0", "1", "4", "16", "64", "256"})
        private int slotCount;

        @Param
        private Reachability reachability;

        private Object[] frameTemplate;
        private Payload[] calculationValues;
        private int[] reachedSlots;
        private StandaloneSchema fullSchema;
        private StandaloneSchema assignmentsSchema;
        private FrameCapture frameCapture;
        private AppendCapture appendCapture;
        private ColumnarMemory columnarMemory;
        private EagerMemory eagerMemory;
        private Object rawResult;

        @Setup(Level.Trial)
        public void setUp() {
            reachedSlots = reachability.slots(slotCount);
            frameTemplate = new Object[NORMAL_FRAME_SIZE];
            Arrays.fill(frameTemplate, UNBOUND);
            for (int index = 0; index < FULL_VARIABLE_SLOTS.length; index++) {
                frameTemplate[FULL_VARIABLE_SLOTS[index]] = value(index);
            }
            // Slots 1/7 model Item Atual and slots 17/19 model memo storage interleaved with variables.
            frameTemplate[1] = UNBOUND;
            frameTemplate[7] = UNBOUND;
            frameTemplate[17] = UNBOUND;
            frameTemplate[19] = UNBOUND;
            calculationValues = new Payload[slotCount];
            Arrays.setAll(calculationValues, index -> new Payload(1_000 + index));
            fullSchema = schema(FULL_VARIABLE_SLOTS, slotCount);
            assignmentsSchema = assignmentsSchema(fullSchema);
            rawResult = List.of(
                    new BigDecimal("123.45"),
                    "materialized",
                    LocalDateTime.of(2026, 8, 19, 12, 30),
                    new ArrayList<>(List.of(new BigDecimal("1.00"), new BigDecimal("2.00"))));

            frameCapture = captureFrameTail();
            appendCapture = captureAppend();
            columnarMemory = freezeFrameTail(frameCapture, fullSchema);
            ColumnarMemory appendMemory = freezeAppend(appendCapture, fullSchema);
            eagerMemory = freezeEager(frameCapture, fullSchema);
            assertEquivalent(columnarMemory, appendMemory, eagerMemory);
        }

        void configure(int slotCount, Reachability reachability) {
            this.slotCount = slotCount;
            this.reachability = reachability;
        }

        ColumnarMemory columnarMemory() {
            return columnarMemory;
        }

        EagerMemory eagerMemory() {
            return eagerMemory;
        }

        StandaloneSchema fullSchema() {
            return fullSchema;
        }

        StandaloneSchema assignmentsSchema() {
            return assignmentsSchema;
        }

        FrameCapture captureFrameTail() {
            Object[] frame = Arrays.copyOf(frameTemplate, NORMAL_FRAME_SIZE + slotCount);
            int reachedCount = 0;
            for (int slot : reachedSlots) {
                frame[NORMAL_FRAME_SIZE + slot] = capturedValue(slot);
                reachedCount++;
            }
            return new FrameCapture(frame, reachedCount);
        }

        FrameCapture captureFrameTailWithoutCount() {
            Object[] frame = Arrays.copyOf(frameTemplate, NORMAL_FRAME_SIZE + slotCount);
            for (int slot : reachedSlots) {
                frame[NORMAL_FRAME_SIZE + slot] = capturedValue(slot);
            }
            return new FrameCapture(frame, -1);
        }

        AppendCapture captureAppend() {
            Object[] frame = frameTemplate.clone();
            Object[] values = new Object[Math.max(1, Math.min(8, reachedSlots.length))];
            int[] ordinals = null;
            int count = 0;
            for (int slot : reachedSlots) {
                if (count == values.length) {
                    values = Arrays.copyOf(values, Math.min(reachedSlots.length, count + (count >> 1) + 1));
                    if (ordinals != null) {
                        ordinals = Arrays.copyOf(ordinals, values.length);
                    }
                }
                if (ordinals == null && slot != count) {
                    ordinals = new int[values.length];
                    Arrays.setAll(ordinals, index -> index);
                }
                values[count] = exposed(capturedValue(slot));
                if (ordinals != null) {
                    ordinals[count] = slot;
                }
                count++;
            }
            return new AppendCapture(frame, values, ordinals, count);
        }

        Object materializePublicResult() {
            List<?> values = (List<?>) rawResult;
            return List.of(values.get(0), values.get(1), values.get(2), List.copyOf((List<?>) values.get(3)));
        }

        Object materializeAssignments(Object[] frame) {
            Object[] values = new Object[ASSIGNMENT_VARIABLE_SLOTS.length];
            for (int index = 0; index < ASSIGNMENT_VARIABLE_SLOTS.length; index++) {
                values[index] = frame[ASSIGNMENT_VARIABLE_SLOTS[index]];
            }
            return List.of(values);
        }

        ColumnarMemory freezeFrameTail(FrameCapture capture, StandaloneSchema schema) {
            Object[] variableValues = copyVariables(capture.frame, schema.variableSlots);
            int count = capture.reachedCount >= 0 ? capture.reachedCount : countReached(capture.frame);
            Object[] calculationValues = new Object[count];
            int[] ordinals = null;
            int target = 0;
            for (int slot = 0; slot < slotCount; slot++) {
                Object value = capture.frame[NORMAL_FRAME_SIZE + slot];
                if (value == null) {
                    continue;
                }
                if (ordinals == null && slot != target) {
                    ordinals = new int[count];
                    Arrays.setAll(ordinals, index -> index);
                }
                calculationValues[target] = exposed(value);
                if (ordinals != null) {
                    ordinals[target] = slot;
                }
                target++;
            }
            return new ColumnarMemory(schema, variableValues, calculationValues, ordinals);
        }

        ColumnarMemory freezeAppend(AppendCapture capture, StandaloneSchema schema) {
            Object[] variableValues = copyVariables(capture.frame, schema.variableSlots);
            Object[] values = capture.values.length == capture.count
                    ? capture.values
                    : Arrays.copyOf(capture.values, capture.count);
            int[] ordinals = capture.ordinals == null || capture.ordinals.length == capture.count
                    ? capture.ordinals
                    : Arrays.copyOf(capture.ordinals, capture.count);
            return new ColumnarMemory(schema, variableValues, values, ordinals);
        }

        EagerMemory freezeEager(FrameCapture capture, StandaloneSchema schema) {
            VariableEntry[] variables = new VariableEntry[schema.variableSlots.length];
            for (int index = 0; index < variables.length; index++) {
                variables[index] = new VariableEntry(schema.variableKeys[index], capture.frame[schema.variableSlots[index]]);
            }
            CalculationEntry[] calculations = new CalculationEntry[capture.reachedCount];
            int target = 0;
            for (int slot = 0; slot < slotCount; slot++) {
                Object value = capture.frame[NORMAL_FRAME_SIZE + slot];
                if (value != null) {
                    calculations[target++] = new CalculationEntry(schema.calculationKeys[slot], exposed(value));
                }
            }
            return new EagerMemory(variables, calculations);
        }

        int countReached(Object[] frame) {
            int count = 0;
            for (int slot = 0; slot < slotCount; slot++) {
                if (frame[NORMAL_FRAME_SIZE + slot] != null) {
                    count++;
                }
            }
            return count;
        }

        private Object[] copyVariables(Object[] frame, int[] slots) {
            Object[] values = new Object[slots.length];
            for (int index = 0; index < slots.length; index++) {
                values[index] = frame[slots[index]];
            }
            return values;
        }

        private Object capturedValue(int slot) {
            return slot % 17 == 0 ? CAPTURED_NULL : calculationValues[slot];
        }

        private static Object exposed(Object value) {
            return value == CAPTURED_NULL ? null : value;
        }

        private static Object value(int index) {
            return switch (index % 5) {
                case 0 -> new BigDecimal(index + ".00");
                case 1 -> "value-" + index;
                case 2 -> LocalDateTime.of(2026, 8, 19, 10, index);
                case 3 -> List.of(index, index + 1);
                default -> new Payload(index);
            };
        }

        private static StandaloneSchema schema(int[] variableSlots, int calculationCount) {
            int[] ownedSlots = variableSlots.clone();
            VariableKey[] variableKeys = new VariableKey[ownedSlots.length];
            for (int index = 0; index < variableKeys.length; index++) {
                VariableOrigin origin = index < variableKeys.length / 2
                        ? VariableOrigin.EXTERNAL
                        : VariableOrigin.INTERNAL;
                variableKeys[index] = new VariableKey("v" + index, origin);
            }
            CalculationKey[] calculationKeys = new CalculationKey[calculationCount];
            for (int index = 0; index < calculationCount; index++) {
                int start = index * 3;
                SourceSpan span = new SourceSpan(start, start + 2, index + 1, 1, index + 1, 3);
                ProvenanceKey provenance = new ProvenanceKey(index + 100, span);
                CalculationKind kind = CalculationKind.values()[index % CalculationKind.values().length];
                calculationKeys[index] = new CalculationKey(provenance, kind, "point-" + index);
            }
            return new StandaloneSchema(ownedSlots, variableKeys, calculationKeys);
        }

        private static StandaloneSchema assignmentsSchema(StandaloneSchema fullSchema) {
            int[] selectedIndexes = {0, 2, 4, 6};
            VariableKey[] keys = new VariableKey[selectedIndexes.length];
            for (int index = 0; index < selectedIndexes.length; index++) {
                keys[index] = fullSchema.variableKeys[selectedIndexes[index]];
            }
            return new StandaloneSchema(
                    ASSIGNMENT_VARIABLE_SLOTS.clone(),
                    keys,
                    fullSchema.calculationKeys);
        }

        private static void assertEquivalent(
                ColumnarMemory frame,
                ColumnarMemory append,
                EagerMemory eager) {
            if (frame.variableCount() != append.variableCount()
                    || frame.variableCount() != eager.variables.length
                    || frame.calculationCount() != append.calculationCount()
                    || frame.calculationCount() != eager.calculations.length) {
                throw new IllegalStateException("binding prototype candidates produced different public payloads");
            }
            for (int index = 0; index < frame.variableCount(); index++) {
                if (!frame.variableKeyAt(index).equals(append.variableKeyAt(index))
                        || !frame.variableKeyAt(index).equals(eager.variables[index].key)
                        || !Objects.equals(frame.variableValueAt(index), append.variableValueAt(index))
                        || !Objects.equals(frame.variableValueAt(index), eager.variables[index].value)) {
                    throw new IllegalStateException("binding prototype variable payload differs at " + index);
                }
            }
            for (int index = 0; index < frame.calculationCount(); index++) {
                if (!frame.calculationKeyAt(index).equals(append.calculationKeyAt(index))
                        || !frame.calculationKeyAt(index).equals(eager.calculations[index].key)
                        || !Objects.equals(frame.calculationValueAt(index), append.calculationValueAt(index))
                        || !Objects.equals(frame.calculationValueAt(index), eager.calculations[index].value)) {
                    throw new IllegalStateException("binding prototype calculation payload differs at " + index);
                }
            }
        }

    }

    @State(Scope.Benchmark)
    public static class BranchState {

        private static final int FRAME_SIZE = 24;
        private static final int CALCULATION_START = 24;
        private static final int OPAQUE_DESCENDANTS = 8;

        @Param
        private NodeShape nodeShape;

        @Param
        private CaptureMode captureMode;

        private Object[] frame;
        private int[] relativeSlots;
        private int[] absoluteSlots;
        private Payload value;

        @Setup(Level.Trial)
        public void setUp() {
            frame = captureMode == CaptureMode.ACTIVE ? new Object[FRAME_SIZE + 1] : new Object[FRAME_SIZE];
            int size = nodeShape == NodeShape.MARKABLE ? 1 : OPAQUE_DESCENDANTS;
            relativeSlots = new int[size];
            absoluteSlots = new int[size];
            Arrays.fill(relativeSlots, nodeShape == NodeShape.MARKABLE ? 0 : -1);
            Arrays.fill(absoluteSlots, nodeShape == NodeShape.MARKABLE ? CALCULATION_START : Integer.MAX_VALUE);
            value = new Payload(42);
        }

        long slotFirst() {
            if (nodeShape == NodeShape.MARKABLE) {
                return slotFirstAt(relativeSlots[0]);
            }
            return opaqueSlotFirst();
        }

        long modeFirst() {
            if (nodeShape == NodeShape.MARKABLE) {
                return modeFirstAt(relativeSlots[0]);
            }
            return opaqueModeFirst();
        }

        long fusedAbsoluteSlot() {
            if (nodeShape == NodeShape.MARKABLE) {
                return fusedAt(absoluteSlots[0]);
            }
            return opaqueFused();
        }

        private long opaqueSlotFirst() {
            return mixBranches(slotFirstAt(relativeSlots[0]), slotFirstAt(relativeSlots[1]),
                    slotFirstAt(relativeSlots[2]), slotFirstAt(relativeSlots[3]),
                    slotFirstAt(relativeSlots[4]), slotFirstAt(relativeSlots[5]),
                    slotFirstAt(relativeSlots[6]), slotFirstAt(relativeSlots[7]));
        }

        private long opaqueModeFirst() {
            return mixBranches(modeFirstAt(relativeSlots[0]), modeFirstAt(relativeSlots[1]),
                    modeFirstAt(relativeSlots[2]), modeFirstAt(relativeSlots[3]),
                    modeFirstAt(relativeSlots[4]), modeFirstAt(relativeSlots[5]),
                    modeFirstAt(relativeSlots[6]), modeFirstAt(relativeSlots[7]));
        }

        private long opaqueFused() {
            return mixBranches(fusedAt(absoluteSlots[0]), fusedAt(absoluteSlots[1]),
                    fusedAt(absoluteSlots[2]), fusedAt(absoluteSlots[3]),
                    fusedAt(absoluteSlots[4]), fusedAt(absoluteSlots[5]),
                    fusedAt(absoluteSlots[6]), fusedAt(absoluteSlots[7]));
        }

        private long slotFirstAt(int relativeSlot) {
            if (relativeSlot >= 0 && frame.length > FRAME_SIZE) {
                frame[CALCULATION_START + relativeSlot] = value;
            }
            return value.value;
        }

        private long modeFirstAt(int relativeSlot) {
            if (frame.length > FRAME_SIZE && relativeSlot >= 0) {
                frame[CALCULATION_START + relativeSlot] = value;
            }
            return value.value;
        }

        private long fusedAt(int absoluteSlot) {
            if (absoluteSlot < frame.length) {
                frame[absoluteSlot] = value;
            }
            return value.value;
        }

        private static long mixBranches(
                long value0,
                long value1,
                long value2,
                long value3,
                long value4,
                long value5,
                long value6,
                long value7) {
            long checksum = 1;
            checksum = checksum * 31 + value0;
            checksum = checksum * 31 + value1;
            checksum = checksum * 31 + value2;
            checksum = checksum * 31 + value3;
            checksum = checksum * 31 + value4;
            checksum = checksum * 31 + value5;
            checksum = checksum * 31 + value6;
            checksum = checksum * 31 + value7;
            return checksum;
        }
    }

    public enum Reachability {
        EMPTY {
            @Override
            int[] slots(int slotCount) {
                return new int[0];
            }
        },
        DENSE {
            @Override
            int[] slots(int slotCount) {
                int[] slots = new int[slotCount];
                Arrays.setAll(slots, index -> index);
                return slots;
            }
        },
        PREFIX {
            @Override
            int[] slots(int slotCount) {
                int[] slots = new int[Math.min(4, slotCount)];
                Arrays.setAll(slots, index -> index);
                return slots;
            }
        },
        ONE_POINT {
            @Override
            int[] slots(int slotCount) {
                return slotCount == 0 ? new int[0] : new int[]{0};
            }
        },
        ALTERNATING {
            @Override
            int[] slots(int slotCount) {
                int[] slots = new int[(slotCount + 1) / 2];
                Arrays.setAll(slots, index -> index * 2);
                return slots;
            }
        },
        SPARSE {
            @Override
            int[] slots(int slotCount) {
                if (slotCount == 0) {
                    return new int[0];
                }
                return Arrays.stream(new int[]{0, slotCount / 4, slotCount / 2, slotCount - 1})
                        .distinct()
                        .toArray();
            }
        };

        abstract int[] slots(int slotCount);
    }

    public enum NodeShape {
        MARKABLE,
        OPAQUE_REPEATED
    }

    public enum CaptureMode {
        INACTIVE,
        ACTIVE
    }

    enum VariableOrigin {
        EXTERNAL,
        INTERNAL
    }

    enum CalculationKind {
        FUNCTION,
        PROPERTY,
        METHOD,
        CURRENT_TEMPORAL
    }

    record VariableKey(String name, VariableOrigin origin) {
    }

    record SourceSpan(int startOffset, int endOffset, int startLine, int startColumn, int endLine, int endColumn) {
    }

    record ProvenanceKey(int nodeId, SourceSpan sourceSpan) {
    }

    record CalculationKey(ProvenanceKey provenance, CalculationKind kind, String name) {
    }

    record VariableEntry(VariableKey key, Object value) {
    }

    record CalculationEntry(CalculationKey key, Object value) {
    }

    record StandaloneSchema(int[] variableSlots, VariableKey[] variableKeys, CalculationKey[] calculationKeys) {
    }

    record FrameCapture(Object[] frame, int reachedCount) {
    }

    record AppendCapture(
            Object[] frame,
            Object[] values,
            int[] ordinals,
            int count) {
    }

    record Computation(Object result, ColumnarMemory memory) {
    }

    record EagerComputation(Object result, EagerMemory memory) {
    }

    static final class ColumnarMemory {

        private final StandaloneSchema schema;
        private final Object[] variableValues;
        private final Object[] calculationValues;
        private final int[] calculationOrdinals;

        ColumnarMemory(
                StandaloneSchema schema,
                Object[] variableValues,
                Object[] calculationValues,
                int[] calculationOrdinals) {
            this.schema = schema;
            this.variableValues = variableValues;
            this.calculationValues = calculationValues;
            this.calculationOrdinals = calculationOrdinals;
        }

        int variableCount() {
            return variableValues.length;
        }

        VariableKey variableKeyAt(int index) {
            return schema.variableKeys[index];
        }

        Object variableValueAt(int index) {
            return variableValues[index];
        }

        int calculationCount() {
            return calculationValues.length;
        }

        CalculationKey calculationKeyAt(int index) {
            int ordinal = calculationOrdinals == null ? index : calculationOrdinals[index];
            return schema.calculationKeys[ordinal];
        }

        Object calculationValueAt(int index) {
            return calculationValues[index];
        }

        List<VariableEntry> variables() {
            if (variableValues.length == 0) {
                return List.of();
            }
            return new AbstractList<>() {
                @Override
                public VariableEntry get(int index) {
                    return new VariableEntry(variableKeyAt(index), variableValueAt(index));
                }

                @Override
                public int size() {
                    return variableCount();
                }
            };
        }

        List<CalculationEntry> calculations() {
            if (calculationValues.length == 0) {
                return List.of();
            }
            return new AbstractList<>() {
                @Override
                public CalculationEntry get(int index) {
                    return new CalculationEntry(calculationKeyAt(index), calculationValueAt(index));
                }

                @Override
                public int size() {
                    return calculationCount();
                }
            };
        }

        long indexedChecksum() {
            long checksum = 1;
            for (int index = 0; index < variableCount(); index++) {
                checksum = mix(checksum, variableKeyAt(index), variableValueAt(index));
            }
            for (int index = 0; index < calculationCount(); index++) {
                checksum = mix(checksum, calculationKeyAt(index), calculationValueAt(index));
            }
            return checksum;
        }

        long listChecksum() {
            long checksum = 1;
            for (VariableEntry entry : variables()) {
                checksum = mix(checksum, entry.key, entry.value);
            }
            for (CalculationEntry entry : calculations()) {
                checksum = mix(checksum, entry.key, entry.value);
            }
            return checksum;
        }

        void consume(Blackhole sink) {
            for (int index = 0; index < variableCount(); index++) {
                VariableKey key = variableKeyAt(index);
                sink.consume(key.name);
                sink.consume(key.origin.ordinal());
                consumeValue(sink, variableValueAt(index));
            }
            for (int index = 0; index < calculationCount(); index++) {
                CalculationKey key = calculationKeyAt(index);
                SourceSpan span = key.provenance.sourceSpan;
                sink.consume(key.provenance.nodeId);
                sink.consume(span.startOffset);
                sink.consume(span.endOffset);
                sink.consume(span.startLine);
                sink.consume(span.startColumn);
                sink.consume(span.endLine);
                sink.consume(span.endColumn);
                sink.consume(key.kind.ordinal());
                sink.consume(key.name);
                consumeValue(sink, calculationValueAt(index));
            }
        }
    }

    record EagerMemory(VariableEntry[] variables, CalculationEntry[] calculations) {

        long checksum() {
            long checksum = 1;
            for (VariableEntry entry : variables) {
                checksum = mix(checksum, entry.key, entry.value);
            }
            for (CalculationEntry entry : calculations) {
                checksum = mix(checksum, entry.key, entry.value);
            }
            return checksum;
        }

        void consume(Blackhole sink) {
            for (VariableEntry entry : variables) {
                sink.consume(entry.key.name);
                sink.consume(entry.key.origin.ordinal());
                consumeValue(sink, entry.value);
            }
            for (CalculationEntry entry : calculations) {
                CalculationKey key = entry.key;
                SourceSpan span = key.provenance.sourceSpan;
                sink.consume(key.provenance.nodeId);
                sink.consume(span.startOffset);
                sink.consume(span.endOffset);
                sink.consume(span.startLine);
                sink.consume(span.startColumn);
                sink.consume(span.endLine);
                sink.consume(span.endColumn);
                sink.consume(key.kind.ordinal());
                sink.consume(key.name);
                consumeValue(sink, entry.value);
            }
        }
    }

    private record Payload(int value) {
    }

    private static long mix(long checksum, Object key, Object value) {
        return (checksum * 31 + key.hashCode()) * 31 + valueHash(value);
    }

    private static int valueHash(Object value) {
        if (value == null) {
            return -1;
        }
        if (value instanceof Payload payload) {
            return payload.value;
        }
        return value.hashCode();
    }

    private static void consumeValue(Blackhole sink, Object value) {
        if (value instanceof List<?> list) {
            sink.consume(list.size());
            for (Object element : list) {
                sink.consume(valueHash(element));
            }
            return;
        }
        sink.consume(valueHash(value));
    }
}
