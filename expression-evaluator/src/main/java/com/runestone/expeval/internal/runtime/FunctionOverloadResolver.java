package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.IssueCode;
import com.runestone.expeval.catalog.FunctionCatalog;
import com.runestone.expeval.catalog.FunctionDescriptor;
import com.runestone.expeval.internal.ast.FunctionCallNode;
import com.runestone.expeval.internal.ast.NodeId;
import com.runestone.expeval.types.ResolvedType;
import com.runestone.expeval.types.UnknownType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class FunctionOverloadResolver {

    private final FunctionCatalog functionCatalog;
    private final Map<NodeId, ResolvedFunctionBinding> functionBindings;
    private final SemanticErrorReporter errorReporter;

    FunctionOverloadResolver(
            FunctionCatalog functionCatalog,
            Map<NodeId, ResolvedFunctionBinding> functionBindings,
            SemanticErrorReporter errorReporter) {
        this.functionCatalog = Objects.requireNonNull(functionCatalog, "functionCatalog");
        this.functionBindings = Objects.requireNonNull(functionBindings, "functionBindings");
        this.errorReporter = Objects.requireNonNull(errorReporter, "errorReporter");
    }

    ResolvedType resolve(FunctionCallNode node, List<ResolvedType> argumentTypes) {
        Collection<FunctionDescriptor> candidates = functionCatalog.findCandidates(node.functionName());
        if (candidates.isEmpty()) {
            errorReporter.error(
                    IssueCode.UNKNOWN_FUNCTION,
                    "unknown function '" + node.functionName() + "'",
                    node.sourceSpan());
            return UnknownType.INSTANCE;
        }

        FunctionDescriptor exactMatch = findExactMatch(node, argumentTypes, candidates);
        if (exactMatch == null) {
            return UnknownType.INSTANCE;
        }
        ResolvedFunctionBinding binding = new ResolvedFunctionBinding(
                exactMatch.functionRef(),
                exactMatch,
                exactMatch.returnType());
        functionBindings.put(node.nodeId(), binding);
        return exactMatch.returnType();
    }

    private FunctionDescriptor findExactMatch(
            FunctionCallNode node,
            List<ResolvedType> argumentTypes,
            Collection<FunctionDescriptor> candidates) {
        int expectedArity = node.arguments().size();
        FunctionDescriptor exactMatch = null;
        boolean arityFound = false;
        for (FunctionDescriptor descriptor : candidates) {
            if (descriptor.arity() != expectedArity) {
                continue;
            }
            arityFound = true;
            if (!SemanticTypeMatcher.matchesArguments(descriptor.parameterResolvedTypes(), argumentTypes)) {
                continue;
            }
            if (exactMatch != null) {
                errorReporter.error(
                        IssueCode.AMBIGUOUS_FUNCTION,
                        "ambiguous function call '" + node.functionName() + "'",
                        node.sourceSpan());
                return null;
            }
            exactMatch = descriptor;
        }
        if (!arityFound) {
            errorReporter.error(
                    IssueCode.INVALID_FUNCTION_ARITY,
                    "invalid arity for function '" + node.functionName() + "'",
                    node.sourceSpan());
            return null;
        }
        if (exactMatch == null) {
            errorReporter.error(
                    IssueCode.INCOMPATIBLE_FUNCTION_ARGUMENTS,
                    "incompatible arguments for function '" + node.functionName() + "'",
                    node.sourceSpan());
        }
        return exactMatch;
    }

}
