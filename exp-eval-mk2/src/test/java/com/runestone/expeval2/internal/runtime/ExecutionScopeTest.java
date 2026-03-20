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
        @DisplayName("CURR_DATE produces a DateValue wrapping today's date")
        void currDateProducesDateValue() {
            ExecutionScope scope = ExecutionScope.readOnly(Map.of());
            LocalDate before = LocalDate.now();

            RuntimeValue result = scope.resolveDynamic(DynamicInstant.CURR_DATE);

            LocalDate after = LocalDate.now();
            assertThat(result).isInstanceOf(RuntimeValue.DateValue.class);
            assertThat((LocalDate) ((RuntimeValue.DateValue) result).raw()).isBetween(before, after);
        }

        @Test
        @DisplayName("CURR_TIME produces a TimeValue wrapping the current time")
        void currTimeProducesTimeValue() {
            ExecutionScope scope = ExecutionScope.readOnly(Map.of());
            LocalTime before = LocalTime.now();

            RuntimeValue result = scope.resolveDynamic(DynamicInstant.CURR_TIME);

            LocalTime after = LocalTime.now();
            assertThat(result).isInstanceOf(RuntimeValue.TimeValue.class);
            assertThat((LocalTime) ((RuntimeValue.TimeValue) result).raw()).isBetween(before, after);
        }

        @Test
        @DisplayName("CURR_DATETIME produces a DateTimeValue wrapping the current date-time")
        void currDateTimeProducesDateTimeValue() {
            ExecutionScope scope = ExecutionScope.readOnly(Map.of());
            LocalDateTime before = LocalDateTime.now();

            RuntimeValue result = scope.resolveDynamic(DynamicInstant.CURR_DATETIME);

            LocalDateTime after = LocalDateTime.now();
            assertThat(result).isInstanceOf(RuntimeValue.DateTimeValue.class);
            assertThat((LocalDateTime) ((RuntimeValue.DateTimeValue) result).raw()).isBetween(before, after);
        }
    }

    @Nested
    @DisplayName("resolveDynamic — per-execution caching")
    class PerExecutionCaching {

        @Test
        @DisplayName("repeated calls with CURR_DATE return the same instance")
        void currDateCachedWithinScope() {
            ExecutionScope scope = ExecutionScope.readOnly(Map.of());

            RuntimeValue first = scope.resolveDynamic(DynamicInstant.CURR_DATE);
            RuntimeValue second = scope.resolveDynamic(DynamicInstant.CURR_DATE);

            assertThat(first).isSameAs(second);
        }

        @Test
        @DisplayName("repeated calls with CURR_TIME return the same instance")
        void currTimeCachedWithinScope() {
            ExecutionScope scope = ExecutionScope.readOnly(Map.of());

            RuntimeValue first = scope.resolveDynamic(DynamicInstant.CURR_TIME);
            RuntimeValue second = scope.resolveDynamic(DynamicInstant.CURR_TIME);

            assertThat(first).isSameAs(second);
        }

        @Test
        @DisplayName("repeated calls with CURR_DATETIME return the same instance")
        void currDateTimeCachedWithinScope() {
            ExecutionScope scope = ExecutionScope.readOnly(Map.of());

            RuntimeValue first = scope.resolveDynamic(DynamicInstant.CURR_DATETIME);
            RuntimeValue second = scope.resolveDynamic(DynamicInstant.CURR_DATETIME);

            assertThat(first).isSameAs(second);
        }

        @Test
        @DisplayName("all three kinds are cached independently within the same scope")
        void allKindsCachedIndependently() {
            ExecutionScope scope = ExecutionScope.readOnly(Map.of());

            RuntimeValue date1 = scope.resolveDynamic(DynamicInstant.CURR_DATE);
            RuntimeValue time1 = scope.resolveDynamic(DynamicInstant.CURR_TIME);
            RuntimeValue datetime1 = scope.resolveDynamic(DynamicInstant.CURR_DATETIME);
            RuntimeValue date2 = scope.resolveDynamic(DynamicInstant.CURR_DATE);
            RuntimeValue time2 = scope.resolveDynamic(DynamicInstant.CURR_TIME);
            RuntimeValue datetime2 = scope.resolveDynamic(DynamicInstant.CURR_DATETIME);

            assertThat(date1).isSameAs(date2);
            assertThat(time1).isSameAs(time2);
            assertThat(datetime1).isSameAs(datetime2);
        }

        @Test
        @DisplayName("two different scope instances do not share the dynamic cache")
        void separateScopesHaveIndependentCaches() {
            ExecutionScope firstScope = ExecutionScope.readOnly(Map.of());
            ExecutionScope secondScope = ExecutionScope.readOnly(Map.of());

            RuntimeValue fromFirst = firstScope.resolveDynamic(DynamicInstant.CURR_DATE);
            RuntimeValue fromSecond = secondScope.resolveDynamic(DynamicInstant.CURR_DATE);

            assertThat(fromFirst).isNotSameAs(fromSecond);
        }

        @Test
        @DisplayName("fromIsolated scope also caches dynamic values per execution")
        void fromIsolatedScopeCachesDynamicValues() {
            ExecutionScope scope = ExecutionScope.fromIsolated(new HashMap<>());

            RuntimeValue first = scope.resolveDynamic(DynamicInstant.CURR_DATE);
            RuntimeValue second = scope.resolveDynamic(DynamicInstant.CURR_DATE);

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
