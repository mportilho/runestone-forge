package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.semantics.RegisteredMethodNavigationBinding;

import java.util.List;
import java.util.Objects;

/** Unoptimized Oracle route retaining generic MethodHandle method invocation. */
public record OracleRegisteredMethodExecutableNode(
        NodeId id,
        SourceSpan sourceSpan,
        ExecutableNode receiver,
        boolean safe,
        RegisteredMethodNavigationBinding binding,
        List<ExecutableNode> arguments,
        int calculationSlot,
        int[] replaySlots) implements CalculationPointExecutableNode {

    public OracleRegisteredMethodExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(receiver, "receiver");
        Objects.requireNonNull(binding, "binding");
        arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments"));
        Objects.requireNonNull(replaySlots, "replaySlots");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        Object value = ExpressionRuntime.oracleInvokeRegisteredMethod(
                receiver.execute(scope), safe, binding, arguments, scope, sourceSpan);
        scope.captureCalculation(calculationSlot, replaySlots, value);
        return value;
    }
}
