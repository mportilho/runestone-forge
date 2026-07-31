package com.runestone.expeval_mk3.internal.runtime;

import ch.obermuhlner.math.big.BigDecimalMath;
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

    public static List<Object> materialize(List<ExecutableNode> elements, ExecutionScope scope) {
        ArrayList<Object> values = new ArrayList<>(elements.size());
        for (ExecutableNode element : elements) {
            values.add(Objects.requireNonNull(element.execute(scope), "collection element"));
        }
        return List.copyOf(values);
    }

    public static Object indexedValue(Object receiver, BigInteger index) {
        List<?> values = (List<?>) receiver;
        int resolvedIndex = SubscriptBounds.normalizedIndex(index, values.size());
        return Objects.requireNonNull(values.get(resolvedIndex), "collection element");
    }

    public static List<Object> slicedValues(
            Object receiver,
            BigInteger startBound,
            BigInteger endBound,
            int maxMaterializedSize) {
        List<?> values = (List<?>) receiver;
        int start = SubscriptBounds.normalizedSliceBound(startBound, values.size(), 0);
        int end = SubscriptBounds.normalizedSliceBound(endBound, values.size(), values.size());
        if (end < start) {
            end = start;
        }
        requireMaterializedSize(end - start, maxMaterializedSize);
        ArrayList<Object> result = new ArrayList<>(end - start);
        for (int index = start; index < end; index++) {
            result.add(Objects.requireNonNull(values.get(index), "collection element"));
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
            throw new NullPointerException("navigation receiver at " + sourceSpan);
        }
        Map<?, ?> values = (Map<?, ?>) receiver;
        Object value = values.get(key);
        if (value == null) {
            if (!values.containsKey(key)) {
                throw new IllegalArgumentException("map key not found: " + key + " at " + sourceSpan);
            }
            throw new NullPointerException("map value at " + sourceSpan);
        }
        return value;
    }

    public static List<Object> filteredValues(
            Object receiver,
            ExecutableNode predicate,
            int currentItemSlot,
            ExecutionScope scope,
            int maxMaterializedSize) {
        List<?> values = (List<?>) receiver;
        ArrayList<Object> result = new ArrayList<>();
        for (Object value : values) {
            Object item = Objects.requireNonNull(value, "collection element");
            Object previous = scope.replace(currentItemSlot, item);
            try {
                if (bool(predicate.execute(scope))) {
                    if (result.size() == maxMaterializedSize) {
                        throw new IllegalStateException(
                                "materialized collection exceeds maxMaterializedSize " + maxMaterializedSize);
                    }
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
            int maxMaterializedSize) {
        if (receiver == null) {
            if (safe) {
                return null;
            }
            throw new NullPointerException("navigation receiver");
        }
        return switch (binding.receiverKind()) {
            case COLLECTION -> collectionWildcardValues(receiver, maxMaterializedSize);
            case MAP -> mapWildcardValues(receiver, maxMaterializedSize);
            case OBJECT -> objectWildcardValues(receiver, binding.objectChildren(), maxMaterializedSize);
        };
    }

    private static List<Object> collectionWildcardValues(Object receiver, int maxMaterializedSize) {
        List<?> values = (List<?>) receiver;
        requireMaterializedSize(values.size(), maxMaterializedSize);
        ArrayList<Object> result = new ArrayList<>(values.size());
        for (Object value : values) {
            result.add(Objects.requireNonNull(value, "collection element"));
        }
        return List.copyOf(result);
    }

    private static List<Object> mapWildcardValues(Object receiver, int maxMaterializedSize) {
        Map<?, ?> values = (Map<?, ?>) receiver;
        requireMaterializedSize(values.size(), maxMaterializedSize);
        ArrayList<String> keys = new ArrayList<>(values.size());
        for (Object key : values.keySet()) {
            keys.add((String) Objects.requireNonNull(key, "map key"));
        }
        Collections.sort(keys);
        ArrayList<Object> result = new ArrayList<>(keys.size());
        for (String key : keys) {
            result.add(Objects.requireNonNull(values.get(key), "map value"));
        }
        return List.copyOf(result);
    }

    private static List<Object> objectWildcardValues(
            Object receiver,
            List<JavaWildcardChildDescriptor> children,
            int maxMaterializedSize) {
        requireMaterializedSize(children.size(), maxMaterializedSize);
        ArrayList<Object> result = new ArrayList<>(children.size());
        for (JavaWildcardChildDescriptor child : children) {
            try {
                result.add(Objects.requireNonNull(child.accessorHandle().invoke(receiver), "wildcard child value"));
            } catch (RuntimeException | Error exception) {
                throw exception;
            } catch (Throwable exception) {
                // MethodHandle.invoke declares Throwable; this boundary preserves accessor failures.
                throw new IllegalStateException("wildcard child accessor failed: " + child.name(), exception);
            }
        }
        return List.copyOf(result);
    }

    public static Object invokeRegisteredMethod(
            Object receiver,
            boolean safe,
            RegisteredMethodNavigationBinding binding,
            List<ExecutableNode> argumentNodes,
            ExecutionScope scope) {
        if (receiver == null) {
            if (safe) {
                return null;
            }
            throw new NullPointerException("navigation receiver");
        }
        Object[] arguments = new Object[argumentNodes.size() + 1];
        arguments[0] = receiver;
        for (int index = 0; index < argumentNodes.size(); index++) {
            arguments[index + 1] = Objects.requireNonNull(
                    argumentNodes.get(index).execute(scope), "registered method argument");
        }
        try {
            return binding.invocationHandle().invokeWithArguments(arguments);
        } catch (RuntimeException | Error exception) {
            throw exception;
        } catch (Throwable exception) {
            // MethodHandle.invokeWithArguments declares Throwable; this boundary preserves method failures.
            throw new IllegalStateException(
                    "registered method invocation failed: " + binding.implementationMetadata().memberName(), exception);
        }
    }

    public static Object registeredPropertyValue(
            Object receiver, boolean safe, RegisteredPropertyNavigationBinding binding) {
        if (receiver == null) {
            if (safe) {
                return null;
            }
            throw new NullPointerException("navigation receiver");
        }
        try {
            return Objects.requireNonNull(binding.accessorHandle().invoke(receiver), "registered property value");
        } catch (RuntimeException | Error exception) {
            throw exception;
        } catch (Throwable exception) {
            // MethodHandle.invoke declares Throwable; this boundary preserves accessor failures.
            throw new IllegalStateException(
                    "registered property accessor failed: " + binding.implementationMetadata().memberName(), exception);
        }
    }

    public static Object contextualMemberValue(
            Object receiver, ContextualMemberNavigationBinding.Member member, boolean safe) {
        if (receiver == null) {
            if (safe) {
                return null;
            }
            throw new NullPointerException("navigation receiver");
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
            ExecutionScope scope) {
        if (receiver == null) {
            if (safe) {
                return null;
            }
            throw new NullPointerException("navigation receiver");
        }
        return executor.execute(binding, receiver, mathContext, maxMaterializedSize, arguments, scope);
    }

    static Object executeAll(
            CollectionOperationRuntimeBinding binding,
            Object receiver,
            MathContext mathContext,
            int maxMaterializedSize,
            ExecutableOperationArguments arguments,
            ExecutionScope scope) {
        return all(receiver, arguments.lambdaArguments().getFirst(), scope, binding.receiverType());
    }

    static Object executeAny(
            CollectionOperationRuntimeBinding binding,
            Object receiver,
            MathContext mathContext,
            int maxMaterializedSize,
            ExecutableOperationArguments arguments,
            ExecutionScope scope) {
        return any(receiver, arguments.lambdaArguments().getFirst(), scope, binding.receiverType());
    }

    static Object executeCount(
            CollectionOperationRuntimeBinding binding,
            Object receiver,
            MathContext mathContext,
            int maxMaterializedSize,
            ExecutableOperationArguments arguments,
            ExecutionScope scope) {
        return count(receiver);
    }

    static Object executeKeys(
            CollectionOperationRuntimeBinding binding,
            Object receiver,
            MathContext mathContext,
            int maxMaterializedSize,
            ExecutableOperationArguments arguments,
            ExecutionScope scope) {
        return mapKeys(receiver, maxMaterializedSize);
    }

    static Object executeValues(
            CollectionOperationRuntimeBinding binding,
            Object receiver,
            MathContext mathContext,
            int maxMaterializedSize,
            ExecutableOperationArguments arguments,
            ExecutionScope scope) {
        return mapValues(receiver, maxMaterializedSize);
    }

    static Object executeMap(
            CollectionOperationRuntimeBinding binding,
            Object receiver,
            MathContext mathContext,
            int maxMaterializedSize,
            ExecutableOperationArguments arguments,
            ExecutionScope scope) {
        return map(receiver, arguments.lambdaArguments().getFirst(), scope, binding.receiverType(), maxMaterializedSize);
    }

    static Object executeSum(
            CollectionOperationRuntimeBinding binding,
            Object receiver,
            MathContext mathContext,
            int maxMaterializedSize,
            ExecutableOperationArguments arguments,
            ExecutionScope scope) {
        return sum(receiver);
    }

    static Object executeAvg(
            CollectionOperationRuntimeBinding binding,
            Object receiver,
            MathContext mathContext,
            int maxMaterializedSize,
            ExecutableOperationArguments arguments,
            ExecutionScope scope) {
        return avg(receiver, mathContext);
    }

    static Object executeReduce(
            CollectionOperationRuntimeBinding binding,
            Object receiver,
            MathContext mathContext,
            int maxMaterializedSize,
            ExecutableOperationArguments arguments,
            ExecutionScope scope) {
        return reduce(receiver, arguments.valueArguments().getFirst().execute(scope),
                arguments.lambdaArguments().getFirst(), scope);
    }

    static Object executeSortBy(
            CollectionOperationRuntimeBinding binding,
            Object receiver,
            MathContext mathContext,
            int maxMaterializedSize,
            ExecutableOperationArguments arguments,
            ExecutionScope scope) {
        return sortBy(receiver, (String) arguments.valueArguments().getFirst().execute(scope),
                arguments.lambdaArguments().getFirst(), scope, maxMaterializedSize,
                binding.sortKeyType());
    }

    static boolean all(
            Object receiver,
            ExecutableLambda lambda,
            ExecutionScope scope,
            ExpressionType receiverType) {
        if (receiverType instanceof CollectionType) {
            for (Object value : (List<?>) receiver) {
                if (!bool(lambda.execute(scope, Objects.requireNonNull(value, "collection element")))) {
                    return false;
                }
            }
            return true;
        }
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) receiver).entrySet()) {
            if (!bool(lambda.execute(scope, mapEntryValue(entry)))) {
                return false;
            }
        }
        return true;
    }

    static boolean any(
            Object receiver,
            ExecutableLambda lambda,
            ExecutionScope scope,
            ExpressionType receiverType) {
        if (receiverType instanceof CollectionType) {
            for (Object value : (List<?>) receiver) {
                if (bool(lambda.execute(scope, Objects.requireNonNull(value, "collection element")))) {
                    return true;
                }
            }
            return false;
        }
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) receiver).entrySet()) {
            if (bool(lambda.execute(scope, mapEntryValue(entry)))) {
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
            int maxMaterializedSize) {
        int size = receiverType instanceof CollectionType
                ? ((List<?>) receiver).size()
                : ((Map<?, ?>) receiver).size();
        requireMaterializedSize(size, maxMaterializedSize);
        ArrayList<Object> result = new ArrayList<>(size);
        if (receiverType instanceof CollectionType) {
            for (Object value : (List<?>) receiver) {
                result.add(Objects.requireNonNull(
                        lambda.execute(scope, Objects.requireNonNull(value, "collection element")),
                        "map lambda result"));
            }
        } else {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) receiver).entrySet()) {
                result.add(Objects.requireNonNull(lambda.execute(scope, mapEntryValue(entry)), "map lambda result"));
            }
        }
        return List.copyOf(result);
    }

    static Object reduce(
            Object receiver,
            Object initialValue,
            ExecutableLambda lambda,
            ExecutionScope scope) {
        Object accumulator = Objects.requireNonNull(initialValue, "reduce initial value");
        List<?> values = (List<?>) receiver;
        for (int index = 0; index < values.size(); index++) {
            Object value = values.get(index);
            Object item = Objects.requireNonNull(value, "collection element");
            accumulator = Objects.requireNonNull(
                    lambda.execute(scope, new ReductionItemValue(accumulator, item)),
                    "reduce lambda result");
        }
        return accumulator;
    }

    static List<Object> sortBy(
            Object receiver,
            String direction,
            ExecutableLambda lambda,
            ExecutionScope scope,
            int maxMaterializedSize,
            ExpressionType keyType) {
        int directionMultiplier = switch (Objects.requireNonNull(direction, "sort direction")) {
            case "asc" -> 1;
            case "desc" -> -1;
            default -> throw new IllegalArgumentException("unsupported sort direction: " + direction);
        };
        List<?> values = (List<?>) receiver;
        requireMaterializedSize(values.size(), maxMaterializedSize);
        ArrayList<SortItem> keyedValues = new ArrayList<>(values.size());
        for (int index = 0; index < values.size(); index++) {
            Object value = values.get(index);
            Object item = Objects.requireNonNull(value, "collection element");
            Object key = Objects.requireNonNull(lambda.execute(scope, item), "sortBy selector result");
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

    private static MapEntryValue mapEntryValue(Map.Entry<?, ?> entry) {
        return new MapEntryValue(
                (String) Objects.requireNonNull(entry.getKey(), "map key"),
                Objects.requireNonNull(entry.getValue(), "map value"));
    }

    private static BigDecimal count(Object receiver) {
        if (receiver instanceof List<?> values) {
            return BigDecimal.valueOf(values.size());
        }
        return BigDecimal.valueOf(((Map<?, ?>) receiver).size());
    }

    private static List<Object> mapKeys(Object receiver, int maxMaterializedSize) {
        Map<?, ?> values = (Map<?, ?>) receiver;
        requireMaterializedSize(values.size(), maxMaterializedSize);
        ArrayList<Object> result = new ArrayList<>(values.size());
        for (Object key : values.keySet()) {
            result.add(Objects.requireNonNull(key, "map key"));
        }
        return List.copyOf(result);
    }

    private static List<Object> mapValues(Object receiver, int maxMaterializedSize) {
        Map<?, ?> values = (Map<?, ?>) receiver;
        requireMaterializedSize(values.size(), maxMaterializedSize);
        ArrayList<Object> result = new ArrayList<>(values.size());
        for (Object value : values.values()) {
            result.add(Objects.requireNonNull(value, "map value"));
        }
        return List.copyOf(result);
    }

    private static BigDecimal sum(Object receiver) {
        BigDecimal result = BigDecimal.ZERO;
        for (Object value : (List<?>) receiver) {
            result = result.add(number(Objects.requireNonNull(value, "collection element")));
        }
        return result;
    }

    private static BigDecimal avg(Object receiver, MathContext mathContext) {
        List<?> values = (List<?>) receiver;
        if (CollectionOperationWiring.isAverageOfEmptyCollectionUndefined(values.size())) {
            throw new IllegalStateException("average over an empty collection is not defined");
        }
        return sum(values).divide(BigDecimal.valueOf(values.size()), mathContext);
    }

    private static void requireMaterializedSize(int size, int maxMaterializedSize) {
        if (size > maxMaterializedSize) {
            throw new IllegalStateException("materialized collection exceeds maxMaterializedSize " + maxMaterializedSize);
        }
    }

    public static BigDecimal pow(BigDecimal base, BigDecimal exponent, MathContext mathContext) {
        BigDecimal normalizedExponent = exponent.stripTrailingZeros();
        if (normalizedExponent.scale() <= 0) {
            BigInteger integerValue = normalizedExponent.toBigInteger();
            if (integerValue.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0
                    || integerValue.compareTo(BigInteger.valueOf((long) Integer.MIN_VALUE + 1)) < 0) {
                return BigDecimalMath.pow(base, exponent, mathContext);
            }
            int integerExponent = integerValue.intValue();
            if (integerExponent >= 0) {
                return base.pow(integerExponent, mathContext);
            }
            return BigDecimal.ONE.divide(base.pow(-integerExponent, mathContext), mathContext);
        }
        return BigDecimalMath.pow(base, exponent, mathContext);
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
