package com.runestone.expeval2.internal.runtime;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.runestone.expeval2.api.AuditEvent;
import com.runestone.expeval2.api.CompilationPosition;
import com.runestone.expeval2.api.ExpressionEvaluationException;
import com.runestone.expeval2.catalog.FunctionDescriptor;
import com.runestone.expeval2.internal.ast.BinaryOperator;
import com.runestone.expeval2.internal.ast.SourceSpan;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Expression evaluator that carries all sub-expression results as {@code Object} values
 * ({@link BigDecimal}, {@link Boolean}, {@link LocalDate}, etc.) with no intermediate boxing.
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

    protected AbstractObjectEvaluator(CompiledExpression compiledExpression,
                                         RuntimeServices runtimeServices,
                                         MathContext mathContext) {
        this.compiledExpression = Objects.requireNonNull(compiledExpression, "compiledExpression must not be null");
        this.runtimeServices = Objects.requireNonNull(runtimeServices, "runtimeServices must not be null");
        this.mathContext = Objects.requireNonNull(mathContext, "mathContext must not be null");
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
            case ExecutableFunctionCall f  -> evaluateFunctionCall(f, scope);
            case ExecutableConditional c   -> evaluateConditional(c, scope);
            case ExecutableSimpleConditional sc -> evaluateSimpleConditional(sc, scope);
            case ExecutableUnaryOp u       -> evaluateUnary(u, scope);
            case ExecutableBinaryOp b      -> evaluateBinary(b, scope);
            case ExecutablePostfixOp p     -> evaluatePostfix(p, scope);
            case ExecutableVectorLiteral v -> evaluateVector(v, scope);
        };
    }

    // -------------------------------------------------------------------------
    // Node-specific evaluators
    // -------------------------------------------------------------------------

    private Object evaluateFunctionCall(ExecutableFunctionCall node, ExecutionScope scope) {
        if (node.isFolded()) {
            Object result = runtimeServices.coerceToResolvedType(
                    node.foldedResult(), node.binding().returnType());
            AuditCollector audit = scope.audit();
            if (audit != null) {
                audit.enterCall();
                audit.exitCall();
                audit.record(new AuditEvent.FunctionCall(
                        node.binding().descriptor().name(),
                        node.foldedArgs(),
                        result,
                        audit.callDepth()
                ));
            }
            return result;
        }

        FunctionDescriptor descriptor = node.binding().descriptor();
        int arity = descriptor.arity();
        AuditCollector audit = scope.audit();

        if (audit == null) {
            List<ExecutableNode> argsNodes = node.arguments();
            List<Class<?>> paramTypes = descriptor.parameterTypes();
            return switch (arity) {
                case 0 -> runtimeServices.coerceToResolvedType(descriptor.invoke(), node.binding().returnType());
                case 1 -> {
                    Object a1 = evaluateExpr(argsNodes.get(0), scope);
                    a1 = runtimeServices.coerce(a1, paramTypes.get(0));
                    yield runtimeServices.coerceToResolvedType(descriptor.invoke(a1), node.binding().returnType());
                }
                case 2 -> {
                    Object a1 = evaluateExpr(argsNodes.get(0), scope);
                    a1 = runtimeServices.coerce(a1, paramTypes.get(0));
                    Object a2 = evaluateExpr(argsNodes.get(1), scope);
                    a2 = runtimeServices.coerce(a2, paramTypes.get(1));
                    yield runtimeServices.coerceToResolvedType(descriptor.invoke(a1, a2), node.binding().returnType());
                }
                case 3 -> {
                    Object a1 = evaluateExpr(argsNodes.get(0), scope);
                    a1 = runtimeServices.coerce(a1, paramTypes.get(0));
                    Object a2 = evaluateExpr(argsNodes.get(1), scope);
                    a2 = runtimeServices.coerce(a2, paramTypes.get(1));
                    Object a3 = evaluateExpr(argsNodes.get(2), scope);
                    a3 = runtimeServices.coerce(a3, paramTypes.get(2));
                    yield runtimeServices.coerceToResolvedType(descriptor.invoke(a1, a2, a3), node.binding().returnType());
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
                    yield runtimeServices.coerceToResolvedType(descriptor.invoke(a1, a2, a3, a4), node.binding().returnType());
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
                    yield runtimeServices.coerceToResolvedType(descriptor.invoke(a1, a2, a3, a4, a5), node.binding().returnType());
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
                    yield runtimeServices.coerceToResolvedType(descriptor.invoke(a1, a2, a3, a4, a5, a6), node.binding().returnType());
                }
                default -> {
                    Object[] args = new Object[arity];
                    for (int i = 0; i < arity; i++) {
                        Object evaluated = evaluateExpr(argsNodes.get(i), scope);
                        args[i] = runtimeServices.coerce(evaluated, paramTypes.get(i));
                    }
                    yield runtimeServices.coerceToResolvedType(descriptor.invoke(args), node.binding().returnType());
                }
            };
        }

        Object[] args = new Object[arity];
        List<ExecutableNode> argsNodes = node.arguments();
        List<Class<?>> paramTypes = descriptor.parameterTypes();
        for (int i = 0; i < arity; i++) {
            Object evaluated = evaluateExpr(argsNodes.get(i), scope);
            args[i] = runtimeServices.coerce(evaluated, paramTypes.get(i));
        }
        if (audit != null) audit.enterCall();
        Object result = runtimeServices.coerceToResolvedType(
                descriptor.invoke(args), node.binding().returnType());
        if (audit != null) {
            audit.exitCall();
            audit.record(new AuditEvent.FunctionCall(descriptor.name(), args, result, audit.callDepth()));
        }
        return result;
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
        return switch (node.operator()) {
            case NEGATE      -> asBigDecimal(operand).negate();
            case LOGICAL_NOT -> !asBoolean(operand);
            case SQRT        -> asBigDecimal(operand).sqrt(mathContext);
            case MODULUS     -> asBigDecimal(operand).abs();
        };
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
        return switch (operator) {
            case ADD      -> asBigDecimal(left).add(asBigDecimal(right));
            case SUBTRACT -> asBigDecimal(left).subtract(asBigDecimal(right));
            case MULTIPLY -> asBigDecimal(left).multiply(asBigDecimal(right));
            case DIVIDE   -> asBigDecimal(left).divide(asBigDecimal(right), mathContext);
            case MODULO   -> asBigDecimal(left).remainder(asBigDecimal(right));
            case POWER    -> pow(asBigDecimal(left), asBigDecimal(right));
            case ROOT     -> BigDecimalMath.root(asBigDecimal(left), asBigDecimal(right), mathContext);
            case AND      -> asBoolean(right);
            case OR       -> asBoolean(right);
            case XOR      -> asBoolean(left) ^ asBoolean(right);
            case XNOR     -> !(asBoolean(left) ^ asBoolean(right));
            case NAND     -> !asBoolean(right);
            case NOR      -> !asBoolean(right);
            case GREATER_THAN          -> compare(left, right) > 0;
            case GREATER_THAN_OR_EQUAL -> compare(left, right) >= 0;
            case LESS_THAN             -> compare(left, right) < 0;
            case LESS_THAN_OR_EQUAL    -> compare(left, right) <= 0;
            case EQUAL                 -> compareEquality(left, right);
            case NOT_EQUAL             -> !compareEquality(left, right);
        };
    }

    private Object evaluatePostfix(ExecutablePostfixOp node, ExecutionScope scope) {
        BigDecimal value = asBigDecimal(evaluateExpr(node.operand(), scope));
        return switch (node.operator()) {
            case PERCENT   -> value.movePointRight(2);
            case FACTORIAL -> BigDecimalMath.factorial(value, mathContext);
        };
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
    // Comparison helpers
    // -------------------------------------------------------------------------

    private int compare(Object left, Object right) {
        if (left instanceof BigDecimal || right instanceof BigDecimal) {
            return asBigDecimal(left).compareTo(asBigDecimal(right));
        }
        if (left instanceof LocalDate || right instanceof LocalDate) {
            return asLocalDate(left).compareTo(asLocalDate(right));
        }
        if (left instanceof LocalTime || right instanceof LocalTime) {
            return asLocalTime(left).compareTo(asLocalTime(right));
        }
        if (left instanceof LocalDateTime || right instanceof LocalDateTime) {
            return asLocalDateTime(left).compareTo(asLocalDateTime(right));
        }
        if (left instanceof String || right instanceof String) {
            return asString(left).compareTo(asString(right));
        }
        if (left instanceof Boolean || right instanceof Boolean) {
            return Boolean.compare(asBoolean(left), asBoolean(right));
        }
        throw new IllegalStateException("unsupported comparison between values");
    }

    private boolean compareEquality(Object left, Object right) {
        if (left == null || right == null) {
            return left == null && right == null;
        }
        if (left instanceof BigDecimal || right instanceof BigDecimal) {
            return asBigDecimal(left).compareTo(asBigDecimal(right)) == 0;
        }
        if (left instanceof List<?> leftList && right instanceof List<?> rightList) {
            if (leftList.size() != rightList.size()) return false;
            for (int i = 0; i < leftList.size(); i++) {
                if (!compareEquality(leftList.get(i), rightList.get(i))) return false;
            }
            return true;
        }
        return Objects.equals(left, right);
    }

    // -------------------------------------------------------------------------
    // Type helpers — fast-path instanceof casts, fallback via RuntimeServices
    // -------------------------------------------------------------------------

    private BigDecimal asBigDecimal(Object value) {
        if (value instanceof BigDecimal bd) return bd;
        return runtimeServices.asNumber(value);
    }

    private boolean asBoolean(Object value) {
        if (value instanceof Boolean b) return b;
        return runtimeServices.asBoolean(value);
    }

    private String asString(Object value) {
        if (value instanceof String s) return s;
        return runtimeServices.asString(value);
    }

    private LocalDate asLocalDate(Object value) {
        if (value instanceof LocalDate d) return d;
        return runtimeServices.asDate(value);
    }

    private LocalTime asLocalTime(Object value) {
        if (value instanceof LocalTime t) return t;
        return runtimeServices.asTime(value);
    }

    private LocalDateTime asLocalDateTime(Object value) {
        if (value instanceof LocalDateTime dt) return dt;
        return runtimeServices.asDateTime(value);
    }

    // -------------------------------------------------------------------------
    // Math helpers
    // -------------------------------------------------------------------------

    private BigDecimal pow(BigDecimal base, BigDecimal exponent) {
        BigDecimal normalized = exponent.stripTrailingZeros();
        if (normalized.scale() <= 0 && normalized.precision() <= 9) {
            return base.pow(normalized.intValue(), mathContext);
        }
        return BigDecimalMath.pow(base, exponent, mathContext);
    }

    // -------------------------------------------------------------------------
    // Error helpers
    // -------------------------------------------------------------------------

    private ExpressionEvaluationException unboundVariableException(ExecutableIdentifier id) {
        SourceSpan span = id.sourceSpan();
        CompilationPosition position = new CompilationPosition(span.startLine(), span.startColumn(), span.endColumn());
        String message = "variable '" + id.ref().name() + "' has no value; call setValue(\""
            + id.ref().name() + "\", ...) before compute()";
        return new ExpressionEvaluationException(compiledExpression.source(), "UNBOUND_VARIABLE", message, position);
    }
}
