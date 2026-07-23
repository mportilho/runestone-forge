package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ArgumentShape.NO_ARGUMENTS;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ArgumentShape.ONE_LAMBDA;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.CurrentItemTypeRule.NONE;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.CurrentItemTypeRule.RECEIVER_ITEM_OR_MAP_ENTRY;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.EvaluationPolicy.EAGER;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.EvaluationPolicy.SHORT_CIRCUIT_ON_FALSE;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.EvaluationPolicy.SHORT_CIRCUIT_ON_TRUE;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.IntrinsicPurity.PURE;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.LambdaResultConstraint.BOOLEAN_NEVER_NULL;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.LambdaResultConstraint.KNOWN_NEVER_NULL;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.LambdaResultConstraint.NO_LAMBDA_RESULT;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.MaterializationPolicy.DOES_NOT_MATERIALIZE;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.MaterializationPolicy.MATERIALIZES;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.NumericResultFact.INTEGRAL_KNOWN;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.NumericResultFact.NO_NUMERIC_FACT;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.NumericResultFact.UNKNOWN_NUMERIC_VALUE_SHAPE;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ReceiverItemConstraint.ANY_ITEM;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ReceiverItemConstraint.NUMBER_ITEM;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ReceiverKind.COLLECTION;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ReceiverKind.MAP;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ResultTypeRule.BOOLEAN;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ResultTypeRule.COLLECTION_OF_MAP_KEYS;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ResultTypeRule.COLLECTION_OF_MAP_VALUES;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ResultTypeRule.MAPPED_ITEM_CONTAINER;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ResultTypeRule.NUMBER;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ShapePreservationRule.MAP_TO_COLLECTION;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ShapePreservationRule.NOT_APPLICABLE;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ShapePreservationRule.COLLECTION_TO_COLLECTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class CollectionOperationCatalogTest {

    @Test
    @DisplayName("official catalog exposes the complete declarative descriptor table")
    void officialCatalogExposesCompleteDeclarativeDescriptorTable() {
        CollectionOperationCatalog catalog = CollectionOperationCatalog.standard();

        assertThat(catalog.descriptors()).containsExactly(
                descriptor("all", List.of(COLLECTION, MAP), ANY_ITEM, ONE_LAMBDA,
                        RECEIVER_ITEM_OR_MAP_ENTRY, BOOLEAN_NEVER_NULL, BOOLEAN, NO_NUMERIC_FACT, NOT_APPLICABLE,
                        PURE, RuntimeNullability.NEVER_NULL, SHORT_CIRCUIT_ON_FALSE, DOES_NOT_MATERIALIZE),
                descriptor("any", List.of(COLLECTION, MAP), ANY_ITEM, ONE_LAMBDA,
                        RECEIVER_ITEM_OR_MAP_ENTRY, BOOLEAN_NEVER_NULL, BOOLEAN, NO_NUMERIC_FACT, NOT_APPLICABLE,
                        PURE, RuntimeNullability.NEVER_NULL, SHORT_CIRCUIT_ON_TRUE, DOES_NOT_MATERIALIZE),
                descriptor("count", List.of(COLLECTION, MAP), ANY_ITEM, NO_ARGUMENTS,
                        NONE, NO_LAMBDA_RESULT, NUMBER, INTEGRAL_KNOWN, NOT_APPLICABLE,
                        PURE, RuntimeNullability.NEVER_NULL, EAGER, DOES_NOT_MATERIALIZE),
                descriptor("keys", List.of(MAP), ANY_ITEM, NO_ARGUMENTS,
                        NONE, NO_LAMBDA_RESULT, COLLECTION_OF_MAP_KEYS, NO_NUMERIC_FACT, MAP_TO_COLLECTION,
                        PURE, RuntimeNullability.NEVER_NULL, EAGER, MATERIALIZES),
                descriptor("map", List.of(COLLECTION, MAP), ANY_ITEM, ONE_LAMBDA,
                        RECEIVER_ITEM_OR_MAP_ENTRY, KNOWN_NEVER_NULL, MAPPED_ITEM_CONTAINER, NO_NUMERIC_FACT,
                        COLLECTION_TO_COLLECTION,
                        PURE, RuntimeNullability.NEVER_NULL, EAGER, MATERIALIZES),
                descriptor("sum", List.of(COLLECTION), NUMBER_ITEM, NO_ARGUMENTS,
                        NONE, NO_LAMBDA_RESULT, NUMBER, UNKNOWN_NUMERIC_VALUE_SHAPE, NOT_APPLICABLE,
                        PURE, RuntimeNullability.NEVER_NULL, EAGER, DOES_NOT_MATERIALIZE),
                descriptor("values", List.of(MAP), ANY_ITEM, NO_ARGUMENTS,
                        NONE, NO_LAMBDA_RESULT, COLLECTION_OF_MAP_VALUES, NO_NUMERIC_FACT, MAP_TO_COLLECTION,
                        PURE, RuntimeNullability.NEVER_NULL, EAGER, MATERIALIZES));
    }

    @Test
    @DisplayName("lookup is direct, case-sensitive, deterministically ordered, and immutable")
    void lookupIsDirectCaseSensitiveDeterministicallyOrderedAndImmutable() {
        CollectionOperationCatalog catalog = CollectionOperationCatalog.of(new ArrayList<>(List.of(
                descriptor("zeta", List.of(COLLECTION), ANY_ITEM, NO_ARGUMENTS, NONE, NO_LAMBDA_RESULT,
                        NUMBER, UNKNOWN_NUMERIC_VALUE_SHAPE, NOT_APPLICABLE, PURE, RuntimeNullability.NEVER_NULL, EAGER,
                        DOES_NOT_MATERIALIZE),
                descriptor("alpha", List.of(COLLECTION), ANY_ITEM, NO_ARGUMENTS, NONE, NO_LAMBDA_RESULT,
                        NUMBER, UNKNOWN_NUMERIC_VALUE_SHAPE, NOT_APPLICABLE, PURE, RuntimeNullability.NEVER_NULL, EAGER,
                        DOES_NOT_MATERIALIZE))));

        assertThat(catalog.operationNames()).containsExactly("alpha", "zeta");
        assertThat(catalog.find("alpha")).contains(catalog.descriptors().getFirst());
        assertThat(catalog.find("Alpha")).isEmpty();
        assertThatThrownBy(() -> catalog.descriptors().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> catalog.operationNames().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("public API does not expose custom collection operation registration")
    void publicApiDoesNotExposeCustomCollectionOperationRegistration() {
        assertThat(List.of(CollectionOperationCatalog.class.getMethods()))
                .filteredOn(method -> method.getDeclaringClass() == CollectionOperationCatalog.class)
                .extracting(Method::getName)
                .doesNotContain("register", "replace", "builder", "of");
        assertThat(List.of(ExpressionEnvironment.Builder.class.getMethods()))
                .filteredOn(method -> Modifier.isPublic(method.getModifiers()))
                .extracting(Method::getName)
                .doesNotContain("collectionOperation", "collectionOperations", "addStandardCollectionOperations");
    }

    @Test
    @DisplayName("generic construction rejects invalid names, receivers, and duplicate operations")
    void genericConstructionRejectsInvalidNamesReceiversAndDuplicateOperations() {
        assertThatThrownBy(() -> scalarDescriptor(" ", List.of(COLLECTION)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name must not be blank");
        assertThatThrownBy(() -> scalarDescriptor("empty", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("receivers must not be empty");
        assertThatThrownBy(() -> scalarDescriptor("duplicate-receiver", List.of(COLLECTION, COLLECTION)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("receivers must not contain duplicates");

        CollectionOperationCatalog.Descriptor duplicate = scalarDescriptor("duplicate", List.of(COLLECTION));
        assertThatThrownBy(() -> CollectionOperationCatalog.of(List.of(duplicate, duplicate)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("collection operation already registered: duplicate");
    }

    @Test
    @DisplayName("generic construction rejects every missing declarative policy")
    void genericConstructionRejectsEveryMissingDeclarativePolicy() {
        List<Supplier<CollectionOperationCatalog.Descriptor>> missingPolicies = List.of(
                () -> descriptor("invalid", List.of(COLLECTION), null, NO_ARGUMENTS, NONE, NO_LAMBDA_RESULT,
                        NUMBER, UNKNOWN_NUMERIC_VALUE_SHAPE, NOT_APPLICABLE, PURE, RuntimeNullability.NEVER_NULL, EAGER,
                        DOES_NOT_MATERIALIZE),
                () -> descriptor("invalid", List.of(COLLECTION), ANY_ITEM, null, NONE, NO_LAMBDA_RESULT,
                        NUMBER, UNKNOWN_NUMERIC_VALUE_SHAPE, NOT_APPLICABLE, PURE, RuntimeNullability.NEVER_NULL, EAGER,
                        DOES_NOT_MATERIALIZE),
                () -> descriptor("invalid", List.of(COLLECTION), ANY_ITEM, NO_ARGUMENTS, null, NO_LAMBDA_RESULT,
                        NUMBER, UNKNOWN_NUMERIC_VALUE_SHAPE, NOT_APPLICABLE, PURE, RuntimeNullability.NEVER_NULL, EAGER,
                        DOES_NOT_MATERIALIZE),
                () -> descriptor("invalid", List.of(COLLECTION), ANY_ITEM, NO_ARGUMENTS, NONE, null,
                        NUMBER, UNKNOWN_NUMERIC_VALUE_SHAPE, NOT_APPLICABLE, PURE, RuntimeNullability.NEVER_NULL, EAGER,
                        DOES_NOT_MATERIALIZE),
                () -> descriptor("invalid", List.of(COLLECTION), ANY_ITEM, NO_ARGUMENTS, NONE, NO_LAMBDA_RESULT,
                        null, UNKNOWN_NUMERIC_VALUE_SHAPE, NOT_APPLICABLE, PURE, RuntimeNullability.NEVER_NULL, EAGER,
                        DOES_NOT_MATERIALIZE),
                () -> descriptor("invalid", List.of(COLLECTION), ANY_ITEM, NO_ARGUMENTS, NONE, NO_LAMBDA_RESULT,
                        NUMBER, null, NOT_APPLICABLE, PURE, RuntimeNullability.NEVER_NULL, EAGER,
                        DOES_NOT_MATERIALIZE),
                () -> descriptor("invalid", List.of(COLLECTION), ANY_ITEM, NO_ARGUMENTS, NONE, NO_LAMBDA_RESULT,
                        NUMBER, UNKNOWN_NUMERIC_VALUE_SHAPE, null, PURE, RuntimeNullability.NEVER_NULL, EAGER,
                        DOES_NOT_MATERIALIZE),
                () -> descriptor("invalid", List.of(COLLECTION), ANY_ITEM, NO_ARGUMENTS, NONE, NO_LAMBDA_RESULT,
                        NUMBER, UNKNOWN_NUMERIC_VALUE_SHAPE, NOT_APPLICABLE, null, RuntimeNullability.NEVER_NULL, EAGER,
                        DOES_NOT_MATERIALIZE),
                () -> descriptor("invalid", List.of(COLLECTION), ANY_ITEM, NO_ARGUMENTS, NONE, NO_LAMBDA_RESULT,
                        NUMBER, UNKNOWN_NUMERIC_VALUE_SHAPE, NOT_APPLICABLE, PURE, null, EAGER,
                        DOES_NOT_MATERIALIZE),
                () -> descriptor("invalid", List.of(COLLECTION), ANY_ITEM, NO_ARGUMENTS, NONE, NO_LAMBDA_RESULT,
                        NUMBER, UNKNOWN_NUMERIC_VALUE_SHAPE, NOT_APPLICABLE, PURE,
                        RuntimeNullability.NEVER_NULL, null, DOES_NOT_MATERIALIZE),
                () -> descriptor("invalid", List.of(COLLECTION), ANY_ITEM, NO_ARGUMENTS, NONE, NO_LAMBDA_RESULT,
                        NUMBER, UNKNOWN_NUMERIC_VALUE_SHAPE, NOT_APPLICABLE, PURE,
                        RuntimeNullability.NEVER_NULL, EAGER, null));

        assertThat(missingPolicies).allSatisfy(construction -> assertThatThrownBy(construction::get)
                .isInstanceOf(NullPointerException.class));
    }

    @Test
    @DisplayName("generic construction rejects contradictory argument and evaluation policies")
    void genericConstructionRejectsContradictoryArgumentAndEvaluationPolicies() {
        assertThatThrownBy(() -> descriptor("lambda-without-item", List.of(COLLECTION), ANY_ITEM, ONE_LAMBDA,
                NONE, KNOWN_NEVER_NULL, MAPPED_ITEM_CONTAINER, NO_NUMERIC_FACT,
                COLLECTION_TO_COLLECTION,
                PURE, RuntimeNullability.NEVER_NULL, EAGER, MATERIALIZES))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("argument shape");
        assertThatThrownBy(() -> descriptor("arguments-with-lambda-contract", List.of(COLLECTION), ANY_ITEM,
                NO_ARGUMENTS, RECEIVER_ITEM_OR_MAP_ENTRY, KNOWN_NEVER_NULL, NUMBER,
                UNKNOWN_NUMERIC_VALUE_SHAPE, NOT_APPLICABLE, PURE,
                RuntimeNullability.NEVER_NULL, EAGER, DOES_NOT_MATERIALIZE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("argument shape");
        assertThatThrownBy(() -> descriptor("invalid-short-circuit", List.of(COLLECTION), ANY_ITEM, ONE_LAMBDA,
                RECEIVER_ITEM_OR_MAP_ENTRY, KNOWN_NEVER_NULL, BOOLEAN, NO_NUMERIC_FACT, NOT_APPLICABLE,
                PURE, RuntimeNullability.NEVER_NULL, SHORT_CIRCUIT_ON_TRUE, DOES_NOT_MATERIALIZE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("short-circuit evaluation");
    }

    @Test
    @DisplayName("generic construction rejects contradictory receiver, result, shape, and materialization policies")
    void genericConstructionRejectsContradictoryReceiverResultShapeAndMaterializationPolicies() {
        assertThatThrownBy(() -> descriptor("numeric-map", List.of(MAP), NUMBER_ITEM, NO_ARGUMENTS,
                NONE, NO_LAMBDA_RESULT, NUMBER, UNKNOWN_NUMERIC_VALUE_SHAPE, NOT_APPLICABLE,
                PURE, RuntimeNullability.NEVER_NULL, EAGER, DOES_NOT_MATERIALIZE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("numeric receiver constraint");
        assertThatThrownBy(() -> descriptor("scalar-shape", List.of(COLLECTION), ANY_ITEM, NO_ARGUMENTS,
                NONE, NO_LAMBDA_RESULT, NUMBER, UNKNOWN_NUMERIC_VALUE_SHAPE,
                COLLECTION_TO_COLLECTION,
                PURE, RuntimeNullability.NEVER_NULL, EAGER, DOES_NOT_MATERIALIZE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("scalar result");
        assertThatThrownBy(() -> descriptor("scalar-materializes", List.of(COLLECTION), ANY_ITEM, NO_ARGUMENTS,
                NONE, NO_LAMBDA_RESULT, NUMBER, UNKNOWN_NUMERIC_VALUE_SHAPE, NOT_APPLICABLE,
                PURE, RuntimeNullability.NEVER_NULL, EAGER, MATERIALIZES))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("scalar result");
        assertThatThrownBy(() -> descriptor("keys-on-collection", List.of(COLLECTION), ANY_ITEM, NO_ARGUMENTS,
                NONE, NO_LAMBDA_RESULT, COLLECTION_OF_MAP_KEYS, NO_NUMERIC_FACT, MAP_TO_COLLECTION,
                PURE, RuntimeNullability.NEVER_NULL, EAGER, MATERIALIZES))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("map collection result");
        assertThatThrownBy(() -> descriptor("non-materialized-map", List.of(COLLECTION), ANY_ITEM,
                ONE_LAMBDA, RECEIVER_ITEM_OR_MAP_ENTRY, KNOWN_NEVER_NULL, MAPPED_ITEM_CONTAINER,
                NO_NUMERIC_FACT, COLLECTION_TO_COLLECTION, PURE,
                RuntimeNullability.NEVER_NULL, EAGER, DOES_NOT_MATERIALIZE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mapped result");
    }

    @Test
    @DisplayName("generic construction permits coherent policies beyond the official set")
    void genericConstructionPermitsCoherentPoliciesBeyondTheOfficialSet() {
        CollectionOperationCatalog.Descriptor numericPredicate = descriptor(
                "numeric-predicate", List.of(COLLECTION), NUMBER_ITEM, NO_ARGUMENTS,
                NONE, NO_LAMBDA_RESULT, BOOLEAN, NO_NUMERIC_FACT, NOT_APPLICABLE,
                PURE, RuntimeNullability.NEVER_NULL, EAGER, DOES_NOT_MATERIALIZE);
        CollectionOperationCatalog.Descriptor collectionTransform = descriptor(
                "collection-transform", List.of(COLLECTION), ANY_ITEM, ONE_LAMBDA,
                RECEIVER_ITEM_OR_MAP_ENTRY, KNOWN_NEVER_NULL, MAPPED_ITEM_CONTAINER, NO_NUMERIC_FACT,
                COLLECTION_TO_COLLECTION, PURE, RuntimeNullability.NEVER_NULL, EAGER, MATERIALIZES);

        assertThat(CollectionOperationCatalog.of(List.of(numericPredicate, collectionTransform)).operationNames())
                .containsExactly("collection-transform", "numeric-predicate");
    }

    @Test
    @DisplayName("environment accepts only the exact official catalog through its internal seam")
    void environmentAcceptsOnlyExactOfficialCatalogThroughInternalSeam() {
        List<CollectionOperationCatalog.Descriptor> official = CollectionOperationCatalog.standard().descriptors();
        CollectionOperationCatalog incomplete = CollectionOperationCatalog.of(official.subList(0, official.size() - 1));
        List<CollectionOperationCatalog.Descriptor> additionalDescriptors = new ArrayList<>(official);
        additionalDescriptors.add(scalarDescriptor("extra", List.of(COLLECTION)));
        CollectionOperationCatalog additional = CollectionOperationCatalog.of(additionalDescriptors);
        List<CollectionOperationCatalog.Descriptor> alteredDescriptors = new ArrayList<>(official);
        CollectionOperationCatalog.Descriptor sum = CollectionOperationCatalog.standard().find("sum").orElseThrow();
        alteredDescriptors.set(alteredDescriptors.indexOf(sum), copyWithPurity(sum, CollectionOperationCatalog.IntrinsicPurity.IMPURE));
        CollectionOperationCatalog altered = CollectionOperationCatalog.of(alteredDescriptors);

        assertThat(ExpressionEnvironment.builder().build(CollectionOperationCatalog.standard()).collectionOperations())
                .isSameAs(CollectionOperationCatalog.standard());
        assertThatThrownBy(() -> ExpressionEnvironment.builder().build(incomplete))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exact official descriptors");
        assertThatThrownBy(() -> ExpressionEnvironment.builder().build(additional))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exact official descriptors");
        assertThatThrownBy(() -> ExpressionEnvironment.builder().build(altered))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exact official descriptors");
    }

    @Test
    @DisplayName("every environment automatically installs the official catalog")
    void everyEnvironmentAutomaticallyInstallsOfficialCatalog() {
        assertThat(ExpressionEnvironment.standard().collectionOperations())
                .isSameAs(CollectionOperationCatalog.standard());
        assertThat(ExpressionEnvironment.builder().build().collectionOperations())
                .isSameAs(CollectionOperationCatalog.standard());
        assertThat(ExpressionEnvironment.builder().maxMaterializedSize(1).build().collectionOperations())
                .isSameAs(CollectionOperationCatalog.standard());
    }

    private static CollectionOperationCatalog.Descriptor scalarDescriptor(
            String name,
            List<CollectionOperationCatalog.ReceiverKind> receivers) {
        return descriptor(name, receivers, ANY_ITEM, NO_ARGUMENTS, NONE, NO_LAMBDA_RESULT,
                NUMBER, UNKNOWN_NUMERIC_VALUE_SHAPE, NOT_APPLICABLE, PURE,
                RuntimeNullability.NEVER_NULL, EAGER, DOES_NOT_MATERIALIZE);
    }

    private static CollectionOperationCatalog.Descriptor copyWithPurity(
            CollectionOperationCatalog.Descriptor source,
            CollectionOperationCatalog.IntrinsicPurity purity) {
        return descriptor(
                source.name(), source.receivers(), source.receiverItemConstraint(), source.argumentShape(),
                source.currentItemTypeRule(), source.lambdaResultConstraint(), source.resultTypeRule(),
                source.numericResultFact(), source.shapePreservationRule(), purity,
                source.resultNullability(), source.evaluationPolicy(),
                source.materializationPolicy());
    }

    private static CollectionOperationCatalog.Descriptor descriptor(
            String name,
            List<CollectionOperationCatalog.ReceiverKind> receivers,
            CollectionOperationCatalog.ReceiverItemConstraint receiverItemConstraint,
            CollectionOperationCatalog.ArgumentShape argumentShape,
            CollectionOperationCatalog.CurrentItemTypeRule currentItemTypeRule,
            CollectionOperationCatalog.LambdaResultConstraint lambdaResultConstraint,
            CollectionOperationCatalog.ResultTypeRule resultTypeRule,
            CollectionOperationCatalog.NumericResultFact numericResultFact,
            CollectionOperationCatalog.ShapePreservationRule shapePreservationRule,
            CollectionOperationCatalog.IntrinsicPurity intrinsicPurity,
            RuntimeNullability resultNullability,
            CollectionOperationCatalog.EvaluationPolicy evaluationPolicy,
            CollectionOperationCatalog.MaterializationPolicy materializationPolicy) {
        return new CollectionOperationCatalog.Descriptor(
                name,
                receivers,
                receiverItemConstraint,
                argumentShape,
                currentItemTypeRule,
                lambdaResultConstraint,
                resultTypeRule,
                numericResultFact,
                shapePreservationRule,
                intrinsicPurity,
                resultNullability,
                evaluationPolicy,
                materializationPolicy);
    }
}
