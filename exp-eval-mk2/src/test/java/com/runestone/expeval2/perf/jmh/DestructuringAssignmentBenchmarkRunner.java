package com.runestone.expeval2.perf.jmh;

import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Runs {@link DestructuringAssignmentBenchmark} with the JMH GC allocation profiler and prints
 * a two-table summary:
 *
 * <ol>
 *   <li>Standard results: benchmark name, ns/op, ±error, bytes/op (gc.alloc.rate.norm)</li>
 *   <li>Audit overhead per scenario: NoAudit vs WithAudit ns/op and allocation delta</li>
 * </ol>
 *
 * <p>Run from the IDE or CLI:
 * <pre>
 *   mvn test-compile -pl exp-eval-mk2 &amp;&amp; \
 *   mvn exec:java -pl exp-eval-mk2 \
 *     -Dexec.mainClass=com.runestone.expeval2.perf.jmh.DestructuringAssignmentBenchmarkRunner \
 *     -Dexec.classpathScope=test
 * </pre>
 */
public final class DestructuringAssignmentBenchmarkRunner {

    private static final String ALLOC_KEY_SUFFIX = "gc.alloc.rate.norm";

    private DestructuringAssignmentBenchmarkRunner() {
    }

    public static void main(String[] args) throws Exception {
        Options opts = new OptionsBuilder()
            .include(DestructuringAssignmentBenchmark.class.getSimpleName())
            .forks(0)  // in-process: avoids ForkedMain classpath issues when run via exec:java
            .warmupIterations(3)
            .warmupTime(TimeValue.milliseconds(300))
            .measurementIterations(5)
            .measurementTime(TimeValue.milliseconds(500))
            .timeUnit(TimeUnit.NANOSECONDS)
            .addProfiler(GCProfiler.class)
            .build();

        Collection<RunResult> results = new Runner(opts).run();
        printTable(results);
        printAuditOverheadTable(results);
    }

    // ── Standard results ────────────────────────────────────────────────────────

    private static void printTable(Collection<RunResult> results) {
        System.out.println();
        System.out.println("=".repeat(90));
        System.out.printf("%-44s  %10s  %8s  %12s%n", "Benchmark", "ns/op", "±error", "B/op");
        System.out.println("-".repeat(90));

        results.stream()
            .sorted(Comparator.comparing(r -> r.getPrimaryResult().getLabel()))
            .forEach(r -> {
                double nsOp = r.getPrimaryResult().getScore();
                double error = r.getPrimaryResult().getScoreError();
                double bOp = allocBytesPerOp(r);
                System.out.printf("%-44s  %10.1f  %8.1f  %12.1f%n",
                    shortLabel(r.getPrimaryResult().getLabel()), nsOp, error, bOp);
            });

        System.out.println("=".repeat(90));
    }

    // ── Audit overhead table ───────────────────────────────────────────────────

    private static final List<String> SCENARIOS = List.of(
        "vectorLiteral", "spread3Slots", "spread5Slots"
    );

    private static void printAuditOverheadTable(Collection<RunResult> results) {
        System.out.println();
        System.out.println("=".repeat(90));
        System.out.printf("%-16s  %12s  %12s  %8s  %12s  %12s  %10s%n",
            "Scenario", "NoAudit ns", "Audit ns", "overhead", "NoAudit B/op", "Audit B/op", "alloc delta");
        System.out.println("-".repeat(90));

        for (String scenario : SCENARIOS) {
            RunResult noAudit = findResult(results, scenario + "NoAudit");
            RunResult withAudit = findResult(results, scenario + "WithAudit");

            if (noAudit == null || withAudit == null) {
                System.out.printf("%-16s  (missing result)%n", scenario);
                continue;
            }

            double nsNoAudit = noAudit.getPrimaryResult().getScore();
            double nsWithAudit = withAudit.getPrimaryResult().getScore();
            double overhead = (nsWithAudit - nsNoAudit) / nsNoAudit * 100.0;

            double bNoAudit = allocBytesPerOp(noAudit);
            double bWithAudit = allocBytesPerOp(withAudit);
            double bDelta = bWithAudit - bNoAudit;

            System.out.printf("%-16s  %12.1f  %12.1f  %7.1f%%  %12.1f  %12.1f  %+10.1f%n",
                scenario, nsNoAudit, nsWithAudit, overhead, bNoAudit, bWithAudit, bDelta);
        }

        System.out.println("=".repeat(90));
        System.out.println("overhead = (Audit - NoAudit) / NoAudit × 100   |   alloc delta = Audit B/op - NoAudit B/op");
        System.out.println();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private static double allocBytesPerOp(RunResult result) {
        return result.getSecondaryResults().entrySet().stream()
            .filter(e -> e.getKey().endsWith(ALLOC_KEY_SUFFIX))
            .mapToDouble(e -> e.getValue().getScore())
            .findFirst()
            .orElse(Double.NaN);
    }

    private static RunResult findResult(Collection<RunResult> results, String methodSuffix) {
        return results.stream()
            .filter(r -> r.getPrimaryResult().getLabel().endsWith(methodSuffix))
            .findFirst()
            .orElse(null);
    }

    private static String shortLabel(String label) {
        // Strip fully-qualified class prefix, keep only ClassName.methodName
        String[] parts = label.split("\\.");
        return parts.length >= 2
            ? parts[parts.length - 2] + "." + parts[parts.length - 1]
            : label;
    }
}
