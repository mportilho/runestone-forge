package com.runestone.expeval_mk3.internal.ast;

import java.util.Objects;

record IndexSubscript(SignedIntegerLiteral index) implements Subscript {

    IndexSubscript {
        Objects.requireNonNull(index, "index");
    }
}
