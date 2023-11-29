package com.runestone.memoization;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Execution(ExecutionMode.CONCURRENT)
public class TestMemoizedExpiringSupplier {

    @Test
    public void testMemoizedExpiringSupplier() {
        final AtomicInteger counter = new AtomicInteger(0);
        MemoizedExpiringSupplier<String> supplier = new MemoizedExpiringSupplier<>(() -> {
            counter.incrementAndGet();
            return "Hello World";
        }, 1, TimeUnit.SECONDS);
        Assertions.assertThat(supplier.get()).isEqualTo("Hello World");
        Assertions.assertThat(supplier.get()).isEqualTo("Hello World");
        Assertions.assertThat(supplier.get()).isEqualTo("Hello World"); // should be cached, checking multiple times to be sure
        Assertions.assertThat(counter.get()).isEqualTo(1); // should have been called only once

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(supplier.get()).isEqualTo("Hello World");
        Assertions.assertThat(counter.get()).isEqualTo(2);
        Assertions.assertThat(supplier.get()).isEqualTo("Hello World");
        Assertions.assertThat(counter.get()).isEqualTo(2);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assertions.assertThat(supplier.get()).isEqualTo("Hello World");
        Assertions.assertThat(counter.get()).isEqualTo(3);
        Assertions.assertThat(supplier.get()).isEqualTo("Hello World");
        Assertions.assertThat(counter.get()).isEqualTo(3);
    }

    @Test
    public void testMemoizedExpiringSupplierWithError() {
        final AtomicInteger counter = new AtomicInteger(0);
        MemoizedExpiringSupplier<String> supplier = new MemoizedExpiringSupplier<>(() -> {
            counter.incrementAndGet();
            if (counter.get() < 4) {
                throw new RuntimeException("Error");
            }
            return "Hello World";
        }, 1, TimeUnit.SECONDS);
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

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(supplier.get()).isEqualTo("Hello World");
        Assertions.assertThat(counter.get()).isEqualTo(5);
        Assertions.assertThat(supplier.get()).isEqualTo("Hello World");
        Assertions.assertThat(counter.get()).isEqualTo(5);
    }

    @Test
    public void testLongRunningExpiringSupplier() {
        final AtomicInteger counter = new AtomicInteger(0);
        MemoizedExpiringSupplier<String> supplier = new MemoizedExpiringSupplier<>(() -> {
            counter.incrementAndGet();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Hello World";
        }, 2, TimeUnit.SECONDS);

        Thread.ofVirtual().start(supplier::get);
        Thread.ofVirtual().start(supplier::get);
        Thread.ofVirtual().start(supplier::get);

        Assertions.assertThat(supplier.get()).isEqualTo("Hello World");
        Assertions.assertThat(supplier.get()).isEqualTo("Hello World");
        Assertions.assertThat(supplier.get()).isEqualTo("Hello World");
        Assertions.assertThat(supplier.get()).isEqualTo("Hello World"); // should be cached, checking multiple times to be sure
        Assertions.assertThat(counter.get()).isEqualTo(1); // should have been called only once

        try {
            Thread.sleep(2100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assertions.assertThat(supplier.get()).isEqualTo("Hello World");
        Assertions.assertThat(supplier.get()).isEqualTo("Hello World");
        Assertions.assertThat(counter.get()).isEqualTo(2);

    }

}
