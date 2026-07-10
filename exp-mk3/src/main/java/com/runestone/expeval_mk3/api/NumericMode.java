package com.runestone.expeval_mk3.api;

/**
 * Numeric interpretation policy used when compiling and executing expressions.
 */
public enum NumericMode {
    /**
     * Decimal-first mode for correctness-focused numeric behavior.
     */
    DECIMAL,

    /**
     * Fast numeric mode for later optimized integer and floating-point paths.
     */
    FAST
}
