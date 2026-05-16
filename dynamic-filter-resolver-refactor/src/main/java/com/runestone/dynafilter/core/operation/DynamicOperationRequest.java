package com.runestone.dynafilter.core.operation;

import com.runestone.dynafilter.core.modifier.FilterModifier;

import java.util.List;
import java.util.Objects;

public record DynamicOperationRequest(
        String path,
        String parameter,
        Class<?> targetType,
        Object value,
        List<Class<? extends FilterModifier>> modifiers,
        String description
) {

    public DynamicOperationRequest {
        path = Objects.requireNonNull(path, "path must not be null");
        parameter = Objects.requireNonNull(parameter, "parameter must not be null");
        targetType = Objects.requireNonNull(targetType, "targetType must not be null");
        value = Objects.requireNonNull(value, "value must not be null");
        modifiers = modifiers == null ? List.of() : List.copyOf(modifiers);
    }
}
