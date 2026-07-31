package com.runestone.expeval_mk3.api;

import java.util.Objects;

public record RelatedInformation(String message, SourceSpan span) {

    public RelatedInformation {
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(span, "span");
    }
}
