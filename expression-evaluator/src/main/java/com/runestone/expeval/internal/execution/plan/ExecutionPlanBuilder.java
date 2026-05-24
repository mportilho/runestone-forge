package com.runestone.expeval.internal.execution.plan;

import com.runestone.expeval.api.AuditEvent;
import com.runestone.expeval.catalog.ExternalSymbolCatalog;
import com.runestone.expeval.catalog.TypeHintCatalog;
import com.runestone.expeval.internal.ast.AssignmentNode;
import com.runestone.expeval.internal.ast.ExpressionFileNode;
import com.runestone.expeval.internal.runtime.RuntimeServices;
import com.runestone.expeval.internal.semantic.SemanticModel;
import com.runestone.expeval.internal.semantic.SymbolIndexAllocator;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

public final class ExecutionPlanBuilder {

    public ExecutionPlan build(
            SemanticModel model,
            RuntimeServices runtimeServices,
            ExternalSymbolCatalog externalSymbolCatalog,
            TypeHintCatalog typeHintCatalog,
            MathContext mathContext) {
        SymbolIndexAllocator.assignIndices(model);

        ExpressionFileNode ast = model.ast();
        ConstantFoldContext foldContext = new ConstantFoldContext();
        foldContext.symbols.putAll(ExternalBindingPlanner.seedNonOverridableConstants(
                model,
                externalSymbolCatalog,
                runtimeServices));

        ExecutableNodeBuilder nodeBuilder = new ExecutableNodeBuilder(
                model,
                runtimeServices,
                externalSymbolCatalog,
                typeHintCatalog,
                mathContext,
                foldContext);

        List<ExecutableAssignment> assignments = new ArrayList<>();
        for (AssignmentNode assignment : ast.assignments()) {
            assignments.add(nodeBuilder.buildAssignment(assignment));
        }

        ExecutableNode resultNode = ast.resultExpression() != null
                ? nodeBuilder.buildNode(ast.resultExpression())
                : null;
        List<AuditEvent> foldedVariableReads = List.copyOf(foldContext.variableReads);
        int maxAuditEvents = AuditEventEstimator.estimate(assignments, resultNode, foldedVariableReads.size());

        int externalSymbolsCount = model.externalSymbolsByName().size();
        Object[] defaults = ExternalBindingPlanner.seedDefaults(model, externalSymbolCatalog, runtimeServices);
        var externalBindingPlans = ExternalBindingPlanner.seedBindingPlans(model, externalSymbolCatalog);

        return new ExecutionPlan(
                assignments,
                resultNode,
                defaults,
                externalBindingPlans,
                externalSymbolsCount,
                maxAuditEvents,
                foldedVariableReads);
    }
}
