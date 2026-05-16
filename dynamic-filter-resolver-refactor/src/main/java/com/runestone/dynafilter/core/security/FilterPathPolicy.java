package com.runestone.dynafilter.core.security;

import com.runestone.dynafilter.core.exception.DynamicFilterConfigurationException;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public record FilterPathPolicy(Set<String> allowedPaths, Set<String> deniedPaths) {

    public static final FilterPathPolicy PERMISSIVE = new FilterPathPolicy(Set.of(), Set.of());

    public FilterPathPolicy {
        allowedPaths = allowedPaths == null ? Set.of() : Set.copyOf(allowedPaths);
        deniedPaths = deniedPaths == null ? Set.of() : Set.copyOf(deniedPaths);
    }

    public static FilterPathPolicy allowOnly(Collection<String> allowedPaths) {
        return new FilterPathPolicy(Set.copyOf(Objects.requireNonNull(allowedPaths, "allowedPaths must not be null")), Set.of());
    }

    public static FilterPathPolicy deny(Collection<String> deniedPaths) {
        return new FilterPathPolicy(Set.of(), Set.copyOf(Objects.requireNonNull(deniedPaths, "deniedPaths must not be null")));
    }

    public void validate(String path) {
        Objects.requireNonNull(path, "path must not be null");
        if (deniedPaths.contains(path)) {
            throw new DynamicFilterConfigurationException("Filter path is denied: " + path);
        }
        if (!allowedPaths.isEmpty() && !allowedPaths.contains(path)) {
            throw new DynamicFilterConfigurationException("Filter path is not allowed: " + path);
        }
    }
}
