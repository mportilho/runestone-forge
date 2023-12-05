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

public class TestDateOperations {

    @Test
    public void testAddDate() {
        Expression expDays = new Expression("2000-01-01 plusDays 1 = 2000-01-02");
        VerifyExpressionsTools.checkWarmUpCache(expDays, 6);
        Assertions.assertThat(expDays.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expDays.toString()).isEqualTo("2000-01-01 plusDays 1 = 2000-01-02");
        VerifyExpressionsTools.commonVerifications(expDays);
        VerifyExpressionsTools.checkCache(expDays, 6);

        Expression expMonths = new Expression("2000-01-01 plusMonths 1 = 2000-02-01");
        VerifyExpressionsTools.checkWarmUpCache(expMonths, 6);
        Assertions.assertThat(expMonths.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expMonths.toString()).isEqualTo("2000-01-01 plusMonths 1 = 2000-02-01");
        VerifyExpressionsTools.commonVerifications(expMonths);
        VerifyExpressionsTools.checkCache(expMonths, 6);

        Expression expYears = new Expression("(2000-01-01 plusYears 1) = 2001-01-01");
        VerifyExpressionsTools.checkWarmUpCache(expYears, 6);
        Assertions.assertThat(expYears.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expYears.toString()).isEqualTo("(2000-01-01 plusYears 1) = 2001-01-01");
        VerifyExpressionsTools.commonVerifications(expYears);
        VerifyExpressionsTools.checkCache(expYears, 6);

        Expression currDate = new Expression("currDate plusYears 1 = currDate plusYears 1 ");
        VerifyExpressionsTools.checkWarmUpCache(currDate, 2);
        Assertions.assertThat(currDate.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(currDate.toString()).isEqualTo("currDate plusYears 1 = currDate plusYears 1");
        VerifyExpressionsTools.commonVerifications(currDate);
        VerifyExpressionsTools.checkCache(currDate, 2);
    }

    @Test
    public void testSubtractDate() {
        Expression expDays = new Expression("2000-01-02 minusDays 1 = 2000-01-01");
        VerifyExpressionsTools.checkWarmUpCache(expDays, 6);
        Assertions.assertThat(expDays.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expDays.toString()).isEqualTo("2000-01-02 minusDays 1 = 2000-01-01");
        VerifyExpressionsTools.commonVerifications(expDays);
        VerifyExpressionsTools.checkCache(expDays, 6);

        Expression expMonths = new Expression("2000-02-01 minusMonths 1 = 2000-01-01");
        VerifyExpressionsTools.checkWarmUpCache(expMonths, 6);
        Assertions.assertThat(expMonths.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expMonths.toString()).isEqualTo("2000-02-01 minusMonths 1 = 2000-01-01");
        VerifyExpressionsTools.commonVerifications(expMonths);
        VerifyExpressionsTools.checkCache(expMonths, 6);

        Expression expYears = new Expression("2001-01-01 minusYears 1 = 2000-01-01");
        VerifyExpressionsTools.checkWarmUpCache(expYears, 6);
        Assertions.assertThat(expYears.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expYears.toString()).isEqualTo("2001-01-01 minusYears 1 = 2000-01-01");
        VerifyExpressionsTools.commonVerifications(expYears);
        VerifyExpressionsTools.checkCache(expYears, 6);
    }

    @Test
    public void testSetDate() {
        Expression expDays = new Expression("2000-01-01 setDays 3 = 2000-01-03");
        VerifyExpressionsTools.checkWarmUpCache(expDays, 6);
        Assertions.assertThat(expDays.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expDays.toString()).isEqualTo("2000-01-01 setDays 3 = 2000-01-03");
        VerifyExpressionsTools.commonVerifications(expDays);
        VerifyExpressionsTools.checkCache(expDays, 6);

        Expression expMonths = new Expression("2000-01-01 setMonths 3 = 2000-03-01");
        VerifyExpressionsTools.checkWarmUpCache(expMonths, 6);
        Assertions.assertThat(expMonths.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expMonths.toString()).isEqualTo("2000-01-01 setMonths 3 = 2000-03-01");
        VerifyExpressionsTools.commonVerifications(expMonths);
        VerifyExpressionsTools.checkCache(expMonths, 6);

        Expression expYears = new Expression("2000-01-01 setYears 2003 = 2003-01-01");
        VerifyExpressionsTools.checkWarmUpCache(expYears, 6);
        Assertions.assertThat(expYears.<Boolean>evaluate()).isTrue();
        Assertions.assertThat(expYears.toString()).isEqualTo("2000-01-01 setYears 2003 = 2003-01-01");
        VerifyExpressionsTools.commonVerifications(expYears);
        VerifyExpressionsTools.checkCache(expYears, 6);
    }

}
