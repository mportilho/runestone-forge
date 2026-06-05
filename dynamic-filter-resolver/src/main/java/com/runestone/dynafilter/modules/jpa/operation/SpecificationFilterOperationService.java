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
import com.runestone.dynafilter.core.operation.FilterOperationMetadata;
import com.runestone.dynafilter.core.operation.FilterOperationRegistry;
import com.runestone.dynafilter.core.operation.types.*;
import com.runestone.dynafilter.core.operation.types.extensions.*;
import com.runestone.dynafilter.modules.jpa.operation.specification.*;
import com.runestone.dynafilter.modules.jpa.operation.specification.extensions.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

public class SpecificationFilterOperationService extends AbstractFilterOperationService<Specification<?>> {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneOffset.UTC;

    public SpecificationFilterOperationService(DataConversionService conversionService) {
        this(conversionService, DEFAULT_ZONE_ID, List.of());
    }

    public SpecificationFilterOperationService(DataConversionService conversionService, List<SpecificationFilterOperationContributor> contributors) {
        this(conversionService, DEFAULT_ZONE_ID, contributors);
    }

    public SpecificationFilterOperationService(DataConversionService conversionService, ZoneId zoneId) {
        this(conversionService, zoneId, List.of());
    }

    public SpecificationFilterOperationService(
            DataConversionService conversionService,
            ZoneId zoneId,
            List<SpecificationFilterOperationContributor> contributors
    ) {
        super(createOperationRegistry(conversionService, zoneId, contributors));
    }

    private static FilterOperationRegistry<Specification<?>> createOperationRegistry(
            DataConversionService conversionService,
            ZoneId zoneId,
            List<SpecificationFilterOperationContributor> contributors
    ) {
        Objects.requireNonNull(conversionService, "conversionService cannot be null");
        Objects.requireNonNull(zoneId, "zoneId cannot be null");
        Objects.requireNonNull(contributors, "contributors cannot be null");

        FilterOperationRegistry<Specification<?>> registry = new FilterOperationRegistry<>();
        registerBuiltInOperations(registry, conversionService, zoneId);
        for (SpecificationFilterOperationContributor contributor : contributors) {
            Objects.requireNonNull(contributor, "contributor cannot be null").contribute(registry);
        }
        return registry;
    }

    private static void registerBuiltInOperations(FilterOperationRegistry<Specification<?>> registry, DataConversionService conversionService, ZoneId zoneId) {
        registry.register(Between.class, (Between<Specification<?>>) filterData -> new SpecificationBetween<>(filterData, conversionService), FilterOperationMetadata.targetField());
        registry.register(EndsWith.class, (EndsWith<Specification<?>>) filterData -> new SpecificationEndsWith<>(filterData, conversionService), FilterOperationMetadata.targetField());
        registry.register(Equals.class, (Equals<Specification<?>>) filterData -> new SpecificationEquals<>(filterData, conversionService), FilterOperationMetadata.targetField());
        registry.register(Greater.class, (Greater<Specification<?>>) filterData -> new SpecificationGreater<>(filterData, conversionService), FilterOperationMetadata.targetField());
        registry.register(GreaterOrEquals.class, (GreaterOrEquals<Specification<?>>) filterData -> new SpecificationGreaterOrEquals<>(filterData, conversionService), FilterOperationMetadata.targetField());
        registry.register(IsIn.class, (IsIn<Specification<?>>) filterData -> new SpecificationIsIn<>(filterData, conversionService), FilterOperationMetadata.arrayValue());
        registry.register(IsNull.class, (IsNull<Specification<?>>) filterData -> new SpecificationIsNull<>(filterData, conversionService), FilterOperationMetadata.booleanValue());
        registry.register(Less.class, (Less<Specification<?>>) filterData -> new SpecificationLess<>(filterData, conversionService), FilterOperationMetadata.targetField());
        registry.register(LessOrEquals.class, (LessOrEquals<Specification<?>>) filterData -> new SpecificationLessOrEquals<>(filterData, conversionService), FilterOperationMetadata.targetField());
        registry.register(Like.class, (Like<Specification<?>>) filterData -> new SpecificationLike<>(filterData, conversionService), FilterOperationMetadata.targetField());
        registry.register(StartsWith.class, (StartsWith<Specification<?>>) filterData -> new SpecificationStartsWith<>(filterData, conversionService), FilterOperationMetadata.targetField());
        registry.register(AnyFieldLike.class, (AnyFieldLike<Specification<?>>) filterData -> new SpecificationAnyFieldLike<>(filterData, conversionService), FilterOperationMetadata.stringValue());
        registry.register(CollectionSize.class, (CollectionSize<Specification<?>>) filterData -> new SpecificationCollectionSize<>(filterData, conversionService), FilterOperationMetadata.stringValue());
        registry.register(ContainsAll.class, (ContainsAll<Specification<?>>) filterData -> new SpecificationContainsAll<>(filterData, conversionService), FilterOperationMetadata.arrayValue());
        registry.register(EffectiveAtClosed.class, (EffectiveAtClosed<Specification<?>>) filterData -> new SpecificationEffectiveAtClosed<>(filterData, conversionService), FilterOperationMetadata.targetField());
        registry.register(EffectiveAtHalfOpen.class, (EffectiveAtHalfOpen<Specification<?>>) filterData -> new SpecificationEffectiveAtHalfOpen<>(filterData, conversionService), FilterOperationMetadata.targetField());
        registry.register(EffectiveAtOpen.class, (EffectiveAtOpen<Specification<?>>) filterData -> new SpecificationEffectiveAtOpen<>(filterData, conversionService), FilterOperationMetadata.targetField());
        registry.register(IsBlank.class, (IsBlank<Specification<?>>) filterData -> new SpecificationIsBlank<>(filterData, conversionService), FilterOperationMetadata.booleanValue());
        registry.register(IsEmptyCollection.class, (IsEmptyCollection<Specification<?>>) filterData -> new SpecificationIsEmptyCollection<>(filterData, conversionService), FilterOperationMetadata.booleanValue());
        registry.register(NullOrGreater.class, (NullOrGreater<Specification<?>>) filterData -> new SpecificationNullOrGreater<>(filterData, conversionService), FilterOperationMetadata.targetField());
        registry.register(NullOrGreaterOrEquals.class, (NullOrGreaterOrEquals<Specification<?>>) filterData -> new SpecificationNullOrGreaterOrEquals<>(filterData, conversionService), FilterOperationMetadata.targetField());
        registry.register(NullOrLess.class, (NullOrLess<Specification<?>>) filterData -> new SpecificationNullOrLess<>(filterData, conversionService), FilterOperationMetadata.targetField());
        registry.register(NullOrLessOrEquals.class, (NullOrLessOrEquals<Specification<?>>) filterData -> new SpecificationNullOrLessOrEquals<>(filterData, conversionService), FilterOperationMetadata.targetField());
        registry.register(OnDate.class, (OnDate<Specification<?>>) filterData -> new SpecificationOnDate<>(filterData, conversionService, zoneId), FilterOperationMetadata.stringValue());
        registry.register(PeriodOverlapsClosed.class, (PeriodOverlapsClosed<Specification<?>>) filterData -> new SpecificationPeriodOverlapsClosed<>(filterData, conversionService), FilterOperationMetadata.targetField());
        registry.register(PeriodOverlapsHalfOpen.class, (PeriodOverlapsHalfOpen<Specification<?>>) filterData -> new SpecificationPeriodOverlapsHalfOpen<>(filterData, conversionService), FilterOperationMetadata.targetField());
        registry.register(PeriodOverlapsOpen.class, (PeriodOverlapsOpen<Specification<?>>) filterData -> new SpecificationPeriodOverlapsOpen<>(filterData, conversionService), FilterOperationMetadata.targetField());
        registry.register(SizeBetween.class, (SizeBetween<Specification<?>>) filterData -> new SpecificationSizeBetween<>(filterData, conversionService), FilterOperationMetadata.targetField());
    }

}
