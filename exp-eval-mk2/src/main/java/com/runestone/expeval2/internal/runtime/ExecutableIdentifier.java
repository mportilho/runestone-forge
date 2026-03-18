package com.runestone.expeval2.internal.runtime;

import java.util.Objects;

record ExecutableIdentifier(SymbolRef ref) implements ExecutableNode {

    ExecutableIdentifier {
        Objects.requireNonNull(ref, "ref must not be null");
    }
}
