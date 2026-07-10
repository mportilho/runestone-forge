package com.runestone.expeval_mk3.api;

import java.util.ArrayList;
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
    private final Map<OverloadKey, List<FunctionDescriptor>> overloadsByNameAndArity;

    private FunctionCatalog(Map<FunctionSignature, FunctionDescriptor> functionsBySignature) {
        TreeMap<FunctionSignature, FunctionDescriptor> sorted = new TreeMap<>(functionsBySignature);
        this.functionsBySignature = Collections.unmodifiableNavigableMap(sorted);
        overloadsByNameAndArity = buildOverloads(sorted.values());
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
            List<ExpressionType> argumentTypes,
            BoundaryCoercion boundaryCoercion) {
        Objects.requireNonNull(boundaryCoercion, "boundaryCoercion");
        List<ExpressionType> immutableArgumentTypes = copyArgumentTypes(argumentTypes);
        FunctionSignature exactSignature = new FunctionSignature(languageName, immutableArgumentTypes);
        FunctionDescriptor exactMatch = functionsBySignature.get(exactSignature);
        if (exactMatch != null) {
            return FunctionLookupResult.exactMatch(exactMatch);
        }

        List<FunctionDescriptor> overloads = overloadsByNameAndArity.get(new OverloadKey(
                exactSignature.languageName(),
                exactSignature.arity()));
        if (overloads == null) {
            return FunctionLookupResult.notFound();
        }

        List<FunctionDescriptor> coercibleCandidates = new ArrayList<>();
        for (FunctionDescriptor overload : overloads) {
            if (isCoercible(immutableArgumentTypes, overload.parameterTypes(), boundaryCoercion)) {
                coercibleCandidates.add(overload);
            }
        }
        if (coercibleCandidates.isEmpty()) {
            return FunctionLookupResult.notFound();
        }
        if (coercibleCandidates.size() == 1) {
            return FunctionLookupResult.boundaryCoercionMatch(coercibleCandidates.getFirst());
        }
        return FunctionLookupResult.ambiguous(coercibleCandidates);
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

    private static boolean isCoercible(
            List<ExpressionType> argumentTypes,
            List<ExpressionType> parameterTypes,
            BoundaryCoercion boundaryCoercion) {
        if (argumentTypes.size() != parameterTypes.size()) {
            return false;
        }
        for (int index = 0; index < argumentTypes.size(); index++) {
            if (!boundaryCoercion.canConvert(argumentTypes.get(index), parameterTypes.get(index))) {
                return false;
            }
        }
        return true;
    }

    private static List<ExpressionType> copyArgumentTypes(List<ExpressionType> argumentTypes) {
        return ExpressionTypes.copyOf(argumentTypes, "argumentTypes");
    }

    private static Map<OverloadKey, List<FunctionDescriptor>> buildOverloads(Collection<FunctionDescriptor> descriptors) {
        Map<OverloadKey, List<FunctionDescriptor>> overloads = new LinkedHashMap<>();
        for (FunctionDescriptor descriptor : descriptors) {
            OverloadKey key = new OverloadKey(descriptor.languageName(), descriptor.arity());
            overloads.computeIfAbsent(key, ignored -> new ArrayList<>()).add(descriptor);
        }
        Map<OverloadKey, List<FunctionDescriptor>> immutableOverloads = new LinkedHashMap<>();
        for (Map.Entry<OverloadKey, List<FunctionDescriptor>> entry : overloads.entrySet()) {
            immutableOverloads.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Collections.unmodifiableMap(immutableOverloads);
    }

    public static final class Builder {

        private final Map<FunctionSignature, FunctionDescriptor> functions = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder register(FunctionDescriptor descriptor) {
            Objects.requireNonNull(descriptor, "descriptor");
            FunctionSignature signature = descriptor.signature();
            FunctionDescriptor previous = functions.putIfAbsent(signature, descriptor);
            if (previous != null) {
                throw new IllegalArgumentException("function signature already registered: " + signature.canonical());
            }
            return this;
        }

        public Builder replace(FunctionDescriptor descriptor) {
            Objects.requireNonNull(descriptor, "descriptor");
            FunctionSignature signature = descriptor.signature();
            if (!functions.containsKey(signature)) {
                throw new IllegalArgumentException("function signature is not registered: " + signature.canonical());
            }
            functions.put(signature, descriptor);
            return this;
        }

        public FunctionCatalog build() {
            if (functions.isEmpty()) {
                return EMPTY;
            }
            return new FunctionCatalog(functions);
        }
    }

    private record OverloadKey(String languageName, int arity) {

        private OverloadKey {
            languageName = FunctionSignature.validateLanguageName(languageName);
            if (arity < 0) {
                throw new IllegalArgumentException("arity must not be negative");
            }
        }
    }
}
