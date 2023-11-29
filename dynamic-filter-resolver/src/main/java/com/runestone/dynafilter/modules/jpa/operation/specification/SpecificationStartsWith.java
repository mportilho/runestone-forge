package com.runestone.dynafilter.modules.jpa.operation.specification;

import com.runestone.converters.DataConversionService;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.modifiers.ModIgnoreCase;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class SpecificationStartsWith<T> implements Specification<T> {

    private final FilterData filterData;
    private final DataConversionService dataConversionService;

    public SpecificationStartsWith(FilterData filterData, DataConversionService dataConversionService) {
        this.filterData = filterData;
        this.dataConversionService = dataConversionService;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Path<String> path = JpaPredicateUtils.computeAttributePath(filterData, root);
        String value = dataConversionService.convert(filterData.findOneValue(), path.getJavaType());

        Expression<String> expression;
        if (filterData.hasModifier(ModIgnoreCase.class)) {
            expression = criteriaBuilder.upper(path);
            value = value != null ? value.toUpperCase() + "%" : null;
        } else {
            expression = path;
            value = value != null ? value + "%" : null;
        }
        return criteriaBuilder.like(expression, value);
    }

}
