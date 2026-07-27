package com.runestone.expeval_mk3.api;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/**
 * Immutable Catalogo de Operacoes de Colecao available to semantic resolution.
 */
public final class CollectionOperationCatalog {

    private static final List<Descriptor> OFFICIAL_DESCRIPTORS = List.of(
            descriptor("all", List.of(ReceiverKind.COLLECTION, ReceiverKind.MAP), ReceiverItemConstraint.ANY_ITEM,
                    List.of(lambda(ArgumentTypeRule.BOOLEAN, CurrentItemTypeRule.RECEIVER_ITEM_OR_MAP_ENTRY)),
                    ResultTypeRule.BOOLEAN_RESULT, NumericResultFact.NO_NUMERIC_FACT,
                    CardinalityPreservation.NOT_APPLICABLE, EvaluationPolicy.SHORT_CIRCUIT_ON_FALSE,
                    MaterializationPolicy.DOES_NOT_MATERIALIZE, NumericComputationPolicy.NO_SPECIAL_POLICY),
            descriptor("any", List.of(ReceiverKind.COLLECTION, ReceiverKind.MAP), ReceiverItemConstraint.ANY_ITEM,
                    List.of(lambda(ArgumentTypeRule.BOOLEAN, CurrentItemTypeRule.RECEIVER_ITEM_OR_MAP_ENTRY)),
                    ResultTypeRule.BOOLEAN_RESULT, NumericResultFact.NO_NUMERIC_FACT,
                    CardinalityPreservation.NOT_APPLICABLE, EvaluationPolicy.SHORT_CIRCUIT_ON_TRUE,
                    MaterializationPolicy.DOES_NOT_MATERIALIZE, NumericComputationPolicy.NO_SPECIAL_POLICY),
            descriptor("avg", List.of(ReceiverKind.COLLECTION), ReceiverItemConstraint.NUMBER_ITEM, List.of(),
                    ResultTypeRule.NUMBER_RESULT, NumericResultFact.UNKNOWN_NUMERIC_VALUE_SHAPE,
                    CardinalityPreservation.NOT_APPLICABLE, EvaluationPolicy.EAGER,
                    MaterializationPolicy.DOES_NOT_MATERIALIZE, NumericComputationPolicy.COMMON_MATH_CONTEXT),
            descriptor("count", List.of(ReceiverKind.COLLECTION, ReceiverKind.MAP), ReceiverItemConstraint.ANY_ITEM,
                    List.of(), ResultTypeRule.NUMBER_RESULT, NumericResultFact.INTEGRAL_KNOWN,
                    CardinalityPreservation.NOT_APPLICABLE, EvaluationPolicy.EAGER,
                    MaterializationPolicy.DOES_NOT_MATERIALIZE,
                    NumericComputationPolicy.NO_SPECIAL_POLICY),
            descriptor("keys", List.of(ReceiverKind.MAP), ReceiverItemConstraint.ANY_ITEM, List.of(),
                    ResultTypeRule.COLLECTION_OF_MAP_KEYS, NumericResultFact.NO_NUMERIC_FACT,
                    CardinalityPreservation.PRESERVES_RECEIVER_CARDINALITY, EvaluationPolicy.EAGER,
                    MaterializationPolicy.MATERIALIZES, NumericComputationPolicy.NO_SPECIAL_POLICY),
            descriptor("map", List.of(ReceiverKind.COLLECTION, ReceiverKind.MAP), ReceiverItemConstraint.ANY_ITEM,
                    List.of(lambda(ArgumentTypeRule.ANY_KNOWN_TYPE,
                            CurrentItemTypeRule.RECEIVER_ITEM_OR_MAP_ENTRY)),
                    ResultTypeRule.COLLECTION_OF_LAMBDA_RESULT, NumericResultFact.NO_NUMERIC_FACT,
                    CardinalityPreservation.PRESERVES_RECEIVER_CARDINALITY, EvaluationPolicy.EAGER,
                    MaterializationPolicy.MATERIALIZES, NumericComputationPolicy.NO_SPECIAL_POLICY),
            descriptor("reduce", List.of(ReceiverKind.COLLECTION), ReceiverItemConstraint.ANY_ITEM,
                    List.of(
                            value(ArgumentTypeRule.ANY_KNOWN_TYPE, ValueConstraint.NONE_REQUIRED),
                            lambda(ArgumentTypeRule.SAME_AS_ARGUMENT_0, CurrentItemTypeRule.REDUCTION_ITEM)),
                    ResultTypeRule.SAME_AS_VALUE_ARGUMENT_0, NumericResultFact.PROPAGATES_ARGUMENT_0,
                    CardinalityPreservation.NOT_APPLICABLE, EvaluationPolicy.EAGER,
                    MaterializationPolicy.DOES_NOT_MATERIALIZE,
                    NumericComputationPolicy.NO_SPECIAL_POLICY),
            descriptor("sortBy", List.of(ReceiverKind.COLLECTION), ReceiverItemConstraint.ANY_ITEM,
                    List.of(
                            lambda(ArgumentTypeRule.ORDERABLE_SCALAR,
                                    CurrentItemTypeRule.RECEIVER_ITEM_OR_MAP_ENTRY),
                            value(ArgumentTypeRule.STRING, ValueConstraint.SORT_DIRECTION)),
                    ResultTypeRule.SAME_AS_RECEIVER_COLLECTION, NumericResultFact.NO_NUMERIC_FACT,
                    CardinalityPreservation.PRESERVES_RECEIVER_CARDINALITY, EvaluationPolicy.EAGER,
                    MaterializationPolicy.MATERIALIZES, NumericComputationPolicy.NO_SPECIAL_POLICY),
            descriptor("sum", List.of(ReceiverKind.COLLECTION), ReceiverItemConstraint.NUMBER_ITEM, List.of(),
                    ResultTypeRule.NUMBER_RESULT, NumericResultFact.UNKNOWN_NUMERIC_VALUE_SHAPE,
                    CardinalityPreservation.NOT_APPLICABLE, EvaluationPolicy.EAGER,
                    MaterializationPolicy.DOES_NOT_MATERIALIZE,
                    NumericComputationPolicy.NO_SPECIAL_POLICY),
            descriptor("values", List.of(ReceiverKind.MAP), ReceiverItemConstraint.ANY_ITEM, List.of(),
                    ResultTypeRule.COLLECTION_OF_MAP_VALUES, NumericResultFact.NO_NUMERIC_FACT,
                    CardinalityPreservation.PRESERVES_RECEIVER_CARDINALITY, EvaluationPolicy.EAGER,
                    MaterializationPolicy.MATERIALIZES, NumericComputationPolicy.NO_SPECIAL_POLICY));

    private static final CollectionOperationCatalog STANDARD = of(OFFICIAL_DESCRIPTORS);

    private final Map<String, Descriptor> descriptorsByName;
    private final List<Descriptor> descriptors;

    private CollectionOperationCatalog(Map<String, Descriptor> descriptorsByName) {
        this.descriptorsByName = Map.copyOf(descriptorsByName);
        descriptors = List.copyOf(descriptorsByName.values());
    }

    public static CollectionOperationCatalog standard() {
        return STANDARD;
    }

    static CollectionOperationCatalog of(List<Descriptor> descriptors) {
        Objects.requireNonNull(descriptors, "descriptors");
        Map<String, Descriptor> byName = new TreeMap<>();
        for (Descriptor descriptor : descriptors) {
            Objects.requireNonNull(descriptor, "descriptor");
            if (byName.putIfAbsent(descriptor.name(), descriptor) != null) {
                throw new IllegalArgumentException("collection operation already registered: " + descriptor.name());
            }
        }
        return new CollectionOperationCatalog(byName);
    }

    static void validateOfficial(CollectionOperationCatalog catalog) {
        Objects.requireNonNull(catalog, "catalog");
        if (!catalog.descriptors.equals(OFFICIAL_DESCRIPTORS)) {
            throw new IllegalArgumentException("collection operation catalog must contain the exact official descriptors");
        }
    }

    public Optional<Descriptor> find(String operationName) {
        Objects.requireNonNull(operationName, "operationName");
        return Optional.ofNullable(descriptorsByName.get(operationName));
    }

    public List<Descriptor> descriptors() {
        return descriptors;
    }

    public List<String> operationNames() {
        return descriptors.stream().map(Descriptor::name).toList();
    }

    public boolean contains(String operationName) {
        return find(operationName).isPresent();
    }

    public int size() {
        return descriptors.size();
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof CollectionOperationCatalog that
                && descriptors.equals(that.descriptors);
    }

    @Override
    public int hashCode() {
        return descriptors.hashCode();
    }

    @Override
    public String toString() {
        return "CollectionOperationCatalog[size=" + descriptors.size() + ']';
    }

    private static Descriptor descriptor(
            String name,
            List<ReceiverKind> receivers,
            ReceiverItemConstraint receiverItemConstraint,
            List<ArgumentContract> arguments,
            ResultTypeRule resultTypeRule,
            NumericResultFact numericResultFact,
            CardinalityPreservation cardinalityPreservation,
            EvaluationPolicy evaluationPolicy,
            MaterializationPolicy materializationPolicy,
            NumericComputationPolicy numericComputationPolicy) {
        return new Descriptor(
                name, receivers, receiverItemConstraint, arguments, resultTypeRule, numericResultFact,
                cardinalityPreservation, IntrinsicPurity.PURE, RuntimeNullability.NEVER_NULL,
                evaluationPolicy, materializationPolicy, numericComputationPolicy);
    }

    private static ArgumentContract value(ArgumentTypeRule typeRule, ValueConstraint valueConstraint) {
        return new ArgumentContract(
                ArgumentKind.VALUE, typeRule, RuntimeNullability.NEVER_NULL, CurrentItemTypeRule.NONE,
                valueConstraint);
    }

    private static ArgumentContract lambda(ArgumentTypeRule typeRule, CurrentItemTypeRule currentItemTypeRule) {
        return new ArgumentContract(
                ArgumentKind.LAMBDA, typeRule, RuntimeNullability.NEVER_NULL, currentItemTypeRule,
                ValueConstraint.NONE_REQUIRED);
    }

    /**
     * Declarative Descritor de Operacao de Colecao. It intentionally carries no runtime behavior.
     */
    public static final class Descriptor {

        private final String name;
        private final OperationIdentity identity;
        private final List<ReceiverKind> receivers;
        private final ReceiverItemConstraint receiverItemConstraint;
        private final List<ArgumentContract> arguments;
        private final ResultTypeRule resultTypeRule;
        private final NumericResultFact numericResultFact;
        private final CardinalityPreservation cardinalityPreservation;
        private final IntrinsicPurity intrinsicPurity;
        private final RuntimeNullability resultNullability;
        private final EvaluationPolicy evaluationPolicy;
        private final MaterializationPolicy materializationPolicy;
        private final NumericComputationPolicy numericComputationPolicy;

        Descriptor(
                String name,
                List<ReceiverKind> receivers,
                ReceiverItemConstraint receiverItemConstraint,
                List<ArgumentContract> arguments,
                ResultTypeRule resultTypeRule,
                NumericResultFact numericResultFact,
                CardinalityPreservation cardinalityPreservation,
                IntrinsicPurity intrinsicPurity,
                RuntimeNullability resultNullability,
                EvaluationPolicy evaluationPolicy,
                MaterializationPolicy materializationPolicy,
                NumericComputationPolicy numericComputationPolicy) {
            this.name = requireName(name);
            identity = OperationIdentity.fromName(this.name).orElse(OperationIdentity.CUSTOM);
            this.receivers = requireReceivers(receivers);
            this.receiverItemConstraint = Objects.requireNonNull(receiverItemConstraint, "receiverItemConstraint");
            this.arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments"));
            this.resultTypeRule = Objects.requireNonNull(resultTypeRule, "resultTypeRule");
            this.numericResultFact = Objects.requireNonNull(numericResultFact, "numericResultFact");
            this.cardinalityPreservation = Objects.requireNonNull(
                    cardinalityPreservation, "cardinalityPreservation");
            this.intrinsicPurity = Objects.requireNonNull(intrinsicPurity, "intrinsicPurity");
            this.resultNullability = Objects.requireNonNull(resultNullability, "resultNullability");
            this.evaluationPolicy = Objects.requireNonNull(evaluationPolicy, "evaluationPolicy");
            this.materializationPolicy = Objects.requireNonNull(materializationPolicy, "materializationPolicy");
            this.numericComputationPolicy = Objects.requireNonNull(
                    numericComputationPolicy, "numericComputationPolicy");
            validateConsistency();
        }

        public String name() {
            return name;
        }

        public OperationIdentity identity() {
            return identity;
        }

        public List<ReceiverKind> receivers() {
            return receivers;
        }

        public ReceiverItemConstraint receiverItemConstraint() {
            return receiverItemConstraint;
        }

        public List<ArgumentContract> arguments() {
            return arguments;
        }

        public ResultTypeRule resultTypeRule() {
            return resultTypeRule;
        }

        public NumericResultFact numericResultFact() {
            return numericResultFact;
        }

        public CardinalityPreservation cardinalityPreservation() {
            return cardinalityPreservation;
        }

        public IntrinsicPurity intrinsicPurity() {
            return intrinsicPurity;
        }

        public RuntimeNullability resultNullability() {
            return resultNullability;
        }

        public EvaluationPolicy evaluationPolicy() {
            return evaluationPolicy;
        }

        public MaterializationPolicy materializationPolicy() {
            return materializationPolicy;
        }

        public NumericComputationPolicy numericComputationPolicy() {
            return numericComputationPolicy;
        }

        private void validateConsistency() {
            if (arguments.stream().anyMatch(Objects::isNull)) {
                throw inconsistent("null argument contract");
            }
            validateDependentArguments();
            validateEvaluationPolicy();
            if (receiverItemConstraint == ReceiverItemConstraint.NUMBER_ITEM
                    && !receivers.stream().allMatch(ReceiverKind::isCollection)) {
                throw inconsistent("numeric receiver constraint");
            }
            validateNumericMetadata();
            validateResultPolicy();
        }

        private void validateDependentArguments() {
            for (int index = 0; index < arguments.size(); index++) {
                ArgumentContract argument = arguments.get(index);
                if (argument.typeRule() == ArgumentTypeRule.SAME_AS_ARGUMENT_0
                        && (index == 0 || arguments.getFirst().kind() != ArgumentKind.VALUE)) {
                    throw inconsistent("dependent argument type requires value argument 0");
                }
                if (argument.currentItemTypeRule() == CurrentItemTypeRule.REDUCTION_ITEM
                        && argument.typeRule() != ArgumentTypeRule.SAME_AS_ARGUMENT_0) {
                    throw inconsistent("reduction item lambda result must match argument 0");
                }
            }
        }

        private void validateEvaluationPolicy() {
            if (evaluationPolicy == EvaluationPolicy.EAGER) {
                return;
            }
            if (arguments.size() != 1
                    || arguments.getFirst().kind() != ArgumentKind.LAMBDA
                    || arguments.getFirst().typeRule() != ArgumentTypeRule.BOOLEAN
                    || arguments.getFirst().nullability() != RuntimeNullability.NEVER_NULL
                    || resultTypeRule != ResultTypeRule.BOOLEAN_RESULT) {
                throw inconsistent("short-circuit evaluation requires one boolean lambda and boolean result");
            }
        }

        private void validateNumericMetadata() {
            if (resultTypeRule == ResultTypeRule.NUMBER_RESULT) {
                if (numericResultFact == NumericResultFact.NO_NUMERIC_FACT
                        || numericResultFact == NumericResultFact.PROPAGATES_ARGUMENT_0) {
                    throw inconsistent("numeric result fact and result type");
                }
            } else if (resultTypeRule == ResultTypeRule.SAME_AS_VALUE_ARGUMENT_0) {
                if (numericResultFact != NumericResultFact.PROPAGATES_ARGUMENT_0) {
                    throw inconsistent("argument-derived result must propagate argument 0 numeric fact");
                }
            } else if (numericResultFact != NumericResultFact.NO_NUMERIC_FACT) {
                throw inconsistent("numeric result fact and result type");
            }
            if (numericComputationPolicy == NumericComputationPolicy.COMMON_MATH_CONTEXT
                    && (receiverItemConstraint != ReceiverItemConstraint.NUMBER_ITEM
                    || resultTypeRule != ResultTypeRule.NUMBER_RESULT)) {
                throw inconsistent("common MathContext requires numeric receiver items and result");
            }
        }

        private void validateResultPolicy() {
            switch (resultTypeRule) {
                case BOOLEAN_RESULT, NUMBER_RESULT -> requireScalarResultPolicy();
                case COLLECTION_OF_MAP_KEYS, COLLECTION_OF_MAP_VALUES -> requireMapCollectionResultPolicy();
                case COLLECTION_OF_LAMBDA_RESULT -> requireLambdaCollectionResultPolicy();
                case SAME_AS_VALUE_ARGUMENT_0 -> requireReductionResultPolicy();
                case SAME_AS_RECEIVER_COLLECTION -> requireReceiverCollectionResultPolicy();
            }
        }

        private void requireScalarResultPolicy() {
            if (cardinalityPreservation != CardinalityPreservation.NOT_APPLICABLE
                    || materializationPolicy != MaterializationPolicy.DOES_NOT_MATERIALIZE) {
                throw inconsistent("scalar result cannot preserve cardinality or materialize");
            }
        }

        private void requireMapCollectionResultPolicy() {
            if (!receivers.equals(List.of(ReceiverKind.MAP))
                    || !arguments.isEmpty()
                    || cardinalityPreservation != CardinalityPreservation.PRESERVES_RECEIVER_CARDINALITY
                    || materializationPolicy != MaterializationPolicy.MATERIALIZES) {
                throw inconsistent("map collection result requires only a map receiver and materialization");
            }
        }

        private void requireLambdaCollectionResultPolicy() {
            if (arguments.size() != 1
                    || arguments.getFirst().kind() != ArgumentKind.LAMBDA
                    || arguments.getFirst().typeRule() != ArgumentTypeRule.ANY_KNOWN_TYPE
                    || arguments.getFirst().nullability() != RuntimeNullability.NEVER_NULL
                    || cardinalityPreservation != CardinalityPreservation.PRESERVES_RECEIVER_CARDINALITY
                    || materializationPolicy != MaterializationPolicy.MATERIALIZES) {
                throw inconsistent("lambda result collection requires one non-null transform and materialization");
            }
        }

        private void requireReductionResultPolicy() {
            if (!receivers.equals(List.of(ReceiverKind.COLLECTION))
                    || arguments.size() != 2
                    || arguments.getFirst().kind() != ArgumentKind.VALUE
                    || arguments.getFirst().nullability() != RuntimeNullability.NEVER_NULL
                    || arguments.get(1).kind() != ArgumentKind.LAMBDA
                    || arguments.get(1).typeRule() != ArgumentTypeRule.SAME_AS_ARGUMENT_0
                    || arguments.get(1).nullability() != RuntimeNullability.NEVER_NULL
                    || arguments.get(1).currentItemTypeRule() != CurrentItemTypeRule.REDUCTION_ITEM
                    || cardinalityPreservation != CardinalityPreservation.NOT_APPLICABLE
                    || materializationPolicy != MaterializationPolicy.DOES_NOT_MATERIALIZE) {
                throw inconsistent("argument-derived result requires an initial value and reduction lambda");
            }
        }

        private void requireReceiverCollectionResultPolicy() {
            if (!receivers.equals(List.of(ReceiverKind.COLLECTION))
                    || arguments.size() != 2
                    || arguments.getFirst().kind() != ArgumentKind.LAMBDA
                    || arguments.getFirst().typeRule() != ArgumentTypeRule.ORDERABLE_SCALAR
                    || arguments.getFirst().nullability() != RuntimeNullability.NEVER_NULL
                    || arguments.get(1).kind() != ArgumentKind.VALUE
                    || arguments.get(1).typeRule() != ArgumentTypeRule.STRING
                    || arguments.get(1).nullability() != RuntimeNullability.NEVER_NULL
                    || arguments.get(1).valueConstraint() != ValueConstraint.SORT_DIRECTION
                    || cardinalityPreservation != CardinalityPreservation.PRESERVES_RECEIVER_CARDINALITY
                    || materializationPolicy != MaterializationPolicy.MATERIALIZES) {
                throw inconsistent("receiver collection result requires an orderable selector and sort direction");
            }
        }

        private IllegalArgumentException inconsistent(String detail) {
            return new IllegalArgumentException("collection operation '" + name + "' has inconsistent " + detail);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Descriptor that)) {
                return false;
            }
            return name.equals(that.name)
                    && identity == that.identity
                    && receivers.equals(that.receivers)
                    && receiverItemConstraint == that.receiverItemConstraint
                    && arguments.equals(that.arguments)
                    && resultTypeRule == that.resultTypeRule
                    && numericResultFact == that.numericResultFact
                    && cardinalityPreservation == that.cardinalityPreservation
                    && intrinsicPurity == that.intrinsicPurity
                    && resultNullability == that.resultNullability
                    && evaluationPolicy == that.evaluationPolicy
                    && materializationPolicy == that.materializationPolicy
                    && numericComputationPolicy == that.numericComputationPolicy;
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    name, identity, receivers, receiverItemConstraint, arguments, resultTypeRule, numericResultFact,
                    cardinalityPreservation, intrinsicPurity, resultNullability, evaluationPolicy,
                    materializationPolicy, numericComputationPolicy);
        }

        @Override
        public String toString() {
            return "Descriptor[name=" + name + ']';
        }

        private static String requireName(String name) {
            Objects.requireNonNull(name, "name");
            if (name.isBlank()) {
                throw new IllegalArgumentException("name must not be blank");
            }
            return name;
        }

        private static List<ReceiverKind> requireReceivers(List<ReceiverKind> receivers) {
            Objects.requireNonNull(receivers, "receivers");
            List<ReceiverKind> copy = List.copyOf(receivers);
            if (copy.isEmpty()) {
                throw new IllegalArgumentException("receivers must not be empty");
            }
            if (EnumSet.copyOf(copy).size() != copy.size()) {
                throw new IllegalArgumentException("receivers must not contain duplicates");
            }
            return copy;
        }
    }

    public enum OperationIdentity {
        ALL("all"),
        ANY("any"),
        AVG("avg"),
        COUNT("count"),
        KEYS("keys"),
        MAP("map"),
        REDUCE("reduce"),
        SORT_BY("sortBy"),
        SUM("sum"),
        VALUES("values"),
        CUSTOM("");

        private final String operationName;

        OperationIdentity(String operationName) {
            this.operationName = operationName;
        }

        public String operationName() {
            return operationName;
        }

        static Optional<OperationIdentity> fromName(String operationName) {
            for (OperationIdentity identity : values()) {
                if (identity != CUSTOM && identity.operationName.equals(operationName)) {
                    return Optional.of(identity);
                }
            }
            return Optional.empty();
        }
    }

    /**
     * One source-ordered value or lambda contract of an operation call.
     */
    public record ArgumentContract(
            ArgumentKind kind,
            ArgumentTypeRule typeRule,
            RuntimeNullability nullability,
            CurrentItemTypeRule currentItemTypeRule,
            ValueConstraint valueConstraint) {

        public ArgumentContract {
            Objects.requireNonNull(kind, "kind");
            Objects.requireNonNull(typeRule, "typeRule");
            Objects.requireNonNull(nullability, "nullability");
            Objects.requireNonNull(currentItemTypeRule, "currentItemTypeRule");
            Objects.requireNonNull(valueConstraint, "valueConstraint");
            if (kind == ArgumentKind.VALUE && currentItemTypeRule != CurrentItemTypeRule.NONE) {
                throw new IllegalArgumentException("value argument cannot declare a current item");
            }
            if (kind == ArgumentKind.LAMBDA && currentItemTypeRule == CurrentItemTypeRule.NONE) {
                throw new IllegalArgumentException("lambda argument requires a current item");
            }
            if (kind == ArgumentKind.LAMBDA && valueConstraint != ValueConstraint.NONE_REQUIRED) {
                throw new IllegalArgumentException("lambda argument cannot declare a value constraint");
            }
            if (valueConstraint == ValueConstraint.SORT_DIRECTION && typeRule != ArgumentTypeRule.STRING) {
                throw new IllegalArgumentException("sort direction constraint requires a string value");
            }
        }
    }

    public enum ReceiverKind {
        COLLECTION,
        MAP;

        private boolean isCollection() {
            return this == COLLECTION;
        }
    }

    public enum ReceiverItemConstraint {
        ANY_ITEM,
        NUMBER_ITEM
    }

    public enum ArgumentKind {
        VALUE,
        LAMBDA
    }

    /**
     * Type constraint for a value argument or for the result of a lambda argument.
     */
    public enum ArgumentTypeRule {
        ANY_KNOWN_TYPE(Set.of()),
        BOOLEAN(Set.of(ScalarType.BOOLEAN)),
        STRING(Set.of(ScalarType.STRING)),
        ORDERABLE_SCALAR(Set.of(
                ScalarType.NUMBER, ScalarType.STRING, ScalarType.DATE, ScalarType.TIME, ScalarType.DATETIME)),
        SAME_AS_ARGUMENT_0(Set.of());

        private final Set<ScalarType> concreteTypes;

        ArgumentTypeRule(Set<ScalarType> concreteTypes) {
            this.concreteTypes = concreteTypes;
        }

        public Set<ScalarType> concreteTypes() {
            return concreteTypes;
        }
    }

    public enum CurrentItemTypeRule {
        NONE,
        RECEIVER_ITEM_OR_MAP_ENTRY,
        REDUCTION_ITEM
    }

    public enum ValueConstraint {
        NONE_REQUIRED(Set.of()),
        SORT_DIRECTION(Set.of("asc", "desc"));

        private final Set<String> allowedTextValues;

        ValueConstraint(Set<String> allowedTextValues) {
            this.allowedTextValues = allowedTextValues;
        }

        public Set<String> allowedTextValues() {
            return allowedTextValues;
        }
    }

    public enum ResultTypeRule {
        BOOLEAN_RESULT,
        NUMBER_RESULT,
        COLLECTION_OF_MAP_KEYS,
        COLLECTION_OF_MAP_VALUES,
        COLLECTION_OF_LAMBDA_RESULT,
        SAME_AS_VALUE_ARGUMENT_0,
        SAME_AS_RECEIVER_COLLECTION
    }

    public enum NumericResultFact {
        NO_NUMERIC_FACT,
        INTEGRAL_KNOWN,
        UNKNOWN_NUMERIC_VALUE_SHAPE,
        PROPAGATES_ARGUMENT_0
    }

    public enum CardinalityPreservation {
        NOT_APPLICABLE,
        PRESERVES_RECEIVER_CARDINALITY
    }

    public enum IntrinsicPurity {
        PURE,
        IMPURE
    }

    public enum EvaluationPolicy {
        EAGER,
        SHORT_CIRCUIT_ON_TRUE,
        SHORT_CIRCUIT_ON_FALSE
    }

    public enum MaterializationPolicy {
        DOES_NOT_MATERIALIZE,
        MATERIALIZES
    }

    public enum NumericComputationPolicy {
        NO_SPECIAL_POLICY,
        COMMON_MATH_CONTEXT
    }
}
