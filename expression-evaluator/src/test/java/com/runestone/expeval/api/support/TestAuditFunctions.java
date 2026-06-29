package com.runestone.expeval.api.support;

import java.math.BigDecimal;

/** Test-only function provider for audit policy coverage. */
public final class TestAuditFunctions {

    private TestAuditFunctions() {
    }

    public static BigDecimal zero() {
        return BigDecimal.ZERO;
    }
}
