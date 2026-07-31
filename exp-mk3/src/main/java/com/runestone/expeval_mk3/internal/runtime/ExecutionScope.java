package com.runestone.expeval_mk3.internal.runtime;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;

public final class ExecutionScope {

    private static final Object UNBOUND = new Object();

    private final Object[] frame;
    private final ZoneId zoneId;
    private final Clock clock;
    private ZonedDateTime currentInstant;

    /** Builds a frame template with every slot set to the {@code UNBOUND} sentinel, distinct from {@code null}. */
    public static Object[] blankFrame(int frameSize) {
        Object[] frame = new Object[frameSize];
        Arrays.fill(frame, UNBOUND);
        return frame;
    }

    public ExecutionScope(Object[] frame, ZoneId zoneId, Clock clock) {
        this.frame = Objects.requireNonNull(frame, "frame");
        this.zoneId = Objects.requireNonNull(zoneId, "zoneId");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public Object read(int slot) {
        Object value = frame[slot];
        if (value == UNBOUND) {
            throw new IllegalStateException("frame slot is unbound: " + slot);
        }
        return value;
    }

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
