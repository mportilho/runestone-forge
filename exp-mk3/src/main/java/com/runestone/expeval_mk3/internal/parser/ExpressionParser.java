package com.runestone.expeval_mk3.internal.parser;

import com.runestone.expeval_mk3.internal.grammar.ExpressionEvaluatorLexer;
import com.runestone.expeval_mk3.internal.grammar.ExpressionEvaluatorParser;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.api.DiagnosticCategory;
import com.runestone.expeval_mk3.api.ExpressionDiagnostic;
import com.runestone.expeval_mk3.api.SourceSpan;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class ExpressionParser {

    private static final List<String> WARM_UP_SOURCES = List.of(
            "a ?? b or c and d = e xor f || g + h * -i root j ^ k%",
            "sum(1, 2, 3)",
            "user?.address.city[0]",
            "items[?(@.active = true)]",
            "[d\"2024-01-02\", t\"10:30\", dt\"2024-01-02T10:30:00+02:00\"]");

    private final ThreadLocal<ParserContext> context = ThreadLocal.withInitial(ParserContext::new);

    public ParseResult parse(String source) {
        Objects.requireNonNull(source, "source");

        ParserContext parserContext = context.get();
        try {
            parserContext.reset(source);
            List<ExpressionDiagnostic> lexicalDiagnostics = collectLexicalDiagnostics(parserContext.tokens);

            try {
                ExpressionEvaluatorParser.StartContext tree = parseSll(parserContext);
                if (lexicalDiagnostics.isEmpty()) {
                    return new ParseSuccess(tree, PredictionPath.SLL);
                }
            } catch (ParseCancellationException | RecognitionException exception) {
                // SLL failures are intentionally discarded; LL retry emits user-facing diagnostics.
            }

            return parseLl(parserContext, lexicalDiagnostics);
        } finally {
            // The lexer/parser pair stays put for reuse; only the previous source, its buffered
            // tokens, and any input reference they hold are released from the thread context.
            parserContext.release();
        }
    }

    public void warmUp() {
        for (String source : WARM_UP_SOURCES) {
            ParseResult result = parse(source);
            if (!(result instanceof ParseSuccess success) || success.predictionPath() != PredictionPath.SLL) {
                throw new IllegalStateException("Parser warm-up source failed SLL parsing: " + source);
            }
        }
    }

    public void clearThreadCache() {
        context.remove();
    }

    static List<String> warmUpSources() {
        return WARM_UP_SOURCES;
    }

    private static ExpressionEvaluatorParser.StartContext parseSll(ParserContext context) throws RecognitionException {
        configureParser(context.parser, PredictionMode.SLL, new BailErrorStrategy());
        return context.parser.start();
    }

    private static ParseResult parseLl(ParserContext context, List<ExpressionDiagnostic> lexicalDiagnostics) {
        CapturingErrorStrategy errorStrategy = new CapturingErrorStrategy(context.source);
        configureParser(context.parser, PredictionMode.LL, errorStrategy);

        ExpressionEvaluatorParser.StartContext tree = context.parser.start();
        List<ExpressionDiagnostic> diagnostics = new ArrayList<>(lexicalDiagnostics.size() + errorStrategy.diagnostics.size());
        diagnostics.addAll(lexicalDiagnostics);
        diagnostics.addAll(errorStrategy.diagnostics);
        diagnostics.sort(DIAGNOSTIC_ORDER);

        if (diagnostics.isEmpty()) {
            return new ParseSuccess(tree, PredictionPath.LL_FALLBACK);
        }
        return new ParseFailure(diagnostics, PredictionPath.LL_FALLBACK);
    }

    private static void configureParser(ExpressionEvaluatorParser parser, PredictionMode mode, DefaultErrorStrategy errorStrategy) {
        parser.reset();
        parser.getInputStream().seek(0);
        parser.removeErrorListeners();
        parser.getInterpreter().setPredictionMode(mode);
        parser.setErrorHandler(errorStrategy);
    }

    private static List<ExpressionDiagnostic> collectLexicalDiagnostics(CommonTokenStream tokens) {
        List<ExpressionDiagnostic> diagnostics = new ArrayList<>();
        for (Token token : tokens.getTokens()) {
            if (token.getType() == ExpressionEvaluatorLexer.ERROR_CHAR) {
                diagnostics.add(ExpressionDiagnostic.error(
                        DiagnosticCategory.PARSE,
                        DiagnosticCode.PARSE_UNRECOGNIZED_CHARACTER.name(),
                        "Unrecognized character: " + token.getText(),
                        tokenSpan(token)));
            }
        }
        return diagnostics;
    }

    private static final Comparator<ExpressionDiagnostic> DIAGNOSTIC_ORDER = Comparator
            .comparingInt((ExpressionDiagnostic diagnostic) -> diagnostic.primarySpan().orElseThrow().offset())
            .thenComparingInt(ExpressionParser::diagnosticPriority)
            .thenComparingInt(diagnostic -> diagnostic.primarySpan().orElseThrow().endOffset());

    private static int diagnosticPriority(ExpressionDiagnostic diagnostic) {
        return diagnostic.code().equals(DiagnosticCode.PARSE_UNRECOGNIZED_CHARACTER.name()) ? 0 : 1;
    }

    private static SourceSpan tokenSpan(Token token) {
        int startOffset = Math.max(0, token.getStartIndex());
        if (token.getType() == Token.EOF) {
            return new SourceSpan(startOffset, startOffset, Math.max(1, token.getLine()), token.getCharPositionInLine() + 1);
        }
        int endOffset = Math.max(startOffset, token.getStopIndex() + 1);
        return new SourceSpan(startOffset, endOffset, Math.max(1, token.getLine()), token.getCharPositionInLine() + 1);
    }

    private static SourceSpan insertionSpan(Parser parser, String source) {
        Token token = parser.getCurrentToken();
        if (token.getType() == Token.EOF) {
            return eofSpan(source);
        }
        return new SourceSpan(Math.max(0, token.getStartIndex()), Math.max(0, token.getStartIndex()),
                Math.max(1, token.getLine()), token.getCharPositionInLine() + 1);
    }

    private static SourceSpan eofSpan(String source) {
        int line = 1;
        int column = 1;
        for (int index = 0; index < source.length(); index++) {
            if (source.charAt(index) == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
        }
        return new SourceSpan(source.length(), source.length(), line, column);
    }

    private static final class ParserContext {

        private final ExpressionEvaluatorLexer lexer;
        private final CommonTokenStream tokens;
        private final ExpressionEvaluatorParser parser;
        private String source;

        private ParserContext() {
            lexer = new ExpressionEvaluatorLexer(CharStreams.fromString(""));
            tokens = new CommonTokenStream(lexer);
            parser = new ExpressionEvaluatorParser(tokens);
        }

        private void reset(String source) {
            this.source = source;
            lexer.setInputStream(CharStreams.fromString(source));
            lexer.removeErrorListeners();
            tokens.setTokenSource(lexer);
            tokens.fill();
            parser.setInputStream(tokens);
        }

        private void release() {
            source = null;
            lexer.setInputStream(CharStreams.fromString(""));
            tokens.setTokenSource(lexer);
        }
    }

    private static final class CapturingErrorStrategy extends DefaultErrorStrategy {

        private final String source;
        private final List<ExpressionDiagnostic> diagnostics = new ArrayList<>();

        private CapturingErrorStrategy(String source) {
            this.source = source;
        }

        @Override
        protected void reportNoViableAlternative(Parser recognizer, NoViableAltException exception) {
            if (isMissingClosingTokenAtEof(recognizer, exception.getOffendingToken(), source)) {
                diagnostics.add(ExpressionDiagnostic.error(
                        DiagnosticCategory.PARSE,
                        DiagnosticCode.PARSE_MISSING_TOKEN.name(),
                        "Missing token",
                        eofSpan(source)));
                return;
            }
            addDiagnostic(DiagnosticCode.PARSE_NO_VIABLE_ALTERNATIVE, "No viable parse alternative", exception.getOffendingToken());
        }

        private static boolean isMissingClosingTokenAtEof(Parser recognizer, Token token, String source) {
            return token != null
                    && token.getType() == Token.EOF
                    && (recognizer.getExpectedTokens().contains(ExpressionEvaluatorParser.RPAREN)
                    || recognizer.getExpectedTokens().contains(ExpressionEvaluatorParser.RBRACKET)
                    || hasUnclosedDelimiter(source));
        }

        private static boolean hasUnclosedDelimiter(String source) {
            int parentheses = 0;
            int brackets = 0;
            boolean inString = false;
            boolean escaping = false;
            for (int index = 0; index < source.length(); index++) {
                char current = source.charAt(index);
                if (escaping) {
                    escaping = false;
                    continue;
                }
                if (inString && current == '\\') {
                    escaping = true;
                    continue;
                }
                if (current == '"') {
                    inString = !inString;
                    continue;
                }
                if (inString) {
                    continue;
                }
                if (current == '(') {
                    parentheses++;
                } else if (current == ')' && parentheses > 0) {
                    parentheses--;
                } else if (current == '[') {
                    brackets++;
                } else if (current == ']' && brackets > 0) {
                    brackets--;
                }
            }
            return parentheses > 0 || brackets > 0;
        }

        @Override
        protected void reportInputMismatch(Parser recognizer, InputMismatchException exception) {
            addDiagnostic(DiagnosticCode.PARSE_UNEXPECTED_TOKEN, "Unexpected token", exception.getOffendingToken());
        }

        @Override
        protected void reportUnwantedToken(Parser recognizer) {
            addDiagnostic(DiagnosticCode.PARSE_EXTRANEOUS_INPUT, "Extraneous input", recognizer.getCurrentToken());
        }

        @Override
        protected void reportMissingToken(Parser recognizer) {
            diagnostics.add(ExpressionDiagnostic.error(
                    DiagnosticCategory.PARSE,
                    DiagnosticCode.PARSE_MISSING_TOKEN.name(),
                    "Missing token",
                    insertionSpan(recognizer, source)));
        }

        @Override
        protected void reportFailedPredicate(Parser recognizer, org.antlr.v4.runtime.FailedPredicateException exception) {
            addDiagnostic(DiagnosticCode.PARSE_UNEXPECTED_TOKEN, "Unexpected token", exception.getOffendingToken());
        }

        private void addDiagnostic(DiagnosticCode code, String message, Token token) {
            SourceSpan span = token == null || token.getType() == Token.EOF
                    ? eofSpan(source)
                    : tokenSpan(token);
            diagnostics.add(ExpressionDiagnostic.error(DiagnosticCategory.PARSE, code.name(), message, span));
        }
    }
}
