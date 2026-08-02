package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.math.MathContext;
import java.util.Objects;

/**
 * {@code .map(...)}, {@code .sum()}, and the other receiver collection operations. The runtime
 * {@link CollectionOperationExecutor} is resolved once by the plan builder and cached here; this node
 * never looks the operation identity up again during execution.
 */
public record CollectionOperationExecutableNode(
        NodeId id,
        SourceSpan sourceSpan,
        ExecutableNode receiver,
        boolean safe,
        CollectionOperationExecutor executor,
        CollectionOperationRuntimeBinding binding,
        MathContext mathContext,
        int maxMaterializedSize,
        ExecutableOperationArguments arguments) implements ExecutableNode {

    public CollectionOperationExecutableNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(receiver, "receiver");
        Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(binding, "binding");
        Objects.requireNonNull(mathContext, "mathContext");
        Objects.requireNonNull(arguments, "arguments");
    }

    @Override
    public Object execute(ExecutionScope scope) {
        return ExpressionRuntime.executeCollectionOperation(
                executor, binding, receiver.execute(scope), safe, mathContext, maxMaterializedSize, arguments, scope,
                sourceSpan);
    }
}
