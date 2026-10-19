package com.runestone.dynafilter.modules.jpa.support;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.generator.SortContract;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Lob;
import jakarta.persistence.metamodel.Attribute;

import java.lang.reflect.AnnotatedElement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Effective catalogs cached within one persistence-unit owner, never Criteria execution objects. */
public final class JpaSortCatalog {
    private static final System.Logger LOG = System.getLogger(JpaSortCatalog.class.getName());
    private final EntityManagerFactory factory;
    private final Cache<Key, Map<String, String>> catalogs = Caffeine.newBuilder()
            .maximumSize(4096).executor(Runnable::run).build();

    public JpaSortCatalog(EntityManagerFactory factory) {
        this.factory = Objects.requireNonNull(factory, "EntityManagerFactory is required");
    }

    public Map<String, String> resolve(SortContract contract, Class<?> entity) {
        Objects.requireNonNull(entity, "entity is required");
        if (!factory.isOpen()) {
            clearCache();
            throw new DynamicFilterConfigurationException("Sort validation requires an open persistence unit");
        }
        return catalogs.get(new Key(contract != null ? contract : SortContract.EMPTY, entity), this::validate);
    }

    public void clearCache() {
        catalogs.invalidateAll();
    }

    private Map<String, String> validate(Key key) {
        var entity = factory.getMetamodel().entity(key.entity());
        Map<String, Attribute<?, ?>> attributes = new java.util.HashMap<>();
        for (Attribute<?, ?> attribute : entity.getAttributes()) {
            attributes.put(attribute.getName(), attribute);
        }
        Map<String, String> names = new LinkedHashMap<>();
        for (var candidate : key.contract().candidates()) {
            // Navigation and more mapping forms belong to subsequent increments.
            if (candidate.path().contains(".")) {
                continue;
            }
            Attribute<?, ?> attribute = attributes.get(candidate.path());
            if (attribute == null) {
                LOG.log(System.Logger.Level.DEBUG, "Omitting non-persistent sort candidate {0}", candidate.path());
                continue;
            }
            if (attribute.isCollection() || attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.BASIC
                    || attribute.getJavaType().isArray()
                    || attribute.getJavaMember() instanceof AnnotatedElement member && member.isAnnotationPresent(Lob.class)) {
                continue;
            }
            publish(names, candidate.path(), candidate.path());
            for (String alias : candidate.aliases()) {
                publish(names, alias, candidate.path());
            }
        }
        return Map.copyOf(names);
    }

    private static void publish(Map<String, String> names, String name, String path) {
        String previous = names.putIfAbsent(name, path);
        if (previous != null && !previous.equals(path)) {
            throw new DynamicFilterConfigurationException("Conflicting sort name '%s'".formatted(name));
        }
    }

    private record Key(SortContract contract, Class<?> entity) {
    }
}
