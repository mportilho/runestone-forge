package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.support.DocumentationPaths;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

final class ExecutableDocumentationTest {

    private static final Pattern EXECUTABLE_EXAMPLE = Pattern.compile(
            "<!--\\s*runestone-example:\\s*([A-Z]+)=(.*?)\\s*-->\\s*```runestone\\R(.*?)\\R```",
            Pattern.DOTALL);

    @Test
    void everyMarkedExpressionExampleCompilesAndProducesItsDocumentedResult() throws IOException {
        List<ExpressionExample> examples = loadExamples();
        ExpressionEngine engine = ExpressionEngine.builder().build();
        ExpressionEnvironment environment = ExpressionEnvironment.standard();

        assertThat(examples).isNotEmpty();
        for (ExpressionExample example : examples) {
            Object result = engine.compileOrThrow(example.source(), environment).asResult().compute();
            assertThat(result)
                    .as("%s:%d%n%s", example.document(), example.line(), example.source())
                    .isEqualTo(example.expectedValue());
        }
    }

    private static List<ExpressionExample> loadExamples() throws IOException {
        Path root = DocumentationPaths.moduleRoot();
        List<Path> documents = new ArrayList<>();
        documents.add(root.resolve("README.md"));
        try (Stream<Path> files = Files.walk(root.resolve("docs/reference"))) {
            files.filter(path -> path.toString().endsWith(".md")).forEach(documents::add);
        }
        try (Stream<Path> files = Files.walk(root.resolve("docs/guides"))) {
            files.filter(path -> path.toString().endsWith(".md")).forEach(documents::add);
        }

        List<ExpressionExample> examples = new ArrayList<>();
        for (Path document : documents) {
            String markdown = Files.readString(document);
            Matcher matcher = EXECUTABLE_EXAMPLE.matcher(markdown);
            while (matcher.find()) {
                int line = 1 + Math.toIntExact(markdown.substring(0, matcher.start()).lines().count());
                examples.add(new ExpressionExample(
                        root.relativize(document).toString(),
                        line,
                        matcher.group(3).strip(),
                        ExpectedType.valueOf(matcher.group(1)),
                        matcher.group(2).strip()));
            }
        }
        return List.copyOf(examples);
    }

    private record ExpressionExample(
            String document,
            int line,
            String source,
            ExpectedType expectedType,
            String expected) {

        private Object expectedValue() {
            return expectedType.parse(expected);
        }
    }

    private enum ExpectedType {
        NUMBER {
            @Override
            Object parse(String value) {
                return new BigDecimal(value);
            }
        },
        BOOLEAN {
            @Override
            Object parse(String value) {
                return Boolean.valueOf(value);
            }
        },
        DATE {
            @Override
            Object parse(String value) {
                return LocalDate.parse(value);
            }
        };

        abstract Object parse(String value);
    }
}
