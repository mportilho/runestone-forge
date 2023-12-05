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

package com.runestone.expeval.support.callsite;

import com.runestone.converters.DataConversionService;
import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.expeval.support.functions.others.ComparableFunctions;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TestOperationCallSite {

    private static final DataConversionService cs = new DefaultDataConversionService();
    private static final CallSiteContext context = new CallSiteContext(MathContext.DECIMAL64, null, ZoneId.systemDefault(), ZonedDateTime::now);

    @Test
    public void testConversionsWithPrimitiveTypes() {
        MethodType methodType = MethodType.methodType(int.class, int.class, int.class);
        OperationCallSite callSite = new OperationCallSite("adder", methodType, p -> (long) ((int) p[0]) + ((int) p[1]));
        Assertions.assertThat(callSite.<Integer>call(context, new Object[]{BigDecimal.ONE, 2}, cs::convert)).isEqualTo(3);
    }

    @Test
    public void testConversionsWithIntegerArray() {
        MethodType methodType = MethodType.methodType(int.class, Integer[].class);
        OperationCallSite callSite = new OperationCallSite("adder", methodType, p -> ((Integer[]) p[0])[0] + ((Integer[]) p[0])[1]);
        Assertions.assertThat(callSite.<Integer>call(context, new Object[]{1, 2}, cs::convert)).isEqualTo(3);
    }

    @Test
    public void testConversionsWithBigDecimalArray() {
        MethodType methodType = MethodType.methodType(int.class, BigDecimal[].class);
        OperationCallSite callSite = new OperationCallSite("adder", methodType, p -> ((BigDecimal[]) p[0])[0].intValue() + ((BigDecimal[]) p[0])[1].intValue());
        Assertions.assertThat(callSite.<Integer>call(context, new Object[]{1, 2}, cs::convert)).isEqualTo(3);
    }

    @Test
    public void testConversionsWithPrimitiveArray() {
        MethodType methodType = MethodType.methodType(int.class, int[].class);
        OperationCallSite callSite = new OperationCallSite("adder", methodType, p -> ((int[]) p[0])[0] + ((int[]) p[0])[1]);
        Assertions.assertThat(callSite.<Integer>call(context, new Object[]{1, 2}, cs::convert)).isEqualTo(3);
    }

    @Test
    public void testConversionWithPrimitiveMultiParameters() {
        MethodType methodType = MethodType.methodType(int.class, float[].class, float.class, float.class);
        OperationCallSite callSite = new OperationCallSite("adder", methodType, p -> ((float[]) p[0])[0] + ((float[]) p[0])[1] + (float) p[1] + (float) p[2]);
        Assertions.assertThat(callSite.<Integer>call(context, new Object[]{new float[]{1, 2}, 3, 4}, cs::convert)).isEqualTo(10);
    }

    @Test
    public void testConversionWithBigDecimalMultiParameters() {
        MethodType methodType = MethodType.methodType(BigDecimal.class, BigDecimal[].class, BigDecimal.class, BigDecimal.class);
        OperationCallSite callSite = new OperationCallSite("adder", methodType, p -> ((BigDecimal[]) p[0])[0].add(((BigDecimal[]) p[0])[1]).add((BigDecimal) p[1]).add((BigDecimal) p[2]));
        Assertions.assertThat(callSite.<BigDecimal>call(context, new Object[]{new float[]{1, 2}, 3, 4}, cs::convert)).isEqualByComparingTo("10");
    }

    @Test
    public void testConversionWithVargs() {
        OperationCallSite callSite = OperationCallSiteFactory.createLambdaCallSites(ComparableFunctions.class, "max").values().stream().findFirst().orElseThrow();
        Assertions.assertThat(callSite.<Float>call(context, new Object[]{new float[]{1, 2, 3, 4}}, cs::convert)).isEqualTo(4);
    }

}
