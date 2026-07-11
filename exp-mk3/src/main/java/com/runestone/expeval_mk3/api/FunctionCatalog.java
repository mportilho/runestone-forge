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

}
