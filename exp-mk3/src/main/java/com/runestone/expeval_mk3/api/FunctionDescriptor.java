package com.runestone.expeval_mk3.api;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;

/**
 * Description of a function available to semantic resolution and later runtime planning.
 */
public final class FunctionDescriptor {

    private final String languageName;
    private final List<ExpressionType> parameterTypes;
    private final ExpressionType returnType;
    private final MethodHandle implementationHandle;
    private final FunctionImplementationMetadata implementationMetadata;
    private final FunctionPurity purity;

    private FunctionDescriptor(
            String languageName,
            List<ExpressionType> parameterTypes,
            ExpressionType returnType,
            MethodHandle implementationHandle,
            FunctionImplementationMetadata implementationMetadata,
            FunctionPurity purity) {
        this.languageName = FunctionSignature.validateLanguageName(languageName);
        this.parameterTypes = ExpressionTypes.copyOf(parameterTypes, "parameterTypes");
        this.returnType = Objects.requireNonNull(returnType, "returnType");
        this.implementationHandle = Objects.requireNonNull(implementationHandle, "implementationHandle");
        this.implementationMetadata = Objects.requireNonNull(implementationMetadata, "implementationMetadata");
        this.purity = Objects.requireNonNull(purity, "purity");
        if (implementationHandle.type().parameterCount() != this.parameterTypes.size()) {
            throw new IllegalArgumentException("implementation handle arity must match parameter types");
        }
    }

    public static FunctionDescriptor fromMethod(
            String languageName,
            Method method,
            List<ExpressionType> parameterTypes,
            ExpressionType returnType,
            FunctionPurity purity) {
        Objects.requireNonNull(method, "method");
        parameterTypes = ExpressionTypes.copyOf(parameterTypes, "parameterTypes");
        Objects.requireNonNull(returnType, "returnType");
        rejectNullType(parameterTypes, returnType);
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("function implementation method must be static");
        }
        if (method.getParameterCount() != parameterTypes.size()) {
            throw new IllegalArgumentException("method arity must match parameter types");
        }
        validateJavaSignature(method, parameterTypes, returnType);
        try {
            MethodHandle methodHandle = MethodHandles.lookup().unreflect(method);
            return new FunctionDescriptor(
                    languageName,
                    parameterTypes,
                    returnType,
                    adapt(methodHandle, parameterTypes.size()),
                    FunctionImplementationMetadata.forMethod(method),
                    purity);
        } catch (IllegalAccessException exception) {
            throw new IllegalArgumentException("function implementation method is not accessible: " + method, exception);
        }
    }

    static FunctionDescriptor fromHandle(
            String languageName,
            MethodHandle implementationHandle,
            FunctionImplementationMetadata implementationMetadata,
            List<ExpressionType> parameterTypes,
            ExpressionType returnType,
            FunctionPurity purity) {
        Objects.requireNonNull(implementationHandle, "implementationHandle");
        Objects.requireNonNull(implementationMetadata, "implementationMetadata");
        Objects.requireNonNull(purity, "purity");
        parameterTypes = ExpressionTypes.copyOf(parameterTypes, "parameterTypes");
        Objects.requireNonNull(returnType, "returnType");
        rejectNullType(parameterTypes, returnType);
        if (implementationHandle.type().parameterCount() != parameterTypes.size()) {
            throw new IllegalArgumentException("implementation handle arity must match parameter types");
        }
        validateHandleType(implementationHandle, parameterTypes, returnType);
        return new FunctionDescriptor(
                languageName,
                parameterTypes,
                returnType,
                implementationHandle,
                implementationMetadata,
                purity);
    }

    public String languageName() {
        return languageName;
    }

    public List<ExpressionType> parameterTypes() {
        return parameterTypes;
    }

    public ExpressionType returnType() {
        return returnType;
    }

    public MethodHandle implementationHandle() {
        return implementationHandle;
    }

    public FunctionImplementationMetadata implementationMetadata() {
        return implementationMetadata;
    }

    public boolean pure() {
        return purity.pure();
    }

    public boolean foldable() {
        return purity.foldable();
    }

    public int arity() {
        return parameterTypes.size();
    }

    public FunctionSignature signature() {
        return new FunctionSignature(languageName, parameterTypes);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FunctionDescriptor that)) {
            return false;
        }
        return languageName.equals(that.languageName)
                && parameterTypes.equals(that.parameterTypes)
                && returnType.equals(that.returnType)
                && implementationMetadata.equals(that.implementationMetadata)
                && purity == that.purity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(languageName, parameterTypes, returnType, implementationMetadata, purity);
    }

    @Override
    public String toString() {
        return "FunctionDescriptor[signature=" + signature().canonical() + ", returnType=" + returnType + ']';
    }

    private static MethodHandle adapt(MethodHandle methodHandle, int arity) {
        Class<?>[] parameterTypes = new Class<?>[arity];
        for (int index = 0; index < parameterTypes.length; index++) {
            parameterTypes[index] = Object.class;
        }
        return methodHandle.asType(MethodType.methodType(Object.class, parameterTypes));
    }

    private static void validateJavaSignature(
            Method method,
            List<ExpressionType> parameterTypes,
            ExpressionType returnType) {
        Class<?>[] methodParameterTypes = method.getParameterTypes();
        for (int index = 0; index < parameterTypes.size(); index++) {
            Class<?> expectedValueType = ExpressionJavaTypes.valueType(parameterTypes.get(index));
            if (expectedValueType != null
                    && !ExpressionJavaTypes.boxed(methodParameterTypes[index]).isAssignableFrom(expectedValueType)) {
                throw new IllegalArgumentException("method parameter " + index + " does not accept "
                        + parameterTypes.get(index));
            }
        }

        Class<?> expectedReturnType = ExpressionJavaTypes.valueType(returnType);
        if (method.getReturnType() == void.class) {
            throw new IllegalArgumentException("method return type must not be void");
        }
        if (expectedReturnType != null && !expectedReturnType.isAssignableFrom(ExpressionJavaTypes.boxed(method.getReturnType()))) {
            throw new IllegalArgumentException("method return type does not produce " + returnType);
        }
    }

    private static void validateHandleType(
            MethodHandle implementationHandle,
            List<ExpressionType> parameterTypes,
            ExpressionType returnType) {
        MethodType methodType = implementationHandle.type();
        for (int index = 0; index < parameterTypes.size(); index++) {
            Class<?> expectedValueType = ExpressionJavaTypes.valueType(parameterTypes.get(index));
            if (expectedValueType != null
                    && !ExpressionJavaTypes.boxed(methodType.parameterType(index)).isAssignableFrom(expectedValueType)) {
                throw new IllegalArgumentException("handle parameter " + index + " does not accept "
                        + parameterTypes.get(index));
            }
        }

        Class<?> expectedReturnType = ExpressionJavaTypes.valueType(returnType);
        if (methodType.returnType() == void.class) {
            throw new IllegalArgumentException("handle return type must not be void");
        }
        if (expectedReturnType != null
                && !expectedReturnType.isAssignableFrom(ExpressionJavaTypes.boxed(methodType.returnType()))) {
            throw new IllegalArgumentException("handle return type does not produce " + returnType);
        }
    }

    private static void rejectNullType(List<ExpressionType> parameterTypes, ExpressionType returnType) {
        for (ExpressionType parameterType : parameterTypes) {
            if (parameterType == NullType.INSTANCE) {
                throw new IllegalArgumentException("function parameter type must not be NullType");
            }
        }
        if (returnType == NullType.INSTANCE) {
            throw new IllegalArgumentException("function return type must not be NullType");
        }
    }
}
