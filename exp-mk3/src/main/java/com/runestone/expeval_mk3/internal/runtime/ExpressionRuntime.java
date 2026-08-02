package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.FunctionDescriptor;
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
public final class ExpressionRuntime {

    private ExpressionRuntime() {
    }

    @SuppressWarnings("removal")
    public static Object invokeFunction(
            FunctionDescriptor descriptor,
            List<ExecutableNode> argumentNodes,
            ExecutionScope scope,
            SourceSpan callSpan) {
        Object[] arguments = new Object[argumentNodes.size()];
        for (int index = 0; index < argumentNodes.size(); index++) {
            Object argument = argumentNodes.get(index).execute(scope);
            if (argument == null) {
                throw RuntimeFailures.forbiddenNull(
                        "function argument must not be null: " + descriptor.languageName(), callSpan);
            }
            arguments[index] = argument;
        }
        MethodHandle handle = descriptor.implementationHandle();
        try {
            // The result filter bound into this handle already rejects a null/incompatible/invalid
            // return as a ProviderReturnViolation before invokeWithArguments returns.
            return handle.invokeWithArguments(arguments);
        } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (ProviderReturnViolation violation) {
            // The provider ran to completion but its return value fails the resolved return
            // contract (null, incompatible type, or invalid container); distinct from a
            // provider-thrown failure.
            throw RuntimeFailures.providerReturnViolation(descriptor.languageName(), callSpan, violation);
        } catch (Throwable exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            // Declared checked exceptions, ordinary runtime exceptions, and nonfatal errors thrown
            // by the provider implementation are all provider-invocation failures; only fatal JVM
            // conditions propagate unchanged.
            throw RuntimeFailures.providerFailure(descriptor.languageName(), callSpan, exception);
        }
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

    @SuppressWarnings("removal")
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
        try {
            return binding.invocationHandle().invokeWithArguments(arguments);
        } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (Throwable exception) {
            // MethodHandle.invokeWithArguments declares Throwable; this boundary preserves method failures.
            throw RuntimeFailures.memberAccessFailure(
                    binding.implementationMetadata().memberName(), sourceSpan, exception);
        }
    }

    @SuppressWarnings("removal")
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
            value = binding.accessorHandle().invoke(receiver);
        } catch (ThreadDeath | VirtualMachineError | LinkageError fatal) {
            throw fatal;
        } catch (Throwable exception) {
            // MethodHandle.invoke declares Throwable; this boundary preserves accessor failures.
            throw RuntimeFailures.memberAccessFailure(memberName, sourceSpan, exception);
        }
        return requiredMemberValue(value, memberName, sourceSpan);
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
