package com.runestone.expeval2.grammar.language;

import com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2ParserFacade;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;

class ExpressionEvaluatorV2GrammarCoverageTest {

    private final ExpressionEvaluatorV2ParserFacade parserFacade = new ExpressionEvaluatorV2ParserFacade();

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("validMathInputs")
    void shouldParseCoveredMathInputs(String scenario, String input) {
        assertThatCode(() -> parserFacade.parseMath(input))
            .as("math input for scenario '%s' should parse: %s", scenario, input)
            .doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("validLogicalInputs")
    void shouldParseCoveredLogicalInputs(String scenario, String input) {
        assertThatCode(() -> parserFacade.parseLogical(input))
            .as("logical input for scenario '%s' should parse: %s", scenario, input)
            .doesNotThrowAnyException();
    }

    private static Stream<Arguments> validMathInputs() {
        return Stream.of(
            Arguments.of("numeric literal and additive expression", "42 + 7"),
            Arguments.of("typed numeric references", "<number>principal + rate"),
            Arguments.of("zero argument function reference", "makeNumber()"),
            Arguments.of("function with comma separated mixed arguments", "metric(1, true, 2024-12-31, 12:30, 2024-12-31T12:30, \"txt\", [2])"),
            Arguments.of("function with semicolon separated arguments", "score(1; false; \"x\")"),
            Arguments.of("parenthesized arithmetic precedence", "(1 + 2) * 3"),
            Arguments.of("square root function", "sqrt(16)"),
            Arguments.of("modulus bars", "|1 - 3|"),
            Arguments.of("postfix factorial and percent", "5!%"),
            Arguments.of("exponentiation with unary operand", "2 ^ -3"),
            Arguments.of("root operator chain", "64 root 4 root 2"),
            Arguments.of("multiplication division and modulo", "10 * 3 / 2 mod 4"),
            Arguments.of("numeric if then elsif else decision", "if true then 1 elsif false then 2 else 3 endif"),
            Arguments.of("numeric functional decision", "if(true, 1, false, 2, 3)"),
            Arguments.of("assignments with every explicit cast type", "boolCast = <bool>(flag); numCast = <number>(amount); textCast = <text>(label); dateCast = <date>(createdOn); timeCast = <time>(createdAt); dateTimeCast = <datetime>(timestamp); vectorCast = <vector>(items); 1"),
            Arguments.of("generic assignment using identifier reference", "target = foo; 1"),
            Arguments.of("generic assignment using function reference", "target = resolve(); 1"),
            Arguments.of("generic assignment with if then elsif else", "target = if true then foo elsif false then bar else baz endif; 1"),
            Arguments.of("generic assignment with functional decision", "target = if(true, foo, false, bar, baz); 1"),
            Arguments.of("scalar assignment with math expression", "answer = 1 + 2 * 3; 1"),
            Arguments.of("scalar assignment with logical expression", "flag = true and !false; 1"),
            Arguments.of("scalar assignment with string constant", "textValue = \"alpha\"; 1"),
            Arguments.of("string decision assignment", "textValue = if true then \"a\" elsif false then \"b\" else \"c\" endif; 1"),
            Arguments.of("string functional decision assignment", "textValue = if(true, \"a\", false, \"b\", \"c\"); 1"),
            Arguments.of("date constant assignment", "dateValue = 2024-12-31; 1"),
            Arguments.of("date current value assignment", "dateValue = currDate; 1"),
            Arguments.of("typed date reference assignment", "dateValue = <date>holiday; 1"),
            Arguments.of("date decision assignment", "dateValue = if true then 2024-12-31 elsif false then 2024-12-30 else currDate endif; 1"),
            Arguments.of("date functional decision assignment", "dateValue = if(true, 2024-12-31, false, 2024-12-30, currDate); 1"),
            Arguments.of("time constant assignment", "timeValue = 12:30:45; 1"),
            Arguments.of("time current value assignment", "timeValue = currTime; 1"),
            Arguments.of("typed time reference assignment", "timeValue = <time>meetingTime; 1"),
            Arguments.of("time decision assignment", "timeValue = if true then 12:30 elsif false then 13:45 else currTime endif; 1"),
            Arguments.of("time functional decision assignment", "timeValue = if(true, 12:30, false, 13:45, currTime); 1"),
            Arguments.of("datetime constant with offset assignment", "dateTimeValue = 2024-12-31T12:30:45-03:00; 1"),
            Arguments.of("datetime current value assignment", "dateTimeValue = currDateTime; 1"),
            Arguments.of("typed datetime reference assignment", "dateTimeValue = <datetime>lastUpdatedAt; 1"),
            Arguments.of("datetime decision assignment", "dateTimeValue = if true then 2024-12-31T12:30 elsif false then 2024-12-30T08:15 else currDateTime endif; 1"),
            Arguments.of("datetime functional decision assignment", "dateTimeValue = if(true, 2024-12-31T12:30, false, 2024-12-30T08:15, currDateTime); 1"),
            Arguments.of("typed vector reference assignment", "items = <vector>payload; 1"),
            Arguments.of("vector literal with all entity families", "items = [1, true, 2024-12-31, 12:30, 2024-12-31T12:30, \"x\", [2]]; 1"),
            Arguments.of("vector decision assignment", "items = if true then [1] elsif false then [2] else [3] endif; 1"),
            Arguments.of("vector functional decision assignment", "items = if(true, [1], false, [2], [3]); 1"),
            Arguments.of("destructuring assignment from vector literal", "[left,right] = [1,2]; 1"),
            Arguments.of("destructuring assignment from function", "[left,right] = makeVec(); 1")
        );
    }

    private static Stream<Arguments> validLogicalInputs() {
        return Stream.of(
            Arguments.of("logical constant", "true"),
            Arguments.of("typed logical reference", "<bool>enabled"),
            Arguments.of("logical function reference", "isReady()"),
            Arguments.of("logical if then elsif else decision", "if true then false elsif false then true else false endif"),
            Arguments.of("logical functional decision", "if(true, false, false, true, false)"),
            Arguments.of("logical not with tilde", "~true"),
            Arguments.of("logical not with exclamation", "!false"),
            Arguments.of("parenthesized or and expression", "(true or false) and true"),
            Arguments.of("bitwise boolean operator chain", "true nand false xor true xnor false nor true"),
            Arguments.of("boolean comparison", "true != false"),
            Arguments.of("math comparison", "1 + 2 > 3 * 4"),
            Arguments.of("string comparison", "<text>left = formatName()"),
            Arguments.of("date comparison", "<date>startDate <= currDate"),
            Arguments.of("time comparison", "<time>startTime < currTime"),
            Arguments.of("datetime comparison", "<datetime>startAt >= currDateTime"),
            Arguments.of("logical input with leading assignments", "value = if true then foo else bar endif; items = [1, true]; 1 < 2 and isReady()")
        );
    }
}
