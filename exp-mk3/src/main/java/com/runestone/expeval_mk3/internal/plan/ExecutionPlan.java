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
import java.util.stream.Collectors;

/**
 * The immutable, thread-shareable, unoptimized execution plan produced by {@link ExecutionPlanBuilder}
 * from a successful {@code SemanticModel}. It does not retain a parse tree, the {@code SemanticModel},
 * source text, or the whole {@code ExpressionEnvironment}.
 */
public final class ExecutionPlan {

    private final ExecutableNode resultExpression;
    private final List<AssignmentExecutable> assignments;
    private final List<ExternalBindingPlan> externalBindings;
    private final Map<String, ExternalBindingPlan> bindingsByName;
    private final Map<String, ExternalSymbol> declaredButUnusedSymbolsByName;
    private final int frameSize;
    private final BoundaryCoercion boundaryCoercion;
    private final ZoneId zoneId;

    ExecutionPlan(
            ExecutableNode resultExpression,
            List<AssignmentExecutable> assignments,
            List<ExternalBindingPlan> externalBindings,
            List<ExternalSymbol> declaredButUnusedSymbols,
            int frameSize,
            BoundaryCoercion boundaryCoercion,
            ZoneId zoneId) {
        this.resultExpression = resultExpression;
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

    public boolean hasResult() {
        return resultExpression != null;
    }

    List<AssignmentExecutable> assignments() {
        return assignments;
    }

    ExecutableNode resultExpression() {
        return resultExpression;
    }

    /**
     * Runs assignments in source order and, when present, the final result expression. Assignment-only
     * plans have no result to invent, so this returns {@code null} for them; a public view over such a
     * plan decides for itself whether that absence is reachable.
     */
    public Object compute(Map<String, ?> overrides) {
        ExecutionScope scope = prepare(overrides);
        return resultExpression == null ? null : resultExpression.execute(scope);
    }

    private ExecutionScope prepare(Map<String, ?> overrides) {
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
        for (AssignmentExecutable assignment : assignments) {
            assignment.execute(scope);
        }
        return scope;
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
