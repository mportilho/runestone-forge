package com.runestone.expeval2.catalog;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class FunctionCatalog {

    private final Map<String, List<FunctionDescriptor>> descriptorsByName;

    public FunctionCatalog(Map<String, List<FunctionDescriptor>> descriptorsByName) {
        this.descriptorsByName = immutableLists(Objects.requireNonNull(descriptorsByName, "descriptorsByName must not be null"));
    }

    public Optional<FunctionDescriptor> findExact(String name, int arity) {
        Objects.requireNonNull(name, "name must not be null");
        List<FunctionDescriptor> candidates = descriptorsByName.getOrDefault(name, List.of()).stream()
            .filter(descriptor -> descriptor.arity() == arity)
            .toList();
        return candidates.size() == 1 ? Optional.of(candidates.getFirst()) : Optional.empty();
    }

    public Collection<FunctionDescriptor> findCandidates(String name) {
        Objects.requireNonNull(name, "name must not be null");
        return descriptorsByName.getOrDefault(name, List.of());
    }

    private static Map<String, List<FunctionDescriptor>> immutableLists(Map<String, List<FunctionDescriptor>> values) {
        Map<String, List<FunctionDescriptor>> copy = new LinkedHashMap<>();
        values.forEach((key, list) -> copy.put(key, List.copyOf(list)));
        return Map.copyOf(copy);
    }
}
