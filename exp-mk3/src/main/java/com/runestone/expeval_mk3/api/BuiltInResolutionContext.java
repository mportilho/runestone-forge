package com.runestone.expeval_mk3.api;

import java.util.Objects;

/**
 * The environment-derived inputs every built-in function group needs to resolve its reflected
 * provider against the real environment instead of a detached, hardcoded configuration.
 */
record BuiltInResolutionContext(JavaTypeCatalog javaTypes, BoundaryCoercion boundaryCoercion, int maxMaterializedSize) {

    BuiltInResolutionContext {
        Objects.requireNonNull(javaTypes, "javaTypes");
        Objects.requireNonNull(boundaryCoercion, "boundaryCoercion");
    }
}
