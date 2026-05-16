package com.runestone.dynafilter.modules.performance;

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
                .result("target/jmh-result.json")
                .resultFormat(ResultFormatType.JSON)
                .build();
        new Runner(options).run();
    }
}
