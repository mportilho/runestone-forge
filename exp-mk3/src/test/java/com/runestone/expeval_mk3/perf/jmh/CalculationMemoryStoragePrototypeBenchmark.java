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

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * PROTOTYPE ONLY: compares three candidate storage models for Etapa 10 calculation memory. This is
 * deliberately isolated from the production runtime and should be removed only after the decision is
 * transferred to production and the user approves benchmark-artifact cleanup.
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class CalculationMemoryStoragePrototypeBenchmark {

    @Benchmark
    public FrameTailMemory captureFrameTail(PrototypeState state) {
        return state.captureFrameTail();
    }

    @Benchmark
    public CompactMemory captureAppend(PrototypeState state) {
        return state.captureAppend();
    }

    @Benchmark
    public CompactMemory captureDense(PrototypeState state) {
        return state.captureDense();
    }

    @Benchmark
    public long traverseFrameTail(PrototypeState state) {
        return state.frameTailMemory.checksum();
    }

    @Benchmark
    public long traverseAppend(PrototypeState state) {
        return state.appendMemory.checksum();
    }

    @Benchmark
    public long traverseDense(PrototypeState state) {
        return state.denseMemory.checksum();
    }

    @State(Scope.Benchmark)
    public static class PrototypeState {

        private static final Object UNBOUND = new Object();
        private static final int FRAME_SIZE = 24;
        private static final int VARIABLE_COUNT = 8;

        @Param
        private Scenario scenario;

        private Object[] frameTemplate;
        private Payload[] calculationValues;
        private int[] reachedSlots;
        private int calculationSlotCount;
        private FrameTailMemory frameTailMemory;
        private CompactMemory appendMemory;
        private CompactMemory denseMemory;

        @Setup(Level.Trial)
        public void setUp() {
            calculationSlotCount = scenario.calculationSlotCount;
            reachedSlots = scenario.reachedSlots();

            frameTemplate = new Object[FRAME_SIZE];
            Arrays.fill(frameTemplate, UNBOUND);
            for (int slot = 0; slot < VARIABLE_COUNT; slot++) {
                frameTemplate[slot] = new Payload(slot + 1);
            }

            calculationValues = new Payload[calculationSlotCount];
            for (int slot = 0; slot < calculationSlotCount; slot++) {
                calculationValues[slot] = new Payload(1_000 + slot);
            }

            frameTailMemory = captureFrameTail();
            appendMemory = captureAppend();
            denseMemory = captureDense();
            long expected = frameTailMemory.checksum();
            if (frameTailMemory.size() != VARIABLE_COUNT + reachedSlots.length
                    || appendMemory.size() != frameTailMemory.size()
                    || denseMemory.size() != frameTailMemory.size()
                    || appendMemory.checksum() != expected
                    || denseMemory.checksum() != expected) {
                throw new IllegalStateException("prototype storage models produced different memories");
            }
        }

        FrameTailMemory captureFrameTail() {
            Object[] frame = Arrays.copyOf(frameTemplate, FRAME_SIZE + calculationSlotCount);
            for (int slot : reachedSlots) {
                Object value = calculationValue(slot);
                frame[FRAME_SIZE + slot] = value == null ? FrameTailMemory.CAPTURED_NULL : value;
            }
            return new FrameTailMemory(frame, FRAME_SIZE, VARIABLE_COUNT, reachedSlots.length);
        }

        CompactMemory captureAppend() {
            Object[] frame = frameTemplate.clone();
            AppendRecorder recorder = new AppendRecorder(VARIABLE_COUNT, calculationSlotCount);
            for (int slot : reachedSlots) {
                recorder.record(slot, calculationValue(slot));
            }
            return recorder.freeze(frame);
        }

        CompactMemory captureDense() {
            Object[] frame = frameTemplate.clone();
            DenseRecorder recorder = new DenseRecorder(VARIABLE_COUNT, calculationSlotCount);
            for (int slot : reachedSlots) {
                recorder.record(slot, calculationValue(slot));
            }
            return recorder.freeze(frame);
        }

        private Object calculationValue(int slot) {
            return slot % 17 == 0 ? null : calculationValues[slot];
        }
    }

    public enum Scenario {
        VARIABLES_ONLY(0, Reachability.NONE),
        SMALL_DENSE(4, Reachability.DENSE),
        MEDIUM_DENSE(64, Reachability.DENSE),
        MEDIUM_ALTERNATING(64, Reachability.ALTERNATING),
        LARGE_NONE(256, Reachability.NONE),
        LARGE_DENSE(256, Reachability.DENSE),
        LARGE_PREFIX(256, Reachability.PREFIX),
        LARGE_SPARSE(256, Reachability.SPARSE);

        private final int calculationSlotCount;
        private final Reachability reachability;

        Scenario(int calculationSlotCount, Reachability reachability) {
            this.calculationSlotCount = calculationSlotCount;
            this.reachability = reachability;
        }

        int[] reachedSlots() {
            return reachability.slots(calculationSlotCount);
        }
    }

    private enum Reachability {
        NONE {
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
        ALTERNATING {
            @Override
            int[] slots(int slotCount) {
                int[] slots = new int[(slotCount + 1) / 2];
                Arrays.setAll(slots, index -> index * 2);
                return slots;
            }
        },
        PREFIX {
            @Override
            int[] slots(int slotCount) {
                int[] slots = new int[Math.min(16, slotCount)];
                Arrays.setAll(slots, index -> index);
                return slots;
            }
        },
        SPARSE {
            @Override
            int[] slots(int slotCount) {
                return new int[]{0, slotCount / 4, slotCount / 2, slotCount - 1};
            }
        };

        abstract int[] slots(int slotCount);
    }

    static final class FrameTailMemory {

        private static final Object CAPTURED_NULL = new Object();

        private final Object[] frame;
        private final int calculationStart;
        private final int variableCount;
        private final int capturedCalculationCount;

        private FrameTailMemory(
                Object[] frame,
                int calculationStart,
                int variableCount,
                int capturedCalculationCount) {
            this.frame = frame;
            this.calculationStart = calculationStart;
            this.variableCount = variableCount;
            this.capturedCalculationCount = capturedCalculationCount;
        }

        int size() {
            return variableCount + capturedCalculationCount;
        }

        long checksum() {
            long checksum = 1;
            for (int slot = 0; slot < variableCount; slot++) {
                checksum = mix(checksum, slot, frame[slot]);
            }
            for (int slot = calculationStart; slot < frame.length; slot++) {
                Object value = frame[slot];
                if (value != null) {
                    Object exposedValue = value == CAPTURED_NULL ? null : value;
                    checksum = mix(checksum, variableCount + slot - calculationStart, exposedValue);
                }
            }
            return checksum;
        }
    }

    static final class CompactMemory {

        private final Object[] values;
        private final int[] slots;

        private CompactMemory(Object[] values, int[] slots) {
            this.values = values;
            this.slots = slots;
        }

        int size() {
            return values.length;
        }

        long checksum() {
            long checksum = 1;
            for (int index = 0; index < values.length; index++) {
                int slot = slots == null ? index : slots[index];
                checksum = mix(checksum, slot, values[index]);
            }
            return checksum;
        }
    }

    private static final class AppendRecorder {

        private static final int INITIAL_CALCULATION_CAPACITY = 8;

        private final int variableCount;
        private final int totalPointCount;
        private Object[] values;
        private int[] slots;
        private int count;

        private AppendRecorder(int variableCount, int calculationSlotCount) {
            this.variableCount = variableCount;
            totalPointCount = variableCount + calculationSlotCount;
            count = variableCount;
        }

        void record(int calculationSlot, Object value) {
            ensureCapacity(count + 1);
            int logicalSlot = variableCount + calculationSlot;
            if (slots == null && logicalSlot != count) {
                slots = new int[values.length];
                for (int index = 0; index < count; index++) {
                    slots[index] = index;
                }
            }
            values[count] = value;
            if (slots != null) {
                slots[count] = logicalSlot;
            }
            count++;
        }

        CompactMemory freeze(Object[] frame) {
            if (values == null) {
                values = new Object[variableCount];
            } else if (values.length != count) {
                values = Arrays.copyOf(values, count);
            }
            System.arraycopy(frame, 0, values, 0, variableCount);
            if (slots != null && slots.length != count) {
                slots = Arrays.copyOf(slots, count);
            }
            return new CompactMemory(values, slots);
        }

        private void ensureCapacity(int requiredCapacity) {
            if (values == null) {
                int initialCapacity = Math.min(totalPointCount, variableCount + INITIAL_CALCULATION_CAPACITY);
                values = new Object[Math.max(requiredCapacity, initialCapacity)];
                return;
            }
            if (requiredCapacity <= values.length) {
                return;
            }
            int grownCapacity = Math.min(totalPointCount, values.length + (values.length >> 1) + 1);
            int newCapacity = Math.max(requiredCapacity, grownCapacity);
            values = Arrays.copyOf(values, newCapacity);
            if (slots != null) {
                slots = Arrays.copyOf(slots, newCapacity);
            }
        }
    }

    private static final class DenseRecorder {

        private final int variableCount;
        private final Object[] values;
        private final long[] presence;
        private int count;

        private DenseRecorder(int variableCount, int calculationSlotCount) {
            this.variableCount = variableCount;
            values = new Object[calculationSlotCount];
            presence = new long[(calculationSlotCount + Long.SIZE - 1) / Long.SIZE];
        }

        void record(int calculationSlot, Object value) {
            values[calculationSlot] = value;
            presence[calculationSlot / Long.SIZE] |= 1L << calculationSlot;
            count++;
        }

        CompactMemory freeze(Object[] frame) {
            Object[] compactValues = new Object[variableCount + count];
            System.arraycopy(frame, 0, compactValues, 0, variableCount);
            int[] compactSlots = null;
            int targetIndex = variableCount;
            for (int wordIndex = 0; wordIndex < presence.length; wordIndex++) {
                long word = presence[wordIndex];
                while (word != 0) {
                    int calculationSlot = wordIndex * Long.SIZE + Long.numberOfTrailingZeros(word);
                    int logicalSlot = variableCount + calculationSlot;
                    if (compactSlots == null && logicalSlot != targetIndex) {
                        compactSlots = new int[compactValues.length];
                        for (int index = 0; index < targetIndex; index++) {
                            compactSlots[index] = index;
                        }
                    }
                    compactValues[targetIndex] = values[calculationSlot];
                    if (compactSlots != null) {
                        compactSlots[targetIndex] = logicalSlot;
                    }
                    targetIndex++;
                    word &= word - 1;
                }
            }
            return new CompactMemory(compactValues, compactSlots);
        }
    }

    private record Payload(int value) {
    }

    private static long mix(long checksum, int slot, Object value) {
        int valueCode = value == null ? -1 : ((Payload) value).value();
        return (checksum * 31 + slot) * 31 + valueCode;
    }
}
