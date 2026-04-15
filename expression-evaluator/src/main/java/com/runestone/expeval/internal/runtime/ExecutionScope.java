package com.runestone.expeval.internal.runtime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Objects;

final class ExecutionScope {

    /**
     * Sentinel returned by {@link #find} when the symbol is not present in this scope.
     * Distinct from {@code null}, which is a valid bound value.
     */
    static final Object UNBOUND = new Object();

    /**
     * Layer 1. In mutable scopes: internal assignment results.
     * In read-only scopes with single layer: provided values.
     * In read-only scopes with two layers: provided overrides.
     */
    private final Object[] layer1;
    /**
     * Layer 2. In mutable scopes: external overrides.
     * In read-only scopes with two layers: default values.
     */
    private final Object[] layer2;
    /**
     * Layer 3. In mutable scopes: default values.
     */
    private final Object[] layer3;
    
    private final boolean mutable;
    private final AuditCollector audit;
    private EnumMap<DynamicInstant, Object> dynamicCache;

    private ExecutionScope(Object[] layer1,
                           Object[] layer2,
                           Object[] layer3,
                           boolean mutable,
                           AuditCollector audit) {
        this.layer1 = layer1;
        this.layer2 = layer2;
        this.layer3 = layer3;
        this.mutable = mutable;
        this.audit = audit;
    }

    /**
     * Mutable: internal results (layer1), external shared (layer2).
     */
    static ExecutionScope from(Object[] sharedExternal, int internalCapacity) {
        Object[] internal = new Object[internalCapacity];
        Arrays.fill(internal, UNBOUND);
        return new ExecutionScope(internal, sharedExternal, null, true, null);
    }

    /**
     * Mutable: internal results (layer1), overrides (layer2), defaults (layer3).
     */
    static ExecutionScope from(Object[] overrides,
                               Object[] defaults,
                               int internalCapacity) {
        Object[] internal = new Object[internalCapacity];
        Arrays.fill(internal, UNBOUND);
        return new ExecutionScope(internal, overrides, defaults, true, null);
    }

    /**
     * Read-only: shared values (layer1).
     */
    static ExecutionScope readOnly(Object[] sharedValues) {
        return new ExecutionScope(sharedValues, null, null, false, null);
    }

    /**
     * Read-only: overrides (layer1), defaults (layer2).
     */
    static ExecutionScope readOnly(Object[] overrides, Object[] defaults) {
        return new ExecutionScope(overrides, defaults, null, false, null);
    }

    static ExecutionScope fromWithAudit(Object[] sharedExternal,
                                        int internalCapacity, AuditCollector audit) {
        Objects.requireNonNull(audit, "audit must not be null");
        Object[] internal = new Object[internalCapacity];
        Arrays.fill(internal, UNBOUND);
        return new ExecutionScope(internal, sharedExternal, null, true, audit);
    }

    static ExecutionScope fromWithAudit(Object[] overrides,
                                        Object[] defaults,
                                        int internalCapacity,
                                        AuditCollector audit) {
        Objects.requireNonNull(audit, "audit must not be null");
        Object[] internal = new Object[internalCapacity];
        Arrays.fill(internal, UNBOUND);
        return new ExecutionScope(internal, overrides, defaults, true, audit);
    }

    static ExecutionScope readOnlyWithAudit(Object[] sharedValues, AuditCollector audit) {
        return new ExecutionScope(
                sharedValues,
                null,
                null,
                false,
                Objects.requireNonNull(audit, "audit must not be null")
        );
    }

    static ExecutionScope readOnlyWithAudit(Object[] overrides,
                                            Object[] defaults,
                                            AuditCollector audit) {
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
     * Looks up the value bound to {@code symbolRef} in this scope.
     *
     * <p>Returns {@link #UNBOUND} — not {@code null} — when the symbol has no value in this scope.
     * {@code null} is a valid bound value (e.g., a nullable external parameter set to null by the
     * caller) and is therefore distinct from the absence of a binding.
     *
     * <p><strong>Index contract:</strong> A {@code symbolRef} with {@code index() < 0} (the sentinel
     * value present before {@code assignIndices()} runs in {@code ExecutionPlanBuilder}) always returns
     * {@link #UNBOUND}. This prevents partially-constructed symbols from silently returning stale data.
     *
     * <p><strong>INTERNAL symbols:</strong>
     * <ul>
     *   <li>Mutable scope — layer 1 holds assignment results; returns the value at
     *       {@code symbolRef.index()} in layer 1, or {@link #UNBOUND} if the slot has not been
     *       written yet.</li>
     *   <li>Read-only scope — always returns {@link #UNBOUND}. Internal symbols represent intermediate
     *       results computed during an evaluation run; a read-only scope has no internal-result layer,
     *       so there is nothing to look up.</li>
     * </ul>
     *
     * <p><strong>EXTERNAL symbols:</strong>
     * <ul>
     *   <li>Mutable scope — layer 2 holds per-call overrides; layer 3 (when present) holds compiled
     *       defaults. Layers are checked in that order; the first non-{@link #UNBOUND} value wins.</li>
     *   <li>Read-only scope — layer 1 holds overrides; layer 2 (when present) holds defaults. Same
     *       priority rule applies.</li>
     * </ul>
     *
     * @param symbolRef the symbol to look up; must not be {@code null}
     * @return the bound value, {@code null} if the symbol is explicitly bound to null,
     *         or {@link #UNBOUND} if not bound in this scope
     * @throws NullPointerException if {@code symbolRef} is {@code null}
     */
    Object find(SymbolRef symbolRef) {
        Objects.requireNonNull(symbolRef, "symbolRef must not be null");
        int idx = symbolRef.index();
        if (idx < 0) return UNBOUND;

        if (symbolRef.kind() == SymbolKind.INTERNAL) {
            if (mutable) {
                // Internal is always layer1 in mutable
                return (layer1 != null && idx < layer1.length) ? layer1[idx] : UNBOUND;
            } else {
                return UNBOUND;
            }
        } else {
            // EXTERNAL
            if (mutable) {
                // Layer 2 (overrides) then Layer 3 (defaults)
                Object v = (layer2 != null && idx < layer2.length) ? layer2[idx] : UNBOUND;
                if (v == UNBOUND && layer3 != null && idx < layer3.length) {
                    v = layer3[idx];
                }
                return v;
            } else {
                // Read-only: Layer 1 then Layer 2
                Object v = (layer1 != null && idx < layer1.length) ? layer1[idx] : UNBOUND;
                if (v == UNBOUND && layer2 != null && idx < layer2.length) {
                    v = layer2[idx];
                }
                return v;
            }
        }
    }

    void assign(SymbolRef symbolRef, Object value) {
        if (!mutable) {
            throw new IllegalStateException("assign() is not allowed on a read-only ExecutionScope");
        }
        if (symbolRef.kind() != SymbolKind.INTERNAL) {
            throw new IllegalStateException("cannot assign to external symbol: " + symbolRef.name());
        }
        int idx = symbolRef.index();
        if (idx < 0 || idx >= layer1.length) {
            throw new IllegalStateException("invalid internal symbol index: " + idx);
        }
        layer1[idx] = value;
    }

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
