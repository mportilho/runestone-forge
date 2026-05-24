package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.AuditEvent;
import com.runestone.expeval.api.CompilationPosition;
import com.runestone.expeval.api.ExpressionEvaluationException;
import com.runestone.expeval.catalog.FunctionDescriptor;
import com.runestone.expeval.internal.ast.BinaryOperator;
import com.runestone.expeval.internal.ast.SourceSpan;
import com.runestone.expeval.internal.semantic.SymbolRef;

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

    /** Sentinel name used for {@code @} (current element) in filter predicates. */
    private static final String CURRENT_ELEMENT_REF = "@";

    private final CompiledExpression compiledExpression;
    private final RuntimeServices runtimeServices;
    private final MathContext mathContext;
    private final String source;
    private final NodeEvaluator nodeEvaluator;

    protected AbstractObjectEvaluator(CompiledExpression compiledExpression,
                                      RuntimeServices runtimeServices,
                                      MathContext mathContext) {
        this.compiledExpression = Objects.requireNonNull(compiledExpression, "compiledExpression must not be null");
        this.runtimeServices = Objects.requireNonNull(runtimeServices, "runtimeServices must not be null");
        this.mathContext = Objects.requireNonNull(mathContext, "mathContext must not be null");
        this.source = compiledExpression.source();
        this.nodeEvaluator = this::evaluateExpr;
    }

    @Override
    public final T evaluate(ExecutionScope scope) {
        Objects.requireNonNull(scope, "scope must not be null");
        ExecutionPlan plan = compiledExpression.executionPlan();
        for (ExecutableAssignment assignment : plan.assignments()) {
            executeAssignment(assignment, scope);
        }
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
        for (ExecutableAssignment assignment : plan.assignments()) {
            executeAssignment(assignment, scope);
        }
        List<ExecutableAssignment> assignments = plan.assignments();
        Map<String, Object> result = new LinkedHashMap<>(assignments.size());
        for (ExecutableAssignment assignment : assignments) {
            switch (assignment) {
                case ExecutableSimpleAssignment s -> {
                    Object value = scope.find(s.target());
                    result.put(s.target().name(), value == ExecutionScope.UNBOUND ? null : value);
                }
                case ExecutableDestructuringAssignment d -> {
                    for (SymbolRef target : d.targets()) {
                        Object value = scope.find(target);
                        result.put(target.name(), value == ExecutionScope.UNBOUND ? null : value);
                    }
                }
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Assignment execution
    // -------------------------------------------------------------------------

    private void executeAssignment(ExecutableAssignment assignment, ExecutionScope scope) {
        switch (assignment) {
            case ExecutableSimpleAssignment s -> {
                Object value = evaluateExpr(s.value(), scope);
                scope.assign(s.target(), value);
                AuditCollector audit = scope.audit();
                if (audit != null) {
                    audit.record(new AuditEvent.AssignmentEvent(s.target().name(), value));
                }
            }
            case ExecutableDestructuringAssignment d -> {
                @SuppressWarnings("unchecked")
                List<Object> elements = (List<Object>) evaluateExpr(d.value(), scope);
                AuditCollector audit = scope.audit();
                List<SymbolRef> targets = d.targets();
                for (int index = 0; index < targets.size(); index++) {
                    SymbolRef target = targets.get(index);
                    Object element = index < elements.size() ? elements.get(index) : null;
                    scope.assign(target, element);
                    if (audit != null) {
                        audit.record(new AuditEvent.AssignmentEvent(target.name(), element));
                    }
                }
            }
        }
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
                if (CURRENT_ELEMENT_REF.equals(id.ref().name())) {
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
            case ExecutableFunctionCall f -> evaluateFunctionCall(f, scope);
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

    private Object evaluateFunctionCall(ExecutableFunctionCall node, ExecutionScope scope) {
        if (node.isFolded()) {
            Object result = runtimeServices.coerceToResolvedType(node.foldedResult(), node.binding().returnType());
            AuditCollector audit = scope.audit();
            if (audit != null) {
                audit.record(new AuditEvent.FunctionCall(
                        node.binding().descriptor().name(),
                        node.foldedArgs(),
                        result
                ));
            }
            return result;
        }

        FunctionDescriptor descriptor = node.binding().descriptor();
        int arity = descriptor.arity();
        AuditCollector audit = scope.audit();

        List<ExecutableNode> argsNodes = node.arguments();
        List<Class<?>> paramTypes = descriptor.parameterTypes();
        return switch (arity) {
            case 0 -> runtimeServices.coerceToResolvedType(descriptor.invoke(), node.binding().returnType());
            case 1 -> {
                Object a1 = evaluateExpr(argsNodes.getFirst(), scope);
                a1 = runtimeServices.coerce(a1, paramTypes.getFirst());
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(a1), node.binding().returnType());
                if (audit != null) auditFunctionCall(audit, descriptor, result, a1);
                yield result;
            }
            case 2 -> {
                Object a1 = evaluateExpr(argsNodes.get(0), scope);
                a1 = runtimeServices.coerce(a1, paramTypes.get(0));
                Object a2 = evaluateExpr(argsNodes.get(1), scope);
                a2 = runtimeServices.coerce(a2, paramTypes.get(1));
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(a1, a2), node.binding().returnType());
                if (audit != null) auditFunctionCall(audit, descriptor, result, a1, a2);
                yield result;
            }
            case 3 -> {
                Object a1 = evaluateExpr(argsNodes.get(0), scope);
                a1 = runtimeServices.coerce(a1, paramTypes.get(0));
                Object a2 = evaluateExpr(argsNodes.get(1), scope);
                a2 = runtimeServices.coerce(a2, paramTypes.get(1));
                Object a3 = evaluateExpr(argsNodes.get(2), scope);
                a3 = runtimeServices.coerce(a3, paramTypes.get(2));
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(a1, a2, a3), node.binding().returnType());
                if (audit != null) auditFunctionCall(audit, descriptor, result, a1, a2, a3);
                yield result;
            }
            case 4 -> {
                Object a1 = evaluateExpr(argsNodes.get(0), scope);
                a1 = runtimeServices.coerce(a1, paramTypes.get(0));
                Object a2 = evaluateExpr(argsNodes.get(1), scope);
                a2 = runtimeServices.coerce(a2, paramTypes.get(1));
                Object a3 = evaluateExpr(argsNodes.get(2), scope);
                a3 = runtimeServices.coerce(a3, paramTypes.get(2));
                Object a4 = evaluateExpr(argsNodes.get(3), scope);
                a4 = runtimeServices.coerce(a4, paramTypes.get(3));
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(a1, a2, a3, a4), node.binding().returnType());
                if (audit != null) auditFunctionCall(audit, descriptor, result, a1, a2, a3, a4);
                yield result;
            }
            case 5 -> {
                Object a1 = evaluateExpr(argsNodes.get(0), scope);
                a1 = runtimeServices.coerce(a1, paramTypes.get(0));
                Object a2 = evaluateExpr(argsNodes.get(1), scope);
                a2 = runtimeServices.coerce(a2, paramTypes.get(1));
                Object a3 = evaluateExpr(argsNodes.get(2), scope);
                a3 = runtimeServices.coerce(a3, paramTypes.get(2));
                Object a4 = evaluateExpr(argsNodes.get(3), scope);
                a4 = runtimeServices.coerce(a4, paramTypes.get(3));
                Object a5 = evaluateExpr(argsNodes.get(4), scope);
                a5 = runtimeServices.coerce(a5, paramTypes.get(4));
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(a1, a2, a3, a4, a5), node.binding().returnType());
                if (audit != null) auditFunctionCall(audit, descriptor, result, a1, a2, a3, a4, a5);
                yield result;
            }
            case 6 -> {
                Object a1 = evaluateExpr(argsNodes.get(0), scope);
                a1 = runtimeServices.coerce(a1, paramTypes.get(0));
                Object a2 = evaluateExpr(argsNodes.get(1), scope);
                a2 = runtimeServices.coerce(a2, paramTypes.get(1));
                Object a3 = evaluateExpr(argsNodes.get(2), scope);
                a3 = runtimeServices.coerce(a3, paramTypes.get(2));
                Object a4 = evaluateExpr(argsNodes.get(3), scope);
                a4 = runtimeServices.coerce(a4, paramTypes.get(3));
                Object a5 = evaluateExpr(argsNodes.get(4), scope);
                a5 = runtimeServices.coerce(a5, paramTypes.get(4));
                Object a6 = evaluateExpr(argsNodes.get(5), scope);
                a6 = runtimeServices.coerce(a6, paramTypes.get(5));
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(a1, a2, a3, a4, a5, a6), node.binding().returnType());
                if (audit != null) auditFunctionCall(audit, descriptor, result, a1, a2, a3, a4, a5, a6);
                yield result;
            }
            default -> {
                Object[] args = new Object[arity];
                for (int i = 0; i < arity; i++) {
                    Object evaluated = evaluateExpr(argsNodes.get(i), scope);
                    args[i] = runtimeServices.coerce(evaluated, paramTypes.get(i));
                }
                Object result = runtimeServices.coerceToResolvedType(descriptor.invoke(args), node.binding().returnType());
                if (audit != null) auditFunctionCall(audit, descriptor, result, args);
                yield result;
            }
        };
    }

    private void auditFunctionCall(AuditCollector audit, FunctionDescriptor descriptor, Object result, Object... args) {
        audit.record(new AuditEvent.FunctionCall(descriptor.name(), args, result));
    }

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
