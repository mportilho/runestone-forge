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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Resolves persistence metadata for HTTP contracts. Unit-cache ownership is synchronized;
 * lifecycle cleanup and discovery never share validation across factory identities.
 */
public final class JpaSortContext {
    private final AnnotationStatementGenerator generator;
    private final ObjectProvider<EntityManagerFactory> factories;
    private final JpaSortPersistenceUnitResolver persistenceUnitResolver;
    private List<Unit> units = List.of();

    public JpaSortContext(AnnotationStatementGenerator generator, ObjectProvider<EntityManagerFactory> factories) {
        this(generator, factories, null);
    }

    public JpaSortContext(AnnotationStatementGenerator generator, ObjectProvider<EntityManagerFactory> factories,
                          JpaSortPersistenceUnitResolver persistenceUnitResolver) {
        this.generator = java.util.Objects.requireNonNull(generator, "generator is required");
        this.factories = java.util.Objects.requireNonNull(factories, "factories are required");
        this.persistenceUnitResolver = persistenceUnitResolver;
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
        EntityManagerFactory selected = persistenceUnitResolver != null ? persistenceUnitResolver.resolve(parameter, entity) : null;
        Class<?> root = entity;
        var matching = units().stream().filter(unit -> selected == null || unit.factory() == selected)
                .flatMap(unit -> unit.factory().getMetamodel().getEntities().stream()
                .filter(type -> type.getJavaType().equals(root)).map(type -> new Binding(unit, type))).toList();
        if (matching.size() != 1) {
            throw new DynamicFilterConfigurationException("Sort contract requires one determinable persistence unit at " + parameter);
        }
        var binding = matching.getFirst();
        return new ResolvedJpaSortContract(binding.unit().catalog().resolve(contract, root), binding.entity());
    }

    private List<Unit> units() {
        List<EntityManagerFactory> active = factories.stream().filter(EntityManagerFactory::isOpen).toList();
        synchronized (this) {
            List<Unit> current = new ArrayList<>(active.size());
            for (EntityManagerFactory factory : active) {
                current.add(units.stream().filter(unit -> unit.factory() == factory).findFirst()
                        .orElseGet(() -> new Unit(factory, new JpaSortCatalog(factory))));
            }
            units.stream().filter(unit -> active.stream().noneMatch(factory -> factory == unit.factory()))
                    .forEach(unit -> unit.catalog().clearCache());
            units = List.copyOf(current);
            return units;
        }
    }

    public synchronized void clearCache() {
        units.forEach(unit -> unit.catalog().clearCache());
        units = List.of();
    }

    private record Unit(EntityManagerFactory factory, JpaSortCatalog catalog) {
    }

    private record Binding(Unit unit, EntityType<?> entity) {
    }

}
