package com.runestone.expeval_mk3.corpus;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

record ExpectedResult(String type, JsonNode result) implements ExpectedOutcome {

    ExpectedResult {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(result, "result");
    }
}
