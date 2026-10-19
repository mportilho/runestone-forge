package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils;
import com.runestone.dynafilter.modules.jpa.support.JpaSortCatalog;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.EntityType;
import org.springframework.beans.factory.ObjectProvider;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

/** Resolves the unambiguous persistence context for a declarative HTTP contract. */
public final class JpaSortContext {
    private final AnnotationStatementGenerator generator;
    private final ObjectProvider<EntityManagerFactory> factories;
    private volatile List<Unit> units;

    public JpaSortContext(AnnotationStatementGenerator generator, ObjectProvider<EntityManagerFactory> factories) {
        this.generator = java.util.Objects.requireNonNull(generator, "generator is required");
        this.factories = java.util.Objects.requireNonNull(factories, "factories are required");
    }

    public Map<String, String> catalog(Parameter parameter) {
        return resolve(parameter).catalog();
    }

    /** Resolves catalog and persistent schema metadata together using the compiled contract's root. */
    public ResolvedJpaSortContract resolve(Parameter parameter) {
        var input = new AnnotationStatementInput(parameter.getType(), parameter.getAnnotations());
        var contract = generator.sortContract(input);
        Class<?> entity = contract.declaredRoot();
        if (entity == null) {
            entity = TypeAnnotationUtils.findFilterTargetClass(parameter);
        }
        if (entity == null) {
            throw new DynamicFilterConfigurationException("Cannot determine JPA root for sort contract at " + parameter);
        }
        Class<?> root = entity;
        var matching = units().stream().flatMap(unit -> unit.factory().getMetamodel().getEntities().stream()
                .filter(type -> type.getJavaType().equals(root)).map(type -> new Binding(unit, type))).toList();
        if (matching.size() != 1) {
            throw new DynamicFilterConfigurationException("Sort contract requires one determinable persistence unit at " + parameter);
        }
        var binding = matching.getFirst();
        return new ResolvedJpaSortContract(binding.unit().catalog().resolve(contract, root), binding.entity());
    }

    private List<Unit> units() {
        List<Unit> current = units;
        if (current == null) {
            synchronized (this) {
                current = units;
                if (current == null) {
                    current = factories.stream().map(factory -> new Unit(factory, new JpaSortCatalog(factory))).toList();
                    units = current;
                }
            }
        }
        return current;
    }

    public void clearCache() {
        List<Unit> current = units;
        if (current != null) {
            current.forEach(unit -> unit.catalog().clearCache());
        }
        units = null;
    }

    private record Unit(EntityManagerFactory factory, JpaSortCatalog catalog) {
    }

    private record Binding(Unit unit, EntityType<?> entity) {
    }

}
