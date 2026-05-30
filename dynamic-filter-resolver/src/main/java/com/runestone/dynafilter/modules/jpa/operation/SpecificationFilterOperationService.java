/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2023 Marcelo Silva Portilho
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.runestone.dynafilter.modules.jpa.operation;

import com.runestone.converters.DataConversionService;
import com.runestone.dynafilter.core.operation.AbstractFilterOperationService;
import com.runestone.dynafilter.core.operation.FilterOperation;
import com.runestone.dynafilter.core.operation.FilterOperationRegistry;
import com.runestone.dynafilter.core.operation.types.*;
import com.runestone.dynafilter.modules.jpa.operation.specification.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SpecificationFilterOperationService extends AbstractFilterOperationService<Specification<?>> {

    public SpecificationFilterOperationService(DataConversionService conversionService) {
        this(conversionService, List.of());
    }

    public SpecificationFilterOperationService(DataConversionService conversionService, List<SpecificationFilterOperationContributor> contributors) {
        super(() -> createOperationMap(conversionService, contributors));
    }

    @SuppressWarnings("rawtypes")
    private static Map<Class<? extends FilterOperation>, FilterOperation<Specification<?>>> createOperationMap(
            DataConversionService conversionService,
            List<SpecificationFilterOperationContributor> contributors
    ) {
        Objects.requireNonNull(conversionService, "conversionService cannot be null");
        Objects.requireNonNull(contributors, "contributors cannot be null");

        FilterOperationRegistry<Specification<?>> registry = new FilterOperationRegistry<>();
        registerBuiltInOperations(registry, conversionService);
        for (SpecificationFilterOperationContributor contributor : contributors) {
            Objects.requireNonNull(contributor, "contributor cannot be null").contribute(registry);
        }
        return registry.toMap();
    }

    private static void registerBuiltInOperations(FilterOperationRegistry<Specification<?>> registry, DataConversionService conversionService) {
        registry.register(Between.class, (Between<Specification<?>>) filterData -> new SpecificationBetween<>(filterData, conversionService));
        registry.register(EndsWith.class, (EndsWith<Specification<?>>) filterData -> new SpecificationEndsWith<>(filterData, conversionService));
        registry.register(Equals.class, (Equals<Specification<?>>) filterData -> new SpecificationEquals<>(filterData, conversionService));
        registry.register(Greater.class, (Greater<Specification<?>>) filterData -> new SpecificationGreater<>(filterData, conversionService));
        registry.register(GreaterOrEquals.class, (GreaterOrEquals<Specification<?>>) filterData -> new SpecificationGreaterOrEquals<>(filterData, conversionService));
        registry.register(IsIn.class, (IsIn<Specification<?>>) filterData -> new SpecificationIsIn<>(filterData, conversionService));
        registry.register(IsNull.class, (IsNull<Specification<?>>) filterData -> new SpecificationIsNull<>(filterData, conversionService));
        registry.register(Less.class, (Less<Specification<?>>) filterData -> new SpecificationLess<>(filterData, conversionService));
        registry.register(LessOrEquals.class, (LessOrEquals<Specification<?>>) filterData -> new SpecificationLessOrEquals<>(filterData, conversionService));
        registry.register(Like.class, (Like<Specification<?>>) filterData -> new SpecificationLike<>(filterData, conversionService));
        registry.register(StartsWith.class, (StartsWith<Specification<?>>) filterData -> new SpecificationStartsWith<>(filterData, conversionService));
    }

}
