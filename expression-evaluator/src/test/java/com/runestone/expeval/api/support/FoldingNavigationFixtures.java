package com.runestone.expeval.api.support;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FoldingNavigationFixtures {

    private FoldingNavigationFixtures() {
    }

    public static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    public static TrackedList<BigDecimal> prices() {
        return new TrackedList<>(List.of(bd("5"), bd("15"), bd("25"), bd("10")));
    }

    public static TrackedList<Book> books() {
        return new TrackedList<>(List.of(
                new Book("Alpha", "Alice", bd("5.99")),
                new Book("Beta", "Bob", bd("12.99")),
                new Book("Gamma", "Alice", bd("8.99")),
                new Book("Delta", "Carol", bd("19.99"))
        ));
    }

    public static Catalog catalog() {
        return new Catalog(books());
    }

    public static TrackedMap<AccountKey, Account> accounts() {
        TrackedMap<AccountKey, Account> accounts = new TrackedMap<>();
        accounts.put(new AccountKey(1, "ops"), new Account(bd("10")));
        accounts.put(new AccountKey(2, "dev"), new Account(bd("20")));
        accounts.put(new AccountKey(3, "ops"), new Account(bd("30")));
        return accounts;
    }

    public static final class TrackedList<T> extends ArrayList<T> {

        private int accessCount;

        public TrackedList(List<T> items) {
            super(items);
        }

        public int accessCount() {
            return accessCount;
        }

        public void resetAccessCount() {
            accessCount = 0;
        }

        @Override
        public T get(int index) {
            accessCount++;
            return super.get(index);
        }

        @Override
        public Iterator<T> iterator() {
            accessCount++;
            return super.iterator();
        }

        @Override
        public int size() {
            accessCount++;
            return super.size();
        }

        @Override
        public List<T> subList(int fromIndex, int toIndex) {
            accessCount++;
            return super.subList(fromIndex, toIndex);
        }
    }

    public static final class TrackedMap<K, V> extends LinkedHashMap<K, V> {

        private int accessCount;

        public int accessCount() {
            return accessCount;
        }

        public void resetAccessCount() {
            accessCount = 0;
        }

        @Override
        public V get(Object key) {
            accessCount++;
            return super.get(key);
        }

        @Override
        public Set<Map.Entry<K, V>> entrySet() {
            accessCount++;
            return super.entrySet();
        }

        @Override
        public Collection<V> values() {
            accessCount++;
            return super.values();
        }

        @Override
        public Set<K> keySet() {
            accessCount++;
            return super.keySet();
        }

        @Override
        public int size() {
            accessCount++;
            return super.size();
        }
    }

    public record Book(String title, String author, BigDecimal price) {
    }

    public record Catalog(List<Book> books) {
    }

    public record AccountKey(int id, String domain) {
    }

    public record Account(BigDecimal balance) {
    }

    public static final class CountingBox {

        private int calls;

        public BigDecimal amount() {
            calls++;
            return bd("7");
        }

        public int calls() {
            return calls;
        }

        public void resetCalls() {
            calls = 0;
        }
    }

    public static final class CountingTypedBox {

        private final BigDecimal amount;
        private int calls;

        public CountingTypedBox(BigDecimal amount) {
            this.amount = amount;
        }

        public BigDecimal multiply(BigDecimal factor) {
            calls++;
            return amount.multiply(factor);
        }

        public int calls() {
            return calls;
        }

        public void resetCalls() {
            calls = 0;
        }
    }

    public record TypedBox(BigDecimal amount) {

        public BigDecimal multiply(BigDecimal factor) {
            return amount.multiply(factor);
        }
    }
}
