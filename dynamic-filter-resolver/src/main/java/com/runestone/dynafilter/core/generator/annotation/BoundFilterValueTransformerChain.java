package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.exceptions.StatementGenerationException;
import com.runestone.dynafilter.core.operation.ComparisonOperation;
import com.runestone.dynafilter.core.operation.FilterOperation;
import com.runestone.dynafilter.core.operation.types.Dynamic;
import com.runestone.dynafilter.core.transformer.FilterValueContext;
import com.runestone.dynafilter.core.transformer.FilterValueTransformer;
import com.runestone.dynafilter.core.transformer.FilterValueTransformerResolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

final class BoundFilterValueTransformerChain {

    private static final BoundFilterValueTransformerChain EMPTY = new BoundFilterValueTransformerChain(
            new Class[0], new FilterValueTransformer[0], new FilterValueContext[0], null);

    private final Class<? extends FilterValueTransformer>[] transformerTypes;
    private final FilterValueTransformer[] transformers;
    private final FilterValueContext[] contexts;
    private final FilterValueContext[] dynamicContexts;

    private BoundFilterValueTransformerChain(Class<? extends FilterValueTransformer>[] transformerTypes,
                                             FilterValueTransformer[] transformers, FilterValueContext[] contexts,
                                             FilterValueContext[] dynamicContexts) {
        this.transformerTypes = transformerTypes;
        this.transformers = transformers;
        this.contexts = contexts;
        this.dynamicContexts = dynamicContexts;
    }

    static BoundFilterValueTransformerChain bind(
            Class<? extends FilterValueTransformer>[] transformerTypes,
            FilterValueTransformerResolver resolver,
            String[] parameters,
            String[] paths,
            @SuppressWarnings("rawtypes") Class<? extends FilterOperation> operation,
            Class<?> declaredTargetType) {
        if (transformerTypes.length == 0) {
            return EMPTY;
        }

        FilterValueTransformer[] transformers = new FilterValueTransformer[transformerTypes.length];
        for (int i = 0; i < transformerTypes.length; i++) {
            FilterValueTransformer transformer = resolver.resolve(transformerTypes[i]);
            if (transformer == null) {
                throw new DynamicFilterConfigurationException(
                        "Resolver returned null for filter value transformer '%s'".formatted(transformerTypes[i].getCanonicalName())
                );
            }
            transformers[i] = transformer;
        }

        List<String> immutablePaths = List.copyOf(Arrays.asList(paths));
        FilterValueContext[] contexts = new FilterValueContext[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            contexts[i] = new FilterValueContext(parameters[i], i, immutablePaths, operation, declaredTargetType);
        }
        FilterValueContext[] dynamicContexts = null;
        if (Dynamic.class.equals(operation)) {
            ComparisonOperation[] operations = ComparisonOperation.values();
            dynamicContexts = new FilterValueContext[operations.length];
            for (ComparisonOperation comparisonOperation : operations) {
                dynamicContexts[comparisonOperation.ordinal()] = new FilterValueContext(parameters[0], 0,
                        immutablePaths, comparisonOperation.getOperation(), declaredTargetType);
            }
        }
        return new BoundFilterValueTransformerChain(transformerTypes.clone(), transformers, contexts, dynamicContexts);
    }

    boolean isEmpty() {
        return transformers.length == 0;
    }

    void transformValues(Object[] values) {
        if (transformers.length == 0) {
            return;
        }
        for (int parameterIndex = 0; parameterIndex < values.length; parameterIndex++) {
            values[parameterIndex] = transformValue(values[parameterIndex], contexts[parameterIndex]);
        }
    }

    void transformScalars(Object[] values) {
        if (transformers.length == 0) {
            return;
        }
        for (int parameterIndex = 0; parameterIndex < values.length; parameterIndex++) {
            Object value = values[parameterIndex];
            if (value != null && !(value instanceof Object[]) && !(value instanceof Collection<?>)) {
                values[parameterIndex] = transformScalar(value, contexts[parameterIndex], -1);
            }
        }
    }

    Object[] transformDynamicPayload(Object[] dynamicValues, int payloadOffset,
                                     ComparisonOperation operation) {
        int size = dynamicValues.length - payloadOffset;
        FilterValueContext context = dynamicContexts[operation.ordinal()];
        return transformArray(dynamicValues, payloadOffset, size, context);
    }

    Object transformDynamicValue(Object value, ComparisonOperation operation) {
        return transformValue(value, dynamicContexts[operation.ordinal()]);
    }

    Object transformValue(Object value, int parameterIndex) {
        return transformValue(value, contexts[parameterIndex]);
    }

    Object transformScalar(Object value, int parameterIndex) {
        if (value == null || transformers.length == 0) {
            return value;
        }
        return transformScalar(value, contexts[parameterIndex], -1);
    }

    private Object transformValue(Object value, FilterValueContext context) {
        if (value == null || transformers.length == 0) {
            return value;
        }
        if (value instanceof Object[] array) {
            return transformArray(array, context);
        }
        if (value instanceof Collection<?> collection) {
            return transformCollection(collection, context);
        }
        return transformScalar(value, context, -1);
    }

    private Object[] transformArray(Object[] values, FilterValueContext context) {
        return transformArray(values, 0, values.length, context);
    }

    private Object[] transformArray(Object[] values, int offset, int size, FilterValueContext context) {
        Object[] transformed = new Object[size];
        if (transformers.length == 1) {
            for (int i = 0; i < size; i++) {
                Object value = values[offset + i];
                transformed[i] = value == null ? null : apply(0, value, context, i);
            }
            return transformed;
        }
        for (int i = 0; i < size; i++) {
            Object current = values[offset + i];
            if (current == null) {
                continue;
            }
            for (int transformerIndex = 0; transformerIndex < transformers.length; transformerIndex++) {
                try {
                    current = transformers[transformerIndex].transform(current, context);
                } catch (RuntimeException exception) {
                    throw failure(transformerIndex, context, i, "threw an exception", exception);
                }
                if (current == null) {
                    throw failure(transformerIndex, context, i, "returned null", null);
                }
            }
            transformed[i] = current;
        }
        return transformed;
    }

    private List<Object> transformCollection(Collection<?> values, FilterValueContext context) {
        List<Object> transformed = new ArrayList<>(values.size());
        int i = 0;
        for (Object value : values) {
            transformed.add(transformScalar(value, context, i++));
        }
        return transformed;
    }

    private Object transformScalar(Object value, FilterValueContext context, int elementIndex) {
        if (value == null || transformers.length == 0) {
            return value;
        }
        if (transformers.length == 1) {
            return apply(0, value, context, elementIndex);
        }
        Object current = value;
        for (int i = 0; i < transformers.length; i++) {
            try {
                current = transformers[i].transform(current, context);
            } catch (RuntimeException exception) {
                throw failure(i, context, elementIndex, "threw an exception", exception);
            }
            if (current == null) {
                throw failure(i, context, elementIndex, "returned null", null);
            }
        }
        return current;
    }

    private Object apply(int transformerIndex, Object value, FilterValueContext context, int elementIndex) {
        Object result;
        try {
            result = transformers[transformerIndex].transform(value, context);
        } catch (RuntimeException exception) {
            throw failure(transformerIndex, context, elementIndex, "threw an exception", exception);
        }
        if (result == null) {
            throw failure(transformerIndex, context, elementIndex, "returned null", null);
        }
        return result;
    }

    private StatementGenerationException failure(int transformerIndex, FilterValueContext context, int elementIndex,
                                                  String reason, RuntimeException cause) {
        String position = elementIndex >= 0 ? " at multivalue position " + elementIndex : "";
        String message = "Filter value transformer '%s' %s for parameter '%s' at index %d%s on paths %s"
                .formatted(transformerTypes[transformerIndex].getCanonicalName(), reason, context.parameter(),
                        context.parameterIndex(), position, context.paths());
        return cause == null ? new StatementGenerationException(message) : new StatementGenerationException(message, cause);
    }
}
