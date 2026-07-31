package com.runestone.expeval_mk3.internal.semantics;

/**
 * The minimal integral/fractional shape known for a {@code NUMBER} node, per Etapa 4 Fatos Numericos.
 * Facts never change the public {@code NUMBER} type; they only support semantic validation.
 */
public enum NumericFactShape {
    INTEGRAL_KNOWN,
    FRACTIONAL_KNOWN,
    UNKNOWN_NUMERIC_VALUE_SHAPE
}
