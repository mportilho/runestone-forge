package com.runestone.expeval_mk3.benchmarks;

import org.openjdk.jmh.annotations.Benchmark;

public class InfrastructureBenchmark {

    @Benchmark
    public int baseline() {
        return 42;
    }
}
