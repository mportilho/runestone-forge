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

package com.runestone.expeval.support.functions.others;

import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

/**
 * Contains a list of date/time functions
 *
 * @author Marcelo Portilho
 */
public class DateTimeFunctions {

    /**
     * Finds the number of seconds between two dates
     *
     * @param t1 first date
     * @param t2 second date
     * @return number of seconds between the two dates
     */
    public static Long secondsBetween(Temporal t1, Temporal t2) {
        return ChronoUnit.SECONDS.between(t1, t2);
    }

    /**
     * Finds the number of minutes between two dates
     *
     * @param t1 first date
     * @param t2 second date
     * @return number of minutes between the two dates
     */
    public static Long minutesBetween(Temporal t1, Temporal t2) {
        return ChronoUnit.MINUTES.between(t1, t2);
    }

    /**
     * Finds the number of hours between two dates
     *
     * @param t1 first date
     * @param t2 second date
     * @return number of hours between the two dates
     */
    public static Long hoursBetween(Temporal t1, Temporal t2) {
        return ChronoUnit.HOURS.between(t1, t2);
    }

    /**
     * Finds the number of days between two dates
     *
     * @param t1 first date
     * @param t2 second date
     * @return number of days between the two dates
     */
    public static Long daysBetween(Temporal t1, Temporal t2) {
        return ChronoUnit.DAYS.between(t1, t2);
    }

    /**
     * Finds the number of months between two dates
     *
     * @param t1 first date
     * @param t2 second date
     * @return number of months between the two dates
     */
    public static Long monthsBetween(Temporal t1, Temporal t2) {
        return ChronoUnit.MONTHS.between(t1, t2);
    }

    /**
     * Finds the number of years between two dates
     *
     * @param t1 first date
     * @param t2 second date
     * @return number of years between the two dates
     */
    public static Long yearsBetween(Temporal t1, Temporal t2) {
        return ChronoUnit.YEARS.between(t1, t2);
    }

}
