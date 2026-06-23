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

package com.runestone.dynafilter.core.operation;

public record FilterArity(int min, int max) {

    public static final int UNBOUNDED = Integer.MAX_VALUE;

    public FilterArity {
        if (min < 0) {
            throw new IllegalArgumentException("min cannot be negative");
        }
        if (max < min) {
            throw new IllegalArgumentException("max cannot be less than min");
        }
    }

    public static FilterArity any() {
        return new FilterArity(0, UNBOUNDED);
    }

    public static FilterArity exactly(int count) {
        return new FilterArity(count, count);
    }

    public static FilterArity atLeast(int count) {
        return new FilterArity(count, UNBOUNDED);
    }

    public static FilterArity between(int min, int max) {
        return new FilterArity(min, max);
    }

    public boolean accepts(int count) {
        return count >= min && count <= max;
    }

}
