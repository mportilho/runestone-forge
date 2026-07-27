package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.CollectionOperationCatalog;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.RuntimeNullability;

import java.util.Objects;

public record CollectionOperationBinding(
        CollectionOperationCatalog.OperationIdentity identity,
        ExpressionType receiverType,
        ExpressionType resultType,
        RuntimeNullability resultNullability,
        CollectionOperationCatalog.EvaluationPolicy evaluationPolicy,
        boolean pure,
        CollectionOperationCatalog.MaterializationPolicy materializationPolicy,
        CollectionOperationCatalog.NumericResultFact numericResultFact,
        CollectionOperationCatalog.CardinalityPreservation cardinalityPreservation) {

    public CollectionOperationBinding {
        Objects.requireNonNull(identity, "identity");
        Objects.requireNonNull(receiverType, "receiverType");
        Objects.requireNonNull(resultType, "resultType");
        Objects.requireNonNull(resultNullability, "resultNullability");
        Objects.requireNonNull(evaluationPolicy, "evaluationPolicy");
        Objects.requireNonNull(materializationPolicy, "materializationPolicy");
        Objects.requireNonNull(numericResultFact, "numericResultFact");
        Objects.requireNonNull(cardinalityPreservation, "cardinalityPreservation");
    }

    static CollectionOperationBinding fromDescriptor(
            CollectionOperationCatalog.Descriptor descriptor,
            ExpressionType receiverType,
            ExpressionType resultType,
            RuntimeNullability resultNullability) {
        Objects.requireNonNull(descriptor, "descriptor");
        return new CollectionOperationBinding(
                descriptor.identity(),
                receiverType,
                resultType,
                resultNullability,
                descriptor.evaluationPolicy(),
                descriptor.intrinsicPurity() == CollectionOperationCatalog.IntrinsicPurity.PURE,
                descriptor.materializationPolicy(),
                descriptor.numericResultFact(),
                descriptor.cardinalityPreservation());
    }
}
