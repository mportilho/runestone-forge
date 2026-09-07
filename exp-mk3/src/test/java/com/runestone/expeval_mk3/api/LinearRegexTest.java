package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LinearRegexTest {

    private final ExpressionEngine engine = ExpressionEngine.builder().build();

    @Test
    void literalRegexOperatorsUseTheSupportedLinearSubset() {
        assertThat(engine.compileOrThrow("\"ABC-1234\" =~ \"^[A-Z]{3}-\\\\d{4}$\"", ExpressionEnvironment.standard())
                .asLogical()
                .compute()).isTrue();
        assertThat(engine.compileOrThrow("\"abc\" !~ \"^z+$\"", ExpressionEnvironment.standard())
                .asLogical()
                .compute()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"a(?=b)", "(?<=a)b", "(a)\\1"})
    void incompatibleLiteralRegexConstructsProduceAPositionedSemanticDiagnostic(String pattern) {
        String escapedPattern = pattern.replace("\\", "\\\\");
        String source = "\"ab\" =~ \"" + escapedPattern + "\"";
        int patternStart = source.indexOf('"', source.indexOf("=~") + 2);

        assertThat(engine.compile(source, ExpressionEnvironment.standard()))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure -> {
                    assertThat(failure.diagnostics()).singleElement().satisfies(diagnostic -> {
                        assertThat(diagnostic.code()).isEqualTo("SEMANTIC_REGEX_PATTERN_INVALID");
                        assertThat(diagnostic.primarySpan()).contains(new SourceSpan(
                                patternStart, source.length(), 1, patternStart + 1));
                    });
                });
    }

    @Test
    void dynamicRegexBuiltInsCompilePatternsAtRuntime() {
        ExpressionEnvironment environment = dynamicRegexEnvironment();

        assertThat(engine.compileOrThrow("replaceAll(text, pattern, \"<$1>\")", environment)
                .asResult()
                .compute(Map.of("text", "a1b22", "pattern", "(\\d+)")))
                .isEqualTo("a<1>b<22>");
        assertThat(engine.compileOrThrow("split(text, pattern)", environment)
                .asResult()
                .compute(Map.of("text", "a,b,", "pattern", ",")))
                .isEqualTo(List.of("a", "b", ""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"replaceAll(text, \"a(?=b)\", \"x\")", "split(text, \"(a)\\\\1\")"})
    void incompatibleLiteralBuiltInPatternsProduceADiagnosticAtThePatternSpan(String source) {
        int patternStart = source.indexOf('"');
        int patternEnd = source.indexOf('"', patternStart + 1) + 1;

        assertThat(engine.compile(source, dynamicRegexEnvironment()))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure -> {
                    assertThat(failure.diagnostics()).singleElement().satisfies(diagnostic -> {
                        assertThat(diagnostic.code()).isEqualTo("SEMANTIC_REGEX_PATTERN_INVALID");
                        assertThat(diagnostic.primarySpan()).contains(new SourceSpan(
                                patternStart, patternEnd, 1, patternStart + 1));
                    });
                });
    }

    @ParameterizedTest
    @ValueSource(strings = {"replaceAll(text, pattern, \"x\")", "split(text, pattern)"})
    void invalidDynamicRegexProducesARuntimeRegexDiagnosticAtTheCallSpan(String source) {
        ResultExpression expression = engine.compileOrThrow(source, dynamicRegexEnvironment()).asResult();

        assertThatThrownBy(() -> expression.compute(Map.of("text", "ab", "pattern", "a(?=b)")))
                .isInstanceOf(ExpressionExecutionException.class)
                .satisfies(thrown -> {
                    ExpressionDiagnostic diagnostic = ((ExpressionExecutionException) thrown).diagnostic();
                    assertThat(diagnostic.code()).isEqualTo("RUNTIME_REGEX_PATTERN_INVALID");
                    assertThat(diagnostic.primarySpan()).contains(new SourceSpan(0, source.length(), 1, 1));
                });
    }

    @Test
    void customOverloadsWithRegexBuiltInNamesRemainOrdinaryFunctions() throws NoSuchMethodException {
        Method method = LinearRegexTest.class.getDeclaredMethod("customSplit", String.class);
        FunctionDescriptor customSplit = FunctionDescriptor.fromMethod(
                "split", method, List.of(ScalarType.STRING), ScalarType.STRING, FunctionPurity.FOLDABLE);
        ExpressionEnvironment environment = ExpressionEnvironment.builder().function(customSplit).build();

        assertThat(engine.compileOrThrow("split(\"abc\")", environment).asResult().compute())
                .isEqualTo("custom:abc");
    }

    public static String customSplit(String value) {
        return "custom:" + value;
    }

    private static ExpressionEnvironment dynamicRegexEnvironment() {
        return ExpressionEnvironment.builder()
                .externalSymbol("text", "", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("pattern", "", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
    }
}
