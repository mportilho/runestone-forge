package com.runestone.expeval_mk3.corpus;

import java.util.Objects;

record ExpectedRuntimeError(String type, String messageContains) implements ExpectedOutcome {

    ExpectedRuntimeError {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(messageContains, "messageContains");
    }
}
