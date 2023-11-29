/*******************************************************************************
 * MIT License
 *
 * Copyright (c) 2022. Marcelo Silva Portilho
 *
 * Copyright (c) 2017 Raymond DeCampo <ray@decampo.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/

package com.runestone.expeval.support.functions.math.xirr;

/**
 * Indicates the algorithm failed to converge in the allotted number of
 * iterations.
 *
 * @author ray
 */
public class NonConvergenceException extends IllegalArgumentException {

    private final double initialGuess;
    private final long iterations;

    public NonConvergenceException(double guess, long iterations) {
        super("Newton-Raphson failed to converge within " + iterations
                + " iterations.");
        this.initialGuess = guess;
        this.iterations = iterations;
    }

    /**
     * Get the initial guess used for the algorithm.
     *
     * @return the initial guess used for the algorithm
     */
    public double getInitialGuess() {
        return initialGuess;
    }

    /**
     * Get the number of iterations applied.
     *
     * @return the number of iterations applied.
     */
    public long getIterations() {
        return iterations;
    }

}
