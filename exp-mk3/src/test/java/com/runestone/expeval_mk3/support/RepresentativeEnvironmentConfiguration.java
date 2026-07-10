package com.runestone.expeval_mk3.support;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;

import java.util.Objects;

public record RepresentativeEnvironmentConfiguration(String name, ExpressionEnvironment environment) {

    public RepresentativeEnvironmentConfiguration {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(environment, "environment");
    }
}
