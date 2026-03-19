package com.runestone.expeval2.perf.jmh;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class MathFunctionsBenchmarkRunner {

    @Test
    public void runBenchmarks() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MathFunctionsBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.TEXT)
                .build();
        new Runner(opt).run();
    }
}
