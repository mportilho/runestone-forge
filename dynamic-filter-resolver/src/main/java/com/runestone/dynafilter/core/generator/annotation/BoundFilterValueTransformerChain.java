package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.exceptions.StatementGenerationException;
import com.runestone.dynafilter.core.operation.FilterOperation;
import com.runestone.dynafilter.core.transformer.FilterValueContext;
import com.runestone.dynafilter.core.transformer.FilterValueTransformer;
import com.runestone.dynafilter.core.transformer.FilterValueTransformerResolver;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

final class BoundFilterValueTransformerChain {

    private static final BoundFilterValueTransformerChain EMPTY = new BoundFilterValueTransformerChain(
            new Class[0], new FilterValueTransformer[0], new FilterValueContext[0]);

    private final Class<? extends FilterValueTransformer>[] transformerTypes;
    private final FilterValueTransformer[] transformers;
    private final FilterValueContext[] contexts;

    private BoundFilterValueTransformerChain(Class<? extends FilterValueTransformer>[] transformerTypes,
                                             FilterValueTransformer[] transformers, FilterValueContext[] contexts) {
        this.transformerTypes = transformerTypes;
        this.transformers = transformers;
        this.contexts = contexts;
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
        return new BoundFilterValueTransformerChain(transformerTypes.clone(), transformers, contexts);
    }

    void transformScalars(Object[] values) {
        if (transformers.length == 0) {
            return;
        }
        for (int parameterIndex = 0; parameterIndex < values.length; parameterIndex++) {
            Object value = values[parameterIndex];
            if (value != null && !(value instanceof Object[]) && !(value instanceof Collection<?>)) {
                values[parameterIndex] = transformScalar(value, parameterIndex);
            }
        }
    }

    Object transformScalar(Object value, int parameterIndex) {
        if (value == null || transformers.length == 0) {
            return value;
        }
        FilterValueContext context = contexts[parameterIndex];
        if (transformers.length == 1) {
            return apply(0, value, context);
        }
        Object current = value;
        for (int i = 0; i < transformers.length; i++) {
            try {
                current = transformers[i].transform(current, context);
            } catch (RuntimeException exception) {
                throw failure(i, context, "threw an exception", exception);
            }
            if (current == null) {
                throw failure(i, context, "returned null", null);
            }
        }
        return current;
    }

    private Object apply(int transformerIndex, Object value, FilterValueContext context) {
        Object result;
        try {
            result = transformers[transformerIndex].transform(value, context);
        } catch (RuntimeException exception) {
            throw failure(transformerIndex, context, "threw an exception", exception);
        }
        if (result == null) {
            throw failure(transformerIndex, context, "returned null", null);
        }
        return result;
    }

    private StatementGenerationException failure(int transformerIndex, FilterValueContext context,
                                                  String reason, RuntimeException cause) {
        String message = "Filter value transformer '%s' %s for parameter '%s' at index %d on paths %s"
                .formatted(transformerTypes[transformerIndex].getCanonicalName(), reason, context.parameter(),
                        context.parameterIndex(), context.paths());
        return cause == null ? new StatementGenerationException(message) : new StatementGenerationException(message, cause);
    }
}
