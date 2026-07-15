package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ExpressionEnvironmentFunctionProviderTest {

    @Test
    @DisplayName("environment imports canonical scalar functions from every provider form")
    void environmentImportsCanonicalScalarFunctionsFromEveryProviderForm() throws Throwable {
        InstanceProvider instanceProvider = new InstanceProvider("instance:");
        ContractProvider contractProvider = new ContractProvider();

        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(StaticProvider.class, FunctionPurity.FOLDABLE)
                .functionsFrom(instanceProvider, FunctionPurity.PURE)
                .functionsFrom(ProviderContract.class, contractProvider, FunctionPurity.IMPURE)
                .build();

        FunctionDescriptor staticFunction = resolve(environment, "canonicalScalars",
                ScalarType.NUMBER,
                ScalarType.BOOLEAN,
                ScalarType.STRING,
                ScalarType.DATE,
                ScalarType.TIME,
                ScalarType.DATETIME);
        FunctionDescriptor instanceFunction = resolve(environment, "prefix", ScalarType.STRING);
        FunctionDescriptor exposedFunction = resolve(environment, "exposed", ScalarType.STRING);

        assertThat(staticFunction.returnType()).isEqualTo(ScalarType.STRING);
        assertThat(staticFunction.foldable()).isTrue();
        assertThat(staticFunction.implementationHandle().invoke(
                BigDecimal.ONE,
                true,
                "stone",
                LocalDate.of(2026, 7, 15),
                LocalTime.NOON,
                LocalDateTime.of(2026, 7, 15, 12, 0)))
                .isEqualTo("stone:true:1");
        assertThat(instanceFunction.pure()).isTrue();
        assertThat(instanceFunction.foldable()).isFalse();
        assertThat(instanceFunction.implementationHandle().invoke("stone")).isEqualTo("instance:stone");
        assertThat(exposedFunction.pure()).isFalse();
        assertThat(exposedFunction.implementationHandle().invoke("stone")).isEqualTo("contract:stone");
        assertThat(environment.functions().find(new FunctionSignature("implementationOnly", List.of(ScalarType.STRING))))
                .isEmpty();
    }

    @Test
    @DisplayName("provider reflection and signature validation are deferred until environment build")
    void providerReflectionAndSignatureValidationAreDeferredUntilEnvironmentBuild() {
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder()
                .functionsFrom(MultipleUnsupportedProvider.class, FunctionPurity.PURE);

        assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unsupportedObject")
                .hasMessageContaining("unsupportedInteger");
    }

    @Test
    @DisplayName("environment imports only eligible directly declared methods")
    void environmentImportsOnlyEligibleDirectlyDeclaredMethods() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(FilteredProvider.class, FunctionPurity.PURE)
                .functionsFrom(new GenericStringProvider(), FunctionPurity.PURE)
                .build();

        assertThat(environment.functions().values())
                .extracting(FunctionDescriptor::languageName)
                .contains("declared", "generic")
                .doesNotContain("inherited", "instanceMethod", "privateMethod", "protectedMethod");
        assertThat(environment.functions().values())
                .filteredOn(descriptor -> descriptor.languageName().equals("generic"))
                .singleElement()
                .extracting(FunctionDescriptor::signature)
                .isEqualTo(new FunctionSignature("generic", List.of(ScalarType.STRING)));
    }

    @Test
    @DisplayName("provider declarations require purity and at least one eligible method")
    void providerDeclarationsRequirePurityAndAtLeastOneEligibleMethod() {
        assertThatNullPointerException()
                .isThrownBy(() -> ExpressionEnvironment.builder().functionsFrom(StaticProvider.class, null))
                .withMessage("purity");
        assertThatNullPointerException()
                .isThrownBy(() -> ExpressionEnvironment.builder().functionsFrom(new InstanceProvider("x"), null))
                .withMessage("purity");
        assertThatNullPointerException()
                .isThrownBy(() -> ExpressionEnvironment.builder()
                        .functionsFrom(ProviderContract.class, new ContractProvider(), null))
                .withMessage("purity");

        ExpressionEnvironment.Builder emptyProvider = ExpressionEnvironment.builder()
                .functionsFrom(NoEligibleStaticProvider.class, FunctionPurity.PURE);
        assertThatThrownBy(emptyProvider::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no eligible methods");
    }

    @Test
    @DisplayName("environment build rejects methods that public lookup cannot access")
    void environmentBuildRejectsMethodsThatPublicLookupCannotAccess() {
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder()
                .functionsFrom(InaccessibleProvider.class, FunctionPurity.PURE);

        assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not accessible");
    }

    @Test
    @DisplayName("provider handles preserve declared checked exceptions")
    void providerHandlesPreserveDeclaredCheckedExceptions() throws Throwable {
        FunctionDescriptor descriptor = resolve(
                ExpressionEnvironment.builder()
                        .functionsFrom(CheckedProvider.class, FunctionPurity.PURE)
                        .build(),
                "fail",
                ScalarType.STRING);

        assertThatThrownBy(() -> descriptor.implementationHandle().invoke("stone"))
                .isInstanceOf(IOException.class)
                .hasMessage("stone");
    }

    @Test
    @DisplayName("repeated builds bind the same provider object to independent environments")
    void repeatedBuildsBindTheSameProviderObjectToIndependentEnvironments() throws Throwable {
        CountingProvider provider = new CountingProvider();
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder()
                .functionsFrom(provider, FunctionPurity.IMPURE);

        ExpressionEnvironment first = builder.build();
        ExpressionEnvironment second = builder.build();

        assertThat(first.environmentId()).isNotEqualTo(second.environmentId());
        assertThat(resolve(first, "next").implementationHandle().invoke()).isEqualTo(BigDecimal.ONE);
        assertThat(resolve(second, "next").implementationHandle().invoke()).isEqualTo(BigDecimal.TWO);
        assertThat(provider.calls()).isEqualTo(2);
    }

    @Test
    @DisplayName("provider resolution is independent of builder configuration order")
    void providerResolutionIsIndependentOfBuilderConfigurationOrder() {
        ExpressionEnvironment first = ExpressionEnvironment.builder()
                .functionsFrom(StaticProvider.class, FunctionPurity.FOLDABLE)
                .registerJavaType(RegisteredType.class)
                .build();
        ExpressionEnvironment second = ExpressionEnvironment.builder()
                .registerJavaType(RegisteredType.class)
                .functionsFrom(StaticProvider.class, FunctionPurity.FOLDABLE)
                .build();

        assertThat(first.functions().values()).containsExactlyElementsOf(second.functions().values());
        assertThat(first.javaTypes().find(RegisteredType.class)).isPresent();
        assertThat(second.javaTypes().find(RegisteredType.class)).isPresent();
    }

    @Test
    @DisplayName("prepared provider handles reject null arguments and results")
    void preparedProviderHandlesRejectNullArgumentsAndResults() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(NullProvider.class, FunctionPurity.PURE)
                .build();

        assertThatThrownBy(() -> resolve(environment, "echo", ScalarType.STRING)
                .implementationHandle().invoke((String) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("function arguments and results must not be null");
        assertThatThrownBy(() -> resolve(environment, "nullResult")
                .implementationHandle().invoke())
                .isInstanceOf(NullPointerException.class)
                .hasMessage("function arguments and results must not be null");
    }

    @Test
    @DisplayName("one invalid provider makes the whole environment build fail")
    void oneInvalidProviderMakesTheWholeEnvironmentBuildFail() {
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder()
                .functionsFrom(StaticProvider.class, FunctionPurity.PURE)
                .functionsFrom(UnsupportedProvider.class, FunctionPurity.PURE);

        assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unsupported canonical scalar");
    }

    @Test
    @DisplayName("provider functions cannot collide with built-in signatures")
    void providerFunctionsCannotCollideWithBuiltInSignatures() {
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder()
                .functionsFrom(BuiltInCollisionProvider.class, FunctionPurity.PURE);

        assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("function signature already registered")
                .hasMessageContaining("abs");
    }

    @Test
    @DisplayName("environment reports all provider failures independently of declaration order")
    void environmentReportsAllProviderFailuresIndependentlyOfDeclarationOrder() {
        ExpressionEnvironment.Builder firstOrder = ExpressionEnvironment.builder()
                .functionsFrom(BuiltInCollisionProvider.class, FunctionPurity.PURE)
                .functionsFrom(UnsupportedProvider.class, FunctionPurity.PURE);
        ExpressionEnvironment.Builder secondOrder = ExpressionEnvironment.builder()
                .functionsFrom(UnsupportedProvider.class, FunctionPurity.PURE)
                .functionsFrom(BuiltInCollisionProvider.class, FunctionPurity.PURE);

        String firstMessage = failureMessage(firstOrder);
        String secondMessage = failureMessage(secondOrder);

        assertThat(firstMessage)
                .isEqualTo(secondMessage)
                .contains("abs")
                .contains("unsupported canonical scalar");
    }

    @Test
    @DisplayName("environment reports invalid methods and collisions from the same provider")
    void environmentReportsInvalidMethodsAndCollisionsFromTheSameProvider() {
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder()
                .functionsFrom(MixedInvalidAndCollisionProvider.class, FunctionPurity.PURE);

        assertThat(failureMessage(builder))
                .contains("unsupported canonical scalar")
                .contains("abs");
    }

    private static FunctionDescriptor resolve(
            ExpressionEnvironment environment,
            String name,
            ExpressionType... parameterTypes) {
        return environment.functions()
                .find(new FunctionSignature(name, List.of(parameterTypes)))
                .orElseThrow();
    }

    private static String failureMessage(ExpressionEnvironment.Builder builder) {
        try {
            builder.build();
            throw new AssertionError("environment build should fail");
        } catch (IllegalArgumentException exception) {
            return exception.getMessage();
        }
    }

    public static final class StaticProvider {
        public static String canonicalScalars(
                BigDecimal number,
                boolean enabled,
                String text,
                LocalDate date,
                LocalTime time,
                LocalDateTime dateTime) {
            return text + ':' + enabled + ':' + number;
        }

        public String oppositeModality(String value) {
            return value;
        }
    }

    public static final class InstanceProvider {
        private final String prefix;

        public InstanceProvider(String prefix) {
            this.prefix = prefix;
        }

        public String prefix(String value) {
            return prefix + value;
        }

        public static String oppositeModality(String value) {
            return value;
        }
    }

    public interface ProviderContract {
        String exposed(String value);
    }

    public static final class ContractProvider implements ProviderContract {
        @Override
        public String exposed(String value) {
            return "contract:" + value;
        }

        public String implementationOnly(String value) {
            return value;
        }
    }

    public static final class UnsupportedProvider {
        public static Object unsupported(Object value) {
            return value;
        }
    }

    public static final class MultipleUnsupportedProvider {
        public static Object unsupportedObject(Object value) {
            return value;
        }

        public static int unsupportedInteger(int value) {
            return value;
        }
    }

    public static class ParentProvider {
        public static String inherited(String value) {
            return value;
        }
    }

    public static final class FilteredProvider extends ParentProvider {
        public static String declared(String value) {
            return value;
        }

        public String instanceMethod(String value) {
            return value;
        }

        private static String privateMethod(String value) {
            return value;
        }

        protected static String protectedMethod(String value) {
            return value;
        }
    }

    public interface GenericProvider<T> {
        T generic(T value);
    }

    public static final class GenericStringProvider implements GenericProvider<String> {
        @Override
        public String generic(String value) {
            return value;
        }
    }

    public static final class NoEligibleStaticProvider {
        public String instanceMethod(String value) {
            return value;
        }
    }

    private static final class InaccessibleProvider {
        public static String inaccessible(String value) {
            return value;
        }
    }

    public static final class CheckedProvider {
        public static String fail(String value) throws IOException {
            throw new IOException(value);
        }
    }

    public static final class CountingProvider {
        private final AtomicInteger calls = new AtomicInteger();

        public BigDecimal next() {
            return BigDecimal.valueOf(calls.incrementAndGet());
        }

        int calls() {
            return calls.get();
        }
    }

    public static final class RegisteredType {
        public String value() {
            return "value";
        }
    }

    public static final class NullProvider {
        public static String echo(String value) {
            return value;
        }

        public static String nullResult() {
            return null;
        }
    }

    public static final class BuiltInCollisionProvider {
        public static BigDecimal abs(BigDecimal value) {
            return value.abs();
        }
    }

    public static final class MixedInvalidAndCollisionProvider {
        public static BigDecimal abs(BigDecimal value) {
            return value.abs();
        }

        public static Object unsupported(Object value) {
            return value;
        }
    }
}
