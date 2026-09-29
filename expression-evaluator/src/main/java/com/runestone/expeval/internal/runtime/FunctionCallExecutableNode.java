package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.FunctionDescriptor;
import com.runestone.expeval.api.SourceSpan;
import com.runestone.expeval.internal.ast.NodeId;
import com.runestone.expeval.internal.regex.PreparedRegexCall;

import java.util.List;
import java.util.Objects;

/** A global function call bound to its {@link FunctionDescriptor}'s method handle during compilation. */
public record FunctionCallExecutableNode(
        NodeId id,
        SourceSpan sourceSpan,
        FunctionDescriptor descriptor,
        List<ExecutableNode> arguments,
        PreparedRegexCall preparedRegexCall,
        int calculationSlot,
        int[] replaySlots) implements CalculationPointExecutableNode {

    public FunctionCallExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(descriptor, "descriptor");
        arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments"));
        Objects.requireNonNull(replaySlots, "replaySlots");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        Object value = preparedRegexCall == null
                ? ExpressionRuntime.invokeFunction(descriptor, arguments, scope, sourceSpan)
                : ExpressionRuntime.invokePreparedRegexBuiltIn(
                        descriptor, arguments, preparedRegexCall, scope, sourceSpan);
        scope.validateValue(value, sourceSpan);
        scope.captureCalculation(calculationSlot, replaySlots, value);
        return value;
    }
}
