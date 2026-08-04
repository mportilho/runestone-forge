package com.runestone.expeval_mk3.internal.runtime;

/**
 * The single seam {@code ExecutionPlanBuilder} uses to attempt eager constant folding (ADR 0019,
 * issue #115). There is no second constant evaluator: a candidate node is executed by itself, against
 * {@link ConstantFoldSentinelScope}, so folding stays in semantic parity with runtime by construction.
 *
 * <p>A subtree folds only when every child the caller names as required is already a
 * {@link ConstantExecutableNode} — the same test a literal already satisfies. A fold that fails at
 * this stage (division by zero, a domain violation, a bound exceeded) leaves the built node
 * unfolded, so it fails during execution exactly as the Unoptimized Oracle would, with the same
 * diagnostic code and source span; a node's {@link com.runestone.expeval_mk3.internal.semantics.DeferredCheck}
 * list is discarded only when its subtree is replaced by a folded constant, since a
 * {@link ConstantExecutableNode} carries no checks of its own.
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
}
