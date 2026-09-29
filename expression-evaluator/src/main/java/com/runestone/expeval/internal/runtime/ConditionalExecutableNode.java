package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.SourceSpan;
import com.runestone.expeval.internal.ast.NodeId;

import java.util.List;
import java.util.Objects;

/** Evaluates branch conditions in order and executes only the selected branch or the else expression. */
public record ConditionalExecutableNode(
        NodeId id,
        SourceSpan sourceSpan,
        List<ExecutableBranch> branches,
        ExecutableNode elseExpression) implements ExecutableNode {

    public ConditionalExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        branches = List.copyOf(Objects.requireNonNull(branches, "branches"));
        Objects.requireNonNull(elseExpression, "elseExpression");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        return ExpressionRuntime.executeConditional(branches, elseExpression, scope);
    }
}
