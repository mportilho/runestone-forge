package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.BoundaryCoercion;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.internal.runtime.ExecutionScope;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ExecutionPlan {

    private final ExecutableNode resultExpression;
    private final List<ExternalBindingPlan> externalBindings;
    private final Map<String, ExternalBindingPlan> bindingsByName;
    private final int frameSize;
    private final BoundaryCoercion boundaryCoercion;

    ExecutionPlan(
            ExecutableNode resultExpression,
            List<ExternalBindingPlan> externalBindings,
            int frameSize,
            BoundaryCoercion boundaryCoercion) {
        this.resultExpression = Objects.requireNonNull(resultExpression, "resultExpression");
        this.externalBindings = List.copyOf(externalBindings);
        bindingsByName = this.externalBindings.stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(binding -> binding.symbol().name(), binding -> binding));
        this.frameSize = frameSize;
        this.boundaryCoercion = Objects.requireNonNull(boundaryCoercion, "boundaryCoercion");
    }

    public Object compute(Map<String, ?> overrides) {
        Objects.requireNonNull(overrides, "overrides");
        ExecutionScope scope = new ExecutionScope(frameSize);
        for (ExternalBindingPlan binding : externalBindings) {
            scope.write(binding.frameSlot(), binding.symbol().defaultValue().value());
        }
        for (Map.Entry<String, ?> override : overrides.entrySet()) {
            ExternalBindingPlan binding = bindingsByName.get(override.getKey());
            if (binding == null) {
                throw new IllegalArgumentException("unknown external symbol override: " + override.getKey());
            }
            if (binding.symbol().overwritePolicy() != ExternalSymbolOverwritePolicy.OVERRIDABLE) {
                throw new IllegalArgumentException("external symbol '" + override.getKey() + "' is not overridable");
            }
            scope.write(
                    binding.frameSlot(),
                    binding.symbol().coerceOverride(override.getValue(), boundaryCoercion));
        }
        return Objects.requireNonNull(resultExpression.execute(scope), "expression result");
    }
}
