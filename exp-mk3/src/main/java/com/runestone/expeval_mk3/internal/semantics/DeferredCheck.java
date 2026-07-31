package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;

/**
 * A closed, typed value precondition on an already-typed construct, deferred from compilation to
 * execution because the concrete value is not statically known. Deferred checks never represent type,
 * overload, or navigation-member choice; those remain compilation diagnostics. {@link SemanticModel#deferredChecks()}
 * is consumed by the execution plan without rediscovering the semantic rule that produced each check.
 */
public sealed interface DeferredCheck
        permits FactorialIntegralDeferredCheck, FactorialNonNegativeDeferredCheck, FactorialMaxBoundDeferredCheck,
        PowerRealDomainDeferredCheck, RootRealDomainDeferredCheck, DestructuringMinimumSizeDeferredCheck,
        SubscriptBoundsDeferredCheck, MaterializationLimitDeferredCheck {

    NodeId nodeId();

    SourceSpan sourceSpan();

    DiagnosticCode runtimeCode();
}
