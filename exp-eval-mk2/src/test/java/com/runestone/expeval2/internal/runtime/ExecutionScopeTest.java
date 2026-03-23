package com.runestone.expeval2.internal.runtime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

@DisplayName("ExecutionScope")
class ExecutionScopeTest {

    @Nested
    @DisplayName("resolveDynamic — type resolution")
    class TypeResolution {

        @Test
        @DisplayName("CURR_DATE produces today's date")
        void currDateProducesLocalDate() {
            ExecutionScope scope = ExecutionScope.readOnly(Map.of());
            LocalDate before = LocalDate.now();

            Object result = scope.resolveDynamic(DynamicInstant.CURR_DATE);

            LocalDate after = LocalDate.now();
            assertThat(result).isInstanceOf(LocalDate.class);
            assertThat((LocalDate) result).isBetween(before, after);
        }

        @Test
        @DisplayName("CURR_TIME produces the current time")
        void currTimeProducesLocalTime() {
            ExecutionScope scope = ExecutionScope.readOnly(Map.of());
            LocalTime before = LocalTime.now();

            Object result = scope.resolveDynamic(DynamicInstant.CURR_TIME);

            LocalTime after = LocalTime.now();
            assertThat(result).isInstanceOf(LocalTime.class);
            assertThat((LocalTime) result).isBetween(before, after);
        }

        @Test
        @DisplayName("CURR_DATETIME produces the current date-time")
        void currDateTimeProducesLocalDateTime() {
            ExecutionScope scope = ExecutionScope.readOnly(Map.of());
            LocalDateTime before = LocalDateTime.now();

            Object result = scope.resolveDynamic(DynamicInstant.CURR_DATETIME);

            LocalDateTime after = LocalDateTime.now();
            assertThat(result).isInstanceOf(LocalDateTime.class);
            assertThat((LocalDateTime) result).isBetween(before, after);
        }
    }

    @Nested
    @DisplayName("resolveDynamic — per-execution caching")
    class PerExecutionCaching {

        @Test
        @DisplayName("repeated calls with CURR_DATE return the same instance")
        void currDateCachedWithinScope() {
            ExecutionScope scope = ExecutionScope.readOnly(Map.of());

            Object first = scope.resolveDynamic(DynamicInstant.CURR_DATE);
            Object second = scope.resolveDynamic(DynamicInstant.CURR_DATE);

            assertThat(first).isSameAs(second);
        }

        @Test
        @DisplayName("repeated calls with CURR_TIME return the same instance")
        void currTimeCachedWithinScope() {
            ExecutionScope scope = ExecutionScope.readOnly(Map.of());

            Object first = scope.resolveDynamic(DynamicInstant.CURR_TIME);
            Object second = scope.resolveDynamic(DynamicInstant.CURR_TIME);

            assertThat(first).isSameAs(second);
        }

        @Test
        @DisplayName("repeated calls with CURR_DATETIME return the same instance")
        void currDateTimeCachedWithinScope() {
            ExecutionScope scope = ExecutionScope.readOnly(Map.of());

            Object first = scope.resolveDynamic(DynamicInstant.CURR_DATETIME);
            Object second = scope.resolveDynamic(DynamicInstant.CURR_DATETIME);

            assertThat(first).isSameAs(second);
        }

        @Test
        @DisplayName("all three kinds are cached independently within the same scope")
        void allKindsCachedIndependently() {
            ExecutionScope scope = ExecutionScope.readOnly(Map.of());

            Object date1     = scope.resolveDynamic(DynamicInstant.CURR_DATE);
            Object time1     = scope.resolveDynamic(DynamicInstant.CURR_TIME);
            Object datetime1 = scope.resolveDynamic(DynamicInstant.CURR_DATETIME);
            Object date2     = scope.resolveDynamic(DynamicInstant.CURR_DATE);
            Object time2     = scope.resolveDynamic(DynamicInstant.CURR_TIME);
            Object datetime2 = scope.resolveDynamic(DynamicInstant.CURR_DATETIME);

            assertThat(date1).isSameAs(date2);
            assertThat(time1).isSameAs(time2);
            assertThat(datetime1).isSameAs(datetime2);
        }

        @Test
        @DisplayName("two different scope instances do not share the dynamic cache")
        void separateScopesHaveIndependentCaches() {
            ExecutionScope firstScope  = ExecutionScope.readOnly(Map.of());
            ExecutionScope secondScope = ExecutionScope.readOnly(Map.of());

            Object fromFirst  = firstScope.resolveDynamic(DynamicInstant.CURR_DATE);
            Object fromSecond = secondScope.resolveDynamic(DynamicInstant.CURR_DATE);

            assertThat(fromFirst).isNotSameAs(fromSecond);
        }

        @Test
        @DisplayName("fromIsolated scope also caches dynamic values per execution")
        void fromIsolatedScopeCachesDynamicValues() {
            ExecutionScope scope = ExecutionScope.from(new HashMap<>(), 2);

            Object first  = scope.resolveDynamic(DynamicInstant.CURR_DATE);
            Object second = scope.resolveDynamic(DynamicInstant.CURR_DATE);

            assertThat(first).isSameAs(second);
        }
    }

    @Nested
    @DisplayName("resolveDynamic — invalid input")
    class InvalidInput {

        @Test
        @DisplayName("null kind throws NullPointerException")
        void nullKindThrowsNullPointerException() {
            ExecutionScope scope = ExecutionScope.readOnly(Map.of());

            assertThatNullPointerException()
                .isThrownBy(() -> scope.resolveDynamic(null))
                .withMessageContaining("kind");
        }
    }
}
