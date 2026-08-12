package com.runestone.expeval_mk3.api;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
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

    private static final int MAX_DIRECT_ARITY = 4;
    private static final MethodHandles.Lookup ENTRY_POINT_LOOKUP = MethodHandles.lookup();

    private final String languageName;
    private final List<ExpressionType> parameterTypes;
    private final ExpressionType returnType;
    private final MethodHandle implementationHandle;
    private final FunctionImplementationMetadata implementationMetadata;
    private final FunctionPurity purity;

    /**
     * Reflection-free invocation entry point, built once here rather than per plan call-site
     * (ADR 0020). For arity 0-4 this is either a {@code LambdaMetafactory}-linked {@code InvokerN}
     * instance (when {@code implementationHandle} is a direct handle {@code LambdaMetafactory} can
     * crack) or, when linking is not possible, a plain {@link MethodHandle} pre-adapted to
     * {@code MethodType.genericMethodType(arity)} and invoked with {@code invokeExact} at a fixed
     * call site. For arity 5+ it is always a {@link MethodHandle} pre-adapted with
     * {@link MethodHandle#asSpreader}. The node executing a call does not know which shape backs it.
     */
    private final Object entryPoint;

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
        int arity = this.parameterTypes.size();
        this.entryPoint = arity <= MAX_DIRECT_ARITY
                ? buildDirectEntryPoint(implementationHandle, arity)
                : buildSpreaderEntryPoint(implementationHandle, arity);
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
                    FunctionImplementationMetadata.forStaticMethod(method),
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

    /**
     * Invokes an arity-0 function through the generated entry point. The caller must hold
     * {@code arity() == 0}; the arity is not re-checked here because the caller already knows it
     * from the call-site argument count.
     */
    public Object invoke() throws Throwable {
        if (entryPoint instanceof MethodHandle handle) {
            return (Object) handle.invokeExact();
        }
        return ((Invoker0) entryPoint).call();
    }

    public Object invoke(Object argument0) throws Throwable {
        if (entryPoint instanceof MethodHandle handle) {
            return (Object) handle.invokeExact(argument0);
        }
        return ((Invoker1) entryPoint).call(argument0);
    }

    public Object invoke(Object argument0, Object argument1) throws Throwable {
        if (entryPoint instanceof MethodHandle handle) {
            return (Object) handle.invokeExact(argument0, argument1);
        }
        return ((Invoker2) entryPoint).call(argument0, argument1);
    }

    public Object invoke(Object argument0, Object argument1, Object argument2) throws Throwable {
        if (entryPoint instanceof MethodHandle handle) {
            return (Object) handle.invokeExact(argument0, argument1, argument2);
        }
        return ((Invoker3) entryPoint).call(argument0, argument1, argument2);
    }

    public Object invoke(Object argument0, Object argument1, Object argument2, Object argument3) throws Throwable {
        if (entryPoint instanceof MethodHandle handle) {
            return (Object) handle.invokeExact(argument0, argument1, argument2, argument3);
        }
        return ((Invoker4) entryPoint).call(argument0, argument1, argument2, argument3);
    }

    /**
     * Invokes an arity-5+ function. {@code arguments.length} must equal {@code arity()}; the
     * pre-adapted spreader handle enforces that at the {@code invokeExact} call below.
     */
    public Object invokeArray(Object[] arguments) throws Throwable {
        return (Object) ((MethodHandle) entryPoint).invokeExact(arguments);
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

    /**
     * {@code LambdaMetafactory} can only crack a direct method handle. Every handle this class
     * receives has already passed through {@code asType} ({@code fromMethod}) or through
     * {@code ProviderMethodAdapter}'s {@code filterArguments}/{@code filterReturnValue} chain and,
     * for exposed-instance providers, {@code bindTo} ({@code fromHandle} via
     * {@code ReflectedFunctionImporter}) — none of those are direct. Linking is therefore attempted
     * opportunistically (it succeeds whenever boundary-coercion elision leaves the handle
     * untouched) and falls back to a plain, pre-adapted {@link MethodHandle} otherwise; both routes
     * are reflection-free and array-free. See ADR 0020's amendment for the measurement behind this.
     * A fatal JVM error surfacing during linking (out-of-memory, stack overflow, a linkage error
     * unrelated to handle directness) must still propagate rather than be silently converted into
     * the fallback path; only the expected negative case — {@code LambdaConversionException} for a
     * non-direct handle, or any other non-fatal failure to link — falls back.
     */
    private static Object buildDirectEntryPoint(MethodHandle implementationHandle, int arity) {
        MethodType genericType = MethodType.genericMethodType(arity);
        try {
            CallSite callSite = LambdaMetafactory.metafactory(
                    ENTRY_POINT_LOOKUP,
                    "call",
                    MethodType.methodType(invokerInterface(arity)),
                    genericType,
                    implementationHandle,
                    implementationHandle.type());
            return callSite.getTarget().invoke();
        } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (Throwable linkingFailure) {
            return implementationHandle.asType(genericType);
        }
    }

    private static MethodHandle buildSpreaderEntryPoint(MethodHandle implementationHandle, int arity) {
        MethodHandle generic = implementationHandle.asType(
                MethodType.methodType(Object.class, implementationHandle.type().parameterArray()));
        return generic.asSpreader(Object[].class, arity);
    }

    private static Class<?> invokerInterface(int arity) {
        return switch (arity) {
            case 0 -> Invoker0.class;
            case 1 -> Invoker1.class;
            case 2 -> Invoker2.class;
            case 3 -> Invoker3.class;
            case 4 -> Invoker4.class;
            default -> throw new IllegalArgumentException("unsupported direct invocation arity: " + arity);
        };
    }

    @FunctionalInterface
    private interface Invoker0 {
        Object call() throws Throwable;
    }

    @FunctionalInterface
    private interface Invoker1 {
        Object call(Object argument0) throws Throwable;
    }

    @FunctionalInterface
    private interface Invoker2 {
        Object call(Object argument0, Object argument1) throws Throwable;
    }

    @FunctionalInterface
    private interface Invoker3 {
        Object call(Object argument0, Object argument1, Object argument2) throws Throwable;
    }

    @FunctionalInterface
    private interface Invoker4 {
        Object call(Object argument0, Object argument1, Object argument2, Object argument3) throws Throwable;
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

}
