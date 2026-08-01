package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The invariant behind issue #92: {@link DeferredCheck} is a closed hierarchy of value preconditions on
 * already-typed constructs, each carrying node identity, source span, and a stable runtime diagnostic
 * code. This test fails to compile if a new permitted subtype is added without updating the exhaustive
 * switch, and fails at runtime if a subtype's {@code runtimeCode()} stops being stable per instance.
 */
class DeferredCheckCoverageTest {

    private static final NodeId NODE_ID = new NodeId(0);
    private static final SourceSpan SPAN = new SourceSpan(0, 1, 1, 1);

    private static final List<DeferredCheck> ONE_OF_EACH = List.of(
            new FactorialIntegralDeferredCheck(NODE_ID, SPAN),
            new FactorialNonNegativeDeferredCheck(NODE_ID, SPAN),
            new FactorialMaxBoundDeferredCheck(NODE_ID, SPAN, 1_000),
            new PowerRealDomainDeferredCheck(NODE_ID, SPAN),
            new RootRealDomainDeferredCheck(NODE_ID, SPAN),
            new DestructuringMinimumSizeDeferredCheck(NODE_ID, SPAN, 2),
            new SubscriptBoundsDeferredCheck(NODE_ID, SPAN),
            new MaterializationLimitDeferredCheck(NODE_ID, SPAN, 1_000));

    @Test
    void everyPermittedSubtypeIsCoveredByAnExhaustiveSwitch() {
        for (DeferredCheck check : ONE_OF_EACH) {
            assertThat(runtimeCodeViaExhaustiveSwitch(check)).isEqualTo(check.runtimeCode());
        }
    }

    @Test
    void oneOfEachCoversTheEntirePermittedHierarchy() {
        Set<Class<?>> covered = ONE_OF_EACH.stream().map(Object::getClass).collect(java.util.stream.Collectors.toSet());
        assertThat(DeferredCheck.class.getPermittedSubclasses()).containsExactlyInAnyOrderElementsOf(covered);
    }

    @Test
    void everyCheckCarriesItsNodeIdAndSourceSpan() {
        for (DeferredCheck check : ONE_OF_EACH) {
            assertThat(check.nodeId()).isEqualTo(NODE_ID);
            assertThat(check.sourceSpan()).isEqualTo(SPAN);
            assertThat(check.runtimeCode()).isNotNull();
        }
    }

    private static DiagnosticCode runtimeCodeViaExhaustiveSwitch(DeferredCheck check) {
        return switch (check) {
            case FactorialIntegralDeferredCheck ignored -> DiagnosticCode.RUNTIME_FACTORIAL_NOT_INTEGRAL;
            case FactorialNonNegativeDeferredCheck ignored -> DiagnosticCode.RUNTIME_FACTORIAL_NEGATIVE;
            case FactorialMaxBoundDeferredCheck ignored -> DiagnosticCode.RUNTIME_FACTORIAL_EXCEEDS_MAXIMUM;
            case PowerRealDomainDeferredCheck ignored -> DiagnosticCode.RUNTIME_POWER_COMPLEX_DOMAIN;
            case RootRealDomainDeferredCheck ignored -> DiagnosticCode.RUNTIME_ROOT_COMPLEX_DOMAIN;
            case DestructuringMinimumSizeDeferredCheck ignored -> DiagnosticCode.RUNTIME_DESTRUCTURING_SIZE_TOO_SMALL;
            case SubscriptBoundsDeferredCheck ignored -> DiagnosticCode.RUNTIME_SUBSCRIPT_OUT_OF_BOUNDS;
            case MaterializationLimitDeferredCheck ignored -> DiagnosticCode.RUNTIME_MATERIALIZATION_LIMIT_EXCEEDED;
        };
    }
}
