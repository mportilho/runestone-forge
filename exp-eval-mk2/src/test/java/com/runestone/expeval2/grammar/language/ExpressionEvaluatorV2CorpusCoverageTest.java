package com.runestone.expeval2.internal.grammar;
import com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2ParserFacade;

import com.runestone.expeval2.perf.ExpressionEvaluatorV2ScenarioCatalog;
import com.runestone.expeval2.perf.ExpressionEvaluatorV2ScenarioCatalog.EntryPoint;
import com.runestone.expeval2.perf.ExpressionEvaluatorV2ScenarioCatalog.Scenario;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;

class ExpressionEvaluatorV2CorpusCoverageTest {

    private final ExpressionEvaluatorV2ParserFacade parserFacade = new ExpressionEvaluatorV2ParserFacade();

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
        return ExpressionEvaluatorV2ScenarioCatalog.corpusScenarios().stream()
            .map(ExpressionEvaluatorV2CorpusCoverageTest::toArguments);
    }

    private static Arguments toArguments(Scenario scenario) {
        return Arguments.of(scenario.name(), scenario.entryPoint(), scenario.input());
    }
}
