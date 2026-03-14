package com.runestone.expeval2.grammar.language.perf;

import com.runestone.expeval2.grammar.language.ExpressionEvaluatorV2Lexer;
import com.runestone.expeval2.grammar.language.ExpressionEvaluatorV2Parser;
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

public final class ExpressionEvaluatorV2ProfileMain {

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

    private static final List<Scenario> SCENARIOS = List.of(
        new Scenario("math-flat-assignment", EntryPoint.MATH, "a = foo; 1"),
        new Scenario("math-nested-decision-assignment", EntryPoint.MATH, nestedDecisionAssignment(8)),
        new Scenario("logical-mixed-comparison", EntryPoint.LOGICAL, "if true then 10 else 20 endif > if false then 1 else 2 endif and !false"),
        new Scenario("vector-assignment", EntryPoint.MATH, "[a,b,c] = makeVec(1, if true then 2 else 3 endif, [4,5]); 1")
    );

    private ExpressionEvaluatorV2ProfileMain() {
    }

    public static void main(String[] args) {
        for (Scenario scenario : SCENARIOS) {
            System.out.println("Scenario: " + scenario.name());
            System.out.println("Input: " + scenario.input());
            ProfileReport report = profile(scenario);
            printTopDecisions(report, 8);
            System.out.println();
        }
    }

    private static ProfileReport profile(Scenario scenario) {
        ExpressionEvaluatorV2Lexer lexer = new ExpressionEvaluatorV2Lexer(CharStreams.fromString(scenario.input()));
        lexer.removeErrorListeners();
        lexer.addErrorListener(FAILING_ERROR_LISTENER);

        ExpressionEvaluatorV2Parser parser = new ExpressionEvaluatorV2Parser(new CommonTokenStream(lexer));
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

    private static String ruleName(ExpressionEvaluatorV2Parser parser, int decision) {
        int ruleIndex = parser.getATN().getDecisionState(decision).ruleIndex;
        return ExpressionEvaluatorV2Parser.ruleNames[ruleIndex];
    }

    private static String nestedDecisionAssignment(int depth) {
        String expression = "value0";
        for (int i = 1; i <= depth; i++) {
            expression = "if flag%d then %s else value%d endif".formatted(i, expression, i);
        }
        return "result = %s; 1 + 2 * 3".formatted(expression);
    }

    private enum EntryPoint {
        MATH,
        LOGICAL
    }

    private record Scenario(String name, EntryPoint entryPoint, String input) {
    }

    private record ProfileReport(ExpressionEvaluatorV2Parser parser, ParseInfo parseInfo) {
    }
}
