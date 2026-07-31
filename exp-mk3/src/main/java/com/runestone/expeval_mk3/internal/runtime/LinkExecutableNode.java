package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.util.Objects;
import java.util.function.Function;

/**
 * Bridges a navigation or collection-operation link's own closure into {@link ExecutableNode} without
 * migrating that link onto a dedicated node class. Navigation and collection-operation nodes remain the
 * scope of a dependent ticket; this adapter only attaches the identity and source position the interface
 * requires so those links keep compiling and executing unchanged in the meantime.
 */
public record LinkExecutableNode(
        NodeId id,
        SourceSpan sourceSpan,
        Function<ExecutionScope, Object> delegate) implements ExecutableNode {

    public LinkExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        return delegate.apply(scope);
    }
}
