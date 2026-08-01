package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.plan.AssignedSymbol;
import com.runestone.expeval_mk3.internal.plan.ExecutionPlan;

import java.util.List;

/**
 * Selection-time compatibility checks shared by every {@code CompiledExpression} view. Each check runs
 * once, from the plan's static {@link ExecutionPlan#resultType()} or
 * {@link ExecutionPlan#assignedSymbolsInCreationOrder()}, before any override is prepared or the plan
 * executes.
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

    static void requireAtLeastOneAssignment(List<AssignedSymbol> assignedSymbols) {
        if (assignedSymbols.isEmpty()) {
            throw ExpressionViewException.of(ExpressionViewException.Reason.NO_ASSIGNMENTS, null, null);
        }
    }

    static void requireAllAssignedSymbolsPubliclyExposable(List<AssignedSymbol> assignedSymbols) {
        for (AssignedSymbol symbol : assignedSymbols) {
            requirePubliclyExposable(symbol.type(), symbol.sourceSpan());
        }
    }

    static void requireWithinAssignmentMaterializationLimit(List<AssignedSymbol> assignedSymbols, int maxMaterializedSize) {
        if (assignedSymbols.size() > maxMaterializedSize) {
            throw ExpressionViewException.of(ExpressionViewException.Reason.MATERIALIZATION_LIMIT_EXCEEDED, null, null);
        }
    }
}
