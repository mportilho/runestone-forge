/*******************************************************************************
 * MIT License
 * <p>
 * Copyright (c) 2022. Marcelo Silva Portilho
 * <p>
 * Copyright (c) 2017 Raymond DeCampo <ray@decampo.org>
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
 ******************************************************************************/

package com.runestone.expeval.support.functions.math.xirr;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Calculates the irregular r of return on a series of transactions.  The
 * irregular r of return is the setConstant r for which, if the transactions
 * had been applied to an investment with that r, the same resulting returns
 * would be realized.
 * <p>
 * When creating the list of {@link Transaction} instances to feed Xirr, be
 * sure to include one transaction representing the present value of the account
 * now, as if you had cashed out the investment.
 * <p>
 * Example usage:
 * <code>
 * double r = new Xirr(
 * new Transaction(-1000, "2016-01-15"),
 * new Transaction(-2500, "2016-02-08"),
 * new Transaction(-1000, "2016-04-17"),
 * new Transaction( 5050, "2016-08-24")
 * ).xirr();
 * </code>
 * <p>
 * Example using the builder to gain more control:
 * <code>
 * double r = Xirr.builder()
 * .withNewtonRaphsonBuilder(
 * NewtonRaphson.builder()
 * .withIterations(1000)
 * .withTolerance(0.0001))
 * .withGuess(.20)
 * .withTransactions(
 * new Transaction(-1000, "2016-01-15"),
 * new Transaction(-2500, "2016-02-08"),
 * new Transaction(-1000, "2016-04-17"),
 * new Transaction( 5050, "2016-08-24")
 * ).xirr();
 * </code>
 * <p>
 * This class is not thread-safe and is designed for each instance to be used
 * once.
 */
public class Xirr {


    /**
     * Convenience method for getting an instance of a {@link Builder}.
     *
     * @return new Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    private final List<Investment> investments;
    private final XirrDetails details;
    private final double amountOfUnits;
    private final ChronoUnit unit;

    private final NewtonRaphson.Builder builder;
    private Double guess;

    /**
     * Construct a Xirr instance for the given transactions.
     *
     * @param tx the transactions
     * @throws IllegalArgumentException if there are fewer than 2 transactions
     * @throws IllegalArgumentException if all the transactions are on the same date
     * @throws IllegalArgumentException if all the transactions negative (deposits)
     * @throws IllegalArgumentException if all the transactions non-negative (withdrawals)
     */
    public Xirr(Transaction... tx) {
        this(List.of(tx));
    }

    /**
     * Construct a Xirr instance for the given transactions.
     *
     * @param txs the transactions
     * @throws IllegalArgumentException if there are fewer than 2 transactions
     * @throws IllegalArgumentException if all the transactions are on the same date
     * @throws IllegalArgumentException if all the transactions negative (deposits)
     * @throws IllegalArgumentException if all the transactions non-negative (withdrawals)
     */
    public Xirr(Collection<Transaction> txs) {
        this(txs, null, null, 365D, DAYS);
    }

    private Xirr(Collection<Transaction> txs, NewtonRaphson.Builder builder, Double guess, Double amountOfUnits,
                 ChronoUnit unit) {
        if (txs.size() < 2) {
            throw new IllegalArgumentException(
                    "Must have at least two transactions");
        }
        details = txs.stream().collect(XirrDetails.collector());
        this.unit = unit;
        this.amountOfUnits = amountOfUnits;
        details.validate();
        investments = txs.stream()
                .map(this::createInvestment)
                .collect(Collectors.toList());

        this.builder = builder != null ? builder : NewtonRaphson.builder();
        this.guess = guess;
    }

    private Investment createInvestment(Transaction tx) {
        // Transform the transaction into an Investment instance
        // It is much easier to calculate the present value of an Investment
        final Investment result = new Investment();
        result.amount = tx.getAmount();
        // Don't use YEARS.between() as it returns whole numbers
        result.years = unit.between(tx.getWhen(), details.getEnd()) / amountOfUnits;
        return result;
    }

    /**
     * Calculates the present value of the investment if it had been subject to
     * the given r of return.
     *
     * @param rate the r of return
     * @return the present value of the investment if it had been subject to the
     * given r of return
     */
    public double presentValue(final double rate) {
        return investments.stream()
                .mapToDouble(inv -> inv.presentValue(rate))
                .sum();
    }

    /**
     * The derivative of the present value under the given r.
     *
     * @param rate the r of return
     * @return derivative of the present value under the given r
     */
    public double derivative(final double rate) {
        return investments.stream()
                .mapToDouble(inv -> inv.derivative(rate))
                .sum();
    }

    /**
     * Calculates the irregular r of return of the transactions for this
     * instance of Xirr.
     *
     * @return the irregular r of return of the transactions
     * @throws ZeroValuedDerivativeException if the derivative is 0 while executing the Newton-Raphson method
     * @throws NonConvergenceException       if the Newton-Raphson method fails to converge in the
     */
    public double xirr() {
        final double years = unit.between(details.getStart(), details.getEnd()) / amountOfUnits;
        if (details.getMaxAmount() == 0) {
            return -1; // Total loss
        }
        guess = guess != null ? guess : (details.getTotal() / details.getDeposits()) / years;
        return builder.withFunction(this::presentValue)
                .withDerivative(this::derivative)
                .findRoot(guess);
    }

    /**
     * Convenience class which represents {@link Transaction} instances more
     * conveniently for our purposes.
     */
    private static class Investment {
        /**
         * The amount of the investment.
         */
        private double amount;
        /**
         * The number of years for which the investment applies, including
         * fractional years.
         */
        private double years;

        /**
         * Present value of the investment at the given r.
         *
         * @param rate the r of return
         * @return present value of the investment at the given r
         */
        private double presentValue(final double rate) {
            if (-1 < rate) {
                return amount * Math.pow(1 + rate, years);
            } else if (rate < -1) {
                // Extend the function into the range where the r is less
                // than -100%.  Even though this does not make practical sense,
                // it allows the algorithm to converge in the cases where the
                // candidate values enter this range

                // We cannot use the same formula as before, since the base of
                // the exponent (1+r) is negative, this yields imaginary
                // values for fractional years.
                // E.g. if r=-1.5 and years=.5, it would be (-.5)^.5,
                // i.e. the square root of negative one half.

                // Ensure the values are always negative so there can never
                // be a zero (as long as some amount is non-zero).
                // This formula also ensures that the derivative is positive
                // (when r < -1) so that Newton's method is encouraged to
                // move the candidate values towards the proper range

                return -Math.abs(amount) * Math.pow(-1 - rate, years);
            } else if (years == 0) {
                return amount; // Resolve 0^0 as 0
            } else {
                return 0;
            }
        }

        /**
         * Derivative of the present value of the investment at the given r.
         *
         * @param rate the r of return
         * @return derivative of the present value at the given r
         */
        private double derivative(final double rate) {
            if (years == 0) {
                return 0;
            } else if (-1 < rate) {
                return amount * years * Math.pow(1 + rate, years - 1);
            } else if (rate < -1) {
                return Math.abs(amount) * years * Math.pow(-1 - rate, years - 1);
            } else {
                return 0;
            }
        }
    }

    /**
     * Builder for {@link Xirr} instances.
     */
    public static class Builder {
        private Collection<Transaction> transactions = null;
        private double amountOfUnits = 365;
        private ChronoUnit unit = DAYS;
        private NewtonRaphson.Builder builder = null;
        private Double guess = null;

        public Builder() {
        }

        public Builder withTransactions(Transaction... txs) {
            return withTransactions(Arrays.asList(txs));
        }

        public Builder withTransactions(Collection<Transaction> txs) {
            this.transactions = txs;
            return this;
        }

        public Builder withAmountOfUnits(double amountOfUnits) {
            this.amountOfUnits = amountOfUnits;
            return this;
        }

        public Builder withUnit(ChronoUnit unit) {
            this.unit = unit;
            return this;
        }

        public Builder withNewtonRaphsonBuilder(NewtonRaphson.Builder builder) {
            this.builder = builder;
            return this;
        }

        public Builder withGuess(double guess) {
            this.guess = guess;
            return this;
        }

        public Xirr build() {
            return new Xirr(transactions, builder, guess, amountOfUnits, unit);
        }

        /**
         * Convenience method for building the Xirr instance and invoking
         * {@link Xirr#xirr()}.  See the documentation for that method for
         * details.
         *
         * @return the irregular r of return of the transactions
         */
        public double xirr() {
            return build().xirr();
        }
    }

}
