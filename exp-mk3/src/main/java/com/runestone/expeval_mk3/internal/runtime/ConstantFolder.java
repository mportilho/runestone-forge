package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.FunctionDescriptor;
import com.runestone.expeval_mk3.api.MapType;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.UnaryOperator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    /**
     * The only implementation the elision in {@link #foldAssertion} is authorized to trust. A
     * language-name check alone would be provenance-blind: it cannot distinguish the genuine
     * built-in from a custom function that happens to reuse the name {@code asNumber} and its
     * signature but does real, non-identity work. Matching the exact declaring class instead ties
     * the elision to the one implementation whose no-op behavior this fold actually verified.
     */
    private static final String SCALAR_ASSERTION_OWNER = "com.runestone.expeval_mk3.api.AssertionBuiltInFunctions";

    /**
     * The single size at which {@code in} switches from a linear scan over the pre-evaluated constant
     * to a downloaded lookup structure (issue #119); the same value for both the sorted-array and the
     * hash-set representation, since two distinct thresholds would not have paid for themselves in
     * testing.
     */
    private static final int MEMBERSHIP_DOWNLOAD_THRESHOLD = 8;

    private ConstantFolder() {
    }

    public static ExecutableNode fold(ExecutableNode built, ExecutableNode... requiredConstantChildren) {
        for (ExecutableNode child : requiredConstantChildren) {
            if (!(child instanceof ConstantExecutableNode)) {
                return built;
            }
        }
        Object value;
        ConstantFoldSentinelScope foldScope = requiresCalculationCapture(built, requiredConstantChildren)
                ? ConstantFoldSentinelScope.capturing()
                : ConstantFoldSentinelScope.INSTANCE;
        try {
            value = built.execute(foldScope);
        } catch (ConstantFoldEligibilityViolation violation) {
            throw new IllegalStateException(
                    "constant fold eligibility violation for node " + built.id() + ": " + violation.getMessage(),
                    violation);
        } catch (RuntimeException executionFailure) {
            return built;
        }
        if (value == null) {
            return built;
        }
        StaticCalculationGroup calculationGroup = foldScope.calculationGroup();
        return calculationGroup.isEmpty()
                ? new ConstantExecutableNode(built.id(), built.sourceSpan(), value)
                : new CapturedConstantExecutableNode(built.id(), built.sourceSpan(), value, calculationGroup);
    }

    private static boolean requiresCalculationCapture(
            ExecutableNode built, ExecutableNode[] requiredConstantChildren) {
        if (built instanceof CalculationPointExecutableNode calculationPoint
                && (calculationPoint.calculationSlot() >= 0 || calculationPoint.replaySlots().length > 0)) {
            return true;
        }
        for (ExecutableNode child : requiredConstantChildren) {
            if (!((ConstantExecutableNode) child).calculationGroup().isEmpty()) {
                return true;
            }
        }
        return false;
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
            if (!constantCondition.calculationGroup().isEmpty()) {
                remaining.add(branch);
                if (Boolean.TRUE.equals(constantCondition.value())) {
                    return new ConditionalExecutableNode(
                            built.id(), built.sourceSpan(), List.copyOf(remaining), branch.consequence());
                }
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

    /**
     * A scalar assertion (one of the six {@code asNumber}/{@code asText}/{@code asBool}/{@code asDate}/
     * {@code asTime}/{@code asDateTime} built-ins, issue #118) is elided to its bare argument when the
     * overload semantic resolution bound is the one whose parameter type already equals its return
     * type: the boundary coercion it would perform on that overload returns the argument unchanged, so
     * the call is a proven no-op regardless of whether the argument is itself constant. Any other
     * overload keeps the call, since the boundary conversion it performs there does real work,
     * including work that can fail with the diagnostic the Unoptimized Oracle would also raise.
     */
    public static ExecutableNode foldAssertion(FunctionCallExecutableNode built) {
        FunctionDescriptor descriptor = built.descriptor();
        if (isElidableAssertion(descriptor)) {
            return built.arguments().getFirst();
        }
        return built;
    }

    public static boolean isElidableAssertion(FunctionDescriptor descriptor) {
        return descriptor.arity() == 1
                && SCALAR_ASSERTION_OWNER.equals(descriptor.implementationMetadata().owner())
                && descriptor.parameterTypes().getFirst().equals(descriptor.returnType());
    }

    /**
     * {@code not not x} cancels to {@code x} regardless of whether {@code x} is constant: the operand
     * of a {@code not} that is itself a {@code not} is statically guaranteed non-nullable boolean
     * (semantic resolution rejects a nullable unary operand), so re-observing it through two
     * cancelling negations can never change what executing it would do or throw. This is the only
     * algebraic rewrite issue #118 authorizes; it is applied once per built node, so a longer chain
     * cancels pairwise as each enclosing {@code not} is built.
     */
    public static ExecutableNode foldDoubleNegation(UnaryExecutableNode built) {
        if (built.operator() == UnaryOperator.LOGICAL_NOT
                && built.operand() instanceof UnaryExecutableNode inner
                && inner.operator() == UnaryOperator.LOGICAL_NOT) {
            return inner.operand();
        }
        return built;
    }

    /**
     * A constant right-hand collection at or above {@link #MEMBERSHIP_DOWNLOAD_THRESHOLD} downloads to
     * a lookup structure chosen by element type (issue #119): {@code NUMBER} downloads to a sorted
     * array searched by {@code compareTo}, and {@code STRING}/{@code BOOLEAN}/{@code DATE}/{@code TIME}/
     * {@code DATETIME} download to a hash set — exactly the types {@link ExpressionRuntime#structuralEquals}
     * compares by {@code equals}. A collection or map element never downloads, because
     * {@code List.equals}/{@code Map.equals} compare contained numbers by {@code equals} and would
     * diverge from the recursive structural equality {@code in} actually uses. A constant map
     * right-hand side is left untouched: its {@code containsKey} lookup is already efficient, and the
     * constant itself was already pre-evaluated by the External Symbol fold (issue #117). Below the
     * threshold, the pre-evaluated constant collection is kept and {@code built}'s own linear scan is
     * used, since the reconstruction it already eliminates is the only thing worth paying for there.
     */
    public static ExecutableNode foldMembership(MembershipExecutableNode built) {
        if (!(built.collection() instanceof ConstantExecutableNode constantCollection)
                || !constantCollection.calculationGroup().isEmpty()
                || !(built.collectionType() instanceof CollectionType collectionType)) {
            return built;
        }
        ExpressionType elementType = collectionType.elementType();
        if (elementType instanceof CollectionType || elementType instanceof MapType) {
            return built;
        }
        List<?> elements = (List<?>) constantCollection.value();
        if (elements.size() < MEMBERSHIP_DOWNLOAD_THRESHOLD) {
            return built;
        }
        if (elementType == ScalarType.NUMBER) {
            List<BigDecimal> sortedElements = elements.stream().map(BigDecimal.class::cast).sorted().toList();
            return new SortedNumberMembershipExecutableNode(
                    built.id(), built.sourceSpan(), built.negated(), built.element(), sortedElements);
        }
        return new HashLookupMembershipExecutableNode(
                built.id(), built.sourceSpan(), built.negated(), built.element(), Set.copyOf(elements));
    }
}
