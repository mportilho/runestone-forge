package com.runestone.dynafilter.modules.jpa.operation.specification;

import com.runestone.converters.DataConversionService;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.modifier.ModIgnoreCase;
import com.runestone.dynafilter.core.operation.DefinedFilterOperation;
import com.runestone.dynafilter.core.operation.StartsWith;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public final class SpecificationStartsWith extends AbstractSpecificationFilterOperation {

    public SpecificationStartsWith(DataConversionService conversionService) {
        super(conversionService);
    }

    @Override
    public Class<? extends DefinedFilterOperation> operationType() {
        return StartsWith.class;
    }

    @Override
    Predicate createPredicate(FilterData filterData, Root<?> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Path<?> path = path(filterData, root);
        String value = String.valueOf(converted(filterData, path));
        Expression<String> expression = path.as(String.class);
        if (filterData.hasModifier(ModIgnoreCase.class)) {
            return criteriaBuilder.like(criteriaBuilder.lower(expression), value.toLowerCase() + '%');
        }
        return criteriaBuilder.like(expression, value + '%');
    }
}
