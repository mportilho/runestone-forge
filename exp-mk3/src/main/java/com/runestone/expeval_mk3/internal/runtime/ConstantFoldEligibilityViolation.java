package com.runestone.expeval_mk3.internal.runtime;

/**
 * Thrown by {@link ConstantFoldSentinelScope} when a subtree the plan builder believed eligible for
 * constant folding turns out to read a frame slot or consult the clock. Eligibility is derived from
 * purity recorded during semantic resolution (ADR 0019), never inferred by the builder itself, so
 * reaching this path is always an internal bug in that derivation and never a normal execution
 * failure; the plan builder lets it propagate as a loud {@code IllegalStateException} instead of
 * silently leaving the subtree unfolded.
 */
final class ConstantFoldEligibilityViolation extends RuntimeException {

    ConstantFoldEligibilityViolation(String message) {
        super(message);
    }
}
