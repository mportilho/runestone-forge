package com.runestone.expeval_mk3.corpus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

final class ExpressionCaseLoader {

    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());

    private ExpressionCaseLoader() {
    }

    static List<ExpressionCase> loadAll() {
        Path root = corpusRoot();
        try (Stream<Path> files = Files.walk(root)) {
            List<Path> caseFiles = files
                    .filter(Files::isRegularFile)
                    .filter(ExpressionCaseLoader::isYamlFile)
                    .sorted()
                    .toList();
            List<ExpressionCase> cases = new ArrayList<>(caseFiles.size());
            for (Path caseFile : caseFiles) {
                cases.add(load(caseFile));
            }
            return List.copyOf(cases);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to scan expression corpus", exception);
        }
    }

    private static Path corpusRoot() {
        var resource = ExpressionCaseLoader.class.getClassLoader().getResource("corpus");
        if (resource == null) {
            throw new IllegalStateException("Expression corpus resource not found");
        }
        try {
            return Path.of(resource.toURI());
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("Invalid expression corpus resource URI", exception);
        }
    }

    private static ExpressionCase load(Path path) {
        try {
            JsonNode root = YAML.readTree(path.toFile());
            String id = requiredText(root, "id", path);
            CasePhase phase = CasePhase.from(requiredText(root, "phase", path));
            CaseKind kind = CaseKind.from(requiredText(root, "kind", path));
            String source = requiredSource(root, path);
            Set<CoverageTag> coverage = coverage(root, path);
            ExpectedOutcome expectedOutcome = expectedOutcome(root, phase, kind, path);
            return new ExpressionCase(id, phase, kind, source, coverage, expectedOutcome, root, path);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Invalid YAML in expression case: " + path, exception);
        }
    }

    private static Set<CoverageTag> coverage(JsonNode root, Path path) {
        JsonNode node = root.get("coverage");
        if (node == null || !node.isArray() || node.isEmpty()) {
            throw new IllegalArgumentException("Missing non-empty coverage array in " + path);
        }
        Set<CoverageTag> tags = new HashSet<>();
        for (JsonNode element : node) {
            if (!element.isTextual()) {
                throw new IllegalArgumentException("Coverage tags must be strings in " + path);
            }
            tags.add(CoverageTag.from(element.textValue()));
        }
        return Set.copyOf(tags);
    }

    private static ExpectedOutcome expectedOutcome(JsonNode root, CasePhase phase, CaseKind kind, Path path) {
        JsonNode expected = root.get("expected");
        if (kind == CaseKind.INVALID && phase == CasePhase.RUNTIME && expected != null && expected.has("runtimeError")) {
            JsonNode runtimeError = requiredObject(expected, "runtimeError", path);
            return new ExpectedRuntimeError(
                    requiredText(runtimeError, "type", path),
                    requiredText(runtimeError, "messageContains", path));
        }
        if (kind == CaseKind.INVALID) {
            JsonNode diagnostic = requiredObject(expected, "diagnostic", path);
            String category = requiredText(diagnostic, "category", path);
            String code = requiredText(diagnostic, "code", path);
            List<SourceSpan> spans = diagnosticSpans(diagnostic, category, code, path);
            return new ExpectedDiagnostic(
                    category,
                    code,
                    spans);
        }
        if (requiresResult(phase)) {
            JsonNode expectedObject = requiredObject(root, "expected", path);
            validateExpectedResult(expectedObject, path);
            return new ExpectedResult(
                    requiredText(expectedObject, "type", path),
                    expectedObject.get("result").deepCopy());
        }
        return NoExpectedOutcome.INSTANCE;
    }

    private static boolean requiresResult(CasePhase phase) {
        return phase == CasePhase.RUNTIME || phase == CasePhase.DIFFERENTIAL;
    }

    private static void validateExpectedResult(JsonNode expected, Path path) {
        String type = requiredText(expected, "type", path);
        JsonNode result = expected.get("result");
        if (result == null) {
            throw new IllegalArgumentException("Missing expected.result in " + path);
        }
        if ("NUMBER".equals(type) && !result.isTextual()) {
            throw new IllegalArgumentException("NUMBER expected.result must be a string in " + path);
        }
        if ("COLLECTION".equals(type)) {
            validateCollectionResult(result, path);
        }
    }

    private static void validateCollectionResult(JsonNode result, Path path) {
        if (!result.isArray()) {
            throw new IllegalArgumentException("COLLECTION expected.result must be an array in " + path);
        }
        for (JsonNode item : result) {
            String itemType = requiredText(item, "type", path);
            if ("NUMBER".equals(itemType)) {
                JsonNode value = item.get("value");
                if (value == null || !value.isTextual()) {
                    throw new IllegalArgumentException("NUMBER collection item value must be a string in " + path);
                }
            }
        }
    }

    private static JsonNode requiredObject(JsonNode root, String field, Path path) {
        if (root == null) {
            throw new IllegalArgumentException("Missing object containing " + field + " in " + path);
        }
        JsonNode node = root.get(field);
        if (node == null || !node.isObject()) {
            throw new IllegalArgumentException("Missing object field '" + field + "' in " + path);
        }
        return node;
    }

    private static List<SourceSpan> diagnosticSpans(JsonNode diagnostic, String category, String code, Path path) {
        JsonNode span = diagnostic.get("span");
        if (span == null) {
            if ("PARSE".equals(category)) {
                throw new IllegalArgumentException("Missing diagnostic.span for PARSE case in " + path);
            }
            return List.of();
        }
        if (!span.isObject()) {
            throw new IllegalArgumentException("Missing object field 'span' in " + path);
        }
        if ("PARSE".equals(category) && "TBD".equals(code)) {
            throw new IllegalArgumentException("PARSE diagnostic code must be stable in " + path);
        }
        return List.of(new SourceSpan(
                requiredInt(span, "offset", path),
                requiredInt(span, "endOffset", path),
                requiredInt(span, "line", path),
                requiredInt(span, "column", path)));
    }

    private static int requiredInt(JsonNode root, String field, Path path) {
        JsonNode node = Objects.requireNonNull(root, "root").get(field);
        if (node == null || !node.canConvertToInt()) {
            throw new IllegalArgumentException("Missing integer field '" + field + "' in " + path);
        }
        return node.intValue();
    }

    private static String requiredText(JsonNode root, String field, Path path) {
        JsonNode node = Objects.requireNonNull(root, "root").get(field);
        if (node == null || !node.isTextual() || node.textValue().isBlank()) {
            throw new IllegalArgumentException("Missing text field '" + field + "' in " + path);
        }
        return node.textValue();
    }

    private static String requiredSource(JsonNode root, Path path) {
        JsonNode node = Objects.requireNonNull(root, "root").get("source");
        if (node == null || !node.isTextual()) {
            throw new IllegalArgumentException("Missing text field 'source' in " + path);
        }
        return node.textValue();
    }

    private static boolean isYamlFile(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.endsWith(".yml") || fileName.endsWith(".yaml");
    }
}
