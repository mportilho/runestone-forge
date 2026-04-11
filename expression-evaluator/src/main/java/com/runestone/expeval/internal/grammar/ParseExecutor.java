package com.runestone.expeval.internal.grammar;

import com.runestone.expeval.internal.grammar.ExpressionEvaluatorParser;

interface ParseExecutor {

    ExpressionEvaluatorParser.MathStartContext parseMath(String input, PredictionStrategy predictionStrategy);

    ExpressionEvaluatorParser.LogicalStartContext parseLogical(String input, PredictionStrategy predictionStrategy);

    ExpressionEvaluatorParser.AssignmentStartContext parseAssignments(String input, PredictionStrategy predictionStrategy);
}
