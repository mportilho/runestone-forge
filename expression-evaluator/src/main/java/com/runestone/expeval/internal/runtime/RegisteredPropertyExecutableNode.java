package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.SourceSpan;
import com.runestone.expeval.internal.ast.NodeId;
import com.runestone.expeval.internal.semantics.RegisteredPropertyNavigationBinding;

import java.util.Objects;

/** {@code .property} on a registered {@code ObjectType} through its environment-prepared entry point. */
public record RegisteredPropertyExecutableNode(
        NodeId id,
        SourceSpan sourceSpan,
        ExecutableNode receiver,
        boolean safe,
        RegisteredPropertyNavigationBinding binding,
        int calculationSlot,
        int[] replaySlots) implements CalculationPointExecutableNode {

    public RegisteredPropertyExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(receiver, "receiver");
        Objects.requireNonNull(binding, "binding");
        Objects.requireNonNull(replaySlots, "replaySlots");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        Object value = ExpressionRuntime.registeredPropertyValue(receiver.execute(scope), safe, binding, sourceSpan);
        scope.validateValue(value, sourceSpan);
        scope.captureCalculation(calculationSlot, replaySlots, value);
        return value;
    }
}
