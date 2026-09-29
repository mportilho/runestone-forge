package com.runestone.expeval.perf.jmh;

import org.openjdk.jmh.annotations.Benchmark;

public class InfrastructureBenchmark {

    @Benchmark
    public int harnessSmokeCheck() {
        return 42;
    }
}
