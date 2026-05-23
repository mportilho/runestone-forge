package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.IssueCode;
import com.runestone.expeval.internal.ast.BinaryOperationNode;
import com.runestone.expeval.internal.ast.PostfixOperationNode;
import com.runestone.expeval.internal.ast.SourceSpan;
import com.runestone.expeval.internal.ast.TernaryOperationNode;
import com.runestone.expeval.internal.ast.UnaryOperationNode;
import com.runestone.expeval.types.CollectionType;
import com.runestone.expeval.types.NullType;
import com.runestone.expeval.types.ResolvedType;
import com.runestone.expeval.types.ResolvedTypes;
import com.runestone.expeval.types.ScalarType;
import com.runestone.expeval.types.UnknownType;
import com.runestone.expeval.types.VectorType;

import java.util.Objects;

final class OperatorTypeChecker {

    private final ErrorReporter errorReporter;

    OperatorTypeChecker(ErrorReporter errorReporter) {
        this.errorReporter = Objects.requireNonNull(errorReporter, "errorReporter");
    }

    ResolvedType resolveUnary(UnaryOperationNode node, ResolvedType operandType) {
        return switch (node.operator()) {
            case NEGATE -> expectType(operandType, ScalarType.NUMBER, "unary negate", node.sourceSpan());
            case LOGICAL_NOT -> expectType(operandType, ScalarType.BOOLEAN, "logical not", node.sourceSpan());
            case SQRT, MODULUS -> expectType(operandType, ScalarType.NUMBER, "numeric unary operator", node.sourceSpan());
        };
    }

    ResolvedType resolveBinary(BinaryOperationNode node, ResolvedType leftType, ResolvedType rightType) {
        return switch (node.operator()) {
            case ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, POWER, ROOT ->
                    arithmeticType(leftType, rightType, node.sourceSpan());
            case AND, OR, XOR, XNOR, NAND, NOR -> {
                expectType(leftType, ScalarType.BOOLEAN, "logical operator", node.left().sourceSpan());
                yield expectType(rightType, ScalarType.BOOLEAN, "logical operator", node.right().sourceSpan());
            }
            case GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, EQUAL, NOT_EQUAL -> {
                if (!compatibleComparison(leftType, rightType)) {
                    errorReporter.error(
                            IssueCode.INCOMPATIBLE_COMPARISON,
                            "comparison uses incompatible operand types",
                            node.sourceSpan());
                }
                yield ScalarType.BOOLEAN;
            }
            case NULL_COALESCE -> ResolvedTypes.merge(leftType, rightType);
            case CONCATENATE -> {
                expectType(leftType, ScalarType.STRING, "string concatenation", node.left().sourceSpan());
                yield expectType(rightType, ScalarType.STRING, "string concatenation", node.right().sourceSpan());
            }
            case REGEX_MATCH, REGEX_NOT_MATCH -> {
                expectType(leftType, ScalarType.STRING, "regex match subject", node.left().sourceSpan());
                expectType(rightType, ScalarType.STRING, "regex match pattern", node.right().sourceSpan());
                yield ScalarType.BOOLEAN;
            }
            case IN, NOT_IN -> {
                if (rightType != UnknownType.INSTANCE
                        && rightType != VectorType.INSTANCE
                        && !(rightType instanceof CollectionType)) {
                    errorReporter.error(
                            IssueCode.INCOMPATIBLE_IN_OPERANDS,
                            "membership operator expects a collection/vector right operand but found " + rightType,
                            node.right().sourceSpan());
                }
                yield ScalarType.BOOLEAN;
            }
        };
    }

    ResolvedType resolveTernary(
            TernaryOperationNode node,
            ResolvedType valueType,
            ResolvedType lowerType,
            ResolvedType upperType) {
        if (!compatibleComparison(valueType, lowerType) || !compatibleComparison(valueType, upperType)) {
            errorReporter.error(
                    IssueCode.INCOMPATIBLE_COMPARISON,
                    "between operator uses incompatible operand types",
                    node.sourceSpan());
        }
        return ScalarType.BOOLEAN;
    }

    ResolvedType resolvePostfix(PostfixOperationNode node, ResolvedType operandType) {
        return expectType(operandType, ScalarType.NUMBER, "postfix operator", node.sourceSpan());
    }

    ResolvedType expectType(
            ResolvedType actualType,
            ScalarType expectedType,
            String operation,
            SourceSpan sourceSpan) {
        if (actualType != UnknownType.INSTANCE && actualType != NullType.INSTANCE && actualType != expectedType) {
            errorReporter.error(
                    IssueCode.TYPE_MISMATCH,
                    operation + " expects " + expectedType + " but found " + actualType,
                    sourceSpan);
            return UnknownType.INSTANCE;
        }
        return expectedType;
    }

    private ResolvedType arithmeticType(ResolvedType leftType, ResolvedType rightType, SourceSpan sourceSpan) {
        expectType(leftType, ScalarType.NUMBER, "arithmetic operator", sourceSpan);
        expectType(rightType, ScalarType.NUMBER, "arithmetic operator", sourceSpan);
        return ScalarType.NUMBER;
    }

    private static boolean compatibleComparison(ResolvedType leftType, ResolvedType rightType) {
        if (leftType == UnknownType.INSTANCE || rightType == UnknownType.INSTANCE) {
            return true;
        }
        if (leftType == NullType.INSTANCE || rightType == NullType.INSTANCE) {
            return true;
        }
        return leftType.equals(rightType);
    }

    @FunctionalInterface
    interface ErrorReporter {

        void error(IssueCode code, String message, SourceSpan sourceSpan);
    }
}
