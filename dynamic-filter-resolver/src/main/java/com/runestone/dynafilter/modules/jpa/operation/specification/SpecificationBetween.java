package com.runestone.dynafilter.modules.jpa.operation.specification;

import com.runestone.converters.DataConversionService;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.modifiers.ModIgnoreCase;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class SpecificationBetween<T> implements Specification<T> {

    private final FilterData filterData;
    private final DataConversionService dataConversionService;

    public SpecificationBetween(FilterData filterData, DataConversionService dataConversionService) {
        this.filterData = filterData;
        this.dataConversionService = dataConversionService;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Expression<Comparable> expression = JpaPredicateUtils.computeAttributePath(filterData, root);
        Comparable lowerValue = dataConversionService.convert(filterData.findValueOnIndex(0), expression.getJavaType());
        Comparable upperValue = dataConversionService.convert(filterData.findValueOnIndex(1), expression.getJavaType());

        if (expression.getJavaType().equals(String.class) && filterData.hasModifier(ModIgnoreCase.class)) {
            expression = criteriaBuilder.upper((Expression) expression);
            lowerValue = lowerValue != null ? lowerValue.toString().toUpperCase() : null;
            upperValue = upperValue != null ? upperValue.toString().toUpperCase() : null;
        }
        return criteriaBuilder.between(expression, lowerValue, upperValue);
    }

}
