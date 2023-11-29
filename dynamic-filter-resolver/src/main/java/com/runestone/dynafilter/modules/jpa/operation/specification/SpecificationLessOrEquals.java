package com.runestone.dynafilter.modules.jpa.operation.specification;

import com.runestone.converters.DataConversionService;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.modifiers.ModIgnoreCase;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class SpecificationLessOrEquals<T> implements Specification<T> {

    private final FilterData filterData;
    private final DataConversionService dataConversionService;

    public SpecificationLessOrEquals(FilterData filterData, DataConversionService dataConversionService) {
        this.filterData = filterData;
        this.dataConversionService = dataConversionService;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Expression<? extends Comparable<?>> expression = JpaPredicateUtils.computeAttributePath(filterData, root);
        Object value = dataConversionService.convert(filterData.findOneValue(), expression.getJavaType());
        if (expression.getJavaType().equals(String.class) && filterData.hasModifier(ModIgnoreCase.class)) {
            expression = criteriaBuilder.upper((Expression<String>) expression);
            value = value != null ? value.toString().toUpperCase() : null;
        }
        return JpaPredicateUtils.toComparablePredicate(expression, value, criteriaBuilder::lessThanOrEqualTo, criteriaBuilder::le);
    }

}
