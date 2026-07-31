package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.semantics.ContextualMemberNavigationBinding;

import java.util.Objects;

/** {@code @.key}/{@code @.value}/{@code @.accumulator}/{@code @.item} against a contextual Item Atual. */
public record ContextualMemberExecutableNode(
        NodeId id,
        SourceSpan sourceSpan,
        ExecutableNode receiver,
        ContextualMemberNavigationBinding.Member member,
        boolean safe) implements ExecutableNode {

    public ContextualMemberExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(receiver, "receiver");
        Objects.requireNonNull(member, "member");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        return ExpressionRuntime.contextualMemberValue(receiver.execute(scope), member, safe);
    }
}
