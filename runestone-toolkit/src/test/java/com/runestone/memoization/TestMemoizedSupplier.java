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
