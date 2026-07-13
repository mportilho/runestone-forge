package com.runestone.expeval_mk3.api;

import java.util.List;
import java.util.Objects;

/**
 * Catalogo de Operacoes de Colecao available to semantic resolution.
 */
public final class CollectionOperationCatalog {

    private static final CollectionOperationCatalog STANDARD = new CollectionOperationCatalog(List.of(
            new Descriptor(
                    "all",
                    List.of(ReceiverKind.VECTOR, ReceiverKind.COLLECTION, ReceiverKind.MAP),
                    CurrentItemContract.PREDICATE_ITEM,
                    ResultShape.SCALAR_BOOLEAN,
                    EvaluationPolicy.LAZY_PER_ITEM,
                    MaterializationPolicy.DOES_NOT_MATERIALIZE),
            new Descriptor(
                    "any",
                    List.of(ReceiverKind.VECTOR, ReceiverKind.COLLECTION, ReceiverKind.MAP),
                    CurrentItemContract.PREDICATE_ITEM,
                    ResultShape.SCALAR_BOOLEAN,
                    EvaluationPolicy.LAZY_PER_ITEM,
                    MaterializationPolicy.DOES_NOT_MATERIALIZE),
            new Descriptor(
                    "count",
                    List.of(ReceiverKind.VECTOR, ReceiverKind.COLLECTION, ReceiverKind.MAP),
                    CurrentItemContract.NONE,
                    ResultShape.SCALAR_NUMBER,
                    EvaluationPolicy.EAGER,
                    MaterializationPolicy.DOES_NOT_MATERIALIZE),
            new Descriptor(
                    "keys",
                    List.of(ReceiverKind.MAP),
                    CurrentItemContract.NONE,
                    ResultShape.COLLECTION_OF_KEYS,
                    EvaluationPolicy.EAGER,
                    MaterializationPolicy.MATERIALIZES),
            new Descriptor(
                    "map",
                    List.of(ReceiverKind.VECTOR, ReceiverKind.COLLECTION, ReceiverKind.MAP),
                    CurrentItemContract.TRANSFORM_ITEM,
                    ResultShape.PRESERVES_SEQUENCE_OR_MAP_TO_COLLECTION,
                    EvaluationPolicy.EAGER,
                    MaterializationPolicy.MATERIALIZES),
            new Descriptor(
                    "sum",
                    List.of(ReceiverKind.VECTOR, ReceiverKind.COLLECTION),
                    CurrentItemContract.OPTIONAL_TRANSFORM_ITEM,
                    ResultShape.SCALAR_NUMBER,
                    EvaluationPolicy.EAGER,
                    MaterializationPolicy.DOES_NOT_MATERIALIZE),
            new Descriptor(
                    "values",
                    List.of(ReceiverKind.MAP),
                    CurrentItemContract.NONE,
                    ResultShape.COLLECTION_OF_VALUES,
                    EvaluationPolicy.EAGER,
                    MaterializationPolicy.MATERIALIZES)));

    private final List<Descriptor> descriptors;

    private CollectionOperationCatalog(List<Descriptor> descriptors) {
        this.descriptors = List.copyOf(descriptors);
    }

    public static CollectionOperationCatalog standard() {
        return STANDARD;
    }

    public List<Descriptor> descriptors() {
        return descriptors;
    }

    public List<String> operationNames() {
        return descriptors.stream().map(Descriptor::name).toList();
    }

    public boolean contains(String operationName) {
        Objects.requireNonNull(operationName, "operationName");
        return descriptors.stream().anyMatch(descriptor -> descriptor.name().equals(operationName));
    }

    public int size() {
        return descriptors.size();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CollectionOperationCatalog that)) {
            return false;
        }
        return descriptors.equals(that.descriptors);
    }

    @Override
    public int hashCode() {
        return descriptors.hashCode();
    }

    @Override
    public String toString() {
        return "CollectionOperationCatalog[size=" + descriptors.size() + ']';
    }

    public record Descriptor(
            String name,
            List<ReceiverKind> receivers,
            CurrentItemContract currentItemContract,
            ResultShape resultShape,
            EvaluationPolicy evaluationPolicy,
            MaterializationPolicy materializationPolicy) {

        public Descriptor {
            Objects.requireNonNull(name, "name");
            if (name.isBlank()) {
                throw new IllegalArgumentException("name must not be blank");
            }
            Objects.requireNonNull(receivers, "receivers");
            receivers = List.copyOf(receivers);
            if (receivers.isEmpty()) {
                throw new IllegalArgumentException("receivers must not be empty");
            }
            Objects.requireNonNull(currentItemContract, "currentItemContract");
            Objects.requireNonNull(resultShape, "resultShape");
            Objects.requireNonNull(evaluationPolicy, "evaluationPolicy");
            Objects.requireNonNull(materializationPolicy, "materializationPolicy");
        }
    }

    public enum ReceiverKind {
        VECTOR,
        COLLECTION,
        MAP
    }

    public enum CurrentItemContract {
        NONE,
        PREDICATE_ITEM,
        TRANSFORM_ITEM,
        OPTIONAL_TRANSFORM_ITEM
    }

    public enum ResultShape {
        SCALAR_BOOLEAN,
        SCALAR_NUMBER,
        COLLECTION_OF_KEYS,
        COLLECTION_OF_VALUES,
        PRESERVES_SEQUENCE_OR_MAP_TO_COLLECTION
    }

    public enum EvaluationPolicy {
        EAGER,
        LAZY_PER_ITEM
    }

    public enum MaterializationPolicy {
        DOES_NOT_MATERIALIZE,
        MATERIALIZES
    }
}
