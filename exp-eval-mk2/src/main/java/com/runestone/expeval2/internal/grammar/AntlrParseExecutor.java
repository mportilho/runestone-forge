package com.runestone.expeval2.internal.grammar;

import com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2Lexer;
import com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2Parser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;

final class AntlrParseExecutor implements ParseExecutor {

    @Override
    public ExpressionEvaluatorV2Parser.MathStartContext parseMath(String input, PredictionStrategy predictionStrategy) {
        return parse(input, predictionStrategy, ExpressionEvaluatorV2Parser::mathStart);
    }

    @Override
    public ExpressionEvaluatorV2Parser.LogicalStartContext parseLogical(String input, PredictionStrategy predictionStrategy) {
        return parse(input, predictionStrategy, ExpressionEvaluatorV2Parser::logicalStart);
    }

    private <T extends ParserRuleContext> T parse(String input, PredictionStrategy predictionStrategy, StartRule<T> startRule) {
        CollectingSyntaxErrorListener errorListener = new CollectingSyntaxErrorListener();
        ExpressionEvaluatorV2Parser parser = newParser(input, errorListener);
        configure(parser, predictionStrategy);

        T root = startRule.parse(parser);
        if (root.exception != null || errorListener.hasErrors()) {
            throw new ParsingException(input, errorListener.errors());
        }
        return root;
    }

    private static ExpressionEvaluatorV2Parser newParser(String input, CollectingSyntaxErrorListener errorListener) {
        ExpressionEvaluatorV2Lexer lexer = new ExpressionEvaluatorV2Lexer(CharStreams.fromString(input));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        ExpressionEvaluatorV2Parser parser = new ExpressionEvaluatorV2Parser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        return parser;
    }

    private static void configure(ExpressionEvaluatorV2Parser parser, PredictionStrategy predictionStrategy) {
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
        T parse(ExpressionEvaluatorV2Parser parser);
    }
}
