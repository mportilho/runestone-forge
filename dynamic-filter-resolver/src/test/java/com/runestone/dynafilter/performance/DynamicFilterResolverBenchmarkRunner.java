package com.runestone.dynafilter.performance;

import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public final class DynamicFilterResolverBenchmarkRunner {

    private DynamicFilterResolverBenchmarkRunner() {
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(DynamicFilterResolverBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .result("dynamic-filter-resolver/target/jmh-result.json")
                .build();
        new Runner(options).run();
    }
}
