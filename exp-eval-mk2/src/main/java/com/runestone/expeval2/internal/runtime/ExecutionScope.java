package com.runestone.expeval2.internal.runtime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

final class ExecutionScope {

    /**
     * Sentinel returned by {@link #find} when the symbol is not present in this scope.
     * Distinct from {@code null}, which is a valid bound value.
     */
    static final Object UNBOUND = new Object();

    private final Map<SymbolRef, Object> values;
    /**
     * Optional read-only second layer checked after {@link #values}.
     * This is used for caller-provided overrides in mutable scopes and for shared defaults in
     * read-only scopes backed by an overrides map.
     */
    private final Map<SymbolRef, Object> secondaryValues;
    /**
     * Optional read-only third layer checked after {@link #secondaryValues}.
     * This is used for immutable catalog defaults when assignments are active and the scope needs
     * both an internal mutable layer and a per-call overrides layer.
     */
    private final Map<SymbolRef, Object> tertiaryValues;
    private final boolean mutable;
    private final AuditCollector audit;
    private EnumMap<DynamicInstant, Object> dynamicCache;

    private ExecutionScope(Map<SymbolRef, Object> values,
                           Map<SymbolRef, Object> secondaryValues,
                           Map<SymbolRef, Object> tertiaryValues,
                           boolean mutable,
                           AuditCollector audit) {
        this.values = Objects.requireNonNull(values, "values must not be null");
        this.secondaryValues = secondaryValues; // nullable
        this.tertiaryValues = tertiaryValues; // nullable
        this.mutable = mutable;
        this.audit = audit;
    }

    /**
     * Creates a two-layer mutable scope: a fresh internal map (pre-sized to {@code internalCapacity})
     * for assignment results, backed by a shared read-only external-binding layer.
     */
    static ExecutionScope from(Map<SymbolRef, Object> sharedExternal, int internalCapacity) {
        Objects.requireNonNull(sharedExternal, "sharedExternal must not be null");
        return new ExecutionScope(new HashMap<>(internalCapacity), sharedExternal, null, true, null);
    }

    /**
     * Creates a three-layer mutable scope: internal assignment results, per-call overrides, and
     * immutable defaults.
     */
    static ExecutionScope from(Map<SymbolRef, Object> overrides,
                               Map<SymbolRef, Object> defaults,
                               int internalCapacity) {
        Objects.requireNonNull(overrides, "overrides must not be null");
        Objects.requireNonNull(defaults, "defaults must not be null");
        return new ExecutionScope(new HashMap<>(internalCapacity), overrides, defaults, true, null);
    }

    /**
     * Creates a read-only scope backed by a shared map.
     */
    static ExecutionScope readOnly(Map<SymbolRef, Object> sharedValues) {
        return new ExecutionScope(Objects.requireNonNull(sharedValues, "sharedValues must not be null"), null, null, false, null);
    }

    /**
     * Creates a read-only scope backed by a per-call overrides layer plus immutable defaults.
     */
    static ExecutionScope readOnly(Map<SymbolRef, Object> overrides, Map<SymbolRef, Object> defaults) {
        Objects.requireNonNull(overrides, "overrides must not be null");
        Objects.requireNonNull(defaults, "defaults must not be null");
        return new ExecutionScope(overrides, defaults, null, false, null);
    }

    static ExecutionScope fromWithAudit(Map<SymbolRef, Object> sharedExternal,
                                        int internalCapacity, AuditCollector audit) {
        Objects.requireNonNull(sharedExternal, "sharedExternal must not be null");
        Objects.requireNonNull(audit, "audit must not be null");
        return new ExecutionScope(new HashMap<>(internalCapacity), sharedExternal, null, true, audit);
    }

    static ExecutionScope fromWithAudit(Map<SymbolRef, Object> overrides,
                                        Map<SymbolRef, Object> defaults,
                                        int internalCapacity,
                                        AuditCollector audit) {
        Objects.requireNonNull(overrides, "overrides must not be null");
        Objects.requireNonNull(defaults, "defaults must not be null");
        Objects.requireNonNull(audit, "audit must not be null");
        return new ExecutionScope(new HashMap<>(internalCapacity), overrides, defaults, true, audit);
    }

    static ExecutionScope readOnlyWithAudit(Map<SymbolRef, Object> sharedValues, AuditCollector audit) {
        return new ExecutionScope(
                Objects.requireNonNull(sharedValues, "sharedValues must not be null"),
                null,
                null,
                false,
                Objects.requireNonNull(audit, "audit must not be null")
        );
    }

    static ExecutionScope readOnlyWithAudit(Map<SymbolRef, Object> overrides,
                                            Map<SymbolRef, Object> defaults,
                                            AuditCollector audit) {
        Objects.requireNonNull(overrides, "overrides must not be null");
        Objects.requireNonNull(defaults, "defaults must not be null");
        Objects.requireNonNull(audit, "audit must not be null");
        return new ExecutionScope(overrides, defaults, null, false, audit);
    }

    boolean hasAudit() {
        return audit != null;
    }

    AuditCollector audit() {
        return audit;
    }

    /**
     * Returns the value bound to {@code symbolRef}, or {@link #UNBOUND} if no binding exists.
     *
     * <p>Prefer this method on hot paths to avoid {@link java.util.Optional} allocation.
     * Callers must compare the return value with {@code ==} against {@link #UNBOUND} to detect
     * the absent case; a {@code null} return means the symbol is explicitly bound to a null value.
     */
    Object find(SymbolRef symbolRef) {
        Objects.requireNonNull(symbolRef, "symbolRef must not be null");
        Object v = values.getOrDefault(symbolRef, UNBOUND);
        if (v == UNBOUND && secondaryValues != null) {
            v = secondaryValues.getOrDefault(symbolRef, UNBOUND);
        }
        if (v == UNBOUND && tertiaryValues != null) {
            v = tertiaryValues.getOrDefault(symbolRef, UNBOUND);
        }
        return v;
    }

    void assign(SymbolRef symbolRef, Object value) {
        if (!mutable) {
            throw new IllegalStateException("assign() is not allowed on a read-only ExecutionScope");
        }
        values.put(
                Objects.requireNonNull(symbolRef, "symbolRef must not be null"),
                value
        );
    }

    /**
     * Resolves the value for a {@link DynamicInstant}, caching the result for the
     * lifetime of this scope so that repeated reads within the same evaluation return
     * the same instant.
     */
    Object resolveDynamic(DynamicInstant kind) {
        if (dynamicCache == null) {
            dynamicCache = new EnumMap<>(DynamicInstant.class);
        }
        return dynamicCache.computeIfAbsent(Objects.requireNonNull(kind, "kind must not be null"), k -> switch (k) {
            case CURR_DATE     -> LocalDate.now();
            case CURR_TIME     -> LocalTime.now();
            case CURR_DATETIME -> LocalDateTime.now();
        });
    }
}
