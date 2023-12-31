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

import java.util.concurrent.atomic.AtomicInteger;

public class TestMemoizedFunction {

    @Test
    public void testMemoizedFunction() {
        AtomicInteger counter = new AtomicInteger(0);
        MemoizedFunction<String, Integer> function = new MemoizedFunction<>(s -> {
            counter.incrementAndGet();
            return Integer.parseInt(s);
        });
        Assertions.assertThat(counter.get()).isEqualTo(0);
        Assertions.assertThat(function.apply("123")).isEqualTo(123);
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThat(function.apply("123")).isEqualTo(123);
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThat(function.apply("456")).isEqualTo(456);
        Assertions.assertThat(counter.get()).isEqualTo(2);
        Assertions.assertThat(function.apply("456")).isEqualTo(456);
        Assertions.assertThat(counter.get()).isEqualTo(2);
        Assertions.assertThat(function.apply("123")).isEqualTo(123);
        Assertions.assertThat(counter.get()).isEqualTo(2);
    }

    @Test
    public void testMemoizedFunctionWithError() {
        AtomicInteger counter = new AtomicInteger(0);
        MemoizedFunction<String, Integer> function = new MemoizedFunction<>(s -> {
            counter.incrementAndGet();
            if (counter.get() < 4) {
                throw new RuntimeException("Error");
            }
            return Integer.parseInt(s);
        });
        Assertions.assertThat(counter.get()).isEqualTo(0);
        Assertions.assertThatThrownBy(() -> function.apply("123")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThatThrownBy(() -> function.apply("123")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(2);
        Assertions.assertThatThrownBy(() -> function.apply("123")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(3);
        Assertions.assertThat(function.apply("123")).isEqualTo(123);
        Assertions.assertThat(counter.get()).isEqualTo(4);
        Assertions.assertThat(function.apply("123")).isEqualTo(123);
        Assertions.assertThat(counter.get()).isEqualTo(4);

        Assertions.assertThat(function.apply("456")).isEqualTo(456);
        Assertions.assertThat(counter.get()).isEqualTo(5);
    }

    @Test
    public void testMemoizedFunctionErrorWithNoRetry() {
        AtomicInteger counter = new AtomicInteger(0);
        MemoizedFunction<String, Integer> function = new MemoizedFunction<>(s -> {
            counter.incrementAndGet();
            throw new RuntimeException("Error");
        }, false);
        Assertions.assertThat(counter.get()).isEqualTo(0);
        Assertions.assertThatThrownBy(() -> function.apply("123")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThatThrownBy(() -> function.apply("123")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThatThrownBy(() -> function.apply("123")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThatThrownBy(() -> function.apply("123")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThatThrownBy(() -> function.apply("123")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    public void testLongRunningMemoizedFunction() {
        AtomicInteger counter = new AtomicInteger(0);
        MemoizedFunction<String, Integer> function = new MemoizedFunction<>(s -> {
            counter.incrementAndGet();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException("Error on test", e);
            }
            return Integer.parseInt(s);
        });
        Assertions.assertThat(counter.get()).isEqualTo(0);
        Assertions.assertThat(function.apply("123")).isEqualTo(123);
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThat(function.apply("123")).isEqualTo(123);
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThat(function.apply("456")).isEqualTo(456);
        Assertions.assertThat(counter.get()).isEqualTo(2);
        Assertions.assertThat(function.apply("456")).isEqualTo(456);
        Assertions.assertThat(counter.get()).isEqualTo(2);
        Assertions.assertThat(function.apply("123")).isEqualTo(123);
        Assertions.assertThat(counter.get()).isEqualTo(2);
    }

}
