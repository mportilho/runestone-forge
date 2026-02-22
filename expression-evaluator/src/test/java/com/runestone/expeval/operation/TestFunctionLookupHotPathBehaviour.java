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

import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.expeval.expression.ExpressionContext;
import com.runestone.expeval.operation.other.FunctionOperation;
import com.runestone.expeval.support.callsite.OperationCallSite;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

class TestFunctionLookupHotPathBehaviour {

    private static final String PRIMARY_FUNCTION_KEY = OperationCallSite.keyName("sum", 8);
    private static final String FALLBACK_FUNCTION_KEY = OperationCallSite.keyName("sum", 1);

    @Test
    void testOperationContextShouldShortCircuitDuplicateLookupsWhenContextsAreTheSame() {
        OperationCallSite fallbackCallSite = fallbackCallSite();
        CountingExpressionContext sharedContext = new CountingExpressionContext(Map.of(
                fallbackCallSite.getKeyName(), fallbackCallSite
        ));
        OperationContext context = createOperationContext(sharedContext, sharedContext);

        OperationCallSite resolved = context.getFunction(PRIMARY_FUNCTION_KEY, FALLBACK_FUNCTION_KEY);

        Assertions.assertThat(resolved).isSameAs(fallbackCallSite);
        Assertions.assertThat(sharedContext.getLookupCount()).isEqualTo(2);
    }

    @Test
    void testFunctionOperationShouldMemoizeResolvedCallSitePerOperationContext() {
        OperationCallSite fallbackCallSite = fallbackCallSite();
        CountingExpressionContext expressionContext = new CountingExpressionContext(Map.of(
                fallbackCallSite.getKeyName(), fallbackCallSite
        ));
        CountingExpressionContext userContext = new CountingExpressionContext(Map.of());
        OperationContext context = createOperationContext(expressionContext, userContext);

        FunctionOperation functionOperation = new FunctionOperation(
                "sum",
                createFixedParameters(),
                false
        );

        for (int i = 0; i < 10; i++) {
            Assertions.assertThat(functionOperation.<BigDecimal>evaluate(context)).isEqualByComparingTo("36");
        }

        Assertions.assertThat(userContext.getLookupCount()).isEqualTo(2);
        Assertions.assertThat(expressionContext.getLookupCount()).isEqualTo(2);
    }

    @Test
    void testFunctionLookupShouldKeepUserContextPrecedence() {
        OperationCallSite userPrimary = fixedArityCallSite("sum", 8, BigDecimal.valueOf(10));
        OperationCallSite expressionPrimary = fixedArityCallSite("sum", 8, BigDecimal.valueOf(20));

        CountingExpressionContext userContext = new CountingExpressionContext(Map.of(
                userPrimary.getKeyName(), userPrimary
        ));
        CountingExpressionContext expressionContext = new CountingExpressionContext(Map.of(
                expressionPrimary.getKeyName(), expressionPrimary
        ));
        OperationContext context = createOperationContext(expressionContext, userContext);

        OperationCallSite resolved = context.getFunction(PRIMARY_FUNCTION_KEY, FALLBACK_FUNCTION_KEY);

        Assertions.assertThat(resolved).isSameAs(userPrimary);
        Assertions.assertThat(userContext.getLookupCount()).isEqualTo(1);
        Assertions.assertThat(expressionContext.getLookupCount()).isZero();
    }

    private static AbstractOperation[] createFixedParameters() {
        return new AbstractOperation[]{
                new FixedValueOperation(BigDecimal.ONE),
                new FixedValueOperation(BigDecimal.TWO),
                new FixedValueOperation(BigDecimal.valueOf(3)),
                new FixedValueOperation(BigDecimal.valueOf(4)),
                new FixedValueOperation(BigDecimal.valueOf(5)),
                new FixedValueOperation(BigDecimal.valueOf(6)),
                new FixedValueOperation(BigDecimal.valueOf(7)),
                new FixedValueOperation(BigDecimal.valueOf(8))
        };
    }

    private static OperationCallSite fallbackCallSite() {
        return new OperationCallSite("sum", MethodType.methodType(BigDecimal.class, Object[].class), parameters -> {
            BigDecimal total = BigDecimal.ZERO;
            for (Object value : (Object[]) parameters[0]) {
                total = total.add((BigDecimal) value);
            }
            return total;
        });
    }

    private static OperationCallSite fixedArityCallSite(String functionName, int paramCount, BigDecimal value) {
        MethodType methodType = MethodType.methodType(BigDecimal.class, createParameterTypes(paramCount));
        return new OperationCallSite(functionName, methodType, parameters -> value);
    }

    private static Class<?>[] createParameterTypes(int paramCount) {
        Class<?>[] paramTypes = new Class<?>[paramCount];
        for (int i = 0; i < paramCount; i++) {
            paramTypes[i] = Object.class;
        }
        return paramTypes;
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

    private static final class CountingExpressionContext extends ExpressionContext {

        private final AtomicInteger lookupCounter = new AtomicInteger();

        private CountingExpressionContext(Map<String, OperationCallSite> functions) {
            super(new HashMap<>(functions));
        }

        @Override
        public OperationCallSite findFunction(String key) {
            lookupCounter.incrementAndGet();
            return super.findFunction(key);
        }

        private int getLookupCount() {
            return lookupCounter.get();
        }
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
            throw new UnsupportedOperationException("Not used in this test");
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
