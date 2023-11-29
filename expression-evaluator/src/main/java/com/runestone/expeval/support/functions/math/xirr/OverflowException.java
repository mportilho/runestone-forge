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
 * Indicates that the algorithm failed to converge due to one of the values
 * (either the candidate value, the function value or derivative value) being
 * an invalid double (NaN, Infinity or -Infinity) or other condition leading to
 * an overflow.
 */
public class OverflowException extends ArithmeticException {

    private final NewtonRaphson.Calculation state;

    OverflowException(String message, NewtonRaphson.Calculation state) {
        super(message);
        this.state = state;
    }

    /**
     * Get the initial guess used by the algorithm.
     *
     * @return the initial guess
     */
    public double getInitialGuess() {
        return state.getGuess();
    }

    /**
     * Get the number of iterations passed when encountering the overflow.
     *
     * @return the number of iterations passed when encountering the overflow
     * condition
     */
    public long getIteration() {
        return state.getIteration();
    }

    /**
     * Get the candidate value when the overflow condition occurred.
     *
     * @return the candidate value when the overflow condition occurred
     */
    public double getCandidate() {
        return state.getCandidate();
    }

    /**
     * Get the function value when the overflow condition occurred.
     *
     * @return the function value when the overflow condition occurred
     */
    public double getValue() {
        return state.getValue();
    }

    /**
     * Get the derivative value when the overflow condition occurred.  A null
     * value indicates the derivative was not yet calculated.
     *
     * @return the derivative value when the overflow condition occurred
     */
    public Double getDerivativeValue() {
        return state.getDerivativeValue();
    }

    @Override
    public String toString() {
        return super.toString() + ": " + state;
    }

}
