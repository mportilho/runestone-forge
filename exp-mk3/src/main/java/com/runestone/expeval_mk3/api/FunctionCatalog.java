package com.runestone.expeval_mk3.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable catalog of registered functions keyed by Assinatura de Funcao.
 */
public final class FunctionCatalog {

    private static final Comparator<FunctionDescriptor> DESCRIPTOR_ORDER = Comparator
            .comparing(FunctionDescriptor::languageName)
            .thenComparingInt(FunctionDescriptor::arity)
            .thenComparing(FunctionDescriptor::parameterTypes, FunctionCatalog::compareParameterTypes);

    private static final FunctionCatalog EMPTY = new FunctionCatalog(Map.of());

    private final Map<FunctionSignature, FunctionDescriptor> descriptorsBySignature;
    private final Map<NameArity, List<FunctionDescriptor>> overloadsByNameAndArity;
    private final List<FunctionDescriptor> descriptors;

    private FunctionCatalog(Map<FunctionSignature, FunctionDescriptor> source) {
        if (source.isEmpty()) {
            descriptorsBySignature = Map.of();
            overloadsByNameAndArity = Map.of();
            descriptors = List.of();
            return;
        }

        ArrayList<FunctionDescriptor> sortedDescriptors = new ArrayList<>(source.values());
        sortedDescriptors.sort(DESCRIPTOR_ORDER);

        LinkedHashMap<FunctionSignature, FunctionDescriptor> bySignature = new LinkedHashMap<>();
        HashMap<NameArity, List<FunctionDescriptor>> overloadIndex = new HashMap<>();
        for (FunctionDescriptor descriptor : sortedDescriptors) {
            bySignature.put(descriptor.signature(), descriptor);
            overloadIndex.computeIfAbsent(NameArity.from(descriptor.signature()), ignored -> new ArrayList<>())
                    .add(descriptor);
        }

        LinkedHashMap<NameArity, List<FunctionDescriptor>> sortedOverloads = new LinkedHashMap<>();
        overloadIndex.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> sortedOverloads.put(entry.getKey(), List.copyOf(entry.getValue())));

        descriptorsBySignature = Collections.unmodifiableMap(bySignature);
        overloadsByNameAndArity = Collections.unmodifiableMap(sortedOverloads);
        descriptors = List.copyOf(sortedDescriptors);
    }

    public static FunctionCatalog empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Collection<FunctionDescriptor> functions() {
        return descriptors;
    }

    public Builder toBuilder() {
        Builder builder = builder();
        for (FunctionDescriptor descriptor : descriptors) {
            builder.register(descriptor);
        }
        return builder;
    }

    public Optional<FunctionDescriptor> find(FunctionSignature signature) {
        return Optional.ofNullable(descriptorsBySignature.get(Objects.requireNonNull(signature, "signature")));
    }

    public FunctionResolution resolve(
            String languageName,
            List<ExpressionType> argumentTypes,
            BoundaryCoercion boundaryCoercion) {
        BoundaryCoercion effectiveCoercion = Objects.requireNonNull(boundaryCoercion, "boundaryCoercion");
        FunctionSignature requestedSignature = FunctionSignature.of(languageName, argumentTypes);
        FunctionDescriptor exactMatch = descriptorsBySignature.get(requestedSignature);
        if (exactMatch != null) {
            return FunctionResolution.exact(exactMatch);
        }

        List<FunctionDescriptor> overloads = overloadsByNameAndArity.get(NameArity.from(requestedSignature));
        if (overloads == null) {
            return FunctionResolution.noMatch();
        }

        ArrayList<FunctionDescriptor> coercibleCandidates = new ArrayList<>();
        for (FunctionDescriptor overload : overloads) {
            if (isCoercible(requestedSignature.parameterTypes(), overload.parameterTypes(), effectiveCoercion)) {
                coercibleCandidates.add(overload);
            }
        }

        return switch (coercibleCandidates.size()) {
            case 0 -> FunctionResolution.noMatch();
            case 1 -> FunctionResolution.boundaryCoercion(coercibleCandidates.getFirst());
            default -> FunctionResolution.ambiguous(coercibleCandidates);
        };
    }

    public int size() {
        return descriptors.size();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FunctionCatalog that)) {
            return false;
        }
        return descriptors.equals(that.descriptors);
    }

    @Override
    public int hashCode() {
        return descriptors.hashCode();
    }

    private static boolean isCoercible(
            List<ExpressionType> sourceTypes,
            List<ExpressionType> targetTypes,
            BoundaryCoercion boundaryCoercion) {
        for (int index = 0; index < sourceTypes.size(); index++) {
            if (!boundaryCoercion.canConvert(sourceTypes.get(index), targetTypes.get(index))) {
                return false;
            }
        }
        return true;
    }

    private static int compareParameterTypes(List<ExpressionType> left, List<ExpressionType> right) {
        for (int index = 0; index < Math.min(left.size(), right.size()); index++) {
            int comparison = ExpressionTypes.canonical(left.get(index))
                    .compareTo(ExpressionTypes.canonical(right.get(index)));
            if (comparison != 0) {
                return comparison;
            }
        }
        return Integer.compare(left.size(), right.size());
    }

    public static final class Builder {

        private final Map<FunctionSignature, FunctionDescriptor> descriptors = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder register(FunctionDescriptor descriptor) {
            Objects.requireNonNull(descriptor, "descriptor");
            FunctionDescriptor previous = descriptors.putIfAbsent(descriptor.signature(), descriptor);
            if (previous != null) {
                throw new IllegalArgumentException("function already registered for signature: " + descriptor.signature());
            }
            return this;
        }

        public Builder replace(FunctionDescriptor descriptor) {
            Objects.requireNonNull(descriptor, "descriptor");
            descriptors.put(descriptor.signature(), descriptor);
            return this;
        }

        public FunctionCatalog build() {
            if (descriptors.isEmpty()) {
                return EMPTY;
            }
            return new FunctionCatalog(descriptors);
        }
    }

    private record NameArity(String languageName, int arity) implements Comparable<NameArity> {

        private NameArity {
            languageName = FunctionSignature.validateLanguageName(languageName);
            if (arity < 0) {
                throw new IllegalArgumentException("function arity must not be negative");
            }
        }

        private static NameArity from(FunctionSignature signature) {
            return new NameArity(signature.languageName(), signature.arity());
        }

        @Override
        public int compareTo(NameArity other) {
            int languageNameComparison = languageName.compareTo(other.languageName);
            if (languageNameComparison != 0) {
                return languageNameComparison;
            }
            return Integer.compare(arity, other.arity);
        }
    }
}
