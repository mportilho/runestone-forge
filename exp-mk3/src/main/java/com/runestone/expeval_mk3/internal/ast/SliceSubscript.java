package com.runestone.expeval_mk3.internal.ast;

import java.util.Objects;
import java.util.Optional;

record SliceSubscript(Optional<SignedIntegerLiteral> start, Optional<SignedIntegerLiteral> end) implements Subscript {

    SliceSubscript {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
    }
}
