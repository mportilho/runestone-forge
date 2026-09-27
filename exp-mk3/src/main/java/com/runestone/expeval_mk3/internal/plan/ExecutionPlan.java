package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.BoundaryCoercion;
import com.runestone.expeval_mk3.api.CalculationMemory;
import com.runestone.expeval_mk3.api.ComputationWithMemory;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.ExpressionResourceLimits;
import com.runestone.expeval_mk3.api.ExternalSymbol;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.diagnostics.RuntimeFailures;
import com.runestone.expeval_mk3.internal.memory.CalculationMemorySchema;
import com.runestone.expeval_mk3.internal.memory.CalculationRecorder;
import com.runestone.expeval_mk3.internal.runtime.ExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.ExecutionScope;
import com.runestone.expeval_mk3.internal.runtime.PublicMaterialization;
import com.runestone.expeval_mk3.internal.runtime.SafeExecutionScope;
import com.runestone.expeval_mk3.internal.runtime.TraversalLimitedExecutionScope;
import com.runestone.expeval_mk3.internal.runtime.TraversalStepContext;
import com.runestone.expeval_mk3.internal.runtime.ValueShapeValidator;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;

import java.time.Clock;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
    private final ExpressionResourceLimits valueLimits;
    private final boolean traversalLimited;

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
        this(resultExpression, resultType, assignments, externalBindings, declaredSymbolsInCanonicalOrder,
                assignedSymbolsInCreationOrder, foldedVariableReads, fullCalculationMemorySchema,
                assignmentCalculationMemorySchema, frameSize, replaySlotCount, boundaryCoercion,
                zoneId, maxMaterializedSize, null, false);
    }

    ExecutionPlan(
            ExecutableNode resultExpression, ExpressionType resultType, List<AssignmentExecutable> assignments,
            List<ExternalBindingPlan> externalBindings, List<ExternalSymbol> declaredSymbolsInCanonicalOrder,
            List<AssignedSymbol> assignedSymbolsInCreationOrder, List<FoldedRead> foldedVariableReads,
            CalculationMemorySchema fullCalculationMemorySchema,
            CalculationMemorySchema assignmentCalculationMemorySchema, int frameSize, int replaySlotCount,
            BoundaryCoercion boundaryCoercion, ZoneId zoneId, int maxMaterializedSize,
            ExpressionResourceLimits valueLimits, boolean traversalLimited) {
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
        this.valueLimits = valueLimits;
        this.traversalLimited = traversalLimited;
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

    public ExpressionResourceLimits valueLimits() {
        return valueLimits;
    }

    /**
     * Conservative source-controlled payload retained by this plan. Environment services and external
     * defaults remain deliberately outside this measure because they are trusted shared components.
     */
    public int estimatedRetainedWeight() {
        return PlanRetainedWeight.estimate(this);
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

    int externalBindingCount() {
        return externalBindings.size();
    }

    int declaredSymbolCount() {
        return declaredSymbolsInCanonicalOrder.size();
    }

    int frameTemplateLength() {
        return frameTemplate.length;
    }

    CalculationMemorySchema fullCalculationMemorySchema() {
        return fullCalculationMemorySchema;
    }

    CalculationMemorySchema assignmentCalculationMemorySchema() {
        return assignmentCalculationMemorySchema;
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

    public Object computeMaterializedResult(Map<String, ?> overrides, Clock clock) {
        ExecutionScope scope = executeAssignments(overrides, clock);
        Object value = executeResult(scope);
        return PublicMaterialization.materialize(
                value, resultType, maxMaterializedSize, resultSourceSpan(), valueLimits, scope);
    }

    public ComputationWithMemory<Object> computeWithMemory(Map<String, ?> overrides, Clock clock) {
        CalculationRecorder recorder = fullCalculationMemorySchema.newRecorder();
        ExecutionScope scope = executeAssignments(overrides, clock, recorder);
        Object value = executeResult(scope);
        Object result = PublicMaterialization.materialize(
                value, resultType, maxMaterializedSize, resultSourceSpan(), valueLimits, scope);
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

    public Map<String, Object> computeMaterializedAssignments(Map<String, ?> overrides, Clock clock) {
        ExecutionScope scope = executeAssignments(overrides, clock);
        Map<String, Object> materialized = new LinkedHashMap<>();
        for (AssignedSymbol symbol : assignedSymbolsInCreationOrder) {
            scope.visitTraversalStep(symbol.sourceSpan());
            materialized.put(symbol.name(), PublicMaterialization.materialize(
                    scope.read(symbol.frameSlot()), symbol.type(), maxMaterializedSize,
                    symbol.sourceSpan(), valueLimits, scope));
        }
        return Collections.unmodifiableMap(materialized);
    }

    public ComputationWithMemory<Map<String, Object>> computeAssignmentsWithMemory(
            Map<String, ?> overrides, Clock clock) {
        CalculationRecorder recorder = assignmentCalculationMemorySchema.newRecorder();
        ExecutionScope scope = executeAssignments(overrides, clock, recorder);
        Map<String, Object> materialized = new LinkedHashMap<>();
        for (AssignedSymbol symbol : assignedSymbolsInCreationOrder) {
            scope.visitTraversalStep(symbol.sourceSpan());
            materialized.put(symbol.name(), PublicMaterialization.materialize(
                    scope.read(symbol.frameSlot()), symbol.type(), maxMaterializedSize,
                    symbol.sourceSpan(), valueLimits, scope));
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
        requireOverrides(overrides);
        Objects.requireNonNull(clock, "clock");

        // Not observable until wrapped in a scope below, so a validation failure here discards this
        // partially-written array with no assignment or provider ever having run against it.
        Object[] frame;
        if (calculationRecorder == null || memoryFrameSize == frameTemplate.length) {
            frame = frameTemplate.clone();
        } else {
            frame = ExecutionScope.extendFrame(frameTemplate, memoryFrameSize);
        }
        ExecutionScope scope = newScope(frame, clock, calculationRecorder, !overrides.isEmpty());
        if (!overrides.isEmpty()) {
            if (everyDeclaredSymbolHasFrameSlot) {
                applyOverridesWithFrameSlots(overrides, frame, scope);
            } else {
                applyOverridesToPartiallyBoundPlan(overrides, frame, scope);
            }
        }
        for (AssignmentExecutable assignment : assignments) {
            assignment.execute(scope);
        }
        return scope;
    }

    private ExecutionScope newScope(
            Object[] frame, Clock clock, CalculationRecorder recorder, boolean hasOverrides) {
        if (valueLimits != null) {
            return traversalLimited || hasOverrides
                    ? new TraversalLimitedExecutionScope(frame, zoneId, clock, recorder, valueLimits)
                    : new SafeExecutionScope(frame, zoneId, clock, recorder, valueLimits);
        }
        return recorder == null
                ? new ExecutionScope(frame, zoneId, clock)
                : new ExecutionScope(frame, zoneId, clock, recorder);
    }

    private void applyOverridesWithFrameSlots(Map<String, ?> overrides, Object[] frame, ExecutionScope scope) {
        for (int index = 0; index < externalBindings.size(); index++) {
            frame[externalBindings.get(index).frameSlot()] = NO_OVERRIDE;
        }

        String smallestUndeclared = null;
        Iterator<? extends Map.Entry<?, ?>> iterator = overrides.entrySet().iterator();
        while (iterator.hasNext()) {
            scope.visitTraversalStep(null);
            Map.Entry<?, ?> entry = iterator.next();
            String name = requireTextOverrideKey(entry.getKey());
            ExternalBindingPlan binding = bindingsByName.get(name);
            if (binding == null) {
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
            frame[frameSlot] = coerceOverride(symbol, override, scope);
        }
    }

    private void applyOverridesToPartiallyBoundPlan(
            Map<String, ?> overrides, Object[] frame, ExecutionScope scope) {
        rejectSmallestUndeclaredOverride(overrides, scope);
        for (int index = 0; index < declaredSymbolsInCanonicalOrder.size(); index++) {
            ExternalSymbol symbol = declaredSymbolsInCanonicalOrder.get(index);
            String name = symbol.name();
            Object override = overrides.get(name);
            if (override == null && !overrides.containsKey(name)) {
                continue;
            }
            scope.visitTraversalStep(null);
            requireOverridable(symbol, name);
            Object coerced = coerceOverride(symbol, override, scope);
            ExternalBindingPlan binding = bindingsByName.get(name);
            if (binding != null) {
                frame[binding.frameSlot()] = coerced;
            }
        }
    }

    private Object executeResult(ExecutionScope scope) {
        return resultExpression == null ? null : resultExpression.execute(scope);
    }

    private void rejectSmallestUndeclaredOverride(Map<String, ?> overrides, ExecutionScope scope) {
        String smallestUndeclared = null;
        Iterator<?> iterator = overrides.keySet().iterator();
        while (iterator.hasNext()) {
            scope.visitTraversalStep(null);
            Object key = iterator.next();
            String name = requireTextOverrideKey(key);
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

    private static void requireOverrides(Map<String, ?> overrides) {
        if (overrides == null) {
            NullPointerException cause = new NullPointerException("overrides");
            throw RuntimeFailures.invalidExternalInput("external symbol overrides must not be null", cause);
        }
    }

    private static String requireTextOverrideKey(Object key) {
        if (key instanceof String name) {
            return name;
        }
        IllegalArgumentException cause = new IllegalArgumentException(
                key == null ? "override key must not be null" : "override key must be text");
        throw RuntimeFailures.invalidExternalInput(cause.getMessage(), cause);
    }

    private Object coerceOverride(ExternalSymbol symbol, Object override, ExecutionScope scope) {
        if (valueLimits != null) {
            requireValueShape(override, scope);
        }
        try {
            Object value;
            if (scope.enforcesTraversalStepLimit()) {
                TraversalStepContext.push(scope, null);
                try {
                    value = symbol.coerceOverride(override, boundaryCoercion);
                } finally {
                    TraversalStepContext.pop();
                }
            } else {
                value = symbol.coerceOverride(override, boundaryCoercion);
            }
            if (valueLimits != null) {
                requireValueShape(value, scope);
            }
            return value;
        } catch (IllegalArgumentException cause) {
            String message = cause.getMessage() == null
                    ? "external symbol '" + symbol.name() + "' override is invalid"
                    : cause.getMessage();
            throw RuntimeFailures.invalidExternalInput(message, cause);
        }
    }

    private void requireValueShape(Object value, ExecutionScope scope) {
        ValueShapeValidator.Violation violation = ValueShapeValidator.check(value, valueLimits, scope, null);
        if (violation != null) {
            if (violation.kind() == ValueShapeValidator.Kind.MATERIALIZATION
                    || violation.kind() == ValueShapeValidator.Kind.FORBIDDEN_NULL) {
                throw RuntimeFailures.invalidExternalInput(violation.message());
            }
            throw RuntimeFailures.domainViolation(DiagnosticCode.RUNTIME_VALUE_SHAPE_EXCEEDED,
                    violation.message(), null);
        }
    }
}
