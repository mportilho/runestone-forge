package com.runestone.dynafilter.modules.performance;

import com.runestone.dynafilter.core.model.FilterRequestData;
import com.runestone.dynafilter.core.operation.Equals;
import com.runestone.dynafilter.modules.jpa.repository.DynamicFilterJpaRepositoryImpl;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Fork(1)
public class DynamicFilterRepositorySortPerfBenchmark {

    @Benchmark
    public void optimizedSortTranslation(HeavyMappingsState state, Blackhole blackhole) {
        blackhole.consume(DynamicFilterJpaRepositoryImpl.updateSortFilterPath(state.sort, state.filters));
    }

    @Benchmark
    public void legacySortTranslation(HeavyMappingsState state, Blackhole blackhole) {
        blackhole.consume(legacyUpdateSortFilterPath(state.sort, state.filters));
    }

    @Benchmark
    public void optimizedSortWithoutTranslations(HeavyMappingsState state, Blackhole blackhole) {
        blackhole.consume(DynamicFilterJpaRepositoryImpl.updateSortFilterPath(state.untranslatedSort, state.filters));
    }

    @State(Scope.Benchmark)
    public static class HeavyMappingsState {

        Sort sort;
        Sort untranslatedSort;
        List<FilterRequestData> filters;

        @Setup
        public void setUp() {
            List<Sort.Order> orders = new ArrayList<>();
            for (int index = 0; index < 50; index++) {
                orders.add(Sort.Order.asc("param" + index));
            }
            sort = Sort.by(orders);
            untranslatedSort = Sort.by(Sort.Order.asc("externalWithoutMapping"));
            filters = new ArrayList<>();
            for (int index = 0; index < 600; index++) {
                filters.add(new FilterRequestData(
                        "nested" + index + ".value",
                        new String[]{"param" + index},
                        Object.class,
                        Equals.class,
                        "false",
                        null,
                        null,
                        "",
                        false,
                        List.of(),
                        ""
                ));
            }
        }
    }

    private static Sort legacyUpdateSortFilterPath(Sort sort, List<FilterRequestData> filters) {
        List<Sort.Order> orders = new ArrayList<>();
        for (Sort.Order order : sort) {
            String property = order.getProperty();
            for (FilterRequestData filter : filters) {
                if (filter.parameters()[0].equals(order.getProperty())) {
                    property = filter.path();
                }
            }
            orders.add(order.withProperty(property));
        }
        return Sort.by(orders);
    }
}
