package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.CurrentTemporalValueKind;
import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.util.Objects;

/** Reads one of the scope's current-temporal values (date, time, or date-time). */
public record CurrentTemporalExecutableNode(
        NodeId id,
        SourceSpan sourceSpan,
        CurrentTemporalValueKind kind) implements ExecutableNode {

    public CurrentTemporalExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(kind, "kind");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        return switch (kind) {
            case DATE -> scope.currentDate();
            case TIME -> scope.currentTime();
            case DATE_TIME -> scope.currentDateTime();
        };
    }
}
