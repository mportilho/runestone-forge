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

package com.runestone.dynafilter.modules.jpa.api;

import com.runestone.converters.RuntimeDataConversionService;
import com.runestone.dynafilter.core.operation.AbstractFilterOperationService;
import com.runestone.dynafilter.core.operation.FilterArity;
import com.runestone.dynafilter.core.operation.FilterOperation;
import com.runestone.dynafilter.core.operation.FilterOperationMetadata;
import com.runestone.dynafilter.core.operation.FilterOperationRegistry;
import com.runestone.dynafilter.core.operation.FilterValueShape;
import com.runestone.dynafilter.core.operation.types.Between;
import com.runestone.dynafilter.core.operation.types.EndsWith;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.Greater;
import com.runestone.dynafilter.core.operation.types.GreaterOrEquals;
import com.runestone.dynafilter.core.operation.types.IsIn;
import com.runestone.dynafilter.core.operation.types.IsNull;
import com.runestone.dynafilter.core.operation.types.Less;
import com.runestone.dynafilter.core.operation.types.LessOrEquals;
import com.runestone.dynafilter.core.operation.types.Like;
import com.runestone.dynafilter.core.operation.types.StartsWith;
import com.runestone.dynafilter.core.operation.types.extensions.AnyFieldLike;
import com.runestone.dynafilter.core.operation.types.extensions.CollectionSize;
import com.runestone.dynafilter.core.operation.types.extensions.ContainsAll;
import com.runestone.dynafilter.core.operation.types.extensions.EffectiveAtClosed;
import com.runestone.dynafilter.core.operation.types.extensions.EffectiveAtHalfOpen;
import com.runestone.dynafilter.core.operation.types.extensions.EffectiveAtOpen;
import com.runestone.dynafilter.core.operation.types.extensions.IsBlank;
import com.runestone.dynafilter.core.operation.types.extensions.IsEmptyCollection;
import com.runestone.dynafilter.core.operation.types.extensions.NullOrGreater;
import com.runestone.dynafilter.core.operation.types.extensions.NullOrGreaterOrEquals;
import com.runestone.dynafilter.core.operation.types.extensions.NullOrLess;
import com.runestone.dynafilter.core.operation.types.extensions.NullOrLessOrEquals;
import com.runestone.dynafilter.core.operation.types.extensions.OnDate;
import com.runestone.dynafilter.core.operation.types.extensions.PeriodOverlapsClosed;
import com.runestone.dynafilter.core.operation.types.extensions.PeriodOverlapsHalfOpen;
import com.runestone.dynafilter.core.operation.types.extensions.PeriodOverlapsOpen;
import com.runestone.dynafilter.core.operation.types.extensions.SizeBetween;
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
import com.runestone.dynafilter.modules.jpa.operation.specification.extensions.SpecificationAnyFieldLike;
import com.runestone.dynafilter.modules.jpa.operation.specification.extensions.SpecificationCollectionSize;
import com.runestone.dynafilter.modules.jpa.operation.specification.extensions.SpecificationContainsAll;
import com.runestone.dynafilter.modules.jpa.operation.specification.extensions.SpecificationEffectiveAtClosed;
import com.runestone.dynafilter.modules.jpa.operation.specification.extensions.SpecificationEffectiveAtHalfOpen;
import com.runestone.dynafilter.modules.jpa.operation.specification.extensions.SpecificationEffectiveAtOpen;
import com.runestone.dynafilter.modules.jpa.operation.specification.extensions.SpecificationIsBlank;
import com.runestone.dynafilter.modules.jpa.operation.specification.extensions.SpecificationIsEmptyCollection;
import com.runestone.dynafilter.modules.jpa.operation.specification.extensions.SpecificationNullOrGreater;
import com.runestone.dynafilter.modules.jpa.operation.specification.extensions.SpecificationNullOrGreaterOrEquals;
import com.runestone.dynafilter.modules.jpa.operation.specification.extensions.SpecificationNullOrLess;
import com.runestone.dynafilter.modules.jpa.operation.specification.extensions.SpecificationNullOrLessOrEquals;
import com.runestone.dynafilter.modules.jpa.operation.specification.extensions.SpecificationOnDate;
import com.runestone.dynafilter.modules.jpa.operation.specification.extensions.SpecificationPeriodOverlapsClosed;
import com.runestone.dynafilter.modules.jpa.operation.specification.extensions.SpecificationPeriodOverlapsHalfOpen;
import com.runestone.dynafilter.modules.jpa.operation.specification.extensions.SpecificationPeriodOverlapsOpen;
import com.runestone.dynafilter.modules.jpa.operation.specification.extensions.SpecificationSizeBetween;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

public class JpaFilterOperationService extends AbstractFilterOperationService<Specification<?>> {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneOffset.UTC;

    public JpaFilterOperationService(RuntimeDataConversionService conversionService) {
        this(conversionService, DEFAULT_ZONE_ID, List.of());
    }

    public JpaFilterOperationService(RuntimeDataConversionService conversionService, List<JpaFilterOperationContributor> contributors) {
        this(conversionService, DEFAULT_ZONE_ID, contributors);
    }

    public JpaFilterOperationService(RuntimeDataConversionService conversionService, ZoneId zoneId) {
        this(conversionService, zoneId, List.of());
    }

    public JpaFilterOperationService(
            RuntimeDataConversionService conversionService,
            ZoneId zoneId,
            List<JpaFilterOperationContributor> contributors
    ) {
        super(createOperationRegistry(conversionService, zoneId, contributors));
    }

    private static FilterOperationRegistry<Specification<?>> createOperationRegistry(
            RuntimeDataConversionService conversionService,
            ZoneId zoneId,
            List<JpaFilterOperationContributor> contributors
    ) {
        Objects.requireNonNull(conversionService, "conversionService cannot be null");
        Objects.requireNonNull(zoneId, "zoneId cannot be null");
        Objects.requireNonNull(contributors, "contributors cannot be null");

        FilterOperationRegistry<Specification<?>> registry = new FilterOperationRegistry<>();
        JpaFilterOperationRegistry jpaRegistry = new DefaultJpaFilterOperationRegistry(registry, conversionService, zoneId);
        registerBuiltInOperations(jpaRegistry);
        for (JpaFilterOperationContributor contributor : contributors) {
            Objects.requireNonNull(contributor, "contributor cannot be null").contribute(jpaRegistry);
        }
        return registry;
    }

    private static void registerBuiltInOperations(JpaFilterOperationRegistry registry) {
        registry.register(Between.class, targetField(1, 2), ctx -> new SpecificationBetween<>(ctx.filterData(), ctx.conversionService()));
        registry.register(EndsWith.class, targetField(1, 1), ctx -> new SpecificationEndsWith<>(ctx.filterData(), ctx.conversionService()));
        registry.register(Equals.class, targetField(1, 1), ctx -> new SpecificationEquals<>(ctx.filterData(), ctx.conversionService()));
        registry.register(Greater.class, targetField(1, 1), ctx -> new SpecificationGreater<>(ctx.filterData(), ctx.conversionService()));
        registry.register(GreaterOrEquals.class, targetField(1, 1), ctx -> new SpecificationGreaterOrEquals<>(ctx.filterData(), ctx.conversionService()));
        registry.register(IsIn.class, arrayValue(1, 1), ctx -> new SpecificationIsIn<>(ctx.filterData(), ctx.conversionService()));
        registry.register(IsNull.class, booleanValue(1, 1), ctx -> new SpecificationIsNull<>(ctx.filterData(), ctx.conversionService()));
        registry.register(Less.class, targetField(1, 1), ctx -> new SpecificationLess<>(ctx.filterData(), ctx.conversionService()));
        registry.register(LessOrEquals.class, targetField(1, 1), ctx -> new SpecificationLessOrEquals<>(ctx.filterData(), ctx.conversionService()));
        registry.register(Like.class, targetField(1, 1), ctx -> new SpecificationLike<>(ctx.filterData(), ctx.conversionService()));
        registry.register(StartsWith.class, targetField(1, 1), ctx -> new SpecificationStartsWith<>(ctx.filterData(), ctx.conversionService()));
        registry.register(AnyFieldLike.class, stringValue(FilterArity.atLeast(1), 1), ctx -> new SpecificationAnyFieldLike<>(ctx.filterData(), ctx.conversionService()));
        registry.register(CollectionSize.class, stringValue(1, 1), ctx -> new SpecificationCollectionSize<>(ctx.filterData(), ctx.conversionService()));
        registry.register(ContainsAll.class, arrayValue(1, 1), ctx -> new SpecificationContainsAll<>(ctx.filterData(), ctx.conversionService()));
        registry.register(EffectiveAtClosed.class, targetField(2, 1), ctx -> new SpecificationEffectiveAtClosed<>(ctx.filterData(), ctx.conversionService()));
        registry.register(EffectiveAtHalfOpen.class, targetField(2, 1), ctx -> new SpecificationEffectiveAtHalfOpen<>(ctx.filterData(), ctx.conversionService()));
        registry.register(EffectiveAtOpen.class, targetField(2, 1), ctx -> new SpecificationEffectiveAtOpen<>(ctx.filterData(), ctx.conversionService()));
        registry.register(IsBlank.class, booleanValue(1, 1), ctx -> new SpecificationIsBlank<>(ctx.filterData(), ctx.conversionService()));
        registry.register(IsEmptyCollection.class, booleanValue(1, 1), ctx -> new SpecificationIsEmptyCollection<>(ctx.filterData(), ctx.conversionService()));
        registry.register(NullOrGreater.class, targetField(1, 1), ctx -> new SpecificationNullOrGreater<>(ctx.filterData(), ctx.conversionService()));
        registry.register(NullOrGreaterOrEquals.class, targetField(1, 1), ctx -> new SpecificationNullOrGreaterOrEquals<>(ctx.filterData(), ctx.conversionService()));
        registry.register(NullOrLess.class, targetField(1, 1), ctx -> new SpecificationNullOrLess<>(ctx.filterData(), ctx.conversionService()));
        registry.register(NullOrLessOrEquals.class, targetField(1, 1), ctx -> new SpecificationNullOrLessOrEquals<>(ctx.filterData(), ctx.conversionService()));
        registry.register(OnDate.class, stringValue(1, 1), ctx -> new SpecificationOnDate<>(ctx.filterData(), ctx.conversionService(), ctx.zoneId()));
        registry.register(PeriodOverlapsClosed.class, targetField(2, 2), ctx -> new SpecificationPeriodOverlapsClosed<>(ctx.filterData(), ctx.conversionService()));
        registry.register(PeriodOverlapsHalfOpen.class, targetField(2, 2), ctx -> new SpecificationPeriodOverlapsHalfOpen<>(ctx.filterData(), ctx.conversionService()));
        registry.register(PeriodOverlapsOpen.class, targetField(2, 2), ctx -> new SpecificationPeriodOverlapsOpen<>(ctx.filterData(), ctx.conversionService()));
        registry.register(SizeBetween.class, targetField(1, 2), ctx -> new SpecificationSizeBetween<>(ctx.filterData(), ctx.conversionService()));
    }

    private static FilterOperationMetadata targetField(int pathCount, int valueCount) {
        return metadata(FilterValueShape.TARGET_FIELD, FilterArity.exactly(pathCount), FilterArity.exactly(valueCount));
    }

    private static FilterOperationMetadata stringValue(int pathCount, int valueCount) {
        return stringValue(FilterArity.exactly(pathCount), valueCount);
    }

    private static FilterOperationMetadata stringValue(FilterArity pathArity, int valueCount) {
        return metadata(FilterValueShape.STRING, pathArity, FilterArity.exactly(valueCount));
    }

    private static FilterOperationMetadata booleanValue(int pathCount, int valueCount) {
        return metadata(FilterValueShape.BOOLEAN, FilterArity.exactly(pathCount), FilterArity.exactly(valueCount));
    }

    private static FilterOperationMetadata arrayValue(int pathCount, int valueCount) {
        return metadata(FilterValueShape.ARRAY, FilterArity.exactly(pathCount), FilterArity.exactly(valueCount));
    }

    private static FilterOperationMetadata metadata(FilterValueShape valueShape, FilterArity pathArity, FilterArity valueArity) {
        return new FilterOperationMetadata(valueShape, pathArity, valueArity, null);
    }

    private record DefaultJpaFilterOperationRegistry(
            FilterOperationRegistry<Specification<?>> registry,
            RuntimeDataConversionService conversionService,
            ZoneId zoneId
    ) implements JpaFilterOperationRegistry {

        private DefaultJpaFilterOperationRegistry {
            Objects.requireNonNull(registry, "registry cannot be null");
            Objects.requireNonNull(conversionService, "conversionService cannot be null");
            Objects.requireNonNull(zoneId, "zoneId cannot be null");
        }

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        public void register(Class<? extends FilterOperation> operationType, FilterOperationMetadata metadata, JpaSpecificationFactory factory) {
            Objects.requireNonNull(factory, "factory cannot be null");
            registry.register(
                    operationType,
                    filterData -> factory.create(new JpaFilterOperationContext(filterData, conversionService, zoneId)),
                    metadata
            );
        }
    }

}
