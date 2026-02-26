package com.runestone.dynafilter.modules.jpa.repository;

import com.runestone.dynafilter.core.model.FilterRequestData;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@Threads(1)
public class DynamicFilterRepositorySortPerfBenchmark {

    @Benchmark
    public void perf05_sortTranslation_optimized_manyOrdersManyFilters(HeavyMappingsState state, Blackhole blackhole) {
        blackhole.consume(DynamicFilterJpaRepositoryImpl.updateSortFilterPath(state.sort, state.filters));
    }

    @Benchmark
    public void perf05_sortTranslation_legacy_manyOrdersManyFilters(HeavyMappingsState state, Blackhole blackhole) {
        blackhole.consume(legacySortTranslation(state.sort, state.filters));
    }

    @Benchmark
    public void perf05_sortTranslation_optimized_noTranslationNeeded(NoTranslationState state, Blackhole blackhole) {
        blackhole.consume(DynamicFilterJpaRepositoryImpl.updateSortFilterPath(state.sort, state.filters));
    }

    @Benchmark
    public void perf05_sortTranslation_legacy_noTranslationNeeded(NoTranslationState state, Blackhole blackhole) {
        blackhole.consume(legacySortTranslation(state.sort, state.filters));
    }

    @State(Scope.Benchmark)
    public static class HeavyMappingsState {
        private Sort sort;
        private List<FilterRequestData> filters;

        @Setup(Level.Trial)
        public void setup() {
            List<Sort.Order> orders = new ArrayList<>(50);
            for (int i = 0; i < 50; i++) {
                orders.add(Sort.Order.asc("p" + i));
            }
            this.sort = Sort.by(orders);

            List<FilterRequestData> allFilters = new ArrayList<>(600);
            for (int i = 0; i < 500; i++) {
                allFilters.add(filter("x" + i, "x" + i));
            }
            for (int i = 0; i < 50; i++) {
                String parameter = "p" + i;
                allFilters.add(filter(parameter, parameter));
                allFilters.add(filter("entity." + parameter, parameter));
            }
            this.filters = List.copyOf(allFilters);
        }
    }

    @State(Scope.Benchmark)
    public static class NoTranslationState {
        private Sort sort;
        private List<FilterRequestData> filters;

        @Setup(Level.Trial)
        public void setup() {
            List<Sort.Order> orders = new ArrayList<>(50);
            for (int i = 0; i < 50; i++) {
                orders.add(Sort.Order.desc("p" + i));
            }
            this.sort = Sort.by(orders);

            List<FilterRequestData> allFilters = new ArrayList<>(600);
            for (int i = 0; i < 600; i++) {
                String parameter = "p" + (i % 50);
                allFilters.add(filter(parameter, parameter));
            }
            this.filters = List.copyOf(allFilters);
        }
    }

    private static Sort legacySortTranslation(Sort sort, List<FilterRequestData> filters) {
        if (!sort.isSorted()) {
            return sort;
        }
        List<Sort.Order> orderList = sort.stream().map(order -> {
            for (FilterRequestData filter : filters) {
                String[] parameters = filter.parameters();
                if (parameters.length > 0 && order.getProperty().equals(parameters[0]) && !order.getProperty().equals(filter.path())) {
                    return order.withProperty(filter.path());
                }
            }
            return order;
        }).toList();
        return Sort.by(orderList);
    }

    private static FilterRequestData filter(String path, String... parameters) {
        return new FilterRequestData(
                path,
                parameters,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                List.of(),
                null
        );
    }
}
