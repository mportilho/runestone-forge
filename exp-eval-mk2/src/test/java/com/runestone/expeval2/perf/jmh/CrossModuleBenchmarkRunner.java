package com.runestone.expeval2.perf.jmh;

import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

public final class CrossModuleBenchmarkRunner {

    private CrossModuleBenchmarkRunner() {
    }

    public static void main(String[] args) throws Exception {
        Options opts = new OptionsBuilder()
            .include(CrossModuleExpressionEngineBenchmark.class.getSimpleName())
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
        System.out.printf("%-46s  %10s  %8s%n", "Benchmark", "ns/op", "error");
        System.out.println("-".repeat(80));

        results.stream()
            .sorted(Comparator.comparing(r -> r.getPrimaryResult().getLabel()))
            .forEach(r -> System.out.printf(
                "%-46s  %10.1f  %8.1f%n",
                r.getPrimaryResult().getLabel(),
                r.getPrimaryResult().getScore(),
                r.getPrimaryResult().getScoreError()
            ));

        System.out.println("=".repeat(80));
        System.out.println();
        printComparisonTable(results);
    }

    private static void printComparisonTable(Collection<RunResult> results) {
        String[] scenarios = {"LiteralDense", "VariableChurn", "UserFunction", "Conditional", "LogarithmChain", "PowerChain"};

        System.out.printf("%-20s  %12s  %12s  %8s%n", "Scenario", "legacy ns/op", "mk2 ns/op", "ratio");
        System.out.println("-".repeat(60));

        for (String scenario : scenarios) {
            String legacyKey = "legacy" + scenario;
            String mk2Key = "mk2" + scenario;

            double legacyScore = results.stream()
                .filter(r -> r.getPrimaryResult().getLabel().endsWith(legacyKey))
                .mapToDouble(r -> r.getPrimaryResult().getScore())
                .findFirst()
                .orElse(Double.NaN);

            double mk2Score = results.stream()
                .filter(r -> r.getPrimaryResult().getLabel().endsWith(mk2Key))
                .mapToDouble(r -> r.getPrimaryResult().getScore())
                .findFirst()
                .orElse(Double.NaN);

            double ratio = legacyScore / mk2Score;
            System.out.printf("%-20s  %12.1f  %12.1f  %8.2fx%n", scenario, legacyScore, mk2Score, ratio);
        }

        System.out.println("=".repeat(60));
        System.out.println("ratio > 1.0 means legacy is slower; ratio < 1.0 means legacy is faster");
    }
}
