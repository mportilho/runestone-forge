package com.runestone.expeval_mk3.internal.runtime;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves issue #96's coherent-time contract at the {@link ExecutionScope} level: the clock is untouched
 * until a current-temporal value is actually read, and once read it is consulted exactly once, truncated
 * to whole seconds, with {@code currDate}/{@code currTime}/{@code currDateTime} derived from that same
 * instant.
 */
class ExecutionScopeTemporalTest {

    @Test
    void neverConsultsTheClockUnlessACurrentTemporalValueIsRead() {
        CountingClock clock = new CountingClock(Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC));

        new ExecutionScope(ExecutionScope.blankFrame(0), ZoneOffset.UTC, clock);

        assertThat(clock.callCount()).isZero();
    }

    @Test
    void consultsTheClockExactlyOnceAndTruncatesToWholeSecondsAcrossAllThreeReads() {
        Instant instantWithNanos = Instant.parse("2024-03-15T10:20:30.123456789Z");
        CountingClock clock = new CountingClock(Clock.fixed(instantWithNanos, ZoneOffset.UTC));
        ExecutionScope scope = new ExecutionScope(ExecutionScope.blankFrame(0), ZoneOffset.UTC, clock);

        LocalDate date = scope.currentDate();
        LocalTime time = scope.currentTime();
        LocalDateTime dateTime = scope.currentDateTime();

        assertThat(date).isEqualTo(LocalDate.of(2024, 3, 15));
        assertThat(time).isEqualTo(LocalTime.of(10, 20, 30));
        assertThat(dateTime).isEqualTo(LocalDateTime.of(2024, 3, 15, 10, 20, 30));
        assertThat(clock.callCount()).isEqualTo(1);
    }

    @Test
    void truncationNeverCrossesTheSecondBoundaryJustBeforeMidnight() {
        Instant justBeforeMidnight = Instant.parse("2024-03-15T23:59:59.999999999Z");
        ExecutionScope scope = new ExecutionScope(
                ExecutionScope.blankFrame(0), ZoneOffset.UTC, Clock.fixed(justBeforeMidnight, ZoneOffset.UTC));

        assertThat(scope.currentDate()).isEqualTo(LocalDate.of(2024, 3, 15));
        assertThat(scope.currentTime()).isEqualTo(LocalTime.of(23, 59, 59));
        assertThat(scope.currentDateTime()).isEqualTo(LocalDateTime.of(2024, 3, 15, 23, 59, 59));
    }

    @Test
    void truncationNeverCrossesTheSecondBoundaryJustAfterMidnight() {
        Instant justAfterMidnight = Instant.parse("2024-03-16T00:00:00.000000001Z");
        ExecutionScope scope = new ExecutionScope(
                ExecutionScope.blankFrame(0), ZoneOffset.UTC, Clock.fixed(justAfterMidnight, ZoneOffset.UTC));

        assertThat(scope.currentDate()).isEqualTo(LocalDate.of(2024, 3, 16));
        assertThat(scope.currentTime()).isEqualTo(LocalTime.of(0, 0, 0));
        assertThat(scope.currentDateTime()).isEqualTo(LocalDateTime.of(2024, 3, 16, 0, 0, 0));
    }
}
