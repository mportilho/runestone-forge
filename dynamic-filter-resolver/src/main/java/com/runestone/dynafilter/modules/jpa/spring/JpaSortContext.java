package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils;
import com.runestone.dynafilter.modules.jpa.support.JpaSortCatalog;
import jakarta.persistence.EntityManagerFactory;
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
        Class<?> entity = TypeAnnotationUtils.findFilterTargetClass(parameter);
        if (entity == null) {
            throw new DynamicFilterConfigurationException("Cannot determine JPA root for sort contract at " + parameter);
        }
        EntityManagerFactory selected = persistenceUnitResolver != null ? persistenceUnitResolver.resolve(parameter, entity) : null;
        List<Unit> matching = units().stream().filter(unit -> selected == null || unit.factory() == selected)
                .filter(unit -> unit.factory().getMetamodel().getEntities().stream()
                .anyMatch(type -> type.getJavaType().equals(entity))).toList();
        if (matching.size() != 1) {
            throw new DynamicFilterConfigurationException("Sort contract requires one determinable persistence unit at " + parameter);
        }
        var input = new AnnotationStatementInput(parameter.getType(), parameter.getAnnotations());
        return matching.getFirst().catalog().resolve(generator.sortContract(input), entity);
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
}
