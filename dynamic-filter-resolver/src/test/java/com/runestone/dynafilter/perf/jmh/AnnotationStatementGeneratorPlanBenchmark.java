package com.runestone.dynafilter.perf.jmh;

import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementGenerator;
import com.runestone.dynafilter.core.generator.annotation.AnnotationStatementInput;
import com.runestone.dynafilter.core.generator.annotation.testquery.SearchPeopleAndGames;
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

import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
public class AnnotationStatementGeneratorPlanBenchmark {

    @Benchmark
    public void warmedPlan(WarmedState state, Blackhole blackhole) {
        StatementWrapper result = state.generator.generateStatements(state.input, state.parameters);
        blackhole.consume(result);
    }

    @Benchmark
    public void coldLocalPlan(ColdState state, Blackhole blackhole) {
        StatementWrapper result = state.generator.generateStatements(state.input, state.parameters);
        blackhole.consume(result);
    }

    @State(Scope.Benchmark)
    public static class WarmedState extends InputState {
        private AnnotationStatementGenerator generator;

        @Setup(Level.Trial)
        public void setupGenerator() {
            generator = new AnnotationStatementGenerator();
            generator.generateStatements(input, parameters);
        }
    }

    @State(Scope.Thread)
    public static class ColdState extends InputState {
        private AnnotationStatementGenerator generator;

        @Setup(Level.Invocation)
        public void setupGenerator() {
            generator = new AnnotationStatementGenerator();
        }
    }

    static class InputState {
        final AnnotationStatementInput input = new AnnotationStatementInput(
                SearchPeopleAndGames.class,
                SearchPeopleAndGames.class.getAnnotations()
        );
        final Map<String, Object> parameters = Map.of(
                "name", "English",
                "documentNumber", "12345678900",
                "all", "true",
                "deleted", "false",
                "genre", "RPG",
                "state", "ON_USE",
                "minCreationDate", "2021-01-01",
                "maxCreationDate", "2021-12-31"
        );
    }
}
