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

public final class CompilePathAllocationBenchmarkRunner {

    private CompilePathAllocationBenchmarkRunner() {
    }

    public static void main(String[] args) throws Exception {
        Options opts = new OptionsBuilder()
            .include(CompilePathAllocationBenchmark.class.getSimpleName())
            .forks(0)
            .warmupIterations(3)
            .warmupTime(TimeValue.milliseconds(300))
            .measurementIterations(5)
            .measurementTime(TimeValue.milliseconds(500))
            .timeUnit(TimeUnit.NANOSECONDS)
            .addProfiler(GCProfiler.class)
            .build();

        Collection<RunResult> results = new Runner(opts).run();
        printTable(results);
    }

    private static void printTable(Collection<RunResult> results) {
        System.out.println();
        System.out.println("=".repeat(80));
        System.out.printf("%-42s  %10s  %10s%n", "Benchmark", "ns/op", "B/op");
        System.out.println("-".repeat(80));

        results.stream()
            .filter(r -> !r.getPrimaryResult().getLabel().contains("gc"))
            .sorted(Comparator.comparing(r -> r.getPrimaryResult().getLabel()))
            .forEach(r -> {
                double bop = r.getSecondaryResults().entrySet().stream()
                    .filter(e -> e.getKey().contains("gc.alloc.rate.norm"))
                    .mapToDouble(e -> e.getValue().getScore())
                    .findFirst()
                    .orElse(Double.NaN);
                System.out.printf("%-42s  %10.1f  %10.1f%n",
                    r.getPrimaryResult().getLabel(),
                    r.getPrimaryResult().getScore(),
                    bop);
            });

        System.out.println("=".repeat(80));
        System.out.println();
    }
}
