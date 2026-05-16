package com.runestone.dynafilter.modules.jpa.operation.specification;

import com.runestone.converters.DataConversionService;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.operation.Between;
import com.runestone.dynafilter.core.operation.DefinedFilterOperation;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public final class SpecificationBetween extends AbstractSpecificationFilterOperation {

    public SpecificationBetween(DataConversionService conversionService) {
        super(conversionService);
    }

    @Override
    public Class<? extends DefinedFilterOperation> operationType() {
        return Between.class;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    Predicate createPredicate(FilterData filterData, Root<?> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Object[] values = filterData.values();
        if (values.length != 2) {
            throw new IllegalArgumentException("Between operation requires exactly two values");
        }
        Path<?> path = path(filterData, root);
        return criteriaBuilder.between(comparable(path), (Comparable) convert(values[0], path.getJavaType()),
                (Comparable) convert(values[1], path.getJavaType()));
    }
}
