package com.runestone.memoization;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Execution(ExecutionMode.CONCURRENT)
public class TestMemoizedExpiringBiFunction {

    @Test
    public void testMemoizedExpiringBiFunction() {
        AtomicInteger counter = new AtomicInteger(0);
        MemoizedExpiringBiFunction<String, String, String> function = new MemoizedExpiringBiFunction<>((s1, s2) -> {
            counter.incrementAndGet();
            return s1 + s2;
        }, 1, TimeUnit.SECONDS);
        Assertions.assertThat(counter.get()).isEqualTo(0);
        Assertions.assertThat(function.apply("123", "456")).isEqualTo("123456");
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThat(function.apply("123", "456")).isEqualTo("123456");
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThat(function.apply("0", "1")).isEqualTo("01");
        Assertions.assertThat(counter.get()).isEqualTo(2);
        Assertions.assertThat(function.apply("0", "1")).isEqualTo("01");
        Assertions.assertThat(counter.get()).isEqualTo(2);

        Assertions.assertThat(function.apply("123", "456")).isEqualTo("123456");
        Assertions.assertThat(counter.get()).isEqualTo(2);
    }

    @Test
    public void testMemoizedExpiringBiFunctionWithError() {
        AtomicInteger counter = new AtomicInteger(0);
        MemoizedExpiringBiFunction<String, String, String> function = new MemoizedExpiringBiFunction<>((s1, s2) -> {
            counter.incrementAndGet();
            if (counter.get() < 4) {
                throw new RuntimeException("Error");
            }
            return s1 + s2;
        }, 1, TimeUnit.SECONDS);
        Assertions.assertThat(counter.get()).isEqualTo(0);
        Assertions.assertThatThrownBy(() -> function.apply("123", "456")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThatThrownBy(() -> function.apply("123", "456")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(2);
        Assertions.assertThatThrownBy(() -> function.apply("123", "456")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(3);
        Assertions.assertThat(function.apply("123", "456")).isEqualTo("123456");
        Assertions.assertThat(counter.get()).isEqualTo(4);
        Assertions.assertThat(function.apply("123", "456")).isEqualTo("123456");
        Assertions.assertThat(counter.get()).isEqualTo(4);

        Assertions.assertThat(function.apply("0", "1")).isEqualTo("01");
        Assertions.assertThat(counter.get()).isEqualTo(5);
        Assertions.assertThat(function.apply("0", "1")).isEqualTo("01");
        Assertions.assertThat(counter.get()).isEqualTo(5);
    }

    @Test
    public void testMemoizedExpiringBiFunctionErrorWithNoRetry() {
        AtomicInteger counter = new AtomicInteger(0);
        MemoizedExpiringBiFunction<String, String, String> function = new MemoizedExpiringBiFunction<>((s1, s2) -> {
            counter.incrementAndGet();
            if (counter.get() < 4) {
                throw new RuntimeException("Error");
            }
            return s1 + s2;
        }, 1, TimeUnit.SECONDS, false);
        Assertions.assertThat(counter.get()).isEqualTo(0);
        Assertions.assertThatThrownBy(() -> function.apply("123", "456")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThatThrownBy(() -> function.apply("123", "456")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThatThrownBy(() -> function.apply("123", "456")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(1);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assertions.assertThatThrownBy(() -> function.apply("123", "456")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(2);
        Assertions.assertThatThrownBy(() -> function.apply("123", "456")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(2);

        Assertions.assertThatThrownBy(() -> function.apply("0", "1")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(3);
        Assertions.assertThatThrownBy(() -> function.apply("0", "1")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(3);
    }

    @Test
    public void testMemoizedExpiringBiFunctionWithTimer() {
        AtomicInteger counter = new AtomicInteger(0);
        MemoizedExpiringBiFunction<String, String, String> function = new MemoizedExpiringBiFunction<>((s1, s2) -> {
            counter.incrementAndGet();
            return s1 + s2;
        }, 1, TimeUnit.SECONDS);

        Assertions.assertThat(counter.get()).isEqualTo(0);
        Assertions.assertThat(function.apply("123", "456")).isEqualTo("123456");
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThat(function.apply("123", "456")).isEqualTo("123456");
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThat(function.apply("123", "456")).isEqualTo("123456");
        Assertions.assertThat(counter.get()).isEqualTo(1);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(function.apply("123", "456")).isEqualTo("123456");
        Assertions.assertThat(counter.get()).isEqualTo(2);
        Assertions.assertThat(function.apply("123", "456")).isEqualTo("123456");
        Assertions.assertThat(counter.get()).isEqualTo(2);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assertions.assertThat(function.apply("123", "456")).isEqualTo("123456");
        Assertions.assertThat(counter.get()).isEqualTo(3);
        Assertions.assertThat(function.apply("123", "456")).isEqualTo("123456");
        Assertions.assertThat(counter.get()).isEqualTo(3);

        Assertions.assertThat(function.apply("0", "1")).isEqualTo("01");
        Assertions.assertThat(counter.get()).isEqualTo(4);
        Assertions.assertThat(function.apply("0", "1")).isEqualTo("01");
        Assertions.assertThat(counter.get()).isEqualTo(4);
    }

    @Test
    public void testMemoizedExpiringBiFunctionWithTimerWithError() {
        AtomicInteger counter = new AtomicInteger(0);
        MemoizedExpiringBiFunction<String, String, String> function = new MemoizedExpiringBiFunction<>((s1, s2) -> {
            counter.incrementAndGet();
            if (counter.get() < 4) {
                throw new RuntimeException("Error");
            }
            return s1 + s2;
        }, 1, TimeUnit.SECONDS);

        Assertions.assertThat(counter.get()).isEqualTo(0);
        Assertions.assertThatThrownBy(() -> function.apply("123", "456")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThatThrownBy(() -> function.apply("0", "1")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(2);
        Assertions.assertThatThrownBy(() -> function.apply("123", "456")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(3);
        Assertions.assertThat(function.apply("123", "456")).isEqualTo("123456");
        Assertions.assertThat(counter.get()).isEqualTo(4);
        Assertions.assertThat(function.apply("123", "456")).isEqualTo("123456");
        Assertions.assertThat(counter.get()).isEqualTo(4);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(function.apply("123", "456")).isEqualTo("123456");
        Assertions.assertThat(counter.get()).isEqualTo(5);
        Assertions.assertThat(function.apply("123", "456")).isEqualTo("123456");
        Assertions.assertThat(counter.get()).isEqualTo(5);

        Assertions.assertThat(function.apply("0", "1")).isEqualTo("01");
        Assertions.assertThat(counter.get()).isEqualTo(6);
        Assertions.assertThat(function.apply("0", "1")).isEqualTo("01");
        Assertions.assertThat(counter.get()).isEqualTo(6);
    }

    @Test
    public void testLongRunningMemoizedExpiringBiFunction() {
        AtomicInteger counter = new AtomicInteger(0);
        MemoizedExpiringBiFunction<String, String, String> function = new MemoizedExpiringBiFunction<>((s1, s2) -> {
            counter.incrementAndGet();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return s1 + s2;
        }, 2, TimeUnit.SECONDS);

        Assertions.assertThat(counter.get()).isEqualTo(0);
        Assertions.assertThat(function.apply("123", "456")).isEqualTo("123456");
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThat(function.apply("123", "456")).isEqualTo("123456");
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThat(function.apply("123", "456")).isEqualTo("123456");
        Assertions.assertThat(counter.get()).isEqualTo(1);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(function.apply("123", "456")).isEqualTo("123456");
        Assertions.assertThat(counter.get()).isEqualTo(2);
        Assertions.assertThat(function.apply("123", "456")).isEqualTo("123456");
        Assertions.assertThat(counter.get()).isEqualTo(2);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assertions.assertThat(function.apply("123", "456")).isEqualTo("123456");
        Assertions.assertThat(counter.get()).isEqualTo(3);
        Assertions.assertThat(function.apply("123", "456")).isEqualTo("123456");
        Assertions.assertThat(counter.get()).isEqualTo(3);
    }

}
