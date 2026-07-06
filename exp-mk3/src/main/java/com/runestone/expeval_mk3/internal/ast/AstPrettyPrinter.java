package com.runestone.expeval_mk3.internal.ast;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

final class AstPrettyPrinter {

    private AstPrettyPrinter() {
    }

    static String print(ExpressionFileNode file) {
        Objects.requireNonNull(file, "file");
        int lineCount = file.assignments().size() + file.resultExpression().map(expression -> 1).orElse(0);
        List<String> lines = new ArrayList<>(lineCount);
        for (AssignmentNode assignment : file.assignments()) {
            lines.add(printAssignment(assignment));
        }
        file.resultExpression().map(AstPrettyPrinter::printExpression).ifPresent(lines::add);
        return String.join("\n", lines);
    }

    private static String printAssignment(AssignmentNode assignment) {
        return printTarget(assignment.target()) + " := " + printExpression(assignment.expression()) + ";";
    }

    private static String printTarget(AssignmentTargetNode target) {
        if (target instanceof IdentifierAssignmentTargetNode identifier) {
            return identifier.name();
        }
        throw new IllegalArgumentException("Unsupported assignment target node: " + target.getClass().getName());
    }

    private static String printExpression(ExpressionNode expression) {
        return switch (expression) {
            case CurrentTemporalValueNode currentTemporalValue -> currentTemporalValue.kind().canonicalName();
            case IdentifierNode identifier -> identifier.name();
            case LiteralNode literal -> printLiteral(literal.value());
        };
    }

    private static String printLiteral(LiteralValue value) {
        return switch (value) {
            case BigIntegerLiteralValue integerValue -> integerValue.value().toString();
            case BooleanLiteralValue booleanValue -> Boolean.toString(booleanValue.value());
            case DateLiteralValue dateValue -> "d\"" + dateValue.value() + "\"";
            case DecimalLiteralValue decimalValue -> decimalValue.value().toPlainString();
            case LocalDateTimeLiteralValue dateTimeValue -> "dt\"" + dateTimeValue.value() + "\"";
            case LongLiteralValue longValue -> Long.toString(longValue.value());
            case NullLiteralValue ignored -> "null";
            case OffsetDateTimeLiteralValue dateTimeValue -> "dt\"" + dateTimeValue.value().toLocalDateTime()
                    + formatOffset(dateTimeValue.value().getOffset()) + "\"";
            case StringLiteralValue stringValue -> quote(stringValue.value());
            case TimeLiteralValue timeValue -> "t\"" + timeValue.value() + "\"";
        };
    }

    private static String formatOffset(ZoneOffset offset) {
        int totalSeconds = offset.getTotalSeconds();
        char sign = totalSeconds < 0 ? '-' : '+';
        int absoluteSeconds = Math.abs(totalSeconds);
        int hours = absoluteSeconds / 3_600;
        int minutes = (absoluteSeconds % 3_600) / 60;
        return String.format(Locale.ROOT, "%c%02d:%02d", sign, hours, minutes);
    }

    private static String quote(String value) {
        StringBuilder builder = new StringBuilder(value.length() + 2);
        builder.append('"');
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            switch (current) {
                case '\b' -> builder.append("\\b");
                case '\t' -> builder.append("\\t");
                case '\n' -> builder.append("\\n");
                case '\f' -> builder.append("\\f");
                case '\r' -> builder.append("\\r");
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                default -> builder.append(current);
            }
        }
        return builder.append('"').toString();
    }
}
