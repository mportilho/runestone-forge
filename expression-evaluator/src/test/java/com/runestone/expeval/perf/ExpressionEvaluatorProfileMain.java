package com.runestone.expeval.perf;

import com.runestone.expeval.internal.grammar.ExpressionEvaluatorLexer;
import com.runestone.expeval.internal.grammar.ExpressionEvaluatorParser;
import com.runestone.expeval.perf.ExpressionEvaluatorScenarioCatalog.Scenario;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.DecisionInfo;
import org.antlr.v4.runtime.atn.ParseInfo;
import org.antlr.v4.runtime.atn.PredictionMode;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ExpressionEvaluatorProfileMain {

    private static final BaseErrorListener FAILING_ERROR_LISTENER = new BaseErrorListener() {
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
    };

    private ExpressionEvaluatorProfileMain() {
    }

    public static void main(String[] args) {
        printGroup("Synthetic scenarios", ExpressionEvaluatorScenarioCatalog.syntheticScenarios());
        printGroup("Corpus scenarios", ExpressionEvaluatorScenarioCatalog.corpusScenarios());
    }

    private static void printGroup(String title, List<Scenario> scenarios) {
        System.out.println(title);
        System.out.println("=".repeat(title.length()));
        for (Scenario scenario : scenarios) {
            System.out.println("Scenario: " + scenario.name());
            System.out.println("Input: " + scenario.input());
            ProfileReport report = profile(scenario);
            printTopDecisions(report, 8);
            System.out.println();
        }
        printAggregateDecisions(scenarios, 8);
        System.out.println();
    }

    private static ProfileReport profile(Scenario scenario) {
        ExpressionEvaluatorLexer lexer = new ExpressionEvaluatorLexer(CharStreams.fromString(scenario.input()));
        lexer.removeErrorListeners();
        lexer.addErrorListener(FAILING_ERROR_LISTENER);

        ExpressionEvaluatorParser parser = new ExpressionEvaluatorParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(FAILING_ERROR_LISTENER);
        parser.setBuildParseTree(false);
        parser.setProfile(true);
        parser.getInterpreter().setPredictionMode(PredictionMode.LL);

        ParserRuleContext context = switch (scenario.entryPoint()) {
            case MATH -> parser.mathStart();
            case LOGICAL -> parser.logicalStart();
        };

        if (context.exception != null) {
            throw new IllegalStateException("parse failed for scenario %s".formatted(scenario.name()), context.exception);
        }

        return new ProfileReport(parser, parser.getParseInfo());
    }

    private static void printTopDecisions(ProfileReport report, int limit) {
        DecisionInfo[] decisions = report.parseInfo().getDecisionInfo();
        Arrays.stream(decisions)
            .filter(info -> info.invocations > 0)
            .sorted(Comparator
                .comparingLong((DecisionInfo info) -> info.timeInPrediction)
                .thenComparingLong(info -> info.SLL_TotalLook)
                .thenComparingLong(info -> info.LL_TotalLook)
                .reversed())
            .limit(limit)
            .forEach(info -> System.out.printf(
                "  decision=%d rule=%s invocations=%d timeNs=%d sllLook=%d llLook=%d llFallback=%d%n",
                info.decision,
                ruleName(report.parser(), info.decision),
                info.invocations,
                info.timeInPrediction,
                info.SLL_TotalLook,
                info.LL_TotalLook,
                info.LL_Fallback
            ));
    }

    private static void printAggregateDecisions(List<Scenario> scenarios, int limit) {
        Map<Integer, DecisionTotals> totalsByDecision = scenarios.stream()
            .map(ExpressionEvaluatorProfileMain::profile)
            .flatMap(report -> Arrays.stream(report.parseInfo().getDecisionInfo())
                .filter(info -> info.invocations > 0)
                .map(info -> new DecisionTotals(
                    info.decision,
                    ruleName(report.parser(), info.decision),
                    info.invocations,
                    info.timeInPrediction,
                    info.SLL_TotalLook,
                    info.LL_TotalLook,
                    info.LL_Fallback
                )))
            .collect(Collectors.toMap(
                DecisionTotals::decision,
                Function.identity(),
                DecisionTotals::merge
            ));

        System.out.println("Aggregate top decisions:");
        totalsByDecision.values().stream()
            .sorted(Comparator
                .comparingLong(DecisionTotals::timeInPrediction)
                .thenComparingLong(DecisionTotals::sllTotalLook)
                .thenComparingLong(DecisionTotals::llTotalLook)
                .reversed())
            .limit(limit)
            .forEach(total -> System.out.printf(
                "  decision=%d rule=%s invocations=%d timeNs=%d sllLook=%d llLook=%d llFallback=%d%n",
                total.decision(),
                total.ruleName(),
                total.invocations(),
                total.timeInPrediction(),
                total.sllTotalLook(),
                total.llTotalLook(),
                total.llFallback()
            ));
    }

    private static String ruleName(ExpressionEvaluatorParser parser, int decision) {
        int ruleIndex = parser.getATN().getDecisionState(decision).ruleIndex;
        return ExpressionEvaluatorParser.ruleNames[ruleIndex];
    }

    private record ProfileReport(ExpressionEvaluatorParser parser, ParseInfo parseInfo) {
    }

    private record DecisionTotals(
        int decision,
        String ruleName,
        long invocations,
        long timeInPrediction,
        long sllTotalLook,
        long llTotalLook,
        long llFallback
    ) {

        private DecisionTotals merge(DecisionTotals other) {
            return new DecisionTotals(
                decision,
                ruleName,
                invocations + other.invocations,
                timeInPrediction + other.timeInPrediction,
                sllTotalLook + other.sllTotalLook,
                llTotalLook + other.llTotalLook,
                llFallback + other.llFallback
            );
        }
    }
}
