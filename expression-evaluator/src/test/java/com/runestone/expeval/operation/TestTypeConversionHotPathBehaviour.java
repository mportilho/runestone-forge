/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2023 Marcelo Silva Portilho
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.runestone.expeval.operation;

import com.runestone.converters.DataConversionService;
import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.expeval.expression.ExpressionContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

class TestTypeConversionHotPathBehaviour {

    @Test
    void testAlternatingTypesShouldAvoidRedundantFallbackConversions() {
        CountingConversionService conversionService = new CountingConversionService();
        OperationContext context = new OperationContext(
                MathContext.DECIMAL64,
                null,
                false,
                () -> ZonedDateTime.now().with(ChronoField.MICRO_OF_SECOND, 0),
                conversionService,
                new ExpressionContext(),
                new ExpressionContext(),
                ZoneId.systemDefault());

        AlternatingTypeOperation operation = new AlternatingTypeOperation();
        operation.expectedType(Boolean.class);
        operation.configureCaching(false);

        for (int i = 0; i < 10; i++) {
            operation.evaluate(context);
        }

        Assertions.assertThat(conversionService.getConvertCalls()).isEqualTo(5);
        Assertions.assertThat(conversionService.getBooleanTargetAttempts()).isZero();
    }

    @Test
    void testBaseOperationShouldKeepMathContextAndScaleForLongValues() {
        OperationContext context = new OperationContext(
                new MathContext(3),
                1,
                false,
                () -> ZonedDateTime.now().with(ChronoField.MICRO_OF_SECOND, 0),
                new DefaultDataConversionService(false),
                new ExpressionContext(),
                new ExpressionContext(),
                ZoneId.systemDefault());
        FixedNumberOperation operation = new FixedNumberOperation(12345L);
        operation.expectedType(Long.class);
        operation.configureCaching(false);
        BaseOperation baseOperation = new BaseOperation(operation, Collections.emptyMap());
        baseOperation.configureCaching(false);

        Assertions.assertThat(baseOperation.<BigDecimal>evaluate(context)).isEqualByComparingTo("12300.0");
    }

    @Test
    void testBaseOperationShouldKeepMathContextForDoubleValues() {
        OperationContext context = new OperationContext(
                new MathContext(3),
                null,
                false,
                () -> ZonedDateTime.now().with(ChronoField.MICRO_OF_SECOND, 0),
                new DefaultDataConversionService(false),
                new ExpressionContext(),
                new ExpressionContext(),
                ZoneId.systemDefault());
        FixedNumberOperation operation = new FixedNumberOperation(1.23456789D);
        operation.expectedType(Double.class);
        operation.configureCaching(false);
        BaseOperation baseOperation = new BaseOperation(operation, Collections.emptyMap());
        baseOperation.configureCaching(false);

        Assertions.assertThat(baseOperation.<BigDecimal>evaluate(context)).isEqualByComparingTo("1.23");
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
            throw new UnsupportedOperationException("Not used in this test");
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

    private static final class CountingConversionService implements DataConversionService {

        private final AtomicInteger convertCalls = new AtomicInteger();
        private final AtomicInteger booleanTargetAttempts = new AtomicInteger();

        @Override
        public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
            return Integer.class.equals(sourceType) && BigDecimal.class.equals(targetType);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <S, T> T convert(S source, Class<T> targetType) {
            convertCalls.incrementAndGet();
            if (targetType.isInstance(source)) {
                return (T) source;
            }
            if (Boolean.class.equals(targetType)) {
                booleanTargetAttempts.incrementAndGet();
                throw new IllegalStateException("Boolean conversion should not be attempted in hot path fallback");
            }
            if (source instanceof Integer value && BigDecimal.class.equals(targetType)) {
                return (T) BigDecimal.valueOf(value.longValue());
            }
            return null;
        }

        int getConvertCalls() {
            return convertCalls.get();
        }

        int getBooleanTargetAttempts() {
            return booleanTargetAttempts.get();
        }
    }

    private static final class FixedNumberOperation extends AbstractOperation {

        private final Number value;

        private FixedNumberOperation(Number value) {
            this.value = value;
        }

        @Override
        protected Object resolve(OperationContext context) {
            return value;
        }

        @Override
        protected AbstractOperation createClone(CloningContext context) {
            return new FixedNumberOperation(value);
        }

        @Override
        public void accept(OperationVisitor<?> visitor) {
            throw new UnsupportedOperationException("Not used in this test");
        }

        @Override
        protected void formatRepresentation(StringBuilder builder) {
            builder.append("fixedNumber");
        }

        @Override
        protected String getOperationToken() {
            return "";
        }
    }
}
