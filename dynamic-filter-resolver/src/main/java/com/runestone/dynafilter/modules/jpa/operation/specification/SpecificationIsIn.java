package com.runestone.dynafilter.modules.jpa.operation.specification;

import com.runestone.converters.DataConversionService;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.modifiers.ModIgnoreCase;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;

public class SpecificationIsIn<T> implements Specification<T> {

    private final FilterData filterData;
    private final DataConversionService dataConversionService;

    public SpecificationIsIn(FilterData filterData, DataConversionService dataConversionService) {
        this.filterData = filterData;
        this.dataConversionService = dataConversionService;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Expression expressionTemp = JpaPredicateUtils.computeAttributePath(filterData, root);
        Object rawValues = filterData.values()[0];
        Object[] arrayValues = rawValues instanceof Object[] arr ? arr : new Object[]{rawValues};
        boolean ignoreCase = expressionTemp.getJavaType().equals(String.class) && filterData.hasModifier(ModIgnoreCase.class);

        Expression expression = ignoreCase ? criteriaBuilder.upper(expressionTemp) : expressionTemp;
        Object[] arr = Arrays.stream(arrayValues).map(v -> {
            Object valueTemp = dataConversionService.convert(v, expression.getJavaType());
            return ignoreCase && valueTemp != null ? valueTemp.toString().toUpperCase() : valueTemp;
        }).toArray();
        return expression.in(arr);
    }

}
