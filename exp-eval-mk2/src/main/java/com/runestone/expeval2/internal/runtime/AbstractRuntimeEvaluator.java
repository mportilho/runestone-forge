package com.runestone.expeval2.internal.runtime;

import ch.obermuhlner.math.big.BigDecimalMath;
import com.runestone.expeval2.api.AuditEvent;
import com.runestone.expeval2.api.CompilationPosition;
import com.runestone.expeval2.api.ExpressionEvaluationException;
import com.runestone.expeval2.catalog.FunctionDescriptor;
import com.runestone.expeval2.internal.ast.BinaryOperator;
import com.runestone.expeval2.internal.ast.SourceSpan;
import com.runestone.expeval2.types.ScalarType;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

abstract class AbstractRuntimeEvaluator<T> {

    private final CompiledExpression compiledExpression;
    private final RuntimeServices runtimeServices;
    private final MathContext mathContext;

    protected AbstractRuntimeEvaluator(CompiledExpression compiledExpression, RuntimeServices runtimeServices,
                                       MathContext mathContext) {
        this.compiledExpression = Objects.requireNonNull(compiledExpression, "compiledExpression must not be null");
        this.runtimeServices = Objects.requireNonNull(runtimeServices, "runtimeServices must not be null");
        this.mathContext = Objects.requireNonNull(mathContext, "mathContext must not be null");
    }

    final T evaluate(ExecutionScope scope) {
        Objects.requireNonNull(scope, "scope must not be null");
        ExecutionPlan plan = compiledExpression.executionPlan();
        for (ExecutableAssignment assignment : plan.assignments()) {
            executeAssignment(assignment, scope);
        }
        return convertResult(evaluateExpression(plan.resultExpression(), scope));
    }

    protected abstract T convertResult(RuntimeValue value);

    final Map<String, Object> evaluateAssignments(ExecutionScope scope) {
        Objects.requireNonNull(scope, "scope must not be null");
        ExecutionPlan plan = compiledExpression.executionPlan();
        for (ExecutableAssignment assignment : plan.assignments()) {
            executeAssignment(assignment, scope);
        }
        // Collect in source-declaration order; plan.assignments() preserves declaration order
        // and already holds the resolved SymbolRefs — no AST traversal or extra map lookup needed
        List<ExecutableAssignment> assignments = plan.assignments();
        Map<String, Object> result = new LinkedHashMap<>(assignments.size());
        for (ExecutableAssignment assignment : assignments) {
            switch (assignment) {
                case ExecutableSimpleAssignment s ->
                    result.put(s.target().name(), scope.find(s.target()).orElse(RuntimeValue.NullValue.INSTANCE).raw());
                case ExecutableDestructuringAssignment d -> {
                    for (SymbolRef target : d.targets()) {
                        result.put(target.name(), scope.find(target).orElse(RuntimeValue.NullValue.INSTANCE).raw());
                    }
                }
            }
        }
        return result;
    }

    private void executeAssignment(ExecutableAssignment assignment, ExecutionScope scope) {
        switch (assignment) {
            case ExecutableSimpleAssignment s -> {
                RuntimeValue value = evaluateExpression(s.value(), scope);
                scope.assign(s.target(), value);
                AuditCollector simpleAudit = scope.audit();
                if (simpleAudit != null) {
                    simpleAudit.record(new AuditEvent.AssignmentEvent(s.target().name(), value.raw()));
                }
            }
            case ExecutableDestructuringAssignment d -> {
                List<RuntimeValue> values = runtimeServices.asVector(evaluateExpression(d.value(), scope));
                AuditCollector destructAudit = scope.audit();
                List<SymbolRef> targets = d.targets();
                for (int index = 0; index < targets.size(); index++) {
                    SymbolRef target = targets.get(index);
                    RuntimeValue value = index < values.size() ? values.get(index) : RuntimeValue.NullValue.INSTANCE;
                    scope.assign(target, value);
                    if (destructAudit != null) {
                        destructAudit.record(new AuditEvent.AssignmentEvent(target.name(), value.raw()));
                    }
                }
            }
        }
    }

    private RuntimeValue evaluateExpression(ExecutableNode node, ExecutionScope scope) {
        return switch (node) {
            case ExecutableLiteral lit -> lit.precomputed();
            case ExecutableDynamicLiteral dyn -> {
                RuntimeValue dynValue = scope.resolveDynamic(dyn.kind());
                AuditCollector dynAudit = scope.audit();
                if (dynAudit != null) {
                    dynAudit.record(new AuditEvent.VariableRead(dyn.kind().canonicalName(), true, dynValue.raw()));
                }
                yield dynValue;
            }
            case ExecutableIdentifier id -> {
                RuntimeValue idValue = scope.find(id.ref())
                        .orElseThrow(() -> unboundVariableException(id));
                AuditCollector idAudit = scope.audit();
                if (idAudit != null) {
                    idAudit.record(new AuditEvent.VariableRead(id.ref().name(), false, idValue.raw()));
                }
                yield idValue;
            }
            case ExecutableFunctionCall f -> evaluateFunctionCall(f, scope);
            case ExecutableConditional c -> evaluateConditional(c, scope);
            case ExecutableUnaryOp u -> evaluateUnary(u, scope);
            case ExecutableBinaryOp b -> evaluateBinary(b, scope);
            case ExecutablePostfixOp p -> evaluatePostfix(p, scope);
            case ExecutableVectorLiteral v -> evaluateVector(v, scope);
        };
    }

    private RuntimeValue evaluateFunctionCall(ExecutableFunctionCall node, ExecutionScope scope) {
        FunctionDescriptor descriptor = node.binding().descriptor();
        int arity = descriptor.arity();
        Object[] args = new Object[arity];
        for (int i = 0; i < arity; i++) {
            RuntimeValue evaluated = evaluateExpression(node.arguments().get(i), scope);
            args[i] = runtimeServices.coerce(evaluated, descriptor.parameterTypes().get(i));
        }
        AuditCollector audit = scope.audit();
        if (audit != null) audit.enterCall();
        Object rawResult = descriptor.invoke(args);
        RuntimeValue result = runtimeServices.from(rawResult, node.binding().returnType());
        if (audit != null) {
            audit.exitCall();
            audit.record(new AuditEvent.FunctionCall(descriptor.name(), args, result.raw(), audit.callDepth()));
        }
        return result;
    }

    private RuntimeValue evaluateConditional(ExecutableConditional node, ExecutionScope scope) {
        for (int index = 0; index < node.conditions().size(); index++) {
            if (runtimeServices.asBoolean(evaluateExpression(node.conditions().get(index), scope))) {
                return evaluateExpression(node.results().get(index), scope);
            }
        }
        return evaluateExpression(node.elseExpression(), scope);
    }

    private RuntimeValue evaluateUnary(ExecutableUnaryOp node, ExecutionScope scope) {
        RuntimeValue operand = evaluateExpression(node.operand(), scope);
        return switch (node.operator()) {
            case NEGATE -> new RuntimeValue.NumberValue(runtimeServices.asNumber(operand).negate());
            case LOGICAL_NOT -> new RuntimeValue.BooleanValue(!runtimeServices.asBoolean(operand));
            case SQRT -> new RuntimeValue.NumberValue(runtimeServices.asNumber(operand).sqrt(mathContext));
            case MODULUS -> new RuntimeValue.NumberValue(runtimeServices.asNumber(operand).abs());
        };
    }

    private RuntimeValue evaluateBinary(ExecutableBinaryOp node, ExecutionScope scope) {
        RuntimeValue left = evaluateExpression(node.left(), scope);
        BinaryOperator operator = node.operator();
        if (operator == BinaryOperator.AND || operator == BinaryOperator.NAND) {
            boolean leftBool = runtimeServices.asBoolean(left);
            if (!leftBool) {
                return new RuntimeValue.BooleanValue(operator == BinaryOperator.NAND);
            }
        } else if (operator == BinaryOperator.OR || operator == BinaryOperator.NOR) {
            boolean leftBool = runtimeServices.asBoolean(left);
            if (leftBool) {
                return new RuntimeValue.BooleanValue(operator == BinaryOperator.OR);
            }
        }
        RuntimeValue right = evaluateExpression(node.right(), scope);
        return switch (operator) {
            case ADD ->
                    new RuntimeValue.NumberValue(runtimeServices.asNumber(left).add(runtimeServices.asNumber(right)));
            case SUBTRACT ->
                    new RuntimeValue.NumberValue(runtimeServices.asNumber(left).subtract(runtimeServices.asNumber(right)));
            case MULTIPLY ->
                    new RuntimeValue.NumberValue(runtimeServices.asNumber(left).multiply(runtimeServices.asNumber(right)));
            case DIVIDE ->
                    new RuntimeValue.NumberValue(runtimeServices.asNumber(left).divide(runtimeServices.asNumber(right), mathContext));
            case MODULO ->
                    new RuntimeValue.NumberValue(runtimeServices.asNumber(left).remainder(runtimeServices.asNumber(right)));
            case POWER ->
                    new RuntimeValue.NumberValue(pow(runtimeServices.asNumber(left), runtimeServices.asNumber(right)));
            case ROOT -> {
                BigDecimal value = runtimeServices.asNumber(left);
                yield new RuntimeValue.NumberValue(BigDecimalMath.root(value, runtimeServices.asNumber(right), mathContext));
            }
            case AND -> new RuntimeValue.BooleanValue(runtimeServices.asBoolean(right));
            case OR -> new RuntimeValue.BooleanValue(runtimeServices.asBoolean(right));
            case XOR ->
                    new RuntimeValue.BooleanValue(runtimeServices.asBoolean(left) ^ runtimeServices.asBoolean(right));
            case XNOR ->
                    new RuntimeValue.BooleanValue(!(runtimeServices.asBoolean(left) ^ runtimeServices.asBoolean(right)));
            case NAND -> new RuntimeValue.BooleanValue(!runtimeServices.asBoolean(right));
            case NOR -> new RuntimeValue.BooleanValue(!runtimeServices.asBoolean(right));
            case GREATER_THAN -> new RuntimeValue.BooleanValue(compare(left, right) > 0);
            case GREATER_THAN_OR_EQUAL -> new RuntimeValue.BooleanValue(compare(left, right) >= 0);
            case LESS_THAN -> new RuntimeValue.BooleanValue(compare(left, right) < 0);
            case LESS_THAN_OR_EQUAL -> new RuntimeValue.BooleanValue(compare(left, right) <= 0);
            case EQUAL -> new RuntimeValue.BooleanValue(compareEquality(left, right));
            case NOT_EQUAL -> new RuntimeValue.BooleanValue(!compareEquality(left, right));
        };
    }

    private RuntimeValue evaluatePostfix(ExecutablePostfixOp node, ExecutionScope scope) {
        BigDecimal value = runtimeServices.asNumber(evaluateExpression(node.operand(), scope));
        return switch (node.operator()) {
            case PERCENT -> new RuntimeValue.NumberValue(value.movePointRight(2));
            case FACTORIAL -> new RuntimeValue.NumberValue(factorial(value));
        };
    }

    private RuntimeValue evaluateVector(ExecutableVectorLiteral node, ExecutionScope scope) {
        List<RuntimeValue> elements = new ArrayList<>(node.elements().size());
        for (ExecutableNode element : node.elements()) {
            elements.add(evaluateExpression(element, scope));
        }
        return new RuntimeValue.VectorValue(elements);
    }

    private int compare(RuntimeValue left, RuntimeValue right) {
        if (left.type() == ScalarType.NUMBER || right.type() == ScalarType.NUMBER) {
            return runtimeServices.asNumber(left).compareTo(runtimeServices.asNumber(right));
        }
        if (left.type() == ScalarType.DATE || right.type() == ScalarType.DATE) {
            return runtimeServices.asDate(left).compareTo(runtimeServices.asDate(right));
        }
        if (left.type() == ScalarType.TIME || right.type() == ScalarType.TIME) {
            return runtimeServices.asTime(left).compareTo(runtimeServices.asTime(right));
        }
        if (left.type() == ScalarType.DATETIME || right.type() == ScalarType.DATETIME) {
            return runtimeServices.asDateTime(left).compareTo(runtimeServices.asDateTime(right));
        }
        if (left.type() == ScalarType.STRING || right.type() == ScalarType.STRING) {
            return runtimeServices.asString(left).compareTo(runtimeServices.asString(right));
        }
        if (left.type() == ScalarType.BOOLEAN || right.type() == ScalarType.BOOLEAN) {
            return Boolean.compare(runtimeServices.asBoolean(left), runtimeServices.asBoolean(right));
        }
        throw new IllegalStateException("unsupported comparison between values");
    }

    private boolean compareEquality(RuntimeValue left, RuntimeValue right) {
        if (left instanceof RuntimeValue.NullValue || right instanceof RuntimeValue.NullValue) {
            return left instanceof RuntimeValue.NullValue && right instanceof RuntimeValue.NullValue;
        }
        if (left.type() == ScalarType.NUMBER || right.type() == ScalarType.NUMBER) {
            return runtimeServices.asNumber(left).compareTo(runtimeServices.asNumber(right)) == 0;
        }
        if (left instanceof RuntimeValue.VectorValue(List<RuntimeValue> leftElements)
            && right instanceof RuntimeValue.VectorValue(List<RuntimeValue> rightElements)) {
            if (leftElements.size() != rightElements.size()) {
                return false;
            }
            for (int i = 0; i < leftElements.size(); i++) {
                if (!compareEquality(leftElements.get(i), rightElements.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return Objects.equals(left.raw(), right.raw());
    }

    private BigDecimal factorial(BigDecimal value) {
        return BigDecimalMath.factorial(value, mathContext);
    }

    private BigDecimal pow(BigDecimal base, BigDecimal exponent) {
        BigDecimal normalized = exponent.stripTrailingZeros();
        if (normalized.scale() <= 0 && normalized.precision() <= 9) {
            return base.pow(normalized.intValue(), mathContext);
        }
        return BigDecimalMath.pow(base, exponent, mathContext);
    }

    private ExpressionEvaluationException unboundVariableException(ExecutableIdentifier id) {
        SourceSpan span = id.sourceSpan();
        CompilationPosition position = new CompilationPosition(span.startLine(), span.startColumn(), span.endColumn());
        String message = "variable '" + id.ref().name() + "' has no value; call setValue(\"" + id.ref().name() + "\", ...) before compute()";
        return new ExpressionEvaluationException(compiledExpression.source(), "UNBOUND_VARIABLE", message, position);
    }
}
