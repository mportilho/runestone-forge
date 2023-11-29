package com.runestone.assertions;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * A collection of assertions that can be used to validate input parameters
 *
 * @author Marcelo Portilho
 */
public class Asserts {

    private Asserts() {
    }

    /**
     * Asserts that the given value is not null and is a positive number
     *
     * @param number value to be validated
     * @return true if the value is not null and is a positive number
     */
    public static boolean isPositive(BigDecimal number) {
        return number != null && number.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Asserts that the given value is not null and is a positive number or zero
     *
     * @param number value to be validated
     * @return true if the value is not null and is a positive number or zero
     */
    public static boolean isPositiveOrZero(BigDecimal number) {
        return number != null && number.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * Asserts that the given value is a positive number or null
     *
     * @param number value to be validated
     * @return true if the value is a positive number or null
     */
    public static boolean isPositiveOrNull(BigDecimal number) {
        return number == null || number.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Asserts that the given value is a positive number or zero or null
     *
     * @param number value to be validated
     * @return true if the value is a positive number or zero or null
     */
    public static boolean isPositiveOrZeroOrNull(BigDecimal number) {
        return number == null || number.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * Asserts that the given value is not null and is a negative number
     *
     * @param number value to be validated
     * @return true if the value is not null and is a negative number
     */
    public static boolean isNegative(BigDecimal number) {
        return number != null && number.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Asserts that the given value is not null and is a negative number or zero
     *
     * @param number value to be validated
     * @return true if the value is not null and is a negative number or zero
     */
    public static boolean isNegativeOrZero(BigDecimal number) {
        return number != null && number.compareTo(BigDecimal.ZERO) <= 0;
    }

    /**
     * Asserts that the given value is a negative number or null or zero
     *
     * @param number value to be validated
     * @return true if the value is a negative number or null or zero
     */
    public static boolean isNegativeOrZeroOrNull(BigDecimal number) {
        return number == null || number.compareTo(BigDecimal.ZERO) <= 0;
    }

    /**
     * Asserts that the given value is a negative number or null
     *
     * @param number value to be validated
     * @return true if the value is a negative number or null
     */
    public static boolean isNegativeOrNull(BigDecimal number) {
        return number == null || number.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Asserts that the given value is not null and is a zero
     *
     * @param number value to be validated
     * @return true if the value is not null and is a zero
     */
    public static boolean isZero(BigDecimal number) {
        return number != null && number.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Asserts that the given value is a zero or null
     *
     * @param number value to be validated
     * @return true if the value is a zero or null
     */
    public static boolean isZeroOrNull(BigDecimal number) {
        return number == null || number.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Compares two values and return true if they are both null or non null
     *
     * @param first  the first value
     * @param second the second value
     * @return true if they are both null or non null
     */
    public static boolean isBothNullOrNonNUll(Object first, Object second) {
        return (first == null && second == null) || (first != null && second != null);
    }

    /**
     * Asserts a array of objects and returns true if only one of them is not null
     *
     * @param array array of objects
     * @return true if only one of them is not null
     */
    public static boolean isOnlyOneNonNull(Object... array) {
        if (array == null || array.length == 0) {
            return false;
        }
        boolean foundOne = false;
        for (Object o : array) {
            if (o != null && foundOne) {
                return false;
            } else if (o != null) {
                foundOne = true;
            }
        }
        return foundOne;
    }

    /**
     * Checks if a number is between two other numbers
     *
     * @param value the value to be checked
     * @param lower the lower limit
     * @param upper the upper limit
     * @param <T>   the type of the numbers
     * @return true if the value is between the lower and upper limits
     */
    public static <T extends Number & Comparable<? super T>> boolean isBetween(T value, T lower, T upper) {
        if (value != null && lower != null && upper != null) {
            return (value.compareTo(lower) >= 0 && value.compareTo(upper) <= 0) || (value.compareTo(upper) >= 0 && value.compareTo(lower) <= 0);
        }
        return false;
    }

    /**
     * Asserts that the given array is null or empty
     *
     * @param array array to be validated
     * @return true if the array is null or empty
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Asserts that the given collection is null or empty
     *
     * @param collection collection to be validated
     * @return true if the collection is null or empty
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Asserts that the given array is not null and not empty
     *
     * @param collection collection to be validated
     * @return true if the collection is not null and not empty
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * Asserts that the given String is null or empty. String with only spaces are considered not empty
     *
     * @param str String to be validated
     * @return true if the String is null or empty
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Asserts that the given String is not null and not blank. String with only spaces are considered blank
     *
     * @param str String to be validated
     * @return true if the String is not null and not blank
     */
    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

}
