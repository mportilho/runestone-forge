package com.runestone.expeval2.perf.jmh;

import com.runestone.expeval2.catalog.functions.StringFunctions;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(value = 1, jvmArgs = {"-Xms1G", "-Xmx1G"})
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class StringFunctionsRegexBenchmark {

    private static final String DIGIT_REGEX = "\\d+";
    private static final String REPLACEMENT = "_";

    private String splitInput;
    private String replaceInput;

    @Setup
    public void setup() {
        splitInput = "col1-100,col2-200,col3-300,col4-400,col5-500,col6-600,col7-700,col8-800,";
        replaceInput = "item100-item200-item300-item400-item500-item600-item700-item800";
    }

    @Benchmark
    public List<String> baselineSplit() {
        return Arrays.asList(Pattern.compile(DIGIT_REGEX).split(splitInput, -1));
    }

    @Benchmark
    public List<String> cachedSplit() {
        return StringFunctions.split(splitInput, DIGIT_REGEX);
    }

    @Benchmark
    public String baselineReplaceAll() {
        return replaceInput.replaceAll(DIGIT_REGEX, REPLACEMENT);
    }

    @Benchmark
    public String cachedReplaceAll() {
        return StringFunctions.replaceAll(replaceInput, DIGIT_REGEX, REPLACEMENT);
    }
}
