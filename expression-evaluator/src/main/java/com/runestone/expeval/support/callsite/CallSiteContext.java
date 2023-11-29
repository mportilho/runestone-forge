package com.runestone.expeval.support.callsite;

import java.math.MathContext;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.function.Supplier;

/**
 * Contextual information for the current function call.
 *
 * @param mathContext     the math context to be used for calculations
 * @param scale           the scale to be used for calculations. Can be null
 * @param zoneId          the zone id to be used for date/time operations
 * @param currentDateTime Supplier to retrieve the same current date and time used for the current evaluation
 */
public record CallSiteContext(MathContext mathContext, Integer scale, ZoneId zoneId, Supplier<Temporal> currentDateTime) {

    public CallSiteContext {
        if (mathContext == null) {
            throw new IllegalArgumentException("MathContext cannot be null");
        }
        if (zoneId == null) {
            throw new IllegalArgumentException("ZoneId cannot be null");
        }
        if (currentDateTime == null) {
            throw new IllegalArgumentException("Current date/time supplier cannot be null");
        }
    }
}
