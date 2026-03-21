package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.internal.ast.SourceSpan;

import java.util.Objects;

record ExecutableIdentifier(SymbolRef ref, SourceSpan sourceSpan) implements ExecutableNode {

    ExecutableIdentifier {
        Objects.requireNonNull(ref, "ref must not be null");
        Objects.requireNonNull(sourceSpan, "sourceSpan must not be null");
    }
}
