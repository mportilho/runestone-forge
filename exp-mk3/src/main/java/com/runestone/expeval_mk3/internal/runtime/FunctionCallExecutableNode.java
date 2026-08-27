package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.FunctionDescriptor;
import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.util.List;
import java.util.Objects;

/** A global function call bound to its {@link FunctionDescriptor}'s method handle during compilation. */
public record FunctionCallExecutableNode(
        NodeId id,
        SourceSpan sourceSpan,
        FunctionDescriptor descriptor,
        List<ExecutableNode> arguments,
        int calculationSlot) implements CalculationPointExecutableNode {

    public FunctionCallExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(descriptor, "descriptor");
        arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments"));
    }

    @Override
    public Object execute(ExecutionScope scope) {
        Object value = ExpressionRuntime.invokeFunction(descriptor, arguments, scope, sourceSpan);
        scope.captureCalculation(calculationSlot, value);
        return value;
    }
}
