package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.diagnostics.RuntimeFailures;

import java.math.BigDecimal;
import java.util.Objects;

public record ModuloDecimalExecutableNode(
        NodeId id, SourceSpan sourceSpan, ExecutableNode left, ExecutableNode right) implements ExecutableNode {

    public ModuloDecimalExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(right, "right");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        BigDecimal dividend = (BigDecimal) left.execute(scope);
        BigDecimal divisor = (BigDecimal) right.execute(scope);
        if (divisor.signum() == 0) {
            throw RuntimeFailures.undefinedOperation("modulo by zero", sourceSpan);
        }
        try {
            return dividend.remainder(divisor);
        } catch (ArithmeticException exception) {
            throw RuntimeFailures.calculationFailure("modulo failed", sourceSpan, exception);
        }
    }
}
