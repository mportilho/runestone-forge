package com.runestone.expeval2.internal.grammar;

import com.runestone.expeval2.grammar.language.ExpressionEvaluatorV2Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public final class ExpressionEvaluatorV2ParserFacade {

    private final ParseExecutor parseExecutor;

    public ExpressionEvaluatorV2ParserFacade() {
        this(new AntlrParseExecutor());
    }

    ExpressionEvaluatorV2ParserFacade(ParseExecutor parseExecutor) {
        this.parseExecutor = Objects.requireNonNull(parseExecutor, "parseExecutor must not be null");
    }

    public ParseResult<ExpressionEvaluatorV2Parser.MathStartContext> parseMath(String input) {
        return parse(input, parseExecutor::parseMath);
    }

    public ParseResult<ExpressionEvaluatorV2Parser.LogicalStartContext> parseLogical(String input) {
        return parse(input, parseExecutor::parseLogical);
    }

    private <T extends ParserRuleContext> ParseResult<T> parse(String input, BiFunction<String, PredictionStrategy, T> parser) {
        String source = requireInput(input);
        try {
            return new ParseResult<>(parser.apply(source, PredictionStrategy.SLL), PredictionStrategy.SLL);
        } catch (ParseCancellationException ignored) {
            return new ParseResult<>(parser.apply(source, PredictionStrategy.LL_FALLBACK), PredictionStrategy.LL_FALLBACK);
        }
    }

    public void warmUp(List<WarmupInput> inputs) {
        Objects.requireNonNull(inputs, "inputs must not be null");
        inputs.forEach(this::warmUp);
    }

    private void warmUp(WarmupInput input) {
        Objects.requireNonNull(input, "warmup input must not be null");
        switch (input.resultType()) {
            case MATH -> this.parseMath(input.input());
            case LOGICAL -> this.parseLogical(input.input());
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
