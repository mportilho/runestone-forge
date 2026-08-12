package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.util.Objects;

public record TwoBranchConditionalExecutableNode(
        NodeId id,
        SourceSpan sourceSpan,
        ExecutableBranch first,
        ExecutableBranch second,
        ExecutableNode elseExpression) implements ExecutableNode {

    public TwoBranchConditionalExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        Objects.requireNonNull(elseExpression, "elseExpression");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        if ((Boolean) first.condition().execute(scope)) {
            return first.consequence().execute(scope);
        }
        return (Boolean) second.condition().execute(scope)
                ? second.consequence().execute(scope)
                : elseExpression.execute(scope);
    }
}
