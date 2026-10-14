package com.runestone.expeval.internal.parser;

import com.runestone.expeval.internal.grammar.ExpressionEvaluatorParser;

import java.util.Objects;

public record ParseSuccess(
        ExpressionEvaluatorParser.StartContext tree,
        PredictionPath predictionPath,
        AntlrSourcePositions sourcePositions) implements ParseResult {

    public ParseSuccess {
        Objects.requireNonNull(tree, "tree");
        Objects.requireNonNull(predictionPath, "predictionPath");
        Objects.requireNonNull(sourcePositions, "sourcePositions");
    }
}
