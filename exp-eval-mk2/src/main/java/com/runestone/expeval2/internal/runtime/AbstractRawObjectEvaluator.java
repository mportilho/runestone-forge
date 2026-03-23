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
 * Expression evaluator that carries all sub-expression results as raw {@code Object} values
 * ({@link BigDecimal}, {@link Boolean}, {@link LocalDate}, etc.) with no intermediate boxing.
 *
 * <h2>Scope contract</h2>
 * <ul>
 *   <li><strong>Scope read</strong> — variables are looked up via
 *       {@link ExecutionScope#findRaw}, which avoids both {@link java.util.Optional} and wrapper
 *       allocation.</li>
 *   <li><strong>Scope write (assignments)</strong> — the raw result is stored directly via
 *       {@link ExecutionScope#assign}.  The scope handles null-value sentinels internally.</li>
 *   <li><strong>Function arguments</strong> — coerced via
 *       {@link RuntimeServices#coerceRaw}, which fast-paths same-type arguments.</li>
 * </ul>
 */
abstract class AbstractRawObjectEvaluator<T> implements Evaluator<T> {

    private final CompiledExpression compiledExpression;
    private final RuntimeServices runtimeServices;
    private final MathContext mathContext;

    protected AbstractRawObjectEvaluator(CompiledExpression compiledExpression,
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
     * Converts the final raw result to the evaluator's declared return type.
     */
    protected abstract T convertResult(Object rawValue);

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
                    Object raw = scope.findRaw(s.target());
                    result.put(s.target().name(), raw == ExecutionScope.UNBOUND ? null : raw);
                }
                case ExecutableDestructuringAssignment d -> {
                    for (SymbolRef target : d.targets()) {
                        Object raw = scope.findRaw(target);
                        result.put(target.name(), raw == ExecutionScope.UNBOUND ? null : raw);
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
                Object rawValue = evaluateExpr(s.value(), scope);
                scope.assign(s.target(), rawValue);
                AuditCollector audit = scope.audit();
                if (audit != null) {
                    audit.record(new AuditEvent.AssignmentEvent(s.target().name(), rawValue));
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
    // Expression evaluation — returns raw Object, no boxing
    // -------------------------------------------------------------------------

    private Object evaluateExpr(ExecutableNode node, ExecutionScope scope) {
        return switch (node) {
            case ExecutableLiteral lit -> lit.precomputed();
            case ExecutableDynamicLiteral dyn -> {
                Object rawValue = scope.resolveDynamic(dyn.kind());
                AuditCollector audit = scope.audit();
                if (audit != null) {
                    audit.record(new AuditEvent.VariableRead(dyn.kind().canonicalName(), true, rawValue));
                }
                yield rawValue;
            }
            case ExecutableIdentifier id -> {
                Object rawValue = scope.findRaw(id.ref());
                if (rawValue == ExecutionScope.UNBOUND) {
                    throw unboundVariableException(id);
                }
                AuditCollector audit = scope.audit();
                if (audit != null) {
                    audit.record(new AuditEvent.VariableRead(id.ref().name(), false, rawValue));
                }
                yield rawValue;
            }
            case ExecutableFunctionCall f  -> evaluateFunctionCall(f, scope);
            case ExecutableConditional c   -> evaluateConditional(c, scope);
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
            Object rawResult = runtimeServices.coerceToResolvedType(
                    node.foldedResult(), node.binding().returnType());
            AuditCollector audit = scope.audit();
            if (audit != null) {
                audit.enterCall();
                audit.exitCall();
                audit.record(new AuditEvent.FunctionCall(
                    node.binding().descriptor().name(),
                    node.foldedArgs(),
                    rawResult,
                    audit.callDepth()
                ));
            }
            return rawResult;
        }
        FunctionDescriptor descriptor = node.binding().descriptor();
        int arity = descriptor.arity();
        Object[] args = new Object[arity];
        for (int i = 0; i < arity; i++) {
            Object evaluated = evaluateExpr(node.arguments().get(i), scope);
            args[i] = runtimeServices.coerceRaw(evaluated, descriptor.parameterTypes().get(i));
        }
        AuditCollector audit = scope.audit();
        if (audit != null) audit.enterCall();
        Object rawResult = runtimeServices.coerceToResolvedType(
                descriptor.invoke(args), node.binding().returnType());
        if (audit != null) {
            audit.exitCall();
            audit.record(new AuditEvent.FunctionCall(descriptor.name(), args, rawResult, audit.callDepth()));
        }
        return rawResult;
    }

    private Object evaluateConditional(ExecutableConditional node, ExecutionScope scope) {
        List<ExecutableNode> conditions = node.conditions();
        for (int index = 0; index < conditions.size(); index++) {
            if (asRawBoolean(evaluateExpr(conditions.get(index), scope))) {
                return evaluateExpr(node.results().get(index), scope);
            }
        }
        return evaluateExpr(node.elseExpression(), scope);
    }

    private Object evaluateUnary(ExecutableUnaryOp node, ExecutionScope scope) {
        Object operand = evaluateExpr(node.operand(), scope);
        return switch (node.operator()) {
            case NEGATE      -> asRawBigDecimal(operand).negate();
            case LOGICAL_NOT -> !asRawBoolean(operand);
            case SQRT        -> asRawBigDecimal(operand).sqrt(mathContext);
            case MODULUS     -> asRawBigDecimal(operand).abs();
        };
    }

    private Object evaluateBinary(ExecutableBinaryOp node, ExecutionScope scope) {
        Object left = evaluateExpr(node.left(), scope);
        BinaryOperator operator = node.operator();
        // Short-circuit evaluation for logical operators
        if (operator == BinaryOperator.AND || operator == BinaryOperator.NAND) {
            boolean leftBool = asRawBoolean(left);
            if (!leftBool) return operator == BinaryOperator.NAND;
        } else if (operator == BinaryOperator.OR || operator == BinaryOperator.NOR) {
            boolean leftBool = asRawBoolean(left);
            if (leftBool) return operator == BinaryOperator.OR;
        }
        Object right = evaluateExpr(node.right(), scope);
        return switch (operator) {
            case ADD      -> asRawBigDecimal(left).add(asRawBigDecimal(right));
            case SUBTRACT -> asRawBigDecimal(left).subtract(asRawBigDecimal(right));
            case MULTIPLY -> asRawBigDecimal(left).multiply(asRawBigDecimal(right));
            case DIVIDE   -> asRawBigDecimal(left).divide(asRawBigDecimal(right), mathContext);
            case MODULO   -> asRawBigDecimal(left).remainder(asRawBigDecimal(right));
            case POWER    -> pow(asRawBigDecimal(left), asRawBigDecimal(right));
            case ROOT     -> BigDecimalMath.root(asRawBigDecimal(left), asRawBigDecimal(right), mathContext);
            case AND      -> asRawBoolean(right);
            case OR       -> asRawBoolean(right);
            case XOR      -> asRawBoolean(left) ^ asRawBoolean(right);
            case XNOR     -> !(asRawBoolean(left) ^ asRawBoolean(right));
            case NAND     -> !asRawBoolean(right);
            case NOR      -> !asRawBoolean(right);
            case GREATER_THAN          -> compare(left, right) > 0;
            case GREATER_THAN_OR_EQUAL -> compare(left, right) >= 0;
            case LESS_THAN             -> compare(left, right) < 0;
            case LESS_THAN_OR_EQUAL    -> compare(left, right) <= 0;
            case EQUAL                 -> compareEquality(left, right);
            case NOT_EQUAL             -> !compareEquality(left, right);
        };
    }

    private Object evaluatePostfix(ExecutablePostfixOp node, ExecutionScope scope) {
        BigDecimal value = asRawBigDecimal(evaluateExpr(node.operand(), scope));
        return switch (node.operator()) {
            case PERCENT   -> value.movePointRight(2);
            case FACTORIAL -> BigDecimalMath.factorial(value, mathContext);
        };
    }

    private List<Object> evaluateVector(ExecutableVectorLiteral node, ExecutionScope scope) {
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
            return asRawBigDecimal(left).compareTo(asRawBigDecimal(right));
        }
        if (left instanceof LocalDate || right instanceof LocalDate) {
            return asRawLocalDate(left).compareTo(asRawLocalDate(right));
        }
        if (left instanceof LocalTime || right instanceof LocalTime) {
            return asRawLocalTime(left).compareTo(asRawLocalTime(right));
        }
        if (left instanceof LocalDateTime || right instanceof LocalDateTime) {
            return asRawLocalDateTime(left).compareTo(asRawLocalDateTime(right));
        }
        if (left instanceof String || right instanceof String) {
            return asRawString(left).compareTo(asRawString(right));
        }
        if (left instanceof Boolean || right instanceof Boolean) {
            return Boolean.compare(asRawBoolean(left), asRawBoolean(right));
        }
        throw new IllegalStateException("unsupported comparison between values");
    }

    private boolean compareEquality(Object left, Object right) {
        if (left == null || right == null) {
            return left == null && right == null;
        }
        if (left instanceof BigDecimal || right instanceof BigDecimal) {
            return asRawBigDecimal(left).compareTo(asRawBigDecimal(right)) == 0;
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
    // Raw type helpers — fast-path instanceof casts, fallback via RuntimeServices
    // -------------------------------------------------------------------------

    private BigDecimal asRawBigDecimal(Object value) {
        if (value instanceof BigDecimal bd) return bd;
        return runtimeServices.asNumber(value);
    }

    private boolean asRawBoolean(Object value) {
        if (value instanceof Boolean b) return b;
        return runtimeServices.asBoolean(value);
    }

    private String asRawString(Object value) {
        if (value instanceof String s) return s;
        return runtimeServices.asString(value);
    }

    private LocalDate asRawLocalDate(Object value) {
        if (value instanceof LocalDate d) return d;
        return runtimeServices.asDate(value);
    }

    private LocalTime asRawLocalTime(Object value) {
        if (value instanceof LocalTime t) return t;
        return runtimeServices.asTime(value);
    }

    private LocalDateTime asRawLocalDateTime(Object value) {
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
