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
                e.printStackTrace();
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