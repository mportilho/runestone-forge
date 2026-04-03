package com.runestone.expeval2.internal.runtime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

@DisplayName("ExecutionScope")
class ExecutionScopeTest {

    private static Object[] emptyArray() {
        return new Object[0];
    }

    private static Object[] unboundArray(int size) {
        Object[] arr = new Object[size];
        Arrays.fill(arr, ExecutionScope.UNBOUND);
        return arr;
    }

    @Nested
    @DisplayName("resolveDynamic — type resolution")
    class TypeResolution {

        @Test
        @DisplayName("CURR_DATE produces today's date")
        void currDateProducesLocalDate() {
            ExecutionScope scope = ExecutionScope.readOnly(emptyArray());
            LocalDate before = LocalDate.now();

            Object result = scope.resolveDynamic(DynamicInstant.CURR_DATE);

            LocalDate after = LocalDate.now();
            assertThat(result).isInstanceOf(LocalDate.class);
            assertThat((LocalDate) result).isBetween(before, after);
        }

        @Test
        @DisplayName("CURR_TIME produces the current time")
        void currTimeProducesLocalTime() {
            ExecutionScope scope = ExecutionScope.readOnly(emptyArray());
            LocalTime before = LocalTime.now();

            Object result = scope.resolveDynamic(DynamicInstant.CURR_TIME);

            LocalTime after = LocalTime.now();
            assertThat(result).isInstanceOf(LocalTime.class);
            assertThat((LocalTime) result).isBetween(before, after);
        }

        @Test
        @DisplayName("CURR_DATETIME produces the current date-time")
        void currDateTimeProducesLocalDateTime() {
            ExecutionScope scope = ExecutionScope.readOnly(emptyArray());
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
            ExecutionScope scope = ExecutionScope.readOnly(emptyArray());

            Object first = scope.resolveDynamic(DynamicInstant.CURR_DATE);
            Object second = scope.resolveDynamic(DynamicInstant.CURR_DATE);

            assertThat(first).isSameAs(second);
        }

        @Test
        @DisplayName("repeated calls with CURR_TIME return the same instance")
        void currTimeCachedWithinScope() {
            ExecutionScope scope = ExecutionScope.readOnly(emptyArray());

            Object first = scope.resolveDynamic(DynamicInstant.CURR_TIME);
            Object second = scope.resolveDynamic(DynamicInstant.CURR_TIME);

            assertThat(first).isSameAs(second);
        }

        @Test
        @DisplayName("repeated calls with CURR_DATETIME return the same instance")
        void currDateTimeCachedWithinScope() {
            ExecutionScope scope = ExecutionScope.readOnly(emptyArray());

            Object first = scope.resolveDynamic(DynamicInstant.CURR_DATETIME);
            Object second = scope.resolveDynamic(DynamicInstant.CURR_DATETIME);

            assertThat(first).isSameAs(second);
        }

        @Test
        @DisplayName("all three kinds are cached independently within the same scope")
        void allKindsCachedIndependently() {
            ExecutionScope scope = ExecutionScope.readOnly(emptyArray());

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
            ExecutionScope firstScope  = ExecutionScope.readOnly(emptyArray());
            ExecutionScope secondScope = ExecutionScope.readOnly(emptyArray());

            Object fromFirst  = firstScope.resolveDynamic(DynamicInstant.CURR_DATE);
            Object fromSecond = secondScope.resolveDynamic(DynamicInstant.CURR_DATE);

            assertThat(fromFirst).isNotSameAs(fromSecond);
        }

        @Test
        @DisplayName("fromIsolated scope also caches dynamic values per execution")
        void fromIsolatedScopeCachesDynamicValues() {
            ExecutionScope scope = ExecutionScope.from(emptyArray(), 2);

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
            ExecutionScope scope = ExecutionScope.readOnly(emptyArray());

            assertThatNullPointerException()
                .isThrownBy(() -> scope.resolveDynamic(null))
                .withMessageContaining("kind");
        }
    }

    @Nested
    @DisplayName("positional lookup")
    class PositionalLookup {

        @Test
        @DisplayName("internal symbol is found in primary layer")
        void internalSymbolFound() {
            SymbolRef ref = new SymbolRef("a", SymbolKind.INTERNAL);
            ref.setIndex(0);
            ExecutionScope scope = ExecutionScope.from(emptyArray(), 1);
            scope.assign(ref, 10);

            assertThat(scope.find(ref)).isEqualTo(10);
        }

        @Test
        @DisplayName("external symbol is found in overrides (secondary) layer")
        void externalSymbolFoundInOverrides() {
            SymbolRef ref = new SymbolRef("x", SymbolKind.EXTERNAL);
            ref.setIndex(0);
            Object[] overrides = new Object[] { 42 };
            ExecutionScope scope = ExecutionScope.from(overrides, 0);

            assertThat(scope.find(ref)).isEqualTo(42);
        }

        @Test
        @DisplayName("external symbol is found in defaults (tertiary) layer")
        void externalSymbolFoundInDefaults() {
            SymbolRef ref = new SymbolRef("x", SymbolKind.EXTERNAL);
            ref.setIndex(0);
            Object[] defaults = new Object[] { 100 };
            ExecutionScope scope = ExecutionScope.from(unboundArray(1), defaults, 0);

            assertThat(scope.find(ref)).isEqualTo(100);
        }

        @Test
        @DisplayName("find() with unset index (sentinel -1) returns UNBOUND")
        void findWithUnsetIndexReturnsUnbound() {
            SymbolRef ref = new SymbolRef("unset", SymbolKind.EXTERNAL);
            // index intentionally NOT set — remains -1
            ExecutionScope scope = ExecutionScope.readOnly(new Object[]{42});

            assertThat(scope.find(ref)).isSameAs(ExecutionScope.UNBOUND);
        }

        @Test
        @DisplayName("INTERNAL symbol in read-only scope always returns UNBOUND")
        void internalSymbolInReadOnlyScopeReturnsUnbound() {
            SymbolRef ref = new SymbolRef("a", SymbolKind.INTERNAL);
            ref.setIndex(0);
            // Read-only scopes have no internal-result layer
            ExecutionScope scope = ExecutionScope.readOnly(new Object[]{99});

            assertThat(scope.find(ref)).isSameAs(ExecutionScope.UNBOUND);
        }

        @Test
        @DisplayName("assign() to an EXTERNAL symbol throws IllegalStateException")
        void assignToExternalSymbolThrows() {
            SymbolRef ref = new SymbolRef("x", SymbolKind.EXTERNAL);
            ref.setIndex(0);
            Object[] overrides = new Object[]{ExecutionScope.UNBOUND};
            ExecutionScope scope = ExecutionScope.from(overrides, 0);

            assertThatIllegalStateException()
                    .isThrownBy(() -> scope.assign(ref, 42))
                    .withMessageContaining("x");
        }

        @Test
        @DisplayName("assign() with out-of-bounds index throws IllegalStateException")
        void assignWithOutOfBoundsIndexThrows() {
            SymbolRef ref = new SymbolRef("a", SymbolKind.INTERNAL);
            ref.setIndex(5); // out of bounds for capacity=1
            ExecutionScope scope = ExecutionScope.from(emptyArray(), 1);

            assertThatIllegalStateException()
                    .isThrownBy(() -> scope.assign(ref, 42))
                    .withMessageContaining("5");
        }
    }
}
