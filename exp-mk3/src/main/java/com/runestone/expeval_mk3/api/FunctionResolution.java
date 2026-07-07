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
            List.of(),
            false);

    private final Kind kind;
    private final FunctionDescriptor descriptor;
    private final List<FunctionDescriptor> ambiguousCandidates;
    private final boolean usesBoundaryCoercion;

    private FunctionResolution(
            Kind kind,
            FunctionDescriptor descriptor,
            List<FunctionDescriptor> ambiguousCandidates,
            boolean usesBoundaryCoercion) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.descriptor = descriptor;
        this.ambiguousCandidates = List.copyOf(Objects.requireNonNull(ambiguousCandidates, "ambiguousCandidates"));
        this.usesBoundaryCoercion = usesBoundaryCoercion;
    }

    static FunctionResolution exact(FunctionDescriptor descriptor) {
        return new FunctionResolution(
                Kind.EXACT_MATCH,
                Objects.requireNonNull(descriptor, "descriptor"),
                List.of(),
                false);
    }

    static FunctionResolution boundaryCoercion(FunctionDescriptor descriptor) {
        return new FunctionResolution(
                Kind.BOUNDARY_COERCION_MATCH,
                Objects.requireNonNull(descriptor, "descriptor"),
                List.of(),
                true);
    }

    static FunctionResolution unknownArgument(FunctionDescriptor descriptor) {
        return new FunctionResolution(
                Kind.UNKNOWN_ARGUMENT_MATCH,
                Objects.requireNonNull(descriptor, "descriptor"),
                List.of(),
                false);
    }

    static FunctionResolution ambiguous(List<FunctionDescriptor> ambiguousCandidates) {
        return ambiguous(ambiguousCandidates, true);
    }

    static FunctionResolution semanticAmbiguous(List<FunctionDescriptor> ambiguousCandidates) {
        return ambiguous(ambiguousCandidates, false);
    }

    private static FunctionResolution ambiguous(List<FunctionDescriptor> ambiguousCandidates, boolean usesBoundaryCoercion) {
        if (ambiguousCandidates.size() < 2) {
            throw new IllegalArgumentException("ambiguous function resolution requires at least two candidates");
        }
        return new FunctionResolution(Kind.AMBIGUOUS, null, ambiguousCandidates, usesBoundaryCoercion);
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
        return usesBoundaryCoercion;
    }

    public enum Kind {
        EXACT_MATCH,
        UNKNOWN_ARGUMENT_MATCH,
        BOUNDARY_COERCION_MATCH,
        AMBIGUOUS,
        NO_MATCH
    }
}
