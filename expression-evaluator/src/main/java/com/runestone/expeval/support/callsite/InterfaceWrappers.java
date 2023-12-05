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

/**
 * Contains interfaces for wrapping functions with a specific number of parameters.
 *
 * @author Marcelo Portilho
 */
class InterfaceWrappers {

    public interface Function1 {
        Object call(Object p1);
    }

    public interface Function2 {
        Object call(Object p1, Object p2);
    }

    public interface Function3 {
        Object call(Object p1, Object p2, Object p3);
    }

    public interface Function4 {
        Object call(Object p1, Object p2, Object p3, Object p4);
    }

    public interface Function5 {
        Object call(Object p1, Object p2, Object p3, Object p4, Object p5);
    }

    public interface Function6 {
        Object call(Object p1, Object p2, Object p3, Object p4, Object p5, Object p6);
    }

    public interface Function7 {
        Object call(Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7);
    }

    public interface Function8 {
        Object call(Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8);
    }

    public interface Function9 {
        Object call(Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9);
    }

    public interface Function10 {
        Object call(
                Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9,
                Object p10);
    }

    public interface Function11 {
        Object call(
                Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9,
                Object p10, Object p11);
    }

}
