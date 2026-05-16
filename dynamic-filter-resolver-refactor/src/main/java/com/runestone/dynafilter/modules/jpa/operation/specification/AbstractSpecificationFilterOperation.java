package com.runestone.dynafilter.modules.jpa.operation.specification;

import com.runestone.converters.DataConversionService;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.operation.DefinedFilterOperation;
import com.runestone.dynafilter.core.operation.FilterOperation;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;

abstract class AbstractSpecificationFilterOperation implements FilterOperation<Specification<?>> {

    private final DataConversionService conversionService;

    AbstractSpecificationFilterOperation(DataConversionService conversionService) {
        this.conversionService = Objects.requireNonNull(conversionService, "conversionService must not be null");
    }

    @Override
    public final Specification<?> createFilter(FilterData filterData) {
        Objects.requireNonNull(filterData, "filterData must not be null");
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = createPredicate(filterData, root, query, criteriaBuilder);
            return filterData.negate() ? predicate.not() : predicate;
        };
    }

    abstract Predicate createPredicate(FilterData filterData, jakarta.persistence.criteria.Root<?> root,
            jakarta.persistence.criteria.CriteriaQuery<?> query, jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder);

    @Override
    public abstract Class<? extends DefinedFilterOperation> operationType();

    protected final Path<?> path(FilterData filterData, jakarta.persistence.criteria.Root<?> root) {
        return JpaPredicateUtils.computeAttributePath(filterData, root);
    }

    protected final Expression<?> joinPath(FilterData filterData, jakarta.persistence.criteria.Root<?> root) {
        return JpaPredicateUtils.computeAttributeJoinPath(filterData, root);
    }

    protected final Object converted(FilterData filterData, Path<?> path) {
        return convert(filterData.findOneValue(), path.getJavaType());
    }

    protected final Object convert(Object value, Class<?> targetType) {
        Class<?> conversionTarget = primitiveWrapper(targetType);
        if (value == null || conversionTarget.isInstance(value)) {
            return value;
        }
        return conversionService.convert(value, conversionTarget);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static Expression<? extends Comparable> comparable(Path<?> path) {
        return (Expression<? extends Comparable>) path;
    }

    private static Class<?> primitiveWrapper(Class<?> type) {
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        return type;
    }
}
