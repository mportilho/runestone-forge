package com.runestone.dynafilter.core.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Immutable declarative sort candidates, independent of active predicates and persistence adapters. */
public record SortContract(List<Candidate> candidates, Class<?> declaredRoot, List<String> exclusions) {

    public static final SortContract EMPTY = new SortContract(List.of());

    /** Contracts without a declared root use the effective entity of the persistence adapter. */
    public SortContract(List<Candidate> candidates) {
        this(candidates, null);
    }

    public SortContract(List<Candidate> candidates, Class<?> declaredRoot) {
        this(candidates, declaredRoot, List.of());
    }

    public SortContract {
        candidates = List.copyOf(candidates);
        exclusions = List.copyOf(exclusions);
    }

    /** Creates a reusable builder for explicit public paths, independent of internal filter conditions. */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<Candidate> candidates = new ArrayList<>();
        private final List<String> exclusions = new ArrayList<>();
        private Class<?> root;

        private Builder() {
        }

        /** Publishes an exact path and any aliases mapping to that same path. */
        public Builder include(String path, String... aliases) {
            candidates.add(new Candidate(path, List.of(aliases), true));
            return this;
        }

        /** Forbids exact canonical destinations and every alias referring to them. */
        public Builder exclude(String... paths) {
            exclusions.addAll(List.of(paths));
            return this;
        }

        /** Optionally limits reuse to this root or its persistence-adapter-compatible subtypes. */
        public Builder root(Class<?> root) {
            this.root = Objects.requireNonNull(root, "root is required");
            return this;
        }

        public SortContract build() {
            return new SortContract(candidates, root, exclusions);
        }
    }

    /** A discovered or explicitly declared destination; explicit invalid paths are configuration errors. */
    public record Candidate(String path, List<String> aliases, boolean explicit) {

        public Candidate(String path, List<String> aliases) {
            this(path, aliases, false);
        }

        public Candidate {
            Objects.requireNonNull(path, "path is required");
            aliases = List.copyOf(aliases);
        }
    }
}
