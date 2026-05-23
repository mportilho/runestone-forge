package com.runestone.expeval.internal.execution.plan;

import com.runestone.expeval.internal.ast.SourceSpan;
import com.runestone.expeval.internal.semantic.SymbolRef;

import java.util.Objects;

public record ExecutableIdentifier(SymbolRef ref, SourceSpan sourceSpan) implements ExecutableNode {

    public ExecutableIdentifier {
        Objects.requireNonNull(ref, "ref must not be null");
        Objects.requireNonNull(sourceSpan, "sourceSpan must not be null");
    }
}
