package com.runestone.expeval.perf.jmh;

import com.runestone.expeval.expression.ExpressionContext;
import com.runestone.expeval.expression.tool.MixedFunctionProvider;
import com.runestone.expeval.support.callsite.OperationCallSiteFactory;
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
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class FunctionRegistrationOverheadBenchmark {

    private ExpressionContext contextWithDefaultsOnly;

    @Setup(Level.Trial)
    public void setup() {
        contextWithDefaultsOnly = new ExpressionContext();
    }

    @Benchmark
    public void createCallSitesFromProviderInstance(Blackhole blackhole) {
        blackhole.consume(OperationCallSiteFactory.createLambdaCallSites(new MixedFunctionProvider()).size());
    }

    @Benchmark
    public void createCallSitesFromProviderClass(Blackhole blackhole) {
        blackhole.consume(OperationCallSiteFactory.createLambdaCallSites(MixedFunctionProvider.class).size());
    }

    @Benchmark
    public void putFunctionsFromProviderPerRequest(Blackhole blackhole) {
        ExpressionContext context = new ExpressionContext();
        context.putFunctionsFromProvider(new MixedFunctionProvider());
        blackhole.consume(context.findFunction("instanceMultiply_2"));
    }

    @Benchmark
    public void defaultFunctionLookup(Blackhole blackhole) {
        blackhole.consume(contextWithDefaultsOnly.findFunction("avg_1"));
    }
}

