package com.runestone.expeval2.internal.grammar;

import com.runestone.expeval2.grammar.language.ExpressionEvaluatorV2Parser;

interface ParseExecutor {

    ExpressionEvaluatorV2Parser.MathStartContext parseMath(String input, PredictionStrategy predictionStrategy);

    ExpressionEvaluatorV2Parser.LogicalStartContext parseLogical(String input, PredictionStrategy predictionStrategy);
}
