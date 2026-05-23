package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.AuditEvent;
import com.runestone.expeval.api.CompilationPosition;
import com.runestone.expeval.api.ExpressionEvaluationException;
import com.runestone.expeval.internal.LanguageSymbols;
import com.runestone.expeval.internal.ast.BinaryOperator;
import com.runestone.expeval.internal.ast.SourceSpan;

import java.math.MathContext;
import java.time.LocalDate;
import java.util.*;

/**
 * Expression evaluator that carries all sub-expression results as {@code Object} values
 * ({@link java.math.BigDecimal}, {@link Boolean}, {@link LocalDate}, etc.) with no intermediate boxing.
 *
 * <h2>Scope contract</h2>
 * <ul>
 *   <li><strong>Scope read</strong> — variables are looked up via
 *       {@link ExecutionScope#find}, which avoids both {@link java.util.Optional} and wrapper
 *       allocation.</li>
 *   <li><strong>Scope write (assignments)</strong> — the result is stored directly via
 *       {@link ExecutionScope#assign}.  The scope handles null-value sentinels internally.</li>
 *   <li><strong>Function arguments</strong> — coerced via
 *       {@link RuntimeServices#coerce}, which fast-paths same-type arguments.</li>
 * </ul>
 */
abstract class AbstractObjectEvaluator<T> implements Evaluator<T> {

    private final CompiledExpression compiledExpression;
    private final RuntimeServices runtimeServices;
    private final MathContext mathContext;
    private final String source;
    private final NodeEvaluator nodeEvaluator;
    private final AssignmentEvaluator assignmentEvaluator;
    private final FunctionCallEvaluator functionCallEvaluator;

    protected AbstractObjectEvaluator(CompiledExpression compiledExpression,
                                      RuntimeServices runtimeServices,
                                      MathContext mathContext) {
        this.compiledExpression = Objects.requireNonNull(compiledExpression, "compiledExpression must not be null");
        this.runtimeServices = Objects.requireNonNull(runtimeServices, "runtimeServices must not be null");
        this.mathContext = Objects.requireNonNull(mathContext, "mathContext must not be null");
        this.source = compiledExpression.source();
        this.nodeEvaluator = this::evaluateExpr;
        this.assignmentEvaluator = new AssignmentEvaluator(nodeEvaluator);
        this.functionCallEvaluator = new FunctionCallEvaluator(runtimeServices, nodeEvaluator);
    }

    @Override
    public final T evaluate(ExecutionScope scope) {
        Objects.requireNonNull(scope, "scope must not be null");
        ExecutionPlan plan = compiledExpression.executionPlan();
        assignmentEvaluator.execute(plan.assignments(), scope);
        return convertResult(evaluateExpr(plan.resultExpression(), scope));
    }

    /**
     * Converts the final result to the evaluator's declared return type.
     */
    protected abstract T convertResult(Object value);

    @Override
    public final Map<String, Object> evaluateAssignments(ExecutionScope scope) {
        Objects.requireNonNull(scope, "scope must not be null");
        ExecutionPlan plan = compiledExpression.executionPlan();
        return assignmentEvaluator.evaluateAssignments(plan.assignments(), scope);
    }

    // -------------------------------------------------------------------------
    // Expression evaluation — returns Object, no boxing
    // -------------------------------------------------------------------------

    private Object evaluateExpr(ExecutableNode node, ExecutionScope scope) {
        return switch (node) {
            case ExecutableLiteral lit -> lit.precomputed();
            case ExecutableDynamicLiteral dyn -> {
                Object value = scope.resolveDynamic(dyn.kind());
                AuditCollector audit = scope.audit();
                if (audit != null) {
                    audit.record(new AuditEvent.VariableRead(dyn.kind().canonicalName(), true, value));
                }
                yield value;
            }
            case ExecutableIdentifier id -> {
                if (LanguageSymbols.CURRENT_ELEMENT.equals(id.ref().name())) {
                    var ctx = FilterContextStack.INSTANCE.get().peek();
                    if (ctx == null) {
                        throw new ExpressionEvaluationException(source,
                                "INVALID_CURRENT_ELEMENT",
                                "'@' used outside of a filter predicate context", null);
                    }
                    yield ctx.isMapContext() ? ctx.mapValue() : ctx.element();
                }
                Object value = scope.find(id.ref());
                if (value == ExecutionScope.UNBOUND) {
                    throw unboundVariableException(id);
                }
                AuditCollector audit = scope.audit();
                if (audit != null) {
                    audit.record(new AuditEvent.VariableRead(id.ref().name(), false, value));
                }
                yield value;
            }
            case ExecutablePropertyChain chain ->
                    PropertyChainOps.evaluatePropertyChain(chain, scope, source, runtimeServices, mathContext, nodeEvaluator);
            case ExecutableFunctionCall f -> functionCallEvaluator.evaluate(f, scope);
            case ExecutableConditional c -> evaluateConditional(c, scope);
            case ExecutableSimpleConditional sc -> evaluateSimpleConditional(sc, scope);
            case ExecutableUnaryOp u -> evaluateUnary(u, scope);
            case ExecutableBinaryOp b -> evaluateBinary(b, scope);
            case ExecutableTernaryOp t -> evaluateTernary(t, scope);
            case ExecutablePostfixOp p -> evaluatePostfix(p, scope);
            case ExecutableVectorLiteral v -> evaluateVector(v, scope);
            case ExecutableNullCoalesce nc -> {
                Object leftVal = evaluateExpr(nc.left(), scope);
                yield leftVal != null ? leftVal : evaluateExpr(nc.right(), scope);
            }
            case ExecutableRegexOp r -> evaluateRegex(r, scope);
        };
    }

    // -------------------------------------------------------------------------
    // Node-specific evaluators
    // -------------------------------------------------------------------------

    private Object evaluateConditional(ExecutableConditional node, ExecutionScope scope) {
        List<ExecutableNode> conditions = node.conditions();
        for (int index = 0; index < conditions.size(); index++) {
            if (asBoolean(evaluateExpr(conditions.get(index), scope))) {
                return evaluateExpr(node.results().get(index), scope);
            }
        }
        return evaluateExpr(node.elseExpression(), scope);
    }

    private Object evaluateSimpleConditional(ExecutableSimpleConditional node, ExecutionScope scope) {
        if (asBoolean(evaluateExpr(node.condition(), scope))) {
            return evaluateExpr(node.thenExpression(), scope);
        }
        return evaluateExpr(node.elseExpression(), scope);
    }

    private Object evaluateUnary(ExecutableUnaryOp node, ExecutionScope scope) {
        Object operand = evaluateExpr(node.operand(), scope);
        return OperatorEvaluator.evaluateUnary(node.operator(), operand, runtimeServices, mathContext);
    }

    private Object evaluateBinary(ExecutableBinaryOp node, ExecutionScope scope) {
        Object left = evaluateExpr(node.left(), scope);
        BinaryOperator operator = node.operator();
        // Short-circuit evaluation for logical operators
        if (operator == BinaryOperator.AND || operator == BinaryOperator.NAND) {
            boolean leftBool = asBoolean(left);
            if (!leftBool) return operator == BinaryOperator.NAND;
        } else if (operator == BinaryOperator.OR || operator == BinaryOperator.NOR) {
            boolean leftBool = asBoolean(left);
            if (leftBool) return operator == BinaryOperator.OR;
        }
        Object right = evaluateExpr(node.right(), scope);
        return OperatorEvaluator.evaluateBinary(operator, left, right, runtimeServices, mathContext);
    }

    private Object evaluateTernary(ExecutableTernaryOp node, ExecutionScope scope) {
        Object value = evaluateExpr(node.first(), scope);
        Object lower = evaluateExpr(node.second(), scope);
        Object upper = evaluateExpr(node.third(), scope);
        return OperatorEvaluator.evaluateTernary(node.operator(), value, lower, upper, runtimeServices);
    }

    private Object evaluateRegex(ExecutableRegexOp node, ExecutionScope scope) {
        String subject = asString(evaluateExpr(node.subject(), scope));
        boolean matches = node.pattern().matcher(subject).find();
        return node.negate() != matches;
    }

    private Object evaluatePostfix(ExecutablePostfixOp node, ExecutionScope scope) {
        Object operand = evaluateExpr(node.operand(), scope);
        return OperatorEvaluator.evaluatePostfix(node.operator(), operand, runtimeServices, mathContext);
    }

    private List<Object> evaluateVector(ExecutableVectorLiteral node, ExecutionScope scope) {
        if (node.isFolded()) {
            return node.foldedValue();
        }
        List<Object> elements = new ArrayList<>(node.elements().size());
        for (ExecutableNode element : node.elements()) {
            elements.add(evaluateExpr(element, scope));
        }
        return elements;
    }

    // -------------------------------------------------------------------------
    // Type helpers — fast-path instanceof casts, fallback via RuntimeServices
    // -------------------------------------------------------------------------

    private boolean asBoolean(Object value) {
        if (value instanceof Boolean b) return b;
        try {
            return runtimeServices.asBoolean(value);
        } catch (IllegalStateException e) {
            throw new ExpressionEvaluationException(source, "NULL_VALUE",
                    "cannot use null value as a boolean", null);
        }
    }

    private String asString(Object value) {
        if (value instanceof String s) return s;
        try {
            return runtimeServices.asString(value);
        } catch (IllegalStateException e) {
            throw new ExpressionEvaluationException(source, "NULL_VALUE",
                    "cannot use null value as a string", null);
        }
    }

    // -------------------------------------------------------------------------
    // Error helpers
    // -------------------------------------------------------------------------

    private ExpressionEvaluationException unboundVariableException(ExecutableIdentifier id) {
        SourceSpan span = id.sourceSpan();
        CompilationPosition position = new CompilationPosition(span.startLine(), span.startColumn(), span.endColumn());
        String message = "variable '" + id.ref().name() + "' has no value; call setValue(\""
                         + id.ref().name() + "\", ...) before compute()";
        return new ExpressionEvaluationException(source, "UNBOUND_VARIABLE", message, position);
    }
}
