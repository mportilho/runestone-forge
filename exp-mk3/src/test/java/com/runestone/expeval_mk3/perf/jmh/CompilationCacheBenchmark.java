package com.runestone.expeval_mk3.perf.jmh;

import com.runestone.expeval_mk3.api.EngineCacheInvalidation;
import com.runestone.expeval_mk3.api.ExpressionCompilationResult;
import com.runestone.expeval_mk3.api.ExpressionEngine;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.MathExpression;
import com.runestone.expeval_mk3.api.UncachedCompilation;
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
import org.openjdk.jmh.infra.Blackhole;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * Issue #137: the four paired Etapa 9 gates, measuring the cache-free pipeline, an {@code
 * ExpressionEngine} miss, a pure hit, and a hit followed by {@code asMath()} from the same literal source
 * text ({@link #SOURCE}) and an identically built {@code ExpressionEnvironment} ({@link #buildEnvironment()}).
 * Each of the four benchmark methods runs in its own forked JVM, so no two of them ever share the exact
 * same {@code ExpressionEnvironment} instance; pairing means identical construction, not object identity
 * across forks. Each state builds its engine and environment once per JMH trial, never per operation. The
 * miss path is prepared outside the measured window by a benchmark-only invalidation seam ({@link
 * EngineCacheInvalidation}); the measured source and environment are never altered to fabricate a miss.
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class CompilationCacheBenchmark {

    private static final String SOURCE = "a + b * 2";

    private static ExpressionEnvironment buildEnvironment() {
        return ExpressionEnvironment.builder()
                .externalSymbol("a", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("b", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
    }

    @Benchmark
    public void pipelineUncached(PipelineFixture fixture, Blackhole blackhole) {
        ExpressionCompilationResult result = UncachedCompilation.compile(fixture.source, fixture.environment);
        blackhole.consume(result);
    }

    @Benchmark
    public void engineMiss(MissFixture fixture, MissPreparation preparation, Blackhole blackhole) {
        ExpressionCompilationResult result = fixture.engine.compile(fixture.source, fixture.environment);
        blackhole.consume(result);
    }

    @Benchmark
    public void engineHitPure(WarmFixture fixture, Blackhole blackhole) {
        ExpressionCompilationResult result = fixture.engine.compile(fixture.source, fixture.environment);
        blackhole.consume(result);
    }

    @Benchmark
    public void engineHitAsMath(WarmFixture fixture, Blackhole blackhole) {
        MathExpression math = fixture.engine.compileOrThrow(fixture.source, fixture.environment).asMath();
        blackhole.consume(math);
    }

    @State(Scope.Benchmark)
    public static class PipelineFixture {

        private String source;
        private ExpressionEnvironment environment;

        @Setup(Level.Trial)
        public void setUp() {
            source = SOURCE;
            environment = buildEnvironment();
        }
    }

    @State(Scope.Benchmark)
    public static class MissFixture {

        private String source;
        private ExpressionEnvironment environment;
        private ExpressionEngine engine;

        @Setup(Level.Trial)
        public void setUp() {
            source = SOURCE;
            environment = buildEnvironment();
            engine = ExpressionEngine.builder().build();
        }
    }

    /**
     * Invalidates the shared {@link MissFixture} entry once per invocation, outside the measured window,
     * so every timed {@code engineMiss} call observes a real miss without rebuilding the engine or
     * environment and without altering the measured source.
     */
    @State(Scope.Thread)
    public static class MissPreparation {

        @Setup(Level.Invocation)
        public void invalidate(MissFixture fixture) {
            EngineCacheInvalidation.invalidate(fixture.engine, fixture.source, fixture.environment);
        }
    }

    @State(Scope.Benchmark)
    public static class WarmFixture {

        private String source;
        private ExpressionEnvironment environment;
        private ExpressionEngine engine;

        @Setup(Level.Trial)
        public void setUp() {
            source = SOURCE;
            environment = buildEnvironment();
            engine = ExpressionEngine.builder().build();
            engine.compile(source, environment);
        }
    }
}
