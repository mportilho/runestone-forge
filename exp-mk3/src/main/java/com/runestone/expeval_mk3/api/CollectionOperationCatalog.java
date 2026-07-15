package com.runestone.expeval_mk3.api;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Immutable Catalogo de Operacoes de Colecao available to semantic resolution.
 */
public final class CollectionOperationCatalog {

    private static final List<Descriptor> OFFICIAL_DESCRIPTORS = List.of(
            new Descriptor(
                    "all",
                    List.of(ReceiverKind.VECTOR, ReceiverKind.COLLECTION, ReceiverKind.MAP),
                    ReceiverItemConstraint.ANY_ITEM,
                    ArgumentShape.ONE_LAMBDA,
                    CurrentItemTypeRule.RECEIVER_ITEM_OR_MAP_ENTRY,
                    LambdaResultConstraint.BOOLEAN_NEVER_NULL,
                    ResultTypeRule.BOOLEAN,
                    NumericResultFact.NO_NUMERIC_FACT,
                    ShapePreservationRule.NOT_APPLICABLE,
                    IntrinsicPurity.PURE,
                    RuntimeNullability.NEVER_NULL,
                    EvaluationPolicy.SHORT_CIRCUIT_ON_FALSE,
                    MaterializationPolicy.DOES_NOT_MATERIALIZE),
            new Descriptor(
                    "any",
                    List.of(ReceiverKind.VECTOR, ReceiverKind.COLLECTION, ReceiverKind.MAP),
                    ReceiverItemConstraint.ANY_ITEM,
                    ArgumentShape.ONE_LAMBDA,
                    CurrentItemTypeRule.RECEIVER_ITEM_OR_MAP_ENTRY,
                    LambdaResultConstraint.BOOLEAN_NEVER_NULL,
                    ResultTypeRule.BOOLEAN,
                    NumericResultFact.NO_NUMERIC_FACT,
                    ShapePreservationRule.NOT_APPLICABLE,
                    IntrinsicPurity.PURE,
                    RuntimeNullability.NEVER_NULL,
                    EvaluationPolicy.SHORT_CIRCUIT_ON_TRUE,
                    MaterializationPolicy.DOES_NOT_MATERIALIZE),
            new Descriptor(
                    "count",
                    List.of(ReceiverKind.VECTOR, ReceiverKind.COLLECTION, ReceiverKind.MAP),
                    ReceiverItemConstraint.ANY_ITEM,
                    ArgumentShape.NO_ARGUMENTS,
                    CurrentItemTypeRule.NONE,
                    LambdaResultConstraint.NO_LAMBDA_RESULT,
                    ResultTypeRule.NUMBER,
                    NumericResultFact.INTEGRAL_KNOWN,
                    ShapePreservationRule.NOT_APPLICABLE,
                    IntrinsicPurity.PURE,
                    RuntimeNullability.NEVER_NULL,
                    EvaluationPolicy.EAGER,
                    MaterializationPolicy.DOES_NOT_MATERIALIZE),
            new Descriptor(
                    "keys",
                    List.of(ReceiverKind.MAP),
                    ReceiverItemConstraint.ANY_ITEM,
                    ArgumentShape.NO_ARGUMENTS,
                    CurrentItemTypeRule.NONE,
                    LambdaResultConstraint.NO_LAMBDA_RESULT,
                    ResultTypeRule.COLLECTION_OF_MAP_KEYS,
                    NumericResultFact.NO_NUMERIC_FACT,
                    ShapePreservationRule.MAP_TO_COLLECTION,
                    IntrinsicPurity.PURE,
                    RuntimeNullability.NEVER_NULL,
                    EvaluationPolicy.EAGER,
                    MaterializationPolicy.MATERIALIZES),
            new Descriptor(
                    "map",
                    List.of(ReceiverKind.VECTOR, ReceiverKind.COLLECTION, ReceiverKind.MAP),
                    ReceiverItemConstraint.ANY_ITEM,
                    ArgumentShape.ONE_LAMBDA,
                    CurrentItemTypeRule.RECEIVER_ITEM_OR_MAP_ENTRY,
                    LambdaResultConstraint.KNOWN_NEVER_NULL,
                    ResultTypeRule.MAPPED_ITEM_CONTAINER,
                    NumericResultFact.NO_NUMERIC_FACT,
                    ShapePreservationRule.VECTOR_TO_VECTOR_OTHERS_TO_COLLECTION,
                    IntrinsicPurity.PURE,
                    RuntimeNullability.NEVER_NULL,
                    EvaluationPolicy.EAGER,
                    MaterializationPolicy.MATERIALIZES),
            new Descriptor(
                    "sum",
                    List.of(ReceiverKind.VECTOR, ReceiverKind.COLLECTION),
                    ReceiverItemConstraint.NUMBER_ITEM,
                    ArgumentShape.NO_ARGUMENTS,
                    CurrentItemTypeRule.NONE,
                    LambdaResultConstraint.NO_LAMBDA_RESULT,
                    ResultTypeRule.NUMBER,
                    NumericResultFact.UNKNOWN_NUMERIC_VALUE_SHAPE,
                    ShapePreservationRule.NOT_APPLICABLE,
                    IntrinsicPurity.PURE,
                    RuntimeNullability.NEVER_NULL,
                    EvaluationPolicy.EAGER,
                    MaterializationPolicy.DOES_NOT_MATERIALIZE),
            new Descriptor(
                    "values",
                    List.of(ReceiverKind.MAP),
                    ReceiverItemConstraint.ANY_ITEM,
                    ArgumentShape.NO_ARGUMENTS,
                    CurrentItemTypeRule.NONE,
                    LambdaResultConstraint.NO_LAMBDA_RESULT,
                    ResultTypeRule.COLLECTION_OF_MAP_VALUES,
                    NumericResultFact.NO_NUMERIC_FACT,
                    ShapePreservationRule.MAP_TO_COLLECTION,
                    IntrinsicPurity.PURE,
                    RuntimeNullability.NEVER_NULL,
                    EvaluationPolicy.EAGER,
                    MaterializationPolicy.MATERIALIZES));

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

    /**
     * Declarative Descritor de Operacao de Colecao. It intentionally carries no runtime behavior.
     */
    public static final class Descriptor {

        private final String name;
        private final List<ReceiverKind> receivers;
        private final ReceiverItemConstraint receiverItemConstraint;
        private final ArgumentShape argumentShape;
        private final CurrentItemTypeRule currentItemTypeRule;
        private final LambdaResultConstraint lambdaResultConstraint;
        private final ResultTypeRule resultTypeRule;
        private final NumericResultFact numericResultFact;
        private final ShapePreservationRule shapePreservationRule;
        private final IntrinsicPurity intrinsicPurity;
        private final RuntimeNullability resultNullability;
        private final EvaluationPolicy evaluationPolicy;
        private final MaterializationPolicy materializationPolicy;

        Descriptor(
                String name,
                List<ReceiverKind> receivers,
                ReceiverItemConstraint receiverItemConstraint,
                ArgumentShape argumentShape,
                CurrentItemTypeRule currentItemTypeRule,
                LambdaResultConstraint lambdaResultConstraint,
                ResultTypeRule resultTypeRule,
                NumericResultFact numericResultFact,
                ShapePreservationRule shapePreservationRule,
                IntrinsicPurity intrinsicPurity,
                RuntimeNullability resultNullability,
                EvaluationPolicy evaluationPolicy,
                MaterializationPolicy materializationPolicy) {
            this.name = requireName(name);
            this.receivers = requireReceivers(receivers);
            this.receiverItemConstraint = Objects.requireNonNull(receiverItemConstraint, "receiverItemConstraint");
            this.argumentShape = Objects.requireNonNull(argumentShape, "argumentShape");
            this.currentItemTypeRule = Objects.requireNonNull(currentItemTypeRule, "currentItemTypeRule");
            this.lambdaResultConstraint = Objects.requireNonNull(lambdaResultConstraint, "lambdaResultConstraint");
            this.resultTypeRule = Objects.requireNonNull(resultTypeRule, "resultTypeRule");
            this.numericResultFact = Objects.requireNonNull(numericResultFact, "numericResultFact");
            this.shapePreservationRule = Objects.requireNonNull(shapePreservationRule, "shapePreservationRule");
            this.intrinsicPurity = Objects.requireNonNull(intrinsicPurity, "intrinsicPurity");
            this.resultNullability = Objects.requireNonNull(resultNullability, "resultNullability");
            this.evaluationPolicy = Objects.requireNonNull(evaluationPolicy, "evaluationPolicy");
            this.materializationPolicy = Objects.requireNonNull(materializationPolicy, "materializationPolicy");
            validateConsistency();
        }

        public String name() {
            return name;
        }

        public List<ReceiverKind> receivers() {
            return receivers;
        }

        public ReceiverItemConstraint receiverItemConstraint() {
            return receiverItemConstraint;
        }

        public ArgumentShape argumentShape() {
            return argumentShape;
        }

        public CurrentItemTypeRule currentItemTypeRule() {
            return currentItemTypeRule;
        }

        public LambdaResultConstraint lambdaResultConstraint() {
            return lambdaResultConstraint;
        }

        public ResultTypeRule resultTypeRule() {
            return resultTypeRule;
        }

        public NumericResultFact numericResultFact() {
            return numericResultFact;
        }

        public ShapePreservationRule shapePreservationRule() {
            return shapePreservationRule;
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

        private void validateConsistency() {
            boolean hasLambda = argumentShape == ArgumentShape.ONE_LAMBDA;
            if (hasLambda != (currentItemTypeRule != CurrentItemTypeRule.NONE)
                    || hasLambda != (lambdaResultConstraint != LambdaResultConstraint.NO_LAMBDA_RESULT)) {
                throw inconsistent("argument shape, current-item rule, and lambda-result constraint");
            }
            if (evaluationPolicy != EvaluationPolicy.EAGER
                    && (argumentShape != ArgumentShape.ONE_LAMBDA
                    || lambdaResultConstraint != LambdaResultConstraint.BOOLEAN_NEVER_NULL
                    || resultTypeRule != ResultTypeRule.BOOLEAN)) {
                throw inconsistent("short-circuit evaluation requires a boolean predicate and boolean result");
            }
            if (receiverItemConstraint == ReceiverItemConstraint.NUMBER_ITEM
                    && !receivers.stream().allMatch(ReceiverKind::isSequence)) {
                throw inconsistent("numeric receiver constraint requires sequence receivers");
            }
            if ((resultTypeRule == ResultTypeRule.NUMBER)
                    != (numericResultFact != NumericResultFact.NO_NUMERIC_FACT)) {
                throw inconsistent("numeric result fact and result type");
            }
            validateResultAndShape();
        }

        private void validateResultAndShape() {
            switch (resultTypeRule) {
                case MAPPED_ITEM_CONTAINER -> {
                    if (argumentShape != ArgumentShape.ONE_LAMBDA
                            || lambdaResultConstraint != LambdaResultConstraint.KNOWN_NEVER_NULL
                            || shapePreservationRule != ShapePreservationRule.VECTOR_TO_VECTOR_OTHERS_TO_COLLECTION
                            || materializationPolicy != MaterializationPolicy.MATERIALIZES) {
                        throw inconsistent("mapped result requires a non-null transform, sequence shape, and materialization");
                    }
                }
                case COLLECTION_OF_MAP_KEYS, COLLECTION_OF_MAP_VALUES -> {
                    if (!receivers.equals(List.of(ReceiverKind.MAP))
                            || argumentShape != ArgumentShape.NO_ARGUMENTS
                            || shapePreservationRule != ShapePreservationRule.MAP_TO_COLLECTION
                            || materializationPolicy != MaterializationPolicy.MATERIALIZES) {
                        throw inconsistent("map collection result requires only a map receiver and materialization");
                    }
                }
                case BOOLEAN, NUMBER -> {
                    if (shapePreservationRule != ShapePreservationRule.NOT_APPLICABLE
                            || materializationPolicy != MaterializationPolicy.DOES_NOT_MATERIALIZE) {
                        throw inconsistent("scalar result cannot preserve shape or materialize");
                    }
                }
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
                    && receivers.equals(that.receivers)
                    && receiverItemConstraint == that.receiverItemConstraint
                    && argumentShape == that.argumentShape
                    && currentItemTypeRule == that.currentItemTypeRule
                    && lambdaResultConstraint == that.lambdaResultConstraint
                    && resultTypeRule == that.resultTypeRule
                    && numericResultFact == that.numericResultFact
                    && shapePreservationRule == that.shapePreservationRule
                    && intrinsicPurity == that.intrinsicPurity
                    && resultNullability == that.resultNullability
                    && evaluationPolicy == that.evaluationPolicy
                    && materializationPolicy == that.materializationPolicy;
        }

        @Override
        public int hashCode() {
            return Objects.hash(
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

    public enum ReceiverKind {
        VECTOR,
        COLLECTION,
        MAP;

        private boolean isSequence() {
            return this == VECTOR || this == COLLECTION;
        }
    }

    public enum ReceiverItemConstraint {
        ANY_ITEM,
        NUMBER_ITEM
    }

    public enum ArgumentShape {
        NO_ARGUMENTS,
        ONE_LAMBDA
    }

    public enum CurrentItemTypeRule {
        NONE,
        RECEIVER_ITEM_OR_MAP_ENTRY
    }

    public enum LambdaResultConstraint {
        NO_LAMBDA_RESULT,
        KNOWN_NEVER_NULL,
        BOOLEAN_NEVER_NULL
    }

    public enum ResultTypeRule {
        BOOLEAN,
        NUMBER,
        COLLECTION_OF_MAP_KEYS,
        COLLECTION_OF_MAP_VALUES,
        MAPPED_ITEM_CONTAINER
    }

    public enum NumericResultFact {
        NO_NUMERIC_FACT,
        INTEGRAL_KNOWN,
        UNKNOWN_NUMERIC_VALUE_SHAPE
    }

    public enum ShapePreservationRule {
        NOT_APPLICABLE,
        VECTOR_TO_VECTOR_OTHERS_TO_COLLECTION,
        MAP_TO_COLLECTION
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
}
