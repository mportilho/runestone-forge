/*******************************************************************************
 * MIT License
 *
 * Copyright (c) 2022. Marcelo Silva Portilho
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/

package com.runestone.expeval.benchmark;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval.expression.ExpressionContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;

import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Threads(1)
public class TestOperationBenchmark {

    private Expression expression;
    ExpressionContext context = new ExpressionContext();
    private final Random random = new Random(System.nanoTime());

    @Param({"10", "100", "1000"})
    public int iterations;

    @Setup(Level.Iteration)
    public void setupIteration() {
        // executed before each invocation of the iteration
        expression = new Expression("a + b");
//        expression = new Expression("sum(a, b)");
        expression.addFunctionFromObject(new FunctionProviderForTests(BigDecimal.valueOf(5)));
        context.putDictionaryEntry("a", 5);
        context.putDictionaryEntry("b", 10);

    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
        // executed before each invocation of the benchmark
        expression.setVariable("a", BigDecimal.valueOf(random.nextInt()));
        expression.setVariable("b", BigDecimal.valueOf(random.nextInt()));
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Fork(warmups = 1, value = 1)
    @Warmup(batchSize = -1, iterations = 3, time = 50, timeUnit = TimeUnit.SECONDS)
    @Measurement(batchSize = -1, iterations = 5, time = 100, timeUnit = TimeUnit.SECONDS)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void invocation() {
        expression.evaluate();
    }

    @Test
    @Disabled
    public void benchmark() throws Exception {
        String[] argv = {};
        org.openjdk.jmh.Main.main(argv);
    }

}
