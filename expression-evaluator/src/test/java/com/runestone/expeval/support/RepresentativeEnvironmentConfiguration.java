package com.runestone.expeval.support;

import com.runestone.expeval.api.ExpressionEnvironment;

import java.util.Objects;

public record RepresentativeEnvironmentConfiguration(String name, ExpressionEnvironment environment) {

    public RepresentativeEnvironmentConfiguration {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(environment, "environment");
    }
}
