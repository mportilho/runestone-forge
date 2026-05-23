package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.ast.LiteralNode;
import com.runestone.expeval.types.NullType;
import com.runestone.expeval.types.ResolvedType;
import com.runestone.expeval.types.ScalarType;
import com.runestone.expeval.types.UnknownType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

final class LiteralTypeInferencer {

    private LiteralTypeInferencer() {
    }

    static ResolvedType infer(LiteralNode node) {
        String value = node.value();
        if (isQuotedStringLiteral(value)) {
            return ScalarType.STRING;
        }
        if ("null".equals(value)) {
            return NullType.INSTANCE;
        }
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return ScalarType.BOOLEAN;
        }
        if ("currDate".equals(value) || canParseDate(value)) {
            return ScalarType.DATE;
        }
        if ("currTime".equals(value) || canParseTime(value)) {
            return ScalarType.TIME;
        }
        if ("currDateTime".equals(value) || canParseDateTime(value)) {
            return ScalarType.DATETIME;
        }
        try {
            new BigDecimal(value);
            return ScalarType.NUMBER;
        } catch (NumberFormatException ignored) {
            return UnknownType.INSTANCE;
        }
    }

    private static boolean isQuotedStringLiteral(String value) {
        return value.length() >= 2 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"';
    }

    private static boolean canParseDate(String value) {
        try {
            LocalDate.parse(value);
            return true;
        } catch (DateTimeParseException ignored) {
            return false;
        }
    }

    private static boolean canParseTime(String value) {
        try {
            LocalTime.parse(value);
            return true;
        } catch (DateTimeParseException ignored) {
            return false;
        }
    }

    private static boolean canParseDateTime(String value) {
        try {
            if (value.contains("+") || value.endsWith("Z")) {
                OffsetDateTime.parse(value);
            } else {
                LocalDateTime.parse(value);
            }
            return true;
        } catch (DateTimeParseException ignored) {
            return false;
        }
    }
}
