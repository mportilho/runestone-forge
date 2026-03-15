package com.runestone.expeval2.grammar.language;

import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.List;
import java.util.Objects;

public final class ExpressionEvaluatorV2ParserFacade {

    private final ParseExecutor parseExecutor;

    public ExpressionEvaluatorV2ParserFacade() {
        this(new AntlrParseExecutor());
    }

    ExpressionEvaluatorV2ParserFacade(ParseExecutor parseExecutor) {
        this.parseExecutor = Objects.requireNonNull(parseExecutor, "parseExecutor must not be null");
    }

    public ParseResult<ExpressionEvaluatorV2Parser.MathStartContext> parseMath(String input) {
        String source = requireInput(input);
        try {
            return new ParseResult<>(parseExecutor.parseMath(source, PredictionStrategy.SLL), PredictionStrategy.SLL);
        } catch (ParseCancellationException ignored) {
            return new ParseResult<>(parseExecutor.parseMath(source, PredictionStrategy.LL_FALLBACK), PredictionStrategy.LL_FALLBACK);
        }
    }

    public ParseResult<ExpressionEvaluatorV2Parser.LogicalStartContext> parseLogical(String input) {
        String source = requireInput(input);
        try {
            return new ParseResult<>(parseExecutor.parseLogical(source, PredictionStrategy.SLL), PredictionStrategy.SLL);
        } catch (ParseCancellationException ignored) {
            return new ParseResult<>(parseExecutor.parseLogical(source, PredictionStrategy.LL_FALLBACK), PredictionStrategy.LL_FALLBACK);
        }
    }

    public void warmUp(List<WarmupInput> inputs) {
        Objects.requireNonNull(inputs, "inputs must not be null");
        inputs.forEach(this::warmUp);
    }

    private void warmUp(WarmupInput input) {
        Objects.requireNonNull(input, "warmup input must not be null");
        switch (input.entryPoint()) {
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
