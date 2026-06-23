package com.runestone.expeval.perf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ExpressionEvaluatorScenarioCatalog {

    private static final String CORPUS_INDEX_RESOURCE = "com/runestone/expeval/grammar/language/perf/corpus/index.tsv";

    private static final List<Scenario> SYNTHETIC_SCENARIOS = List.of(
        new Scenario("math-flat-assignment", EntryPoint.MATH, "a = foo; 1", Origin.SYNTHETIC),
        new Scenario("math-nested-decision-assignment", EntryPoint.MATH, nestedDecisionAssignment(8), Origin.SYNTHETIC),
        new Scenario("logical-mixed-comparison", EntryPoint.LOGICAL, "if true then 10 else 20 endif > if false then 1 else 2 endif and !false", Origin.SYNTHETIC),
        new Scenario("vector-assignment", EntryPoint.MATH, "[a,b,c] = makeVec(1, if true then 2 else 3 endif, [4,5]); 1", Origin.SYNTHETIC)
    );

    private static final List<Scenario> CORPUS_SCENARIOS = loadCorpusScenarios();

    private static final Map<String, Scenario> SCENARIOS_BY_NAME = Stream
        .concat(SYNTHETIC_SCENARIOS.stream(), CORPUS_SCENARIOS.stream())
        .collect(Collectors.toUnmodifiableMap(Scenario::name, Function.identity()));

    private ExpressionEvaluatorScenarioCatalog() {
    }

    public static List<Scenario> syntheticScenarios() {
        return SYNTHETIC_SCENARIOS;
    }

    public static List<Scenario> corpusScenarios() {
        return CORPUS_SCENARIOS;
    }

    public static Scenario requireScenario(String name) {
        Scenario scenario = SCENARIOS_BY_NAME.get(name);
        if (scenario == null) {
            throw new IllegalArgumentException("unknown scenario: " + name);
        }
        return scenario;
    }

    private static List<Scenario> loadCorpusScenarios() {
        try (BufferedReader reader = openResource(CORPUS_INDEX_RESOURCE)) {
            return reader.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .map(ExpressionEvaluatorScenarioCatalog::parseCorpusRow)
                .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("failed to load corpus index: " + CORPUS_INDEX_RESOURCE, e);
        }
    }

    private static Scenario parseCorpusRow(String row) {
        String[] columns = row.split("\t");
        if (columns.length != 3) {
            throw new IllegalStateException("invalid corpus index row: " + row);
        }

        EntryPoint entryPoint = EntryPoint.valueOf(columns[1]);
        String input = readResource(columns[2]);
        return new Scenario(columns[0], entryPoint, input.strip(), Origin.CORPUS);
    }

    private static String readResource(String resourcePath) {
        try (BufferedReader reader = openResource(resourcePath)) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            throw new UncheckedIOException("failed to load scenario resource: " + resourcePath, e);
        }
    }

    private static BufferedReader openResource(String resourcePath) throws IOException {
        InputStream inputStream = ExpressionEvaluatorScenarioCatalog.class.getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IOException("resource not found: " + resourcePath);
        }
        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    private static String nestedDecisionAssignment(int depth) {
        String expression = "value0";
        for (int i = 1; i <= depth; i++) {
            expression = "if flag%d then %s else value%d endif".formatted(i, expression, i);
        }
        return "result = %s; 1 + 2 * 3".formatted(expression);
    }

    public enum EntryPoint {
        MATH,
        LOGICAL
    }

    public enum Origin {
        SYNTHETIC,
        CORPUS
    }

    public record Scenario(String name, EntryPoint entryPoint, String input, Origin origin) {
    }
}
