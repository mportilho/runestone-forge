package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.BoundaryCoercion;
import com.runestone.expeval_mk3.api.CalculationMemory;
import com.runestone.expeval_mk3.api.ComputationWithMemory;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.ExternalSymbol;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.diagnostics.RuntimeFailures;
import com.runestone.expeval_mk3.internal.memory.CalculationMemorySchema;
import com.runestone.expeval_mk3.internal.memory.CalculationRecorder;
import com.runestone.expeval_mk3.internal.runtime.ExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.ExecutionScope;
import com.runestone.expeval_mk3.internal.runtime.PublicMaterialization;

import java.time.Clock;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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

    private static final Object NO_OVERRIDE = new Object();

    private final ExecutableNode resultExpression;
    private final ExpressionType resultType;
    private final List<AssignmentExecutable> assignments;
    private final List<ExternalBindingPlan> externalBindings;
    private final Map<String, ExternalBindingPlan> bindingsByName;
    private final List<ExternalSymbol> declaredSymbolsInCanonicalOrder;
    private final Set<String> declaredSymbolNames;
    private final boolean everyDeclaredSymbolHasFrameSlot;
    private final List<AssignedSymbol> assignedSymbolsInCreationOrder;
    private final List<FoldedRead> foldedVariableReads;
    private final CalculationMemorySchema fullCalculationMemorySchema;
    private final CalculationMemorySchema assignmentCalculationMemorySchema;
    private final Object[] frameTemplate;
    private final int memoryFrameSize;
    private final BoundaryCoercion boundaryCoercion;
    private final ZoneId zoneId;
    private final int maxMaterializedSize;

    ExecutionPlan(
            ExecutableNode resultExpression,
            ExpressionType resultType,
            List<AssignmentExecutable> assignments,
            List<ExternalBindingPlan> externalBindings,
            List<ExternalSymbol> declaredSymbolsInCanonicalOrder,
            List<AssignedSymbol> assignedSymbolsInCreationOrder,
            List<FoldedRead> foldedVariableReads,
            CalculationMemorySchema fullCalculationMemorySchema,
            CalculationMemorySchema assignmentCalculationMemorySchema,
            int frameSize,
            int replaySlotCount,
            BoundaryCoercion boundaryCoercion,
            ZoneId zoneId,
            int maxMaterializedSize) {
        if ((resultExpression == null) != (resultType == null)) {
            throw new IllegalStateException("resultType must be present if and only if resultExpression is present");
        }
        this.resultExpression = resultExpression;
        this.resultType = resultType;
        this.assignments = List.copyOf(assignments);
        this.externalBindings = List.copyOf(externalBindings);
        this.assignedSymbolsInCreationOrder = List.copyOf(assignedSymbolsInCreationOrder);
        this.foldedVariableReads = List.copyOf(foldedVariableReads);
        this.fullCalculationMemorySchema = Objects.requireNonNull(
                fullCalculationMemorySchema, "fullCalculationMemorySchema");
        this.assignmentCalculationMemorySchema = Objects.requireNonNull(
                assignmentCalculationMemorySchema, "assignmentCalculationMemorySchema");
        bindingsByName = this.externalBindings.stream()
                .collect(Collectors.toUnmodifiableMap(binding -> binding.symbol().name(), binding -> binding));
        this.declaredSymbolsInCanonicalOrder = List.copyOf(declaredSymbolsInCanonicalOrder);
        declaredSymbolNames = this.declaredSymbolsInCanonicalOrder.stream()
                .map(ExternalSymbol::name)
                .collect(Collectors.toUnmodifiableSet());
        everyDeclaredSymbolHasFrameSlot = this.externalBindings.size() == this.declaredSymbolsInCanonicalOrder.size();
        Object[] template = ExecutionScope.blankFrame(frameSize);
        for (ExternalBindingPlan binding : this.externalBindings) {
            template[binding.frameSlot()] = binding.symbol().defaultValue().value();
        }
        this.frameTemplate = template;
        this.memoryFrameSize = frameSize + replaySlotCount;
        this.boundaryCoercion = Objects.requireNonNull(boundaryCoercion, "boundaryCoercion");
        this.zoneId = Objects.requireNonNull(zoneId, "zoneId");
        this.maxMaterializedSize = maxMaterializedSize;
    }

    public boolean hasResult() {
        return resultExpression != null;
    }

    /**
     * The result expression's resolved public type, or {@code null} for an assignment-only plan.
     */
    public ExpressionType resultType() {
        return resultType;
    }

    /**
     * The result expression's source position, or {@code null} for an assignment-only plan.
     */
    public SourceSpan resultSourceSpan() {
        return resultExpression == null ? null : resultExpression.sourceSpan();
    }

    public int maxMaterializedSize() {
        return maxMaterializedSize;
    }

    /**
     * Every internal symbol reachable through the assignments view, in first-creation source order.
     * Reassignment reuses the same frame slot and does not move a symbol's position.
     */
    public List<AssignedSymbol> assignedSymbolsInCreationOrder() {
        return assignedSymbolsInCreationOrder;
    }

    /**
     * Every symbol read that collapsed into a compile-time constant (ADR 0019, issue #117), in build
     * order. Construction-time metadata only, with no public consumer before the Etapa 10 audit.
     */
    List<FoldedRead> foldedVariableReads() {
        return foldedVariableReads;
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
        ExecutionScope scope = executeAssignments(overrides, clock);
        return executeResult(scope);
    }

    public ComputationWithMemory<Object> computeWithMemory(Map<String, ?> overrides, Clock clock) {
        CalculationRecorder recorder = fullCalculationMemorySchema.newRecorder();
        ExecutionScope scope = executeAssignments(overrides, clock, recorder);
        Object value = executeResult(scope);
        Object result = PublicMaterialization.materialize(
                value, resultType, maxMaterializedSize, resultSourceSpan());
        CalculationMemory memory = fullCalculationMemorySchema.freeze(scope, recorder);
        return new ComputationWithMemory<>(result, memory);
    }

    /**
     * Runs assignments in source order only, deliberately skipping any final result expression, and
     * returns each assigned symbol's final raw value in {@link #assignedSymbolsInCreationOrder()} order.
     */
    public List<Object> computeAssignedValues(Map<String, ?> overrides, Clock clock) {
        ExecutionScope scope = executeAssignments(overrides, clock);
        List<Object> values = new ArrayList<>(assignedSymbolsInCreationOrder.size());
        for (AssignedSymbol symbol : assignedSymbolsInCreationOrder) {
            values.add(scope.read(symbol.frameSlot()));
        }
        return values;
    }

    public ComputationWithMemory<Map<String, Object>> computeAssignmentsWithMemory(
            Map<String, ?> overrides, Clock clock) {
        CalculationRecorder recorder = assignmentCalculationMemorySchema.newRecorder();
        ExecutionScope scope = executeAssignments(overrides, clock, recorder);
        Map<String, Object> materialized = new LinkedHashMap<>();
        for (AssignedSymbol symbol : assignedSymbolsInCreationOrder) {
            materialized.put(symbol.name(), PublicMaterialization.materialize(
                    scope.read(symbol.frameSlot()), symbol.type(), maxMaterializedSize, symbol.sourceSpan()));
        }
        Map<String, Object> result = Collections.unmodifiableMap(materialized);
        CalculationMemory memory = assignmentCalculationMemorySchema.freeze(scope, recorder);
        return new ComputationWithMemory<>(result, memory);
    }

    private ExecutionScope executeAssignments(Map<String, ?> overrides, Clock clock) {
        return executeAssignments(overrides, clock, null);
    }

    private ExecutionScope executeAssignments(
            Map<String, ?> overrides, Clock clock, CalculationRecorder calculationRecorder) {
        Objects.requireNonNull(overrides, "overrides");
        Objects.requireNonNull(clock, "clock");

        // Not observable until wrapped in a scope below, so a validation failure here discards this
        // partially-written array with no assignment or provider ever having run against it.
        Object[] frame;
        if (calculationRecorder == null || memoryFrameSize == frameTemplate.length) {
            frame = frameTemplate.clone();
        } else {
            frame = ExecutionScope.extendFrame(frameTemplate, memoryFrameSize);
        }
        if (!overrides.isEmpty()) {
            if (everyDeclaredSymbolHasFrameSlot) {
                applyOverridesWithFrameSlots(overrides, frame);
            } else {
                applyOverridesToPartiallyBoundPlan(overrides, frame);
            }
        }

        ExecutionScope scope = calculationRecorder == null
                ? new ExecutionScope(frame, zoneId, clock)
                : new ExecutionScope(frame, zoneId, clock, calculationRecorder);
        for (AssignmentExecutable assignment : assignments) {
            assignment.execute(scope);
        }
        return scope;
    }

    private void applyOverridesWithFrameSlots(Map<String, ?> overrides, Object[] frame) {
        for (int index = 0; index < externalBindings.size(); index++) {
            frame[externalBindings.get(index).frameSlot()] = NO_OVERRIDE;
        }

        String smallestUndeclared = null;
        for (Map.Entry<String, ?> entry : overrides.entrySet()) {
            ExternalBindingPlan binding = bindingsByName.get(entry.getKey());
            if (binding == null) {
                String name = entry.getKey();
                if (smallestUndeclared == null || name.compareTo(smallestUndeclared) < 0) {
                    smallestUndeclared = name;
                }
            } else {
                frame[binding.frameSlot()] = entry.getValue();
            }
        }
        rejectUndeclaredOverride(smallestUndeclared);

        for (int index = 0; index < externalBindings.size(); index++) {
            ExternalBindingPlan binding = externalBindings.get(index);
            int frameSlot = binding.frameSlot();
            Object override = frame[frameSlot];
            if (override == NO_OVERRIDE) {
                frame[frameSlot] = frameTemplate[frameSlot];
                continue;
            }
            ExternalSymbol symbol = binding.symbol();
            requireOverridable(symbol, symbol.name());
            frame[frameSlot] = symbol.coerceOverride(override, boundaryCoercion);
        }
    }

    private void applyOverridesToPartiallyBoundPlan(Map<String, ?> overrides, Object[] frame) {
        rejectSmallestUndeclaredOverride(overrides);
        for (int index = 0; index < declaredSymbolsInCanonicalOrder.size(); index++) {
            ExternalSymbol symbol = declaredSymbolsInCanonicalOrder.get(index);
            String name = symbol.name();
            Object override = overrides.get(name);
            if (override == null && !overrides.containsKey(name)) {
                continue;
            }
            requireOverridable(symbol, name);
            Object coerced = symbol.coerceOverride(override, boundaryCoercion);
            ExternalBindingPlan binding = bindingsByName.get(name);
            if (binding != null) {
                frame[binding.frameSlot()] = coerced;
            }
        }
    }

    private Object executeResult(ExecutionScope scope) {
        return resultExpression == null ? null : resultExpression.execute(scope);
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
        rejectUndeclaredOverride(smallestUndeclared);
    }

    private static void rejectUndeclaredOverride(String smallestUndeclared) {
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
