package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ArgumentKind.LAMBDA;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ArgumentKind.VALUE;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ArgumentTypeRule.ANY_KNOWN_TYPE;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ArgumentTypeRule.BOOLEAN;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ArgumentTypeRule.ORDERABLE_SCALAR;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ArgumentTypeRule.SAME_AS_ARGUMENT_0;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ArgumentTypeRule.STRING;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.CardinalityPreservation.NOT_APPLICABLE;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.CardinalityPreservation.PRESERVES_RECEIVER_CARDINALITY;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.CurrentItemTypeRule.NONE;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.CurrentItemTypeRule.RECEIVER_ITEM_OR_MAP_ENTRY;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.CurrentItemTypeRule.REDUCTION_ITEM;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.EvaluationPolicy.EAGER;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.EvaluationPolicy.SHORT_CIRCUIT_ON_FALSE;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.EvaluationPolicy.SHORT_CIRCUIT_ON_TRUE;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.IntrinsicPurity.PURE;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.MaterializationPolicy.DOES_NOT_MATERIALIZE;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.MaterializationPolicy.MATERIALIZES;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.NumericComputationPolicy.COMMON_MATH_CONTEXT;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.NumericComputationPolicy.NO_SPECIAL_POLICY;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.NumericResultFact.INTEGRAL_KNOWN;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.NumericResultFact.NO_NUMERIC_FACT;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.NumericResultFact.PROPAGATES_ARGUMENT_0;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.NumericResultFact.UNKNOWN_NUMERIC_VALUE_SHAPE;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ReceiverItemConstraint.ANY_ITEM;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ReceiverItemConstraint.NUMBER_ITEM;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ReceiverKind.COLLECTION;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ReceiverKind.MAP;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ResultTypeRule.BOOLEAN_RESULT;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ResultTypeRule.COLLECTION_OF_LAMBDA_RESULT;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ResultTypeRule.COLLECTION_OF_MAP_KEYS;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ResultTypeRule.COLLECTION_OF_MAP_VALUES;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ResultTypeRule.NUMBER_RESULT;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ResultTypeRule.SAME_AS_VALUE_ARGUMENT_0;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ResultTypeRule.SAME_AS_RECEIVER_COLLECTION;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ValueConstraint.NONE_REQUIRED;
import static com.runestone.expeval_mk3.api.CollectionOperationCatalog.ValueConstraint.SORT_DIRECTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class CollectionOperationCatalogTest {

    @Test
    @DisplayName("official catalog exposes exactly ten complete declarative descriptors")
    void officialCatalogExposesExactlyTenCompleteDeclarativeDescriptors() {
        CollectionOperationCatalog catalog = CollectionOperationCatalog.standard();

        assertThat(catalog.descriptors()).containsExactly(
                descriptor("all", List.of(COLLECTION, MAP), ANY_ITEM,
                        List.of(lambda(BOOLEAN, RECEIVER_ITEM_OR_MAP_ENTRY)), BOOLEAN_RESULT,
                        NO_NUMERIC_FACT, NOT_APPLICABLE, SHORT_CIRCUIT_ON_FALSE, DOES_NOT_MATERIALIZE,
                        NO_SPECIAL_POLICY),
                descriptor("any", List.of(COLLECTION, MAP), ANY_ITEM,
                        List.of(lambda(BOOLEAN, RECEIVER_ITEM_OR_MAP_ENTRY)), BOOLEAN_RESULT,
                        NO_NUMERIC_FACT, NOT_APPLICABLE, SHORT_CIRCUIT_ON_TRUE, DOES_NOT_MATERIALIZE,
                        NO_SPECIAL_POLICY),
                descriptor("avg", List.of(COLLECTION), NUMBER_ITEM, List.of(), NUMBER_RESULT,
                        UNKNOWN_NUMERIC_VALUE_SHAPE, NOT_APPLICABLE, EAGER, DOES_NOT_MATERIALIZE,
                        COMMON_MATH_CONTEXT),
                descriptor("count", List.of(COLLECTION, MAP), ANY_ITEM, List.of(), NUMBER_RESULT,
                        INTEGRAL_KNOWN, NOT_APPLICABLE, EAGER, DOES_NOT_MATERIALIZE, NO_SPECIAL_POLICY),
                descriptor("keys", List.of(MAP), ANY_ITEM, List.of(), COLLECTION_OF_MAP_KEYS,
                        NO_NUMERIC_FACT, PRESERVES_RECEIVER_CARDINALITY, EAGER, MATERIALIZES,
                        NO_SPECIAL_POLICY),
                descriptor("map", List.of(COLLECTION, MAP), ANY_ITEM,
                        List.of(lambda(ANY_KNOWN_TYPE, RECEIVER_ITEM_OR_MAP_ENTRY)), COLLECTION_OF_LAMBDA_RESULT,
                        NO_NUMERIC_FACT, PRESERVES_RECEIVER_CARDINALITY, EAGER, MATERIALIZES,
                        NO_SPECIAL_POLICY),
                descriptor("reduce", List.of(COLLECTION), ANY_ITEM,
                        List.of(value(ANY_KNOWN_TYPE, NONE_REQUIRED), lambda(SAME_AS_ARGUMENT_0, REDUCTION_ITEM)),
                        SAME_AS_VALUE_ARGUMENT_0, PROPAGATES_ARGUMENT_0, NOT_APPLICABLE, EAGER, DOES_NOT_MATERIALIZE,
                        NO_SPECIAL_POLICY),
                descriptor("sortBy", List.of(COLLECTION), ANY_ITEM,
                        List.of(lambda(ORDERABLE_SCALAR, RECEIVER_ITEM_OR_MAP_ENTRY), value(STRING, SORT_DIRECTION)),
                        SAME_AS_RECEIVER_COLLECTION, NO_NUMERIC_FACT, PRESERVES_RECEIVER_CARDINALITY, EAGER,
                        MATERIALIZES, NO_SPECIAL_POLICY),
                descriptor("sum", List.of(COLLECTION), NUMBER_ITEM, List.of(), NUMBER_RESULT,
                        UNKNOWN_NUMERIC_VALUE_SHAPE, NOT_APPLICABLE, EAGER, DOES_NOT_MATERIALIZE,
                        NO_SPECIAL_POLICY),
                descriptor("values", List.of(MAP), ANY_ITEM, List.of(), COLLECTION_OF_MAP_VALUES,
                        NO_NUMERIC_FACT, PRESERVES_RECEIVER_CARDINALITY, EAGER, MATERIALIZES,
                        NO_SPECIAL_POLICY));
    }

    @Test
    @DisplayName("reduce and sortBy retain mixed argument contracts in source order")
    void reduceAndSortByRetainMixedArgumentContractsInSourceOrder() {
        assertThat(CollectionOperationCatalog.standard().find("reduce").orElseThrow().arguments())
                .containsExactly(
                        value(ANY_KNOWN_TYPE, NONE_REQUIRED),
                        lambda(SAME_AS_ARGUMENT_0, REDUCTION_ITEM));
        assertThat(CollectionOperationCatalog.standard().find("sortBy").orElseThrow().arguments())
                .containsExactly(
                        lambda(ORDERABLE_SCALAR, RECEIVER_ITEM_OR_MAP_ENTRY),
                        value(STRING, SORT_DIRECTION));
        assertThat(ORDERABLE_SCALAR.concreteTypes())
                .containsExactlyInAnyOrder(
                        ScalarType.NUMBER, ScalarType.STRING, ScalarType.DATE, ScalarType.TIME, ScalarType.DATETIME);
        assertThat(SORT_DIRECTION.allowedTextValues()).isEqualTo(Set.of("asc", "desc"));
    }

    @Test
    @DisplayName("catalog is immutable, ordered, and case-sensitive")
    void catalogIsImmutableOrderedAndCaseSensitive() {
        CollectionOperationCatalog catalog = CollectionOperationCatalog.standard();

        assertThat(catalog.operationNames())
                .containsExactly("all", "any", "avg", "count", "keys", "map", "reduce", "sortBy", "sum", "values");
        assertThat(catalog.find("sortBy")).isPresent();
        assertThat(catalog.find("SortBy")).isEmpty();
        assertThatThrownBy(() -> catalog.descriptors().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> catalog.find("reduce").orElseThrow().arguments().clear())
                .isInstanceOf(UnsupportedOperationException.class);
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
    @DisplayName("construction rejects duplicates, incomplete official sets, and altered contracts")
    void constructionRejectsDuplicatesIncompleteOfficialSetsAndAlteredContracts() {
        List<CollectionOperationCatalog.Descriptor> official = CollectionOperationCatalog.standard().descriptors();
        CollectionOperationCatalog.Descriptor duplicate = official.getFirst();

        assertThatThrownBy(() -> CollectionOperationCatalog.of(List.of(duplicate, duplicate)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("collection operation already registered: all");

        CollectionOperationCatalog incomplete = CollectionOperationCatalog.of(official.subList(0, official.size() - 1));
        List<CollectionOperationCatalog.Descriptor> additionalDescriptors = new ArrayList<>(official);
        CollectionOperationCatalog.Descriptor count = CollectionOperationCatalog.standard().find("count").orElseThrow();
        additionalDescriptors.add(descriptor(
                "extra", count.receivers(), count.receiverItemConstraint(), count.arguments(), count.resultTypeRule(),
                count.numericResultFact(), count.cardinalityPreservation(), count.evaluationPolicy(),
                count.materializationPolicy(), count.numericComputationPolicy()));
        List<CollectionOperationCatalog.Descriptor> alteredDescriptors = new ArrayList<>(official);
        alteredDescriptors.set(alteredDescriptors.indexOf(count), descriptor(
                count.name(), count.receivers(), count.receiverItemConstraint(), count.arguments(), count.resultTypeRule(),
                count.numericResultFact(), count.cardinalityPreservation(), count.evaluationPolicy(),
                count.materializationPolicy(), count.numericComputationPolicy(),
                CollectionOperationCatalog.IntrinsicPurity.IMPURE));

        assertThatThrownBy(() -> ExpressionEnvironment.builder().build(incomplete))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exact official descriptors");
        assertThatThrownBy(() -> ExpressionEnvironment.builder().build(CollectionOperationCatalog.of(additionalDescriptors)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exact official descriptors");
        assertThatThrownBy(() -> ExpressionEnvironment.builder().build(CollectionOperationCatalog.of(alteredDescriptors)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exact official descriptors");
    }

    @Test
    @DisplayName("construction rejects inconsistent argument and result rules")
    void constructionRejectsInconsistentArgumentAndResultRules() {
        assertThatThrownBy(() -> new CollectionOperationCatalog.ArgumentContract(
                VALUE, STRING, RuntimeNullability.NEVER_NULL, RECEIVER_ITEM_OR_MAP_ENTRY, NONE_REQUIRED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("value argument cannot declare a current item");
        assertThatThrownBy(() -> new CollectionOperationCatalog.ArgumentContract(
                LAMBDA, BOOLEAN, RuntimeNullability.NEVER_NULL, NONE, NONE_REQUIRED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lambda argument requires a current item");
        assertThatThrownBy(() -> new CollectionOperationCatalog.ArgumentContract(
                LAMBDA, BOOLEAN, RuntimeNullability.NEVER_NULL, RECEIVER_ITEM_OR_MAP_ENTRY, SORT_DIRECTION))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lambda argument cannot declare a value constraint");
        assertThatThrownBy(() -> descriptor("bad-short-circuit", List.of(COLLECTION), ANY_ITEM,
                List.of(lambda(ANY_KNOWN_TYPE, RECEIVER_ITEM_OR_MAP_ENTRY)), BOOLEAN_RESULT,
                NO_NUMERIC_FACT, NOT_APPLICABLE, SHORT_CIRCUIT_ON_TRUE, DOES_NOT_MATERIALIZE,
                NO_SPECIAL_POLICY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("short-circuit evaluation");
        CollectionOperationCatalog.ArgumentContract nullablePredicate = new CollectionOperationCatalog.ArgumentContract(
                LAMBDA, BOOLEAN, RuntimeNullability.MAY_BE_NULL, RECEIVER_ITEM_OR_MAP_ENTRY, NONE_REQUIRED);
        assertThatThrownBy(() -> descriptor("nullable-predicate", List.of(COLLECTION), ANY_ITEM,
                List.of(nullablePredicate), BOOLEAN_RESULT, NO_NUMERIC_FACT, NOT_APPLICABLE,
                SHORT_CIRCUIT_ON_FALSE, DOES_NOT_MATERIALIZE, NO_SPECIAL_POLICY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("short-circuit evaluation");
        CollectionOperationCatalog.ArgumentContract nullableInitialValue = new CollectionOperationCatalog.ArgumentContract(
                VALUE, ANY_KNOWN_TYPE, RuntimeNullability.MAY_BE_NULL, NONE, NONE_REQUIRED);
        assertThatThrownBy(() -> descriptor("nullable-initial", List.of(COLLECTION), ANY_ITEM,
                List.of(nullableInitialValue, lambda(SAME_AS_ARGUMENT_0, REDUCTION_ITEM)),
                SAME_AS_VALUE_ARGUMENT_0, PROPAGATES_ARGUMENT_0, NOT_APPLICABLE, EAGER,
                DOES_NOT_MATERIALIZE, NO_SPECIAL_POLICY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("initial value and reduction lambda");
        CollectionOperationCatalog.ArgumentContract nullableSelector = new CollectionOperationCatalog.ArgumentContract(
                LAMBDA, ORDERABLE_SCALAR, RuntimeNullability.MAY_BE_NULL,
                RECEIVER_ITEM_OR_MAP_ENTRY, NONE_REQUIRED);
        assertThatThrownBy(() -> descriptor("nullable-selector", List.of(COLLECTION), ANY_ITEM,
                List.of(nullableSelector, value(STRING, SORT_DIRECTION)), SAME_AS_RECEIVER_COLLECTION,
                NO_NUMERIC_FACT, PRESERVES_RECEIVER_CARDINALITY, EAGER, MATERIALIZES, NO_SPECIAL_POLICY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderable selector and sort direction");
        assertThatThrownBy(() -> descriptor("bad-map", List.of(COLLECTION), ANY_ITEM, List.of(),
                COLLECTION_OF_LAMBDA_RESULT, NO_NUMERIC_FACT, PRESERVES_RECEIVER_CARDINALITY, EAGER, MATERIALIZES,
                NO_SPECIAL_POLICY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lambda result collection");
        assertThatThrownBy(() -> descriptor("bad-numeric", List.of(MAP), NUMBER_ITEM, List.of(), NUMBER_RESULT,
                UNKNOWN_NUMERIC_VALUE_SHAPE, NOT_APPLICABLE, EAGER, DOES_NOT_MATERIALIZE,
                NO_SPECIAL_POLICY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("numeric receiver constraint");
    }

    @Test
    @DisplayName("every environment installs operations separately from global functions")
    void everyEnvironmentInstallsOperationsSeparatelyFromGlobalFunctions() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();

        assertThat(environment.collectionOperations()).isSameAs(CollectionOperationCatalog.standard());
        assertThat(environment.functions().values())
                .extracting(FunctionDescriptor::languageName)
                .doesNotContain("all", "any", "count", "keys", "map", "sum", "values", "avg", "reduce", "sortBy");
    }

    private static CollectionOperationCatalog.ArgumentContract value(
            CollectionOperationCatalog.ArgumentTypeRule typeRule,
            CollectionOperationCatalog.ValueConstraint valueConstraint) {
        return new CollectionOperationCatalog.ArgumentContract(
                VALUE, typeRule, RuntimeNullability.NEVER_NULL, NONE, valueConstraint);
    }

    private static CollectionOperationCatalog.ArgumentContract lambda(
            CollectionOperationCatalog.ArgumentTypeRule resultTypeRule,
            CollectionOperationCatalog.CurrentItemTypeRule currentItemTypeRule) {
        return new CollectionOperationCatalog.ArgumentContract(
                LAMBDA, resultTypeRule, RuntimeNullability.NEVER_NULL, currentItemTypeRule, NONE_REQUIRED);
    }

    private static CollectionOperationCatalog.Descriptor descriptor(
            String name,
            List<CollectionOperationCatalog.ReceiverKind> receivers,
            CollectionOperationCatalog.ReceiverItemConstraint receiverItemConstraint,
            List<CollectionOperationCatalog.ArgumentContract> arguments,
            CollectionOperationCatalog.ResultTypeRule resultTypeRule,
            CollectionOperationCatalog.NumericResultFact numericResultFact,
            CollectionOperationCatalog.CardinalityPreservation cardinalityPreservation,
            CollectionOperationCatalog.EvaluationPolicy evaluationPolicy,
            CollectionOperationCatalog.MaterializationPolicy materializationPolicy,
            CollectionOperationCatalog.NumericComputationPolicy numericComputationPolicy) {
        return descriptor(name, receivers, receiverItemConstraint, arguments, resultTypeRule, numericResultFact,
                cardinalityPreservation, evaluationPolicy, materializationPolicy, numericComputationPolicy, PURE);
    }

    private static CollectionOperationCatalog.Descriptor descriptor(
            String name,
            List<CollectionOperationCatalog.ReceiverKind> receivers,
            CollectionOperationCatalog.ReceiverItemConstraint receiverItemConstraint,
            List<CollectionOperationCatalog.ArgumentContract> arguments,
            CollectionOperationCatalog.ResultTypeRule resultTypeRule,
            CollectionOperationCatalog.NumericResultFact numericResultFact,
            CollectionOperationCatalog.CardinalityPreservation cardinalityPreservation,
            CollectionOperationCatalog.EvaluationPolicy evaluationPolicy,
            CollectionOperationCatalog.MaterializationPolicy materializationPolicy,
            CollectionOperationCatalog.NumericComputationPolicy numericComputationPolicy,
            CollectionOperationCatalog.IntrinsicPurity purity) {
        return new CollectionOperationCatalog.Descriptor(
                name, receivers, receiverItemConstraint, arguments, resultTypeRule, numericResultFact,
                cardinalityPreservation, purity, RuntimeNullability.NEVER_NULL, evaluationPolicy,
                materializationPolicy, numericComputationPolicy);
    }
}
