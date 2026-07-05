package com.runestone.expeval_mk3.internal.parser;

public sealed interface ParseResult permits ParseSuccess, ParseFailure {

    PredictionPath predictionPath();
}
