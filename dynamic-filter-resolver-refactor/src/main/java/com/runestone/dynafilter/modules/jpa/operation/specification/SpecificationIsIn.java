package com.runestone.dynafilter.modules.jpa.operation.specification;

import com.runestone.converters.DataConversionService;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.operation.DefinedFilterOperation;
import com.runestone.dynafilter.core.operation.IsIn;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.Collection;

public final class SpecificationIsIn extends AbstractSpecificationFilterOperation {

    public SpecificationIsIn(DataConversionService conversionService) {
        super(conversionService);
    }

    @Override
    public Class<? extends DefinedFilterOperation> operationType() {
        return IsIn.class;
    }

    @Override
    Predicate createPredicate(FilterData filterData, Root<?> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Path<?> path = path(filterData, root);
        Expression<?> expression = path;
        if (Collection.class.isAssignableFrom(path.getJavaType())) {
            expression = joinPath(filterData, root);
            query.distinct(true);
        }
        CriteriaBuilder.In<Object> in = criteriaBuilder.in(expression);
        for (Object value : filterData.values()) {
            in.value(convert(value, expression.getJavaType()));
        }
        return in;
    }
}
