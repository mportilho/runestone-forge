package com.runestone.expeval.internal.parser;

import com.runestone.expeval.internal.grammar.ExpressionEvaluatorLexer;
import com.runestone.expeval.internal.grammar.ExpressionEvaluatorParser;
import com.runestone.expeval.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval.internal.diagnostics.ExpressionDiagnostics;
import com.runestone.expeval.api.ExpressionDiagnostic;
import com.runestone.expeval.api.SourceSpan;
import com.runestone.expeval.api.ExpressionResourceLimits;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.ArrayList;
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
        return parse(source, null);
    }

    public ParseResult parse(String source, ExpressionResourceLimits limits) {
        Objects.requireNonNull(source, "source");

        ParserContext parserContext = context.get();
        try {
            AntlrSourcePositions sourcePositions = AntlrSourcePositions.from(source);
            parserContext.reset(source, limits, sourcePositions);
            if (limits != null) {
                SyntaxDepthPreflight.check(parserContext.tokens.getTokens(), limits.maxSyntaxDepth(), sourcePositions);
            }
            List<ExpressionDiagnostic> lexicalDiagnostics = collectLexicalDiagnostics(parserContext.tokens, sourcePositions);

            try {
                ExpressionEvaluatorParser.StartContext tree = parseSll(parserContext);
                if (lexicalDiagnostics.isEmpty()) {
                    return new ParseSuccess(tree, PredictionPath.SLL, sourcePositions);
                }
            } catch (ParseCancellationException | RecognitionException exception) {
                // SLL failures are intentionally discarded; LL retry emits user-facing diagnostics.
            }

            return parseLl(parserContext, lexicalDiagnostics, sourcePositions);
        } catch (CompilationLimitException exception) {
            return new ParseFailure(List.of(exception.diagnostic()), PredictionPath.SLL);
        } finally {
            // The lexer/parser pair and DFA stay put for reuse; all input-specific state is released.
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

    private static ParseResult parseLl(
            ParserContext context,
            List<ExpressionDiagnostic> lexicalDiagnostics,
            AntlrSourcePositions sourcePositions) {
        CapturingErrorStrategy errorStrategy = new CapturingErrorStrategy(context.source, sourcePositions);
        configureParser(context.parser, PredictionMode.LL, errorStrategy);

        ExpressionEvaluatorParser.StartContext tree = context.parser.start();
        List<ExpressionDiagnostic> diagnostics = new ArrayList<>(lexicalDiagnostics.size() + errorStrategy.diagnostics.size());
        diagnostics.addAll(lexicalDiagnostics);
        diagnostics.addAll(errorStrategy.diagnostics);
        diagnostics.sort(ExpressionDiagnostics.CANONICAL_ORDER);

        if (diagnostics.isEmpty()) {
            return new ParseSuccess(tree, PredictionPath.LL_FALLBACK, sourcePositions);
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

    private static List<ExpressionDiagnostic> collectLexicalDiagnostics(
            CommonTokenStream tokens, AntlrSourcePositions sourcePositions) {
        List<ExpressionDiagnostic> diagnostics = new ArrayList<>();
        for (Token token : tokens.getTokens()) {
            if (token.getType() == ExpressionEvaluatorLexer.ERROR_CHAR) {
                diagnostics.add(ExpressionDiagnostics.create(
                        DiagnosticCode.PARSE_UNRECOGNIZED_CHARACTER,
                        "Unrecognized character: " + token.getText(),
                        sourcePositions.span(token)));
            }
        }
        return diagnostics;
    }

    private static SourceSpan insertionSpan(Parser parser, AntlrSourcePositions sourcePositions) {
        Token token = parser.getCurrentToken();
        if (token.getType() == Token.EOF) {
            return sourcePositions.eofSpan();
        }
        return sourcePositions.insertionSpan(token);
    }

    private static final class ParserContext {

        private final ExpressionEvaluatorLexer lexer;
        private final BoundedTokenStream tokens;
        private final ReusableExpressionEvaluatorParser parser;
        private final DefaultErrorStrategy idleErrorStrategy;
        private String source;

        private ParserContext() {
            lexer = new ExpressionEvaluatorLexer(CharStreams.fromString(""));
            tokens = new BoundedTokenStream(lexer);
            parser = new ReusableExpressionEvaluatorParser(tokens);
            idleErrorStrategy = new DefaultErrorStrategy();
        }

        private void reset(String source, ExpressionResourceLimits limits, AntlrSourcePositions positions) {
            this.source = source;
            lexer.setInputStream(CharStreams.fromString(source));
            lexer.removeErrorListeners();
            tokens.setTokenSource(lexer);
            tokens.configure(limits == null ? Integer.MAX_VALUE : limits.maxTokenCount(), positions);
            tokens.fill();
            parser.setInputStream(tokens);
        }

        private void release() {
            source = null;
            tokens.configure(Integer.MAX_VALUE, null);
            lexer.setInputStream(CharStreams.fromString(""));
            tokens.setTokenSource(lexer);
            parser.setErrorHandler(idleErrorStrategy);
            parser.setInputStream(tokens);
            parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
            parser.releaseTransientPredictionState();
        }
    }

    static final class BoundedTokenStream extends CommonTokenStream {

        private int limit = Integer.MAX_VALUE;
        private int count;
        private AntlrSourcePositions positions;

        BoundedTokenStream(ExpressionEvaluatorLexer lexer) {
            super(lexer);
        }

        void configure(int limit, AntlrSourcePositions positions) {
            this.limit = limit;
            this.positions = positions;
            count = 0;
        }

        @Override
        protected int fetch(int requested) {
            int fetched = 0;
            while (fetched < requested && !fetchedEOF) {
                int added = super.fetch(1);
                fetched += added;
                Token token = tokens.getLast();
                // Count all emitted tokens, including hidden whitespace, but not EOF.
                if (token.getType() != Token.EOF && ++count > limit) {
                    throw new CompilationLimitException(ExpressionDiagnostics.create(
                            DiagnosticCode.COMPILE_TOKEN_COUNT_EXCEEDED,
                            "Expression token count exceeds the compilation limit", positions.span(token)));
                }
            }
            return fetched;
        }
    }

    private static final class ReusableExpressionEvaluatorParser extends ExpressionEvaluatorParser {

        private final RetentionSafeParserATNSimulator reusableInterpreter;

        private ReusableExpressionEvaluatorParser(TokenStream input) {
            super(input);
            reusableInterpreter = new RetentionSafeParserATNSimulator(
                    this, getATN(), _decisionToDFA, _sharedContextCache);
            setInterpreter(reusableInterpreter);
        }

        private void releaseTransientPredictionState() {
            reusableInterpreter.releaseTransientState();
        }
    }

    private static final class RetentionSafeParserATNSimulator extends ParserATNSimulator {

        private RetentionSafeParserATNSimulator(
                Parser parser,
                ATN atn,
                DFA[] decisionToDfa,
                PredictionContextCache sharedContextCache) {
            super(parser, atn, decisionToDfa, sharedContextCache);
        }

        private void releaseTransientState() {
            mergeCache = null;
            _input = null;
            _startIndex = 0;
            _outerContext = null;
            _dfa = null;
        }
    }

    private static final class CapturingErrorStrategy extends DefaultErrorStrategy {

        private final String source;
        private final AntlrSourcePositions sourcePositions;
        private final List<ExpressionDiagnostic> diagnostics = new ArrayList<>();

        private CapturingErrorStrategy(String source, AntlrSourcePositions sourcePositions) {
            this.source = source;
            this.sourcePositions = sourcePositions;
        }

        @Override
        protected void reportNoViableAlternative(Parser recognizer, NoViableAltException exception) {
            if (isMissingClosingTokenAtEof(recognizer, exception.getOffendingToken(), source)) {
                diagnostics.add(ExpressionDiagnostics.create(
                        DiagnosticCode.PARSE_MISSING_TOKEN,
                        "Missing token",
                        sourcePositions.eofSpan()));
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
            diagnostics.add(ExpressionDiagnostics.create(
                    DiagnosticCode.PARSE_MISSING_TOKEN,
                    "Missing token",
                    insertionSpan(recognizer, sourcePositions)));
        }

        @Override
        protected void reportFailedPredicate(Parser recognizer, org.antlr.v4.runtime.FailedPredicateException exception) {
            addDiagnostic(DiagnosticCode.PARSE_UNEXPECTED_TOKEN, "Unexpected token", exception.getOffendingToken());
        }

        private void addDiagnostic(DiagnosticCode code, String message, Token token) {
            SourceSpan span = token == null || token.getType() == Token.EOF
                    ? sourcePositions.eofSpan()
                    : sourcePositions.span(token);
            diagnostics.add(ExpressionDiagnostics.create(code, message, span));
        }
    }
}
