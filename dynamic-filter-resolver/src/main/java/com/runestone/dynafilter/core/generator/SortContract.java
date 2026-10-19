package com.runestone.dynafilter.core.generator;

import java.util.List;

/** Immutable declarative sort candidates, independent of active predicates and persistence adapters. */
public record SortContract(List<Candidate> candidates, Class<?> declaredRoot) {

    public static final SortContract EMPTY = new SortContract(List.of());

    /** Contracts without a declared root use the effective entity of the persistence adapter. */
    public SortContract(List<Candidate> candidates) {
        this(candidates, null);
    }

    public SortContract {
        candidates = List.copyOf(candidates);
    }

    public record Candidate(String path, List<String> aliases) {
        public Candidate {
            java.util.Objects.requireNonNull(path, "path is required");
            aliases = List.copyOf(aliases);
        }
    }
}
