package com.runestone.expeval_mk3.internal.runtime;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;

/**
 * The scope a candidate constant subtree is executed against while the plan builder attempts to fold
 * it (ADR 0019). It carries no real frame or clock: every operation a genuinely constant subtree could
 * never need throws {@link ConstantFoldEligibilityViolation} instead of quietly succeeding, so an
 * eligibility mistake in the builder surfaces as an internal bug rather than a silently wrong fold.
 * {@link CurrentTemporalExecutableNode} is the construct this exists to catch: it is pure by
 * construction, so nothing about a purity check stops it from reaching a fold attempt.
 */
final class ConstantFoldSentinelScope extends ExecutionScope {

    static final ConstantFoldSentinelScope INSTANCE = new ConstantFoldSentinelScope();

    private ConstantFoldSentinelScope() {
        super(new Object[0], ZoneOffset.UTC, Clock.systemUTC());
    }

    @Override
    public Object read(int slot) {
        throw new ConstantFoldEligibilityViolation("constant fold attempted a frame read at slot " + slot);
    }

    @Override
    public void write(int slot, Object value) {
        throw new ConstantFoldEligibilityViolation("constant fold attempted a frame write at slot " + slot);
    }

    @Override
    public Object replace(int slot, Object value) {
        throw new ConstantFoldEligibilityViolation("constant fold attempted a frame replace at slot " + slot);
    }

    @Override
    public void restore(int slot, Object previous) {
        throw new ConstantFoldEligibilityViolation("constant fold attempted a frame restore at slot " + slot);
    }

    @Override
    public LocalDate currentDate() {
        throw new ConstantFoldEligibilityViolation("constant fold attempted to read the current date");
    }

    @Override
    public LocalTime currentTime() {
        throw new ConstantFoldEligibilityViolation("constant fold attempted to read the current time");
    }

    @Override
    public LocalDateTime currentDateTime() {
        throw new ConstantFoldEligibilityViolation("constant fold attempted to read the current date-time");
    }
}
