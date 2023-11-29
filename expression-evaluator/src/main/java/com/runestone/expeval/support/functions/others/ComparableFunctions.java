package com.runestone.expeval.support.functions.others;

/**
 * Contains a list of functions for Comparable objects
 *
 * @author Marcelo Portilho
 */
public class ComparableFunctions {

    /**
     * Finds the maximum value of a list of values
     *
     * @param p Array of values
     * @return maximum value
     */
    public static <T extends Comparable<? super T>> T max(T[] p) {
        if (p.length == 1) {
            return p[0];
        } else if (p.length == 2) {
            return p[0].compareTo(p[1]) >= 0 ? p[0] : p[1];
        }
        T maxOne = p[0];
        for (int i = 1, parametersLength = p.length; i < parametersLength; i++) {
            T param = p[i];
            if (maxOne.compareTo(param) < 0) {
                maxOne = param;
            }
        }
        return maxOne;
    }

    /**
     * Finds the minimum value of a list of values
     *
     * @param p Array of values
     * @return minimum value
     */
    public static <T extends Comparable<? super T>> T min(T[] p) {
        if (p.length == 1) {
            return p[0];
        } else if (p.length == 2) {
            return p[0].compareTo(p[1]) <= 0 ? p[0] : p[1];
        }
        T minOne = p[0];
        for (int i = 1, parametersLength = p.length; i < parametersLength; i++) {
            T parameter = p[i];
            if (minOne.compareTo(parameter) > 0) {
                minOne = parameter;
            }
        }
        return minOne;
    }

}
