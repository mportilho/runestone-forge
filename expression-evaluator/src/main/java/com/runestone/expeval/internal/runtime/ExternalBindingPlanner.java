package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.execution.plan.ExternalBindingPlan;

import com.runestone.expeval.catalog.ExternalSymbolCatalog;
import com.runestone.expeval.catalog.ExternalSymbolDescriptor;
import com.runestone.expeval.internal.semantic.SemanticModel;
import com.runestone.expeval.internal.semantic.SymbolRef;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

final class ExternalBindingPlanner {

    private ExternalBindingPlanner() {
    }

    static Object[] seedDefaults(SemanticModel semanticModel,
                                 ExternalSymbolCatalog catalog,
                                 RuntimeServices runtimeServices) {
        Objects.requireNonNull(semanticModel, "semanticModel must not be null");
        Objects.requireNonNull(catalog, "catalog must not be null");
        Objects.requireNonNull(runtimeServices, "runtimeServices must not be null");

        int externalSymbolsCount = semanticModel.externalSymbolsByName().size();
        if (externalSymbolsCount == 0) {
            return new Object[0];
        }

        Object[] defaults = new Object[externalSymbolsCount];
        Arrays.fill(defaults, ExecutionScope.UNBOUND);
        semanticModel.externalSymbolsByName().forEach((name, symbolRef) -> {
            ExternalSymbolDescriptor descriptor = catalog.findOrNull(name);
            if (descriptor != null) {
                defaults[symbolRef.index()] = runtimeServices.coerceToResolvedType(
                        descriptor.defaultValue(),
                        descriptor.declaredType());
            }
        });
        return defaults;
    }

    static Map<SymbolRef, Object> seedNonOverridableConstants(SemanticModel semanticModel,
                                                              ExternalSymbolCatalog catalog,
                                                              RuntimeServices runtimeServices) {
        Objects.requireNonNull(semanticModel, "semanticModel must not be null");
        Objects.requireNonNull(catalog, "catalog must not be null");
        Objects.requireNonNull(runtimeServices, "runtimeServices must not be null");

        if (semanticModel.externalSymbolsByName().isEmpty()) {
            return Map.of();
        }

        Map<SymbolRef, Object> constants = new HashMap<>();
        semanticModel.externalSymbolsByName().forEach((name, symbolRef) -> {
            ExternalSymbolDescriptor descriptor = catalog.findOrNull(name);
            if (descriptor != null && !descriptor.overridable()) {
                constants.put(symbolRef, runtimeServices.coerceToResolvedType(
                        descriptor.defaultValue(),
                        descriptor.declaredType()));
            }
        });
        return constants;
    }

    static Map<String, ExternalBindingPlan> seedBindingPlans(SemanticModel semanticModel,
                                                             ExternalSymbolCatalog catalog) {
        Objects.requireNonNull(semanticModel, "semanticModel must not be null");
        Objects.requireNonNull(catalog, "catalog must not be null");

        if (semanticModel.externalSymbolsByName().isEmpty()) {
            return Map.of();
        }

        Map<String, ExternalBindingPlan> bindings = new HashMap<>();
        semanticModel.externalSymbolsByName().forEach((name, symbolRef) -> {
            ExternalSymbolDescriptor descriptor = catalog.findOrNull(name);
            bindings.put(name, new ExternalBindingPlan(
                    symbolRef,
                    descriptor != null ? descriptor.declaredType() : null,
                    descriptor == null || descriptor.overridable()
            ));
        });
        return bindings;
    }
}
