package com.runestone.expeval_mk3.perf.jmh;

import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseResult;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

public class ParsingBenchmark {

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @Warmup(iterations = 0)
    @Measurement(iterations = 1)
    @Fork(1)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public ParseResult coldParser(Cases cases) {
        return new ExpressionParser().parse(cases.source);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 3)
    @Measurement(iterations = 5)
    @Fork(1)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public ParseResult warmParser(WarmParser parser, Cases cases) {
        return parser.expressionParser.parse(cases.source);
    }

    @State(Scope.Benchmark)
    public static class WarmParser {

        private final ExpressionParser expressionParser = new ExpressionParser();

        @Setup(Level.Trial)
        public void setUp() {
            expressionParser.warmUp();
        }
    }

    @State(Scope.Benchmark)
    public static class Cases {

        private final String source = "a ?? b or c and d = e xor f || g + h * -i root j ^ k%";
    }
}
