package com.runestone.dynafilter.modules.jpa.support;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.mapping.BasicValuedModelPart;
import org.hibernate.metamodel.mapping.EmbeddableValuedModelPart;
import org.hibernate.metamodel.mapping.EntityAssociationMapping;
import org.hibernate.metamodel.mapping.ModelPart;
import org.hibernate.metamodel.mapping.ModelPartContainer;

/** Effective JDBC types supplement the JPA metamodel, which has no LOB or storage-type metadata. */
final class HibernateSortStorage {
    private final SessionFactoryImplementor factory;

    HibernateSortStorage(EntityManagerFactory factory) {
        this.factory = factory.unwrap(SessionFactoryImplementor.class);
    }

    boolean isLobOrBinary(Class<?> entity, String path) {
        ModelPartContainer container = factory.getMappingMetamodel().getEntityDescriptor(entity);
        String[] segments = path.split("\\.", -1);
        for (int i = 0; i < segments.length; i++) {
            ModelPart part = container.findSubPart(segments[i], null);
            if (i == segments.length - 1 && part instanceof BasicValuedModelPart basic) {
                var jdbcType = basic.getJdbcMapping().getJdbcType();
                return jdbcType.isLob() || jdbcType.isBinary();
            }
            if (part instanceof EntityAssociationMapping association) {
                container = association.getAssociatedEntityMappingType();
            } else if (part instanceof EmbeddableValuedModelPart embedded) {
                container = embedded.getEmbeddableTypeDescriptor();
            } else {
                throw new DynamicFilterConfigurationException("Cannot determine storage mapping for sort path '%s'".formatted(path));
            }
        }
        throw new DynamicFilterConfigurationException("Cannot determine storage mapping for sort path '%s'".formatted(path));
    }
}
