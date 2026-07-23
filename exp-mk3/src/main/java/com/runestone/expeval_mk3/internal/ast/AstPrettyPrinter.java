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
        return switch (target) {
            case DestructuringAssignmentTargetNode destructuring -> printDestructuringTarget(destructuring);
            case IdentifierAssignmentTargetNode identifier -> identifier.name();
        };
    }

    private static String printDestructuringTarget(DestructuringAssignmentTargetNode destructuring) {
        List<String> names = new ArrayList<>(destructuring.elements().size());
        for (IdentifierAssignmentTargetNode element : destructuring.elements()) {
            names.add(element.name());
        }
        return "[" + String.join(", ", names) + "]";
    }

    private static String printExpression(ExpressionNode expression) {
        return switch (expression) {
            case BetweenNode between -> printBetween(between);
            case BinaryOperationNode binary -> printBinary(binary);
            case ConditionalNode conditional -> printConditional(conditional);
            case CurrentItemNode ignored -> "@";
            case CurrentTemporalValueNode currentTemporalValue -> printCurrentTemporalValue(currentTemporalValue.kind());
            case FunctionCallNode functionCall -> printFunctionCall(functionCall);
            case GroupedExpressionNode grouped -> "(" + printExpression(grouped.expression()) + ")";
            case IdentifierNode identifier -> identifier.name();
            case LiteralNode literal -> printLiteral(literal.value());
            case MembershipNode membership -> printMembership(membership);
            case NavigationChainNode navigation -> printNavigationChain(navigation);
            case NullCoalesceNode coalesce -> joinExpressions(coalesce.operands(), " ?? ");
            case PostfixOperationNode postfix -> printPostfix(postfix);
            case UnaryOperationNode unary -> printUnary(unary);
            case CollectionLiteralNode collection -> "[" + joinExpressions(collection.elements(), ", ") + "]";
        };
    }

    private static String printConditional(ConditionalNode conditional) {
        return switch (conditional.syntax()) {
            case CLASSIC -> printClassicConditional(conditional);
            case FUNCTIONAL -> printFunctionalConditional(conditional);
        };
    }

    private static String printClassicConditional(ConditionalNode conditional) {
        StringBuilder builder = new StringBuilder("if ");
        ConditionalBranchNode firstBranch = conditional.branches().getFirst();
        builder.append(printExpression(firstBranch.condition()))
                .append(" then ")
                .append(printExpression(firstBranch.consequence()));
        for (int index = 1; index < conditional.branches().size(); index++) {
            ConditionalBranchNode branch = conditional.branches().get(index);
            builder.append(" elsif ")
                    .append(printExpression(branch.condition()))
                    .append(" then ")
                    .append(printExpression(branch.consequence()));
        }
        return builder.append(" else ")
                .append(printExpression(conditional.elseExpression()))
                .append(" endif")
                .toString();
    }

    private static String printFunctionalConditional(ConditionalNode conditional) {
        StringBuilder builder = new StringBuilder("if(");
        int separatorIndex = 0;
        for (ConditionalBranchNode branch : conditional.branches()) {
            builder.append(printExpression(branch.condition()))
                    .append(conditional.separators().get(separatorIndex++).separator().text())
                    .append(' ')
                    .append(printExpression(branch.consequence()))
                    .append(conditional.separators().get(separatorIndex++).separator().text())
                    .append(' ');
        }
        return builder.append(printExpression(conditional.elseExpression())).append(')').toString();
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

    private static String printFunctionCall(FunctionCallNode functionCall) {
        return functionCall.name().value() + "(" + joinCallArguments(functionCall.arguments()) + ")";
    }

    private static String printNavigationChain(NavigationChainNode navigation) {
        StringBuilder builder = new StringBuilder(printExpression(navigation.receiver()));
        for (NavigationLink link : navigation.links()) {
            builder.append(printNavigationLink(link));
        }
        return builder.toString();
    }

    private static String printNavigationLink(NavigationLink link) {
        return switch (link) {
            case CallNavigationLink call -> (call.safe() ? "?." : ".")
                    + call.memberName().value()
                    + "(" + joinCallArguments(call.arguments()) + ")";
            case FilterNavigationLink filter -> safePrefix(filter.safe()) + "[?(" + printExpression(filter.predicate()) + ")]";
            case IndexSubscriptNavigationLink index -> safePrefix(index.safe())
                    + "[" + printSubscriptInteger(index.index()) + "]";
            case PropertyNavigationLink property -> (property.safe() ? "?." : ".") + property.memberName().value();
            case SliceSubscriptNavigationLink slice -> safePrefix(slice.safe())
                    + "[" + printSliceBound(slice.start()) + ":" + printSliceBound(slice.end()) + "]";
            case StringKeySubscriptNavigationLink stringKey -> safePrefix(stringKey.safe()) + "[" + quote(stringKey.key()) + "]";
            case WildcardNavigationLink wildcard -> safePrefix(wildcard.safe()) + "[*]";
        };
    }

    private static String joinCallArguments(List<CallArgument> arguments) {
        List<String> printed = new ArrayList<>(arguments.size());
        for (CallArgument argument : arguments) {
            printed.add(printCallArgument(argument));
        }
        return String.join(", ", printed);
    }

    private static String printCallArgument(CallArgument argument) {
        return switch (argument) {
            case ExpressionCallArgument expression -> printExpression(expression.expression());
            case LambdaCallArgument lambda -> printLambda(lambda.lambda());
        };
    }

    private static String printLambda(LambdaNode lambda) {
        return printExpression(lambda.currentItem()) + " -> " + printExpression(lambda.body());
    }

    private static String safePrefix(boolean safe) {
        return safe ? "?." : "";
    }

    private static String printSliceBound(SubscriptSliceBound bound) {
        return switch (bound) {
            case IntegerSubscriptSliceBound integer -> printSubscriptInteger(integer.integer());
            case UnboundedSubscriptSliceBound ignored -> "";
        };
    }

    private static String printSubscriptInteger(SubscriptIntegerLiteral integer) {
        return integer.value().toString();
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
