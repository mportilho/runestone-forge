package com.runestone.expeval_mk3.api;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * Registered callable metadata used by semantic function binding.
 */
public final class FunctionDescriptor {

    private final FunctionSignature signature;
    private final ExpressionType returnType;
    private final MethodHandle implementationHandle;
    private final String implementationDescription;
    private final boolean pure;
    private final boolean foldable;

    private FunctionDescriptor(Builder builder) {
        signature = FunctionSignature.of(builder.languageName, builder.parameterTypes);
        returnType = Objects.requireNonNull(builder.returnType, "returnType");
        implementationHandle = Objects.requireNonNull(builder.implementationHandle, "implementationHandle");
        implementationDescription = validateImplementationDescription(
                Objects.requireNonNull(builder.implementationDescription, "implementationDescription"));
        pure = builder.pure;
        foldable = validateFoldable(pure, builder.foldable);
        if (implementationHandle.type().parameterCount() != signature.arity()) {
            throw new IllegalArgumentException("implementation handle arity must match function signature arity");
        }
    }

    public static Builder builder(String languageName) {
        return new Builder(languageName);
    }

    public String languageName() {
        return signature.languageName();
    }

    public int arity() {
        return signature.arity();
    }

    public List<ExpressionType> parameterTypes() {
        return signature.parameterTypes();
    }

    public ExpressionType returnType() {
        return returnType;
    }

    public FunctionSignature signature() {
        return signature;
    }

    public MethodHandle implementationHandle() {
        return implementationHandle;
    }

    public String implementationDescription() {
        return implementationDescription;
    }

    public boolean isPure() {
        return pure;
    }

    public boolean isFoldable() {
        return foldable;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FunctionDescriptor that)) {
            return false;
        }
        return pure == that.pure
                && foldable == that.foldable
                && signature.equals(that.signature)
                && returnType.equals(that.returnType)
                && implementationHandle.type().equals(that.implementationHandle.type())
                && implementationDescription.equals(that.implementationDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                signature,
                returnType,
                implementationHandle.type(),
                implementationDescription,
                pure,
                foldable);
    }

    @Override
    public String toString() {
        return "FunctionDescriptor[signature=" + signature
                + ", returnType=" + returnType
                + ", pure=" + pure
                + ", foldable=" + foldable
                + ']';
    }

    private static String validateImplementationDescription(String implementationDescription) {
        Objects.requireNonNull(implementationDescription, "implementationDescription");
        if (implementationDescription.isBlank()) {
            throw new IllegalArgumentException("implementation description must not be blank");
        }
        return implementationDescription;
    }

    private static boolean validateFoldable(boolean pure, boolean foldable) {
        if (foldable && !pure) {
            throw new IllegalArgumentException("foldable functions must also be pure");
        }
        return foldable;
    }

    public static final class Builder {

        private final String languageName;
        private List<ExpressionType> parameterTypes = List.of();
        private ExpressionType returnType;
        private MethodHandle implementationHandle;
        private String implementationDescription;
        private boolean pure;
        private boolean foldable;

        private Builder(String languageName) {
            this.languageName = FunctionSignature.validateLanguageName(languageName);
        }

        public Builder parameterTypes(List<ExpressionType> parameterTypes) {
            this.parameterTypes = List.copyOf(Objects.requireNonNull(parameterTypes, "parameterTypes"));
            return this;
        }

        public Builder returnType(ExpressionType returnType) {
            this.returnType = Objects.requireNonNull(returnType, "returnType");
            return this;
        }

        public Builder implementationHandle(MethodHandle implementationHandle, String implementationDescription) {
            this.implementationHandle = Objects.requireNonNull(implementationHandle, "implementationHandle");
            this.implementationDescription = validateImplementationDescription(implementationDescription);
            return this;
        }

        public Builder implementationMethod(Method method) {
            Objects.requireNonNull(method, "method");
            try {
                implementationHandle = MethodHandles.publicLookup().unreflect(method);
                implementationDescription = method.getDeclaringClass().getName() + '.' + method.getName();
                return this;
            } catch (IllegalAccessException exception) {
                throw new IllegalArgumentException("function implementation method is not accessible: " + method, exception);
            }
        }

        public Builder pure() {
            pure = true;
            return this;
        }

        public Builder foldable() {
            foldable = true;
            return this;
        }

        public FunctionDescriptor build() {
            return new FunctionDescriptor(this);
        }
    }
}
