package com.runestone.expeval_mk3.corpus;

import com.fasterxml.jackson.databind.JsonNode;
import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.MapType;
import com.runestone.expeval_mk3.api.ObjectType;
import com.runestone.expeval_mk3.api.ScalarType;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds the {@code ExpressionEnvironment} and the compute-time input map an {@link ExpressionCase}
 * declares, from its raw YAML {@code environment}/{@code inputs} nodes. Shared by every test that needs
 * to reproduce a corpus case's fixture rather than only its expected outcome, so this concern lives here
 * instead of being reached into on whichever test class happened to need it first.
 */
final class ExpressionCaseEnvironments {

    private ExpressionCaseEnvironments() {
    }

    static ExpressionEnvironment environment(ExpressionCase expressionCase) {
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder();
        JsonNode environment = expressionCase.root().get("environment");
        applyEnvironmentFields(builder, environment);
        applyExternalSymbols(builder, environment == null ? null : environment.get("symbols"));
        return builder.build();
    }

    static Map<String, Object> inputs(ExpressionCase expressionCase) {
        JsonNode inputs = expressionCase.root().get("inputs");
        if (inputs == null) {
            return Map.of();
        }
        Map<String, Object> values = new LinkedHashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = inputs.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> input = fields.next();
            JsonNode value = input.getValue();
            values.put(input.getKey(), typedValue(value.get("type").textValue(), value));
        }
        return Map.copyOf(values);
    }

    private static void applyEnvironmentFields(ExpressionEnvironment.Builder builder, JsonNode environment) {
        if (environment == null) {
            return;
        }
        JsonNode maxMaterializedSize = environment.get("maxMaterializedSize");
        if (maxMaterializedSize != null) {
            builder.maxMaterializedSize(maxMaterializedSize.intValue());
        }
        JsonNode maxFactorialInput = environment.get("maxFactorialInput");
        if (maxFactorialInput != null) {
            builder.maxFactorialInput(maxFactorialInput.intValue());
        }
        JsonNode maxCurrentItemDepth = environment.get("maxCurrentItemDepth");
        if (maxCurrentItemDepth != null) {
            builder.maxCurrentItemDepth(maxCurrentItemDepth.intValue());
        }
        JsonNode mathContext = environment.get("mathContext");
        if (mathContext != null) {
            builder.mathContext(new MathContext(
                    mathContext.get("precision").intValue(),
                    RoundingMode.valueOf(mathContext.get("roundingMode").textValue())));
        }
    }

    private static void applyExternalSymbols(ExpressionEnvironment.Builder builder, JsonNode symbols) {
        if (symbols == null) {
            return;
        }
        Iterator<Map.Entry<String, JsonNode>> fields = symbols.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> symbol = fields.next();
            JsonNode declaration = symbol.getValue();
            String type = declaration.get("type").textValue();
            if (type.startsWith("OBJECT_")) {
                applyJavaObjectSymbol(builder, symbol.getKey(), type, declaration.get("default"));
                continue;
            }
            builder.externalSymbol(
                    symbol.getKey(),
                    expressionType(type),
                    typedValue(type, declaration.get("default")),
                    ExternalSymbolOverwritePolicy.valueOf(declaration.get("overwritePolicy").textValue()));
        }
    }

    private static void applyJavaObjectSymbol(
            ExpressionEnvironment.Builder builder, String symbolName, String type, JsonNode declaration) {
        if ("OBJECT_COLLECTION_METHOD_PROVIDER".equals(type)) {
            // This provider declares methods, not wildcard children, so it reads no child list.
            builder
                    .registerJavaTypeMethod(ExpressionCorpusExecutionTest.CollectionMethodProvider.class, "itens")
                    .registerJavaTypeMethod(ExpressionCorpusExecutionTest.CollectionMethodProvider.class, "valores")
                    .registerJavaTypeMethod(ExpressionCorpusExecutionTest.CollectionMethodProvider.class, "mapa")
                    .externalSymbol(
                            symbolName,
                            new ObjectType(ExpressionCorpusExecutionTest.CollectionMethodProvider.class.getName()),
                            new ExpressionCorpusExecutionTest.CollectionMethodProvider(),
                            ExternalSymbolOverwritePolicy.FIXED);
            return;
        }
        if ("OBJECT_POST_FIXED_CONTRACT_PROVIDER".equals(type)) {
            builder
                    .registerJavaType(ExpressionCorpusExecutionTest.PostFixedContractProvider.class)
                    .externalSymbol(
                            symbolName,
                            new ObjectType(ExpressionCorpusExecutionTest.PostFixedContractProvider.class.getName()),
                            new ExpressionCorpusExecutionTest.PostFixedContractProvider(),
                            ExternalSymbolOverwritePolicy.FIXED);
            return;
        }
        if ("OBJECT_NAVIGATION_CONTRACT_PROVIDER".equals(type)) {
            builder
                    .registerJavaType(ExpressionCorpusExecutionTest.NavigationContractProvider.class)
                    .registerJavaTypeMethod(ExpressionCorpusExecutionTest.NavigationContractProvider.class, "describe")
                    .registerJavaTypeMethod(ExpressionCorpusExecutionTest.NavigationContractProvider.class, "fail")
                    .registerJavaTypeMethod(
                            ExpressionCorpusExecutionTest.NavigationContractProvider.class, "echo", BigDecimal.class)
                    .registerJavaTypeMethod(ExpressionCorpusExecutionTest.NavigationContractProvider.class, "attributes")
                    .registerJavaTypeMethod(ExpressionCorpusExecutionTest.CollectionMethodProvider.class, "itens")
                    .externalSymbol(
                            symbolName,
                            new ObjectType(ExpressionCorpusExecutionTest.NavigationContractProvider.class.getName()),
                            new ExpressionCorpusExecutionTest.NavigationContractProvider(),
                            ExternalSymbolOverwritePolicy.FIXED);
            return;
        }
        List<String> wildcardChildren = textList(declaration.get("wildcardChildren"));
        boolean ordered = declaration.path("ordered").asBoolean(true);
        if ("OBJECT_WILDCARD_CHILD_PROVIDER".equals(type)) {
            registerWildcardChildProvider(builder, symbolName, wildcardChildren, ordered);
        } else if ("OBJECT_FAILING_WILDCARD_CHILD_PROVIDER".equals(type)) {
            builder
                    .registerJavaTypeWildcardChildren(
                            ExpressionCorpusExecutionTest.FailingWildcardChildProvider.class,
                            wildcardChildren.toArray(String[]::new))
                    .externalSymbol(
                            symbolName,
                            new ObjectType(ExpressionCorpusExecutionTest.FailingWildcardChildProvider.class.getName()),
                            new ExpressionCorpusExecutionTest.FailingWildcardChildProvider(),
                            ExternalSymbolOverwritePolicy.FIXED);
        } else {
            throw new IllegalArgumentException("Unsupported corpus object type: " + type);
        }
    }

    private static void registerWildcardChildProvider(
            ExpressionEnvironment.Builder builder,
            String symbolName,
            List<String> wildcardChildren,
            boolean ordered) {
        if (ordered) {
            builder.registerJavaTypeWildcardChildren(
                    ExpressionCorpusExecutionTest.WildcardChildProvider.class, wildcardChildren.toArray(String[]::new));
        } else {
            builder.registerJavaTypeWildcardChildren(
                    ExpressionCorpusExecutionTest.WildcardChildProvider.class, Set.copyOf(wildcardChildren));
        }
        builder.externalSymbol(
                symbolName,
                new ObjectType(ExpressionCorpusExecutionTest.WildcardChildProvider.class.getName()),
                new ExpressionCorpusExecutionTest.WildcardChildProvider(),
                ExternalSymbolOverwritePolicy.FIXED);
    }

    private static List<String> textList(JsonNode node) {
        List<String> values = new ArrayList<>(node.size());
        for (JsonNode value : node) {
            values.add(value.textValue());
        }
        return List.copyOf(values);
    }

    private static ExpressionType expressionType(String type) {
        return switch (type) {
            case "NUMBER" -> ScalarType.NUMBER;
            case "BOOLEAN" -> ScalarType.BOOLEAN;
            case "STRING" -> ScalarType.STRING;
            case "DATE" -> ScalarType.DATE;
            case "TIME" -> ScalarType.TIME;
            case "DATETIME" -> ScalarType.DATETIME;
            case "COLLECTION_NUMBER" -> new CollectionType(ScalarType.NUMBER);
            case "MAP_NUMBER" -> new MapType(ScalarType.NUMBER);
            case "MAP_COLLECTION_NUMBER" -> new MapType(new CollectionType(ScalarType.NUMBER));
            default -> throw new IllegalArgumentException("Unsupported corpus expression type: " + type);
        };
    }

    static Object typedValue(String type, JsonNode node) {
        return switch (type) {
            case "NUMBER", "BOOLEAN", "STRING", "DATE", "TIME", "DATETIME" -> scalarValue(type, node.get("value"));
            case "COLLECTION", "COLLECTION_NUMBER" -> expectedCollection(node);
            case "MAP_NUMBER" -> mapValue(node);
            case "MAP_COLLECTION_NUMBER" -> mapCollectionNumber(node);
            default -> throw new IllegalArgumentException("Unsupported corpus value type: " + type);
        };
    }

    static Object scalarValue(String type, JsonNode value) {
        return switch (type) {
            case "NUMBER" -> new BigDecimal(value.textValue());
            case "BOOLEAN" -> value.booleanValue();
            case "STRING" -> value.textValue();
            case "DATE" -> LocalDate.parse(value.textValue());
            case "TIME" -> LocalTime.parse(value.textValue());
            case "DATETIME" -> LocalDateTime.parse(value.textValue());
            default -> throw new IllegalArgumentException("Unsupported scalar corpus type: " + type);
        };
    }

    private static Map<String, Object> mapCollectionNumber(JsonNode value) {
        Map<String, Object> result = new LinkedHashMap<>();
        value.fields().forEachRemaining(entry -> result.put(entry.getKey(), expectedCollection(entry.getValue())));
        return Map.copyOf(result);
    }

    private static Map<String, Object> mapValue(JsonNode value) {
        Map<String, Object> result = new LinkedHashMap<>();
        value.fields().forEachRemaining(entry -> {
            JsonNode typedValue = entry.getValue();
            result.put(entry.getKey(), typedValue(typedValue.get("type").textValue(), typedValue));
        });
        return Map.copyOf(result);
    }

    private static List<Object> expectedCollection(JsonNode values) {
        List<Object> result = new ArrayList<>(values.size());
        for (JsonNode value : values) {
            result.add(typedValue(value.get("type").textValue(), value));
        }
        return List.copyOf(result);
    }
}
