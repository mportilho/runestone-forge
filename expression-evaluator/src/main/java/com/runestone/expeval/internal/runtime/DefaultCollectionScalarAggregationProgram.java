package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.ExpressionEvaluationException;
import com.runestone.expeval.internal.navigation.MapProjectionKind;
import com.runestone.expeval.internal.navigation.VectorAggregationKind;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class DefaultCollectionScalarAggregationProgram implements CollectionScalarAggregationProgram {

    private final List<ExecutablePropertyChain.ExecutableAccess> chain;
    private final int startIndex;
    private final int aggregationIndex;
    private final ExecutablePropertyChain.ExecutableVectorAggregation aggregation;

    DefaultCollectionScalarAggregationProgram(
            List<ExecutablePropertyChain.ExecutableAccess> chain,
            int startIndex,
            int aggregationIndex,
            ExecutablePropertyChain.ExecutableVectorAggregation aggregation) {
        this.chain = List.copyOf(chain);
        this.startIndex = startIndex;
        this.aggregationIndex = aggregationIndex;
        this.aggregation = aggregation;
    }

    @Override
    public int startIndex() {
        return startIndex;
    }

    @Override
    public Object compute(Object current, ExecutionScope scope, ScalarAggregationRuntime runtime) {
        ScalarAggregationScratch scratch = ScalarAggregationScratch.acquire();
        Object currentValue = current;
        ScalarAggregationScratch.Buffer currentBuffer = null;
        try {
            for (int index = startIndex; index < aggregationIndex; index++) {
                ExecutablePropertyChain.ExecutableAccess access = chain.get(index);
                if (currentBuffer == null && currentValue == null) {
                    if (PropertyChainOps.isSafeAccess(access)) {
                        return null;
                    }
                    throw nullInChain(runtime);
                }
                StepResult result = applyStep(access, currentValue, currentBuffer, scratch, scope, runtime);
                currentValue = result.value;
                currentBuffer = result.buffer;
            }
            if (currentBuffer == null && currentValue == null) {
                throw nullInChain(runtime);
            }
            return aggregate(currentValue, currentBuffer, scratch, scope, runtime);
        } finally {
            scratch.release();
        }
    }

    private StepResult applyStep(
            ExecutablePropertyChain.ExecutableAccess access,
            @Nullable Object currentValue,
            ScalarAggregationScratch.Buffer currentBuffer,
            ScalarAggregationScratch scratch,
            ExecutionScope scope,
            ScalarAggregationRuntime runtime) {
        return switch (access) {
            case ExecutablePropertyChain.ExecutableIndexAccess indexAccess ->
                    applyIndex(indexAccess, currentValue, currentBuffer, scope, runtime);
            case ExecutablePropertyChain.ExecutableSliceAccess sliceAccess ->
                    applySlice(sliceAccess, currentValue, currentBuffer, scratch, scope, runtime);
            case ExecutablePropertyChain.ExecutableWildcard ignored ->
                    applyWildcard(currentValue, currentBuffer, scratch);
            case ExecutablePropertyChain.ExecutableFilterPredicate filterPredicate ->
                    applyFilter(filterPredicate, currentValue, currentBuffer, scratch, scope, runtime);
            case ExecutablePropertyChain.ExecutableMapProjection mapProjection ->
                    applyMapProjection(mapProjection, currentValue, currentBuffer, scratch, runtime);
            case ExecutablePropertyChain.ExecutableVectorMap vectorMap ->
                    applyMapTransform(vectorMap, currentValue, currentBuffer, scratch, scope, runtime);
            case ExecutablePropertyChain.ExecutableFieldGet fieldGet ->
                    applyFieldGet(fieldGet, currentValue, currentBuffer, scratch, runtime);
            case ExecutablePropertyChain.ReflectivePropertyAccess propertyAccess ->
                    applyReflectiveProperty(propertyAccess, currentValue, currentBuffer, scratch, runtime);
            default -> throw new IllegalStateException("scalar aggregation program contains unsupported access: " + access);
        };
    }

    private StepResult applyIndex(
            ExecutablePropertyChain.ExecutableIndexAccess indexAccess,
            @Nullable Object currentValue,
            ScalarAggregationScratch.Buffer currentBuffer,
            ExecutionScope scope,
            ScalarAggregationRuntime runtime) {
        int index = (int) PropertyChainOps.asBigDecimalStrict(
                runtime.evaluator().evaluate(indexAccess.index(), scope),
                runtime.runtimeServices()).longValue();
        if (currentBuffer != null) {
            if (currentBuffer.map) {
                throw typeMismatch("index", "Map", runtime.source());
            }
            int effective = effectiveIndex(index, currentBuffer.size, runtime.source());
            return StepResult.value(currentBuffer.values[effective]);
        }
        return StepResult.value(CollectionNavigationOps.applyIndex(currentValue, index, runtime.source()));
    }

    private StepResult applySlice(
            ExecutablePropertyChain.ExecutableSliceAccess sliceAccess,
            @Nullable Object currentValue,
            ScalarAggregationScratch.Buffer currentBuffer,
            ScalarAggregationScratch scratch,
            ExecutionScope scope,
            ScalarAggregationRuntime runtime) {
        Integer start = sliceAccess.start() == null ? null
                : (int) PropertyChainOps.asBigDecimalStrict(
                        runtime.evaluator().evaluate(sliceAccess.start(), scope),
                        runtime.runtimeServices()).longValue();
        Integer end = sliceAccess.end() == null ? null
                : (int) PropertyChainOps.asBigDecimalStrict(
                        runtime.evaluator().evaluate(sliceAccess.end(), scope),
                        runtime.runtimeServices()).longValue();
        ScalarAggregationScratch.Buffer target = scratch.nextBuffer(currentBuffer);
        if (currentBuffer != null) {
            if (currentBuffer.map) {
                throw typeMismatch("slice", "Map", runtime.source());
            }
            sliceBuffer(currentBuffer, start, end, target);
            return StepResult.buffer(target);
        }
        List<?> list = CollectionNavigationOps.requireList(currentValue, "slice", runtime.source());
        sliceList(list, start, end, target);
        return StepResult.buffer(target);
    }

    private StepResult applyWildcard(
            @Nullable Object currentValue,
            ScalarAggregationScratch.Buffer currentBuffer,
            ScalarAggregationScratch scratch) {
        if (currentBuffer != null) {
            if (!currentBuffer.map) {
                return StepResult.buffer(currentBuffer);
            }
            ScalarAggregationScratch.Buffer target = scratch.nextBuffer(currentBuffer);
            target.resetList(currentBuffer.size);
            for (int index = 0; index < currentBuffer.size; index++) {
                target.addValue(currentBuffer.values[index]);
            }
            return StepResult.buffer(target);
        }
        if (currentValue instanceof List<?>) {
            return StepResult.value(currentValue);
        }
        ScalarAggregationScratch.Buffer target = scratch.nextBuffer(null);
        if (currentValue instanceof Map<?, ?> map) {
            target.resetList(map.size());
            for (Object value : map.values()) {
                target.addValue(value);
            }
        } else {
            target.resetList(1);
            target.addValue(currentValue);
        }
        return StepResult.buffer(target);
    }

    private StepResult applyFilter(
            ExecutablePropertyChain.ExecutableFilterPredicate filterPredicate,
            @Nullable Object currentValue,
            ScalarAggregationScratch.Buffer currentBuffer,
            ScalarAggregationScratch scratch,
            ExecutionScope scope,
            ScalarAggregationRuntime runtime) {
        ScalarAggregationScratch.Buffer target = scratch.nextBuffer(currentBuffer);
        FilterContextStack stack = FilterContextStack.INSTANCE.get();
        if (currentBuffer != null) {
            if (currentBuffer.map) {
                target.resetMap(currentBuffer.size);
                for (int index = 0; index < currentBuffer.size; index++) {
                    filterMapEntry(target, currentBuffer.keys[index], currentBuffer.values[index], filterPredicate, stack, scope, runtime);
                }
            } else {
                target.resetList(currentBuffer.size);
                for (int index = 0; index < currentBuffer.size; index++) {
                    filterElement(target, currentBuffer.values[index], filterPredicate, stack, scope, runtime);
                }
            }
            return StepResult.buffer(target);
        }
        if (currentValue instanceof Map<?, ?> map) {
            target.resetMap(map.size());
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                filterMapEntry(target, entry.getKey(), entry.getValue(), filterPredicate, stack, scope, runtime);
            }
            return StepResult.buffer(target);
        }
        List<?> list = CollectionNavigationOps.requireList(currentValue, "filter", runtime.source());
        target.resetList(list.size());
        for (Object element : list) {
            filterElement(target, element, filterPredicate, stack, scope, runtime);
        }
        return StepResult.buffer(target);
    }

    private void filterMapEntry(
            ScalarAggregationScratch.Buffer target,
            Object key,
            Object value,
            ExecutablePropertyChain.ExecutableFilterPredicate filterPredicate,
            FilterContextStack stack,
            ExecutionScope scope,
            ScalarAggregationRuntime runtime) {
        stack.pushMapEntry(key, value);
        try {
            if (asBoolean(runtime.evaluator().evaluate(filterPredicate.predicate(), scope), runtime)) {
                target.addEntry(key, value);
            }
        } finally {
            stack.pop();
        }
    }

    private void filterElement(
            ScalarAggregationScratch.Buffer target,
            Object element,
            ExecutablePropertyChain.ExecutableFilterPredicate filterPredicate,
            FilterContextStack stack,
            ExecutionScope scope,
            ScalarAggregationRuntime runtime) {
        stack.pushElement(element);
        try {
            if (asBoolean(runtime.evaluator().evaluate(filterPredicate.predicate(), scope), runtime)) {
                target.addValue(element);
            }
        } finally {
            stack.pop();
        }
    }

    private StepResult applyMapProjection(
            ExecutablePropertyChain.ExecutableMapProjection mapProjection,
            @Nullable Object currentValue,
            ScalarAggregationScratch.Buffer currentBuffer,
            ScalarAggregationScratch scratch,
            ScalarAggregationRuntime runtime) {
        MapProjectionKind kind = mapProjection.kind();
        if (currentBuffer != null) {
            if (currentBuffer.map) {
                ScalarAggregationScratch.Buffer target = scratch.nextBuffer(currentBuffer);
                target.resetList(currentBuffer.size);
                Object[] source = kind == MapProjectionKind.KEYS ? currentBuffer.keys : currentBuffer.values;
                for (int index = 0; index < currentBuffer.size; index++) {
                    target.addValue(source[index]);
                }
                return StepResult.buffer(target);
            }
            if (kind == MapProjectionKind.VALUES) {
                return StepResult.buffer(currentBuffer);
            }
            throw mapProjectionTypeMismatch("List", runtime.source());
        }
        if (currentValue instanceof Map<?, ?> map) {
            ScalarAggregationScratch.Buffer target = scratch.nextBuffer(null);
            target.resetList(map.size());
            if (kind == MapProjectionKind.KEYS) {
                for (Object key : map.keySet()) {
                    target.addValue(key);
                }
            } else {
                for (Object value : map.values()) {
                    target.addValue(value);
                }
            }
            return StepResult.buffer(target);
        }
        if (kind == MapProjectionKind.VALUES && currentValue instanceof List<?>) {
            return StepResult.value(currentValue);
        }
        throw mapProjectionTypeMismatch(currentValue == null ? "null" : currentValue.getClass().getSimpleName(), runtime.source());
    }

    private StepResult applyMapTransform(
            ExecutablePropertyChain.ExecutableVectorMap vectorMap,
            @Nullable Object currentValue,
            ScalarAggregationScratch.Buffer currentBuffer,
            ScalarAggregationScratch scratch,
            ExecutionScope scope,
            ScalarAggregationRuntime runtime) {
        ScalarAggregationScratch.Buffer target = scratch.nextBuffer(currentBuffer);
        FilterContextStack stack = FilterContextStack.INSTANCE.get();
        if (currentBuffer != null) {
            target.resetList(currentBuffer.size);
            if (currentBuffer.map) {
                for (int index = 0; index < currentBuffer.size; index++) {
                    mapEntryTransform(target, currentBuffer.keys[index], currentBuffer.values[index], vectorMap, stack, scope, runtime);
                }
            } else {
                for (int index = 0; index < currentBuffer.size; index++) {
                    elementTransform(target, currentBuffer.values[index], vectorMap, stack, scope, runtime);
                }
            }
            return StepResult.buffer(target);
        }
        if (currentValue instanceof Map<?, ?> map) {
            target.resetList(map.size());
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                mapEntryTransform(target, entry.getKey(), entry.getValue(), vectorMap, stack, scope, runtime);
            }
            return StepResult.buffer(target);
        }
        List<?> list = CollectionNavigationOps.requireList(currentValue, "map", runtime.source());
        target.resetList(list.size());
        for (Object element : list) {
            elementTransform(target, element, vectorMap, stack, scope, runtime);
        }
        return StepResult.buffer(target);
    }

    private void mapEntryTransform(
            ScalarAggregationScratch.Buffer target,
            Object key,
            Object value,
            ExecutablePropertyChain.ExecutableVectorMap vectorMap,
            FilterContextStack stack,
            ExecutionScope scope,
            ScalarAggregationRuntime runtime) {
        stack.pushMapEntry(key, value);
        try {
            target.addValue(runtime.evaluator().evaluate(vectorMap.transform(), scope));
        } finally {
            stack.pop();
        }
    }

    private void elementTransform(
            ScalarAggregationScratch.Buffer target,
            Object element,
            ExecutablePropertyChain.ExecutableVectorMap vectorMap,
            FilterContextStack stack,
            ExecutionScope scope,
            ScalarAggregationRuntime runtime) {
        stack.pushElement(element);
        try {
            target.addValue(runtime.evaluator().evaluate(vectorMap.transform(), scope));
        } finally {
            stack.pop();
        }
    }

    private StepResult applyFieldGet(
            ExecutablePropertyChain.ExecutableFieldGet fieldGet,
            @Nullable Object currentValue,
            ScalarAggregationScratch.Buffer currentBuffer,
            ScalarAggregationScratch scratch,
            ScalarAggregationRuntime runtime) {
        if (currentBuffer != null) {
            if (currentBuffer.map) {
                return StepResult.value(PropertyChainOps.invokeGetter(
                        runtime.rootName(), currentBuffer.toLinkedHashMap(), fieldGet, runtime.source(), runtime.runtimeServices()));
            }
            ScalarAggregationScratch.Buffer target = scratch.nextBuffer(currentBuffer);
            target.resetList(currentBuffer.size);
            for (int index = 0; index < currentBuffer.size; index++) {
                Object element = currentBuffer.values[index];
                if (element != null) {
                    target.addValue(PropertyChainOps.invokeGetter(
                            runtime.rootName(), element, fieldGet, runtime.source(), runtime.runtimeServices()));
                }
            }
            return StepResult.buffer(target);
        }
        if (currentValue instanceof List<?> list) {
            ScalarAggregationScratch.Buffer target = scratch.nextBuffer(null);
            target.resetList(list.size());
            for (Object element : list) {
                if (element != null) {
                    target.addValue(PropertyChainOps.invokeGetter(
                            runtime.rootName(), element, fieldGet, runtime.source(), runtime.runtimeServices()));
                }
            }
            return StepResult.buffer(target);
        }
        return StepResult.value(PropertyChainOps.invokeGetter(
                runtime.rootName(), currentValue, fieldGet, runtime.source(), runtime.runtimeServices()));
    }

    private StepResult applyReflectiveProperty(
            ExecutablePropertyChain.ReflectivePropertyAccess propertyAccess,
            @Nullable Object currentValue,
            ScalarAggregationScratch.Buffer currentBuffer,
            ScalarAggregationScratch scratch,
            ScalarAggregationRuntime runtime) {
        if (currentBuffer != null) {
            if (currentBuffer.map) {
                return StepResult.value(PropertyChainOps.resolvePropertyReflective(
                        runtime.source(), currentBuffer.toLinkedHashMap(), propertyAccess.name()));
            }
            ScalarAggregationScratch.Buffer target = scratch.nextBuffer(currentBuffer);
            target.resetList(currentBuffer.size);
            for (int index = 0; index < currentBuffer.size; index++) {
                Object element = currentBuffer.values[index];
                if (element != null) {
                    target.addValue(PropertyChainOps.resolvePropertyReflective(runtime.source(), element, propertyAccess.name()));
                }
            }
            return StepResult.buffer(target);
        }
        if (currentValue instanceof List<?> list) {
            ScalarAggregationScratch.Buffer target = scratch.nextBuffer(null);
            target.resetList(list.size());
            for (Object element : list) {
                if (element != null) {
                    target.addValue(PropertyChainOps.resolvePropertyReflective(runtime.source(), element, propertyAccess.name()));
                }
            }
            return StepResult.buffer(target);
        }
        return StepResult.value(PropertyChainOps.resolvePropertyReflective(runtime.source(), currentValue, propertyAccess.name()));
    }

    private Object aggregate(
            @Nullable Object currentValue,
            ScalarAggregationScratch.Buffer currentBuffer,
            ScalarAggregationScratch scratch,
            ExecutionScope scope,
            ScalarAggregationRuntime runtime) {
        VectorAggregationKind kind = aggregation.kind();
        if (currentBuffer != null) {
            if (currentBuffer.map) {
                if (kind == VectorAggregationKind.COUNT) {
                    return BigDecimal.valueOf(currentBuffer.size);
                }
                throw typeMismatch("aggregation", "Map", runtime.source());
            }
            return aggregateValues(currentBuffer.values, currentBuffer.size, kind, aggregation.transform(), scratch, scope, runtime);
        }
        if (currentValue instanceof Map<?, ?> map && kind == VectorAggregationKind.COUNT) {
            return BigDecimal.valueOf(map.size());
        }
        List<?> list = CollectionNavigationOps.requireList(currentValue, "aggregation", runtime.source());
        if (kind == VectorAggregationKind.COUNT) {
            return BigDecimal.valueOf(list.size());
        }
        return aggregateList(list, kind, aggregation.transform(), scratch, scope, runtime);
    }

    private Object aggregateList(
            List<?> list,
            VectorAggregationKind kind,
            @Nullable ExecutableNode transform,
            ScalarAggregationScratch scratch,
            ExecutionScope scope,
            ScalarAggregationRuntime runtime) {
        int size = list.size();
        if (size == 0) {
            return emptyAggregationValue(kind);
        }
        BigDecimal[] numbers = scratch.numbers(size);
        if (transform == null) {
            for (int index = 0; index < size; index++) {
                numbers[index] = asBigDecimal(list.get(index), runtime);
            }
        } else {
            FilterContextStack stack = FilterContextStack.INSTANCE.get();
            for (int index = 0; index < size; index++) {
                stack.pushElement(list.get(index));
                try {
                    numbers[index] = asBigDecimal(runtime.evaluator().evaluate(transform, scope), runtime);
                } finally {
                    stack.pop();
                }
            }
        }
        return aggregateNumbers(numbers, size, kind, runtime);
    }

    private Object aggregateValues(
            Object[] values,
            int size,
            VectorAggregationKind kind,
            @Nullable ExecutableNode transform,
            ScalarAggregationScratch scratch,
            ExecutionScope scope,
            ScalarAggregationRuntime runtime) {
        if (kind == VectorAggregationKind.COUNT) {
            return BigDecimal.valueOf(size);
        }
        if (size == 0) {
            return emptyAggregationValue(kind);
        }
        BigDecimal[] numbers = scratch.numbers(size);
        if (transform == null) {
            for (int index = 0; index < size; index++) {
                numbers[index] = asBigDecimal(values[index], runtime);
            }
        } else {
            FilterContextStack stack = FilterContextStack.INSTANCE.get();
            for (int index = 0; index < size; index++) {
                stack.pushElement(values[index]);
                try {
                    numbers[index] = asBigDecimal(runtime.evaluator().evaluate(transform, scope), runtime);
                } finally {
                    stack.pop();
                }
            }
        }
        return aggregateNumbers(numbers, size, kind, runtime);
    }

    private Object aggregateNumbers(BigDecimal[] numbers, int size, VectorAggregationKind kind, ScalarAggregationRuntime runtime) {
        BigDecimal acc = numbers[0];
        return switch (kind) {
            case SUM -> {
                for (int index = 1; index < size; index++) {
                    acc = acc.add(numbers[index], runtime.mathContext());
                }
                yield acc;
            }
            case AVG -> {
                for (int index = 1; index < size; index++) {
                    acc = acc.add(numbers[index], runtime.mathContext());
                }
                yield acc.divide(BigDecimal.valueOf(size), runtime.mathContext());
            }
            case MIN -> {
                for (int index = 1; index < size; index++) {
                    BigDecimal value = numbers[index];
                    if (value.compareTo(acc) < 0) {
                        acc = value;
                    }
                }
                yield acc;
            }
            case MAX -> {
                for (int index = 1; index < size; index++) {
                    BigDecimal value = numbers[index];
                    if (value.compareTo(acc) > 0) {
                        acc = value;
                    }
                }
                yield acc;
            }
            case PROD -> {
                for (int index = 1; index < size; index++) {
                    acc = acc.multiply(numbers[index], runtime.mathContext());
                }
                yield acc;
            }
            case COUNT -> BigDecimal.valueOf(size);
        };
    }

    private static @Nullable Object emptyAggregationValue(VectorAggregationKind kind) {
        return switch (kind) {
            case SUM -> BigDecimal.ZERO;
            case PROD -> BigDecimal.ONE;
            case COUNT -> BigDecimal.ZERO;
            default -> null;
        };
    }

    private static void sliceList(List<?> list, @Nullable Integer start, @Nullable Integer end,
            ScalarAggregationScratch.Buffer target) {
        int size = list.size();
        int from = start == null ? 0 : (start < 0 ? Math.max(0, size + start) : Math.min(start, size));
        int to = end == null ? size : (end < 0 ? Math.max(0, size + end) : Math.min(end, size));
        target.resetList(Math.max(0, to - from));
        for (int index = from; index < to; index++) {
            target.addValue(list.get(index));
        }
    }

    private static void sliceBuffer(ScalarAggregationScratch.Buffer buffer, @Nullable Integer start, @Nullable Integer end,
            ScalarAggregationScratch.Buffer target) {
        int size = buffer.size;
        int from = start == null ? 0 : (start < 0 ? Math.max(0, size + start) : Math.min(start, size));
        int to = end == null ? size : (end < 0 ? Math.max(0, size + end) : Math.min(end, size));
        target.resetList(Math.max(0, to - from));
        for (int index = from; index < to; index++) {
            target.addValue(buffer.values[index]);
        }
    }

    private static int effectiveIndex(int index, int size, String source) {
        int effective = index < 0 ? size + index : index;
        if (effective < 0 || effective >= size) {
            throw new ExpressionEvaluationException(source, "INDEX_OUT_OF_BOUNDS",
                    "index " + index + " is out of bounds for collection of size " + size, null);
        }
        return effective;
    }

    private static BigDecimal asBigDecimal(Object value, ScalarAggregationRuntime runtime) {
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        try {
            return runtime.runtimeServices().asNumber(value);
        } catch (IllegalStateException exception) {
            throw new ExpressionEvaluationException(runtime.source(), "NULL_VALUE",
                    "cannot use null value as a number", null);
        }
    }

    private static boolean asBoolean(Object value, ScalarAggregationRuntime runtime) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        try {
            return runtime.runtimeServices().asBoolean(value);
        } catch (IllegalStateException exception) {
            throw new ExpressionEvaluationException(runtime.source(), "NULL_VALUE",
                    "cannot use null value as a boolean", null);
        }
    }

    private static ExpressionEvaluationException nullInChain(ScalarAggregationRuntime runtime) {
        return new ExpressionEvaluationException(runtime.source(), "NULL_IN_CHAIN",
                "null value encountered navigating '" + runtime.rootName() + "'", null);
    }

    private static ExpressionEvaluationException typeMismatch(String operation, String typeName, String source) {
        return new ExpressionEvaluationException(source, "TYPE_MISMATCH",
                operation + " requires a List but got: " + typeName, null);
    }

    private static ExpressionEvaluationException mapProjectionTypeMismatch(String typeName, String source) {
        return new ExpressionEvaluationException(source, "MAP_PROJECTION_TYPE_MISMATCH",
                "map projection requires a Map but got: " + typeName, null);
    }

    private record StepResult(@Nullable Object value, ScalarAggregationScratch.Buffer buffer) {

        static StepResult value(@Nullable Object value) {
            return new StepResult(value, null);
        }

        static StepResult buffer(ScalarAggregationScratch.Buffer buffer) {
            return new StepResult(null, buffer);
        }
    }

    private static final class ScalarAggregationScratch {

        private static final int INITIAL_CAPACITY = 16;
        private static final int MAX_RETAINED_CAPACITY = 4_096;
        private static final ThreadLocal<ScalarAggregationScratch> LOCAL =
                ThreadLocal.withInitial(() -> new ScalarAggregationScratch(true));

        private final boolean retained;
        private final Buffer first = new Buffer();
        private final Buffer second = new Buffer();
        private boolean inUse;
        private BigDecimal[] numbers = new BigDecimal[INITIAL_CAPACITY];
        private int numberSize;

        private ScalarAggregationScratch(boolean retained) {
            this.retained = retained;
        }

        static ScalarAggregationScratch acquire() {
            ScalarAggregationScratch scratch = LOCAL.get();
            if (scratch.inUse) {
                return new ScalarAggregationScratch(false);
            }
            scratch.inUse = true;
            return scratch;
        }

        Buffer nextBuffer(@Nullable Buffer current) {
            return current == first ? second : first;
        }

        BigDecimal[] numbers(int size) {
            clearNumbers();
            if (numbers.length < size) {
                int capacity = numbers.length;
                while (capacity < size) {
                    capacity *= 2;
                }
                numbers = new BigDecimal[capacity];
            }
            numberSize = size;
            return numbers;
        }

        void release() {
            first.release();
            second.release();
            clearNumbers();
            if (numbers.length > MAX_RETAINED_CAPACITY) {
                numbers = new BigDecimal[INITIAL_CAPACITY];
            }
            if (retained) {
                inUse = false;
            }
        }

        private void clearNumbers() {
            if (numberSize > 0) {
                Arrays.fill(numbers, 0, numberSize, null);
                numberSize = 0;
            }
        }

        private static final class Buffer {

            private Object[] keys = new Object[INITIAL_CAPACITY];
            private Object[] values = new Object[INITIAL_CAPACITY];
            private int size;
            private boolean map;

            void resetList(int expectedSize) {
                releaseUsedReferences();
                map = false;
                ensureCapacity(expectedSize);
            }

            void resetMap(int expectedSize) {
                releaseUsedReferences();
                map = true;
                ensureCapacity(expectedSize);
            }

            void addValue(Object value) {
                ensureCapacity(size + 1);
                values[size++] = value;
            }

            void addEntry(Object key, Object value) {
                ensureCapacity(size + 1);
                keys[size] = key;
                values[size] = value;
                size++;
            }

            Map<Object, Object> toLinkedHashMap() {
                Map<Object, Object> result = new LinkedHashMap<>(size * 2);
                for (int index = 0; index < size; index++) {
                    result.put(keys[index], values[index]);
                }
                return result;
            }

            void release() {
                releaseUsedReferences();
                map = false;
                if (values.length > MAX_RETAINED_CAPACITY) {
                    values = new Object[INITIAL_CAPACITY];
                    keys = new Object[INITIAL_CAPACITY];
                }
            }

            private void ensureCapacity(int expectedSize) {
                if (values.length >= expectedSize) {
                    return;
                }
                int capacity = values.length;
                while (capacity < expectedSize) {
                    capacity *= 2;
                }
                values = Arrays.copyOf(values, capacity);
                keys = Arrays.copyOf(keys, capacity);
            }

            private void releaseUsedReferences() {
                if (size == 0) {
                    return;
                }
                Arrays.fill(values, 0, size, null);
                if (map) {
                    Arrays.fill(keys, 0, size, null);
                }
                size = 0;
            }
        }
    }
}
