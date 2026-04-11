package com.runestone.expeval.perf.jmh;

import com.runestone.expeval.perf.MembershipBenchmarkSupport;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

/**
 * Measures the per-call cost of the {@code in} and {@code not in} membership operators.
 *
 * <p>All expressions are pre-compiled in {@link MembershipBenchmarkSupport}; the benchmark
 * loop only exercises the evaluation phase.  Run with {@code -prof gc} to collect B/op.
 *
 * <p>Scenarios:
 * <ul>
 *   <li>{@code numericHitFirst}  — BigDecimal hit at index 0 of a 5-element folded vector</li>
 *   <li>{@code numericHitLast}   — BigDecimal hit at index 4 of a 5-element folded vector (worst hit)</li>
 *   <li>{@code numericMiss}      — BigDecimal miss on a 5-element folded vector (full linear scan)</li>
 *   <li>{@code numericLargeHit}  — BigDecimal hit at index 49 of a 50-element folded vector</li>
 *   <li>{@code numericLargeMiss} — BigDecimal miss on a 50-element folded vector (full linear scan)</li>
 *   <li>{@code stringMiss}       — String miss on a 5-element folded vector</li>
 *   <li>{@code externalVectorHit}— dynamic List supplied at runtime, hit at last element</li>
 *   <li>{@code notInMiss}        — {@code not in}, subject absent (full scan, returns true)</li>
 * </ul>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class MembershipBenchmark {

    @Benchmark
    public boolean numericHitFirst(MembershipState state) {
        return state.support.evaluateNumericHitFirst();
    }

    @Benchmark
    public boolean numericHitLast(MembershipState state) {
        return state.support.evaluateNumericHitLast();
    }

    @Benchmark
    public boolean numericMiss(MembershipState state) {
        return state.support.evaluateNumericMiss();
    }

    @Benchmark
    public boolean numericLargeHit(MembershipState state) {
        return state.support.evaluateNumericLargeHit();
    }

    @Benchmark
    public boolean numericLargeMiss(MembershipState state) {
        return state.support.evaluateNumericLargeMiss();
    }

    @Benchmark
    public boolean stringMiss(MembershipState state) {
        return state.support.evaluateStringMiss();
    }

    @Benchmark
    public boolean externalVectorHit(MembershipState state) {
        return state.support.evaluateExternalVectorHit();
    }

    @Benchmark
    public boolean notInMiss(MembershipState state) {
        return state.support.evaluateNotInMiss();
    }

    @State(Scope.Thread)
    public static class MembershipState {

        MembershipBenchmarkSupport support;

        @Setup(Level.Trial)
        public void setUp() {
            support = new MembershipBenchmarkSupport();
        }
    }
}
