package com.runestone.dynafilter.core.transformer;

import com.runestone.dynafilter.core.operation.FilterOperation;

import java.util.List;
import java.util.Objects;

/**
 * Immutable metadata for one declared filter parameter.
 *
 * @param parameter          declared parameter name
 * @param parameterIndex     parameter position in the filter declaration
 * @param paths              all declared filter paths
 * @param operation          effective filter operation
 * @param declaredTargetType target type declared by the filter
 */
public record FilterValueContext(
        String parameter,
        int parameterIndex,
        List<String> paths,
        @SuppressWarnings("rawtypes") Class<? extends FilterOperation> operation,
        Class<?> declaredTargetType
) {

    public FilterValueContext {
        Objects.requireNonNull(parameter, "parameter cannot be null");
        if (parameterIndex < 0) {
            throw new IllegalArgumentException("parameterIndex cannot be negative");
        }
        paths = List.copyOf(Objects.requireNonNull(paths, "paths cannot be null"));
        Objects.requireNonNull(operation, "operation cannot be null");
        Objects.requireNonNull(declaredTargetType, "declaredTargetType cannot be null");
    }
}
