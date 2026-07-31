package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.ExpressionType;

import java.util.Objects;

/**
 * Compact resolved data a {@link CollectionOperationExecutor} needs at execution time, extracted once
 * from {@code CollectionOperationBinding} by the plan builder. Unlike the semantic binding, this record
 * never reaches an AST node: {@code CollectionOperationBinding.lambdaBindings()} carries a
 * {@code LambdaNode} per entry, so the executable node must not retain the binding itself.
 *
 * @param sortKeyType the {@code sortBy} selector's result type, used to compare keys; {@code null} for
 *                     every other operation identity, which does not need a key type to execute.
 */
public record CollectionOperationRuntimeBinding(ExpressionType receiverType, ExpressionType sortKeyType) {

    public CollectionOperationRuntimeBinding {
        Objects.requireNonNull(receiverType, "receiverType");
    }
}
