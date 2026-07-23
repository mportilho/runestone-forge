package com.runestone.expeval_mk3.api;

import com.runestone.converters.ConversionContext;
import com.runestone.converters.DataConversionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionEnvironmentProviderContainerContractTest {

    @Test
    @DisplayName("arrays lists collections iterables and maps expose recursive expression types")
    void containersExposeRecursiveExpressionTypes() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(ContainerProvider.class, FunctionPurity.PURE)
                .build();
        CollectionType numberCollection = new CollectionType(ScalarType.NUMBER);
        CollectionType textCollection = new CollectionType(ScalarType.STRING);
        MapType nestedMap = new MapType(numberCollection);

        assertThat(resolve(environment, "array", numberCollection).returnType()).isEqualTo(numberCollection);
        assertThat(resolve(environment, "list", numberCollection).returnType()).isEqualTo(numberCollection);
        assertThat(resolve(environment, "collection", textCollection).returnType()).isEqualTo(textCollection);
        assertThat(resolve(environment, "set", textCollection).returnType()).isEqualTo(textCollection);
        assertThat(resolve(environment, "iterable", textCollection).returnType()).isEqualTo(textCollection);
        assertThat(resolve(environment, "nestedMap", nestedMap).returnType()).isEqualTo(nestedMap);
        assertThat(resolve(environment, "concrete", numberCollection).returnType()).isEqualTo(numberCollection);
    }

    @Test
    @DisplayName("prepared handles adapt arrays containers maps and recursively numeric elements")
    void preparedHandlesAdaptContainersAndNestedElements() throws Throwable {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(ContainerProvider.class, FunctionPurity.PURE)
                .build();
        CollectionType numberCollection = new CollectionType(ScalarType.NUMBER);
        CollectionType textCollection = new CollectionType(ScalarType.STRING);
        MapType nestedMap = new MapType(numberCollection);

        assertThat(resolve(environment, "array", numberCollection).implementationHandle()
                .invoke(List.of(BigDecimal.ONE, BigDecimal.TWO)))
                .isEqualTo(List.of(new BigDecimal("2"), new BigDecimal("3")));
        assertThat(resolve(environment, "list", numberCollection).implementationHandle()
                .invoke(List.of(new BigDecimal("3"))))
                .isEqualTo(List.of(new BigDecimal("6")));
        assertThat(resolve(environment, "collection", textCollection).implementationHandle()
                .invoke(List.of("stone")))
                .isEqualTo(List.of("stone", "collection"));
        assertThat(resolve(environment, "iterable", textCollection).implementationHandle()
                .invoke(List.of("stone")))
                .isEqualTo(List.of("stone", "iterable"));
        assertThat(resolve(environment, "nestedMap", nestedMap).implementationHandle()
                .invoke(Map.of("values", List.of(new BigDecimal("4")))))
                .isEqualTo(Map.of("values", List.of(new BigDecimal("5"))));
        assertThat(resolve(environment, "concrete", numberCollection).implementationHandle()
                .invoke(List.of(new BigDecimal("7"))))
                .isEqualTo(List.of(new BigDecimal("8")));
    }

    @Test
    @DisplayName("provider container results are recursive immutable snapshots")
    void providerContainerResultsAreRecursiveImmutableSnapshots() throws Throwable {
        MutableResultProvider.reset();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(MutableResultProvider.class, FunctionPurity.IMPURE)
                .build();
        MapType resultType = new MapType(new CollectionType(ScalarType.NUMBER));

        @SuppressWarnings("unchecked")
        Map<String, List<BigDecimal>> result = (Map<String, List<BigDecimal>>) resolve(environment, "values")
                .implementationHandle().invoke();
        MutableResultProvider.mutate();

        assertThat(result).isEqualTo(Map.of("numbers", List.of(BigDecimal.ONE)));
        assertThatThrownBy(() -> result.put("other", List.of()))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> result.get("numbers").add(BigDecimal.TWO))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(resolve(environment, "values").returnType()).isEqualTo(resultType);
    }

    @Test
    @DisplayName("provider container boundaries reject null recursively and non-text map keys")
    void providerContainerBoundariesRejectInvalidNestedValues() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(InvalidResultProvider.class, FunctionPurity.PURE)
                .build();

        assertThatThrownBy(() -> resolve(environment, "nullElement").implementationHandle().invoke())
                .isInstanceOf(NullPointerException.class)
                .hasMessage("function arguments and results must not be null");
        assertThatThrownBy(() -> resolve(environment, "nullMapValue").implementationHandle().invoke())
                .isInstanceOf(NullPointerException.class)
                .hasMessage("function arguments and results must not be null");
        assertThatThrownBy(() -> resolve(environment, "nonTextRuntimeKey").implementationHandle().invoke())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("String keys");
        assertThatThrownBy(() -> resolve(
                        environment,
                        "echo",
                        new MapType(new CollectionType(ScalarType.NUMBER)))
                .implementationHandle().invoke(Map.of("numbers", Arrays.asList(BigDecimal.ONE, null))))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("function arguments and results must not be null");
    }

    @Test
    @DisplayName("iterable results stop after maxMaterializedSize plus one elements")
    void iterableResultsStopAtMaterializationLimit() {
        InfiniteIterableProvider.reset();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(InfiniteIterableProvider.class, FunctionPurity.IMPURE)
                .maxMaterializedSize(2)
                .build();

        assertThatThrownBy(() -> resolve(environment, "values").implementationHandle().invoke())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxMaterializedSize 2");
        assertThat(InfiniteIterableProvider.produced()).isEqualTo(3);
    }

    @Test
    @DisplayName("raw wildcard type-variable non-text map and multidimensional contracts fail at build")
    void unresolvedAndUnsupportedContainerContractsFailAtBuild() {
        assertBuildFailure(RawListProvider.class, "raw");
        assertBuildFailure(WildcardProvider.class, "wildcard");
        assertBuildFailure(GenericMethodProvider.class, "generic provider methods");
        assertBuildFailure(NonTextMapProvider.class, "String keys");
        assertBuildFailure(MultidimensionalArrayProvider.class, "multidimensional");
        assertBuildFailure(VarargsProvider.class, "varargs");
    }

    @Test
    @DisplayName("Java sequence overloads that share one collection signature are rejected")
    void javaSequenceOverloadsThatShareOneCollectionSignatureAreRejected() {
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .functionsFrom(CollapsedSequenceOverloadProvider.class, FunctionPurity.PURE)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered")
                .hasMessageContaining("sequence");
    }

    @Test
    @DisplayName("concrete containers require adaptation from the configured boundary coercion")
    void concreteContainersRequireConfiguredBoundaryAdaptation() {
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .functionsFrom(CustomConcreteTextProvider.class, FunctionPurity.PURE)
                .boundaryCoercion(new NoConversionService())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(List.class.getName())
                .hasMessageContaining(CustomList.class.getName());
    }

    private static FunctionDescriptor resolve(
            ExpressionEnvironment environment,
            String name,
            ExpressionType... parameterTypes) {
        return environment.functions()
                .find(new FunctionSignature(name, List.of(parameterTypes)))
                .orElseThrow();
    }

    private static void assertBuildFailure(Class<?> providerType, String message) {
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .functionsFrom(providerType, FunctionPurity.PURE)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(message);
    }

    public static final class ContainerProvider {
        public static long[] array(int[] values) {
            long[] result = new long[values.length];
            for (int index = 0; index < values.length; index++) {
                result[index] = (long) values[index] + 1;
            }
            return result;
        }

        public static List<Long> list(List<Integer> values) {
            return List.of(values.getFirst().longValue() * 2);
        }

        public static Collection<String> collection(Collection<String> values) {
            return List.of(values.iterator().next(), "collection");
        }

        public static Set<String> set(Set<String> values) {
            return values;
        }

        public static Iterable<String> iterable(Iterable<String> values) {
            return List.of(values.iterator().next(), "iterable");
        }

        public static Map<String, List<BigDecimal>> nestedMap(Map<String, List<BigDecimal>> values) {
            return Map.of("values", List.of(values.get("values").getFirst().add(BigDecimal.ONE)));
        }

        public static ArrayList<Integer> concrete(ArrayList<Integer> values) {
            return new ArrayList<>(List.of(values.getFirst() + 1));
        }
    }

    public static final class MutableResultProvider {
        private static final List<Integer> NUMBERS = new ArrayList<>();
        private static final Map<String, List<Integer>> VALUES = new LinkedHashMap<>();

        static void reset() {
            NUMBERS.clear();
            NUMBERS.add(1);
            VALUES.clear();
            VALUES.put("numbers", NUMBERS);
        }

        static void mutate() {
            NUMBERS.add(2);
            VALUES.put("other", List.of(3));
        }

        public static Map<String, List<Integer>> values() {
            return VALUES;
        }
    }

    public static final class InvalidResultProvider {
        public static List<String> nullElement() {
            return Arrays.asList("stone", null);
        }

        public static Map<String, List<String>> nullMapValue() {
            LinkedHashMap<String, List<String>> values = new LinkedHashMap<>();
            values.put("stone", null);
            return values;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        public static Map<String, String> nonTextRuntimeKey() {
            Map values = new LinkedHashMap();
            values.put(1, "stone");
            return values;
        }

        public static Map<String, List<BigDecimal>> echo(Map<String, List<BigDecimal>> values) {
            return values;
        }
    }

    public static final class InfiniteIterableProvider {
        private static final AtomicInteger PRODUCED = new AtomicInteger();

        static void reset() {
            PRODUCED.set(0);
        }

        static int produced() {
            return PRODUCED.get();
        }

        public static Iterable<String> values() {
            return () -> new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return true;
                }

                @Override
                public String next() {
                    return "value-" + PRODUCED.incrementAndGet();
                }
            };
        }
    }

    public static final class RawListProvider {
        public static List values() {
            return List.of();
        }
    }

    public static final class WildcardProvider {
        public static List<?> values() {
            return List.of();
        }
    }

    public static final class GenericMethodProvider {
        public static <T> T value(T value) {
            return value;
        }
    }

    public static final class NonTextMapProvider {
        public static Map<Integer, String> values() {
            return Map.of();
        }
    }

    public static final class MultidimensionalArrayProvider {
        public static String[][] values() {
            return new String[0][];
        }
    }

    public static final class VarargsProvider {
        public static String values(String... values) {
            return String.join(",", values);
        }
    }

    public static final class CollapsedSequenceOverloadProvider {
        public static List<Integer> sequence(List<Integer> values) {
            return values;
        }

        public static Set<Integer> sequence(Set<Integer> values) {
            return values;
        }
    }

    public static final class CustomList<T> extends ArrayList<T> {
    }

    public static final class CustomConcreteTextProvider {
        public static CustomList<String> values(CustomList<String> values) {
            return values;
        }
    }

    private static final class NoConversionService implements DataConversionService {
        @Override
        public ConversionContext conversionContext() {
            return ConversionContext.standard();
        }

        @Override
        public String conversionProfileIdentity() {
            return "test-no-conversions";
        }

        @Override
        public String conversionProfileHash() {
            return "test-no-conversions";
        }

        @Override
        public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
            return false;
        }

        @Override
        public <S, T> T convert(S source, Class<T> targetType) {
            throw new IllegalArgumentException("unsupported test conversion");
        }

        @Override
        public <T> T copyFoldableValue(T value) {
            return value;
        }
    }
}
