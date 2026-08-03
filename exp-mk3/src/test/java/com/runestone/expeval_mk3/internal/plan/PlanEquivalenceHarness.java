package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionDiagnostic;
import com.runestone.expeval_mk3.api.ExpressionExecutionException;
import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;

import java.time.Clock;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The ADR 0019 proof mechanism: builds both the optimized plan and the Unoptimized Oracle from the same
 * {@code SemanticModel}, runs both, and asserts they agree in value, scale, failure code and source span,
 * and observable effect order. This is a test-only class, kept public so it can be driven from the
 * corpus package and from property tests without exposing the oracle path as a public library seam.
 */
public final class PlanEquivalenceHarness {

    private PlanEquivalenceHarness() {
    }

    public static void assertEquivalent(
            SemanticModel model, ExpressionEnvironment environment, Map<String, ?> inputs, Clock clock) {
        assertEquivalent(model, environment, inputs, clock, () -> { }, () -> null);
    }

    public static void assertEquivalent(
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<String, ?> inputs,
            Clock clock,
            Runnable resetObservedEffects,
            Supplier<?> observedEffectsSnapshot) {
        ExecutionPlanBuilder builder = new ExecutionPlanBuilder();
        ExecutionPlan optimized = builder.build(model, environment);
        ExecutionPlan oracle = builder.buildOracle(model, environment);

        resetObservedEffects.run();
        Outcome optimizedOutcome = execute(optimized, inputs, clock);
        Object optimizedEffects = observedEffectsSnapshot.get();

        resetObservedEffects.run();
        Outcome oracleOutcome = execute(oracle, inputs, clock);
        Object oracleEffects = observedEffectsSnapshot.get();

        assertThat(optimizedOutcome).isEqualTo(oracleOutcome);
        assertThat(optimizedEffects).isEqualTo(oracleEffects);
    }

    private static Outcome execute(ExecutionPlan plan, Map<String, ?> inputs, Clock clock) {
        try {
            Object value = plan.hasResult() ? plan.compute(inputs, clock) : plan.computeAssignedValues(inputs, clock);
            return Outcome.success(value);
        } catch (ExpressionExecutionException exception) {
            ExpressionDiagnostic diagnostic = exception.diagnostic();
            return Outcome.failure(diagnostic.code(), diagnostic.primarySpan().orElse(null), diagnostic.message());
        } catch (RuntimeException exception) {
            return Outcome.failure(exception.getClass().getName(), null, exception.getMessage());
        }
    }

    private record Outcome(Object value, String failureCode, SourceSpan failureSpan, String failureMessage) {

        static Outcome success(Object value) {
            return new Outcome(value, null, null, null);
        }

        static Outcome failure(String code, SourceSpan span, String message) {
            return new Outcome(null, code, span, message);
        }
    }
}
