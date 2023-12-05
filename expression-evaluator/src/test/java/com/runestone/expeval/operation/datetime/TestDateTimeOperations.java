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

public class TestDateTimeOperations {

    @Test
    public void testAddDateTime() {
        Expression expSeconds = new Expression("2000-01-01T12:00:00 plusSeconds 1 = 2000-01-01T12:00:01");
        Assertions.assertThat(expSeconds.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expSeconds.toString()).isEqualTo("2000-01-01T12:00:00 plusSeconds 1 = 2000-01-01T12:00:01");
        VerifyExpressionsTools.commonVerifications(expSeconds);
        VerifyExpressionsTools.checkCache(expSeconds, 6);

        Expression expMinutes = new Expression("2000-01-01T12:00:20 plusMinutes 1 = 2000-01-01T12:01:20");
        Assertions.assertThat(expMinutes.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expMinutes.toString()).isEqualTo("2000-01-01T12:00:20 plusMinutes 1 = 2000-01-01T12:01:20");
        VerifyExpressionsTools.commonVerifications(expMinutes);
        VerifyExpressionsTools.checkCache(expMinutes, 6);

        Expression expHours = new Expression("2000-01-01T12:00:00 plusHours 1 = 2000-01-01T13:00:00");
        Assertions.assertThat(expHours.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expHours.toString()).isEqualTo("2000-01-01T12:00:00 plusHours 1 = 2000-01-01T13:00:00");
        VerifyExpressionsTools.commonVerifications(expHours);
        VerifyExpressionsTools.checkCache(expHours, 6);

        Expression expDays = new Expression("2000-01-01T12:00:00 plusDays 1 = 2000-01-02T12:00:00");
        Assertions.assertThat(expDays.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expDays.toString()).isEqualTo("2000-01-01T12:00:00 plusDays 1 = 2000-01-02T12:00:00");
        VerifyExpressionsTools.commonVerifications(expDays);
        VerifyExpressionsTools.checkCache(expDays, 6);

        Expression expMonths = new Expression("2000-01-01T12:00:00 plusMonths 1 = 2000-02-01T12:00:00");
        Assertions.assertThat(expMonths.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expMonths.toString()).isEqualTo("2000-01-01T12:00:00 plusMonths 1 = 2000-02-01T12:00:00");
        VerifyExpressionsTools.commonVerifications(expMonths);
        VerifyExpressionsTools.checkCache(expMonths, 6);

        Expression expYears = new Expression("(2000-01-01T12:00:00 plusYears 1) = 2001-01-01T12:00:00");
        Assertions.assertThat(expYears.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expYears.toString()).isEqualTo("(2000-01-01T12:00:00 plusYears 1) = 2001-01-01T12:00:00");
        VerifyExpressionsTools.commonVerifications(expYears);
        VerifyExpressionsTools.checkCache(expYears, 6);

        Expression currDateTime = new Expression("currDateTime plusYears 1 = currDateTime plusYears 1 ");
        Assertions.assertThat(currDateTime.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(currDateTime.toString()).isEqualTo("currDateTime plusYears 1 = currDateTime plusYears 1");
        VerifyExpressionsTools.commonVerifications(currDateTime);
        VerifyExpressionsTools.checkCache(currDateTime, 2);
    }

    @Test
    public void testSubtractDateTime() {
        Expression expSeconds = new Expression("2000-01-01T12:00:01 minusSeconds 1 = 2000-01-01T12:00:00");
        Assertions.assertThat(expSeconds.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expSeconds.toString()).isEqualTo("2000-01-01T12:00:01 minusSeconds 1 = 2000-01-01T12:00:00");
        VerifyExpressionsTools.commonVerifications(expSeconds);
        VerifyExpressionsTools.checkCache(expSeconds, 6);

        Expression expMinutes = new Expression("2000-01-01T12:01:20 minusMinutes 1 = 2000-01-01T12:00:20");
        Assertions.assertThat(expMinutes.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expMinutes.toString()).isEqualTo("2000-01-01T12:01:20 minusMinutes 1 = 2000-01-01T12:00:20");
        VerifyExpressionsTools.commonVerifications(expMinutes);
        VerifyExpressionsTools.checkCache(expMinutes, 6);

        Expression expHours = new Expression("2000-01-01T13:00:00 minusHours 1 = 2000-01-01T12:00:00");
        Assertions.assertThat(expHours.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expHours.toString()).isEqualTo("2000-01-01T13:00:00 minusHours 1 = 2000-01-01T12:00:00");
        VerifyExpressionsTools.commonVerifications(expHours);
        VerifyExpressionsTools.checkCache(expHours, 6);

        Expression expDays = new Expression("2000-01-02T12:00:00 minusDays 1 = 2000-01-01T12:00:00");
        Assertions.assertThat(expDays.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expDays.toString()).isEqualTo("2000-01-02T12:00:00 minusDays 1 = 2000-01-01T12:00:00");
        VerifyExpressionsTools.commonVerifications(expDays);
        VerifyExpressionsTools.checkCache(expDays, 6);

        Expression expMonths = new Expression("2000-02-01T12:00:00 minusMonths 1 = 2000-01-01T12:00:00");
        Assertions.assertThat(expMonths.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expMonths.toString()).isEqualTo("2000-02-01T12:00:00 minusMonths 1 = 2000-01-01T12:00:00");
        VerifyExpressionsTools.commonVerifications(expMonths);
        VerifyExpressionsTools.checkCache(expMonths, 6);

        Expression expYears = new Expression("2001-01-01T12:00:00 minusYears 1 = 2000-01-01T12:00:00");
        Assertions.assertThat(expYears.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expYears.toString()).isEqualTo("2001-01-01T12:00:00 minusYears 1 = 2000-01-01T12:00:00");
        VerifyExpressionsTools.commonVerifications(expYears);
        VerifyExpressionsTools.checkCache(expYears, 6);
    }

    @Test
    public void testSetDateTime() {
        Expression expSeconds = new Expression("2000-01-01T12:00:00 setSeconds 3 = 2000-01-01T12:00:03");
        VerifyExpressionsTools.checkWarmUpCache(expSeconds, 6);
        Assertions.assertThat(expSeconds.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expSeconds.toString()).isEqualTo("2000-01-01T12:00:00 setSeconds 3 = 2000-01-01T12:00:03");
        VerifyExpressionsTools.commonVerifications(expSeconds);
        VerifyExpressionsTools.checkCache(expSeconds, 6);

        Expression expMinutes = new Expression("2000-01-01T12:00:00 setMinutes 3 = 2000-01-01T12:03:00");
        VerifyExpressionsTools.checkWarmUpCache(expMinutes, 6);
        Assertions.assertThat(expMinutes.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expMinutes.toString()).isEqualTo("2000-01-01T12:00:00 setMinutes 3 = 2000-01-01T12:03:00");
        VerifyExpressionsTools.commonVerifications(expMinutes);
        VerifyExpressionsTools.checkCache(expMinutes, 6);

        Expression expHours = new Expression("2000-01-01T12:00:00 setHours 3 = 2000-01-01T03:00:00");
        VerifyExpressionsTools.checkWarmUpCache(expHours, 6);
        Assertions.assertThat(expHours.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expHours.toString()).isEqualTo("2000-01-01T12:00:00 setHours 3 = 2000-01-01T03:00:00");
        VerifyExpressionsTools.commonVerifications(expHours);
        VerifyExpressionsTools.checkCache(expHours, 6);

        Expression expDays = new Expression("2000-01-01T12:00:00 setDays 3 = 2000-01-03T12:00:00");
        VerifyExpressionsTools.checkWarmUpCache(expDays, 6);
        Assertions.assertThat(expDays.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expDays.toString()).isEqualTo("2000-01-01T12:00:00 setDays 3 = 2000-01-03T12:00:00");
        VerifyExpressionsTools.commonVerifications(expDays);
        VerifyExpressionsTools.checkCache(expDays, 6);

        Expression expMonths = new Expression("2000-01-01T12:00:00 setMonths 3 = 2000-03-01T12:00:00");
        VerifyExpressionsTools.checkWarmUpCache(expMonths, 6);
        Assertions.assertThat(expMonths.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expMonths.toString()).isEqualTo("2000-01-01T12:00:00 setMonths 3 = 2000-03-01T12:00:00");
        VerifyExpressionsTools.commonVerifications(expMonths);
        VerifyExpressionsTools.checkCache(expMonths, 6);

        Expression expYears = new Expression("2000-01-01T12:00:00 setYears 2003 = 2003-01-01T12:00:00");
        VerifyExpressionsTools.checkWarmUpCache(expYears, 6);
        Assertions.assertThat(expYears.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expYears.toString()).isEqualTo("2000-01-01T12:00:00 setYears 2003 = 2003-01-01T12:00:00");
        VerifyExpressionsTools.commonVerifications(expYears);
        VerifyExpressionsTools.checkCache(expYears, 6);
    }

}
