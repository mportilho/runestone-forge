package com.runestone.expeval2.runtime;

import com.runestone.expeval2.ast.*;
import com.runestone.expeval2.compiler.CompiledExpression;
import com.runestone.expeval2.runtime.values.*;
import com.runestone.expeval2.semantic.ResolvedFunctionBinding;
import com.runestone.expeval2.semantic.SymbolRef;
import com.runestone.expeval2.types.ResolvedType;
import com.runestone.expeval2.types.ScalarType;

import ch.obermuhlner.math.big.BigDecimalMath;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
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
        for (AssignmentNode assignment : compiledExpression.semanticModel().ast().assignments()) {
            executeAssignment(assignment, scope);
        }
        RuntimeValue result = evaluateExpression(compiledExpression.semanticModel().ast().resultExpression(), scope);
        return convertResult(result);
    }

    protected abstract T convertResult(RuntimeValue value);

    private void executeAssignment(AssignmentNode assignment, ExecutionScope scope) {
        switch (assignment) {
            case SimpleAssignmentNode simpleAssignment -> {
                SymbolRef target = compiledExpression.semanticModel().internalSymbolsByName().get(simpleAssignment.targetName());
                scope.assign(target, evaluateExpression(simpleAssignment.value(), scope));
            }
            case DestructuringAssignmentNode destructuringAssignment -> {
                List<RuntimeValue> values = runtimeCoercionService.asVector(evaluateExpression(destructuringAssignment.value(), scope));
                for (int index = 0; index < destructuringAssignment.targetNames().size(); index++) {
                    String targetName = destructuringAssignment.targetNames().get(index);
                    RuntimeValue value = index < values.size() ? values.get(index) : NullValue.INSTANCE;
                    scope.assign(compiledExpression.semanticModel().internalSymbolsByName().get(targetName), value);
                }
            }
        }
    }

    private RuntimeValue evaluateExpression(ExpressionNode node, ExecutionScope scope) {
        return switch (node) {
            case LiteralNode literalNode -> evaluateLiteral(literalNode);
            case IdentifierNode identifierNode -> evaluateIdentifier(identifierNode, scope);
            case FunctionCallNode functionCallNode -> evaluateFunctionCall(functionCallNode, scope);
            case ConditionalNode conditionalNode -> evaluateConditional(conditionalNode, scope);
            case UnaryOperationNode unaryOperationNode -> evaluateUnary(unaryOperationNode, scope);
            case BinaryOperationNode binaryOperationNode -> evaluateBinary(binaryOperationNode, scope);
            case PostfixOperationNode postfixOperationNode -> evaluatePostfix(postfixOperationNode, scope);
            case VectorLiteralNode vectorLiteralNode -> evaluateVector(vectorLiteralNode, scope);
        };
    }

    private RuntimeValue evaluateLiteral(LiteralNode node) {
        ResolvedType resolvedType = resolvedType(node.nodeId());
        String raw = node.value();
        if (resolvedType == ScalarType.NUMBER) {
            return new NumberValue(new BigDecimal(raw));
        }
        if (resolvedType == ScalarType.BOOLEAN) {
            return new BooleanValue(Boolean.parseBoolean(raw));
        }
        if (resolvedType == ScalarType.STRING) {
            return new StringValue(unquote(raw));
        }
        if (resolvedType == ScalarType.DATE) {
            if ("currDate".equals(raw)) {
                return new DateValue(LocalDate.now());
            }
            return new DateValue(LocalDate.parse(raw));
        }
        if (resolvedType == ScalarType.TIME) {
            if ("currTime".equals(raw)) {
                return new TimeValue(LocalTime.now());
            }
            return new TimeValue(LocalTime.parse(raw));
        }
        if (resolvedType == ScalarType.DATETIME) {
            if ("currDateTime".equals(raw)) {
                return new DateTimeValue(LocalDateTime.now());
            }
            return raw.contains("+") || raw.endsWith("Z")
                    ? new DateTimeValue(OffsetDateTime.parse(raw).toLocalDateTime())
                    : new DateTimeValue(LocalDateTime.parse(raw));
        }
        return runtimeValueFactory.from(raw);
    }

    private RuntimeValue evaluateIdentifier(IdentifierNode node, ExecutionScope scope) {
        SymbolRef symbolRef = compiledExpression.semanticModel().findSymbol(node.nodeId())
                .orElseThrow(() -> new IllegalStateException("missing symbol for identifier '" + node.name() + "'"));
        return scope.find(symbolRef)
                .orElseThrow(() -> new IllegalStateException("missing value for symbol '" + symbolRef.name() + "'"));
    }

    private RuntimeValue evaluateFunctionCall(FunctionCallNode node, ExecutionScope scope) {
        ResolvedFunctionBinding binding = compiledExpression.semanticModel().findFunctionBinding(node.nodeId())
                .orElseThrow(() -> new IllegalStateException("missing function binding for '" + node.functionName() + "'"));
        List<RuntimeValue> arguments = node.arguments().stream()
                .map(argument -> evaluateExpression(argument, scope))
                .toList();
        List<Object> rawArguments = new ArrayList<>(arguments.size());
        for (int index = 0; index < arguments.size(); index++) {
            rawArguments.add(runtimeCoercionService.coerce(arguments.get(index), binding.descriptor().parameterTypes().get(index)));
        }
        Object rawResult = binding.descriptor().invoke(rawArguments);
        return runtimeValueFactory.from(rawResult, binding.returnType());
    }

    private RuntimeValue evaluateConditional(ConditionalNode node, ExecutionScope scope) {
        for (int index = 0; index < node.conditions().size(); index++) {
            if (runtimeCoercionService.asBoolean(evaluateExpression(node.conditions().get(index), scope))) {
                return evaluateExpression(node.results().get(index), scope);
            }
        }
        return evaluateExpression(node.elseExpression(), scope);
    }

    private RuntimeValue evaluateUnary(UnaryOperationNode node, ExecutionScope scope) {
        RuntimeValue operand = evaluateExpression(node.operand(), scope);
        return switch (node.operator()) {
            case NEGATE -> new NumberValue(runtimeCoercionService.asNumber(operand).negate());
            case LOGICAL_NOT -> new BooleanValue(!runtimeCoercionService.asBoolean(operand));
            case SQRT -> new NumberValue(runtimeCoercionService.asNumber(operand).sqrt(MathContext.DECIMAL128));
            case MODULUS -> new NumberValue(runtimeCoercionService.asNumber(operand).abs());
        };
    }

    private RuntimeValue evaluateBinary(BinaryOperationNode node, ExecutionScope scope) {
        RuntimeValue left = evaluateExpression(node.left(), scope);
        BinaryOperator operator = node.operator();
        if (operator == BinaryOperator.AND || operator == BinaryOperator.NAND) {
            boolean leftBool = runtimeCoercionService.asBoolean(left);
            if (!leftBool) {
                return new BooleanValue(operator == BinaryOperator.NAND);
            }
        } else if (operator == BinaryOperator.OR || operator == BinaryOperator.NOR) {
            boolean leftBool = runtimeCoercionService.asBoolean(left);
            if (leftBool) {
                return new BooleanValue(operator == BinaryOperator.OR);
            }
        }
        RuntimeValue right = evaluateExpression(node.right(), scope);
        return switch (operator) {
            case ADD ->
                    new NumberValue(runtimeCoercionService.asNumber(left).add(runtimeCoercionService.asNumber(right)));
            case SUBTRACT ->
                    new NumberValue(runtimeCoercionService.asNumber(left).subtract(runtimeCoercionService.asNumber(right)));
            case MULTIPLY ->
                    new NumberValue(runtimeCoercionService.asNumber(left).multiply(runtimeCoercionService.asNumber(right)));
            case DIVIDE ->
                    new NumberValue(runtimeCoercionService.asNumber(left).divide(runtimeCoercionService.asNumber(right), MathContext.DECIMAL128));
            case MODULO ->
                    new NumberValue(runtimeCoercionService.asNumber(left).remainder(runtimeCoercionService.asNumber(right)));
            case POWER ->
                    new NumberValue(pow(runtimeCoercionService.asNumber(left), runtimeCoercionService.asNumber(right)));
            case ROOT -> {
                BigDecimal value = runtimeCoercionService.asNumber(left);
                yield new NumberValue(BigDecimalMath.root(value, runtimeCoercionService.asNumber(right), MathContext.DECIMAL128));
            }
            case AND -> new BooleanValue(runtimeCoercionService.asBoolean(right));
            case OR -> new BooleanValue(runtimeCoercionService.asBoolean(right));
            case XOR ->
                    new BooleanValue(runtimeCoercionService.asBoolean(left) ^ runtimeCoercionService.asBoolean(right));
            case XNOR ->
                    new BooleanValue(!(runtimeCoercionService.asBoolean(left) ^ runtimeCoercionService.asBoolean(right)));
            case NAND -> new BooleanValue(!runtimeCoercionService.asBoolean(right));
            case NOR -> new BooleanValue(!runtimeCoercionService.asBoolean(right));
            case GREATER_THAN -> new BooleanValue(compare(left, right) > 0);
            case GREATER_THAN_OR_EQUAL -> new BooleanValue(compare(left, right) >= 0);
            case LESS_THAN -> new BooleanValue(compare(left, right) < 0);
            case LESS_THAN_OR_EQUAL -> new BooleanValue(compare(left, right) <= 0);
            case EQUAL -> new BooleanValue(compareEquality(left, right));
            case NOT_EQUAL -> new BooleanValue(!compareEquality(left, right));
        };
    }

    private RuntimeValue evaluatePostfix(PostfixOperationNode node, ExecutionScope scope) {
        BigDecimal value = runtimeCoercionService.asNumber(evaluateExpression(node.operand(), scope));
        return switch (node.operator()) {
            case PERCENT -> new NumberValue(value.movePointRight(2));
            case FACTORIAL -> new NumberValue(factorial(value));
        };
    }

    private RuntimeValue evaluateVector(VectorLiteralNode node, ExecutionScope scope) {
        return new VectorValue(node.elements().stream()
                .map(element -> evaluateExpression(element, scope))
                .toList());
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

    private ResolvedType resolvedType(NodeId nodeId) {
        return compiledExpression.semanticModel().findResolvedType(nodeId)
                .orElseThrow(() -> new IllegalStateException("missing resolved type for node '" + nodeId.value() + "'"));
    }

    private String unquote(String value) {
        if (value.length() < 2) {
            return value;
        }
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        }
        return value;
    }
}
