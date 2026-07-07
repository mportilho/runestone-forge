package com.runestone.expeval_mk3.internal.semantics;

import java.util.Objects;
import java.util.regex.Pattern;

public record PreparedRegexPatternValue(Pattern pattern) implements PreparedSemanticValue {

    public PreparedRegexPatternValue {
        Objects.requireNonNull(pattern, "pattern");
    }
}
