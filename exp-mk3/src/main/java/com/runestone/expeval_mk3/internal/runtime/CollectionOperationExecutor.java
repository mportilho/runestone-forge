package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;

import java.math.MathContext;

/**
 * Runtime dispatch target for one {@code CollectionOperationCatalog.OperationIdentity}, resolved once
 * per compiled navigation link by {@link CollectionOperationExecutors} and cached on the executable node.
 */
@FunctionalInterface
public interface CollectionOperationExecutor {

    Object execute(
            CollectionOperationRuntimeBinding binding,
            Object receiver,
            MathContext mathContext,
            int maxMaterializedSize,
            ExecutableOperationArguments arguments,
            ExecutionScope scope,
            SourceSpan sourceSpan);
}
