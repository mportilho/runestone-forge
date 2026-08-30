package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExternalSymbolContainerSnapshotTest {

    @Test
    void sequentialDefaultsAcceptEverySupportedSourceAndPreserveEncounterOrder() {
        Iterable<Integer> iterable = () -> List.of(9, 10).iterator();

        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("objectArray", new Integer[]{1, 2}, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("primitiveArray", new int[]{3, 4}, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("list", List.of(5, 6), ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("set", new LinkedHashSet<>(List.of(7, 8)), ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("collection", new ArrayDeque<>(List.of(11, 12)), ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("iterable", iterable, ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThat(defaultValue(environment, "objectArray")).isEqualTo(numbers(1, 2));
        assertThat(defaultValue(environment, "primitiveArray")).isEqualTo(numbers(3, 4));
        assertThat(defaultValue(environment, "list")).isEqualTo(numbers(5, 6));
        assertThat(defaultValue(environment, "set")).isEqualTo(numbers(7, 8));
        assertThat(defaultValue(environment, "iterable")).isEqualTo(numbers(9, 10));
        assertThat(defaultValue(environment, "collection")).isEqualTo(numbers(11, 12));
        assertThat(environment.externalSymbols().find("iterable").orElseThrow().type())
                .isEqualTo(new CollectionType(ScalarType.NUMBER));
    }

    @Test
    void defaultsAreRecursiveImmutableSnapshotsIsolatedFromSourceMutation() {
        List<String> nested = new ArrayList<>(List.of("before"));
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("nested", nested);
        List<Object> source = new ArrayList<>();
        source.add(map);

        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol(
                        "values",
                        new CollectionType(new MapType(new CollectionType(ScalarType.STRING))),
                        source,
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        Object snapshot = defaultValue(environment, "values");

        nested.add("after");
        map.put("other", List.of("changed"));
        source.clear();

        assertThat(snapshot).isEqualTo(List.of(Map.of("nested", List.of("before"))));
        List<?> outer = (List<?>) snapshot;
        Map<?, ?> snapshotMap = (Map<?, ?>) outer.getFirst();
        List<?> snapshotNested = (List<?>) snapshotMap.get("nested");
        assertThatThrownBy(() -> ((List<Object>) outer).add("x"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> ((Map<Object, Object>) snapshotMap).put("x", "y"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> ((List<Object>) snapshotNested).add("x"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void defaultsRejectNestedNullAndHeterogeneousInferredContainers() {
        List<Object> nestedNull = new ArrayList<>();
        nestedNull.add(List.of("valid"));
        nestedNull.add(new ArrayList<>(Arrays.asList("valid", null)));

        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .externalSymbol("values", nestedNull, ExternalSymbolOverwritePolicy.FIXED)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("values");
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .externalSymbol("values", (Iterable<Object>) () -> List.<Object>of("text", 1).iterator(),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("heterogeneous");
    }

    @Test
    void defaultsEnforceExactKnownAndUnknownMaterializationLimits() {
        AtomicInteger consumed = new AtomicInteger();
        Iterable<Integer> unbounded = () -> new Iterator<>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Integer next() {
                return consumed.incrementAndGet();
            }
        };

        ExpressionEnvironment exact = ExpressionEnvironment.builder()
                .maxMaterializedSize(2)
                .externalSymbol("values", List.of(1, 2), ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThat(defaultValue(exact, "values")).isEqualTo(numbers(1, 2));
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .maxMaterializedSize(2)
                .externalSymbol("values", new int[]{1, 2, 3}, ExternalSymbolOverwritePolicy.FIXED)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxMaterializedSize 2");
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .maxMaterializedSize(2)
                .externalSymbol("values", unbounded, ExternalSymbolOverwritePolicy.FIXED)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxMaterializedSize 2");
        assertThat(consumed).hasValue(3);
    }

    @Test
    void overridesUseTheEnvironmentLimitAndReturnIsolatedSnapshots() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .maxMaterializedSize(2)
                .externalSymbol(
                        "values",
                        new CollectionType(ScalarType.STRING),
                        List.of("default"),
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        ExternalSymbol symbol = environment.externalSymbols().find("values").orElseThrow();
        List<String> source = new ArrayList<>(List.of("one", "two"));

        Object snapshot = symbol.coerceOverride(source, environment.boundaryCoercion());
        source.add("after");

        assertThat(snapshot).isEqualTo(List.of("one", "two"));
        assertThatThrownBy(() -> symbol.coerceOverride(List.of("one", "two", "three"),
                environment.boundaryCoercion()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxMaterializedSize 2");
    }

    @Test
    void listConversionPreservesCanonicalElementsBeforeTheFirstConvertedElement() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol(
                        "values",
                        new CollectionType(ScalarType.NUMBER),
                        List.of(BigDecimal.ZERO),
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        ExternalSymbol symbol = environment.externalSymbols().find("values").orElseThrow();

        Object snapshot = symbol.coerceOverride(
                List.of(BigDecimal.ONE, 2, BigDecimal.valueOf(3)), environment.boundaryCoercion());

        assertThat(snapshot).isEqualTo(numbers(1, 2, 3));
    }

    @Test
    void mapsAreRecursivelyCopiedAndOrderedByBinaryTextComparison() {
        List<String> nested = new ArrayList<>(List.of("value"));
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("á", nested);
        source.put("a", List.of("lower"));
        source.put("Z", List.of("upper"));

        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .maxMaterializedSize(3)
                .externalSymbol("values", source, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        Map<?, ?> snapshot = (Map<?, ?>) defaultValue(environment, "values");

        nested.add("after");
        source.clear();

        assertThat(keys(snapshot)).containsExactly("Z", "a", "á");
        assertThat(snapshot.get("á")).isEqualTo(List.of("value"));

        ExternalSymbol symbol = environment.externalSymbols().find("values").orElseThrow();
        Map<String, List<String>> override = new LinkedHashMap<>();
        override.put("b", List.of("second"));
        override.put("A", List.of("first"));
        Map<?, ?> overrideSnapshot = (Map<?, ?>) symbol.coerceOverride(
                override, environment.boundaryCoercion());

        override.clear();

        assertThat(keys(overrideSnapshot)).containsExactly("A", "b");
        assertThatThrownBy(() -> ((Map<Object, Object>) overrideSnapshot).put("x", "y"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void everyNestedContainerEnforcesTheMaterializationLimit() {
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .maxMaterializedSize(2)
                .externalSymbol(
                        "values",
                        new CollectionType(new MapType(new CollectionType(ScalarType.STRING))),
                        List.of(Map.of("items", List.of("one", "two", "three"))),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxMaterializedSize 2");

        Map<String, String> exact = new LinkedHashMap<>();
        exact.put("b", "second");
        exact.put("a", "first");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .maxMaterializedSize(2)
                .externalSymbol("values", exact, ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThat(keys((Map<?, ?>) defaultValue(environment, "values")))
                .containsExactly("a", "b");
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .maxMaterializedSize(2)
                .externalSymbol(
                        "values",
                        new MapType(ScalarType.STRING),
                        Map.of("a", "one", "b", "two", "c", "three"),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxMaterializedSize 2");
    }

    @Test
    void snapshotsAllMutableSequentialSourcesAndRetainsNominalElementsByReference() {
        Integer[] objectArray = {1, 2};
        int[] primitiveArray = {3, 4};
        List<Integer> list = new ArrayList<>(List.of(5, 6));
        Set<Integer> set = new LinkedHashSet<>(List.of(7, 8));
        ArrayDeque<Integer> collection = new ArrayDeque<>(List.of(9, 10));
        List<Integer> iterableValues = new ArrayList<>(List.of(11, 12));
        Iterable<Integer> iterable = iterableValues::iterator;
        Object nominal = new Object();

        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("objectArray", objectArray, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("primitiveArray", primitiveArray, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("list", list, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("set", set, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("collection", collection, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("iterable", iterable, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("nominal", List.of(nominal), ExternalSymbolOverwritePolicy.FIXED)
                .build();

        objectArray[0] = 99;
        primitiveArray[0] = 99;
        list.clear();
        set.clear();
        collection.clear();
        iterableValues.clear();

        assertThat(defaultValue(environment, "objectArray")).isEqualTo(numbers(1, 2));
        assertThat(defaultValue(environment, "primitiveArray")).isEqualTo(numbers(3, 4));
        assertThat(defaultValue(environment, "list")).isEqualTo(numbers(5, 6));
        assertThat(defaultValue(environment, "set")).isEqualTo(numbers(7, 8));
        assertThat(defaultValue(environment, "collection")).isEqualTo(numbers(9, 10));
        assertThat(defaultValue(environment, "iterable")).isEqualTo(numbers(11, 12));
        assertThat((List<Object>) defaultValue(environment, "nominal")).containsExactly(nominal);
        assertThat(((List<?>) defaultValue(environment, "nominal")).getFirst()).isSameAs(nominal);
    }

    @Test
    void unknownSizeIterableAcceptsTheExactLimitAndOverridesRejectNestedNulls() {
        AtomicInteger consumed = new AtomicInteger();
        Iterable<Integer> exact = () -> new Iterator<>() {
            @Override
            public boolean hasNext() {
                return consumed.get() < 2;
            }

            @Override
            public Integer next() {
                return consumed.incrementAndGet();
            }
        };
        ExpressionEnvironment exactEnvironment = ExpressionEnvironment.builder()
                .maxMaterializedSize(2)
                .externalSymbol("values", exact, ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThat(defaultValue(exactEnvironment, "values")).isEqualTo(numbers(1, 2));
        assertThat(consumed).hasValue(2);

        ExpressionEnvironment overrideEnvironment = ExpressionEnvironment.builder()
                .externalSymbol(
                        "values",
                        new CollectionType(new CollectionType(ScalarType.STRING)),
                        List.of(List.of("default")),
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        List<Object> nestedNull = new ArrayList<>();
        nestedNull.add(new ArrayList<>(Arrays.asList("valid", null)));

        assertThatThrownBy(() -> overrideEnvironment.externalSymbols()
                .find("values")
                .orElseThrow()
                .coerceOverride(nestedNull, overrideEnvironment.boundaryCoercion()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("values");
    }

    @Test
    void iterationStillEnforcesTheLimitWhenContainersUnderreportTheirSize() {
        CollectionWithUnderreportedSize values = new CollectionWithUnderreportedSize();

        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .maxMaterializedSize(2)
                .externalSymbol("values", values, ExternalSymbolOverwritePolicy.FIXED)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxMaterializedSize 2");

        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .maxMaterializedSize(2)
                .externalSymbol(
                        "values",
                        new MapType(ScalarType.STRING),
                        Map.of("default", "value"),
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();

        assertThatThrownBy(() -> environment.externalSymbols()
                .find("values")
                .orElseThrow()
                .coerceOverride(new MapWithUnderreportedSize(), environment.boundaryCoercion()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxMaterializedSize 2");
    }

    @Test
    void functionBindingFallbackDoesNotExpandItsProviderIterableContract() {
        Iterable<String> values = () -> List.of("value").iterator();

        assertThatThrownBy(() -> BoundaryCoercion.standard().convertFunctionBindingFallback(
                values, new CollectionType(ScalarType.STRING)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be a collection");
    }

    private static Object defaultValue(ExpressionEnvironment environment, String name) {
        return environment.externalSymbols().find(name).orElseThrow().defaultValue().value();
    }

    private static List<BigDecimal> numbers(int... values) {
        return Arrays.stream(values).mapToObj(BigDecimal::valueOf).toList();
    }

    private static List<Object> keys(Map<?, ?> values) {
        return new ArrayList<>(values.keySet());
    }

    private static final class CollectionWithUnderreportedSize extends AbstractCollection<Integer> {

        @Override
        public Iterator<Integer> iterator() {
            return List.of(1, 2, 3).iterator();
        }

        @Override
        public int size() {
            return 0;
        }
    }

    private static final class MapWithUnderreportedSize extends AbstractMap<String, String> {

        @Override
        public Set<Entry<String, String>> entrySet() {
            return new AbstractSet<>() {
                @Override
                public Iterator<Entry<String, String>> iterator() {
                    return List.of(
                            Map.entry("a", "one"),
                            Map.entry("b", "two"),
                            Map.entry("c", "three")).iterator();
                }

                @Override
                public int size() {
                    return 0;
                }
            };
        }
    }
}
