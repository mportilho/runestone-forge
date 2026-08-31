package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.FunctionDescriptor;
import com.runestone.expeval_mk3.api.JavaMethodDescriptor;
import com.runestone.expeval_mk3.api.JavaWildcardChildDescriptor;
import com.runestone.expeval_mk3.api.MapType;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.SubscriptBounds;
import com.runestone.expeval_mk3.internal.diagnostics.ProviderReturnViolation;
import com.runestone.expeval_mk3.internal.diagnostics.RuntimeFailures;
import com.runestone.expeval_mk3.internal.semantics.CollectionOperationWiring;
import com.runestone.expeval_mk3.internal.semantics.ContextualMemberNavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.RegisteredMethodNavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.RegisteredPropertyNavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.WildcardNavigationBinding;
import com.runestone.expeval_mk3.api.SourceSpan;

import java.lang.invoke.MethodHandle;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Executes {@link ExecutableNode} trees built by {@code ExecutionPlanBuilder} against an
 * {@link ExecutionScope}: collection/scalar operation dispatch, navigation value access, and
 * the scalar helpers (arithmetic coercion, comparison, structural equality) those operations share.
 */
@SuppressWarnings("removal")
public final class ExpressionRuntime {

    private ExpressionRuntime() {
    }

    public static Object invokeFunction(
            FunctionDescriptor descriptor,
            List<ExecutableNode> argumentNodes,
            ExecutionScope scope,
            SourceSpan callSpan) {
        // Arity 0-10 evaluates arguments positionally and calls its FunctionDescriptor entry point
        // directly, with no Object[] allocation; arity 11+ falls back to the array-based entry
        // point. Argument evaluation and null-checking happen before the try block so
        // RuntimeFailures.forbiddenNull is not reclassified as a provider failure below.
        switch (argumentNodes.size()) {
            case 0:
                try {
                    return descriptor.invoke();
                } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
                    throw fatal;
                } catch (Throwable exception) {
                    throw classify(descriptor, callSpan, exception);
                }
            case 1: {
                Object argument0 = requiredArgument(argumentNodes, 0, scope, descriptor, callSpan);
                try {
                    return descriptor.invoke(argument0);
                } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
                    throw fatal;
                } catch (Throwable exception) {
                    throw classify(descriptor, callSpan, exception);
                }
            }
            case 2: {
                Object argument0 = requiredArgument(argumentNodes, 0, scope, descriptor, callSpan);
                Object argument1 = requiredArgument(argumentNodes, 1, scope, descriptor, callSpan);
                try {
                    return descriptor.invoke(argument0, argument1);
                } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
                    throw fatal;
                } catch (Throwable exception) {
                    throw classify(descriptor, callSpan, exception);
                }
            }
            case 3: {
                Object argument0 = requiredArgument(argumentNodes, 0, scope, descriptor, callSpan);
                Object argument1 = requiredArgument(argumentNodes, 1, scope, descriptor, callSpan);
                Object argument2 = requiredArgument(argumentNodes, 2, scope, descriptor, callSpan);
                try {
                    return descriptor.invoke(argument0, argument1, argument2);
                } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
                    throw fatal;
                } catch (Throwable exception) {
                    throw classify(descriptor, callSpan, exception);
                }
            }
            case 4: {
                Object argument0 = requiredArgument(argumentNodes, 0, scope, descriptor, callSpan);
                Object argument1 = requiredArgument(argumentNodes, 1, scope, descriptor, callSpan);
                Object argument2 = requiredArgument(argumentNodes, 2, scope, descriptor, callSpan);
                Object argument3 = requiredArgument(argumentNodes, 3, scope, descriptor, callSpan);
                try {
                    return descriptor.invoke(argument0, argument1, argument2, argument3);
                } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
                    throw fatal;
                } catch (Throwable exception) {
                    throw classify(descriptor, callSpan, exception);
                }
            }
            case 5: {
                Object argument0 = requiredArgument(argumentNodes, 0, scope, descriptor, callSpan);
                Object argument1 = requiredArgument(argumentNodes, 1, scope, descriptor, callSpan);
                Object argument2 = requiredArgument(argumentNodes, 2, scope, descriptor, callSpan);
                Object argument3 = requiredArgument(argumentNodes, 3, scope, descriptor, callSpan);
                Object argument4 = requiredArgument(argumentNodes, 4, scope, descriptor, callSpan);
                try {
                    return descriptor.invoke(argument0, argument1, argument2, argument3, argument4);
                } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
                    throw fatal;
                } catch (Throwable exception) {
                    throw classify(descriptor, callSpan, exception);
                }
            }
            case 6: {
                Object argument0 = requiredArgument(argumentNodes, 0, scope, descriptor, callSpan);
                Object argument1 = requiredArgument(argumentNodes, 1, scope, descriptor, callSpan);
                Object argument2 = requiredArgument(argumentNodes, 2, scope, descriptor, callSpan);
                Object argument3 = requiredArgument(argumentNodes, 3, scope, descriptor, callSpan);
                Object argument4 = requiredArgument(argumentNodes, 4, scope, descriptor, callSpan);
                Object argument5 = requiredArgument(argumentNodes, 5, scope, descriptor, callSpan);
                try {
                    return descriptor.invoke(argument0, argument1, argument2, argument3, argument4, argument5);
                } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
                    throw fatal;
                } catch (Throwable exception) {
                    throw classify(descriptor, callSpan, exception);
                }
            }
            case 7: {
                Object argument0 = requiredArgument(argumentNodes, 0, scope, descriptor, callSpan);
                Object argument1 = requiredArgument(argumentNodes, 1, scope, descriptor, callSpan);
                Object argument2 = requiredArgument(argumentNodes, 2, scope, descriptor, callSpan);
                Object argument3 = requiredArgument(argumentNodes, 3, scope, descriptor, callSpan);
                Object argument4 = requiredArgument(argumentNodes, 4, scope, descriptor, callSpan);
                Object argument5 = requiredArgument(argumentNodes, 5, scope, descriptor, callSpan);
                Object argument6 = requiredArgument(argumentNodes, 6, scope, descriptor, callSpan);
                try {
                    return descriptor.invoke(
                            argument0, argument1, argument2, argument3, argument4, argument5, argument6);
                } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
                    throw fatal;
                } catch (Throwable exception) {
                    throw classify(descriptor, callSpan, exception);
                }
            }
            case 8: {
                Object argument0 = requiredArgument(argumentNodes, 0, scope, descriptor, callSpan);
                Object argument1 = requiredArgument(argumentNodes, 1, scope, descriptor, callSpan);
                Object argument2 = requiredArgument(argumentNodes, 2, scope, descriptor, callSpan);
                Object argument3 = requiredArgument(argumentNodes, 3, scope, descriptor, callSpan);
                Object argument4 = requiredArgument(argumentNodes, 4, scope, descriptor, callSpan);
                Object argument5 = requiredArgument(argumentNodes, 5, scope, descriptor, callSpan);
                Object argument6 = requiredArgument(argumentNodes, 6, scope, descriptor, callSpan);
                Object argument7 = requiredArgument(argumentNodes, 7, scope, descriptor, callSpan);
                try {
                    return descriptor.invoke(
                            argument0, argument1, argument2, argument3, argument4, argument5, argument6, argument7);
                } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
                    throw fatal;
                } catch (Throwable exception) {
                    throw classify(descriptor, callSpan, exception);
                }
            }
            case 9: {
                Object argument0 = requiredArgument(argumentNodes, 0, scope, descriptor, callSpan);
                Object argument1 = requiredArgument(argumentNodes, 1, scope, descriptor, callSpan);
                Object argument2 = requiredArgument(argumentNodes, 2, scope, descriptor, callSpan);
                Object argument3 = requiredArgument(argumentNodes, 3, scope, descriptor, callSpan);
                Object argument4 = requiredArgument(argumentNodes, 4, scope, descriptor, callSpan);
                Object argument5 = requiredArgument(argumentNodes, 5, scope, descriptor, callSpan);
                Object argument6 = requiredArgument(argumentNodes, 6, scope, descriptor, callSpan);
                Object argument7 = requiredArgument(argumentNodes, 7, scope, descriptor, callSpan);
                Object argument8 = requiredArgument(argumentNodes, 8, scope, descriptor, callSpan);
                try {
                    return descriptor.invoke(
                            argument0, argument1, argument2, argument3, argument4, argument5, argument6, argument7,
                            argument8);
                } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
                    throw fatal;
                } catch (Throwable exception) {
                    throw classify(descriptor, callSpan, exception);
                }
            }
            case 10: {
                Object argument0 = requiredArgument(argumentNodes, 0, scope, descriptor, callSpan);
                Object argument1 = requiredArgument(argumentNodes, 1, scope, descriptor, callSpan);
                Object argument2 = requiredArgument(argumentNodes, 2, scope, descriptor, callSpan);
                Object argument3 = requiredArgument(argumentNodes, 3, scope, descriptor, callSpan);
                Object argument4 = requiredArgument(argumentNodes, 4, scope, descriptor, callSpan);
                Object argument5 = requiredArgument(argumentNodes, 5, scope, descriptor, callSpan);
                Object argument6 = requiredArgument(argumentNodes, 6, scope, descriptor, callSpan);
                Object argument7 = requiredArgument(argumentNodes, 7, scope, descriptor, callSpan);
                Object argument8 = requiredArgument(argumentNodes, 8, scope, descriptor, callSpan);
                Object argument9 = requiredArgument(argumentNodes, 9, scope, descriptor, callSpan);
                try {
                    return descriptor.invoke(
                            argument0, argument1, argument2, argument3, argument4, argument5, argument6, argument7,
                            argument8, argument9);
                } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
                    throw fatal;
                } catch (Throwable exception) {
                    throw classify(descriptor, callSpan, exception);
                }
            }
            default: {
                Object[] arguments = new Object[argumentNodes.size()];
                for (int index = 0; index < argumentNodes.size(); index++) {
                    arguments[index] = requiredArgument(argumentNodes, index, scope, descriptor, callSpan);
                }
                try {
                    return descriptor.invokeArray(arguments);
                } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
                    throw fatal;
                } catch (Throwable exception) {
                    throw classify(descriptor, callSpan, exception);
                }
            }
        }
    }

    private static Object requiredArgument(
            List<ExecutableNode> argumentNodes,
            int index,
            ExecutionScope scope,
            FunctionDescriptor descriptor,
            SourceSpan callSpan) {
        Object argument = argumentNodes.get(index).execute(scope);
        if (argument == null) {
            throw RuntimeFailures.forbiddenNull(
                    "function argument must not be null: " + descriptor.languageName(), callSpan);
        }
        return argument;
    }

    private static RuntimeException classify(FunctionDescriptor descriptor, SourceSpan callSpan, Throwable exception) {
        if (exception instanceof ProviderReturnViolation violation) {
            // The provider ran to completion but its return value fails the resolved return
            // contract (null, incompatible type, or invalid container); distinct from a
            // provider-thrown failure.
            return RuntimeFailures.providerReturnViolation(descriptor.languageName(), callSpan, violation);
        }
        if (exception instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        // Declared checked exceptions, ordinary runtime exceptions, and nonfatal errors thrown by
        // the provider implementation are all provider-invocation failures; fatal JVM conditions
        // are rethrown unchanged by the caller's ThreadDeath|VirtualMachineError|LinkageError catch.
        return RuntimeFailures.providerFailure(descriptor.languageName(), callSpan, exception);
    }

    public static List<Object> materialize(List<ExecutableNode> elements, ExecutionScope scope, SourceSpan sourceSpan) {
        ArrayList<Object> values = new ArrayList<>(elements.size());
        for (ExecutableNode element : elements) {
            values.add(requiredElement(element.execute(scope), sourceSpan));
        }
        return List.copyOf(values);
    }

    public static Object indexedValue(Object receiver, BigInteger index, boolean safe, SourceSpan sourceSpan) {
        if (receiver == null) {
            if (safe) {
                return null;
            }
            throw nullReceiverInvariant(sourceSpan);
        }
        List<?> values = (List<?>) receiver;
        int resolvedIndex = SubscriptBounds.normalizedIndexOrOutOfBounds(index, values.size());
        if (resolvedIndex == SubscriptBounds.INDEX_OUT_OF_BOUNDS) {
            // ADR 0018: an out-of-range index is legitimate absence on a safe link and a failure on a strict one.
            if (safe) {
                return null;
            }
            throw RuntimeFailures.subscriptOutOfBounds(index, values.size(), sourceSpan);
        }
        return requiredElement(values.get(resolvedIndex), sourceSpan);
    }

    public static Object slicedValues(
            Object receiver,
            BigInteger startBound,
            BigInteger endBound,
            boolean safe,
            int maxMaterializedSize,
            SourceSpan sourceSpan) {
        if (receiver == null) {
            if (safe) {
                return null;
            }
            throw nullReceiverInvariant(sourceSpan);
        }
        List<?> values = (List<?>) receiver;
        int start = SubscriptBounds.normalizedSliceBound(startBound, values.size(), 0);
        int end = SubscriptBounds.normalizedSliceBound(endBound, values.size(), values.size());
        if (end < start) {
            end = start;
        }
        requireMaterializedSize(end - start, maxMaterializedSize, sourceSpan);
        ArrayList<Object> result = new ArrayList<>(end - start);
        for (int index = start; index < end; index++) {
            result.add(requiredElement(values.get(index), sourceSpan));
        }
        return List.copyOf(result);
    }

    public static Object mapKeyValue(Object receiver, String key, boolean safe, SourceSpan sourceSpan) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        if (receiver == null) {
            if (safe) {
                return null;
            }
            throw nullReceiverInvariant(sourceSpan);
        }
        Map<?, ?> values = (Map<?, ?>) receiver;
        Object value = values.get(key);
        if (value == null) {
            if (!values.containsKey(key)) {
                // ADR 0018: an absent key is legitimate absence on a safe link and a failure on a strict one.
                if (safe) {
                    return null;
                }
                throw RuntimeFailures.mapKeyNotFound(key, sourceSpan);
            }
            // A present key bound to null violates the map value contract on both link forms.
            throw RuntimeFailures.forbiddenNull("map value must not be null: " + key, sourceSpan);
        }
        return value;
    }

    public static Object filteredValues(
            Object receiver,
            boolean safe,
            ExecutableNode predicate,
            int currentItemSlot,
            ExecutionScope scope,
            int maxMaterializedSize,
            SourceSpan sourceSpan) {
        if (receiver == null) {
            if (safe) {
                return null;
            }
            throw nullReceiverInvariant(sourceSpan);
        }
        List<?> values = (List<?>) receiver;
        ArrayList<Object> result = new ArrayList<>();
        for (Object value : values) {
            Object item = requiredElement(value, sourceSpan);
            Object previous = scope.replace(currentItemSlot, item);
            try {
                if (bool(predicate.execute(scope))) {
                    requireMaterializedSize(result.size() + 1, maxMaterializedSize, sourceSpan);
                    result.add(item);
                }
            } finally {
                scope.restore(currentItemSlot, previous);
            }
        }
        return List.copyOf(result);
    }

    public static Object wildcardValues(
            Object receiver,
            boolean safe,
            WildcardNavigationBinding binding,
            int maxMaterializedSize,
            SourceSpan sourceSpan) {
        if (receiver == null) {
            if (safe) {
                return null;
            }
            throw nullReceiverInvariant(sourceSpan);
        }
        return switch (binding.receiverKind()) {
            case COLLECTION -> collectionWildcardValues(receiver, maxMaterializedSize, sourceSpan);
            case MAP -> mapWildcardValues(receiver, maxMaterializedSize, sourceSpan);
            case OBJECT -> objectWildcardValues(receiver, binding.objectChildren(), maxMaterializedSize, sourceSpan);
        };
    }

    private static List<Object> collectionWildcardValues(
            Object receiver, int maxMaterializedSize, SourceSpan sourceSpan) {
        List<?> values = (List<?>) receiver;
        requireMaterializedSize(values.size(), maxMaterializedSize, sourceSpan);
        ArrayList<Object> result = new ArrayList<>(values.size());
        for (Object value : values) {
            result.add(requiredElement(value, sourceSpan));
        }
        return List.copyOf(result);
    }

    private static List<Object> mapWildcardValues(Object receiver, int maxMaterializedSize, SourceSpan sourceSpan) {
        Map<?, ?> values = (Map<?, ?>) receiver;
        requireMaterializedSize(values.size(), maxMaterializedSize, sourceSpan);
        ArrayList<String> keys = new ArrayList<>(values.size());
        for (Object key : values.keySet()) {
            keys.add((String) requiredMapKey(key, sourceSpan));
        }
        Collections.sort(keys);
        ArrayList<Object> result = new ArrayList<>(keys.size());
        for (String key : keys) {
            result.add(requiredMapValue(values.get(key), sourceSpan));
        }
        return List.copyOf(result);
    }

    @SuppressWarnings("removal")
    private static List<Object> objectWildcardValues(
            Object receiver,
            List<JavaWildcardChildDescriptor> children,
            int maxMaterializedSize,
            SourceSpan sourceSpan) {
        requireMaterializedSize(children.size(), maxMaterializedSize, sourceSpan);
        ArrayList<Object> result = new ArrayList<>(children.size());
        for (JavaWildcardChildDescriptor child : children) {
            Object value;
            try {
                value = child.accessorHandle().invoke(receiver);
            } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
                throw fatal;
            } catch (Throwable exception) {
                // MethodHandle.invoke declares Throwable; every nonfatal accessor failure is a member
                // access failure, including one a safe link must not mask.
                throw RuntimeFailures.memberAccessFailure(child.name(), sourceSpan, exception);
            }
            result.add(requiredMemberValue(value, child.name(), sourceSpan));
        }
        return List.copyOf(result);
    }

    public static Object invokeRegisteredMethod(
            Object receiver,
            boolean safe,
            RegisteredMethodNavigationBinding binding,
            List<ExecutableNode> argumentNodes,
            ExecutionScope scope,
            SourceSpan sourceSpan) {
        if (receiver == null) {
            if (safe) {
                return null;
            }
            throw nullReceiverInvariant(sourceSpan);
        }
        String memberName = binding.implementationMetadata().memberName();
        JavaMethodDescriptor descriptor = binding.descriptor();
        Object result = switch (argumentNodes.size()) {
            case 0 -> invokeRegisteredMethodEntryPoint(descriptor, receiver, memberName, sourceSpan);
            case 1 -> {
                Object argument0 = requiredRegisteredMethodArgument(
                        argumentNodes, 0, scope, memberName, sourceSpan);
                yield invokeRegisteredMethodEntryPoint(descriptor, receiver, argument0, memberName, sourceSpan);
            }
            case 2 -> {
                Object argument0 = requiredRegisteredMethodArgument(
                        argumentNodes, 0, scope, memberName, sourceSpan);
                Object argument1 = requiredRegisteredMethodArgument(
                        argumentNodes, 1, scope, memberName, sourceSpan);
                yield invokeRegisteredMethodEntryPoint(
                        descriptor, receiver, argument0, argument1, memberName, sourceSpan);
            }
            case 3 -> {
                Object argument0 = requiredRegisteredMethodArgument(
                        argumentNodes, 0, scope, memberName, sourceSpan);
                Object argument1 = requiredRegisteredMethodArgument(
                        argumentNodes, 1, scope, memberName, sourceSpan);
                Object argument2 = requiredRegisteredMethodArgument(
                        argumentNodes, 2, scope, memberName, sourceSpan);
                yield invokeRegisteredMethodEntryPoint(
                        descriptor, receiver, argument0, argument1, argument2, memberName, sourceSpan);
            }
            case 4 -> {
                Object argument0 = requiredRegisteredMethodArgument(argumentNodes, 0, scope, memberName, sourceSpan);
                Object argument1 = requiredRegisteredMethodArgument(argumentNodes, 1, scope, memberName, sourceSpan);
                Object argument2 = requiredRegisteredMethodArgument(argumentNodes, 2, scope, memberName, sourceSpan);
                Object argument3 = requiredRegisteredMethodArgument(argumentNodes, 3, scope, memberName, sourceSpan);
                yield invokeRegisteredMethodEntryPoint(
                        descriptor, receiver, argument0, argument1, argument2, argument3, memberName, sourceSpan);
            }
            case 5 -> {
                Object argument0 = requiredRegisteredMethodArgument(argumentNodes, 0, scope, memberName, sourceSpan);
                Object argument1 = requiredRegisteredMethodArgument(argumentNodes, 1, scope, memberName, sourceSpan);
                Object argument2 = requiredRegisteredMethodArgument(argumentNodes, 2, scope, memberName, sourceSpan);
                Object argument3 = requiredRegisteredMethodArgument(argumentNodes, 3, scope, memberName, sourceSpan);
                Object argument4 = requiredRegisteredMethodArgument(argumentNodes, 4, scope, memberName, sourceSpan);
                yield invokeRegisteredMethodEntryPoint(
                        descriptor, receiver, argument0, argument1, argument2, argument3, argument4,
                        memberName, sourceSpan);
            }
            case 6 -> {
                Object argument0 = requiredRegisteredMethodArgument(argumentNodes, 0, scope, memberName, sourceSpan);
                Object argument1 = requiredRegisteredMethodArgument(argumentNodes, 1, scope, memberName, sourceSpan);
                Object argument2 = requiredRegisteredMethodArgument(argumentNodes, 2, scope, memberName, sourceSpan);
                Object argument3 = requiredRegisteredMethodArgument(argumentNodes, 3, scope, memberName, sourceSpan);
                Object argument4 = requiredRegisteredMethodArgument(argumentNodes, 4, scope, memberName, sourceSpan);
                Object argument5 = requiredRegisteredMethodArgument(argumentNodes, 5, scope, memberName, sourceSpan);
                yield invokeRegisteredMethodEntryPoint(
                        descriptor, receiver, argument0, argument1, argument2, argument3, argument4, argument5,
                        memberName, sourceSpan);
            }
            case 7 -> {
                Object argument0 = requiredRegisteredMethodArgument(argumentNodes, 0, scope, memberName, sourceSpan);
                Object argument1 = requiredRegisteredMethodArgument(argumentNodes, 1, scope, memberName, sourceSpan);
                Object argument2 = requiredRegisteredMethodArgument(argumentNodes, 2, scope, memberName, sourceSpan);
                Object argument3 = requiredRegisteredMethodArgument(argumentNodes, 3, scope, memberName, sourceSpan);
                Object argument4 = requiredRegisteredMethodArgument(argumentNodes, 4, scope, memberName, sourceSpan);
                Object argument5 = requiredRegisteredMethodArgument(argumentNodes, 5, scope, memberName, sourceSpan);
                Object argument6 = requiredRegisteredMethodArgument(argumentNodes, 6, scope, memberName, sourceSpan);
                yield invokeRegisteredMethodEntryPoint(
                        descriptor, receiver, argument0, argument1, argument2, argument3, argument4, argument5,
                        argument6, memberName, sourceSpan);
            }
            case 8 -> {
                Object argument0 = requiredRegisteredMethodArgument(argumentNodes, 0, scope, memberName, sourceSpan);
                Object argument1 = requiredRegisteredMethodArgument(argumentNodes, 1, scope, memberName, sourceSpan);
                Object argument2 = requiredRegisteredMethodArgument(argumentNodes, 2, scope, memberName, sourceSpan);
                Object argument3 = requiredRegisteredMethodArgument(argumentNodes, 3, scope, memberName, sourceSpan);
                Object argument4 = requiredRegisteredMethodArgument(argumentNodes, 4, scope, memberName, sourceSpan);
                Object argument5 = requiredRegisteredMethodArgument(argumentNodes, 5, scope, memberName, sourceSpan);
                Object argument6 = requiredRegisteredMethodArgument(argumentNodes, 6, scope, memberName, sourceSpan);
                Object argument7 = requiredRegisteredMethodArgument(argumentNodes, 7, scope, memberName, sourceSpan);
                yield invokeRegisteredMethodEntryPoint(
                        descriptor, receiver, argument0, argument1, argument2, argument3, argument4, argument5,
                        argument6, argument7, memberName, sourceSpan);
            }
            case 9 -> {
                Object argument0 = requiredRegisteredMethodArgument(argumentNodes, 0, scope, memberName, sourceSpan);
                Object argument1 = requiredRegisteredMethodArgument(argumentNodes, 1, scope, memberName, sourceSpan);
                Object argument2 = requiredRegisteredMethodArgument(argumentNodes, 2, scope, memberName, sourceSpan);
                Object argument3 = requiredRegisteredMethodArgument(argumentNodes, 3, scope, memberName, sourceSpan);
                Object argument4 = requiredRegisteredMethodArgument(argumentNodes, 4, scope, memberName, sourceSpan);
                Object argument5 = requiredRegisteredMethodArgument(argumentNodes, 5, scope, memberName, sourceSpan);
                Object argument6 = requiredRegisteredMethodArgument(argumentNodes, 6, scope, memberName, sourceSpan);
                Object argument7 = requiredRegisteredMethodArgument(argumentNodes, 7, scope, memberName, sourceSpan);
                Object argument8 = requiredRegisteredMethodArgument(argumentNodes, 8, scope, memberName, sourceSpan);
                yield invokeRegisteredMethodEntryPoint(
                        descriptor, receiver, argument0, argument1, argument2, argument3, argument4, argument5,
                        argument6, argument7, argument8, memberName, sourceSpan);
            }
            case 10 -> {
                Object argument0 = requiredRegisteredMethodArgument(argumentNodes, 0, scope, memberName, sourceSpan);
                Object argument1 = requiredRegisteredMethodArgument(argumentNodes, 1, scope, memberName, sourceSpan);
                Object argument2 = requiredRegisteredMethodArgument(argumentNodes, 2, scope, memberName, sourceSpan);
                Object argument3 = requiredRegisteredMethodArgument(argumentNodes, 3, scope, memberName, sourceSpan);
                Object argument4 = requiredRegisteredMethodArgument(argumentNodes, 4, scope, memberName, sourceSpan);
                Object argument5 = requiredRegisteredMethodArgument(argumentNodes, 5, scope, memberName, sourceSpan);
                Object argument6 = requiredRegisteredMethodArgument(argumentNodes, 6, scope, memberName, sourceSpan);
                Object argument7 = requiredRegisteredMethodArgument(argumentNodes, 7, scope, memberName, sourceSpan);
                Object argument8 = requiredRegisteredMethodArgument(argumentNodes, 8, scope, memberName, sourceSpan);
                Object argument9 = requiredRegisteredMethodArgument(argumentNodes, 9, scope, memberName, sourceSpan);
                yield invokeRegisteredMethodEntryPoint(
                        descriptor, receiver, argument0, argument1, argument2, argument3, argument4, argument5,
                        argument6, argument7, argument8, argument9, memberName, sourceSpan);
            }
            default -> invokeRegisteredMethodEntryPoint(
                    descriptor,
                    registeredMethodArguments(receiver, argumentNodes, scope, memberName, sourceSpan),
                    memberName,
                    sourceSpan);
        };
        return requiredMemberValue(result, memberName, sourceSpan);
    }

    @SuppressWarnings("removal")
    public static Object oracleInvokeRegisteredMethod(
            Object receiver,
            boolean safe,
            RegisteredMethodNavigationBinding binding,
            List<ExecutableNode> argumentNodes,
            ExecutionScope scope,
            SourceSpan sourceSpan) {
        if (receiver == null) {
            if (safe) {
                return null;
            }
            throw nullReceiverInvariant(sourceSpan);
        }
        Object[] arguments = new Object[argumentNodes.size() + 1];
        arguments[0] = receiver;
        // Argument nodes run outside the invocation try block so a navigation failure inside an
        // argument keeps its own diagnostic instead of being relabelled a member access failure.
        for (int index = 0; index < argumentNodes.size(); index++) {
            Object argument = argumentNodes.get(index).execute(scope);
            if (argument == null) {
                throw RuntimeFailures.forbiddenNull(
                        "registered method argument must not be null: "
                                + binding.implementationMetadata().memberName(), sourceSpan);
            }
            arguments[index + 1] = argument;
        }
        String memberName = binding.implementationMetadata().memberName();
        Object result;
        try {
            result = binding.invocationHandle().invokeWithArguments(arguments);
        } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (Throwable exception) {
            // MethodHandle.invokeWithArguments declares Throwable; this boundary preserves method failures.
            throw RuntimeFailures.memberAccessFailure(memberName, sourceSpan, exception);
        }
        return requiredMemberValue(result, memberName, sourceSpan);
    }

    public static Object registeredPropertyValue(
            Object receiver,
            boolean safe,
            RegisteredPropertyNavigationBinding binding,
            SourceSpan sourceSpan) {
        if (receiver == null) {
            if (safe) {
                return null;
            }
            throw nullReceiverInvariant(sourceSpan);
        }
        String memberName = binding.implementationMetadata().memberName();
        Object value;
        try {
            value = binding.descriptor().invoke(receiver);
        } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (Throwable exception) {
            throw RuntimeFailures.memberAccessFailure(memberName, sourceSpan, exception);
        }
        return requiredMemberValue(value, memberName, sourceSpan);
    }

    @SuppressWarnings("removal")
    public static Object oracleRegisteredPropertyValue(
            Object receiver,
            boolean safe,
            RegisteredPropertyNavigationBinding binding,
            SourceSpan sourceSpan) {
        if (receiver == null) {
            if (safe) {
                return null;
            }
            throw nullReceiverInvariant(sourceSpan);
        }
        String memberName = binding.implementationMetadata().memberName();
        Object value;
        try {
            value = binding.accessorHandle().invoke(receiver);
        } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (Throwable exception) {
            // MethodHandle.invoke declares Throwable; this boundary preserves accessor failures.
            throw RuntimeFailures.memberAccessFailure(memberName, sourceSpan, exception);
        }
        return requiredMemberValue(value, memberName, sourceSpan);
    }

    private static Object requiredRegisteredMethodArgument(
            List<ExecutableNode> argumentNodes,
            int index,
            ExecutionScope scope,
            String memberName,
            SourceSpan sourceSpan) {
        Object argument = argumentNodes.get(index).execute(scope);
        if (argument == null) {
            throw RuntimeFailures.forbiddenNull(
                    "registered method argument must not be null: " + memberName, sourceSpan);
        }
        return argument;
    }

    private static Object[] registeredMethodArguments(
            Object receiver,
            List<ExecutableNode> argumentNodes,
            ExecutionScope scope,
            String memberName,
            SourceSpan sourceSpan) {
        Object[] arguments = new Object[argumentNodes.size() + 1];
        arguments[0] = receiver;
        for (int index = 0; index < argumentNodes.size(); index++) {
            arguments[index + 1] = requiredRegisteredMethodArgument(
                    argumentNodes, index, scope, memberName, sourceSpan);
        }
        return arguments;
    }

    private static Object invokeRegisteredMethodEntryPoint(
            JavaMethodDescriptor descriptor, Object receiver, String memberName, SourceSpan sourceSpan) {
        try {
            return descriptor.invoke(receiver);
        } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (Throwable exception) {
            throw RuntimeFailures.memberAccessFailure(memberName, sourceSpan, exception);
        }
    }

    private static Object invokeRegisteredMethodEntryPoint(
            JavaMethodDescriptor descriptor,
            Object receiver,
            Object argument0,
            String memberName,
            SourceSpan sourceSpan) {
        try {
            return descriptor.invoke(receiver, argument0);
        } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (Throwable exception) {
            throw RuntimeFailures.memberAccessFailure(memberName, sourceSpan, exception);
        }
    }

    private static Object invokeRegisteredMethodEntryPoint(
            JavaMethodDescriptor descriptor,
            Object receiver,
            Object argument0,
            Object argument1,
            Object argument2,
            Object argument3,
            Object argument4,
            Object argument5,
            Object argument6,
            Object argument7,
            Object argument8,
            Object argument9,
            String memberName,
            SourceSpan sourceSpan) {
        try {
            return descriptor.invoke(
                    receiver, argument0, argument1, argument2, argument3, argument4, argument5, argument6,
                    argument7, argument8, argument9);
        } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (Throwable exception) {
            throw RuntimeFailures.memberAccessFailure(memberName, sourceSpan, exception);
        }
    }

    private static Object invokeRegisteredMethodEntryPoint(
            JavaMethodDescriptor descriptor,
            Object receiver,
            Object argument0,
            Object argument1,
            Object argument2,
            Object argument3,
            String memberName,
            SourceSpan sourceSpan) {
        try {
            return descriptor.invoke(receiver, argument0, argument1, argument2, argument3);
        } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (Throwable exception) {
            throw RuntimeFailures.memberAccessFailure(memberName, sourceSpan, exception);
        }
    }

    private static Object invokeRegisteredMethodEntryPoint(
            JavaMethodDescriptor descriptor,
            Object receiver,
            Object argument0,
            Object argument1,
            Object argument2,
            Object argument3,
            Object argument4,
            String memberName,
            SourceSpan sourceSpan) {
        try {
            return descriptor.invoke(receiver, argument0, argument1, argument2, argument3, argument4);
        } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (Throwable exception) {
            throw RuntimeFailures.memberAccessFailure(memberName, sourceSpan, exception);
        }
    }

    private static Object invokeRegisteredMethodEntryPoint(
            JavaMethodDescriptor descriptor,
            Object receiver,
            Object argument0,
            Object argument1,
            Object argument2,
            Object argument3,
            Object argument4,
            Object argument5,
            String memberName,
            SourceSpan sourceSpan) {
        try {
            return descriptor.invoke(receiver, argument0, argument1, argument2, argument3, argument4, argument5);
        } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (Throwable exception) {
            throw RuntimeFailures.memberAccessFailure(memberName, sourceSpan, exception);
        }
    }

    private static Object invokeRegisteredMethodEntryPoint(
            JavaMethodDescriptor descriptor,
            Object receiver,
            Object argument0,
            Object argument1,
            Object argument2,
            Object argument3,
            Object argument4,
            Object argument5,
            Object argument6,
            String memberName,
            SourceSpan sourceSpan) {
        try {
            return descriptor.invoke(
                    receiver, argument0, argument1, argument2, argument3, argument4, argument5, argument6);
        } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (Throwable exception) {
            throw RuntimeFailures.memberAccessFailure(memberName, sourceSpan, exception);
        }
    }

    private static Object invokeRegisteredMethodEntryPoint(
            JavaMethodDescriptor descriptor,
            Object receiver,
            Object argument0,
            Object argument1,
            Object argument2,
            Object argument3,
            Object argument4,
            Object argument5,
            Object argument6,
            Object argument7,
            String memberName,
            SourceSpan sourceSpan) {
        try {
            return descriptor.invoke(
                    receiver, argument0, argument1, argument2, argument3, argument4, argument5, argument6,
                    argument7);
        } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (Throwable exception) {
            throw RuntimeFailures.memberAccessFailure(memberName, sourceSpan, exception);
        }
    }

    private static Object invokeRegisteredMethodEntryPoint(
            JavaMethodDescriptor descriptor,
            Object receiver,
            Object argument0,
            Object argument1,
            Object argument2,
            Object argument3,
            Object argument4,
            Object argument5,
            Object argument6,
            Object argument7,
            Object argument8,
            String memberName,
            SourceSpan sourceSpan) {
        try {
            return descriptor.invoke(
                    receiver, argument0, argument1, argument2, argument3, argument4, argument5, argument6,
                    argument7, argument8);
        } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (Throwable exception) {
            throw RuntimeFailures.memberAccessFailure(memberName, sourceSpan, exception);
        }
    }

    private static Object invokeRegisteredMethodEntryPoint(
            JavaMethodDescriptor descriptor,
            Object receiver,
            Object argument0,
            Object argument1,
            String memberName,
            SourceSpan sourceSpan) {
        try {
            return descriptor.invoke(receiver, argument0, argument1);
        } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (Throwable exception) {
            throw RuntimeFailures.memberAccessFailure(memberName, sourceSpan, exception);
        }
    }

    private static Object invokeRegisteredMethodEntryPoint(
            JavaMethodDescriptor descriptor,
            Object receiver,
            Object argument0,
            Object argument1,
            Object argument2,
            String memberName,
            SourceSpan sourceSpan) {
        try {
            return descriptor.invoke(receiver, argument0, argument1, argument2);
        } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (Throwable exception) {
            throw RuntimeFailures.memberAccessFailure(memberName, sourceSpan, exception);
        }
    }

    private static Object invokeRegisteredMethodEntryPoint(
            JavaMethodDescriptor descriptor, Object[] arguments, String memberName, SourceSpan sourceSpan) {
        try {
            return descriptor.invokeArray(arguments);
        } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (Throwable exception) {
            throw RuntimeFailures.memberAccessFailure(memberName, sourceSpan, exception);
        }
    }

    public static Object contextualMemberValue(
            Object receiver, ContextualMemberNavigationBinding.Member member, boolean safe, SourceSpan sourceSpan) {
        if (receiver == null) {
            if (safe) {
                return null;
            }
            throw nullReceiverInvariant(sourceSpan);
        }
        return switch (member) {
            case MAP_ENTRY_KEY -> ((MapEntryValue) receiver).key();
            case MAP_ENTRY_VALUE -> ((MapEntryValue) receiver).value();
            case REDUCTION_ACCUMULATOR -> ((ReductionItemValue) receiver).accumulator();
            case REDUCTION_ITEM -> ((ReductionItemValue) receiver).item();
        };
    }

    public static Object executeConditional(
            List<ExecutableBranch> branches,
            ExecutableNode elseExpression,
            ExecutionScope scope) {
        for (ExecutableBranch branch : branches) {
            if (bool(branch.condition().execute(scope))) {
                return branch.consequence().execute(scope);
            }
        }
        return elseExpression.execute(scope);
    }

    public static Object executeCollectionOperation(
            CollectionOperationExecutor executor,
            CollectionOperationRuntimeBinding binding,
            Object receiver,
            boolean safe,
            MathContext mathContext,
            int maxMaterializedSize,
            ExecutableOperationArguments arguments,
            ExecutionScope scope,
            SourceSpan sourceSpan) {
        if (receiver == null) {
            if (safe) {
                return null;
            }
            throw nullReceiverInvariant(sourceSpan);
        }
        return executor.execute(binding, receiver, mathContext, maxMaterializedSize, arguments, scope, sourceSpan);
    }

    static Object executeAll(
            CollectionOperationRuntimeBinding binding,
            Object receiver,
            MathContext mathContext,
            int maxMaterializedSize,
            ExecutableOperationArguments arguments,
            ExecutionScope scope,
            SourceSpan sourceSpan) {
        return all(receiver, arguments.lambdaArguments().getFirst(), scope, binding.receiverType(), sourceSpan);
    }

    static Object executeAny(
            CollectionOperationRuntimeBinding binding,
            Object receiver,
            MathContext mathContext,
            int maxMaterializedSize,
            ExecutableOperationArguments arguments,
            ExecutionScope scope,
            SourceSpan sourceSpan) {
        return any(receiver, arguments.lambdaArguments().getFirst(), scope, binding.receiverType(), sourceSpan);
    }

    static Object executeCount(
            CollectionOperationRuntimeBinding binding,
            Object receiver,
            MathContext mathContext,
            int maxMaterializedSize,
            ExecutableOperationArguments arguments,
            ExecutionScope scope,
            SourceSpan sourceSpan) {
        return count(receiver);
    }

    static Object executeKeys(
            CollectionOperationRuntimeBinding binding,
            Object receiver,
            MathContext mathContext,
            int maxMaterializedSize,
            ExecutableOperationArguments arguments,
            ExecutionScope scope,
            SourceSpan sourceSpan) {
        return mapKeys(receiver, maxMaterializedSize, sourceSpan);
    }

    static Object executeValues(
            CollectionOperationRuntimeBinding binding,
            Object receiver,
            MathContext mathContext,
            int maxMaterializedSize,
            ExecutableOperationArguments arguments,
            ExecutionScope scope,
            SourceSpan sourceSpan) {
        return mapValues(receiver, maxMaterializedSize, sourceSpan);
    }

    static Object executeMap(
            CollectionOperationRuntimeBinding binding,
            Object receiver,
            MathContext mathContext,
            int maxMaterializedSize,
            ExecutableOperationArguments arguments,
            ExecutionScope scope,
            SourceSpan sourceSpan) {
        return map(receiver, arguments.lambdaArguments().getFirst(), scope, binding.receiverType(),
                maxMaterializedSize, sourceSpan);
    }

    static Object executeSum(
            CollectionOperationRuntimeBinding binding,
            Object receiver,
            MathContext mathContext,
            int maxMaterializedSize,
            ExecutableOperationArguments arguments,
            ExecutionScope scope,
            SourceSpan sourceSpan) {
        return sum(receiver, sourceSpan);
    }

    static Object executeAvg(
            CollectionOperationRuntimeBinding binding,
            Object receiver,
            MathContext mathContext,
            int maxMaterializedSize,
            ExecutableOperationArguments arguments,
            ExecutionScope scope,
            SourceSpan sourceSpan) {
        return avg(receiver, mathContext, sourceSpan);
    }

    static Object executeReduce(
            CollectionOperationRuntimeBinding binding,
            Object receiver,
            MathContext mathContext,
            int maxMaterializedSize,
            ExecutableOperationArguments arguments,
            ExecutionScope scope,
            SourceSpan sourceSpan) {
        return reduce(receiver, arguments.valueArguments().getFirst().execute(scope),
                arguments.lambdaArguments().getFirst(), scope, sourceSpan);
    }

    static Object executeSortBy(
            CollectionOperationRuntimeBinding binding,
            Object receiver,
            MathContext mathContext,
            int maxMaterializedSize,
            ExecutableOperationArguments arguments,
            ExecutionScope scope,
            SourceSpan sourceSpan) {
        return sortBy(receiver, (String) arguments.valueArguments().getFirst().execute(scope),
                arguments.lambdaArguments().getFirst(), scope, maxMaterializedSize,
                binding.sortKeyType(), sourceSpan);
    }

    static boolean all(
            Object receiver,
            ExecutableLambda lambda,
            ExecutionScope scope,
            ExpressionType receiverType,
            SourceSpan sourceSpan) {
        if (receiverType instanceof CollectionType) {
            for (Object value : (List<?>) receiver) {
                if (!bool(lambda.execute(scope, requiredElement(value, sourceSpan)))) {
                    return false;
                }
            }
            return true;
        }
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) receiver).entrySet()) {
            if (!bool(lambda.execute(scope, mapEntryValue(entry, sourceSpan)))) {
                return false;
            }
        }
        return true;
    }

    static boolean any(
            Object receiver,
            ExecutableLambda lambda,
            ExecutionScope scope,
            ExpressionType receiverType,
            SourceSpan sourceSpan) {
        if (receiverType instanceof CollectionType) {
            for (Object value : (List<?>) receiver) {
                if (bool(lambda.execute(scope, requiredElement(value, sourceSpan)))) {
                    return true;
                }
            }
            return false;
        }
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) receiver).entrySet()) {
            if (bool(lambda.execute(scope, mapEntryValue(entry, sourceSpan)))) {
                return true;
            }
        }
        return false;
    }

    static List<Object> map(
            Object receiver,
            ExecutableLambda lambda,
            ExecutionScope scope,
            ExpressionType receiverType,
            int maxMaterializedSize,
            SourceSpan sourceSpan) {
        int size = receiverType instanceof CollectionType
                ? ((List<?>) receiver).size()
                : ((Map<?, ?>) receiver).size();
        requireMaterializedSize(size, maxMaterializedSize, sourceSpan);
        ArrayList<Object> result = new ArrayList<>(size);
        if (receiverType instanceof CollectionType) {
            for (Object value : (List<?>) receiver) {
                result.add(requiredLambdaResult(
                        lambda.execute(scope, requiredElement(value, sourceSpan)), "map", sourceSpan));
            }
        } else {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) receiver).entrySet()) {
                result.add(requiredLambdaResult(
                        lambda.execute(scope, mapEntryValue(entry, sourceSpan)), "map", sourceSpan));
            }
        }
        return List.copyOf(result);
    }

    static Object reduce(
            Object receiver,
            Object initialValue,
            ExecutableLambda lambda,
            ExecutionScope scope,
            SourceSpan sourceSpan) {
        if (initialValue == null) {
            throw RuntimeFailures.forbiddenNull("reduce initial value must not be null", sourceSpan);
        }
        Object accumulator = initialValue;
        List<?> values = (List<?>) receiver;
        for (int index = 0; index < values.size(); index++) {
            Object item = requiredElement(values.get(index), sourceSpan);
            accumulator = requiredLambdaResult(
                    lambda.execute(scope, new ReductionItemValue(accumulator, item)), "reduce", sourceSpan);
        }
        return accumulator;
    }

    static List<Object> sortBy(
            Object receiver,
            String direction,
            ExecutableLambda lambda,
            ExecutionScope scope,
            int maxMaterializedSize,
            ExpressionType keyType,
            SourceSpan sourceSpan) {
        // The semantic layer rejects a literal direction outside asc/desc; only a computed value can
        // still be invalid here, which ADR 0018 routes to an invalid operation argument at runtime.
        if (direction == null) {
            throw RuntimeFailures.forbiddenNull("sortBy direction must not be null", sourceSpan);
        }
        int directionMultiplier = switch (direction) {
            case "asc" -> 1;
            case "desc" -> -1;
            default -> throw RuntimeFailures.invalidOperationArgument(
                    "sortBy direction must be \"asc\" or \"desc\": " + direction, sourceSpan);
        };
        List<?> values = (List<?>) receiver;
        requireMaterializedSize(values.size(), maxMaterializedSize, sourceSpan);
        ArrayList<SortItem> keyedValues = new ArrayList<>(values.size());
        for (int index = 0; index < values.size(); index++) {
            Object item = requiredElement(values.get(index), sourceSpan);
            Object key = requiredLambdaResult(lambda.execute(scope, item), "sortBy selector", sourceSpan);
            keyedValues.add(new SortItem(item, key));
        }
        keyedValues.sort((left, right) -> directionMultiplier == 1
                ? compareValues(left.key(), right.key(), keyType)
                : compareValues(right.key(), left.key(), keyType));
        ArrayList<Object> result = new ArrayList<>(keyedValues.size());
        for (int index = 0; index < keyedValues.size(); index++) {
            SortItem keyedValue = keyedValues.get(index);
            result.add(keyedValue.value());
        }
        return List.copyOf(result);
    }

    private static MapEntryValue mapEntryValue(Map.Entry<?, ?> entry, SourceSpan sourceSpan) {
        String key = (String) requiredMapKey(entry.getKey(), sourceSpan);
        return new MapEntryValue(key, requiredMapValue(entry.getValue(), sourceSpan));
    }

    private static BigDecimal count(Object receiver) {
        if (receiver instanceof List<?> values) {
            return BigDecimal.valueOf(values.size());
        }
        return BigDecimal.valueOf(((Map<?, ?>) receiver).size());
    }

    private static List<Object> mapKeys(Object receiver, int maxMaterializedSize, SourceSpan sourceSpan) {
        Map<?, ?> values = (Map<?, ?>) receiver;
        requireMaterializedSize(values.size(), maxMaterializedSize, sourceSpan);
        ArrayList<Object> result = new ArrayList<>(values.size());
        for (Object key : values.keySet()) {
            result.add(requiredMapKey(key, sourceSpan));
        }
        return List.copyOf(result);
    }

    private static List<Object> mapValues(Object receiver, int maxMaterializedSize, SourceSpan sourceSpan) {
        Map<?, ?> values = (Map<?, ?>) receiver;
        requireMaterializedSize(values.size(), maxMaterializedSize, sourceSpan);
        ArrayList<Object> result = new ArrayList<>(values.size());
        for (Object value : values.values()) {
            result.add(requiredMapValue(value, sourceSpan));
        }
        return List.copyOf(result);
    }

    private static BigDecimal sum(Object receiver, SourceSpan sourceSpan) {
        BigDecimal result = BigDecimal.ZERO;
        for (Object value : (List<?>) receiver) {
            result = result.add(number(requiredElement(value, sourceSpan)));
        }
        return result;
    }

    private static BigDecimal avg(Object receiver, MathContext mathContext, SourceSpan sourceSpan) {
        List<?> values = (List<?>) receiver;
        if (CollectionOperationWiring.isAverageOfEmptyCollectionUndefined(values.size())) {
            throw RuntimeFailures.undefinedOperation("average over an empty collection is not defined", sourceSpan);
        }
        return sum(values, sourceSpan).divide(BigDecimal.valueOf(values.size()), mathContext);
    }

    private static void requireMaterializedSize(int size, int maxMaterializedSize, SourceSpan sourceSpan) {
        if (size > maxMaterializedSize) {
            throw RuntimeFailures.materializationLimitExceeded(size, maxMaterializedSize, sourceSpan);
        }
    }

    private static Object requiredElement(Object value, SourceSpan sourceSpan) {
        if (value == null) {
            throw RuntimeFailures.forbiddenNull("collection element must not be null", sourceSpan);
        }
        return value;
    }

    private static Object requiredMapKey(Object key, SourceSpan sourceSpan) {
        if (key == null) {
            throw RuntimeFailures.forbiddenNull("map key must not be null", sourceSpan);
        }
        return key;
    }

    private static Object requiredMapValue(Object value, SourceSpan sourceSpan) {
        if (value == null) {
            throw RuntimeFailures.forbiddenNull("map value must not be null", sourceSpan);
        }
        return value;
    }

    private static Object requiredMemberValue(Object value, String memberName, SourceSpan sourceSpan) {
        if (value == null) {
            throw RuntimeFailures.forbiddenNull("member value must not be null: " + memberName, sourceSpan);
        }
        return value;
    }

    private static Object requiredLambdaResult(Object value, String operationName, SourceSpan sourceSpan) {
        if (value == null) {
            throw RuntimeFailures.forbiddenNull(operationName + " result must not be null", sourceSpan);
        }
        return value;
    }

    /**
     * A null receiver on a strict link is unreachable: the semantic resolver rejects a nullable receiver
     * before the plan is built. ADR 0018 keeps this an internal invariant guard on purpose, so it must
     * not be turned into a public diagnostic code.
     */
    private static IllegalStateException nullReceiverInvariant(SourceSpan sourceSpan) {
        return new IllegalStateException("internal invariant: null navigation receiver on a strict link at " + sourceSpan);
    }

    public static boolean structuralEquals(Object left, Object right, ExpressionType type) {
        if (type == ScalarType.NUMBER) {
            return ((BigDecimal) left).compareTo((BigDecimal) right) == 0;
        }
        if (type instanceof CollectionType collectionType) {
            List<?> leftValues = (List<?>) left;
            List<?> rightValues = (List<?>) right;
            if (leftValues.size() != rightValues.size()) {
                return false;
            }
            for (int index = 0; index < leftValues.size(); index++) {
                if (!structuralEquals(leftValues.get(index), rightValues.get(index), collectionType.elementType())) {
                    return false;
                }
            }
            return true;
        }
        if (type instanceof MapType mapType) {
            Map<?, ?> leftValues = (Map<?, ?>) left;
            Map<?, ?> rightValues = (Map<?, ?>) right;
            if (!leftValues.keySet().equals(rightValues.keySet())) {
                return false;
            }
            for (Object key : leftValues.keySet()) {
                if (!structuralEquals(leftValues.get(key), rightValues.get(key), mapType.valueType())) {
                    return false;
                }
            }
            return true;
        }
        return left.equals(right);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static int compareValues(Object left, Object right, ExpressionType type) {
        if (type == ScalarType.NUMBER) {
            return ((BigDecimal) left).compareTo((BigDecimal) right);
        }
        return ((Comparable) left).compareTo(right);
    }

    public static BigDecimal number(Object value) {
        return (BigDecimal) value;
    }

    public static boolean bool(Object value) {
        return (Boolean) value;
    }

    private record SortItem(Object value, Object key) {

        private SortItem {
            Objects.requireNonNull(value, "value");
            Objects.requireNonNull(key, "key");
        }
    }

    private record MapEntryValue(String key, Object value) {

        private MapEntryValue {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(value, "value");
        }
    }

    private record ReductionItemValue(Object accumulator, Object item) {

        private ReductionItemValue {
            Objects.requireNonNull(accumulator, "accumulator");
            Objects.requireNonNull(item, "item");
        }
    }
}
