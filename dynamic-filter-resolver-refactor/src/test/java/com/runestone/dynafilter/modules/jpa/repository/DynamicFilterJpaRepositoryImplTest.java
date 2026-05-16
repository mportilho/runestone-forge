package com.runestone.dynafilter.modules.jpa.repository;

import com.runestone.dynafilter.core.model.FilterRequestData;
import com.runestone.dynafilter.core.operation.Equals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DynamicFilterJpaRepositoryImplTest {

    @Test
    @DisplayName("translates external sort parameter to entity path")
    void translatesExternalSortParameterToEntityPath() {
        FilterRequestData filter = new FilterRequestData(
                "addresses.location.city",
                new String[]{"city"},
                Object.class,
                Equals.class,
                "false",
                null,
                null,
                "",
                false,
                List.of(),
                ""
        );

        Sort translated = DynamicFilterJpaRepositoryImpl.updateSortFilterPath(Sort.by(Sort.Order.desc("city")), List.of(filter));

        assertThat(translated).singleElement().satisfies(order -> {
            assertThat(order.getProperty()).isEqualTo("addresses.location.city");
            assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
        });
    }
}
