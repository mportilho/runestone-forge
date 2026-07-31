package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.util.Map;
import java.util.Objects;

/**
 * The minimal numeric fact recorded for a {@code NUMBER} node, keyed by {@code NodeId} in
 * {@link SemanticModel#numericFacts()}. {@code parity} is present only when the exact value is known
 * (literals and simple sign propagation); it is absent for conservatively propagated integral shapes.
 */
public record NumericFact(NumericFactShape shape, RationalParity parity) {

    private static final NumericFact UNKNOWN = new NumericFact(NumericFactShape.UNKNOWN_NUMERIC_VALUE_SHAPE, null);
    private static final NumericFact INTEGRAL_WITHOUT_PARITY = new NumericFact(NumericFactShape.INTEGRAL_KNOWN, null);

    public NumericFact {
        Objects.requireNonNull(shape, "shape");
        if (parity != null && shape == NumericFactShape.UNKNOWN_NUMERIC_VALUE_SHAPE) {
            throw new IllegalArgumentException("unknown numeric shape must not carry a parity payload");
        }
    }

    public static NumericFact unknown() {
        return UNKNOWN;
    }

    public static NumericFact integralWithoutParity() {
        return INTEGRAL_WITHOUT_PARITY;
    }

    public static NumericFact integral(RationalParity parity) {
        return new NumericFact(NumericFactShape.INTEGRAL_KNOWN, Objects.requireNonNull(parity, "parity"));
    }

    public static NumericFact fractional(RationalParity parity) {
        return new NumericFact(NumericFactShape.FRACTIONAL_KNOWN, Objects.requireNonNull(parity, "parity"));
    }

    public static NumericFact of(Map<NodeId, NumericFact> facts, NodeId nodeId) {
        return facts.getOrDefault(nodeId, UNKNOWN);
    }

    public static NumericFact combineAdditive(NumericFact left, NumericFact right) {
        if (left.shape == NumericFactShape.INTEGRAL_KNOWN && right.shape == NumericFactShape.INTEGRAL_KNOWN) {
            return INTEGRAL_WITHOUT_PARITY;
        }
        return UNKNOWN;
    }

    public boolean hasParity() {
        return parity != null;
    }

    public NumericFact negate() {
        if (parity == null) {
            return this;
        }
        return new NumericFact(shape, parity.negate());
    }
}
