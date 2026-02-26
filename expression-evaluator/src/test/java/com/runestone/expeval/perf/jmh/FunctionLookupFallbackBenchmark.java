package com.runestone.expeval.perf.jmh;

import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.expeval.expression.ExpressionContext;
import com.runestone.expeval.operation.AbstractOperation;
import com.runestone.expeval.operation.CloningContext;
import com.runestone.expeval.operation.OperationContext;
import com.runestone.expeval.operation.OperationVisitor;
import com.runestone.expeval.operation.other.FunctionOperation;
import com.runestone.expeval.support.callsite.OperationCallSite;
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

import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class FunctionLookupFallbackBenchmark {

    private static final int INVOCATIONS_PER_EVALUATION = 64;
    private static final MethodType VAR_ARG_TYPE = MethodType.methodType(BigDecimal.class, Object[].class);

    private final AbstractOperation[] parameters = new AbstractOperation[]{
            new FixedValueOperation(BigDecimal.ONE),
            new FixedValueOperation(BigDecimal.TWO),
            new FixedValueOperation(BigDecimal.valueOf(3)),
            new FixedValueOperation(BigDecimal.valueOf(4)),
            new FixedValueOperation(BigDecimal.valueOf(5)),
            new FixedValueOperation(BigDecimal.valueOf(6)),
            new FixedValueOperation(BigDecimal.valueOf(7)),
            new FixedValueOperation(BigDecimal.valueOf(8))
    };

    private FunctionOperation sameContextOperation;
    private FunctionOperation splitContextOperation;
    private ExpressionContext userContextWithFallback;
    private ExpressionContext expressionContextWithFallback;
    private ExpressionContext emptyContext;

    private OperationContext sameContext;
    private OperationContext splitContext;

    @Setup(Level.Trial)
    public void setupTrial() {
        OperationCallSite fallbackCallSite = new OperationCallSite("sum", VAR_ARG_TYPE, args -> {
            BigDecimal result = BigDecimal.ZERO;
            for (Object arg : (Object[]) args[0]) {
                result = result.add((BigDecimal) arg);
            }
            return result;
        });

        Map<String, OperationCallSite> fallbackFunctions = new HashMap<>();
        fallbackFunctions.put(fallbackCallSite.getKeyName(), fallbackCallSite);

        userContextWithFallback = new ExpressionContext(fallbackFunctions);
        expressionContextWithFallback = new ExpressionContext(new HashMap<>(fallbackFunctions));
        emptyContext = new ExpressionContext(Map.of());

        sameContextOperation = new FunctionOperation("sum", parameters, false);
        splitContextOperation = new FunctionOperation("sum", parameters, false);
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
        sameContext = createOperationContext(userContextWithFallback, userContextWithFallback);
        splitContext = createOperationContext(expressionContextWithFallback, emptyContext);
    }

    @Benchmark
    public void fallbackLookupSameContext(Blackhole blackhole) {
        for (int i = 0; i < INVOCATIONS_PER_EVALUATION; i++) {
            blackhole.consume(sameContextOperation.evaluate(sameContext));
        }
    }

    @Benchmark
    public void fallbackLookupSplitContext(Blackhole blackhole) {
        for (int i = 0; i < INVOCATIONS_PER_EVALUATION; i++) {
            blackhole.consume(splitContextOperation.evaluate(splitContext));
        }
    }

    private static OperationContext createOperationContext(ExpressionContext expressionContext, ExpressionContext userContext) {
        return new OperationContext(
                MathContext.DECIMAL64,
                null,
                false,
                () -> ZonedDateTime.now().with(ChronoField.MICRO_OF_SECOND, 0),
                new DefaultDataConversionService(false),
                expressionContext,
                userContext,
                ZoneId.of("UTC"));
    }

    private static final class FixedValueOperation extends AbstractOperation {

        private final BigDecimal value;

        private FixedValueOperation(BigDecimal value) {
            this.value = value;
        }

        @Override
        protected Object resolve(OperationContext context) {
            return value;
        }

        @Override
        protected AbstractOperation createClone(CloningContext context) {
            return new FixedValueOperation(value);
        }

        @Override
        public void accept(OperationVisitor<?> visitor) {
            throw new UnsupportedOperationException("Not used in benchmark");
        }

        @Override
        protected void formatRepresentation(StringBuilder builder) {
            builder.append(value);
        }

        @Override
        protected String getOperationToken() {
            return "";
        }

        @Override
        public boolean getCacheHint() {
            return false;
        }
    }
}
