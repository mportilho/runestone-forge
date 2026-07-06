package com.runestone.expeval_mk3.api;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Immutable catalog of Simbolo Externo declarations keyed by symbol name.
 */
public final class ExternalSymbolCatalog {

    private static final ExternalSymbolCatalog EMPTY = new ExternalSymbolCatalog(Map.of());

    private final Map<String, ExternalSymbol> symbols;

    private ExternalSymbolCatalog(Map<String, ExternalSymbol> symbols) {
        this.symbols = symbols;
    }

    static ExternalSymbolCatalog from(Map<String, ExternalSymbol> source) {
        if (source.isEmpty()) {
            return EMPTY;
        }
        TreeMap<String, ExternalSymbol> sorted = new TreeMap<>(source);
        return new ExternalSymbolCatalog(Collections.unmodifiableMap(new LinkedHashMap<>(sorted)));
    }

    public static ExternalSymbolCatalog empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public Map<String, ExternalSymbol> asMap() {
        return symbols;
    }

    public Collection<ExternalSymbol> values() {
        return symbols.values();
    }

    public Optional<ExternalSymbol> find(String name) {
        return Optional.ofNullable(symbols.get(ExternalSymbolNames.validate(name)));
    }

    public boolean contains(String name) {
        return symbols.containsKey(ExternalSymbolNames.validate(name));
    }

    public int size() {
        return symbols.size();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ExternalSymbolCatalog that)) {
            return false;
        }
        return symbols.equals(that.symbols);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbols);
    }

    @Override
    public String toString() {
        return "ExternalSymbolCatalog[size=" + symbols.size() + ']';
    }

    public static final class Builder {

        private final Map<String, ExternalSymbol> symbols = new LinkedHashMap<>();

        private Builder() {
        }

        private Builder(ExternalSymbolCatalog catalog) {
            symbols.putAll(catalog.symbols);
        }

        public Builder externalSymbol(String name) {
            return addExternalSymbol(ExternalSymbol.unknown(name));
        }

        public Builder externalSymbol(String name, ExpressionType type) {
            return addExternalSymbol(ExternalSymbol.declared(name, type));
        }

        public Builder externalSymbolWithDefault(String name, Object defaultValue) {
            String validatedName = ExternalSymbolNames.validate(name);
            ExpressionType type = ExternalSymbolDefaults.inferType(validatedName, defaultValue);
            return addExternalSymbol(ExternalSymbol.withDefault(validatedName, type, defaultValue));
        }

        public Builder externalSymbolWithDefault(String name, ExpressionType type, Object defaultValue) {
            return addExternalSymbol(ExternalSymbol.withDefault(name, type, defaultValue));
        }

        public ExternalSymbolCatalog build() {
            return ExternalSymbolCatalog.from(symbols);
        }

        private Builder addExternalSymbol(ExternalSymbol externalSymbol) {
            ExternalSymbol previous = symbols.putIfAbsent(externalSymbol.name(), externalSymbol);
            if (previous != null) {
                throw new IllegalArgumentException("external symbol already declared: " + externalSymbol.name());
            }
            return this;
        }
    }
}
