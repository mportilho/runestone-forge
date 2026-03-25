package com.runestone.expeval2.perf.jmh;

import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

public final class ObjectNavigationBenchmarkRunner {

    private ObjectNavigationBenchmarkRunner() {
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(ObjectNavigationBenchmark.class.getSimpleName())
                .forks(0)
                .warmupIterations(3)
                .warmupTime(TimeValue.milliseconds(300))
                .measurementIterations(5)
                .measurementTime(TimeValue.milliseconds(500))
                .timeUnit(TimeUnit.NANOSECONDS)
                .addProfiler(GCProfiler.class)
                .build();

        Collection<RunResult> results = new Runner(options).run();
        printTable(results);
    }

    private static void printTable(Collection<RunResult> results) {
        System.out.println();
        System.out.println("=".repeat(90));
        System.out.printf("%-50s  %10s  %10s%n", "Benchmark", "ns/op", "B/op");
        System.out.println("-".repeat(90));

        results.stream()
                .filter(result -> !result.getPrimaryResult().getLabel().contains("gc"))
                .sorted(Comparator.comparing(result -> result.getPrimaryResult().getLabel()))
                .forEach(result -> {
                    double bytesPerOp = result.getSecondaryResults().entrySet().stream()
                            .filter(entry -> entry.getKey().contains("gc.alloc.rate.norm"))
                            .mapToDouble(entry -> entry.getValue().getScore())
                            .findFirst()
                            .orElse(Double.NaN);
                    System.out.printf(
                            "%-50s  %10.1f  %10.1f%n",
                            result.getPrimaryResult().getLabel(),
                            result.getPrimaryResult().getScore(),
                            bytesPerOp
                    );
                });

        System.out.println("=".repeat(90));
    }
}
