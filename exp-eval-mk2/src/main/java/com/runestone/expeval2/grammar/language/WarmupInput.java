package com.runestone.expeval2.grammar.language;

import java.util.Objects;

public record WarmupInput(String input, WarmupTarget entryPoint) {

    public WarmupInput {
        Objects.requireNonNull(input, "input must not be null");
        Objects.requireNonNull(entryPoint, "entryPoint must not be null");
    }
}
