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

import com.runestone.converters.DataConversionService;
import com.runestone.dynafilter.core.operation.AbstractFilterOperationService;
import com.runestone.dynafilter.core.operation.FilterOperation;
import com.runestone.dynafilter.core.operation.FilterOperationMetadata;
import com.runestone.dynafilter.core.operation.FilterOperationRegistry;
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

    public JpaFilterOperationService(DataConversionService conversionService) {
        this(conversionService, DEFAULT_ZONE_ID, List.of());
    }

    public JpaFilterOperationService(DataConversionService conversionService, List<JpaFilterOperationContributor> contributors) {
        this(conversionService, DEFAULT_ZONE_ID, contributors);
    }

    public JpaFilterOperationService(DataConversionService conversionService, ZoneId zoneId) {
        this(conversionService, zoneId, List.of());
    }

    public JpaFilterOperationService(
            DataConversionService conversionService,
            ZoneId zoneId,
            List<JpaFilterOperationContributor> contributors
    ) {
        super(createOperationRegistry(conversionService, zoneId, contributors));
    }

    private static FilterOperationRegistry<Specification<?>> createOperationRegistry(
            DataConversionService conversionService,
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
        registry.register(Between.class, FilterOperationMetadata.targetField(), ctx -> new SpecificationBetween<>(ctx.filterData(), ctx.conversionService()));
        registry.register(EndsWith.class, FilterOperationMetadata.targetField(), ctx -> new SpecificationEndsWith<>(ctx.filterData(), ctx.conversionService()));
        registry.register(Equals.class, FilterOperationMetadata.targetField(), ctx -> new SpecificationEquals<>(ctx.filterData(), ctx.conversionService()));
        registry.register(Greater.class, FilterOperationMetadata.targetField(), ctx -> new SpecificationGreater<>(ctx.filterData(), ctx.conversionService()));
        registry.register(GreaterOrEquals.class, FilterOperationMetadata.targetField(), ctx -> new SpecificationGreaterOrEquals<>(ctx.filterData(), ctx.conversionService()));
        registry.register(IsIn.class, FilterOperationMetadata.arrayValue(), ctx -> new SpecificationIsIn<>(ctx.filterData(), ctx.conversionService()));
        registry.register(IsNull.class, FilterOperationMetadata.booleanValue(), ctx -> new SpecificationIsNull<>(ctx.filterData(), ctx.conversionService()));
        registry.register(Less.class, FilterOperationMetadata.targetField(), ctx -> new SpecificationLess<>(ctx.filterData(), ctx.conversionService()));
        registry.register(LessOrEquals.class, FilterOperationMetadata.targetField(), ctx -> new SpecificationLessOrEquals<>(ctx.filterData(), ctx.conversionService()));
        registry.register(Like.class, FilterOperationMetadata.targetField(), ctx -> new SpecificationLike<>(ctx.filterData(), ctx.conversionService()));
        registry.register(StartsWith.class, FilterOperationMetadata.targetField(), ctx -> new SpecificationStartsWith<>(ctx.filterData(), ctx.conversionService()));
        registry.register(AnyFieldLike.class, FilterOperationMetadata.stringValue(), ctx -> new SpecificationAnyFieldLike<>(ctx.filterData(), ctx.conversionService()));
        registry.register(CollectionSize.class, FilterOperationMetadata.stringValue(), ctx -> new SpecificationCollectionSize<>(ctx.filterData(), ctx.conversionService()));
        registry.register(ContainsAll.class, FilterOperationMetadata.arrayValue(), ctx -> new SpecificationContainsAll<>(ctx.filterData(), ctx.conversionService()));
        registry.register(EffectiveAtClosed.class, FilterOperationMetadata.targetField(), ctx -> new SpecificationEffectiveAtClosed<>(ctx.filterData(), ctx.conversionService()));
        registry.register(EffectiveAtHalfOpen.class, FilterOperationMetadata.targetField(), ctx -> new SpecificationEffectiveAtHalfOpen<>(ctx.filterData(), ctx.conversionService()));
        registry.register(EffectiveAtOpen.class, FilterOperationMetadata.targetField(), ctx -> new SpecificationEffectiveAtOpen<>(ctx.filterData(), ctx.conversionService()));
        registry.register(IsBlank.class, FilterOperationMetadata.booleanValue(), ctx -> new SpecificationIsBlank<>(ctx.filterData(), ctx.conversionService()));
        registry.register(IsEmptyCollection.class, FilterOperationMetadata.booleanValue(), ctx -> new SpecificationIsEmptyCollection<>(ctx.filterData(), ctx.conversionService()));
        registry.register(NullOrGreater.class, FilterOperationMetadata.targetField(), ctx -> new SpecificationNullOrGreater<>(ctx.filterData(), ctx.conversionService()));
        registry.register(NullOrGreaterOrEquals.class, FilterOperationMetadata.targetField(), ctx -> new SpecificationNullOrGreaterOrEquals<>(ctx.filterData(), ctx.conversionService()));
        registry.register(NullOrLess.class, FilterOperationMetadata.targetField(), ctx -> new SpecificationNullOrLess<>(ctx.filterData(), ctx.conversionService()));
        registry.register(NullOrLessOrEquals.class, FilterOperationMetadata.targetField(), ctx -> new SpecificationNullOrLessOrEquals<>(ctx.filterData(), ctx.conversionService()));
        registry.register(OnDate.class, FilterOperationMetadata.stringValue(), ctx -> new SpecificationOnDate<>(ctx.filterData(), ctx.conversionService(), ctx.zoneId()));
        registry.register(PeriodOverlapsClosed.class, FilterOperationMetadata.targetField(), ctx -> new SpecificationPeriodOverlapsClosed<>(ctx.filterData(), ctx.conversionService()));
        registry.register(PeriodOverlapsHalfOpen.class, FilterOperationMetadata.targetField(), ctx -> new SpecificationPeriodOverlapsHalfOpen<>(ctx.filterData(), ctx.conversionService()));
        registry.register(PeriodOverlapsOpen.class, FilterOperationMetadata.targetField(), ctx -> new SpecificationPeriodOverlapsOpen<>(ctx.filterData(), ctx.conversionService()));
        registry.register(SizeBetween.class, FilterOperationMetadata.targetField(), ctx -> new SpecificationSizeBetween<>(ctx.filterData(), ctx.conversionService()));
    }

    private record DefaultJpaFilterOperationRegistry(
            FilterOperationRegistry<Specification<?>> registry,
            DataConversionService conversionService,
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
