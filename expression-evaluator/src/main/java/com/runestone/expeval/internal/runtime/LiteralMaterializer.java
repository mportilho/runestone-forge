package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.ast.LiteralNode;
import com.runestone.expeval.types.NullType;
import com.runestone.expeval.types.ResolvedType;
import com.runestone.expeval.types.ScalarType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;

final class LiteralMaterializer {

    private LiteralMaterializer() {
    }

    static ExecutableNode build(LiteralNode literal, SemanticModel model) {
        String text = literal.value();
        return switch (text) {
            case "currDate" -> new ExecutableDynamicLiteral(DynamicInstant.CURR_DATE);
            case "currTime" -> new ExecutableDynamicLiteral(DynamicInstant.CURR_TIME);
            case "currDateTime" -> new ExecutableDynamicLiteral(DynamicInstant.CURR_DATETIME);
            default -> {
                ResolvedType resolvedType = model.findResolvedType(literal.nodeId())
                        .orElseThrow(() -> new IllegalStateException(
                                "missing resolved type for literal '" + text + "'"));
                yield new ExecutableLiteral(materialize(text, resolvedType));
            }
        };
    }

    static String unquoteStringLiteral(String value) {
        if (value.length() < 2) {
            return value;
        }
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        }
        return value;
    }

    private static Object materialize(String text, ResolvedType resolvedType) {
        if (resolvedType == NullType.INSTANCE) return null;
        if (resolvedType == ScalarType.NUMBER) return new BigDecimal(text);
        if (resolvedType == ScalarType.BOOLEAN) return Boolean.parseBoolean(text);
        if (resolvedType == ScalarType.STRING) return unquoteStringLiteral(text);
        if (resolvedType == ScalarType.DATE) return LocalDate.parse(text);
        if (resolvedType == ScalarType.TIME) return LocalTime.parse(text);
        if (resolvedType == ScalarType.DATETIME) {
            return text.contains("+") || text.endsWith("Z")
                    ? OffsetDateTime.parse(text).toLocalDateTime()
                    : LocalDateTime.parse(text);
        }
        throw new IllegalStateException("unsupported literal type: " + resolvedType);
    }
}
