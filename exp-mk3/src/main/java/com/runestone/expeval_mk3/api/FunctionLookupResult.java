package com.runestone.expeval_mk3.api;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Deterministic overload lookup outcome for a function call candidate.
 */
public final class FunctionLookupResult {

    private final Status status;
    private final FunctionDescriptor descriptor;
    private final List<FunctionDescriptor> candidates;

    private FunctionLookupResult(Status status, FunctionDescriptor descriptor, List<FunctionDescriptor> candidates) {
        this.status = Objects.requireNonNull(status, "status");
        this.descriptor = descriptor;
        this.candidates = List.copyOf(Objects.requireNonNull(candidates, "candidates"));
    }

    static FunctionLookupResult exactMatch(FunctionDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor");
        return new FunctionLookupResult(Status.EXACT_MATCH, descriptor, List.of(descriptor));
    }

    static FunctionLookupResult notFound() {
        return new FunctionLookupResult(Status.NOT_FOUND, null, List.of());
    }

    public Status status() {
        return status;
    }

    public Optional<FunctionDescriptor> descriptor() {
        return Optional.ofNullable(descriptor);
    }

    public List<FunctionDescriptor> candidates() {
        return candidates;
    }

    public enum Status {
        EXACT_MATCH,
        NOT_FOUND
    }
}
