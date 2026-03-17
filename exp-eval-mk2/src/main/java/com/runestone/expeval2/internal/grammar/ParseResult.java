package com.runestone.expeval2.internal.grammar;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Objects;

public record ParseResult<T extends ParserRuleContext>(T root, PredictionStrategy predictionStrategy) {

    public ParseResult {
        Objects.requireNonNull(root, "root must not be null");
        Objects.requireNonNull(predictionStrategy, "predictionStrategy must not be null");
    }
}
