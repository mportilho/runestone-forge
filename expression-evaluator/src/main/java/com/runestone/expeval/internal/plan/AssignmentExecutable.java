package com.runestone.expeval.internal.plan;

import com.runestone.expeval.api.SourceSpan;
import com.runestone.expeval.internal.ast.NodeId;
import com.runestone.expeval.internal.diagnostics.RuntimeFailures;
import com.runestone.expeval.internal.runtime.ExecutableNode;
import com.runestone.expeval.internal.runtime.ExecutionScope;
import com.runestone.expeval.internal.semantics.DeferredCheck;
import com.runestone.expeval.internal.semantics.DestructuringMinimumSizeDeferredCheck;

import java.util.List;
import java.util.Objects;

/**
 * A source-ordered assignment in the plan: either a single identifier target or a destructuring target
 * writing several frame slots from one evaluated list. Preserves the target's {@link NodeId} and
 * {@link SourceSpan} instead of an opaque {@code Consumer<ExecutionScope>}.
 */
public final class AssignmentExecutable {

    private final NodeId id;
    private final SourceSpan sourceSpan;
    private final ExecutableNode expression;
    private final int[] frameSlots;
    private final DestructuringMinimumSizeDeferredCheck minimumSizeCheck;

    private AssignmentExecutable(
            NodeId id,
            SourceSpan sourceSpan,
            ExecutableNode expression,
            int[] frameSlots,
            DestructuringMinimumSizeDeferredCheck minimumSizeCheck) {
        this.id = Objects.requireNonNull(id, "id");
        this.sourceSpan = Objects.requireNonNull(sourceSpan, "sourceSpan");
        this.expression = Objects.requireNonNull(expression, "expression");
        this.frameSlots = frameSlots.clone();
        this.minimumSizeCheck = minimumSizeCheck;
    }

    public static AssignmentExecutable identifier(
            NodeId id, SourceSpan sourceSpan, int frameSlot, ExecutableNode expression) {
        return new AssignmentExecutable(id, sourceSpan, expression, new int[] {frameSlot}, null);
    }

    public static AssignmentExecutable destructuring(
            NodeId id,
            SourceSpan sourceSpan,
            int[] frameSlots,
            ExecutableNode expression,
            DestructuringMinimumSizeDeferredCheck minimumSizeCheck) {
        if (frameSlots.length == 0) {
            throw new IllegalArgumentException("frameSlots must not be empty");
        }
        return new AssignmentExecutable(id, sourceSpan, expression, frameSlots, minimumSizeCheck);
    }

    public NodeId id() {
        return id;
    }

    public SourceSpan sourceSpan() {
        return sourceSpan;
    }

    public List<DeferredCheck> deferredChecks() {
        return minimumSizeCheck == null ? List.of() : List.of(minimumSizeCheck);
    }

    ExecutableNode expression() {
        return expression;
    }

    int frameSlotCount() {
        return frameSlots.length;
    }

    public void execute(ExecutionScope scope) {
        if (frameSlots.length == 1) {
            scope.write(frameSlots[0], expression.execute(scope));
            return;
        }
        List<?> values = (List<?>) expression.execute(scope);
        if (values.size() < frameSlots.length) {
            throw RuntimeFailures.destructuringInsufficient(frameSlots.length, values.size(), sourceSpan);
        }
        for (int index = 0; index < frameSlots.length; index++) {
            com.runestone.expeval.internal.runtime.TraversalSteps.visit(scope, sourceSpan);
            scope.write(frameSlots[index], values.get(index));
        }
    }
}
