package com.runestone.expeval_mk3.api;

/** Resource enforcement scope; functional language contracts apply in every mode. */
public enum ExpressionTrustMode {
    /** Resource containment is delegated to the integrator. */
    UNSAFE,
    /** Enforces compilation resources only (the default). */
    TRUSTED,
    /** Enforces both compilation and execution resources. */
    SAFE
}
