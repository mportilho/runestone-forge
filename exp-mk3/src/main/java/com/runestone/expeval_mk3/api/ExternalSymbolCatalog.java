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

        public Builder externalSymbol(
                String name,
                Object defaultValue,
                ExternalSymbolOverwritePolicy overwritePolicy) {
            String validatedName = ExternalSymbolNames.validate(name);
            if (defaultValue == null) {
                throw new IllegalArgumentException(
                        "external symbol '" + validatedName + "' default must not be null");
            }
            return addDeclaration(ExternalSymbolDeclaration.withInferredDefault(
                    validatedName, defaultValue, overwritePolicy));
        }

        public Builder externalSymbol(
                String name,
                ExpressionType type,
                Object defaultValue,
                ExternalSymbolOverwritePolicy overwritePolicy) {
            Objects.requireNonNull(defaultValue, "defaultValue");
            return addDeclaration(ExternalSymbolDeclaration.withDefault(name, type, defaultValue, overwritePolicy));
        }

        public ExternalSymbolCatalog build() {
            return build(BoundaryCoercion.standard(), BoundaryCoercion.DEFAULT_MAX_MATERIALIZED_SIZE);
        }

        ExternalSymbolCatalog build(BoundaryCoercion boundaryCoercion, int maxMaterializedSize) {
            Map<String, ExternalSymbol> builtSymbols = new LinkedHashMap<>();
            for (ExternalSymbolDeclaration declaration : symbols.values()) {
                ExternalSymbol externalSymbol = declaration.toExternalSymbol(boundaryCoercion, maxMaterializedSize);
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
                Object defaultValue,
                ExternalSymbolOverwritePolicy overwritePolicy,
                boolean inferred) {

            private ExternalSymbolDeclaration {
                name = ExternalSymbolNames.validate(name);
                if (!inferred) {
                    type = Objects.requireNonNull(type, "type");
                }
                Objects.requireNonNull(defaultValue, "defaultValue");
                Objects.requireNonNull(overwritePolicy, "overwritePolicy");
            }

            private static ExternalSymbolDeclaration withDefault(
                    String name,
                    ExpressionType type,
                    Object defaultValue,
                    ExternalSymbolOverwritePolicy overwritePolicy) {
                return new ExternalSymbolDeclaration(name, type, defaultValue, overwritePolicy, false);
            }

            private static ExternalSymbolDeclaration withInferredDefault(
                    String name,
                    Object defaultValue,
                    ExternalSymbolOverwritePolicy overwritePolicy) {
                return new ExternalSymbolDeclaration(name, null, defaultValue, overwritePolicy, true);
            }

            private ExternalSymbol toExternalSymbol(BoundaryCoercion boundaryCoercion, int maxMaterializedSize) {
                if (inferred) {
                    return ExternalSymbol.withInferredDefault(
                            name, defaultValue, overwritePolicy, boundaryCoercion, maxMaterializedSize);
                }
                return ExternalSymbol.withDefault(
                        name, type, defaultValue, overwritePolicy, boundaryCoercion, maxMaterializedSize);
            }
        }
    }
}
