package com.runestone.expeval.grammar.language;
import com.runestone.expeval.internal.grammar.ExpressionEvaluatorParserFacade;

import com.runestone.expeval.perf.ExpressionEvaluatorScenarioCatalog;
import com.runestone.expeval.perf.ExpressionEvaluatorScenarioCatalog.EntryPoint;
import com.runestone.expeval.perf.ExpressionEvaluatorScenarioCatalog.Scenario;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;

class ExpressionEvaluatorCorpusCoverageTest {

    private final ExpressionEvaluatorParserFacade parserFacade = new ExpressionEvaluatorParserFacade();

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("corpusScenarios")
    void shouldParseVersionedCorpusInputs(String scenarioName, EntryPoint entryPoint, String input) {
        assertThatCode(() -> parse(entryPoint, input))
            .as("corpus input for scenario '%s' should parse", scenarioName)
            .doesNotThrowAnyException();
    }

    private void parse(EntryPoint entryPoint, String input) {
        switch (entryPoint) {
            case MATH -> parserFacade.parseMath(input);
            case LOGICAL -> parserFacade.parseLogical(input);
        }
    }

    private static Stream<Arguments> corpusScenarios() {
        return ExpressionEvaluatorScenarioCatalog.corpusScenarios().stream()
            .map(ExpressionEvaluatorCorpusCoverageTest::toArguments);
    }

    private static Arguments toArguments(Scenario scenario) {
        return Arguments.of(scenario.name(), scenario.entryPoint(), scenario.input());
    }
}
