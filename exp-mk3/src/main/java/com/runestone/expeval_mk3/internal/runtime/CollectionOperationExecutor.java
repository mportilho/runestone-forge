package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.internal.semantics.CollectionOperationBinding;

import java.math.MathContext;

/**
 * Runtime dispatch target for one {@code CollectionOperationCatalog.OperationIdentity}, resolved once
 * per compiled navigation link by {@link CollectionOperationExecutors} and cached on the executable node.
 */
@FunctionalInterface
public interface CollectionOperationExecutor {

    Object execute(
            CollectionOperationBinding binding,
            Object receiver,
            MathContext mathContext,
            int maxMaterializedSize,
            ExecutableOperationArguments arguments,
            ExecutionScope scope);
}
