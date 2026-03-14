package com.runestone.expeval2.grammar.language;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.ArrayList;
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

    public ParseResult<com.runestone.expeval2.grammar.language.ExpressionEvaluatorV2Parser.MathStartContext> parseMath(String input) {
        return parse(input, EntryPoint.MATH);
    }

    public ParseResult<com.runestone.expeval2.grammar.language.ExpressionEvaluatorV2Parser.LogicalStartContext> parseLogical(String input) {
        return parse(input, EntryPoint.LOGICAL);
    }

    @SuppressWarnings("unchecked")
    private <T extends ParserRuleContext> ParseResult<T> parse(String input, EntryPoint entryPoint) {
        String source = requireInput(input);
        try {
            return new ParseResult<>((T) parseExecutor.parse(source, entryPoint, PredictionStrategy.SLL), PredictionStrategy.SLL);
        } catch (ParseCancellationException ignored) {
            return new ParseResult<>((T) parseExecutor.parse(source, entryPoint, PredictionStrategy.LL_FALLBACK), PredictionStrategy.LL_FALLBACK);
        }
    }

    private static String requireInput(String input) {
        Objects.requireNonNull(input, "input must not be null");
        if (input.isBlank()) {
            throw new IllegalArgumentException("input must not be blank");
        }
        return input;
    }

    public record ParseResult<T extends ParserRuleContext>(T root, PredictionStrategy predictionStrategy) {

        public ParseResult {
            Objects.requireNonNull(root, "root must not be null");
            Objects.requireNonNull(predictionStrategy, "predictionStrategy must not be null");
        }
    }

    public enum PredictionStrategy {
        SLL,
        LL_FALLBACK
    }

    public record SyntaxError(int line, int charPositionInLine, String message) {

        public SyntaxError {
            Objects.requireNonNull(message, "message must not be null");
        }
    }

    public static final class ParsingException extends RuntimeException {

        private final List<SyntaxError> errors;

        ParsingException(String input, List<SyntaxError> errors) {
            super(buildMessage(input, errors));
            this.errors = List.copyOf(errors);
        }

        public List<SyntaxError> errors() {
            return errors;
        }

        private static String buildMessage(String input, List<SyntaxError> errors) {
            if (errors.isEmpty()) {
                return "failed to parse input: %s".formatted(input);
            }

            SyntaxError firstError = errors.getFirst();
            return "failed to parse input at %d:%d - %s".formatted(
                firstError.line(),
                firstError.charPositionInLine(),
                firstError.message()
            );
        }
    }

    interface ParseExecutor {
        ParserRuleContext parse(String input, EntryPoint entryPoint, PredictionStrategy predictionStrategy);
    }

    static final class AntlrParseExecutor implements ParseExecutor {

        @Override
        public ParserRuleContext parse(String input, EntryPoint entryPoint, PredictionStrategy predictionStrategy) {
            CollectingErrorListener errorListener = new CollectingErrorListener();

            com.runestone.expeval2.grammar.language.ExpressionEvaluatorV2Lexer lexer = new com.runestone.expeval2.grammar.language.ExpressionEvaluatorV2Lexer(CharStreams.fromString(input));
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);

            com.runestone.expeval2.grammar.language.ExpressionEvaluatorV2Parser parser = new com.runestone.expeval2.grammar.language.ExpressionEvaluatorV2Parser(new CommonTokenStream(lexer));
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            if (predictionStrategy == PredictionStrategy.SLL) {
                parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
                parser.setErrorHandler(new BailErrorStrategy());
            } else {
                parser.getInterpreter().setPredictionMode(PredictionMode.LL);
                parser.setErrorHandler(new DefaultErrorStrategy());
            }

            ParserRuleContext root = switch (entryPoint) {
                case MATH -> parser.mathStart();
                case LOGICAL -> parser.logicalStart();
            };

            if (root.exception != null || errorListener.hasErrors()) {
                throw new ParsingException(input, errorListener.errors());
            }

            return root;
        }
    }

    enum EntryPoint {
        MATH,
        LOGICAL
    }

    static final class CollectingErrorListener extends BaseErrorListener {

        private final List<SyntaxError> errors = new ArrayList<>();

        @Override
        public void syntaxError(
            Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int line,
            int charPositionInLine,
            String msg,
            RecognitionException e
        ) {
            errors.add(new SyntaxError(line, charPositionInLine, msg));
        }

        boolean hasErrors() {
            return !errors.isEmpty();
        }

        List<SyntaxError> errors() {
            return List.copyOf(errors);
        }
    }
}
