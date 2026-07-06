package com.runestone.expeval_mk3.internal.ast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.runestone.expeval_mk3.internal.source.SourceSpan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SemanticAstCorpusGateTest {

    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());
    private static final List<String> REPRESENTATIVE_METADATA_SOURCES = List.of(
            "x := 1; x",
            "[a, b] := pair; a",
            "if(true, 1, 0)",
            "currDate",
            "sum(1, 2)",
            "(1)",
            "1 in [1, 2]",
            "1 between 0 and 2",
            "x ?? 1 ?? 2",
            "value%!",
            "!flag",
            "obj.name.method(1)[0][*][?(true)]",
            "obj.*",
            "items..sum(1, 2)",
            "items..map(@ -> @)");

    @Test
    @DisplayName("every valid expression case builds a round-trippable AST")
    void everyValidExpressionCaseBuildsRoundTrippableAst() {
        List<CorpusCase> cases = validCases();
        assertThat(cases).isNotEmpty();
        for (CorpusCase expressionCase : cases) {
            ExpressionFileNode ast = AstTestSupport.build(expressionCase.source());
            String printed = AstPrettyPrinter.print(ast);
            ExpressionFileNode reparsed = AstTestSupport.build(printed);

            assertThat(AstStructuralEquality.equals(ast, reparsed))
                    .as("%s printed as %s", expressionCase.path(), printed)
                    .isTrue();
        }
    }

    @Test
    @DisplayName("every corpus AST node and navigation link has deterministic metadata")
    void everyCorpusAstNodeAndNavigationLinkHasDeterministicMetadata() {
        List<CorpusCase> cases = validCases();
        assertThat(cases).isNotEmpty();
        Set<Class<?>> observedTypes = new HashSet<>();
        for (CorpusCase expressionCase : cases) {
            assertDeterministicMetadata(expressionCase.path().toString(), expressionCase.source(), observedTypes);
        }
        for (String source : REPRESENTATIVE_METADATA_SOURCES) {
            assertDeterministicMetadata(source, source, observedTypes);
        }
        assertThat(observedTypes).containsAll(concreteAstNodeTypes());
    }

    private static void assertDeterministicMetadata(String description, String source, Set<Class<?>> observedTypes) {
        ExpressionFileNode ast = AstTestSupport.build(source);
        ExpressionFileNode rebuilt = AstTestSupport.build(source);
        List<AstNode> nodes = AstTestSupport.flatten(ast);
        nodes.stream().map(Object::getClass).forEach(observedTypes::add);

        assertThat(nodes)
                .as("%s", description)
                .allSatisfy(node -> {
                    assertThat(node.id()).isNotEqualTo(NodeId.UNASSIGNED);
                    assertThat(node.sourceSpan().offset()).isBetween(0, source.length());
                    assertThat(node.sourceSpan().endOffset()).isBetween(node.sourceSpan().offset(), source.length());
                    assertThat(node.sourceSpan().line()).isPositive();
                    assertThat(node.sourceSpan().column()).isPositive();
                });
        assertThat(nodes.stream().map(node -> node.id().value()).toList())
                .as("%s", description)
                .containsExactlyElementsOf(Stream.iterate(0, index -> index + 1)
                        .limit(nodes.size())
                        .toList());
        assertThat(sourceSpans(ast))
                .as("%s", description)
                .containsExactlyElementsOf(sourceSpans(rebuilt));
    }

    private static List<SourceSpan> sourceSpans(ExpressionFileNode ast) {
        return AstTestSupport.flatten(ast).stream()
                .map(AstNode::sourceSpan)
                .toList();
    }

    private static Set<Class<?>> concreteAstNodeTypes() {
        Set<Class<?>> visited = new HashSet<>();
        Set<Class<?>> concreteTypes = new HashSet<>();
        Queue<Class<?>> pending = new ArrayDeque<>();
        pending.add(AstNode.class);
        while (!pending.isEmpty()) {
            Class<?> type = pending.remove();
            if (!visited.add(type)) {
                continue;
            }
            if (AstNode.class.isAssignableFrom(type) && !type.isInterface()) {
                concreteTypes.add(type);
            }
            Class<?>[] permittedSubclasses = type.getPermittedSubclasses();
            if (permittedSubclasses != null) {
                for (Class<?> permittedSubclass : permittedSubclasses) {
                    pending.add(permittedSubclass);
                }
            }
        }
        return Set.copyOf(concreteTypes);
    }

    private static List<CorpusCase> validCases() {
        try (Stream<Path> files = Files.walk(corpusRoot())) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(SemanticAstCorpusGateTest::isYamlFile)
                    .map(SemanticAstCorpusGateTest::load)
                    .filter(CorpusCase::valid)
                    .filter(CorpusCase::etapa2Relevant)
                    .sorted()
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to scan expression corpus", exception);
        }
    }

    private static Path corpusRoot() {
        var resource = SemanticAstCorpusGateTest.class.getClassLoader().getResource("corpus");
        if (resource == null) {
            throw new IllegalStateException("Expression corpus resource not found");
        }
        try {
            return Path.of(resource.toURI());
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("Invalid expression corpus resource URI", exception);
        }
    }

    private static CorpusCase load(Path path) {
        try {
            JsonNode root = YAML.readTree(path.toFile());
            return new CorpusCase(
                    path,
                    requiredText(root, "phase", path),
                    requiredText(root, "kind", path),
                    requiredText(root, "source", path));
        } catch (IOException exception) {
            throw new IllegalArgumentException("Invalid YAML in expression case: " + path, exception);
        }
    }

    private static String requiredText(JsonNode root, String field, Path path) {
        JsonNode node = Objects.requireNonNull(root, "root").get(field);
        if (node == null || !node.isTextual()) {
            throw new IllegalArgumentException("Missing text field '" + field + "' in " + path);
        }
        return node.textValue();
    }

    private static boolean isYamlFile(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.endsWith(".yml") || fileName.endsWith(".yaml");
    }

    private record CorpusCase(Path path, String phase, String kind, String source) implements Comparable<CorpusCase> {

        private CorpusCase {
            Objects.requireNonNull(path, "path");
            Objects.requireNonNull(phase, "phase");
            Objects.requireNonNull(kind, "kind");
            Objects.requireNonNull(source, "source");
        }

        boolean valid() {
            return "valid".equals(kind);
        }

        boolean etapa2Relevant() {
            return switch (phase) {
                case "parser", "semantics", "runtime", "differential" -> true;
                case "migration" -> false;
                default -> throw new IllegalArgumentException("Unknown corpus phase: " + phase);
            };
        }

        @Override
        public int compareTo(CorpusCase other) {
            return path.compareTo(other.path);
        }
    }
}
