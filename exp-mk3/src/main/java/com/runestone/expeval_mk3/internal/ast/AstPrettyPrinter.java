package com.runestone.expeval_mk3.internal.ast;

import java.math.BigInteger;
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
            case BinaryOperationNode binary -> printExpression(binary.left()) + " " + binary.operator().canonicalSymbol()
                    + " " + printExpression(binary.right());
            case ConditionalNode conditional -> printConditional(conditional);
            case CurrentItemNode ignored -> "@";
            case CurrentTemporalValueNode currentTemporalValue -> currentTemporalValue.kind().canonicalName();
            case FunctionCallNode functionCall -> printFunctionCall(functionCall);
            case GroupedExpressionNode grouped -> "(" + printExpression(grouped.expression()) + ")";
            case IdentifierNode identifier -> identifier.name();
            case LiteralNode literal -> printLiteral(literal.value());
            case MembershipNode membership -> printMembership(membership);
            case NavigationChainNode navigationChain -> printNavigationChain(navigationChain);
            case NullCoalescenceNode nullCoalescence -> printNullCoalescence(nullCoalescence);
            case PostfixOperationNode postfix -> printPostfix(postfix);
            case UnaryOperationNode unary -> unary.operator().canonicalSymbol() + printExpression(unary.operand());
            case VectorLiteralNode vectorLiteral -> printVectorLiteral(vectorLiteral);
        };
    }

    private static String printConditional(ConditionalNode conditional) {
        return switch (conditional.sourceForm()) {
            case CLASSIC -> printClassicConditional(conditional);
            case FUNCTIONAL -> printFunctionalConditional(conditional);
        };
    }

    private static String printNavigationChain(NavigationChainNode navigationChain) {
        StringBuilder builder = new StringBuilder(printExpression(navigationChain.receiver()));
        for (NavigationLink link : navigationChain.links()) {
            builder.append(printNavigationLink(link));
        }
        return builder.toString();
    }

    private static String printNavigationLink(NavigationLink link) {
        return switch (link) {
            case CollectionOperationNavigationLink collectionOperation -> ".."
                    + collectionOperation.operationName().value() + "("
                    + printCollectionOperationArguments(collectionOperation.arguments()) + ")";
            case FilterNavigationLink filter -> (filter.safeNavigation() ? "?." : "")
                    + "[?(" + printExpression(filter.predicate()) + ")]";
            case MethodNavigationLink method -> (method.safeNavigation() ? "?." : ".")
                    + method.memberName().value() + "(" + printArguments(method.arguments()) + ")";
            case PropertyNavigationLink property -> (property.safeNavigation() ? "?." : ".")
                    + property.memberName().value();
            case SubscriptNavigationLink subscript -> (subscript.safeNavigation() ? "?." : "")
                    + printSubscript(subscript.subscript());
            case WildcardNavigationLink ignored -> ".*";
        };
    }

    private static String printCollectionOperationArguments(List<CollectionOperationArgument> arguments) {
        List<String> printed = new ArrayList<>(arguments.size());
        for (CollectionOperationArgument argument : arguments) {
            printed.add(printCollectionOperationArgument(argument));
        }
        return String.join(", ", printed);
    }

    private static String printCollectionOperationArgument(CollectionOperationArgument argument) {
        return switch (argument) {
            case LambdaCollectionOperationArgument lambda -> "@ -> " + printExpression(lambda.lambda().body());
            case PositionalCollectionOperationArgument positional -> printExpression(positional.expression());
        };
    }

    private static String printFunctionCall(FunctionCallNode functionCall) {
        return functionCall.name().value() + "(" + printArguments(functionCall.arguments()) + ")";
    }

    private static String printArguments(List<ExpressionNode> arguments) {
        List<String> printed = new ArrayList<>(arguments.size());
        for (ExpressionNode argument : arguments) {
            printed.add(printExpression(argument));
        }
        return String.join(", ", printed);
    }

    private static String printSubscript(Subscript subscript) {
        return switch (subscript) {
            case IndexSubscript index -> "[" + printSignedInteger(index.index()) + "]";
            case SliceSubscript slice -> "[" + slice.start().map(AstPrettyPrinter::printSignedInteger).orElse("")
                    + ":" + slice.end().map(AstPrettyPrinter::printSignedInteger).orElse("") + "]";
            case StringKeySubscript stringKey -> "[" + quote(stringKey.key()) + "]";
            case WildcardSubscript ignored -> "[*]";
        };
    }

    private static String printSignedInteger(SignedIntegerLiteral integer) {
        BigInteger absoluteValue = integer.value().abs();
        String sign = integer.value().signum() < 0 ? "-" : "";
        return switch (integer.format()) {
            case DECIMAL -> integer.value().toString();
            case HEXADECIMAL -> sign + "0x" + absoluteValue.toString(16);
            case OCTAL -> sign + "0" + absoluteValue.toString(8);
        };
    }

    private static String printClassicConditional(ConditionalNode conditional) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < conditional.branches().size(); index++) {
            ConditionalBranchNode branch = conditional.branches().get(index);
            builder.append(index == 0 ? "if " : " elsif ")
                    .append(printExpression(branch.condition()))
                    .append(" then ")
                    .append(printExpression(branch.resultExpression()));
        }
        return builder.append(" else ")
                .append(printExpression(conditional.elseExpression()))
                .append(" endif")
                .toString();
    }

    private static String printFunctionalConditional(ConditionalNode conditional) {
        List<String> expressions = new ArrayList<>(conditional.branches().size() * 2 + 1);
        for (ConditionalBranchNode branch : conditional.branches()) {
            expressions.add(printExpression(branch.condition()));
            expressions.add(printExpression(branch.resultExpression()));
        }
        expressions.add(printExpression(conditional.elseExpression()));
        return "if(" + String.join(", ", expressions) + ")";
    }

    private static String printBetween(BetweenNode between) {
        String operator = between.negated() ? " not between " : " between ";
        return printExpression(between.value()) + operator + printExpression(between.lowerBound())
                + " and " + printExpression(between.upperBound());
    }

    private static String printMembership(MembershipNode membership) {
        String operator = membership.negated() ? " not in " : " in ";
        return printExpression(membership.value()) + operator + printExpression(membership.candidates());
    }

    private static String printNullCoalescence(NullCoalescenceNode nullCoalescence) {
        List<String> operands = new ArrayList<>(nullCoalescence.operands().size());
        for (ExpressionNode operand : nullCoalescence.operands()) {
            operands.add(printExpression(operand));
        }
        return String.join(" ?? ", operands);
    }

    private static String printVectorLiteral(VectorLiteralNode vectorLiteral) {
        List<String> elements = new ArrayList<>(vectorLiteral.elements().size());
        for (ExpressionNode element : vectorLiteral.elements()) {
            elements.add(printExpression(element));
        }
        return "[" + String.join(", ", elements) + "]";
    }

    private static String printPostfix(PostfixOperationNode postfix) {
        StringBuilder builder = new StringBuilder(printExpression(postfix.operand()));
        for (PostfixOperatorOccurrence operator : postfix.operators()) {
            builder.append(operator.operator().canonicalSymbol());
        }
        return builder.toString();
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
