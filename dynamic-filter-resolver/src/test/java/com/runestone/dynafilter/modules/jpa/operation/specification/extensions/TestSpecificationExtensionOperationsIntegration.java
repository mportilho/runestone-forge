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

package com.runestone.dynafilter.modules.jpa.operation.specification.extensions;

import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.modifiers.ModIgnoreCase;
import com.runestone.dynafilter.core.operation.FilterOperation;
import com.runestone.dynafilter.core.operation.types.extensions.*;
import com.runestone.dynafilter.modules.jpa.api.JpaFilterOperationService;
import com.runestone.dynafilter.modules.jpa.tools.app.database.FilterExtensionRecordRepository;
import com.runestone.dynafilter.modules.jpa.tools.app.database.InMemoryDatabaseApplication;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.FilterExtensionRecord;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

@DataJpaTest
@ContextConfiguration(classes = InMemoryDatabaseApplication.class)
public class TestSpecificationExtensionOperationsIntegration {

    private static final DefaultDataConversionService CONVERSION_SERVICE = new DefaultDataConversionService();
    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");

    @Autowired
    private FilterExtensionRecordRepository repository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    public void setup() {
        repository.deleteAll();
        entityManager.flush();

        repository.save(record("BLANK_NULL", null, "Null name", null,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10),
                LocalDateTime.of(2026, 6, 4, 10, 0), Instant.parse("2026-06-03T15:00:00Z")));
        repository.save(record("BLANK_EMPTY", "", "Alpha searchable", Set.of(),
                LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 20),
                LocalDateTime.of(2026, 6, 3, 23, 59), Instant.parse("2026-06-04T03:30:00Z")));
        repository.save(record("BLANK_SPACES", "   ", "Beta docs", Set.of("alpha"),
                LocalDate.of(2026, 1, 20), null,
                LocalDateTime.of(2026, 6, 4, 0, 0), Instant.parse("2026-06-04T23:00:00Z")));
        repository.save(record("FILLED_ALPHA", "Alpha Product", "Catalog item", Set.of("blue", "green"),
                LocalDate.of(2026, 1, 21), LocalDate.of(2026, 1, 25),
                LocalDateTime.of(2026, 6, 4, 23, 59), Instant.parse("2026-06-05T02:30:00Z")));
        repository.save(record("FILLED_BRAVO", "Bravo", "Needle description", Set.of("blue", "green", "red"),
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 10),
                LocalDateTime.of(2026, 6, 5, 0, 0), Instant.parse("2026-06-05T03:00:00Z")));

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("IsBlank true matches null, empty and space-only strings")
    public void testIsBlankTrueMatchesNullEmptyAndSpaces() {
        Specification<FilterExtensionRecord> specification = new SpecificationIsBlank<>(
                filterData("name", IsBlank.class, true), CONVERSION_SERVICE
        );

        List<FilterExtensionRecord> result = repository.findAll(specification);

        Assertions.assertThat(codes(result)).containsExactlyInAnyOrder("BLANK_NULL", "BLANK_EMPTY", "BLANK_SPACES");
    }

    @Test
    @DisplayName("IsBlank false matches only text with non-space content")
    public void testIsBlankFalseMatchesNonBlankText() {
        Specification<FilterExtensionRecord> specification = new SpecificationIsBlank<>(
                filterData("name", IsBlank.class, false), CONVERSION_SERVICE
        );

        List<FilterExtensionRecord> result = repository.findAll(specification);

        Assertions.assertThat(codes(result)).containsExactlyInAnyOrder("FILLED_ALPHA", "FILLED_BRAVO");
    }

    @Test
    @DisplayName("IsEmptyCollection true matches null and empty collections")
    public void testIsEmptyCollectionTrueMatchesNullAndEmptyCollections() {
        Specification<FilterExtensionRecord> specification = new SpecificationIsEmptyCollection<>(
                filterData("tags", IsEmptyCollection.class, true), CONVERSION_SERVICE
        );

        List<FilterExtensionRecord> result = repository.findAll(specification);

        Assertions.assertThat(codes(result)).containsExactlyInAnyOrder("BLANK_NULL", "BLANK_EMPTY");
    }

    @Test
    @DisplayName("IsEmptyCollection false matches non-empty collections")
    public void testIsEmptyCollectionFalseMatchesNonEmptyCollections() {
        Specification<FilterExtensionRecord> specification = new SpecificationIsEmptyCollection<>(
                filterData("tags", IsEmptyCollection.class, false), CONVERSION_SERVICE
        );

        List<FilterExtensionRecord> result = repository.findAll(specification);

        Assertions.assertThat(codes(result)).containsExactlyInAnyOrder("BLANK_SPACES", "FILLED_ALPHA", "FILLED_BRAVO");
    }

    @Test
    @DisplayName("ContainsAll matches records containing every requested collection value")
    public void testContainsAllRequiresEveryRequestedValue() {
        Specification<FilterExtensionRecord> specification = new SpecificationContainsAll<>(
                filterData(new String[]{"tags"}, ContainsAll.class, new Object[]{new Object[]{"blue", "green"}}, null), CONVERSION_SERVICE
        );

        List<FilterExtensionRecord> result = repository.findAll(specification);

        Assertions.assertThat(codes(result)).containsExactlyInAnyOrder("FILLED_ALPHA", "FILLED_BRAVO");
    }

    @Test
    @DisplayName("ContainsAll with empty input applies no restriction")
    public void testContainsAllWithEmptyInputAppliesNoRestriction() {
        Specification<FilterExtensionRecord> specification = new SpecificationContainsAll<>(
                filterData(new String[]{"tags"}, ContainsAll.class, new Object[]{new Object[]{}}, null), CONVERSION_SERVICE
        );

        List<FilterExtensionRecord> result = repository.findAll(specification);
        Assertions.assertThat(codes(result)).containsExactlyInAnyOrder(
                "BLANK_NULL", "BLANK_EMPTY", "BLANK_SPACES", "FILLED_ALPHA", "FILLED_BRAVO"
        );
    }

    @Test
    @DisplayName("CollectionSize matches collections by exact size")
    public void testCollectionSizeMatchesExactSize() {
        Specification<FilterExtensionRecord> specification = new SpecificationCollectionSize<>(
                filterData("tags", CollectionSize.class, 2), CONVERSION_SERVICE
        );

        List<FilterExtensionRecord> result = repository.findAll(specification);
        Assertions.assertThat(codes(result)).containsExactly("FILLED_ALPHA");
    }

    @Test
    @DisplayName("SizeBetween matches collections by inclusive size range")
    public void testSizeBetweenMatchesInclusiveRange() {
        Specification<FilterExtensionRecord> specification = new SpecificationSizeBetween<>(
                filterData("tags", SizeBetween.class, 1, 2), CONVERSION_SERVICE
        );

        List<FilterExtensionRecord> result = repository.findAll(specification);
        Assertions.assertThat(codes(result)).containsExactlyInAnyOrder("BLANK_SPACES", "FILLED_ALPHA");
    }

    @Test
    @DisplayName("OnDate matches LocalDateTime values inside the target calendar day")
    public void testOnDateMatchesLocalDateTimeDay() {
        Specification<FilterExtensionRecord> specification = new SpecificationOnDate<>(
                filterData("createdAt", OnDate.class, LocalDate.of(2026, 6, 4)), CONVERSION_SERVICE, SAO_PAULO
        );

        List<FilterExtensionRecord> result = repository.findAll(specification);
        Assertions.assertThat(codes(result)).containsExactlyInAnyOrder("BLANK_NULL", "BLANK_SPACES", "FILLED_ALPHA");
    }

    @Test
    @DisplayName("OnDate accepts java.sql.Date filter values")
    public void testOnDateAcceptsSqlDateFilterValue() {
        Specification<FilterExtensionRecord> specification = new SpecificationOnDate<>(
                filterData("createdAt", OnDate.class, java.sql.Date.valueOf(LocalDate.of(2026, 6, 4))),
                CONVERSION_SERVICE,
                SAO_PAULO
        );

        List<FilterExtensionRecord> result = repository.findAll(specification);
        Assertions.assertThat(codes(result)).containsExactlyInAnyOrder("BLANK_NULL", "BLANK_SPACES", "FILLED_ALPHA");
    }

    @Test
    @DisplayName("OnDate uses the configured ZoneId for Instant day boundaries")
    public void testOnDateUsesConfiguredZoneForInstantDayBoundaries() {
        Specification<FilterExtensionRecord> specification = new SpecificationOnDate<>(
                filterData("publishedAt", OnDate.class, LocalDate.of(2026, 6, 4)), CONVERSION_SERVICE, SAO_PAULO
        );

        List<FilterExtensionRecord> result = repository.findAll(specification);
        Assertions.assertThat(codes(result)).containsExactlyInAnyOrder("BLANK_EMPTY", "BLANK_SPACES", "FILLED_ALPHA");
    }

    @Test
    @DisplayName("SpecificationFilterOperationService defaults OnDate to UTC")
    public void testSpecificationFilterOperationServiceDefaultsOnDateToUtc() {
        TimeZone originalDefault = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone(SAO_PAULO));
            JpaFilterOperationService service = new JpaFilterOperationService(CONVERSION_SERVICE);
            @SuppressWarnings("unchecked")
            Specification<FilterExtensionRecord> specification = (Specification<FilterExtensionRecord>) service.createFilter(
                    filterData("publishedAt", OnDate.class, LocalDate.of(2026, 6, 4))
            );

            List<FilterExtensionRecord> result = repository.findAll(specification);
            Assertions.assertThat(codes(result)).containsExactlyInAnyOrder("BLANK_EMPTY", "BLANK_SPACES");
        } finally {
            TimeZone.setDefault(originalDefault);
        }
    }

    @Test
    @DisplayName("PeriodOverlapsClosed includes intervals touching the filter boundaries")
    public void testPeriodOverlapsClosedIncludesTouchingBoundaries() {
        Specification<FilterExtensionRecord> specification = new SpecificationPeriodOverlapsClosed<>(
                filterData(new String[]{"effectiveStart", "effectiveEnd"}, PeriodOverlapsClosed.class,
                        LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 20)), CONVERSION_SERVICE
        );

        List<FilterExtensionRecord> result = repository.findAll(specification);
        Assertions.assertThat(codes(result)).containsExactlyInAnyOrder("BLANK_NULL", "BLANK_EMPTY", "BLANK_SPACES");
    }

    @Test
    @DisplayName("PeriodOverlapsHalfOpen excludes intervals that only touch boundaries")
    public void testPeriodOverlapsHalfOpenExcludesTouchingBoundaries() {
        Specification<FilterExtensionRecord> specification = new SpecificationPeriodOverlapsHalfOpen<>(
                filterData(new String[]{"effectiveStart", "effectiveEnd"}, PeriodOverlapsHalfOpen.class,
                        LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 20)), CONVERSION_SERVICE
        );

        List<FilterExtensionRecord> result = repository.findAll(specification);
        Assertions.assertThat(codes(result)).containsExactly("BLANK_EMPTY");
    }

    @Test
    @DisplayName("PeriodOverlapsOpen excludes intervals that only touch boundaries")
    public void testPeriodOverlapsOpenExcludesTouchingBoundaries() {
        Specification<FilterExtensionRecord> specification = new SpecificationPeriodOverlapsOpen<>(
                filterData(new String[]{"effectiveStart", "effectiveEnd"}, PeriodOverlapsOpen.class,
                        LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 20)), CONVERSION_SERVICE
        );

        List<FilterExtensionRecord> result = repository.findAll(specification);
        Assertions.assertThat(codes(result)).containsExactly("BLANK_EMPTY");
    }

    @Test
    @DisplayName("PeriodOverlapsHalfOpen treats null entity end as an unbounded future")
    public void testPeriodOverlapsHalfOpenTreatsNullEndAsUnboundedFuture() {
        Specification<FilterExtensionRecord> specification = new SpecificationPeriodOverlapsHalfOpen<>(
                filterData(new String[]{"effectiveStart", "effectiveEnd"}, PeriodOverlapsHalfOpen.class,
                        LocalDate.of(2026, 1, 20), LocalDate.of(2026, 1, 21)), CONVERSION_SERVICE
        );

        List<FilterExtensionRecord> result = repository.findAll(specification);
        Assertions.assertThat(codes(result)).containsExactly("BLANK_SPACES");
    }

    @Test
    @DisplayName("AnyFieldLike matches any configured text field using OR semantics")
    public void testAnyFieldLikeMatchesAnyConfiguredField() {
        Specification<FilterExtensionRecord> specification = new SpecificationAnyFieldLike<>(
                filterData(new String[]{"name", "description"}, AnyFieldLike.class, "Needle"), CONVERSION_SERVICE
        );

        List<FilterExtensionRecord> result = repository.findAll(specification);
        Assertions.assertThat(codes(result)).containsExactly("FILLED_BRAVO");
    }

    @Test
    @DisplayName("AnyFieldLike honors ModIgnoreCase across all configured fields")
    public void testAnyFieldLikeHonorsIgnoreCaseModifier() {
        Specification<FilterExtensionRecord> specification = new SpecificationAnyFieldLike<>(
                filterData(new String[]{"name", "description"}, AnyFieldLike.class, new Object[]{"alpha"}, Set.of(ModIgnoreCase.class)),
                CONVERSION_SERVICE
        );

        List<FilterExtensionRecord> result = repository.findAll(specification);
        Assertions.assertThat(codes(result)).containsExactlyInAnyOrder("BLANK_EMPTY", "FILLED_ALPHA");
    }

    @Test
    @DisplayName("EffectiveAtClosed includes start and end boundaries")
    public void testEffectiveAtClosedIncludesBoundaries() {
        Specification<FilterExtensionRecord> specification = new SpecificationEffectiveAtClosed<>(
                filterData(new String[]{"effectiveStart", "effectiveEnd"}, EffectiveAtClosed.class, LocalDate.of(2026, 1, 10)),
                CONVERSION_SERVICE
        );

        List<FilterExtensionRecord> result = repository.findAll(specification);
        Assertions.assertThat(codes(result)).containsExactlyInAnyOrder("BLANK_NULL", "BLANK_EMPTY");
    }

    @Test
    @DisplayName("EffectiveAtHalfOpen includes start boundary and excludes end boundary")
    public void testEffectiveAtHalfOpenIncludesStartAndExcludesEnd() {
        Specification<FilterExtensionRecord> specification = new SpecificationEffectiveAtHalfOpen<>(
                filterData(new String[]{"effectiveStart", "effectiveEnd"}, EffectiveAtHalfOpen.class, LocalDate.of(2026, 1, 10)),
                CONVERSION_SERVICE
        );

        List<FilterExtensionRecord> result = repository.findAll(specification);
        Assertions.assertThat(codes(result)).containsExactly("BLANK_EMPTY");
    }

    @Test
    @DisplayName("EffectiveAtOpen excludes both start and end boundaries")
    public void testEffectiveAtOpenExcludesBoundaries() {
        Specification<FilterExtensionRecord> specification = new SpecificationEffectiveAtOpen<>(
                filterData(new String[]{"effectiveStart", "effectiveEnd"}, EffectiveAtOpen.class, LocalDate.of(2026, 1, 10)),
                CONVERSION_SERVICE
        );

        List<FilterExtensionRecord> result = repository.findAll(specification);
        Assertions.assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("EffectiveAtHalfOpen treats null entity end as an unbounded future")
    public void testEffectiveAtHalfOpenTreatsNullEndAsUnboundedFuture() {
        Specification<FilterExtensionRecord> specification = new SpecificationEffectiveAtHalfOpen<>(
                filterData(new String[]{"effectiveStart", "effectiveEnd"}, EffectiveAtHalfOpen.class, LocalDate.of(2026, 1, 21)),
                CONVERSION_SERVICE
        );

        List<FilterExtensionRecord> result = repository.findAll(specification);
        Assertions.assertThat(codes(result)).containsExactlyInAnyOrder("BLANK_SPACES", "FILLED_ALPHA");
    }

    @Test
    @DisplayName("NullOrLess matches null values and values before the reference")
    public void testNullOrLessMatchesNullAndValuesBeforeReference() {
        Specification<FilterExtensionRecord> specification = new SpecificationNullOrLess<>(
                filterData("effectiveEnd", NullOrLess.class, LocalDate.of(2026, 1, 20)), CONVERSION_SERVICE
        );

        List<FilterExtensionRecord> result = repository.findAll(specification);
        Assertions.assertThat(codes(result)).containsExactlyInAnyOrder("BLANK_NULL", "BLANK_SPACES");
    }

    @Test
    @DisplayName("NullOrLessOrEquals matches null values and values at or before the reference")
    public void testNullOrLessOrEqualsMatchesNullAndValuesAtOrBeforeReference() {
        Specification<FilterExtensionRecord> specification = new SpecificationNullOrLessOrEquals<>(
                filterData("effectiveEnd", NullOrLessOrEquals.class, LocalDate.of(2026, 1, 20)), CONVERSION_SERVICE
        );

        List<FilterExtensionRecord> result = repository.findAll(specification);
        Assertions.assertThat(codes(result)).containsExactlyInAnyOrder("BLANK_NULL", "BLANK_EMPTY", "BLANK_SPACES");
    }

    @Test
    @DisplayName("NullOrGreater matches null values and values after the reference")
    public void testNullOrGreaterMatchesNullAndValuesAfterReference() {
        Specification<FilterExtensionRecord> specification = new SpecificationNullOrGreater<>(
                filterData("effectiveEnd", NullOrGreater.class, LocalDate.of(2026, 1, 20)), CONVERSION_SERVICE
        );

        List<FilterExtensionRecord> result = repository.findAll(specification);
        Assertions.assertThat(codes(result)).containsExactlyInAnyOrder("BLANK_SPACES", "FILLED_ALPHA", "FILLED_BRAVO");
    }

    @Test
    @DisplayName("NullOrGreaterOrEquals matches null values and values at or after the reference")
    public void testNullOrGreaterOrEqualsMatchesNullAndValuesAtOrAfterReference() {
        Specification<FilterExtensionRecord> specification = new SpecificationNullOrGreaterOrEquals<>(
                filterData("effectiveEnd", NullOrGreaterOrEquals.class, LocalDate.of(2026, 1, 20)), CONVERSION_SERVICE
        );

        List<FilterExtensionRecord> result = repository.findAll(specification);
        Assertions.assertThat(codes(result)).containsExactlyInAnyOrder("BLANK_EMPTY", "BLANK_SPACES", "FILLED_ALPHA", "FILLED_BRAVO");
    }

    private static FilterExtensionRecord record(
            String code,
            String name,
            String description,
            Set<String> tags,
            LocalDate effectiveStart,
            LocalDate effectiveEnd,
            LocalDateTime createdAt,
            Instant publishedAt
    ) {
        return new FilterExtensionRecord(code, name, description, tags, effectiveStart, effectiveEnd, createdAt, publishedAt);
    }

    private static List<String> codes(List<FilterExtensionRecord> records) {
        return records.stream().map(FilterExtensionRecord::getCode).toList();
    }

    private static FilterData filterData(
            String path,
            @SuppressWarnings("rawtypes") Class<? extends FilterOperation> operation,
            Object... values
    ) {
        return filterData(new String[]{path}, operation, values, null);
    }

    private static FilterData filterData(
            String[] path,
            @SuppressWarnings("rawtypes") Class<? extends FilterOperation> operation,
            Object... values
    ) {
        return filterData(path, operation, values, null);
    }

    private static FilterData filterData(
            String[] path,
            @SuppressWarnings("rawtypes") Class<? extends FilterOperation> operation,
            Object[] values,
            Set<Class<? extends com.runestone.dynafilter.core.model.FilterModifier>> modifiers
    ) {
        return new FilterData(path, parameterNames(values.length), Object.class, operation, false, values,
                modifiers == null ? null : List.copyOf(modifiers), null);
    }

    private static String[] parameterNames(int size) {
        String[] parameters = new String[size];
        for (int i = 0; i < size; i++) {
            parameters[i] = "p" + i;
        }
        return parameters;
    }
}
