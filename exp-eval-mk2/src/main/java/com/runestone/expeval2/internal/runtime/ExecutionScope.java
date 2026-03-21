package com.runestone.expeval2.internal.runtime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

final class ExecutionScope {

    private final Map<SymbolRef, RuntimeValue> values;
    /**
     * Optional read-only external-binding layer used in the two-layer scope variant.
     * When non-null, {@link #find} checks {@code values} (internal/mutable) first and
     * falls back to this map for external symbols. {@link #assign} always writes only
     * to {@code values}, so the shared external layer is never mutated by assignments.
     */
    private final Map<SymbolRef, RuntimeValue> externalValues;
    private final boolean mutable;
    private final AuditCollector audit;
    private EnumMap<DynamicInstant, RuntimeValue> dynamicCache;

    private ExecutionScope(Map<SymbolRef, RuntimeValue> values, Map<SymbolRef, RuntimeValue> externalValues,
                           boolean mutable, AuditCollector audit) {
        this.values = Objects.requireNonNull(values, "values must not be null");
        this.externalValues = externalValues; // nullable
        this.mutable = mutable;
        this.audit = audit;
    }

    /**
     * Creates a two-layer mutable scope: a fresh internal map (pre-sized to {@code internalCapacity})
     * for assignment results, backed by a shared read-only external-binding layer.
     * <p>
     * This avoids copying the external bindings on every {@code compute()} call: the shared layer
     * is referenced directly, while assignments write only to the fresh internal layer.
     */
    static ExecutionScope from(Map<SymbolRef, RuntimeValue> sharedExternal, int internalCapacity) {
        Objects.requireNonNull(sharedExternal, "sharedExternal must not be null");
        return new ExecutionScope(new HashMap<>(internalCapacity), sharedExternal, true, null);
    }

    /**
     * Creates a read-only scope backed by a shared map.
     * <p>
     * Use only when the expression has no assignments. Calling {@link #assign} on the
     * returned scope throws {@link IllegalStateException}.
     */
    static ExecutionScope readOnly(Map<SymbolRef, RuntimeValue> sharedValues) {
        return new ExecutionScope(Objects.requireNonNull(sharedValues, "sharedValues must not be null"), null, false, null);
    }

    static ExecutionScope fromWithAudit(Map<SymbolRef, RuntimeValue> sharedExternal,
                                        int internalCapacity, AuditCollector audit) {
        Objects.requireNonNull(sharedExternal, "sharedExternal must not be null");
        Objects.requireNonNull(audit, "audit must not be null");
        return new ExecutionScope(new HashMap<>(internalCapacity), sharedExternal, true, audit);
    }

    static ExecutionScope readOnlyWithAudit(Map<SymbolRef, RuntimeValue> sharedValues, AuditCollector audit) {
        return new ExecutionScope(
                Objects.requireNonNull(sharedValues, "sharedValues must not be null"),
                null,
                false,
                Objects.requireNonNull(audit, "audit must not be null")
        );
    }

    boolean hasAudit() {
        return audit != null;
    }

    AuditCollector audit() {
        return audit;
    }

    public Optional<RuntimeValue> find(SymbolRef symbolRef) {
        Objects.requireNonNull(symbolRef, "symbolRef must not be null");
        RuntimeValue v = values.get(symbolRef);
        if (v == null && externalValues != null) {
            v = externalValues.get(symbolRef);
        }
        return Optional.ofNullable(v);
    }

    public void assign(SymbolRef symbolRef, RuntimeValue value) {
        if (!mutable) {
            throw new IllegalStateException("assign() is not allowed on a read-only ExecutionScope");
        }
        values.put(
                Objects.requireNonNull(symbolRef, "symbolRef must not be null"),
                Objects.requireNonNull(value, "value must not be null")
        );
    }

    RuntimeValue resolveDynamic(DynamicInstant kind) {
        if (dynamicCache == null) {
            dynamicCache = new EnumMap<>(DynamicInstant.class);
        }
        return dynamicCache.computeIfAbsent(Objects.requireNonNull(kind, "kind must not be null"), k -> switch (k) {
            case CURR_DATE -> new RuntimeValue.DateValue(LocalDate.now());
            case CURR_TIME -> new RuntimeValue.TimeValue(LocalTime.now());
            case CURR_DATETIME -> new RuntimeValue.DateTimeValue(LocalDateTime.now());
        });
    }
}
