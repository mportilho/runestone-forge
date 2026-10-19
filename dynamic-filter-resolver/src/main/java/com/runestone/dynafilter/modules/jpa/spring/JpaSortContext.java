package com.runestone.dynafilter.modules.jpa.spring;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.TypeAnnotationUtils;
import com.runestone.dynafilter.modules.jpa.support.JpaSortCatalog;
import jakarta.persistence.EntityManagerFactory;
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
        Class<?> entity = TypeAnnotationUtils.findFilterTargetClass(parameter);
        if (entity == null) {
            throw new DynamicFilterConfigurationException("Cannot determine JPA root for sort contract at " + parameter);
        }
        List<Unit> matching = units().stream().filter(unit -> unit.factory().getMetamodel().getEntities().stream()
                .anyMatch(type -> type.getJavaType().equals(entity))).toList();
        if (matching.size() != 1) {
            throw new DynamicFilterConfigurationException("Sort contract requires one determinable persistence unit at " + parameter);
        }
        var input = new AnnotationStatementInput(parameter.getType(), parameter.getAnnotations());
        return matching.getFirst().catalog().resolve(generator.sortContract(input), entity);
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
}
