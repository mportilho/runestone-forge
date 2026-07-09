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
            case BetweenNode between -> printBetween(between);
            case BinaryOperationNode binary -> printBinary(binary);
            case CurrentTemporalValueNode currentTemporalValue -> printCurrentTemporalValue(currentTemporalValue.kind());
            case GroupedExpressionNode grouped -> "(" + printExpression(grouped.expression()) + ")";
            case IdentifierNode identifier -> identifier.name();
            case LiteralNode literal -> printLiteral(literal.value());
            case MembershipNode membership -> printMembership(membership);
            case NullCoalesceNode coalesce -> joinExpressions(coalesce.operands(), " ?? ");
            case PostfixOperationNode postfix -> printPostfix(postfix);
            case UnaryOperationNode unary -> printUnary(unary);
            case VectorLiteralNode vector -> "[" + joinExpressions(vector.elements(), ", ") + "]";
        };
    }

    private static String printBetween(BetweenNode between) {
        return printExpression(between.value())
                + (between.negated() ? " not between " : " between ")
                + printExpression(between.lowerBound())
                + " and "
                + printExpression(between.upperBound());
    }

    private static String printBinary(BinaryOperationNode binary) {
        return printExpression(binary.left()) + " " + printBinaryOperator(binary.operator()) + " " + printExpression(binary.right());
    }

    private static String printBinaryOperator(BinaryOperator operator) {
        return switch (operator) {
            case ADD -> "+";
            case CONCATENATE -> "||";
            case DIVIDE -> "/";
            case EQUAL -> "=";
            case EXPONENTIATE -> "^";
            case GREATER_THAN -> ">";
            case GREATER_THAN_OR_EQUAL -> ">=";
            case LESS_THAN -> "<";
            case LESS_THAN_OR_EQUAL -> "<=";
            case LOGICAL_AND -> "and";
            case LOGICAL_NAND -> "nand";
            case LOGICAL_NOR -> "nor";
            case LOGICAL_OR -> "or";
            case LOGICAL_XNOR -> "xnor";
            case LOGICAL_XOR -> "xor";
            case MODULO -> "mod";
            case MULTIPLY -> "*";
            case NOT_EQUAL -> "<>";
            case REGEX_MATCH -> "=~";
            case REGEX_NOT_MATCH -> "!~";
            case ROOT -> "root";
            case SUBTRACT -> "-";
        };
    }

    private static String printMembership(MembershipNode membership) {
        return printExpression(membership.element())
                + (membership.negated() ? " not in " : " in ")
                + printExpression(membership.collection());
    }

    private static String printPostfix(PostfixOperationNode postfix) {
        StringBuilder builder = new StringBuilder(printExpression(postfix.operand()));
        for (PostfixOperatorOccurrence operation : postfix.operations()) {
            builder.append(switch (operation.operator()) {
                case FACTORIAL -> "!";
                case PERCENT -> "%";
            });
        }
        return builder.toString();
    }

    private static String printUnary(UnaryOperationNode unary) {
        String operator = switch (unary.operator()) {
            case LOGICAL_NOT -> "~";
            case NEGATE -> "-";
        };
        return operator + printExpression(unary.operand());
    }

    private static String joinExpressions(List<ExpressionNode> expressions, String delimiter) {
        List<String> printed = new ArrayList<>(expressions.size());
        for (ExpressionNode expression : expressions) {
            printed.add(printExpression(expression));
        }
        return String.join(delimiter, printed);
    }

    private static String printCurrentTemporalValue(CurrentTemporalValueKind kind) {
        return switch (kind) {
            case DATE -> "currDate";
            case DATE_TIME -> "currDateTime";
            case TIME -> "currTime";
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
