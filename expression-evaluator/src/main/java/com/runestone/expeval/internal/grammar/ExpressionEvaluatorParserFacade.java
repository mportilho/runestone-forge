package com.runestone.expeval.internal.grammar;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.Objects;
import java.util.function.BiFunction;

public final class ExpressionEvaluatorParserFacade {

    private final ParseExecutor parseExecutor;

    public ExpressionEvaluatorParserFacade() {
        this(new AntlrParseExecutor());
    }

    ExpressionEvaluatorParserFacade(ParseExecutor parseExecutor) {
        this.parseExecutor = Objects.requireNonNull(parseExecutor, "parseExecutor must not be null");
    }

    public ParseResult<ExpressionEvaluatorParser.MathStartContext> parseMath(String input) {
        return parse(input, parseExecutor::parseMath);
    }

    public ParseResult<ExpressionEvaluatorParser.LogicalStartContext> parseLogical(String input) {
        return parse(input, parseExecutor::parseLogical);
    }

    public ParseResult<ExpressionEvaluatorParser.AssignmentStartContext> parseAssignments(String input) {
        return parse(input, parseExecutor::parseAssignments);
    }

    private <T extends ParserRuleContext> ParseResult<T> parse(String input, BiFunction<String, PredictionStrategy, T> parser) {
        String source = requireInput(input);
        try {
            return new ParseResult<>(parser.apply(source, PredictionStrategy.SLL), PredictionStrategy.SLL);
        } catch (ParseCancellationException ignored) {
            return new ParseResult<>(parser.apply(source, PredictionStrategy.LL_FALLBACK), PredictionStrategy.LL_FALLBACK);
        }
    }

    private static String requireInput(String input) {
        Objects.requireNonNull(input, "input must not be null");
        if (input.isBlank()) {
            throw new IllegalArgumentException("input must not be blank");
        }
        return input;
    }

}
