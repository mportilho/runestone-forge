package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.semantics.DeferredCheck;

import java.util.List;
import java.util.Objects;

/**
 * Wraps one occurrence of a Subexpressao Comum Memoizada (issue #121, ADR 0019): on first execution of
 * this specific occurrence it evaluates {@code delegate} and stores the result in the plan's memo
 * slot appended past the semantic frame; every later occurrence sharing that slot, whether this same
 * node executed again or a structurally identical sibling occurrence elsewhere in the plan, reads the
 * cached value instead of recomputing. The memo is lazy in place: nothing is hoisted, so an occurrence
 * inside a not-taken lazy branch never runs and never writes the slot, and the first occurrence that
 * actually executes fails exactly where the Oraculo Sem Otimizacoes would fail.
 */
public final class MemoizedExecutableNode implements ExecutableNode {

    private final NodeId id;
    private final SourceSpan sourceSpan;
    private final int memoSlot;
    private final ExecutableNode delegate;
    private final int[] calculationSlots;
    private final int[] replaySlots;

    public MemoizedExecutableNode(
            NodeId id,
            SourceSpan sourceSpan,
            int memoSlot,
            ExecutableNode delegate,
            int[] calculationSlots,
            int[] replaySlots) {
        this.id = Objects.requireNonNull(id, "id");
        this.sourceSpan = Objects.requireNonNull(sourceSpan, "sourceSpan");
        this.memoSlot = memoSlot;
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.calculationSlots = Objects.requireNonNull(calculationSlots, "calculationSlots");
        this.replaySlots = Objects.requireNonNull(replaySlots, "replaySlots");
        if (calculationSlots.length != replaySlots.length) {
            throw new IllegalArgumentException("calculation and replay slots must have equal lengths");
        }
    }

    @Override
    public NodeId id() {
        return id;
    }

    @Override
    public SourceSpan sourceSpan() {
        return sourceSpan;
    }

    @Override
    public Object execute(ExecutionScope scope) {
        if (scope.isMemoUnbound(memoSlot)) {
            Object value = delegate.execute(scope);
            scope.writeMemo(memoSlot, value);
            return value;
        }
        scope.replayCalculations(calculationSlots, replaySlots);
        return scope.readMemo(memoSlot);
    }

    @Override
    public List<DeferredCheck> deferredChecks() {
        return delegate.deferredChecks();
    }
}
