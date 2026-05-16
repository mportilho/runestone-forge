package com.runestone.dynafilter.modules.jpa.operation.specification;

import com.runestone.converters.DataConversionService;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.operation.DefinedFilterOperation;
import com.runestone.dynafilter.core.operation.IsNull;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public final class SpecificationIsNull extends AbstractSpecificationFilterOperation {

    public SpecificationIsNull(DataConversionService conversionService) {
        super(conversionService);
    }

    @Override
    public Class<? extends DefinedFilterOperation> operationType() {
        return IsNull.class;
    }

    @Override
    Predicate createPredicate(FilterData filterData, Root<?> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Path<?> path = path(filterData, root);
        Object value = filterData.findOneValue();
        boolean isNull = value instanceof Boolean booleanValue ? booleanValue : Boolean.parseBoolean(String.valueOf(value));
        return isNull ? criteriaBuilder.isNull(path) : criteriaBuilder.isNotNull(path);
    }
}
