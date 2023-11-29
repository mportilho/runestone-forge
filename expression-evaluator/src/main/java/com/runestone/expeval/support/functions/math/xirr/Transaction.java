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

import com.runestone.utils.DateUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

import static java.time.ZoneOffset.UTC;

/**
 * Represents a transaction for the purposes of computing the irregular r
 * of return.
 * <p>
 * Note that negative amounts represent deposits into the investment (and so
 * withdrawals from your cash).  Positive amounts represent withdrawals from the
 * investment (deposits into cash).  Zero amounts are allowed in case your
 * investment is now worthless.
 *
 * @see Xirr
 */
public class Transaction {

    private final double amount;
    private final Instant when;

    /**
     * Construct a Transaction instance with the given amount at the given day.
     *
     * @param amount the amount transferred
     * @param when   the day the transaction took place
     */
    public Transaction(double amount, Instant when) {
        this.amount = amount;
        this.when = when;
    }

    public Transaction(double amount, LocalDate when) {
        this.amount = amount;
        this.when = when.atStartOfDay().toInstant(UTC);
    }

    /**
     * Construct a Transaction instance with the given amount at the given day.
     *
     * @param amount the amount transferred
     * @param when   the day the transaction took place
     */
    public Transaction(double amount, Date when) {
        this.amount = amount;
        this.when = when.toInstant();
    }

    /**
     * Construct a Transaction instance with the given amount at the given day.
     *
     * @param amount the amount transferred
     * @param when   the day the transaction took place, see
     *               {@link LocalDate#parse(CharSequence) }
     *               for the format
     */
    public Transaction(double amount, String when) {
        this.amount = amount;
        this.when = DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse(when, LocalDate::from).atStartOfDay().toInstant(UTC);
    }

    /**
     * The amount transferred in this transaction.
     *
     * @return amount transferred in this transaction
     */
    public double getAmount() {
        return amount;
    }

    /**
     * The day the transaction took place.
     *
     * @return day the transaction took place
     */
    public Instant getWhen() {
        return when;
    }
}
