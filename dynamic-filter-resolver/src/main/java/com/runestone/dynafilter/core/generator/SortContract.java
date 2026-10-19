package com.runestone.dynafilter.core.generator;

import java.util.List;

/** Immutable declarative sort candidates, independent of active predicates and persistence adapters. */
public record SortContract(List<Candidate> candidates) {

    public static final SortContract EMPTY = new SortContract(List.of());

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
