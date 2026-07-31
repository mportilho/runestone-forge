package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.BoundaryCoercion;
import com.runestone.expeval_mk3.api.ExternalSymbol;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.internal.runtime.ExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.ExecutionScope;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class ExecutionPlan {

    private final ExecutableNode resultExpression;
    private final List<Consumer<ExecutionScope>> assignments;
    private final List<ExternalBindingPlan> externalBindings;
    private final Map<String, ExternalBindingPlan> bindingsByName;
    private final Map<String, ExternalSymbol> declaredButUnusedSymbolsByName;
    private final int frameSize;
    private final BoundaryCoercion boundaryCoercion;
    private final ZoneId zoneId;

    ExecutionPlan(
            ExecutableNode resultExpression,
            List<Consumer<ExecutionScope>> assignments,
            List<ExternalBindingPlan> externalBindings,
            List<ExternalSymbol> declaredButUnusedSymbols,
            int frameSize,
            BoundaryCoercion boundaryCoercion,
            ZoneId zoneId) {
        this.resultExpression = Objects.requireNonNull(resultExpression, "resultExpression");
        this.assignments = List.copyOf(assignments);
        this.externalBindings = List.copyOf(externalBindings);
        bindingsByName = this.externalBindings.stream()
                .collect(Collectors.toUnmodifiableMap(binding -> binding.symbol().name(), binding -> binding));
        declaredButUnusedSymbolsByName = declaredButUnusedSymbols.stream()
                .collect(Collectors.toUnmodifiableMap(ExternalSymbol::name, symbol -> symbol));
        this.frameSize = frameSize;
        this.boundaryCoercion = Objects.requireNonNull(boundaryCoercion, "boundaryCoercion");
        this.zoneId = Objects.requireNonNull(zoneId, "zoneId");
    }

    public Object compute(Map<String, ?> overrides) {
        Objects.requireNonNull(overrides, "overrides");
        ExecutionScope scope = new ExecutionScope(frameSize, zoneId);
        for (ExternalBindingPlan binding : externalBindings) {
            scope.write(binding.frameSlot(), binding.symbol().defaultValue().value());
        }
        for (Map.Entry<String, ?> override : overrides.entrySet()) {
            ExternalBindingPlan binding = bindingsByName.get(override.getKey());
            if (binding == null) {
                validateUnusedSymbolOverride(override.getKey(), override.getValue());
                continue;
            }
            requireOverridable(binding.symbol(), override.getKey());
            scope.write(
                    binding.frameSlot(),
                    binding.symbol().coerceOverride(override.getValue(), boundaryCoercion));
        }
        for (Consumer<ExecutionScope> assignment : assignments) {
            assignment.accept(scope);
        }
        return resultExpression.execute(scope);
    }

    private void validateUnusedSymbolOverride(String name, Object value) {
        ExternalSymbol symbol = declaredButUnusedSymbolsByName.get(name);
        if (symbol == null) {
            throw new IllegalArgumentException("unknown external symbol override: " + name);
        }
        requireOverridable(symbol, name);
        symbol.coerceOverride(value, boundaryCoercion); // no frame slot to write into: symbol is unused, so only the override is validated
    }

    private static void requireOverridable(ExternalSymbol symbol, String name) {
        if (symbol.overwritePolicy() != ExternalSymbolOverwritePolicy.OVERRIDABLE) {
            throw new IllegalArgumentException("external symbol '" + name + "' is not overridable");
        }
    }
}
