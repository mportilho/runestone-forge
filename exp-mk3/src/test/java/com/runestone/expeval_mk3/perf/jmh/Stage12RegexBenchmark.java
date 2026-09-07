package com.runestone.expeval_mk3.perf.jmh;

import com.runestone.expeval_mk3.api.ExpressionEngine;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.LogicalExpression;
import com.runestone.expeval_mk3.api.ResultExpression;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Etapa 12 characterization for benign and adversarial linear regex workloads. */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class Stage12RegexBenchmark {

    @Benchmark
    public boolean literalMatch(RegexState state) {
        return state.literalMatch.compute(state.textOverride);
    }

    @Benchmark
    public Object dynamicReplaceAll(RegexState state) {
        return state.dynamicReplaceAll.compute(state.dynamicOverrides);
    }

    @Benchmark
    public Object dynamicSplit(RegexState state) {
        return state.dynamicSplit.compute(state.dynamicOverrides);
    }

    @Benchmark
    public boolean adversarialNonMatch(AdversarialRegexState state) {
        return state.expression.compute(state.override);
    }

    @State(Scope.Benchmark)
    public static class RegexState {

        private LogicalExpression literalMatch;
        private ResultExpression dynamicReplaceAll;
        private ResultExpression dynamicSplit;
        private Map<String, Object> textOverride;
        private Map<String, Object> dynamicOverrides;

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = ExpressionEnvironment.builder()
                    .externalSymbol("text", "ABC-1234", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .externalSymbol("pattern", "[-]", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .build();
            ExpressionEngine engine = ExpressionEngine.builder().build();
            literalMatch = engine.compileOrThrow("text =~ \"^[A-Z]{3}-\\\\d{4}$\"", environment).asLogical();
            dynamicReplaceAll = engine.compileOrThrow("replaceAll(text, pattern, \"_\")", environment).asResult();
            dynamicSplit = engine.compileOrThrow("split(text, pattern)", environment).asResult();
            textOverride = Map.of("text", "XYZ-9876");
            dynamicOverrides = Map.of("text", "ABC-1234", "pattern", "[-]");
        }
    }

    @State(Scope.Benchmark)
    public static class AdversarialRegexState {

        @Param({"128", "1024", "8192"})
        private int inputLength;

        private LogicalExpression expression;
        private Map<String, Object> override;

        @Setup(Level.Trial)
        public void setUp() {
            ExpressionEnvironment environment = ExpressionEnvironment.builder()
                    .externalSymbol("text", "", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                    .build();
            expression = ExpressionEngine.builder().build()
                    .compileOrThrow("text =~ \"(a+)+b\"", environment)
                    .asLogical();
            override = Map.of("text", "a".repeat(inputLength));
        }
    }
}
