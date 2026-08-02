package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.util.List;
import java.util.Objects;

/** Materializes a collection literal by evaluating each element left to right. */
public record CollectionLiteralExecutableNode(
        NodeId id,
        SourceSpan sourceSpan,
        List<ExecutableNode> elements) implements ExecutableNode {

    public CollectionLiteralExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        elements = List.copyOf(Objects.requireNonNull(elements, "elements"));
    }

    @Override
    public Object execute(ExecutionScope scope) {
        return ExpressionRuntime.materialize(elements, scope, sourceSpan);
    }
}
