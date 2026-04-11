package com.runestone.expeval.internal.grammar;

import com.runestone.expeval.internal.grammar.ExpressionEvaluatorLexer;
import com.runestone.expeval.internal.grammar.ExpressionEvaluatorParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;

final class AntlrParseExecutor implements ParseExecutor {

    @Override
    public ExpressionEvaluatorParser.MathStartContext parseMath(String input, PredictionStrategy predictionStrategy) {
        return parse(input, predictionStrategy, ExpressionEvaluatorParser::mathStart);
    }

    @Override
    public ExpressionEvaluatorParser.LogicalStartContext parseLogical(String input, PredictionStrategy predictionStrategy) {
        return parse(input, predictionStrategy, ExpressionEvaluatorParser::logicalStart);
    }

    @Override
    public ExpressionEvaluatorParser.AssignmentStartContext parseAssignments(String input, PredictionStrategy predictionStrategy) {
        return parse(input, predictionStrategy, ExpressionEvaluatorParser::assignmentStart);
    }

    private <T extends ParserRuleContext> T parse(String input, PredictionStrategy predictionStrategy, StartRule<T> startRule) {
        CollectingSyntaxErrorListener errorListener = new CollectingSyntaxErrorListener();
        ExpressionEvaluatorParser parser = newParser(input, errorListener);
        configure(parser, predictionStrategy);

        T root = startRule.parse(parser);
        if (root.exception != null || errorListener.hasErrors()) {
            throw new ParsingException(input, errorListener.errors());
        }
        return root;
    }

    private static ExpressionEvaluatorParser newParser(String input, CollectingSyntaxErrorListener errorListener) {
        ExpressionEvaluatorLexer lexer = new ExpressionEvaluatorLexer(CharStreams.fromString(input));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        ExpressionEvaluatorParser parser = new ExpressionEvaluatorParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        return parser;
    }

    private static void configure(ExpressionEvaluatorParser parser, PredictionStrategy predictionStrategy) {
        if (predictionStrategy == PredictionStrategy.SLL) {
            parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
            parser.setErrorHandler(new BailErrorStrategy());
            return;
        }

        parser.getInterpreter().setPredictionMode(PredictionMode.LL);
        parser.setErrorHandler(new DefaultErrorStrategy());
    }

    @FunctionalInterface
    private interface StartRule<T extends ParserRuleContext> {
        T parse(ExpressionEvaluatorParser parser);
    }
}
