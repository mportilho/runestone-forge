package com.runestone.dynafilter.modules.jpa.operation;

import com.runestone.converters.DataConversionService;
import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.dynafilter.core.operation.AbstractFilterOperationService;
import com.runestone.dynafilter.core.operation.FilterOperation;
import com.runestone.dynafilter.modules.jpa.operation.specification.SpecificationBetween;
import com.runestone.dynafilter.modules.jpa.operation.specification.SpecificationEndsWith;
import com.runestone.dynafilter.modules.jpa.operation.specification.SpecificationEquals;
import com.runestone.dynafilter.modules.jpa.operation.specification.SpecificationGreater;
import com.runestone.dynafilter.modules.jpa.operation.specification.SpecificationGreaterOrEquals;
import com.runestone.dynafilter.modules.jpa.operation.specification.SpecificationIsIn;
import com.runestone.dynafilter.modules.jpa.operation.specification.SpecificationIsNull;
import com.runestone.dynafilter.modules.jpa.operation.specification.SpecificationLess;
import com.runestone.dynafilter.modules.jpa.operation.specification.SpecificationLessOrEquals;
import com.runestone.dynafilter.modules.jpa.operation.specification.SpecificationLike;
import com.runestone.dynafilter.modules.jpa.operation.specification.SpecificationStartsWith;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public final class SpecificationFilterOperationService extends AbstractFilterOperationService<Specification<?>> {

    public SpecificationFilterOperationService() {
        this(new DefaultDataConversionService(false));
    }

    public SpecificationFilterOperationService(DataConversionService conversionService) {
        super(defaultOperations(conversionService));
    }

    public SpecificationFilterOperationService(List<? extends FilterOperation<Specification<?>>> operations) {
        super(operations);
    }

    private static List<FilterOperation<Specification<?>>> defaultOperations(DataConversionService conversionService) {
        return List.of(
                new SpecificationEquals(conversionService),
                new SpecificationLike(conversionService),
                new SpecificationStartsWith(conversionService),
                new SpecificationEndsWith(conversionService),
                new SpecificationLess(conversionService),
                new SpecificationLessOrEquals(conversionService),
                new SpecificationGreater(conversionService),
                new SpecificationGreaterOrEquals(conversionService),
                new SpecificationBetween(conversionService),
                new SpecificationIsIn(conversionService),
                new SpecificationIsNull(conversionService)
        );
    }
}
