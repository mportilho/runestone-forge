package com.runestone.expeval_mk3.internal.diagnostics;

import com.runestone.expeval_mk3.api.DiagnosticCategory;
import com.runestone.expeval_mk3.api.DiagnosticSeverity;
import com.runestone.expeval_mk3.api.ExpressionDiagnostic;
import com.runestone.expeval_mk3.api.SourceSpan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiagnosticContractTest {

    private static final SourceSpan SPAN = new SourceSpan(2, 4, 1, 3);

    @ParameterizedTest(name = "{0}")
    @EnumSource(DiagnosticCode.class)
    void everyRegisteredCodeCanBeEmittedOnlyWithItsDeclaredContract(DiagnosticCode code) {
        SourceSpan span = code.sourceSpanPolicy() == SourceSpanPolicy.FORBIDDEN ? null : SPAN;
        String suggestion = code.suggestionPolicy() == SuggestionPolicy.REQUIRED ? "Apply the local correction" : null;

        ExpressionDiagnostic diagnostic = ExpressionDiagnostics.create(code, "message", span, suggestion);

        assertThat(diagnostic.code()).isEqualTo(code.code());
        assertThat(diagnostic.category()).isEqualTo(code.category());
        assertThat(diagnostic.severity()).isEqualTo(code.severity());
        assertThat(diagnostic.primarySpan()).isEqualTo(Optional.ofNullable(span));
        assertThat(diagnostic.suggestion()).isEqualTo(Optional.ofNullable(suggestion));
    }

    @Test
    void registeredPoliciesAreEnforcedAtTheProductionFactory() {
        assertThatThrownBy(() -> ExpressionDiagnostics.create(
                        DiagnosticCode.SEMANTIC_UNKNOWN_SYMBOL, "message", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("requires a primary source span");
        assertThatThrownBy(() -> ExpressionDiagnostics.create(
                        DiagnosticCode.RUNTIME_INVALID_EXTERNAL_INPUT, "message", SPAN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("forbids a primary source span");
        assertThatThrownBy(() -> ExpressionDiagnostics.create(
                        DiagnosticCode.SEMANTIC_NULLABLE_RESULT_NOT_ALLOWED, "message", SPAN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("requires a suggestion");
    }

    @Test
    void everyRegisteredCodeHasAParticularProductionEmitter() throws IOException {
        Path sourceRoot = Path.of(System.getProperty("user.dir"), "src/main/java");
        if (!Files.isDirectory(sourceRoot)) {
            sourceRoot = Path.of(System.getProperty("user.dir"), "exp-mk3/src/main/java");
        }
        String productionSources;
        try (Stream<Path> files = Files.walk(sourceRoot)) {
            productionSources = files
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !path.getFileName().toString().equals("DiagnosticCode.java"))
                    .filter(path -> !path.getFileName().toString().equals("ExpressionDiagnostics.java"))
                    .map(DiagnosticContractTest::readSource)
                    .reduce("", (left, right) -> left + '\n' + right);
        }

        for (DiagnosticCode code : DiagnosticCode.values()) {
            assertThat(productionSources).as("production emitter for %s", code.code()).contains(code.name());
        }
    }

    @Test
    void everyRegisteredCodeHasAnEmitterExpectationInTheTestSuite() throws IOException {
        Path testRoot = Path.of(System.getProperty("user.dir"), "src/test");
        if (!Files.isDirectory(testRoot)) {
            testRoot = Path.of(System.getProperty("user.dir"), "exp-mk3/src/test");
        }
        String testSources;
        try (Stream<Path> files = Files.walk(testRoot)) {
            testSources = files
                    .filter(Files::isRegularFile)
                    .filter(path -> !path.getFileName().toString().equals("DiagnosticContractTest.java"))
                    .map(DiagnosticContractTest::readSource)
                    .reduce("", (left, right) -> left + '\n' + right);
        }

        for (DiagnosticCode code : DiagnosticCode.values()) {
            assertThat(testSources).as("emitter expectation for %s", code.code()).contains(code.code());
        }
    }

    @Test
    void canonicalOrderUsesSpanPresenceOffsetsSeverityCategoryCodeAndEndOffset() {
        ExpressionDiagnostic unpositioned = ExpressionDiagnostic.error(
                DiagnosticCategory.RUNTIME, "A", "message", null);
        ExpressionDiagnostic later = ExpressionDiagnostic.error(
                DiagnosticCategory.PARSE, "A", "message", new SourceSpan(8, 9, 1, 9));
        ExpressionDiagnostic warning = ExpressionDiagnostic.warning(
                DiagnosticCategory.PARSE, "A", "message", new SourceSpan(2, 3, 1, 3));
        ExpressionDiagnostic category = ExpressionDiagnostic.error(
                DiagnosticCategory.SEMANTIC, "A", "message", new SourceSpan(2, 3, 1, 3));
        ExpressionDiagnostic code = ExpressionDiagnostic.error(
                DiagnosticCategory.PARSE, "B", "message", new SourceSpan(2, 3, 1, 3));
        ExpressionDiagnostic end = ExpressionDiagnostic.error(
                DiagnosticCategory.PARSE, "A", "message", new SourceSpan(2, 4, 1, 3));
        ExpressionDiagnostic first = ExpressionDiagnostic.error(
                DiagnosticCategory.PARSE, "A", "message", new SourceSpan(2, 3, 1, 3));

        assertThat(ExpressionDiagnostics.canonicalCopy(List.of(
                unpositioned, later, warning, category, code, end, first)))
                .containsExactly(first, end, code, category, warning, later, unpositioned);
    }

    private static String readSource(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read " + path, exception);
        }
    }
}
