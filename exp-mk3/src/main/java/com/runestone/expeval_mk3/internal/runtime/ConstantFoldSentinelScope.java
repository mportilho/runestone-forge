package com.runestone.expeval_mk3.internal.runtime;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Arrays;

/**
 * The scope a candidate constant subtree is executed against while the plan builder attempts to fold
 * it (ADR 0019). It carries no real frame or clock: every operation a genuinely constant subtree could
 * never need throws {@link ConstantFoldEligibilityViolation} instead of quietly succeeding, so an
 * eligibility mistake in the builder surfaces as an internal bug rather than a silently wrong fold.
 * {@link CurrentTemporalExecutableNode} is the construct this exists to catch: it is pure by
 * construction, so nothing about a purity check stops it from reaching a fold attempt.
 */
final class ConstantFoldSentinelScope extends ExecutionScope {

    static final ConstantFoldSentinelScope INSTANCE = new ConstantFoldSentinelScope(false);

    private final boolean capturesCalculations;
    private int[] calculationSlots;
    private Object[] calculationValues;
    private int calculationCount;

    private ConstantFoldSentinelScope(boolean capturesCalculations) {
        super(new Object[0], ZoneOffset.UTC, Clock.systemUTC());
        this.capturesCalculations = capturesCalculations;
    }

    static ConstantFoldSentinelScope capturing() {
        return new ConstantFoldSentinelScope(true);
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
    public void captureCalculation(int calculationSlot, Object value) {
        if (calculationSlot < 0) {
            return;
        }
        if (!capturesCalculations) {
            throw new ConstantFoldEligibilityViolation(
                    "constant fold attempted to capture an unanticipated calculation point");
        }
        if (calculationSlots == null) {
            calculationSlots = new int[4];
            calculationValues = new Object[4];
        }
        if (calculationCount > 0 && calculationSlot <= calculationSlots[calculationCount - 1]) {
            throw new ConstantFoldEligibilityViolation(
                    "constant fold captured calculation ordinals out of order: " + calculationSlot);
        }
        if (calculationCount == calculationSlots.length) {
            calculationSlots = Arrays.copyOf(calculationSlots, calculationCount << 1);
            calculationValues = Arrays.copyOf(calculationValues, calculationCount << 1);
        }
        calculationSlots[calculationCount] = calculationSlot;
        calculationValues[calculationCount] = value;
        calculationCount++;
    }

    @Override
    void captureCalculations(int[] slots, Object[] values) {
        for (int index = 0; index < slots.length; index++) {
            captureCalculation(slots[index], values[index]);
        }
    }

    StaticCalculationGroup calculationGroup() {
        return calculationCount == 0
                ? StaticCalculationGroup.EMPTY
                : new StaticCalculationGroup(
                        Arrays.copyOf(calculationSlots, calculationCount),
                        Arrays.copyOf(calculationValues, calculationCount));
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
