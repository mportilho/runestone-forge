package com.runestone.dynafilter.modules.jpa.operation.specification;

import com.runestone.converters.DataConversionService;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.modifiers.ModIgnoreCase;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class SpecificationEquals<T> implements Specification<T> {

    private final FilterData filterData;
    private final DataConversionService dataConversionService;

    public SpecificationEquals(FilterData filterData, DataConversionService dataConversionService) {
        this.filterData = filterData;
        this.dataConversionService = dataConversionService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Expression<T> expression = JpaPredicateUtils.computeAttributePath(filterData, root);
        Object value = dataConversionService.convert(filterData.findOneValue(), expression.getJavaType());

        if (expression.getJavaType().equals(String.class) && filterData.hasModifier(ModIgnoreCase.class)) {
            expression = (Expression<T>) criteriaBuilder.upper((Expression<String>) expression);
            value = value != null ? value.toString().toUpperCase() : null;
        }
        return criteriaBuilder.equal(expression, value);
    }

}
