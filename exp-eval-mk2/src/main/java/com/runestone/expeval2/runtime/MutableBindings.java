package com.runestone.expeval2.runtime;

import com.runestone.expeval2.catalog.ExternalSymbolCatalog;
import com.runestone.expeval2.catalog.ExternalSymbolDescriptor;
import com.runestone.expeval2.semantic.SemanticModel;
import com.runestone.expeval2.semantic.SymbolRef;
import com.runestone.expeval2.types.ResolvedType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class MutableBindings {

    private final SemanticModel semanticModel;
    private final RuntimeValueFactory runtimeValueFactory;
    private final ExternalSymbolCatalog externalSymbolCatalog;
    private final Map<SymbolRef, RuntimeValue> values = new HashMap<>();

    private MutableBindings(SemanticModel semanticModel, ExternalSymbolCatalog externalSymbolCatalog, RuntimeValueFactory runtimeValueFactory) {
        this.semanticModel = Objects.requireNonNull(semanticModel, "semanticModel must not be null");
        this.externalSymbolCatalog = Objects.requireNonNull(externalSymbolCatalog, "externalSymbolCatalog must not be null");
        this.runtimeValueFactory = Objects.requireNonNull(runtimeValueFactory, "runtimeValueFactory must not be null");
    }

    public static MutableBindings from(SemanticModel semanticModel, ExternalSymbolCatalog externalSymbolCatalog, RuntimeValueFactory runtimeValueFactory) {
        MutableBindings bindings = new MutableBindings(semanticModel, externalSymbolCatalog, runtimeValueFactory);
        bindings.seedDefaults();
        return bindings;
    }

    public void setValue(String symbolName, Object rawValue) {
        rejectWhenInternal(symbolName);
        SymbolRef symbolRef = requireExternalSymbol(symbolName);
        rejectWhenNonOverridable(symbolName);
        values.put(symbolRef, runtimeValueFactory.from(rawValue, expectedType(symbolName)));
    }

    public Optional<RuntimeValue> find(SymbolRef symbolRef) {
        Objects.requireNonNull(symbolRef, "symbolRef must not be null");
        return Optional.ofNullable(values.get(symbolRef));
    }

    public Map<SymbolRef, RuntimeValue> snapshot() {
        return Map.copyOf(values);
    }

    private void seedDefaults() {
        semanticModel.externalSymbolsByName().forEach((name, symbolRef) ->
                externalSymbolCatalog.find(name)
                        .ifPresent(descriptor -> values.put(
                                symbolRef,
                                runtimeValueFactory.from(descriptor.defaultValue(), descriptor.declaredType())
                        ))
        );
    }

    private SymbolRef requireExternalSymbol(String symbolName) {
        Objects.requireNonNull(symbolName, "symbolName must not be null");
        SymbolRef symbolRef = semanticModel.externalSymbolsByName().get(symbolName);
        if (symbolRef == null) {
            throw new IllegalArgumentException("unknown external symbol '" + symbolName + "'");
        }
        return symbolRef;
    }

    private void rejectWhenInternal(String symbolName) {
        if (semanticModel.internalSymbolsByName().containsKey(symbolName)) {
            throw new IllegalArgumentException("symbol '" + symbolName + "' is internal to the expression");
        }
    }

    private void rejectWhenNonOverridable(String symbolName) {
        externalSymbolCatalog.find(symbolName)
                .filter(descriptor -> !descriptor.overridable())
                .ifPresent(descriptor -> {
                    throw new IllegalStateException("symbol '" + symbolName + "' is not overridable");
                });
    }

    private ResolvedType expectedType(String symbolName) {
        return externalSymbolCatalog.find(symbolName)
                .map(ExternalSymbolDescriptor::declaredType)
                .orElse(null);
    }
}
