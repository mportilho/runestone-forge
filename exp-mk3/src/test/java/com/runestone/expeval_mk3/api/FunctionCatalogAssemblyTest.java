package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FunctionCatalogAssemblyTest {

    private static final BoundaryCoercion BOUNDARY_COERCION = BoundaryCoercion.standard();
    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL128;
    private static final JavaTypeCatalog JAVA_TYPES = JavaTypeCatalog.builder().build();
    private static final int MAX_MATERIALIZED_SIZE = 10_000;

    @Test
    @DisplayName("assembly merges built-ins, custom functions, and reflected imports into one catalog")
    void assemblyMergesBuiltInsCustomFunctionsAndReflectedImports() throws Throwable {
        FunctionDescriptor customGreet = firstDescriptor(GreetProvider.class);

        FunctionCatalog catalog = FunctionCatalogAssembly.assemble(
                BOUNDARY_COERCION,
                MATH_CONTEXT,
                MATH_CONTEXT,
                MAX_MATERIALIZED_SIZE,
                JAVA_TYPES,
                List.of(customGreet),
                List.of(ReflectedFunctionImporter.importAll(ShoutProvider.class, FunctionPurity.PURE)),
                List.of(),
                List.of());

        assertThat(catalog.find(new FunctionSignature("abs", List.of(ScalarType.NUMBER)))).isPresent();
        assertThat(catalog.find(new FunctionSignature("greet", List.of(ScalarType.STRING)))
                .orElseThrow().implementationHandle().invoke("stone"))
                .isEqualTo("hi:stone");
        assertThat(catalog.find(new FunctionSignature("shout", List.of(ScalarType.STRING)))
                .orElseThrow().implementationHandle().invoke("stone"))
                .isEqualTo("STONE");
    }

    @Test
    @DisplayName("reflected replacement takes precedence over the custom function it targets")
    void reflectedReplacementTakesPrecedenceOverCustomFunction() throws Throwable {
        FunctionDescriptor customGreet = firstDescriptor(GreetProvider.class);

        FunctionCatalog catalog = FunctionCatalogAssembly.assemble(
                BOUNDARY_COERCION,
                MATH_CONTEXT,
                MATH_CONTEXT,
                MAX_MATERIALIZED_SIZE,
                JAVA_TYPES,
                List.of(customGreet),
                List.of(),
                List.of(),
                List.of(ReflectedFunctionImporter.importAll(ReplacementGreetProvider.class, FunctionPurity.PURE)));

        assertThat(catalog.find(new FunctionSignature("greet", List.of(ScalarType.STRING)))
                .orElseThrow().implementationHandle().invoke("stone"))
                .isEqualTo("replaced:stone");
    }

    @Test
    @DisplayName("problems from custom, provider, and replacement sources aggregate into one exception with suppressed causes")
    void problemsFromAllThreeSourcesAggregateIntoOneException() throws Throwable {
        FunctionDescriptor builtInCollision = firstDescriptor(BuiltInCollisionProvider.class);

        assertThatThrownBy(() -> FunctionCatalogAssembly.assemble(
                BOUNDARY_COERCION,
                MATH_CONTEXT,
                MATH_CONTEXT,
                MAX_MATERIALIZED_SIZE,
                JAVA_TYPES,
                List.of(builtInCollision),
                List.of(ReflectedFunctionImporter.importAll(UnsupportedProvider.class, FunctionPurity.PURE)),
                List.of(),
                List.of(ReflectedFunctionImporter.importAll(GreetProvider.class, FunctionPurity.PURE))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("function signature already registered")
                .hasMessageContaining("Object provider method types are not supported")
                .hasMessageContaining("no custom target")
                .satisfies(exception -> assertThat(exception.getSuppressed()).hasSize(2));
    }

    private static FunctionDescriptor firstDescriptor(Class<?> providerClass) {
        return ReflectedFunctionImporter.resolveForEnvironment(
                        ReflectedFunctionImporter.importAll(providerClass, FunctionPurity.PURE),
                        JAVA_TYPES,
                        BOUNDARY_COERCION,
                        MAX_MATERIALIZED_SIZE)
                .descriptors()
                .getFirst();
    }

    public static final class GreetProvider {
        public static String greet(String name) {
            return "hi:" + name;
        }
    }

    public static final class ReplacementGreetProvider {
        public static String greet(String name) {
            return "replaced:" + name;
        }
    }

    public static final class ShoutProvider {
        public static String shout(String name) {
            return name.toUpperCase();
        }
    }

    public static final class BuiltInCollisionProvider {
        public static BigDecimal abs(BigDecimal value) {
            return value.abs();
        }
    }

    public static final class UnsupportedProvider {
        public static Object unsupported(Object value) {
            return value;
        }
    }
}
