package com.runestone.expeval.perf.jmh;

import com.runestone.converters.DataConversionService;
import com.runestone.expeval.expression.Expression;
import com.runestone.expeval.expression.ExpressionContext;
import com.runestone.expeval.operation.AbstractOperation;
import com.runestone.expeval.operation.CloningContext;
import com.runestone.expeval.operation.OperationContext;
import com.runestone.expeval.operation.OperationVisitor;
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

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class TypeConversionHotPathBenchmark {

    private long sequence;

    private AlternatingTypeOperation alternatingTypeOperation;
    private OperationContext alternatingTypeContext;

    private Expression longExpression;
    private Expression doubleExpression;

    @Setup(Level.Trial)
    public void setup() {
        alternatingTypeOperation = new AlternatingTypeOperation();
        alternatingTypeOperation.expectedType(Boolean.class);
        alternatingTypeOperation.configureCaching(false);
        alternatingTypeContext = new OperationContext(
                MathContext.DECIMAL64,
                null,
                false,
                () -> ZonedDateTime.now().with(ChronoField.MICRO_OF_SECOND, 0),
                new BenchmarkConversionService(),
                new ExpressionContext(),
                new ExpressionContext(),
                ZoneId.systemDefault());

        longExpression = new Expression("a");
        doubleExpression = new Expression("a");
    }

    @Benchmark
    public void alternatingTypeConversion(Blackhole blackhole) {
        blackhole.consume(alternatingTypeOperation.evaluate(alternatingTypeContext));
    }

    @Benchmark
    public void baseOperationLongNormalization(Blackhole blackhole) {
        long value = ++sequence;
        longExpression.setVariable("a", value);
        blackhole.consume(longExpression.evaluate());
    }

    @Benchmark
    public void baseOperationDoubleNormalization(Blackhole blackhole) {
        double value = (++sequence) * 1.00001D;
        doubleExpression.setVariable("a", value);
        blackhole.consume(doubleExpression.evaluate());
    }

    private static final class AlternatingTypeOperation extends AbstractOperation {

        private boolean numberResult = true;

        @Override
        protected Object resolve(OperationContext context) {
            Object result = numberResult ? 10 : Boolean.TRUE;
            numberResult = !numberResult;
            return result;
        }

        @Override
        protected AbstractOperation createClone(CloningContext context) {
            AlternatingTypeOperation clone = new AlternatingTypeOperation();
            clone.numberResult = this.numberResult;
            return clone;
        }

        @Override
        public void accept(OperationVisitor<?> visitor) {
            throw new UnsupportedOperationException("Not used in benchmark");
        }

        @Override
        protected void formatRepresentation(StringBuilder builder) {
            builder.append("alternating");
        }

        @Override
        protected String getOperationToken() {
            return "";
        }
    }

    private static final class BenchmarkConversionService implements DataConversionService {

        @Override
        public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
            return Integer.class.equals(sourceType) && BigDecimal.class.equals(targetType);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <S, T> T convert(S source, Class<T> targetType) {
            if (targetType.isInstance(source)) {
                return (T) source;
            }
            if (source instanceof Integer value && BigDecimal.class.equals(targetType)) {
                return (T) BigDecimal.valueOf(value.longValue());
            }
            throw new IllegalStateException("Unsupported conversion for benchmark: " + source.getClass() + " -> " + targetType);
        }
    }
}

