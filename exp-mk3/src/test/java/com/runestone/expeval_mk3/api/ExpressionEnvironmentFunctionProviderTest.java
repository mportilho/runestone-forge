package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
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
    @DisplayName("selective plans resolve exact overloads against Java types registered in either builder order")
    void selectivePlansResolveRegisteredJavaTypesIndependentlyOfBuilderOrder() throws Throwable {
        RegisteredCustomer customer = new RegisteredCustomer("stone");
        ReflectedFunctionImporter.Selection plan = ReflectedFunctionImporter
                .importSelected(RegisteredTypeProvider.class, FunctionPurity.PURE)
                .method("calculate", RegisteredCustomer.class)
                .rename("calculate", RegisteredCustomer.class, "customerScore");

        ExpressionEnvironment first = ExpressionEnvironment.builder()
                .functions(plan)
                .registerJavaType(RegisteredCustomer.class)
                .build();
        ExpressionEnvironment second = ExpressionEnvironment.builder()
                .registerJavaType(RegisteredCustomer.class)
                .functions(plan)
                .build();

        ObjectType customerType = new ObjectType(RegisteredCustomer.class.getName());
        assertThat(resolve(first, "customerScore", customerType).implementationHandle().invoke(customer))
                .isEqualTo("score:stone");
        assertThat(resolve(second, "customerScore", customerType).implementationHandle().invoke(customer))
                .isEqualTo("score:stone");
        assertThatThrownBy(() -> ExpressionEnvironment.builder().functions(plan).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("registered Java type")
                .hasMessageContaining(RegisteredCustomer.class.getName());
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
                .hasMessageContaining("Object provider method types are not supported");
    }

    @Test
    @DisplayName("provider functions cannot collide with built-in signatures")
    void providerFunctionsCannotCollideWithBuiltInSignatures() {
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder()
                .functionsFrom(BuiltInCollisionProvider.class, FunctionPurity.PURE);

        assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("function signature already registered")
                .hasMessageContaining("abs")
                .hasMessageContaining("official built-in")
                .hasMessageContaining(BuiltInCollisionProvider.class.getName());
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
                .contains("Object provider method types are not supported");
    }

    @Test
    @DisplayName("environment reports invalid methods and collisions from the same provider")
    void environmentReportsInvalidMethodsAndCollisionsFromTheSameProvider() {
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder()
                .functionsFrom(MixedInvalidAndCollisionProvider.class, FunctionPurity.PURE);

        assertThat(failureMessage(builder))
                .contains("Object provider method types are not supported")
                .contains("abs");
    }

    @Test
    @DisplayName("environment resolves attached selective plans only during build")
    void environmentResolvesAttachedSelectivePlansOnlyDuringBuild() {
        ReflectedFunctionImporter.Selection plan = ReflectedFunctionImporter
                .importSelected(SelectiveEnvironmentProvider.class, FunctionPurity.PURE)
                .method("value", String.class)
                .rename("value", String.class, "textValue");

        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functions(plan)
                .build();

        assertThat(resolve(environment, "textValue", ScalarType.STRING).returnType())
                .isEqualTo(ScalarType.STRING);
        assertThat(environment.functions().find(new FunctionSignature("value", List.of(ScalarType.NUMBER))))
                .isEmpty();
    }

    @Test
    @DisplayName("attached plans are snapshots and later operations cannot mutate builder configuration")
    void attachedPlansCannotBeMutatedAfterBuilderConfiguration() {
        ReflectedFunctionImporter.Selection plan = ReflectedFunctionImporter
                .importSelected(SelectiveEnvironmentProvider.class, FunctionPurity.PURE)
                .method("value", String.class);
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder().functions(plan);

        plan.rename("value", String.class, "changedAfterAttachment");

        ExpressionEnvironment environment = builder.build();
        assertThat(environment.functions().find(new FunctionSignature("value", List.of(ScalarType.STRING))))
                .isPresent();
        assertThat(environment.functions().find(new FunctionSignature(
                "changedAfterAttachment", List.of(ScalarType.STRING))))
                .isEmpty();
    }

    @Test
    @DisplayName("missing selections renames and invalid language names fail during environment build")
    void invalidSelectiveConfigurationFailsDuringEnvironmentBuild() {
        ExpressionEnvironment.Builder missingSelection = ExpressionEnvironment.builder().functions(
                ReflectedFunctionImporter
                        .importSelected(SelectiveEnvironmentProvider.class, FunctionPurity.PURE)
                        .method("missing", String.class));
        ExpressionEnvironment.Builder missingRename = ExpressionEnvironment.builder().functions(
                ReflectedFunctionImporter
                        .importSelected(SelectiveEnvironmentProvider.class, FunctionPurity.PURE)
                        .method("value", String.class)
                        .rename("missing", String.class, "renamed"));
        ExpressionEnvironment.Builder invalidName = ExpressionEnvironment.builder().functions(
                ReflectedFunctionImporter
                        .importSelected(SelectiveEnvironmentProvider.class, FunctionPurity.PURE)
                        .method("value", String.class)
                        .rename("value", String.class, "not-valid"));
        ExpressionEnvironment.Builder conflictingRename = ExpressionEnvironment.builder().functions(
                ReflectedFunctionImporter
                        .importSelected(SelectiveEnvironmentProvider.class, FunctionPurity.PURE)
                        .method("value", String.class)
                        .rename("value", "allValues")
                        .rename("value", String.class, "textValue"));

        assertThatThrownBy(missingSelection::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("selected method has no imported target")
                .hasMessageContaining("missing");
        assertThatThrownBy(missingRename::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rename has no imported target")
                .hasMessageContaining("missing");
        assertThatThrownBy(invalidName::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid identifier");
        assertThatThrownBy(conflictingRename::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("conflicting renames")
                .hasMessageContaining("value");
    }

    @Test
    @DisplayName("ordinary collisions report both origins independently of declaration order")
    void ordinaryCollisionsReportBothOriginsIndependentlyOfDeclarationOrder() {
        ExpressionEnvironment.Builder firstOrder = ExpressionEnvironment.builder()
                .functions(ReflectedFunctionImporter.importAll(CollidingProviderOne.class, FunctionPurity.PURE))
                .functions(ReflectedFunctionImporter.importAll(CollidingProviderTwo.class, FunctionPurity.PURE));
        ExpressionEnvironment.Builder secondOrder = ExpressionEnvironment.builder()
                .functions(ReflectedFunctionImporter.importAll(CollidingProviderTwo.class, FunctionPurity.PURE))
                .functions(ReflectedFunctionImporter.importAll(CollidingProviderOne.class, FunctionPurity.PURE));

        assertThat(failureMessage(firstOrder))
                .isEqualTo(failureMessage(secondOrder))
                .contains(CollidingProviderOne.class.getName())
                .contains(CollidingProviderTwo.class.getName());
    }

    @Test
    @DisplayName("same language name may extend built-ins and custom functions with different signatures")
    void sameLanguageNameMayHaveDifferentSignatures() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functions(ReflectedFunctionImporter.importAll(DifferentSignatureProvider.class, FunctionPurity.PURE))
                .build();

        assertThat(environment.functions().find(new FunctionSignature("abs", List.of(ScalarType.NUMBER))))
                .isPresent();
        assertThat(environment.functions().find(new FunctionSignature("abs", List.of(ScalarType.STRING))))
                .isPresent();
        assertThat(environment.functions().find(new FunctionSignature("value", List.of(ScalarType.NUMBER))))
                .isPresent();
        assertThat(environment.functions().find(new FunctionSignature("value", List.of(ScalarType.STRING))))
                .isPresent();
    }

    @Test
    @DisplayName("explicit reflected replacement replaces one custom signature independently of declaration order")
    void reflectedReplacementReplacesOneCustomSignatureIndependentlyOfDeclarationOrder() throws Throwable {
        ReflectedFunctionImporter.ImportPlan original = ReflectedFunctionImporter
                .importAll(OriginalCustomProvider.class, FunctionPurity.PURE);
        ReflectedFunctionImporter.ImportPlan replacement = ReflectedFunctionImporter
                .importSelected(ReplacementCustomProvider.class, FunctionPurity.PURE)
                .method("custom", String.class);

        ExpressionEnvironment first = ExpressionEnvironment.builder()
                .functions(original)
                .replaceFunctions(replacement)
                .build();
        ExpressionEnvironment second = ExpressionEnvironment.builder()
                .replaceFunctions(replacement)
                .functions(original)
                .build();

        assertThat(resolve(first, "custom", ScalarType.STRING).implementationHandle().invoke("stone"))
                .isEqualTo("replacement:stone");
        assertThat(resolve(second, "custom", ScalarType.STRING).implementationHandle().invoke("stone"))
                .isEqualTo("replacement:stone");
    }

    @Test
    @DisplayName("reflected replacement rejects absent and official built-in targets")
    void reflectedReplacementRejectsAbsentAndBuiltInTargets() {
        ReflectedFunctionImporter.ImportPlan absentReplacement = ReflectedFunctionImporter
                .importSelected(ReplacementCustomProvider.class, FunctionPurity.PURE)
                .method("custom", String.class);
        ReflectedFunctionImporter.ImportPlan builtInReplacement = ReflectedFunctionImporter
                .importSelected(BuiltInCollisionProvider.class, FunctionPurity.PURE)
                .method("abs", BigDecimal.class);

        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .replaceFunctions(absentReplacement)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no custom target")
                .hasMessageContaining("custom");
        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .replaceFunctions(builtInReplacement)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("official built-in")
                .hasMessageContaining("abs");
    }

    @Test
    @DisplayName("reflected replacement rejects multiple Java methods converging on one target")
    void reflectedReplacementRejectsMultipleMethodsConvergingOnOneTarget() {
        FunctionDescriptor original = ReflectedFunctionImporter
                .importSelected(OriginalNumberProvider.class, FunctionPurity.PURE)
                .method("original", BigDecimal.class)
                .rename("original", BigDecimal.class, "numberValue")
                .toList()
                .getFirst();
        ReflectedFunctionImporter.ImportPlan replacement = ReflectedFunctionImporter
                .importSelected(ConvergingReplacementProvider.class, FunctionPurity.PURE)
                .methods("replacement")
                .rename("replacement", "numberValue");

        assertThatThrownBy(() -> ExpressionEnvironment.builder()
                .function(original)
                .replaceFunctions(replacement)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("more than one imported method")
                .hasMessageContaining("numberValue");
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

        public static Optional<String> unsupportedInteger(String value) {
            return Optional.of(value);
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

    public record RegisteredCustomer(String name) {
    }

    public static final class RegisteredTypeProvider {
        public static String calculate(RegisteredCustomer customer) {
            return "score:" + customer.name();
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

    public static final class SelectiveEnvironmentProvider {
        public static BigDecimal value(BigDecimal value) {
            return value;
        }

        public static String value(String value) {
            return value;
        }
    }

    public static final class CollidingProviderOne {
        public static String collision(String value) {
            return "one:" + value;
        }
    }

    public static final class CollidingProviderTwo {
        public static String collision(String value) {
            return "two:" + value;
        }
    }

    public static final class DifferentSignatureProvider {
        public static String abs(String value) {
            return value;
        }

        public static BigDecimal value(BigDecimal value) {
            return value;
        }

        public static String value(String value) {
            return value;
        }
    }

    public static final class OriginalCustomProvider {
        public static String custom(String value) {
            return "original:" + value;
        }
    }

    public static final class ReplacementCustomProvider {
        public static String custom(String value) {
            return "replacement:" + value;
        }
    }

    public static final class OriginalNumberProvider {
        public static BigDecimal original(BigDecimal value) {
            return value;
        }
    }

    public static final class ConvergingReplacementProvider {
        public static int replacement(int value) {
            return value;
        }

        public static long replacement(long value) {
            return value;
        }
    }
}
