package com.runestone.expeval2.perf.jmh;

import com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2Lexer;
import com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2Parser;
import com.runestone.expeval2.perf.ExpressionEvaluatorV2ScenarioCatalog;
import com.runestone.expeval2.perf.ExpressionEvaluatorV2ScenarioCatalog.EntryPoint;
import com.runestone.expeval2.perf.ExpressionEvaluatorV2ScenarioCatalog.Scenario;
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
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ExpressionEvaluatorV2WarmupBenchmark {

    private static final List<Scenario> CORPUS_SCENARIOS = ExpressionEvaluatorV2ScenarioCatalog.corpusScenarios();

    @Benchmark
    public Object parseWithSllFallbackCold(ColdScenarioState state) {
        return parseUsingSllFallback(state.scenario);
    }

    @Benchmark
    public Object parseWithSllFallbackWarmedCorpus(WarmedScenarioState state) {
        return parseUsingSllFallback(state.scenario);
    }

    private static ParserRuleContext parseUsingSllFallback(Scenario scenario) {
        try {
            ParseSession fastSession = newSession(scenario.input());
            fastSession.parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
            fastSession.parser.setErrorHandler(new BailErrorStrategy());
            return parseEntryPoint(fastSession.parser, scenario.entryPoint());
        } catch (ParseCancellationException ignored) {
            ParseSession retrySession = newSession(scenario.input());
            retrySession.parser.getInterpreter().setPredictionMode(PredictionMode.LL);
            retrySession.parser.setErrorHandler(new DefaultErrorStrategy());
            return parseEntryPoint(retrySession.parser, scenario.entryPoint());
        }
    }

    private static ParserRuleContext parseEntryPoint(ExpressionEvaluatorV2Parser parser, EntryPoint entryPoint) {
        return switch (entryPoint) {
            case MATH -> parser.mathStart();
            case LOGICAL -> parser.logicalStart();
        };
    }

    private static void clearSharedDfaCaches() {
        ExpressionEvaluatorV2Lexer lexer = new ExpressionEvaluatorV2Lexer(CharStreams.fromString(""));
        lexer.getInterpreter().clearDFA();

        ExpressionEvaluatorV2Parser parser = new ExpressionEvaluatorV2Parser(new CommonTokenStream(lexer));
        parser.getInterpreter().clearDFA();
    }

    private static void warmWithCorpus() {
        for (Scenario scenario : CORPUS_SCENARIOS) {
            parseUsingSllFallback(scenario);
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
    public abstract static class BaseScenarioState {

        @Param({
            "loan-payment-projection",
            "portfolio-discount-allocation",
            "customer-eligibility-gate",
            "settlement-window-check"
        })
        public String scenarioName;

        protected Scenario scenario;

        @Setup(Level.Trial)
        public void resolveScenario() {
            scenario = ExpressionEvaluatorV2ScenarioCatalog.requireScenario(scenarioName);
        }
    }

    public static class ColdScenarioState extends BaseScenarioState {

        @Setup(Level.Invocation)
        public void prepareInvocation() {
            clearSharedDfaCaches();
        }
    }

    public static class WarmedScenarioState extends BaseScenarioState {

        @Setup(Level.Invocation)
        public void prepareInvocation() {
            clearSharedDfaCaches();
            warmWithCorpus();
        }
    }

    private record ParseSession(ExpressionEvaluatorV2Parser parser) {
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
