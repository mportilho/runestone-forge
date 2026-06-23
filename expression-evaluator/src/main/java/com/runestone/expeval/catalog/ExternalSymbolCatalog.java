package com.runestone.expeval.catalog;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ExternalSymbolCatalog {

    private final Map<String, ExternalSymbolDescriptor> symbolsByName;

    public ExternalSymbolCatalog(Map<String, ExternalSymbolDescriptor> symbolsByName) {
        this.symbolsByName = Map.copyOf(Objects.requireNonNull(symbolsByName, "symbolsByName must not be null"));
    }

    public Optional<ExternalSymbolDescriptor> find(String name) {
        Objects.requireNonNull(name, "name must not be null");
        return Optional.ofNullable(symbolsByName.get(name));
    }

    /**
     * Returns the descriptor for {@code name}, or {@code null} if absent.
     *
     * <p>Prefer this over {@link #find(String)} in hot paths where the result is consumed
     * immediately and the {@code Optional} allocation would be wasted.
     */
    public ExternalSymbolDescriptor findOrNull(String name) {
        Objects.requireNonNull(name, "name must not be null");
        return symbolsByName.get(name);
    }

    public Map<String, ExternalSymbolDescriptor> symbolsByName() {
        return symbolsByName;
    }
}
