package com.runestone.expeval_mk3.internal.runtime;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;

public final class ExecutionScope {

    private static final Object UNBOUND = new Object();

    private final Object[] frame;
    private final LocalDate currentDate;
    private final LocalTime currentTime;
    private final LocalDateTime currentDateTime;

    public ExecutionScope(int frameSize, ZoneId zoneId) {
        Objects.requireNonNull(zoneId, "zoneId");
        frame = new Object[frameSize];
        Arrays.fill(frame, UNBOUND);
        ZonedDateTime current = Instant.now().atZone(zoneId);
        currentDate = current.toLocalDate();
        currentTime = current.toLocalTime();
        currentDateTime = current.toLocalDateTime();
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
        return currentDate;
    }

    public LocalTime currentTime() {
        return currentTime;
    }

    public LocalDateTime currentDateTime() {
        return currentDateTime;
    }
}
