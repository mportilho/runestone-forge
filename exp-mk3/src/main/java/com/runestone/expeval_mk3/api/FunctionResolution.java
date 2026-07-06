package com.runestone.expeval_mk3.api;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Deterministic result of resolving a function call against a FunctionCatalog.
 */
public final class FunctionResolution {

    private static final FunctionResolution NO_MATCH = new FunctionResolution(
            Kind.NO_MATCH,
            null,
            List.of());

    private final Kind kind;
    private final FunctionDescriptor descriptor;
    private final List<FunctionDescriptor> ambiguousCandidates;

    private FunctionResolution(
            Kind kind,
            FunctionDescriptor descriptor,
            List<FunctionDescriptor> ambiguousCandidates) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.descriptor = descriptor;
        this.ambiguousCandidates = List.copyOf(Objects.requireNonNull(ambiguousCandidates, "ambiguousCandidates"));
    }

    static FunctionResolution exact(FunctionDescriptor descriptor) {
        return new FunctionResolution(Kind.EXACT_MATCH, Objects.requireNonNull(descriptor, "descriptor"), List.of());
    }

    static FunctionResolution boundaryCoercion(FunctionDescriptor descriptor) {
        return new FunctionResolution(
                Kind.BOUNDARY_COERCION_MATCH,
                Objects.requireNonNull(descriptor, "descriptor"),
                List.of());
    }

    static FunctionResolution ambiguous(List<FunctionDescriptor> ambiguousCandidates) {
        if (ambiguousCandidates.size() < 2) {
            throw new IllegalArgumentException("ambiguous function resolution requires at least two candidates");
        }
        return new FunctionResolution(Kind.AMBIGUOUS, null, ambiguousCandidates);
    }

    static FunctionResolution noMatch() {
        return NO_MATCH;
    }

    public Kind kind() {
        return kind;
    }

    public Optional<FunctionDescriptor> descriptor() {
        return Optional.ofNullable(descriptor);
    }

    public List<FunctionDescriptor> ambiguousCandidates() {
        return ambiguousCandidates;
    }

    public boolean usesBoundaryCoercion() {
        return kind == Kind.BOUNDARY_COERCION_MATCH || kind == Kind.AMBIGUOUS;
    }

    public enum Kind {
        EXACT_MATCH,
        BOUNDARY_COERCION_MATCH,
        AMBIGUOUS,
        NO_MATCH
    }
}
