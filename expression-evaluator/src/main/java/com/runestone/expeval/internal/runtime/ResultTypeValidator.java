package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.IssueCode;
import com.runestone.expeval.internal.ast.ExpressionNode;
import com.runestone.expeval.internal.ast.PropertyChainNode;
import com.runestone.expeval.internal.grammar.ExpressionResultType;
import com.runestone.expeval.types.ResolvedType;
import com.runestone.expeval.types.ScalarType;
import com.runestone.expeval.types.UnknownType;

import java.util.Objects;

final class ResultTypeValidator {

    private final SemanticErrorReporter errorReporter;

    ResultTypeValidator(SemanticErrorReporter errorReporter) {
        this.errorReporter = Objects.requireNonNull(errorReporter, "errorReporter");
    }

    void validate(ExpressionResultType expectedResultType, ExpressionNode expression, ResolvedType actualType) {
        boolean tolerateUnknownPropertyChain = expression instanceof PropertyChainNode
                && actualType == UnknownType.INSTANCE;
        if (tolerateUnknownPropertyChain || actualType == UnknownType.INSTANCE) {
            return;
        }
        if (expectedResultType == ExpressionResultType.MATH && actualType != ScalarType.NUMBER) {
            errorReporter.error(
                    IssueCode.RESULT_TYPE_MISMATCH,
                    "math expressions must resolve to NUMBER",
                    expression.sourceSpan());
        }
        if (expectedResultType == ExpressionResultType.LOGICAL && actualType != ScalarType.BOOLEAN) {
            errorReporter.error(
                    IssueCode.RESULT_TYPE_MISMATCH,
                    "logical expressions must resolve to BOOLEAN",
                    expression.sourceSpan());
        }
    }

}
