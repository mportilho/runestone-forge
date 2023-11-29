package com.runestone.memoization;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class TestMemoizedBiFunction {

    @Test
    public void testMemoizedBiFunction() {
        AtomicInteger counter = new AtomicInteger(0);
        MemoizedBiFunction<String, String, String> function = new MemoizedBiFunction<>((s1, s2) -> {
            counter.incrementAndGet();
            return s1 + s2;
        });
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
    public void testMemoizedBiFunctionWithError() {
        AtomicInteger counter = new AtomicInteger(0);
        MemoizedBiFunction<String, String, String> function = new MemoizedBiFunction<>((s1, s2) -> {
            counter.incrementAndGet();
            if (counter.get() < 4) {
                throw new RuntimeException("Error");
            }
            return s1 + s2;
        });
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
    public void testMemoizedFunctionErrorWithNoRetry() {
        AtomicInteger counter = new AtomicInteger(0);
        MemoizedBiFunction<String, String, String> function = new MemoizedBiFunction<>((s1, s2) -> {
            counter.incrementAndGet();
            throw new RuntimeException("Error");
        }, false);
        Assertions.assertThat(counter.get()).isEqualTo(0);
        Assertions.assertThatThrownBy(() -> function.apply("123", "456")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThatThrownBy(() -> function.apply("123", "456")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThatThrownBy(() -> function.apply("123", "456")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThatThrownBy(() -> function.apply("123", "456")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(1);
        Assertions.assertThatThrownBy(() -> function.apply("123", "456")).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    public void testLongRunningMemoizedBiFunction() {
        AtomicInteger counter = new AtomicInteger(0);
        MemoizedBiFunction<String, String, String> function = new MemoizedBiFunction<>((s1, s2) -> {
            counter.incrementAndGet();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return s1 + s2;
        });

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
}
