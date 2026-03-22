package com.runestone.expeval2.perf;

import com.runestone.expeval2.perf.jmh.CrossModuleComparisonBenchmark;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.concurrent.TimeUnit;

/**
 * Runs {@link CrossModuleComparisonBenchmark} with a quick configuration suitable for
 * generating the comparison tables (1 fork, reduced iterations).
 *
 * <p>Run via Maven:
 * <pre>
 *   mvn test-compile -pl exp-eval-mk2 -am -q
 *   mvn exec:java \
 *     -pl exp-eval-mk2 \
 *     -Dexec.mainClass=com.runestone.expeval2.perf.CrossModuleComparisonBenchmarkMain \
 *     -Dexec.classpathScope=test
 * </pre>
 *
 * <p>Results are written to {@code /tmp/cross-module-comparison.txt} and also printed to stdout.
 */
public final class CrossModuleComparisonBenchmarkMain {

    private CrossModuleComparisonBenchmarkMain() {
    }

    public static void main(String[] args) throws RunnerException {
        Options opts = new OptionsBuilder()
            .include(CrossModuleComparisonBenchmark.class.getSimpleName())
            .warmupIterations(3)
            .warmupTime(TimeValue.milliseconds(500))
            .measurementIterations(5)
            .measurementTime(TimeValue.milliseconds(500))
            .forks(0)
            .addProfiler(GCProfiler.class)
            .resultFormat(ResultFormatType.TEXT)
            .result("/tmp/cross-module-comparison.txt")
            .build();

        new Runner(opts).run();

        System.out.println();
        System.out.println("Full results written to /tmp/cross-module-comparison.txt");
    }
}
