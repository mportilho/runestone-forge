package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.CollectionOperationCatalog.OperationIdentity;
import com.runestone.expeval_mk3.internal.semantics.CollectionOperationWiring;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Maps each {@link OperationIdentity} to its runtime {@link CollectionOperationExecutor}. Membership is
 * cross-checked against {@link CollectionOperationWiring} at class-init so the two layers cannot silently
 * drift: an identity the wiring table marks {@code WIRED} must have an executor here, and vice versa.
 */
public final class CollectionOperationExecutors {

    private static final Map<OperationIdentity, CollectionOperationExecutor> EXECUTORS = buildExecutors();

    private CollectionOperationExecutors() {
    }

    public static CollectionOperationExecutor executorFor(OperationIdentity identity) {
        CollectionOperationExecutor executor = EXECUTORS.get(Objects.requireNonNull(identity, "identity"));
        if (executor == null) {
            throw new IllegalStateException("no runtime executor wired for collection operation: " + identity);
        }
        return executor;
    }

    private static Map<OperationIdentity, CollectionOperationExecutor> buildExecutors() {
        Map<OperationIdentity, CollectionOperationExecutor> executors = new EnumMap<>(OperationIdentity.class);
        executors.put(OperationIdentity.ALL, ExpressionRuntime::executeAll);
        executors.put(OperationIdentity.ANY, ExpressionRuntime::executeAny);
        executors.put(OperationIdentity.AVG, ExpressionRuntime::executeAvg);
        executors.put(OperationIdentity.COUNT, ExpressionRuntime::executeCount);
        executors.put(OperationIdentity.KEYS, ExpressionRuntime::executeKeys);
        executors.put(OperationIdentity.MAP, ExpressionRuntime::executeMap);
        executors.put(OperationIdentity.REDUCE, ExpressionRuntime::executeReduce);
        executors.put(OperationIdentity.SORT_BY, ExpressionRuntime::executeSortBy);
        executors.put(OperationIdentity.SUM, ExpressionRuntime::executeSum);
        executors.put(OperationIdentity.VALUES, ExpressionRuntime::executeValues);
        requireWiringAgreement(executors.keySet());
        return Map.copyOf(executors);
    }

    private static void requireWiringAgreement(Set<OperationIdentity> wiredExecutors) {
        for (OperationIdentity identity : EnumSet.allOf(OperationIdentity.class)) {
            if (CollectionOperationWiring.runtimeWired(identity) != wiredExecutors.contains(identity)) {
                throw new IllegalStateException(
                        "collection operation runtime executor table disagrees with wiring registry for " + identity);
            }
        }
    }
}
