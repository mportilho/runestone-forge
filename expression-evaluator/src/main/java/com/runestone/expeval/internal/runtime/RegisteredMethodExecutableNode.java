package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.SourceSpan;
import com.runestone.expeval.internal.ast.NodeId;
import com.runestone.expeval.internal.semantics.RegisteredMethodNavigationBinding;

import java.util.List;
import java.util.Objects;

/** {@code .method(args)} on a registered {@code ObjectType} through its environment-prepared entry point. */
public record RegisteredMethodExecutableNode(
        NodeId id,
        SourceSpan sourceSpan,
        ExecutableNode receiver,
        boolean safe,
        RegisteredMethodNavigationBinding binding,
        List<ExecutableNode> arguments,
        int calculationSlot,
        int[] replaySlots) implements CalculationPointExecutableNode {

    public RegisteredMethodExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(receiver, "receiver");
        Objects.requireNonNull(binding, "binding");
        arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments"));
        Objects.requireNonNull(replaySlots, "replaySlots");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        Object value = ExpressionRuntime.invokeRegisteredMethod(
                receiver.execute(scope), safe, binding, arguments, scope, sourceSpan);
        scope.validateValue(value, sourceSpan);
        scope.captureCalculation(calculationSlot, replaySlots, value);
        return value;
    }
}
