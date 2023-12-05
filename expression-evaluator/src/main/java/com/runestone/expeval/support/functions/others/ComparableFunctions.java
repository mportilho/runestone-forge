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
