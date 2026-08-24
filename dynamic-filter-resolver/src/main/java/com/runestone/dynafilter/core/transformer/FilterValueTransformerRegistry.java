package com.runestone.dynafilter.core.transformer;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Portable bootstrap registry for transformer instances. Registration is intended for
 * single-threaded bootstrap; the resolver snapshot is immutable and safe to publish.
 */
public final class FilterValueTransformerRegistry {

    private final Map<Class<? extends FilterValueTransformer>, FilterValueTransformer> transformers = new LinkedHashMap<>();

    public <T extends FilterValueTransformer> void register(Class<T> transformerType, T transformer) {
        Objects.requireNonNull(transformerType, "transformerType cannot be null");
        Objects.requireNonNull(transformer, "transformer cannot be null");
        if (!transformerType.isInstance(transformer)) {
            throw new IllegalArgumentException("transformer must be an instance of " + transformerType.getCanonicalName());
        }
        FilterValueTransformer previous = transformers.putIfAbsent(transformerType, transformer);
        if (previous != null) {
            throw new DynamicFilterConfigurationException(
                    "Filter value transformer '%s' is already registered".formatted(transformerType.getCanonicalName())
            );
        }
    }

    /**
     * Publishes a resolver backed by an immutable snapshot of the current registrations.
     */
    public FilterValueTransformerResolver toResolver() {
        Map<Class<? extends FilterValueTransformer>, FilterValueTransformer> snapshot = Map.copyOf(transformers);
        return transformerType -> {
            FilterValueTransformer transformer = snapshot.get(transformerType);
            if (transformer == null) {
                throw new DynamicFilterConfigurationException(
                        "Filter value transformer '%s' is not registered".formatted(transformerType.getCanonicalName())
                );
            }
            return transformer;
        };
    }
}
