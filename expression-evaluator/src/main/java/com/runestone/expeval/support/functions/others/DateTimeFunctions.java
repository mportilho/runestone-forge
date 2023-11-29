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
