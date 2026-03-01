package com.runestone.expeval.perf.jmh;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 2)
@Threads(4)
public class CacheComparisonBenchmark {

    private Map<String, String> legacyLruCache;
    private Map<String, String> concurrentHashMap;
    private Map<String, String> caffeineCache;

    @Setup(Level.Iteration)
    public void setup() {
        legacyLruCache = new LruCacheLegacy<>(1024);
        concurrentHashMap = new ConcurrentHashMap<>(1024);
        caffeineCache = Caffeine.newBuilder().maximumSize(1024).<String, String>build().asMap();
        
        for (int i = 0; i < 500; i++) {
            String key = "key" + i;
            String val = "val" + i;
            legacyLruCache.put(key, val);
            concurrentHashMap.put(key, val);
            caffeineCache.put(key, val);
        }
    }

    @Benchmark
    public String testLegacyLruCache(ThreadState state) {
        String key = "key" + (state.index++ % 2000);
        return legacyLruCache.computeIfAbsent(key, k -> "new_val");
    }

    @Benchmark
    public String testConcurrentHashMap(ThreadState state) {
        String key = "key" + (state.index++ % 2000);
        return concurrentHashMap.computeIfAbsent(key, k -> "new_val");
    }

    @Benchmark
    public String testCaffeineCache(ThreadState state) {
        String key = "key" + (state.index++ % 2000);
        return caffeineCache.computeIfAbsent(key, k -> "new_val");
    }

    @State(Scope.Thread)
    public static class ThreadState {
        int index = 0;
    }

    // Manual implementation of the old LruCache for exact comparison
    public static class LruCacheLegacy<K, V> extends LinkedHashMap<K, V> {
        private final int maxEntries;

        public LruCacheLegacy(int maxEntries) {
            super(Math.min(maxEntries, 512), 0.75f, true);
            this.maxEntries = maxEntries;
        }

        @Override
        public synchronized V get(Object key) { return super.get(key); }
        @Override
        public synchronized V put(K key, V value) { return super.put(key, value); }
        @Override
        public synchronized V putIfAbsent(K key, V value) { return super.putIfAbsent(key, value); }
        @Override
        public synchronized V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) { return super.computeIfAbsent(key, mappingFunction); }
        @Override
        public synchronized V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) { return super.computeIfPresent(key, remappingFunction); }
        @Override
        public synchronized V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) { return super.compute(key, remappingFunction); }
        @Override
        public synchronized V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) { return super.merge(key, value, remappingFunction); }
        @Override
        public synchronized void clear() { super.clear(); }
        @Override
        public synchronized int size() { return super.size(); }
        @Override
        public synchronized V remove(Object key) { return super.remove(key); }
        @Override
        public synchronized boolean remove(Object key, Object value) { return super.remove(key, value); }
        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) { return size() > maxEntries; }
    }

    @Test
    public void runBenchmark() throws Exception {
        String[] argv = {this.getClass().getName()};
        org.openjdk.jmh.Main.main(argv);
    }
}
