package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.semantics.DeferredCheck;

import java.util.List;

/**
 * A node of the immutable, non-optimized execution plan. Implementations preserve the identity and
 * source position of the expression construct they came from, together with only the resolved operator,
 * binding, prepared value, and typed deferred checks that construct needs; none may retain the parse
 * tree, {@code SemanticModel}, source text, or the whole {@code ExpressionEnvironment}.
 */
public interface ExecutableNode {

    NodeId id();

    SourceSpan sourceSpan();

    Object execute(ExecutionScope scope);

    default List<DeferredCheck> deferredChecks() {
        return List.of();
    }
}
