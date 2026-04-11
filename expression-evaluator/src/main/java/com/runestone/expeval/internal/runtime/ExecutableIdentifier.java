package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.ast.SourceSpan;

import java.util.Objects;

record ExecutableIdentifier(SymbolRef ref, SourceSpan sourceSpan) implements ExecutableNode {

    ExecutableIdentifier {
        Objects.requireNonNull(ref, "ref must not be null");
        Objects.requireNonNull(sourceSpan, "sourceSpan must not be null");
    }
}
