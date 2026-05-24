package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.AuditEvent;
import com.runestone.expeval.api.AuditResult;
import com.runestone.expeval.catalog.ExternalSymbolCatalog;
import com.runestone.expeval.environment.ExpressionEnvironment;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

/**
 * Bridge between the compilation pipeline and the public expression API.
 *
 * <p>Each instance holds the compiled plan and evaluators for a single expression. Call
 * {@code computeMath} / {@code computeLogical} / {@code computeAssignments} with a
 * {@code Map<String, Object>} of variable values to evaluate. The same instance may safely be
 * called concurrently from multiple threads because there is no mutable per-instance state; each
 * call receives its own {@link ExecutionScope}.
 *
 * <h2>Thread safety</h2>
 * <p>All static factory methods and all instance {@code compute*} methods are thread-safe.
 * Compiled instances hold no mutable state: variable values are supplied per call via a
 * {@code Map<String, Object>} and each call builds its own {@link ExecutionScope}.
 */
public final class ExpressionRuntimeSupport {

    private final CompiledExpression compiledExpression;
    /**
     * Immutable snapshot of default values seeded from the {@link ExternalSymbolCatalog} at
     * compile time. Shared across all {@code compute*} calls; never mutated after construction.
     */
    private final Object[] defaultValues;
    /**
     * Precomputed binding metadata for external symbols referenced by this expression.
     */
    private final Map<String, ExternalBindingPlan> externalBindingsByName;
    private final Set<String> internalSymbolNames;
    private final RuntimeServices runtimeServices;
    private final Evaluator<BigDecimal> mathEvaluator;
    private final Evaluator<Boolean> logicalEvaluator;
    private final boolean hasAssignments;
    private final int internalSymbolCount;
    private final int externalSymbolCount;
    private final int maxAuditEvents;
    private final List<AuditEvent> foldedVariableReads;

    private ExpressionRuntimeSupport(CompiledExpression compiledExpression,
                                     Object[] defaultValues,
                                     Map<String, ExternalBindingPlan> externalBindingsByName,
                                     Set<String> internalSymbolNames,
                                     RuntimeServices runtimeServices,
                                     MathContext mathContext) {
        this.compiledExpression = Objects.requireNonNull(compiledExpression, "compiledExpression must not be null");
        this.defaultValues = Objects.requireNonNull(defaultValues, "defaultValues must not be null");
        this.externalBindingsByName = Objects.requireNonNull(externalBindingsByName, "externalBindingsByName must not be null");
        this.internalSymbolNames = Objects.requireNonNull(internalSymbolNames, "internalSymbolNames must not be null");
        this.runtimeServices = Objects.requireNonNull(runtimeServices, "runtimeServices must not be null");
        Objects.requireNonNull(mathContext, "mathContext must not be null");
        this.mathEvaluator = new MathEvaluator(compiledExpression, runtimeServices, mathContext);
        this.logicalEvaluator = new LogicalEvaluator(compiledExpression, runtimeServices, mathContext);
        this.hasAssignments = !compiledExpression.executionPlan().assignments().isEmpty();
        this.internalSymbolCount = compiledExpression.semanticModel().internalSymbolsByName().size();
        this.externalSymbolCount = compiledExpression.executionPlan().externalSymbolsCount();
        this.maxAuditEvents = compiledExpression.executionPlan().maxAuditEvents();
        this.foldedVariableReads = compiledExpression.executionPlan().foldedVariableReads();
    }

    public static ExpressionRuntimeSupport from(CompiledExpression compiledExpression, ExpressionEnvironment environment) {
        Objects.requireNonNull(environment, "environment must not be null");
        RuntimeServices runtimeServices = environment.runtimeServices();
        SemanticModel semanticModel = compiledExpression.semanticModel();
        Object[] defaults = compiledExpression.executionPlan().defaults();
        Map<String, ExternalBindingPlan> bindings = compiledExpression.executionPlan().externalBindings();
        return new ExpressionRuntimeSupport(compiledExpression, defaults, bindings, semanticModel.internalSymbolsByName().keySet(), runtimeServices,
                environment.mathContext());
    }


    /**
     * Builds the per-call overrides array, leaving immutable defaults shared across evaluations.
     */
    private Object[] buildOverrides(Map<String, Object> userValues) {
        if (userValues == null || userValues.isEmpty()) {
            return null;
        }
        Object[] result = new Object[externalSymbolCount];
        Arrays.fill(result, ExecutionScope.UNBOUND);
        for (Map.Entry<String, Object> entry : userValues.entrySet()) {
            String name = entry.getKey();
            ExternalBindingPlan binding = lookupExternalBinding(name);
            if (!binding.overridable()) {
                throw new IllegalStateException("symbol '" + name + "' is not overridable");
            }
            result[binding.index()] = runtimeServices.coerceToResolvedType(
                    entry.getValue(),
                    binding.declaredType());
        }
        return result;
    }

    private ExternalBindingPlan lookupExternalBinding(String name) {
        ExternalBindingPlan binding = externalBindingsByName.get(name);
        if (binding != null) {
            return binding;
        }
        if (internalSymbolNames.contains(name)) {
            throw new IllegalArgumentException("symbol '" + name + "' is internal to the expression");
        }
        throw new IllegalArgumentException("unknown external symbol '" + name + "'");
    }

    private ExecutionScope createExecutionScope(Map<String, Object> userValues) {
        if (userValues == null || userValues.isEmpty()) {
            if (hasAssignments) {
                return ExecutionScope.from(defaultValues, internalSymbolCount);
            }
            return ExecutionScope.readOnly(defaultValues);
        }
        Object[] overrides = buildOverrides(userValues);
        if (hasAssignments) {
            if (defaultValues.length == 0) {
                return ExecutionScope.from(overrides, internalSymbolCount);
            }
            return ExecutionScope.from(overrides, defaultValues, internalSymbolCount);
        }
        if (defaultValues.length == 0) {
            return ExecutionScope.readOnly(overrides);
        }
        return ExecutionScope.readOnly(overrides, defaultValues);
    }

    private ExecutionScope createAuditedExecutionScope(Map<String, Object> userValues, AuditCollector collector) {
        if (userValues == null || userValues.isEmpty()) {
            if (hasAssignments) {
                return ExecutionScope.fromWithAudit(defaultValues, internalSymbolCount, collector);
            }
            return ExecutionScope.readOnlyWithAudit(defaultValues, collector);
        }
        Object[] overrides = buildOverrides(userValues);
        if (hasAssignments) {
            if (defaultValues.length == 0) {
                return ExecutionScope.fromWithAudit(overrides, internalSymbolCount, collector);
            }
            return ExecutionScope.fromWithAudit(overrides, defaultValues, internalSymbolCount, collector);
        }
        if (defaultValues.length == 0) {
            return ExecutionScope.readOnlyWithAudit(overrides, collector);
        }
        return ExecutionScope.readOnlyWithAudit(overrides, defaultValues, collector);
    }

    // -------------------------------------------------------------------------
    // Evaluation
    // -------------------------------------------------------------------------

    public BigDecimal computeMath(Map<String, Object> values) {
        return mathEvaluator.evaluate(createExecutionScope(values));
    }

    public boolean computeLogical(Map<String, Object> values) {
        return logicalEvaluator.evaluate(createExecutionScope(values));
    }

    public AuditResult<BigDecimal> computeMathWithAudit(Map<String, Object> values) {
        AuditCollector collector = new AuditCollector(maxAuditEvents, foldedVariableReads);
        BigDecimal result = mathEvaluator.evaluate(createAuditedExecutionScope(values, collector));
        return new AuditResult<>(result, collector.buildTrace());
    }

    public AuditResult<Boolean> computeLogicalWithAudit(Map<String, Object> values) {
        AuditCollector collector = new AuditCollector(maxAuditEvents, foldedVariableReads);
        boolean result = logicalEvaluator.evaluate(createAuditedExecutionScope(values, collector));
        return new AuditResult<>(result, collector.buildTrace());
    }

    public Map<String, Object> computeAssignments(Map<String, Object> values) {
        return mathEvaluator.evaluateAssignments(createExecutionScope(values));
    }

    public AuditResult<Map<String, Object>> computeAssignmentsWithAudit(Map<String, Object> values) {
        AuditCollector collector = new AuditCollector(maxAuditEvents, foldedVariableReads);
        Map<String, Object> result = mathEvaluator.evaluateAssignments(createAuditedExecutionScope(values, collector));
        return new AuditResult<>(result, collector.buildTrace());
    }

    public CompiledExpression getCompiledExpression() {
        return compiledExpression;
    }

}
