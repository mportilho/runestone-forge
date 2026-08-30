package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.internal.memory.CalculationRecorder;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;

public class ExecutionScope {

    private static final Object UNBOUND = new Object();
    private static final int[] NO_REPLAY_SLOTS = new int[0];

    private final Object[] frame;
    private final ZoneId zoneId;
    private final Clock clock;
    private final CalculationRecorder calculationRecorder;
    private ZonedDateTime currentInstant;

    /** Builds a frame template with every slot set to the {@code UNBOUND} sentinel, distinct from {@code null}. */
    public static Object[] blankFrame(int frameSize) {
        Object[] frame = new Object[frameSize];
        Arrays.fill(frame, UNBOUND);
        return frame;
    }

    public static Object[] extendFrame(Object[] template, int frameSize) {
        Object[] frame = Arrays.copyOf(template, frameSize);
        Arrays.fill(frame, template.length, frameSize, UNBOUND);
        return frame;
    }

    public ExecutionScope(Object[] frame, ZoneId zoneId, Clock clock) {
        this(frame, zoneId, clock, null);
    }

    public ExecutionScope(Object[] frame, ZoneId zoneId, Clock clock, CalculationRecorder calculationRecorder) {
        this.frame = Objects.requireNonNull(frame, "frame");
        this.zoneId = Objects.requireNonNull(zoneId, "zoneId");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.calculationRecorder = calculationRecorder;
    }

    public Object read(int slot) {
        Object value = frame[slot];
        if (value == UNBOUND) {
            throw new IllegalStateException("frame slot is unbound: " + slot);
        }
        return value;
    }

    /** Rejects {@code null}; see {@link #writeMemo} for the one frame-slot family that must accept it. */
    public void write(int slot, Object value) {
        frame[slot] = Objects.requireNonNull(value, "value");
    }

    public Object replace(int slot, Object value) {
        Object previous = frame[slot];
        frame[slot] = Objects.requireNonNull(value, "value");
        return previous;
    }

    public void restore(int slot, Object previous) {
        frame[slot] = Objects.requireNonNull(previous, "previous");
    }

    /**
     * Whether a Subexpressao Comum Memoizada slot still holds the {@code UNBOUND} sentinel, i.e. no
     * occurrence has computed it yet for this call.
     */
    public boolean isMemoUnbound(int slot) {
        return frame[slot] == UNBOUND;
    }

    /**
     * Writes a memoized value in place, unlike {@link #write} this accepts {@code null}: an eligible
     * memo subtree can legitimately evaluate to null (e.g. through safe navigation), and {@code null}
     * remains distinct from the {@code UNBOUND} sentinel.
     */
    public void writeMemo(int slot, Object value) {
        frame[slot] = value;
    }

    /** Reads a memoized value already known bound by a prior {@link #isMemoUnbound} check. */
    public Object readMemo(int slot) {
        return frame[slot];
    }

    public void captureCalculation(int calculationSlot, Object value) {
        captureCalculation(calculationSlot, NO_REPLAY_SLOTS, value);
    }

    public void captureCalculation(int calculationSlot, int[] replaySlots, Object value) {
        CalculationRecorder active = calculationRecorder;
        if (active != null) {
            for (int replaySlot : replaySlots) {
                frame[replaySlot] = value;
            }
            if (calculationSlot >= 0) {
                active.append(calculationSlot, value);
            }
        }
    }

    void captureCalculations(int[] calculationSlots, Object[] values) {
        CalculationRecorder active = calculationRecorder;
        if (active == null) {
            return;
        }
        for (int index = 0; index < calculationSlots.length; index++) {
            active.append(calculationSlots[index], values[index]);
        }
    }

    void captureCalculations(int[] calculationSlots, int[][] replaySlots, Object[] values) {
        CalculationRecorder active = calculationRecorder;
        if (active == null) {
            return;
        }
        for (int index = 0; index < calculationSlots.length; index++) {
            for (int replaySlot : replaySlots[index]) {
                frame[replaySlot] = values[index];
            }
            if (calculationSlots[index] >= 0) {
                active.append(calculationSlots[index], values[index]);
            }
        }
    }

    void replayCalculations(int[] calculationSlots, int[] replaySlots) {
        CalculationRecorder active = calculationRecorder;
        if (active == null) {
            return;
        }
        for (int index = 0; index < calculationSlots.length; index++) {
            Object value = frame[replaySlots[index]];
            if (value != UNBOUND) {
                active.append(calculationSlots[index], value);
            }
        }
    }

    public LocalDate currentDate() {
        return currentZonedDateTime().toLocalDate();
    }

    public LocalTime currentTime() {
        return currentZonedDateTime().toLocalTime();
    }

    public LocalDateTime currentDateTime() {
        return currentZonedDateTime().toLocalDateTime();
    }

    /**
     * Consults the clock at most once per scope, on first use, truncated to whole seconds so that
     * {@code currDate}, {@code currTime}, and {@code currDateTime} observed in the same call are coherent.
     */
    private ZonedDateTime currentZonedDateTime() {
        if (currentInstant == null) {
            currentInstant = clock.instant().truncatedTo(ChronoUnit.SECONDS).atZone(zoneId);
        }
        return currentInstant;
    }
}
