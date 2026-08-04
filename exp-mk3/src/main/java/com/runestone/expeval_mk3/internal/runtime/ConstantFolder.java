package com.runestone.expeval_mk3.internal.runtime;

import java.util.ArrayList;
import java.util.List;

/**
 * The single seam {@code ExecutionPlanBuilder} uses to attempt constant folding (ADR 0019, issues
 * #115 and #116). There is no second constant evaluator: an eager candidate node is executed by
 * itself, against {@link ConstantFoldSentinelScope}, so folding stays in semantic parity with runtime
 * by construction.
 *
 * <p>A subtree folds only when every child the caller names as required is already a
 * {@link ConstantExecutableNode} — the same test a literal already satisfies. A fold that fails at
 * this stage (division by zero, a domain violation, a bound exceeded) leaves the built node
 * unfolded, so it fails during execution exactly as the Unoptimized Oracle would, with the same
 * diagnostic code and source span; a node's {@link com.runestone.expeval_mk3.internal.semantics.DeferredCheck}
 * list is discarded only when its subtree is replaced by a folded constant, since a
 * {@link ConstantExecutableNode} carries no checks of its own.
 *
 * <p>The lazy constructs ({@code ??} and the conditional) fold differently: {@link #foldNullCoalesce}
 * and {@link #foldConditional} never execute a discarded branch, not even against the sentinel,
 * because the branch a lazy construct does not take is exactly the branch the Unoptimized Oracle
 * would not evaluate either (issue #116). Folding them is a structural rewrite — drop what can never
 * be reached — not a value computation.
 */
public final class ConstantFolder {

    private ConstantFolder() {
    }

    public static ExecutableNode fold(ExecutableNode built, ExecutableNode... requiredConstantChildren) {
        for (ExecutableNode child : requiredConstantChildren) {
            if (!(child instanceof ConstantExecutableNode)) {
                return built;
            }
        }
        Object value;
        try {
            value = built.execute(ConstantFoldSentinelScope.INSTANCE);
        } catch (ConstantFoldEligibilityViolation violation) {
            throw new IllegalStateException(
                    "constant fold eligibility violation for node " + built.id() + ": " + violation.getMessage(),
                    violation);
        } catch (RuntimeException executionFailure) {
            return built;
        }
        return value == null ? built : new ConstantExecutableNode(built.id(), built.sourceSpan(), value);
    }

    /**
     * When the first operand already folded to a constant, it is necessarily non-null (a
     * {@link ConstantExecutableNode} never holds {@code null}), so the coalesce can never reach its
     * later operands: it collapses to that first operand without evaluating anything else.
     */
    public static ExecutableNode foldNullCoalesce(NullCoalesceExecutableNode built) {
        ExecutableNode first = built.operands().getFirst();
        return first instanceof ConstantExecutableNode ? first : built;
    }

    /**
     * A branch whose condition already folded to constant {@code false} can never be taken and is
     * dropped; one that folded to constant {@code true} is guaranteed taken over every branch and the
     * else expression after it, so the conditional collapses to whatever non-constant branches remain
     * before it, with this branch's consequence standing in as their else. Neither a dropped branch nor
     * its condition is ever executed.
     */
    public static ExecutableNode foldConditional(ConditionalExecutableNode built) {
        List<ExecutableBranch> branches = built.branches();
        List<ExecutableBranch> remaining = new ArrayList<>();
        for (ExecutableBranch branch : branches) {
            if (!(branch.condition() instanceof ConstantExecutableNode constantCondition)) {
                remaining.add(branch);
                continue;
            }
            if (Boolean.TRUE.equals(constantCondition.value())) {
                return remaining.isEmpty()
                        ? branch.consequence()
                        : new ConditionalExecutableNode(
                                built.id(), built.sourceSpan(), List.copyOf(remaining), branch.consequence());
            }
        }
        if (remaining.size() == branches.size()) {
            return built;
        }
        return remaining.isEmpty()
                ? built.elseExpression()
                : new ConditionalExecutableNode(built.id(), built.sourceSpan(), List.copyOf(remaining), built.elseExpression());
    }
}
