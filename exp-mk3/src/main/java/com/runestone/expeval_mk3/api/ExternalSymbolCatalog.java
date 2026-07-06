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

        private final Map<String, ExternalSymbolDeclaration> symbols = new LinkedHashMap<>();

        private Builder() {
        }

        private Builder(ExternalSymbolCatalog catalog) {
            for (ExternalSymbol externalSymbol : catalog.symbols.values()) {
                symbols.put(externalSymbol.name(), ExternalSymbolDeclaration.from(externalSymbol));
            }
        }

        public Builder externalSymbol(String name) {
            return addDeclaration(ExternalSymbolDeclaration.unknown(name));
        }

        public Builder externalSymbol(String name, ExpressionType type) {
            return addDeclaration(ExternalSymbolDeclaration.declared(name, type));
        }

        public Builder externalSymbolWithDefault(String name, Object defaultValue) {
            return addDeclaration(ExternalSymbolDeclaration.withDefault(name, UnknownType.INSTANCE, defaultValue));
        }

        public Builder externalSymbolWithDefault(String name, ExpressionType type, Object defaultValue) {
            return addDeclaration(ExternalSymbolDeclaration.withDefault(name, type, defaultValue));
        }

        public ExternalSymbolCatalog build() {
            return build(BoundaryCoercion.standard());
        }

        ExternalSymbolCatalog build(BoundaryCoercion boundaryCoercion) {
            Map<String, ExternalSymbol> builtSymbols = new LinkedHashMap<>();
            for (ExternalSymbolDeclaration declaration : symbols.values()) {
                ExternalSymbol externalSymbol = declaration.toExternalSymbol(boundaryCoercion);
                builtSymbols.put(externalSymbol.name(), externalSymbol);
            }
            return ExternalSymbolCatalog.from(builtSymbols);
        }

        private Builder addDeclaration(ExternalSymbolDeclaration declaration) {
            ExternalSymbolDeclaration previous = symbols.putIfAbsent(declaration.name(), declaration);
            if (previous != null) {
                throw new IllegalArgumentException("external symbol already declared: " + declaration.name());
            }
            return this;
        }

        private record ExternalSymbolDeclaration(
                String name,
                ExpressionType type,
                boolean hasDefaultValue,
                Object defaultValue) {

            private ExternalSymbolDeclaration {
                name = ExternalSymbolNames.validate(name);
                type = Objects.requireNonNull(type, "type");
            }

            private static ExternalSymbolDeclaration unknown(String name) {
                return new ExternalSymbolDeclaration(name, UnknownType.INSTANCE, false, null);
            }

            private static ExternalSymbolDeclaration declared(String name, ExpressionType type) {
                return new ExternalSymbolDeclaration(name, type, false, null);
            }

            private static ExternalSymbolDeclaration withDefault(String name, ExpressionType type, Object defaultValue) {
                return new ExternalSymbolDeclaration(name, type, true, defaultValue);
            }

            private static ExternalSymbolDeclaration from(ExternalSymbol externalSymbol) {
                return externalSymbol.defaultValue()
                        .map(defaultValue -> withDefault(externalSymbol.name(), externalSymbol.type(), defaultValue.value()))
                        .orElseGet(() -> externalSymbol.type() == UnknownType.INSTANCE
                                ? unknown(externalSymbol.name())
                                : declared(externalSymbol.name(), externalSymbol.type()));
            }

            private ExternalSymbol toExternalSymbol(BoundaryCoercion boundaryCoercion) {
                if (hasDefaultValue) {
                    return ExternalSymbol.withDefault(name, type, defaultValue, boundaryCoercion);
                }
                if (type == UnknownType.INSTANCE) {
                    return ExternalSymbol.unknown(name);
                }
                return ExternalSymbol.declared(name, type);
            }
        }
    }
}
