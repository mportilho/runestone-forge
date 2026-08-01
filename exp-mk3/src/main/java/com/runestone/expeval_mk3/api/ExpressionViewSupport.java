package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.plan.ExecutionPlan;

/**
 * Selection-time compatibility checks shared by every {@code CompiledExpression} view. Each check runs
 * once, from the plan's static {@link ExecutionPlan#resultType()}, before any override is prepared or the
 * plan executes.
 */
final class ExpressionViewSupport {

    private ExpressionViewSupport() {
    }

    static ExpressionType requireResultType(ExecutionPlan plan) {
        ExpressionType resultType = plan.resultType();
        if (resultType == null) {
            throw ExpressionViewException.of(ExpressionViewException.Reason.NO_RESULT_EXPRESSION, null, null);
        }
        return resultType;
    }

    static void requireExactScalarType(ExpressionType resultType, ScalarType expected, SourceSpan span) {
        if (resultType != expected) {
            throw ExpressionViewException.of(ExpressionViewException.Reason.TYPE_MISMATCH, resultType, span);
        }
    }

    static void requirePubliclyExposable(ExpressionType resultType, SourceSpan span) {
        if (!PublicMaterialization.isPubliclyExposable(resultType)) {
            throw ExpressionViewException.of(ExpressionViewException.Reason.TYPE_NOT_PUBLICLY_EXPOSABLE, resultType, span);
        }
    }
}
