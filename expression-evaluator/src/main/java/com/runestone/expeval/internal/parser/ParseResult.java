package com.runestone.expeval.internal.parser;

public sealed interface ParseResult permits ParseSuccess, ParseFailure {

    PredictionPath predictionPath();
}
