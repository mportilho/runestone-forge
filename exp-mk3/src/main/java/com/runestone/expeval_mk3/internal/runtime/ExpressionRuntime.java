package com.runestone.expeval_mk3.internal.runtime;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.FunctionDescriptor;
import com.runestone.expeval_mk3.api.JavaWildcardChildDescriptor;
import com.runestone.expeval_mk3.api.MapType;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.IndexSubscriptNavigationLink;
import com.runestone.expeval_mk3.internal.ast.PostfixOperationNode;
import com.runestone.expeval_mk3.internal.ast.PostfixOperator;
import com.runestone.expeval_mk3.internal.ast.PropertyNavigationLink;
import com.runestone.expeval_mk3.internal.ast.SliceSubscriptNavigationLink;
import com.runestone.expeval_mk3.internal.ast.SubscriptBounds;
import com.runestone.expeval_mk3.internal.semantics.CollectionOperationBinding;
import com.runestone.expeval_mk3.internal.semantics.WildcardNavigationBinding;
import com.runestone.expeval_mk3.internal.source.SourceSpan;

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

    private static final String MAP_ENTRY_KEY_MEMBER = "k";
    private static final String MAP_ENTRY_VALUE_MEMBER = "v";
    private static final String REDUCTION_ACCUMULATOR_MEMBER = "accumulator";
    private static final String REDUCTION_ITEM_MEMBER = "item";

    private ExpressionRuntime() {
    }

    public static Object invokeFunction(
            FunctionDescriptor descriptor,
            List<ExecutableNode> argumentNodes,
            ExecutionScope scope) {
        Object[] arguments = new Object[argumentNodes.size()];
        for (int index = 0; index < argumentNodes.size(); index++) {
            arguments[index] = Objects.requireNonNull(argumentNodes.get(index).execute(scope), "function argument");
        }
        MethodHandle handle = descriptor.implementationHandle();
        try {
            return Objects.requireNonNull(handle.invokeWithArguments(arguments), "function result");
        } catch (RuntimeException | Error exception) {
            throw exception;
        } catch (Throwable exception) {
            // MethodHandle.invokeWithArguments declares Throwable; this boundary preserves provider failures.
            throw new IllegalStateException("function invocation failed: " + descriptor, exception);
        }
    }

    public static List<Object> materialize(List<ExecutableNode> elements, ExecutionScope scope) {
        ArrayList<Object> values = new ArrayList<>(elements.size());
        for (ExecutableNode element : elements) {
            values.add(Objects.requireNonNull(element.execute(scope), "collection element"));
        }
        return List.copyOf(values);
    }

    public static Object indexedValue(Object receiver, IndexSubscriptNavigationLink index) {
        List<?> values = (List<?>) receiver;
        int resolvedIndex = SubscriptBounds.normalizedIndex(index.index().value(), values.size());
        return Objects.requireNonNull(values.get(resolvedIndex), "collection element");
    }

    public static List<Object> slicedValues(
            Object receiver,
            SliceSubscriptNavigationLink slice,
            int maxMaterializedSize) {
        List<?> values = (List<?>) receiver;
        int start = SubscriptBounds.normalizedSliceBound(slice.start(), values.size(), 0);
        int end = SubscriptBounds.normalizedSliceBound(slice.end(), values.size(), values.size());
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

    public static Object propertyValue(Object receiver, PropertyNavigationLink property) {
        if (receiver == null) {
            if (property.safe()) {
                return null;
            }
            throw new NullPointerException("navigation receiver");
        }
        if (receiver instanceof MapEntryValue entry) {
            return switch (property.memberName().value()) {
                case MAP_ENTRY_KEY_MEMBER -> entry.key();
                case MAP_ENTRY_VALUE_MEMBER -> entry.value();
                default -> throw new IllegalStateException(
                        "unsupported map entry property: " + property.memberName().value());
            };
        }
        return reductionItemProperty(receiver, property);
    }

    private static Object reductionItemProperty(Object receiver, PropertyNavigationLink property) {
        ReductionItemValue reductionItem = (ReductionItemValue) receiver;
        return switch (property.memberName().value()) {
            case REDUCTION_ACCUMULATOR_MEMBER -> reductionItem.accumulator();
            case REDUCTION_ITEM_MEMBER -> reductionItem.item();
            default -> throw new IllegalStateException(
                    "unsupported contextual item property: " + property.memberName().value());
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
            CollectionOperationBinding binding,
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
        return switch (binding.identity()) {
            case ALL -> all(receiver, arguments.lambdaArguments().getFirst(), scope, binding.receiverType());
            case ANY -> any(receiver, arguments.lambdaArguments().getFirst(), scope, binding.receiverType());
            case COUNT -> count(receiver);
            case KEYS -> mapKeys(receiver, maxMaterializedSize);
            case VALUES -> mapValues(receiver, maxMaterializedSize);
            case MAP -> map(receiver, arguments.lambdaArguments().getFirst(), scope, binding.receiverType(), maxMaterializedSize);
            case SUM -> sum(receiver);
            case AVG -> avg(receiver, mathContext);
            case REDUCE -> reduce(receiver, arguments.valueArguments().getFirst().execute(scope),
                    arguments.lambdaArguments().getFirst(), scope);
            case SORT_BY -> sortBy(receiver, (String) arguments.valueArguments().getFirst().execute(scope),
                    arguments.lambdaArguments().getFirst(), scope, maxMaterializedSize,
                    binding.lambdaBindings().getFirst().resultType());
            case CUSTOM -> throw new IllegalStateException("unsupported collection operation binding: " + binding.identity());
        };
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
        if (values.isEmpty()) {
            throw new IllegalStateException("average over an empty collection is not defined");
        }
        return sum(values).divide(BigDecimal.valueOf(values.size()), mathContext);
    }

    private static void requireMaterializedSize(int size, int maxMaterializedSize) {
        if (size > maxMaterializedSize) {
            throw new IllegalStateException("materialized collection exceeds maxMaterializedSize " + maxMaterializedSize);
        }
    }

    public static BigDecimal executePostfix(
            BigDecimal initial,
            PostfixOperationNode postfix,
            ExpressionEnvironment environment) {
        BigDecimal result = initial;
        for (var operation : postfix.operations()) {
            result = operation.operator() == PostfixOperator.PERCENT
                    ? result.movePointLeft(2)
                    : factorial(result, environment.maxFactorialInput());
        }
        return result;
    }

    private static BigDecimal factorial(BigDecimal value, int maxFactorialInput) {
        BigDecimal normalized = value.stripTrailingZeros();
        if (normalized.scale() > 0) {
            throw new ArithmeticException("factorial input must be integral: " + value);
        }
        BigInteger integerValue = normalized.toBigInteger();
        if (integerValue.signum() < 0 || integerValue.compareTo(BigInteger.valueOf(maxFactorialInput)) > 0) {
            throw new ArithmeticException("factorial input out of range: " + value);
        }
        int integer = integerValue.intValue();
        BigInteger result = BigInteger.ONE;
        for (int factor = 2; factor <= integer; factor++) {
            result = result.multiply(BigInteger.valueOf(factor));
        }
        return new BigDecimal(result);
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
