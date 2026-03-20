package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.catalog.FunctionDescriptor;
import com.runestone.expeval2.internal.ast.BinaryOperator;
import com.runestone.expeval2.types.ScalarType;

import ch.obermuhlner.math.big.BigDecimalMath;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

abstract class AbstractRuntimeEvaluator<T> {

    private final CompiledExpression compiledExpression;
    private final RuntimeValueFactory runtimeValueFactory;
    private final RuntimeCoercionService runtimeCoercionService;

    protected AbstractRuntimeEvaluator(CompiledExpression compiledExpression, RuntimeValueFactory runtimeValueFactory, RuntimeCoercionService runtimeCoercionService) {
        this.compiledExpression = Objects.requireNonNull(compiledExpression, "compiledExpression must not be null");
        this.runtimeValueFactory = Objects.requireNonNull(runtimeValueFactory, "runtimeValueFactory must not be null");
        this.runtimeCoercionService = Objects.requireNonNull(runtimeCoercionService, "runtimeCoercionService must not be null");
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

    private void executeAssignment(ExecutableAssignment assignment, ExecutionScope scope) {
        switch (assignment) {
            case ExecutableSimpleAssignment s ->
                    scope.assign(s.target(), evaluateExpression(s.value(), scope));
            case ExecutableDestructuringAssignment d -> {
                List<RuntimeValue> values = runtimeCoercionService.asVector(evaluateExpression(d.value(), scope));
                for (int index = 0; index < d.targets().size(); index++) {
                    RuntimeValue value = index < values.size() ? values.get(index) : RuntimeValue.NullValue.INSTANCE;
                    scope.assign(d.targets().get(index), value);
                }
            }
        }
    }

    private RuntimeValue evaluateExpression(ExecutableNode node, ExecutionScope scope) {
        return switch (node) {
            case ExecutableLiteral lit -> lit.precomputed();
            case ExecutableDynamicLiteral dyn -> scope.resolveDynamic(dyn.kind());
            case ExecutableIdentifier id -> scope.find(id.ref())
                    .orElseThrow(() -> new IllegalStateException("missing value for symbol '" + id.ref().name() + "'"));
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
            args[i] = runtimeCoercionService.coerce(evaluated, descriptor.parameterTypes().get(i));
        }
        Object rawResult = descriptor.invoke(args);
        return runtimeValueFactory.from(rawResult, node.binding().returnType());
    }

    private RuntimeValue evaluateConditional(ExecutableConditional node, ExecutionScope scope) {
        for (int index = 0; index < node.conditions().size(); index++) {
            if (runtimeCoercionService.asBoolean(evaluateExpression(node.conditions().get(index), scope))) {
                return evaluateExpression(node.results().get(index), scope);
            }
        }
        return evaluateExpression(node.elseExpression(), scope);
    }

    private RuntimeValue evaluateUnary(ExecutableUnaryOp node, ExecutionScope scope) {
        RuntimeValue operand = evaluateExpression(node.operand(), scope);
        return switch (node.operator()) {
            case NEGATE -> new RuntimeValue.NumberValue(runtimeCoercionService.asNumber(operand).negate());
            case LOGICAL_NOT -> new RuntimeValue.BooleanValue(!runtimeCoercionService.asBoolean(operand));
            case SQRT -> new RuntimeValue.NumberValue(runtimeCoercionService.asNumber(operand).sqrt(MathContext.DECIMAL128));
            case MODULUS -> new RuntimeValue.NumberValue(runtimeCoercionService.asNumber(operand).abs());
        };
    }

    private RuntimeValue evaluateBinary(ExecutableBinaryOp node, ExecutionScope scope) {
        RuntimeValue left = evaluateExpression(node.left(), scope);
        BinaryOperator operator = node.operator();
        if (operator == BinaryOperator.AND || operator == BinaryOperator.NAND) {
            boolean leftBool = runtimeCoercionService.asBoolean(left);
            if (!leftBool) {
                return new RuntimeValue.BooleanValue(operator == BinaryOperator.NAND);
            }
        } else if (operator == BinaryOperator.OR || operator == BinaryOperator.NOR) {
            boolean leftBool = runtimeCoercionService.asBoolean(left);
            if (leftBool) {
                return new RuntimeValue.BooleanValue(operator == BinaryOperator.OR);
            }
        }
        RuntimeValue right = evaluateExpression(node.right(), scope);
        return switch (operator) {
            case ADD ->
                    new RuntimeValue.NumberValue(runtimeCoercionService.asNumber(left).add(runtimeCoercionService.asNumber(right)));
            case SUBTRACT ->
                    new RuntimeValue.NumberValue(runtimeCoercionService.asNumber(left).subtract(runtimeCoercionService.asNumber(right)));
            case MULTIPLY ->
                    new RuntimeValue.NumberValue(runtimeCoercionService.asNumber(left).multiply(runtimeCoercionService.asNumber(right)));
            case DIVIDE ->
                    new RuntimeValue.NumberValue(runtimeCoercionService.asNumber(left).divide(runtimeCoercionService.asNumber(right), MathContext.DECIMAL128));
            case MODULO ->
                    new RuntimeValue.NumberValue(runtimeCoercionService.asNumber(left).remainder(runtimeCoercionService.asNumber(right)));
            case POWER ->
                    new RuntimeValue.NumberValue(pow(runtimeCoercionService.asNumber(left), runtimeCoercionService.asNumber(right)));
            case ROOT -> {
                BigDecimal value = runtimeCoercionService.asNumber(left);
                yield new RuntimeValue.NumberValue(BigDecimalMath.root(value, runtimeCoercionService.asNumber(right), MathContext.DECIMAL128));
            }
            case AND -> new RuntimeValue.BooleanValue(runtimeCoercionService.asBoolean(right));
            case OR -> new RuntimeValue.BooleanValue(runtimeCoercionService.asBoolean(right));
            case XOR ->
                    new RuntimeValue.BooleanValue(runtimeCoercionService.asBoolean(left) ^ runtimeCoercionService.asBoolean(right));
            case XNOR ->
                    new RuntimeValue.BooleanValue(!(runtimeCoercionService.asBoolean(left) ^ runtimeCoercionService.asBoolean(right)));
            case NAND -> new RuntimeValue.BooleanValue(!runtimeCoercionService.asBoolean(right));
            case NOR -> new RuntimeValue.BooleanValue(!runtimeCoercionService.asBoolean(right));
            case GREATER_THAN -> new RuntimeValue.BooleanValue(compare(left, right) > 0);
            case GREATER_THAN_OR_EQUAL -> new RuntimeValue.BooleanValue(compare(left, right) >= 0);
            case LESS_THAN -> new RuntimeValue.BooleanValue(compare(left, right) < 0);
            case LESS_THAN_OR_EQUAL -> new RuntimeValue.BooleanValue(compare(left, right) <= 0);
            case EQUAL -> new RuntimeValue.BooleanValue(compareEquality(left, right));
            case NOT_EQUAL -> new RuntimeValue.BooleanValue(!compareEquality(left, right));
        };
    }

    private RuntimeValue evaluatePostfix(ExecutablePostfixOp node, ExecutionScope scope) {
        BigDecimal value = runtimeCoercionService.asNumber(evaluateExpression(node.operand(), scope));
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
            return runtimeCoercionService.asNumber(left).compareTo(runtimeCoercionService.asNumber(right));
        }
        if (left.type() == ScalarType.DATE || right.type() == ScalarType.DATE) {
            return runtimeCoercionService.asDate(left).compareTo(runtimeCoercionService.asDate(right));
        }
        if (left.type() == ScalarType.TIME || right.type() == ScalarType.TIME) {
            return runtimeCoercionService.asTime(left).compareTo(runtimeCoercionService.asTime(right));
        }
        if (left.type() == ScalarType.DATETIME || right.type() == ScalarType.DATETIME) {
            return runtimeCoercionService.asDateTime(left).compareTo(runtimeCoercionService.asDateTime(right));
        }
        if (left.type() == ScalarType.STRING || right.type() == ScalarType.STRING) {
            return runtimeCoercionService.asString(left).compareTo(runtimeCoercionService.asString(right));
        }
        if (left.type() == ScalarType.BOOLEAN || right.type() == ScalarType.BOOLEAN) {
            return Boolean.compare(runtimeCoercionService.asBoolean(left), runtimeCoercionService.asBoolean(right));
        }
        throw new IllegalStateException("unsupported comparison between values");
    }

    private boolean compareEquality(RuntimeValue left, RuntimeValue right) {
        if (left.type() == ScalarType.NUMBER || right.type() == ScalarType.NUMBER) {
            return runtimeCoercionService.asNumber(left).compareTo(runtimeCoercionService.asNumber(right)) == 0;
        }
        return Objects.equals(left.raw(), right.raw());
    }

    private BigDecimal factorial(BigDecimal value) {
        BigDecimal normalized = value.stripTrailingZeros();
        int intValue = normalized.intValueExact();
        if (intValue < 0) {
            throw new IllegalStateException("factorial requires a non-negative integer");
        }
        BigDecimal result = BigDecimal.ONE;
        for (int factor = 2; factor <= intValue; factor++) {
            result = result.multiply(BigDecimal.valueOf(factor));
        }
        return result;
    }

    private BigDecimal pow(BigDecimal base, BigDecimal exponent) {
        BigDecimal normalized = exponent.stripTrailingZeros();
        if (normalized.scale() <= 0 && normalized.precision() <= 9) {
            return base.pow(normalized.intValue(), MathContext.DECIMAL128);
        }
        return BigDecimalMath.pow(base, exponent, MathContext.DECIMAL128);
    }
}
