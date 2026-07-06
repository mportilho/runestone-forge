package com.runestone.expeval_mk3.api;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Environment-owned normalization result for an offset date-time literal.
 */
public record OffsetDateTimeLiteralNormalization(
        OffsetDateTime originalLiteral,
        ZoneId environmentZoneId,
        LocalDateTime normalizedLocalDateTime) {

    public OffsetDateTimeLiteralNormalization {
        Objects.requireNonNull(originalLiteral, "originalLiteral");
        Objects.requireNonNull(environmentZoneId, "environmentZoneId");
        Objects.requireNonNull(normalizedLocalDateTime, "normalizedLocalDateTime");
        LocalDateTime expected = normalize(originalLiteral, environmentZoneId);
        if (!normalizedLocalDateTime.equals(expected)) {
            throw new IllegalArgumentException(
                    "normalizedLocalDateTime must match originalLiteral normalized in environmentZoneId");
        }
    }

    public static OffsetDateTimeLiteralNormalization of(OffsetDateTime originalLiteral, ZoneId environmentZoneId) {
        Objects.requireNonNull(originalLiteral, "originalLiteral");
        Objects.requireNonNull(environmentZoneId, "environmentZoneId");
        return new OffsetDateTimeLiteralNormalization(
                originalLiteral,
                environmentZoneId,
                normalize(originalLiteral, environmentZoneId));
    }

    public ScalarType expressionType() {
        return ScalarType.DATETIME;
    }

    private static LocalDateTime normalize(OffsetDateTime originalLiteral, ZoneId environmentZoneId) {
        return originalLiteral.atZoneSameInstant(environmentZoneId).toLocalDateTime();
    }
}
