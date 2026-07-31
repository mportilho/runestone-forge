package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.BoundaryCoercion;
import com.runestone.expeval_mk3.api.ExternalSymbol;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.internal.diagnostics.RuntimeFailures;
import com.runestone.expeval_mk3.internal.runtime.ExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.ExecutionScope;

import java.time.Clock;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    private final List<ExternalSymbol> declaredSymbolsInCanonicalOrder;
    private final Set<String> declaredSymbolNames;
    private final Object[] frameTemplate;
    private final BoundaryCoercion boundaryCoercion;
    private final ZoneId zoneId;

    ExecutionPlan(
            ExecutableNode resultExpression,
            List<AssignmentExecutable> assignments,
            List<ExternalBindingPlan> externalBindings,
            List<ExternalSymbol> declaredSymbolsInCanonicalOrder,
            int frameSize,
            BoundaryCoercion boundaryCoercion,
            ZoneId zoneId) {
        this.resultExpression = resultExpression;
        this.assignments = List.copyOf(assignments);
        this.externalBindings = List.copyOf(externalBindings);
        bindingsByName = this.externalBindings.stream()
                .collect(Collectors.toUnmodifiableMap(binding -> binding.symbol().name(), binding -> binding));
        this.declaredSymbolsInCanonicalOrder = List.copyOf(declaredSymbolsInCanonicalOrder);
        declaredSymbolNames = this.declaredSymbolsInCanonicalOrder.stream()
                .map(ExternalSymbol::name)
                .collect(Collectors.toUnmodifiableSet());
        Object[] template = ExecutionScope.blankFrame(frameSize);
        for (ExternalBindingPlan binding : this.externalBindings) {
            template[binding.frameSlot()] = binding.symbol().defaultValue().value();
        }
        this.frameTemplate = template;
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
    public Object compute(Map<String, ?> overrides, Clock clock) {
        ExecutionScope scope = prepare(overrides, clock);
        return resultExpression == null ? null : resultExpression.execute(scope);
    }

    private ExecutionScope prepare(Map<String, ?> overrides, Clock clock) {
        Objects.requireNonNull(overrides, "overrides");
        Objects.requireNonNull(clock, "clock");
        rejectSmallestUndeclaredOverride(overrides);

        // Not observable until wrapped in a scope below, so a validation failure here discards this
        // partially-written array with no assignment or provider ever having run against it.
        Object[] frame = frameTemplate.clone();
        for (ExternalSymbol symbol : declaredSymbolsInCanonicalOrder) {
            String name = symbol.name();
            if (!overrides.containsKey(name)) {
                continue;
            }
            requireOverridable(symbol, name);
            Object coerced = symbol.coerceOverride(overrides.get(name), boundaryCoercion);
            ExternalBindingPlan binding = bindingsByName.get(name);
            if (binding != null) {
                frame[binding.frameSlot()] = coerced;
            }
        }

        ExecutionScope scope = new ExecutionScope(frame, zoneId, clock);
        for (AssignmentExecutable assignment : assignments) {
            assignment.execute(scope);
        }
        return scope;
    }

    private void rejectSmallestUndeclaredOverride(Map<String, ?> overrides) {
        String smallestUndeclared = null;
        for (String name : overrides.keySet()) {
            if (declaredSymbolNames.contains(name)) {
                continue;
            }
            if (smallestUndeclared == null || name.compareTo(smallestUndeclared) < 0) {
                smallestUndeclared = name;
            }
        }
        if (smallestUndeclared != null) {
            throw RuntimeFailures.invalidExternalInput("unknown external symbol override: " + smallestUndeclared);
        }
    }

    private static void requireOverridable(ExternalSymbol symbol, String name) {
        if (symbol.overwritePolicy() != ExternalSymbolOverwritePolicy.OVERRIDABLE) {
            throw RuntimeFailures.invalidExternalInput("external symbol '" + name + "' is not overridable");
        }
    }
}
