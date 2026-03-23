package com.runestone.expeval2.perf.jmh;

import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class CrossModuleBenchmarkRunner {

    private static final String ALLOC_KEY_SUFFIX = "gc.alloc.rate.norm";
    private static final String LEGACY_SUFFIX = "_legacy";
    private static final String COMPUTE_SUFFIX = "_mk2Compute";
    private static final String AUDIT_SUFFIX = "_mk2Audit";
    private static final List<String> SCENARIOS = List.of(
        "literalDense",
        "variableChurn",
        "userFunction",
        "conditional",
        "powerChain"
    );

    private CrossModuleBenchmarkRunner() {
    }

    public static void main(String[] args) throws Exception {
        Options opts = new OptionsBuilder()
            .include(CrossModuleComparisonBenchmark.class.getSimpleName())
            .forks(0)
            .warmupIterations(3)
            .warmupTime(TimeValue.milliseconds(300))
            .measurementIterations(5)
            .measurementTime(TimeValue.milliseconds(500))
            .timeUnit(TimeUnit.NANOSECONDS)
            .addProfiler(GCProfiler.class)
            .build();

        Collection<RunResult> results = new Runner(opts).run();
        printRawResults(results);
        printComparisonGroup(
            "Group 1 - public API end-to-end: legacy.evaluate() vs mk2.compute(Map)",
            LEGACY_SUFFIX,
            COMPUTE_SUFFIX,
            results
        );
        printComparisonGroup(
            "Group 2 - public API end-to-end: legacy.evaluate() vs mk2.computeWithAudit(Map)",
            LEGACY_SUFFIX,
            AUDIT_SUFFIX,
            results
        );
        printAuditOverheadTable(results);
    }

    private static void printRawResults(Collection<RunResult> results) {
        System.out.println();
        System.out.println("=".repeat(100));
        System.out.printf("%-56s  %12s  %8s  %12s%n", "Benchmark", "ns/op", "±error", "B/op");
        System.out.println("-".repeat(100));

        results.stream()
            .sorted(Comparator.comparing(r -> r.getPrimaryResult().getLabel()))
            .forEach(result -> System.out.printf(
                "%-56s  %12.1f  %8.1f  %12.1f%n",
                shortLabel(result.getPrimaryResult().getLabel()),
                result.getPrimaryResult().getScore(),
                result.getPrimaryResult().getScoreError(),
                allocBytesPerOp(result)
            ));

        System.out.println("=".repeat(100));
    }

    private static void printComparisonGroup(
        String title,
        String baselineSuffix,
        String contenderSuffix,
        Collection<RunResult> results
    ) {
        System.out.println();
        System.out.println(title);
        System.out.println("=".repeat(100));
        System.out.printf(
            "%-16s  %12s  %12s  %9s  %12s  %12s  %9s%n",
            "Scenario",
            "legacy ns/op",
            "mk2 ns/op",
            "delta",
            "legacy B/op",
            "mk2 B/op",
            "delta"
        );
        System.out.println("-".repeat(100));

        for (String scenario : SCENARIOS) {
            RunResult baseline = findResult(results, scenario + baselineSuffix);
            RunResult contender = findResult(results, scenario + contenderSuffix);

            if (baseline == null || contender == null) {
                System.out.printf("%-16s  (missing result)%n", scenario);
                continue;
            }

            double baselineNs = baseline.getPrimaryResult().getScore();
            double contenderNs = contender.getPrimaryResult().getScore();
            double baselineAlloc = allocBytesPerOp(baseline);
            double contenderAlloc = allocBytesPerOp(contender);

            System.out.printf(
                "%-16s  %12.1f  %12.1f  %8s  %12.1f  %12.1f  %8s%n",
                scenario,
                baselineNs,
                contenderNs,
                formatDeltaPercent(baselineNs, contenderNs),
                baselineAlloc,
                contenderAlloc,
                formatDeltaPercent(baselineAlloc, contenderAlloc)
            );
        }

        System.out.println("=".repeat(100));
        System.out.println("delta = (mk2 - legacy) / legacy x 100");
    }

    private static void printAuditOverheadTable(Collection<RunResult> results) {
        System.out.println();
        System.out.println("Audit overhead - mk2.computeWithAudit(Map) vs mk2.compute(Map)");
        System.out.println("=".repeat(100));
        System.out.printf(
            "%-16s  %12s  %12s  %9s  %12s  %12s  %9s%n",
            "Scenario",
            "compute ns/op",
            "audit ns/op",
            "delta",
            "compute B/op",
            "audit B/op",
            "delta"
        );
        System.out.println("-".repeat(100));

        for (String scenario : SCENARIOS) {
            RunResult compute = findResult(results, scenario + COMPUTE_SUFFIX);
            RunResult audit = findResult(results, scenario + AUDIT_SUFFIX);

            if (compute == null || audit == null) {
                System.out.printf("%-16s  (missing result)%n", scenario);
                continue;
            }

            double computeNs = compute.getPrimaryResult().getScore();
            double auditNs = audit.getPrimaryResult().getScore();
            double computeAlloc = allocBytesPerOp(compute);
            double auditAlloc = allocBytesPerOp(audit);

            System.out.printf(
                "%-16s  %12.1f  %12.1f  %8s  %12.1f  %12.1f  %8s%n",
                scenario,
                computeNs,
                auditNs,
                formatDeltaPercent(computeNs, auditNs),
                computeAlloc,
                auditAlloc,
                formatDeltaPercent(computeAlloc, auditAlloc)
            );
        }

        System.out.println("=".repeat(100));
        System.out.println("delta = (audit - compute) / compute x 100");
    }

    private static RunResult findResult(Collection<RunResult> results, String methodSuffix) {
        return results.stream()
            .filter(result -> result.getPrimaryResult().getLabel().endsWith(methodSuffix))
            .findFirst()
            .orElse(null);
    }

    private static double allocBytesPerOp(RunResult result) {
        return result.getSecondaryResults().entrySet().stream()
            .filter(entry -> entry.getKey().endsWith(ALLOC_KEY_SUFFIX))
            .mapToDouble(entry -> entry.getValue().getScore())
            .findFirst()
            .orElse(Double.NaN);
    }

    private static String formatDeltaPercent(double baseline, double contender) {
        if (Double.isNaN(baseline) || Double.isNaN(contender) || baseline == 0.0d) {
            return "n/a";
        }
        return "%+.1f%%".formatted(((contender - baseline) / baseline) * 100.0d);
    }

    private static String shortLabel(String label) {
        String[] parts = label.split("\\.");
        return parts.length >= 2
            ? parts[parts.length - 2] + "." + parts[parts.length - 1]
            : label;
    }
}
