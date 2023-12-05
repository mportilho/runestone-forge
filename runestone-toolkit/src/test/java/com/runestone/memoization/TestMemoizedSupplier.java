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

package com.runestone.memoization;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.concurrent.atomic.AtomicInteger;

@Execution(ExecutionMode.CONCURRENT)
public class TestMemoizedSupplier {

    @Test
    public void testMemoizedSupplier() {
        final AtomicInteger counter = new AtomicInteger(0);
        MemoizedSupplier<String> supplier = new MemoizedSupplier<>(() -> {
            counter.incrementAndGet();
            return "Hello World";
        });
        Assertions.assertThat(supplier.get()).isEqualTo("Hello World");
        Assertions.assertThat(supplier.get()).isEqualTo("Hello World");
        Assertions.assertThat(supplier.get()).isEqualTo("Hello World"); // should be cached, checking multiple times to be sure
        Assertions.assertThat(counter.get()).isEqualTo(1); // should have been called only once
    }

    @Test
    public void testMemoizedSupplierWithError() {
        final AtomicInteger counter = new AtomicInteger(0);
        MemoizedSupplier<String> supplier = new MemoizedSupplier<>(() -> {
            counter.incrementAndGet();
            if (counter.get() < 4) {
                throw new RuntimeException("Error");
            }
            return "Hello World";
        });
        Assertions.assertThatThrownBy(supplier::get).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThatThrownBy(supplier::get).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(2);
        Assertions.assertThatThrownBy(supplier::get).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(3);
        Assertions.assertThat(supplier.get()).isEqualTo("Hello World");
        Assertions.assertThat(counter.get()).isEqualTo(4);
        Assertions.assertThat(supplier.get()).isEqualTo("Hello World");
        Assertions.assertThat(counter.get()).isEqualTo(4);
    }

    @Test
    public void testLongRunningSupplier() {
        final AtomicInteger counter = new AtomicInteger(0);
        MemoizedSupplier<String> supplier = new MemoizedSupplier<>(() -> {
            counter.incrementAndGet();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Hello World";
        });

        Thread.ofVirtual().start(supplier::get);
        Thread.ofVirtual().start(supplier::get);
        Thread.ofVirtual().start(supplier::get);

        Assertions.assertThat(supplier.get()).isEqualTo("Hello World");
        Assertions.assertThat(supplier.get()).isEqualTo("Hello World");
        Assertions.assertThat(supplier.get()).isEqualTo("Hello World");
        Assertions.assertThat(supplier.get()).isEqualTo("Hello World"); // should be cached, checking multiple times to be sure
        Assertions.assertThat(counter.get()).isEqualTo(1); // should have been called only once
    }

}
