package com.runestone.expeval_mk3.internal.ast;

import java.util.Objects;

record StringKeySubscript(String key) implements Subscript {

    StringKeySubscript {
        Objects.requireNonNull(key, "key");
    }
}
