package com.runestone.expeval2.catalog;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class TypeHintCatalog {

    public static final TypeHintCatalog EMPTY = new TypeHintCatalog(Map.of());

    private final Map<Class<?>, TypeMetadata> metadata;

    public TypeHintCatalog(Map<Class<?>, TypeMetadata> metadata) {
        this.metadata = Map.copyOf(Objects.requireNonNull(metadata, "metadata must not be null"));
    }

    public Optional<TypeMetadata> find(Class<?> type) {
        Objects.requireNonNull(type, "type must not be null");
        return Optional.ofNullable(metadata.get(type));
    }

    public boolean isRegistered(Class<?> type) {
        Objects.requireNonNull(type, "type must not be null");
        return metadata.containsKey(type);
    }
}
