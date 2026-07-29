package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.CollectionOperationCatalog.OperationIdentity;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;

/**
 * Single source of truth for which {@link OperationIdentity} values the semantics layer accepts and
 * which ones have a wired runtime executor. {@code SemanticResolver} and {@code ExpressionRuntime} (in
 * {@code internal.runtime}) both query this table instead of independently re-deriving the same membership.
 */
public final class CollectionOperationWiring {

    private static final Map<OperationIdentity, Entry> ENTRIES = buildEntries();

    private CollectionOperationWiring() {
    }

    public static boolean semanticsSupported(OperationIdentity identity) {
        return entryFor(identity).semanticsSupport() == SemanticsSupport.SUPPORTED;
    }

    public static boolean runtimeWired(OperationIdentity identity) {
        return entryFor(identity).runtimeWiring() == RuntimeWiring.WIRED;
    }

    public static boolean isAverageOfEmptyCollectionUndefined(int collectionSize) {
        return collectionSize == 0;
    }

    private static Entry entryFor(OperationIdentity identity) {
        Entry entry = ENTRIES.get(Objects.requireNonNull(identity, "identity"));
        if (entry == null) {
            throw new IllegalStateException("no collection operation wiring entry for " + identity);
        }
        return entry;
    }

    private static Map<OperationIdentity, Entry> buildEntries() {
        Map<OperationIdentity, Entry> entries = new EnumMap<>(OperationIdentity.class);
        entries.put(OperationIdentity.ALL, new Entry(SemanticsSupport.SUPPORTED, RuntimeWiring.WIRED));
        entries.put(OperationIdentity.ANY, new Entry(SemanticsSupport.SUPPORTED, RuntimeWiring.WIRED));
        entries.put(OperationIdentity.AVG, new Entry(SemanticsSupport.SUPPORTED, RuntimeWiring.WIRED));
        entries.put(OperationIdentity.COUNT, new Entry(SemanticsSupport.SUPPORTED, RuntimeWiring.WIRED));
        entries.put(OperationIdentity.KEYS, new Entry(SemanticsSupport.SUPPORTED, RuntimeWiring.WIRED));
        entries.put(OperationIdentity.MAP, new Entry(SemanticsSupport.SUPPORTED, RuntimeWiring.WIRED));
        entries.put(OperationIdentity.REDUCE, new Entry(SemanticsSupport.SUPPORTED, RuntimeWiring.WIRED));
        entries.put(OperationIdentity.SORT_BY, new Entry(SemanticsSupport.SUPPORTED, RuntimeWiring.WIRED));
        entries.put(OperationIdentity.SUM, new Entry(SemanticsSupport.SUPPORTED, RuntimeWiring.WIRED));
        entries.put(OperationIdentity.VALUES, new Entry(SemanticsSupport.SUPPORTED, RuntimeWiring.WIRED));
        // CUSTOM is modeled by the catalog as the ADR-0005 user-extension seam, but neither layer
        // accepts it yet; both facts are explicit here rather than falling out of a default branch.
        entries.put(OperationIdentity.CUSTOM, new Entry(SemanticsSupport.NOT_SUPPORTED, RuntimeWiring.NOT_WIRED));
        if (!entries.keySet().equals(EnumSet.allOf(OperationIdentity.class))) {
            throw new IllegalStateException("collection operation wiring table does not cover every OperationIdentity");
        }
        return Map.copyOf(entries);
    }

    private enum SemanticsSupport {
        SUPPORTED,
        NOT_SUPPORTED
    }

    private enum RuntimeWiring {
        WIRED,
        NOT_WIRED
    }

    private record Entry(SemanticsSupport semanticsSupport, RuntimeWiring runtimeWiring) {

        private Entry {
            Objects.requireNonNull(semanticsSupport, "semanticsSupport");
            Objects.requireNonNull(runtimeWiring, "runtimeWiring");
        }
    }
}
