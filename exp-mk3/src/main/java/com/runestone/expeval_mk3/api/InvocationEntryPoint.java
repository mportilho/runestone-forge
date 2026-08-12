package com.runestone.expeval_mk3.api;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/** Prepared reflection-free invocation shared by functions and registered Java members. */
@SuppressWarnings("removal")
final class InvocationEntryPoint {

    private static final int MAX_DIRECT_ARITY = 4;
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final Object target;

    private InvocationEntryPoint(MethodHandle implementationHandle) {
        int arity = implementationHandle.type().parameterCount();
        target = arity <= MAX_DIRECT_ARITY
                ? buildDirectEntryPoint(implementationHandle, arity)
                : buildSpreaderEntryPoint(implementationHandle, arity);
    }

    static InvocationEntryPoint prepare(MethodHandle implementationHandle) {
        return new InvocationEntryPoint(implementationHandle);
    }

    Object invoke() throws Throwable {
        if (target instanceof MethodHandle handle) {
            return (Object) handle.invokeExact();
        }
        return ((Invoker0) target).call();
    }

    Object invoke(Object argument0) throws Throwable {
        if (target instanceof MethodHandle handle) {
            return (Object) handle.invokeExact(argument0);
        }
        return ((Invoker1) target).call(argument0);
    }

    Object invoke(Object argument0, Object argument1) throws Throwable {
        if (target instanceof MethodHandle handle) {
            return (Object) handle.invokeExact(argument0, argument1);
        }
        return ((Invoker2) target).call(argument0, argument1);
    }

    Object invoke(Object argument0, Object argument1, Object argument2) throws Throwable {
        if (target instanceof MethodHandle handle) {
            return (Object) handle.invokeExact(argument0, argument1, argument2);
        }
        return ((Invoker3) target).call(argument0, argument1, argument2);
    }

    Object invoke(Object argument0, Object argument1, Object argument2, Object argument3) throws Throwable {
        if (target instanceof MethodHandle handle) {
            return (Object) handle.invokeExact(argument0, argument1, argument2, argument3);
        }
        return ((Invoker4) target).call(argument0, argument1, argument2, argument3);
    }

    Object invokeArray(Object[] arguments) throws Throwable {
        return (Object) ((MethodHandle) target).invokeExact(arguments);
    }

    private static Object buildDirectEntryPoint(MethodHandle implementationHandle, int arity) {
        MethodType genericType = MethodType.genericMethodType(arity);
        try {
            CallSite callSite = LambdaMetafactory.metafactory(
                    LOOKUP,
                    "call",
                    MethodType.methodType(invokerInterface(arity)),
                    genericType,
                    implementationHandle,
                    implementationHandle.type());
            return callSite.getTarget().invoke();
        } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (Throwable linkingFailure) {
            // Non-direct and otherwise uncrackable handles use ADR 0020's pre-adapted fallback.
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
}
