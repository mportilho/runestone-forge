package com.runestone.expeval_mk3.api;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Immutable catalog of registered expression functions keyed by Assinatura de Funcao.
 */
public final class FunctionCatalog {

    private static final FunctionCatalog EMPTY = new FunctionCatalog(Map.of());

    private final NavigableMap<FunctionSignature, FunctionDescriptor> functionsBySignature;

    private FunctionCatalog(Map<FunctionSignature, FunctionDescriptor> functionsBySignature) {
        TreeMap<FunctionSignature, FunctionDescriptor> sorted = new TreeMap<>(functionsBySignature);
        this.functionsBySignature = Collections.unmodifiableNavigableMap(sorted);
    }

    public static FunctionCatalog empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<FunctionDescriptor> find(FunctionSignature signature) {
        return Optional.ofNullable(functionsBySignature.get(Objects.requireNonNull(signature, "signature")));
    }

    public FunctionLookupResult resolve(
            String languageName,
            List<ExpressionType> argumentTypes) {
        List<ExpressionType> immutableArgumentTypes = copyArgumentTypes(argumentTypes);
        FunctionSignature exactSignature = new FunctionSignature(languageName, immutableArgumentTypes);
        FunctionDescriptor exactMatch = functionsBySignature.get(exactSignature);
        if (exactMatch != null) {
            return FunctionLookupResult.exactMatch(exactMatch);
        }
        return FunctionLookupResult.notFound();
    }

    public Collection<FunctionDescriptor> values() {
        return functionsBySignature.values();
    }

    public int size() {
        return functionsBySignature.size();
    }

    @Override
    public String toString() {
        return "FunctionCatalog[size=" + functionsBySignature.size() + ']';
    }

    private static List<ExpressionType> copyArgumentTypes(List<ExpressionType> argumentTypes) {
        return ExpressionTypes.copyOf(argumentTypes, "argumentTypes");
    }

    public static final class Builder {

        private final Map<FunctionSignature, Registration> functions = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder register(FunctionDescriptor descriptor) {
            Objects.requireNonNull(descriptor, "descriptor");
            return register(descriptor, FunctionProvenance.custom(descriptor));
        }

        Builder registerImported(FunctionDescriptor descriptor) {
            return register(descriptor, FunctionProvenance.imported(descriptor));
        }

        Builder registerOfficial(FunctionDescriptor descriptor) {
            return register(descriptor, FunctionProvenance.official(descriptor));
        }

        private Builder register(FunctionDescriptor descriptor, FunctionProvenance provenance) {
            Objects.requireNonNull(descriptor, "descriptor");
            FunctionSignature signature = descriptor.signature();
            Registration registration = new Registration(descriptor, provenance);
            Registration previous = functions.putIfAbsent(signature, registration);
            if (previous != null) {
                throw duplicateRegistration(signature, previous.provenance(), provenance);
            }
            return this;
        }

        public Builder replace(FunctionDescriptor descriptor) {
            Objects.requireNonNull(descriptor, "descriptor");
            return replace(descriptor, FunctionProvenance.custom(descriptor));
        }

        Builder replaceImported(FunctionDescriptor descriptor) {
            return replace(descriptor, FunctionProvenance.imported(descriptor));
        }

        private Builder replace(FunctionDescriptor descriptor, FunctionProvenance replacementProvenance) {
            Objects.requireNonNull(descriptor, "descriptor");
            FunctionSignature signature = descriptor.signature();
            Registration existing = functions.get(signature);
            if (existing == null) {
                throw new IllegalArgumentException(
                        "function replacement has no custom target: " + signature.canonical());
            }
            if (existing.provenance().official()) {
                throw new IllegalArgumentException(
                        "official built-in function cannot be replaced: " + signature.canonical()
                                + " from " + existing.provenance().description());
            }
            functions.put(signature, new Registration(descriptor, replacementProvenance));
            return this;
        }

        public FunctionCatalog build() {
            if (functions.isEmpty()) {
                return EMPTY;
            }
            Map<FunctionSignature, FunctionDescriptor> descriptors = new LinkedHashMap<>();
            for (Map.Entry<FunctionSignature, Registration> entry : functions.entrySet()) {
                descriptors.put(entry.getKey(), entry.getValue().descriptor());
            }
            return new FunctionCatalog(descriptors);
        }

        private static IllegalArgumentException duplicateRegistration(
                FunctionSignature signature,
                FunctionProvenance first,
                FunctionProvenance second) {
            List<String> origins = java.util.stream.Stream.of(first.description(), second.description())
                    .sorted()
                    .toList();
            return new IllegalArgumentException("function signature already registered: " + signature.canonical()
                    + "; origins: " + origins.getFirst() + " and " + origins.getLast());
        }
    }

    private record Registration(FunctionDescriptor descriptor, FunctionProvenance provenance) {
    }

    private record FunctionProvenance(boolean official, String description) {

        private FunctionProvenance {
            Objects.requireNonNull(description, "description");
        }

        private static FunctionProvenance official(FunctionDescriptor descriptor) {
            return new FunctionProvenance(true, "official built-in " + implementation(descriptor));
        }

        private static FunctionProvenance custom(FunctionDescriptor descriptor) {
            return new FunctionProvenance(false, "custom function " + implementation(descriptor));
        }

        private static FunctionProvenance imported(FunctionDescriptor descriptor) {
            return new FunctionProvenance(false, "imported provider " + implementation(descriptor));
        }

        private static String implementation(FunctionDescriptor descriptor) {
            return descriptor.implementationMetadata().describeImplementation();
        }
    }

}
