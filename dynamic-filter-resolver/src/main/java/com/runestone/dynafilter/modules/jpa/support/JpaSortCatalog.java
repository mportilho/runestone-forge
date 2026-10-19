package com.runestone.dynafilter.modules.jpa.support;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.generator.SortContract;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Lob;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.IdentifiableType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.PluralAttribute;
import org.springframework.data.jpa.provider.PersistenceProvider;

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
        var metamodel = factory.getMetamodel();
        var entity = metamodel.entity(key.entity());
        var storage = PersistenceProvider.fromMetamodel(metamodel) == PersistenceProvider.HIBERNATE
                ? new HibernateSortStorage(factory) : null;
        validateRoot(key.contract().declaredRoot(), entity, metamodel);
        for (String exclusion : key.contract().exclusions()) {
            if (resolveAttribute(entity, exclusion, false) == null) {
                throw new DynamicFilterConfigurationException("Invalid sort exclusion '%s'".formatted(exclusion));
            }
        }
        Map<String, String> names = new LinkedHashMap<>();
        for (var candidate : key.contract().candidates()) {
            if (!isEligible(entity, candidate.path(), storage)) {
                rejectExplicitOrOmit(candidate);
                continue;
            }
            publish(names, candidate.path(), candidate.path());
            for (String alias : candidate.aliases()) {
                if (candidate.explicit() && (alias.isBlank() || !alias.equals(alias.strip()))) {
                    throw new DynamicFilterConfigurationException("Invalid explicit sort alias '%s'".formatted(alias));
                }
                if (candidate.explicit() && !alias.equals(candidate.path()) && isEligible(entity, alias, storage)) {
                    throw new DynamicFilterConfigurationException("Conflicting sort name '%s'".formatted(alias));
                }
                publish(names, alias, candidate.path());
            }
        }
        names.values().removeAll(key.contract().exclusions());
        return Map.copyOf(names);
    }

    private static void rejectExplicitOrOmit(SortContract.Candidate candidate) {
        if (candidate.explicit()) {
            throw new DynamicFilterConfigurationException("Invalid explicit sort path '%s'".formatted(candidate.path()));
        }
        LOG.log(System.Logger.Level.DEBUG, "Omitting ineligible sort candidate {0}", candidate.path());
    }

    private static void validateRoot(Class<?> declaredRoot, EntityType<?> effective, Metamodel metamodel) {
        if (declaredRoot == null) {
            return;
        }
        try {
            metamodel.entity(declaredRoot);
        } catch (IllegalArgumentException exception) {
            throw new DynamicFilterConfigurationException("Declared sort root is not a JPA entity: " + declaredRoot.getName(), exception);
        }
        for (IdentifiableType<?> type = effective; type != null; type = type.getSupertype()) {
            if (type.getJavaType().equals(declaredRoot)) {
                return;
            }
        }
        throw new DynamicFilterConfigurationException("Effective JPA entity is incompatible with declared sort root " + declaredRoot.getName());
    }

    private static boolean isEligible(ManagedType<?> root, String path, HibernateSortStorage storage) {
        var attribute = resolveAttribute(root, path, true);
        return attribute instanceof SingularAttribute<?, ?> singular
                && singular.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC
                && !isBinary(singular.getJavaType())
                && !java.sql.Clob.class.isAssignableFrom(singular.getJavaType())
                && (storage != null ? !storage.isLobOrUnsupportedBinary(root.getJavaType(), path)
                    : !(singular.getJavaMember() instanceof AnnotatedElement member && member.isAnnotationPresent(Lob.class)));
    }

    /** One metamodel traversal for sortable paths and preventive existence-only exclusions. */
    private static Attribute<?, ?> resolveAttribute(ManagedType<?> root, String path, boolean requireSingular) {
        String[] segments = path.split("\\.", -1);
        ManagedType<?> current = root;
        for (int i = 0; i < segments.length; i++) {
            // getAttributes includes inherited attributes and honors the provider's access strategy.
            Attribute<?, ?> attribute = null;
            for (var persistent : current.getAttributes()) {
                if (persistent.getName().equals(segments[i])) {
                    attribute = persistent;
                    break;
                }
            }
            if (attribute == null || requireSingular && !(attribute instanceof SingularAttribute<?, ?>)) {
                return null;
            }
            if (i == segments.length - 1) {
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

    private static boolean isBinary(Class<?> type) {
        return type == byte[].class || type == Byte[].class || java.sql.Blob.class.isAssignableFrom(type)
                || java.nio.ByteBuffer.class.isAssignableFrom(type);
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
