package com.runestone.expeval2.perf.jmh;

import com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2Lexer;
import com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2Parser;
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
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ExpressionEvaluatorV2ParsingBenchmark {

    @Benchmark
    public Object parseWithDefaultAdaptiveLl(ScenarioState state) {
        return parse(state, Strategy.DEFAULT_ADAPTIVE_LL);
    }

    @Benchmark
    public Object parseWithSllFallback(ScenarioState state) {
        return parse(state, Strategy.SLL_FALLBACK);
    }

    private ParserRuleContext parse(ScenarioState state, Strategy strategy) {
        return switch (strategy) {
            case DEFAULT_ADAPTIVE_LL -> parseDefault(state);
            case SLL_FALLBACK -> parseUsingSllFallback(state);
        };
    }

    private ParserRuleContext parseDefault(ScenarioState state) {
        ParseSession session = newSession(state.input);
        return switch (state.entryPoint) {
            case MATH -> session.parser.mathStart();
            case LOGICAL -> session.parser.logicalStart();
        };
    }

    private ParserRuleContext parseUsingSllFallback(ScenarioState state) {
        try {
            ParseSession fastSession = newSession(state.input);
            fastSession.parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
            fastSession.parser.setErrorHandler(new BailErrorStrategy());
            return switch (state.entryPoint) {
                case MATH -> fastSession.parser.mathStart();
                case LOGICAL -> fastSession.parser.logicalStart();
            };
        } catch (ParseCancellationException ignored) {
            ParseSession retrySession = newSession(state.input);
            retrySession.parser.getInterpreter().setPredictionMode(PredictionMode.LL);
            retrySession.parser.setErrorHandler(new DefaultErrorStrategy());
            return switch (state.entryPoint) {
                case MATH -> retrySession.parser.mathStart();
                case LOGICAL -> retrySession.parser.logicalStart();
            };
        }
    }

    private static ParseSession newSession(String input) {
        ExpressionEvaluatorV2Lexer lexer = new ExpressionEvaluatorV2Lexer(CharStreams.fromString(input));
        lexer.removeErrorListeners();
        lexer.addErrorListener(FailingErrorListener.INSTANCE);

        ExpressionEvaluatorV2Parser parser = new ExpressionEvaluatorV2Parser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(FailingErrorListener.INSTANCE);
        parser.setBuildParseTree(false);

        return new ParseSession(parser);
    }

    @State(Scope.Thread)
    public static class ScenarioState {

        @Param({
            "mathFlatAssignment",
            "mathNestedDecisionAssignment",
            "logicalMixedComparison",
            "vectorAssignment"
        })
        public String scenario;

        private EntryPoint entryPoint;
        private String input;

        @Setup
        public void setUp() {
            switch (scenario) {
                case "mathFlatAssignment" -> {
                    entryPoint = EntryPoint.MATH;
                    input = "a = foo; 1";
                }
                case "mathNestedDecisionAssignment" -> {
                    entryPoint = EntryPoint.MATH;
                    input = nestedDecisionAssignment(8);
                }
                case "logicalMixedComparison" -> {
                    entryPoint = EntryPoint.LOGICAL;
                    input = "if true then 10 else 20 endif > if false then 1 else 2 endif and !false";
                }
                case "vectorAssignment" -> {
                    entryPoint = EntryPoint.MATH;
                    input = "[a,b,c] = makeVec(1, if true then 2 else 3 endif, [4,5]); 1";
                }
                default -> throw new IllegalArgumentException("unknown scenario: " + scenario);
            }
        }
    }

    private static String nestedDecisionAssignment(int depth) {
        String expression = "value0";
        for (int i = 1; i <= depth; i++) {
            expression = "if flag%d then %s else value%d endif".formatted(i, expression, i);
        }
        return "result = %s; 1 + 2 * 3".formatted(expression);
    }

    private record ParseSession(ExpressionEvaluatorV2Parser parser) {
    }

    private enum EntryPoint {
        MATH,
        LOGICAL
    }

    private enum Strategy {
        DEFAULT_ADAPTIVE_LL,
        SLL_FALLBACK
    }

    private static final class FailingErrorListener extends BaseErrorListener {

        private static final FailingErrorListener INSTANCE = new FailingErrorListener();

        @Override
        public void syntaxError(
            Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int line,
            int charPositionInLine,
            String msg,
            RecognitionException e
        ) {
            throw new IllegalStateException("syntax error at %d:%d - %s".formatted(line, charPositionInLine, msg), e);
        }
    }
}
