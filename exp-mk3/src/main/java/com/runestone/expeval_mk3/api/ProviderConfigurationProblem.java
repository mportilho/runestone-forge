package com.runestone.expeval_mk3.api;

import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.Objects;

/**
 * One independently discoverable function-provider configuration problem, carrying only safe
 * reflective metadata: never the provider instance, and never its {@code toString()}.
 */
public record ProviderConfigurationProblem(
        Category category,
        String providerExposureType,
        String javaMethodName,
        String javaMethodDescriptor,
        Boolean staticMethod,
        FunctionPurity purity,
        String message,
        RuntimeException cause) {

    static final Comparator<ProviderConfigurationProblem> ORDER = Comparator
            .comparing(ProviderConfigurationProblem::providerExposureType)
            .thenComparing(problem -> problem.javaMethodDescriptor() == null ? "" : problem.javaMethodDescriptor())
            .thenComparing(problem -> problem.javaMethodName() == null ? "" : problem.javaMethodName())
            .thenComparing(problem -> problem.category().name())
            .thenComparing(ProviderConfigurationProblem::message);

    public ProviderConfigurationProblem {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(providerExposureType, "providerExposureType");
        Objects.requireNonNull(message, "message");
    }

    public enum Category {
        NO_ELIGIBLE_METHODS,
        SELECTION,
        RENAME,
        METHOD_REJECTED,
        DUPLICATE_SIGNATURE,
        REGISTRATION_COLLISION,
        REPLACEMENT_CARDINALITY
    }

    static ProviderConfigurationProblem providerLevel(
            Category category,
            Class<?> exposureType,
            FunctionPurity purity,
            String message,
            RuntimeException cause) {
        return new ProviderConfigurationProblem(
                category, exposureType.getName(), null, null, null, purity, message, cause);
    }

    static ProviderConfigurationProblem methodLevel(
            Category category,
            Class<?> exposureType,
            Method method,
            FunctionPurity purity,
            String detail,
            RuntimeException cause) {
        MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
        return new ProviderConfigurationProblem(
                category,
                exposureType.getName(),
                method.getName(),
                methodType.toMethodDescriptorString(),
                Modifier.isStatic(method.getModifiers()),
                purity,
                "invalid function provider " + exposureType.getName() + ": " + method.toGenericString()
                        + ": " + detail,
                cause);
    }

    static ProviderConfigurationProblem fromDescriptor(
            Category category,
            FunctionDescriptor descriptor,
            String message,
            RuntimeException cause) {
        FunctionImplementationMetadata metadata = descriptor.implementationMetadata();
        FunctionPurity purity = descriptor.foldable()
                ? FunctionPurity.FOLDABLE
                : descriptor.pure() ? FunctionPurity.PURE : FunctionPurity.IMPURE;
        return new ProviderConfigurationProblem(
                category,
                metadata.owner(),
                metadata.memberName(),
                metadata.methodType(),
                "static-method".equals(metadata.kind()),
                purity,
                message,
                cause);
    }
}
