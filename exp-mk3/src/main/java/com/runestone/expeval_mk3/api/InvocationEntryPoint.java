package com.runestone.expeval_mk3.api;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/** Prepared reflection-free invocation shared by functions and registered Java members. */
@SuppressWarnings("removal")
final class InvocationEntryPoint {

    private static final int MAX_DIRECT_ARITY = 10;
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final int arity;
    private final boolean direct;
    private final Object target;

    private InvocationEntryPoint(MethodHandle implementationHandle, int maxDirectArity) {
        arity = implementationHandle.type().parameterCount();
        direct = arity <= maxDirectArity;
        target = direct
                ? buildDirectEntryPoint(implementationHandle, arity)
                : buildSpreaderEntryPoint(implementationHandle, arity);
    }

    static InvocationEntryPoint prepare(MethodHandle implementationHandle) {
        return new InvocationEntryPoint(implementationHandle, MAX_DIRECT_ARITY);
    }

    static InvocationEntryPoint prepare(MethodHandle implementationHandle, int maxDirectArity) {
        if (maxDirectArity < 0 || maxDirectArity > 11) {
            throw new IllegalArgumentException("max direct arity must be between 0 and 11");
        }
        return new InvocationEntryPoint(implementationHandle, maxDirectArity);
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

    Object invoke(Object argument0, Object argument1, Object argument2, Object argument3, Object argument4)
            throws Throwable {
        if (target instanceof MethodHandle handle) {
            return (Object) handle.invokeExact(argument0, argument1, argument2, argument3, argument4);
        }
        return ((Invoker5) target).call(argument0, argument1, argument2, argument3, argument4);
    }

    Object invoke(
            Object argument0, Object argument1, Object argument2, Object argument3, Object argument4,
            Object argument5) throws Throwable {
        if (target instanceof MethodHandle handle) {
            return (Object) handle.invokeExact(
                    argument0, argument1, argument2, argument3, argument4, argument5);
        }
        return ((Invoker6) target).call(argument0, argument1, argument2, argument3, argument4, argument5);
    }

    Object invoke(
            Object argument0, Object argument1, Object argument2, Object argument3, Object argument4,
            Object argument5, Object argument6) throws Throwable {
        if (target instanceof MethodHandle handle) {
            return (Object) handle.invokeExact(
                    argument0, argument1, argument2, argument3, argument4, argument5, argument6);
        }
        return ((Invoker7) target).call(
                argument0, argument1, argument2, argument3, argument4, argument5, argument6);
    }

    Object invoke(
            Object argument0, Object argument1, Object argument2, Object argument3, Object argument4,
            Object argument5, Object argument6, Object argument7) throws Throwable {
        if (target instanceof MethodHandle handle) {
            return (Object) handle.invokeExact(
                    argument0, argument1, argument2, argument3, argument4, argument5, argument6, argument7);
        }
        return ((Invoker8) target).call(
                argument0, argument1, argument2, argument3, argument4, argument5, argument6, argument7);
    }

    Object invoke(
            Object argument0, Object argument1, Object argument2, Object argument3, Object argument4,
            Object argument5, Object argument6, Object argument7, Object argument8) throws Throwable {
        if (target instanceof MethodHandle handle) {
            return (Object) handle.invokeExact(
                    argument0, argument1, argument2, argument3, argument4, argument5, argument6, argument7,
                    argument8);
        }
        return ((Invoker9) target).call(
                argument0, argument1, argument2, argument3, argument4, argument5, argument6, argument7,
                argument8);
    }

    Object invoke(
            Object argument0, Object argument1, Object argument2, Object argument3, Object argument4,
            Object argument5, Object argument6, Object argument7, Object argument8, Object argument9)
            throws Throwable {
        if (target instanceof MethodHandle handle) {
            return (Object) handle.invokeExact(
                    argument0, argument1, argument2, argument3, argument4, argument5, argument6, argument7,
                    argument8, argument9);
        }
        return ((Invoker10) target).call(
                argument0, argument1, argument2, argument3, argument4, argument5, argument6, argument7,
                argument8, argument9);
    }

    Object invoke(
            Object argument0, Object argument1, Object argument2, Object argument3, Object argument4,
            Object argument5, Object argument6, Object argument7, Object argument8, Object argument9,
            Object argument10) throws Throwable {
        if (target instanceof MethodHandle handle) {
            return (Object) handle.invokeExact(
                    argument0, argument1, argument2, argument3, argument4, argument5, argument6, argument7,
                    argument8, argument9, argument10);
        }
        return ((Invoker11) target).call(
                argument0, argument1, argument2, argument3, argument4, argument5, argument6, argument7,
                argument8, argument9, argument10);
    }

    Object invokeArray(Object[] arguments) throws Throwable {
        if (arguments.length != arity) {
            throw new IllegalArgumentException("argument count must match invocation arity");
        }
        if (!direct) {
            return (Object) ((MethodHandle) target).invokeExact(arguments);
        }
        return switch (arguments.length) {
            case 0 -> invoke();
            case 1 -> invoke(arguments[0]);
            case 2 -> invoke(arguments[0], arguments[1]);
            case 3 -> invoke(arguments[0], arguments[1], arguments[2]);
            case 4 -> invoke(arguments[0], arguments[1], arguments[2], arguments[3]);
            case 5 -> invoke(arguments[0], arguments[1], arguments[2], arguments[3], arguments[4]);
            case 6 -> invoke(arguments[0], arguments[1], arguments[2], arguments[3], arguments[4], arguments[5]);
            case 7 -> invoke(
                    arguments[0], arguments[1], arguments[2], arguments[3], arguments[4], arguments[5],
                    arguments[6]);
            case 8 -> invoke(
                    arguments[0], arguments[1], arguments[2], arguments[3], arguments[4], arguments[5],
                    arguments[6], arguments[7]);
            case 9 -> invoke(
                    arguments[0], arguments[1], arguments[2], arguments[3], arguments[4], arguments[5],
                    arguments[6], arguments[7], arguments[8]);
            case 10 -> invoke(
                    arguments[0], arguments[1], arguments[2], arguments[3], arguments[4], arguments[5],
                    arguments[6], arguments[7], arguments[8], arguments[9]);
            case 11 -> invoke(
                    arguments[0], arguments[1], arguments[2], arguments[3], arguments[4], arguments[5],
                    arguments[6], arguments[7], arguments[8], arguments[9], arguments[10]);
            default -> throw new IllegalStateException("unsupported direct invocation arity: " + arity);
        };
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
            case 5 -> Invoker5.class;
            case 6 -> Invoker6.class;
            case 7 -> Invoker7.class;
            case 8 -> Invoker8.class;
            case 9 -> Invoker9.class;
            case 10 -> Invoker10.class;
            case 11 -> Invoker11.class;
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

    @FunctionalInterface
    private interface Invoker5 {
        Object call(Object argument0, Object argument1, Object argument2, Object argument3, Object argument4)
                throws Throwable;
    }

    @FunctionalInterface
    private interface Invoker6 {
        Object call(
                Object argument0, Object argument1, Object argument2, Object argument3, Object argument4,
                Object argument5) throws Throwable;
    }

    @FunctionalInterface
    private interface Invoker7 {
        Object call(
                Object argument0, Object argument1, Object argument2, Object argument3, Object argument4,
                Object argument5, Object argument6) throws Throwable;
    }

    @FunctionalInterface
    private interface Invoker8 {
        Object call(
                Object argument0, Object argument1, Object argument2, Object argument3, Object argument4,
                Object argument5, Object argument6, Object argument7) throws Throwable;
    }

    @FunctionalInterface
    private interface Invoker9 {
        Object call(
                Object argument0, Object argument1, Object argument2, Object argument3, Object argument4,
                Object argument5, Object argument6, Object argument7, Object argument8) throws Throwable;
    }

    @FunctionalInterface
    private interface Invoker10 {
        Object call(
                Object argument0, Object argument1, Object argument2, Object argument3, Object argument4,
                Object argument5, Object argument6, Object argument7, Object argument8, Object argument9)
                throws Throwable;
    }

    @FunctionalInterface
    private interface Invoker11 {
        Object call(
                Object argument0, Object argument1, Object argument2, Object argument3, Object argument4,
                Object argument5, Object argument6, Object argument7, Object argument8, Object argument9,
                Object argument10) throws Throwable;
    }
}
