package com.runestone.dynafilter.modules.jpa.spring;

import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SingularAttribute;

import java.util.Map;
import java.util.Objects;

/** An effective sort catalog and the persistent metadata used for associated filter schemas. */
public record ResolvedJpaSortContract(Map<String, String> catalog, EntityType<?> entity) {
    public ResolvedJpaSortContract {
        catalog = Map.copyOf(catalog);
        Objects.requireNonNull(entity, "entity is required");
    }

    /**
     * Finds a persistent attribute for a filter schema, or returns null for a nonpersistent path.
     * Navigation can be plural here; this lookup does not contribute additional sort names.
     */
    public Attribute<?, ?> findAttribute(String path) {
        ManagedType<?> current = entity;
        String[] segments = path.split("\\.", -1);
        for (int i = 0; i < segments.length; i++) {
            Attribute<?, ?> attribute = null;
            for (var persistent : current.getAttributes()) {
                if (persistent.getName().equals(segments[i])) {
                    attribute = persistent;
                    break;
                }
            }
            if (attribute == null || i == segments.length - 1) {
                return attribute;
            }
            var type = attribute instanceof SingularAttribute<?, ?> singular ? singular.getType()
                    : ((PluralAttribute<?, ?, ?>) attribute).getElementType();
            if (!(type instanceof ManagedType<?> next)) {
                return null;
            }
            current = next;
        }
        return null;
    }
}
