package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.ExpressionType;

import java.util.List;
import java.util.Objects;

public record NavigationBindingDetail(String name, List<ExpressionType> argumentTypes) {

    private static final NavigationBindingDetail EMPTY = new NavigationBindingDetail("", List.of());

    public NavigationBindingDetail {
        name = Objects.requireNonNull(name, "name");
        Objects.requireNonNull(argumentTypes, "argumentTypes");
        argumentTypes = List.copyOf(argumentTypes);
    }

    public static NavigationBindingDetail empty() {
        return EMPTY;
    }

    public static NavigationBindingDetail named(String name) {
        return new NavigationBindingDetail(name, List.of());
    }

    public static NavigationBindingDetail withArguments(String name, List<ExpressionType> argumentTypes) {
        return new NavigationBindingDetail(name, argumentTypes);
    }
}
