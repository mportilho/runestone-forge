package com.runestone.memoization;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Execution(ExecutionMode.CONCURRENT)
public class TestMemoizedExpiringFunction {

    @Test
    public void testMemoizedExpiringFunction() {
        AtomicInteger counter = new AtomicInteger(0);
        MemoizedExpiringFunction<String, Integer> function = new MemoizedExpiringFunction<>(s -> {
            counter.incrementAndGet();
            return Integer.parseInt(s);
        }, 2, TimeUnit.SECONDS);

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
    public void testMemoizedExpiringFunctionWithError() {
        AtomicInteger counter = new AtomicInteger(0);
        MemoizedExpiringFunction<String, Integer> function = new MemoizedExpiringFunction<>(s -> {
            counter.incrementAndGet();
            if (counter.get() < 4) {
                throw new RuntimeException("Error");
            }
            return Integer.parseInt(s);
        }, 2, TimeUnit.SECONDS);

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
    public void testMemoizedExpiringFunctionErrorWithNoRetry() {
        AtomicInteger counter = new AtomicInteger(0);
        MemoizedExpiringFunction<String, Integer> function = new MemoizedExpiringFunction<>(s -> {
            counter.incrementAndGet();
            throw new RuntimeException("Error");
        }, 1, TimeUnit.SECONDS, false);
        Assertions.assertThat(counter.get()).isEqualTo(0);
        Assertions.assertThatThrownBy(() -> function.apply("123")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThatThrownBy(() -> function.apply("123")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThatThrownBy(() -> function.apply("123")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(1);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assertions.assertThatThrownBy(() -> function.apply("123")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(2);
        Assertions.assertThatThrownBy(() -> function.apply("123")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(2);

        Assertions.assertThatThrownBy(() -> function.apply("789")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(3);

        Assertions.assertThatThrownBy(() -> function.apply("789")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(3);
    }

    @Test
    public void testMemoizedExpiringFunctionWithTimer() {
        AtomicInteger counter = new AtomicInteger(0);
        MemoizedExpiringFunction<String, Integer> function = new MemoizedExpiringFunction<>(s -> {
            counter.incrementAndGet();
            return Integer.parseInt(s);
        }, 1, TimeUnit.SECONDS);

        Assertions.assertThat(counter.get()).isEqualTo(0);
        Assertions.assertThat(function.apply("123")).isEqualTo(123);
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThat(function.apply("123")).isEqualTo(123);
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThat(function.apply("123")).isEqualTo(123);
        Assertions.assertThat(counter.get()).isEqualTo(1);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(function.apply("123")).isEqualTo(123);
        Assertions.assertThat(counter.get()).isEqualTo(2);
        Assertions.assertThat(function.apply("123")).isEqualTo(123);
        Assertions.assertThat(counter.get()).isEqualTo(2);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assertions.assertThat(function.apply("123")).isEqualTo(123);
        Assertions.assertThat(counter.get()).isEqualTo(3);
        Assertions.assertThat(function.apply("123")).isEqualTo(123);
        Assertions.assertThat(counter.get()).isEqualTo(3);

        Assertions.assertThat(function.apply("789")).isEqualTo(789);
        Assertions.assertThat(counter.get()).isEqualTo(4);
        Assertions.assertThat(function.apply("789")).isEqualTo(789);
        Assertions.assertThat(counter.get()).isEqualTo(4);
    }

    @Test
    public void testMemoizedExpiringFunctionWithTimerWithError() {
        final AtomicInteger counter = new AtomicInteger(0);

        MemoizedExpiringFunction<String, Integer> function = new MemoizedExpiringFunction<>(s -> {
            counter.incrementAndGet();
            if (counter.get() < 4) {
                throw new RuntimeException("Error");
            }
            return Integer.parseInt(s);
        }, 1, TimeUnit.SECONDS);

        Assertions.assertThat(counter.get()).isEqualTo(0);
        Assertions.assertThatThrownBy(() -> function.apply("123")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThatThrownBy(() -> function.apply("789")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(2);
        Assertions.assertThatThrownBy(() -> function.apply("123")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(3);
        Assertions.assertThat(function.apply("123")).isEqualTo(123);
        Assertions.assertThat(counter.get()).isEqualTo(4);
        Assertions.assertThat(function.apply("123")).isEqualTo(123);
        Assertions.assertThat(counter.get()).isEqualTo(4);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(function.apply("123")).isEqualTo(123);
        Assertions.assertThat(counter.get()).isEqualTo(5);
        Assertions.assertThat(function.apply("123")).isEqualTo(123);
        Assertions.assertThat(counter.get()).isEqualTo(5);

        Assertions.assertThat(function.apply("789")).isEqualTo(789);
        Assertions.assertThat(counter.get()).isEqualTo(6);
        Assertions.assertThat(function.apply("789")).isEqualTo(789);
        Assertions.assertThat(counter.get()).isEqualTo(6);
    }

    @Test
    public void testLongRunningMemoizedExpiringFunction() {
        AtomicInteger counter = new AtomicInteger(0);
        MemoizedExpiringFunction<String, Integer> function = new MemoizedExpiringFunction<>(s -> {
            counter.incrementAndGet();
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return Integer.parseInt(s);
        }, 2, TimeUnit.SECONDS);

        Assertions.assertThat(counter.get()).isEqualTo(0);
        Assertions.assertThat(function.apply("123")).isEqualTo(123);
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThat(function.apply("123")).isEqualTo(123);
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThat(function.apply("456")).isEqualTo(456);
        Assertions.assertThat(counter.get()).isEqualTo(2);
        Assertions.assertThat(function.apply("456")).isEqualTo(456);
        Assertions.assertThat(counter.get()).isEqualTo(2);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assertions.assertThat(function.apply("123")).isEqualTo(123);
        Assertions.assertThat(counter.get()).isEqualTo(3);
        Assertions.assertThat(function.apply("456")).isEqualTo(456);
        Assertions.assertThat(counter.get()).isEqualTo(4);
    }

}
