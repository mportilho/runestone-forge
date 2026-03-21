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

public final class LogarithmPrecisionBenchmarkRunner {

    private LogarithmPrecisionBenchmarkRunner() {
    }

    public static void main(String[] args) throws Exception {
        Options opts = new OptionsBuilder()
            .include(CrossModuleExpressionEngineBenchmark.class.getSimpleName() + ".mk2LogarithmChain.*")
            .forks(1)
            .warmupIterations(3)
            .warmupTime(TimeValue.milliseconds(500))
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
        System.out.println("=".repeat(90));
        System.out.printf("%-52s  %12s  %12s%n", "Benchmark", "ns/op", "B/op");
        System.out.println("-".repeat(90));

        results.stream()
            .filter(r -> !r.getPrimaryResult().getLabel().contains("gc"))
            .sorted(Comparator.comparing(r -> r.getPrimaryResult().getLabel()))
            .forEach(r -> {
                double bop = r.getSecondaryResults().entrySet().stream()
                    .filter(e -> e.getKey().contains("gc.alloc.rate.norm"))
                    .mapToDouble(e -> e.getValue().getScore())
                    .findFirst()
                    .orElse(Double.NaN);
                System.out.printf("%-52s  %12.1f  %12.1f%n",
                    r.getPrimaryResult().getLabel(),
                    r.getPrimaryResult().getScore(),
                    bop);
            });

        System.out.println("=".repeat(90));
        System.out.println();

        printComparisonTable(results);
    }

    private static void printComparisonTable(Collection<RunResult> results) {
        double d128ns = scoreFor(results, "mk2LogarithmChainDecimal128");
        double d64ns = scoreFor(results, "mk2LogarithmChainDecimal64");
        double d128bop = bopFor(results, "mk2LogarithmChainDecimal128");
        double d64bop = bopFor(results, "mk2LogarithmChainDecimal64");

        System.out.printf("%-20s  %12s  %12s  %10s%n", "Metric", "DECIMAL128", "DECIMAL64", "speedup");
        System.out.println("-".repeat(60));
        System.out.printf("%-20s  %12.1f  %12.1f  %9.2fx%n", "ns/op", d128ns, d64ns, d128ns / d64ns);
        System.out.printf("%-20s  %12.1f  %12.1f  %9.2fx%n", "B/op", d128bop, d64bop, d128bop / d64bop);
        System.out.println("=".repeat(60));
        System.out.println("speedup > 1.0 means DECIMAL64 is faster / allocates less");
    }

    private static double scoreFor(Collection<RunResult> results, String suffix) {
        return results.stream()
            .filter(r -> r.getPrimaryResult().getLabel().endsWith(suffix))
            .mapToDouble(r -> r.getPrimaryResult().getScore())
            .findFirst()
            .orElse(Double.NaN);
    }

    private static double bopFor(Collection<RunResult> results, String suffix) {
        return results.stream()
            .filter(r -> r.getPrimaryResult().getLabel().endsWith(suffix))
            .flatMap(r -> r.getSecondaryResults().entrySet().stream())
            .filter(e -> e.getKey().contains("gc.alloc.rate.norm"))
            .mapToDouble(e -> e.getValue().getScore())
            .findFirst()
            .orElse(Double.NaN);
    }
}
