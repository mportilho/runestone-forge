package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.CollectionOperationCatalog;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.RuntimeNullability;
import com.runestone.expeval_mk3.internal.ast.LambdaNode;

import java.util.List;
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
        CollectionOperationCatalog.CardinalityPreservation cardinalityPreservation,
        List<LambdaBinding> lambdaBindings) implements NavigationBinding {

    public CollectionOperationBinding {
        Objects.requireNonNull(identity, "identity");
        Objects.requireNonNull(receiverType, "receiverType");
        Objects.requireNonNull(resultType, "resultType");
        Objects.requireNonNull(resultNullability, "resultNullability");
        Objects.requireNonNull(evaluationPolicy, "evaluationPolicy");
        Objects.requireNonNull(materializationPolicy, "materializationPolicy");
        Objects.requireNonNull(numericResultFact, "numericResultFact");
        Objects.requireNonNull(cardinalityPreservation, "cardinalityPreservation");
        lambdaBindings = List.copyOf(Objects.requireNonNull(lambdaBindings, "lambdaBindings"));
    }

    static CollectionOperationBinding fromDescriptor(
            CollectionOperationCatalog.Descriptor descriptor,
            ExpressionType receiverType,
            ExpressionType resultType,
            RuntimeNullability resultNullability) {
        return fromDescriptor(descriptor, receiverType, resultType, resultNullability, List.of());
    }

    static CollectionOperationBinding fromDescriptor(
            CollectionOperationCatalog.Descriptor descriptor,
            ExpressionType receiverType,
            ExpressionType resultType,
            RuntimeNullability resultNullability,
            List<LambdaBinding> lambdaBindings) {
        return fromDescriptor(descriptor, receiverType, resultType, resultNullability, lambdaBindings,
                descriptor.intrinsicPurity() == CollectionOperationCatalog.IntrinsicPurity.PURE);
    }

    static CollectionOperationBinding fromDescriptor(
            CollectionOperationCatalog.Descriptor descriptor,
            ExpressionType receiverType,
            ExpressionType resultType,
            RuntimeNullability resultNullability,
            List<LambdaBinding> lambdaBindings,
            boolean pure) {
        Objects.requireNonNull(descriptor, "descriptor");
        return new CollectionOperationBinding(
                descriptor.identity(),
                receiverType,
                resultType,
                resultNullability,
                descriptor.evaluationPolicy(),
                pure,
                descriptor.materializationPolicy(),
                descriptor.numericResultFact(),
                descriptor.cardinalityPreservation(),
                lambdaBindings);
    }

    public record LambdaBinding(
            LambdaNode lambda,
            ExpressionType resultType,
            RuntimeNullability resultNullability,
            int currentItemFrameSlot,
            boolean pure) {

        public LambdaBinding {
            Objects.requireNonNull(lambda, "lambda");
            Objects.requireNonNull(resultType, "resultType");
            Objects.requireNonNull(resultNullability, "resultNullability");
            if (currentItemFrameSlot < 0) {
                throw new IllegalArgumentException("currentItemFrameSlot must not be negative");
            }
        }
    }
}
