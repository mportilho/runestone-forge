package com.runestone.expeval.perf.jmh;

import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

public final class ObjectEvaluatorBenchmarkRunner {

    private ObjectEvaluatorBenchmarkRunner() {
    }

    public static void main(String[] args) throws Exception {
        Options opts = new OptionsBuilder()
            .include(ObjectEvaluatorBenchmark.class.getSimpleName())
            .forks(1)
            .warmupIterations(3)
            .warmupTime(TimeValue.milliseconds(300))
            .measurementIterations(5)
            .measurementTime(TimeValue.milliseconds(500))
            .timeUnit(TimeUnit.NANOSECONDS)
            .build();

        Collection<RunResult> results = new Runner(opts).run();
        printTable(results);
    }

    private static void printTable(Collection<RunResult> results) {
        System.out.println();
        System.out.println("=".repeat(80));
        System.out.printf("%-50s  %10s  %8s%n", "Benchmark", "ns/op", "±error");
        System.out.println("-".repeat(80));

        results.stream()
            .sorted(Comparator.comparing(r -> r.getPrimaryResult().getLabel()))
            .forEach(r -> System.out.printf(
                "%-50s  %10.1f  %8.1f%n",
                r.getPrimaryResult().getLabel(),
                r.getPrimaryResult().getScore(),
                r.getPrimaryResult().getScoreError()
            ));

        System.out.println("=".repeat(80));
    }
}
