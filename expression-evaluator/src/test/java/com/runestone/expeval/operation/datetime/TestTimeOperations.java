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

package com.runestone.expeval.operation.datetime;

import com.runestone.expeval.expression.Expression;
import com.runestone.expeval.tools.VerifyExpressionsTools;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestTimeOperations {

    @Test
    public void testAddTime() {
        Expression expSeconds = new Expression("12:00:00 plusSeconds 1 = 12:00:01");
        VerifyExpressionsTools.checkWarmUpCache(expSeconds, 6);
        Assertions.assertThat(expSeconds.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expSeconds.toString()).isEqualTo("12:00 plusSeconds 1 = 12:00:01");
        VerifyExpressionsTools.commonVerifications(expSeconds);
        VerifyExpressionsTools.checkCache(expSeconds, 6);

        Expression expMinutes = new Expression("12:00:20 plusMinutes 1 = 12:01:20");
        VerifyExpressionsTools.checkWarmUpCache(expMinutes, 6);
        Assertions.assertThat(expMinutes.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expMinutes.toString()).isEqualTo("12:00:20 plusMinutes 1 = 12:01:20");
        VerifyExpressionsTools.commonVerifications(expMinutes);
        VerifyExpressionsTools.checkCache(expMinutes, 6);

        Expression expHours = new Expression("(12:00 plusHours 1) = 13:00:00");
        VerifyExpressionsTools.checkWarmUpCache(expHours, 6);
        Assertions.assertThat(expHours.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expHours.toString()).isEqualTo("(12:00 plusHours 1) = 13:00");
        VerifyExpressionsTools.commonVerifications(expHours);
        VerifyExpressionsTools.checkCache(expHours, 6);

        Expression currTime = new Expression("currTime plusHours 1 = currTime plusHours 1 ");
        VerifyExpressionsTools.checkWarmUpCache(currTime, 2);
        Assertions.assertThat(currTime.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(currTime.toString()).isEqualTo("currTime plusHours 1 = currTime plusHours 1");
        VerifyExpressionsTools.commonVerifications(currTime);
        VerifyExpressionsTools.checkCache(currTime, 2);
    }

    @Test
    public void testSubtractTime() {
        Expression expSeconds = new Expression("12:00:01 minusSeconds 1 = 12:00:00");
        VerifyExpressionsTools.checkWarmUpCache(expSeconds, 6);
        Assertions.assertThat(expSeconds.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expSeconds.toString()).isEqualTo("12:00:01 minusSeconds 1 = 12:00");
        VerifyExpressionsTools.commonVerifications(expSeconds);
        VerifyExpressionsTools.checkCache(expSeconds, 6);

        Expression expMinutes = new Expression("12:01:20 minusMinutes 1 = 12:00:20");
        VerifyExpressionsTools.checkWarmUpCache(expMinutes, 6);
        Assertions.assertThat(expMinutes.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expMinutes.toString()).isEqualTo("12:01:20 minusMinutes 1 = 12:00:20");
        VerifyExpressionsTools.commonVerifications(expMinutes);
        VerifyExpressionsTools.checkCache(expMinutes, 6);

        Expression expHours = new Expression("13:00 minusHours 1 = 12:00:00");
        VerifyExpressionsTools.checkWarmUpCache(expHours, 6);
        Assertions.assertThat(expHours.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expHours.toString()).isEqualTo("13:00 minusHours 1 = 12:00");
        VerifyExpressionsTools.commonVerifications(expHours);
        VerifyExpressionsTools.checkCache(expHours, 6);
    }

    @Test
    public void testSetTime() {
        Expression expSeconds = new Expression("12:00:00 setSeconds 3 = 12:00:03");
        VerifyExpressionsTools.checkWarmUpCache(expSeconds, 6);
        Assertions.assertThat(expSeconds.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expSeconds.toString()).isEqualTo("12:00 setSeconds 3 = 12:00:03");
        VerifyExpressionsTools.commonVerifications(expSeconds);
        VerifyExpressionsTools.checkCache(expSeconds, 6);

        Expression expMinutes = new Expression("12:00:00 setMinutes 3 = 12:03:00");
        VerifyExpressionsTools.checkWarmUpCache(expMinutes, 6);
        Assertions.assertThat(expMinutes.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expMinutes.toString()).isEqualTo("12:00 setMinutes 3 = 12:03");
        VerifyExpressionsTools.commonVerifications(expMinutes);
        VerifyExpressionsTools.checkCache(expMinutes, 6);

        Expression expHours = new Expression("12:00 setHours 3 = 03:00:00");
        VerifyExpressionsTools.checkWarmUpCache(expHours, 6);
        Assertions.assertThat(expHours.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expHours.toString()).isEqualTo("12:00 setHours 3 = 03:00");
        VerifyExpressionsTools.commonVerifications(expHours);
        VerifyExpressionsTools.checkCache(expHours, 6);
    }


}
