package com.runestone.dynafilter.modules.jpa.operation.specification;

import com.runestone.converters.DataConversionService;
import com.runestone.dynafilter.core.model.FilterData;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class SpecificationIsNull<T> implements Specification<T> {

    private final FilterData filterData;
    private final DataConversionService dataConversionService;

    public SpecificationIsNull(FilterData filterData, DataConversionService dataConversionService) {
        this.filterData = filterData;
        this.dataConversionService = dataConversionService;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        return dataConversionService.convert(filterData.findOneValue(), Boolean.class)
                ? criteriaBuilder.isNull(JpaPredicateUtils.computeAttributePath(filterData, root))
                : criteriaBuilder.isNotNull(JpaPredicateUtils.computeAttributePath(filterData, root));
    }

}
