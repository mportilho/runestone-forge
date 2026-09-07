package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.ast.PostfixOperator;
import com.runestone.expeval_mk3.internal.ast.PostfixOperatorOccurrence;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.diagnostics.RuntimeFailures;
import com.runestone.expeval_mk3.internal.semantics.DeferredCheck;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

public final class PostfixExecutableNode implements ExecutableNode {

    private final NodeId id;
    private final SourceSpan sourceSpan;
    private final ExecutableNode operand;
    private final List<RuntimePostfixOperation> operations;
    private final int maxFactorialInput;
    private final List<DeferredCheck> deferredChecks;

    public PostfixExecutableNode(
            NodeId id,
            SourceSpan sourceSpan,
            ExecutableNode operand,
            List<PostfixOperatorOccurrence> operations,
            int maxFactorialInput,
            List<DeferredCheck> deferredChecks) {
        this.id = Objects.requireNonNull(id, "id");
        this.sourceSpan = Objects.requireNonNull(sourceSpan, "sourceSpan");
        this.operand = Objects.requireNonNull(operand, "operand");
        Objects.requireNonNull(operations, "operations");
        RuntimePostfixOperation[] copiedOperations = new RuntimePostfixOperation[operations.size()];
        for (int index = 0; index < operations.size(); index++) {
            PostfixOperatorOccurrence operation = operations.get(index);
            copiedOperations[index] = new RuntimePostfixOperation(operation.operator(), operation.sourceSpan());
        }
        this.operations = List.of(copiedOperations);
        this.maxFactorialInput = maxFactorialInput;
        this.deferredChecks = List.copyOf(Objects.requireNonNull(deferredChecks, "deferredChecks"));
    }

    @Override
    public NodeId id() {
        return id;
    }

    @Override
    public SourceSpan sourceSpan() {
        return sourceSpan;
    }

    @Override
    public List<DeferredCheck> deferredChecks() {
        return deferredChecks;
    }

    @Override
    public Object execute(ExecutionScope scope) {
        BigDecimal result = ExpressionRuntime.number(operand.execute(scope));
        for (RuntimePostfixOperation operation : operations) {
            result = operation.operator() == PostfixOperator.PERCENT
                    ? result.movePointLeft(2)
                    : factorial(result, operation.sourceSpan());
        }
        return result;
    }

    private BigDecimal factorial(BigDecimal value, SourceSpan operationSpan) {
        BigDecimal normalized = value.stripTrailingZeros();
        if (normalized.scale() > 0) {
            throw RuntimeFailures.domainViolation(
                    DiagnosticCode.RUNTIME_FACTORIAL_NOT_INTEGRAL,
                    "factorial input must be integral: " + value,
                    operationSpan);
        }
        BigInteger integerValue = normalized.toBigInteger();
        if (integerValue.signum() < 0) {
            throw RuntimeFailures.domainViolation(
                    DiagnosticCode.RUNTIME_FACTORIAL_NEGATIVE,
                    "factorial input must not be negative: " + value,
                    operationSpan);
        }
        if (integerValue.compareTo(BigInteger.valueOf(maxFactorialInput)) > 0) {
            throw RuntimeFailures.domainViolation(
                    DiagnosticCode.RUNTIME_FACTORIAL_EXCEEDS_MAXIMUM,
                    "factorial input exceeds maxFactorialInput " + maxFactorialInput + ": " + value,
                    operationSpan);
        }
        int integer = integerValue.intValue();
        BigInteger result = BigInteger.ONE;
        for (int factor = 2; factor <= integer; factor++) {
            result = result.multiply(BigInteger.valueOf(factor));
        }
        return new BigDecimal(result);
    }

    private static final class RuntimePostfixOperation {

        private final PostfixOperator operator;
        private final SourceSpan sourceSpan;

        private RuntimePostfixOperation(PostfixOperator operator, SourceSpan sourceSpan) {
            this.operator = Objects.requireNonNull(operator, "operator");
            this.sourceSpan = Objects.requireNonNull(sourceSpan, "sourceSpan");
        }

        private PostfixOperator operator() {
            return operator;
        }

        private SourceSpan sourceSpan() {
            return sourceSpan;
        }
    }
}
