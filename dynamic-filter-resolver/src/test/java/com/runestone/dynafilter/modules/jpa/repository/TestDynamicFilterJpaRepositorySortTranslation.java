/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2023 Marcelo Silva Portilho
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.runestone.dynafilter.modules.jpa.repository;

import com.runestone.dynafilter.core.model.FilterRequestData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.List;

public class TestDynamicFilterJpaRepositorySortTranslation {

    @Test
    public void testTranslateMappedSortProperties() {
        Sort sort = Sort.by(Sort.Order.asc("nameParam"), Sort.Order.desc("ageParam"), Sort.Order.asc("raw"));
        List<FilterRequestData> filters = List.of(
                filter("person.name", "nameParam"),
                filter("person.age", "ageParam"),
                filter("raw", "raw")
        );

        Sort translatedSort = DynamicFilterJpaRepositoryImpl.updateSortFilterPath(sort, filters);

        Assertions.assertThat(translatedSort.stream().map(Sort.Order::getProperty).toList())
                .containsExactly("person.name", "person.age", "raw");
        Assertions.assertThat(translatedSort.stream().map(Sort.Order::getDirection).toList())
                .containsExactly(Sort.Direction.ASC, Sort.Direction.DESC, Sort.Direction.ASC);
    }

    @Test
    public void testKeepSameSortWhenNoTranslationIsNeeded() {
        Sort sort = Sort.by(Sort.Order.asc("raw"));
        List<FilterRequestData> filters = List.of(
                filter("raw", "raw"),
                filter("entity.name", "name")
        );

        Sort translatedSort = DynamicFilterJpaRepositoryImpl.updateSortFilterPath(sort, filters);

        Assertions.assertThat(translatedSort).isSameAs(sort);
    }

    @Test
    public void testUseFirstTranslatablePathForDuplicatedParameter() {
        Sort sort = Sort.by(Sort.Order.asc("status"));
        List<FilterRequestData> filters = List.of(
                filter("status", "status"),
                filter("person.status", "status"),
                filter("address.status", "status")
        );

        Sort translatedSort = DynamicFilterJpaRepositoryImpl.updateSortFilterPath(sort, filters);

        Assertions.assertThat(translatedSort.getOrderFor("person.status")).isNotNull();
        Assertions.assertThat(translatedSort.getOrderFor("address.status")).isNull();
    }

    @Test
    public void testUnsortedInputKeepsReference() {
        Sort sort = Sort.unsorted();

        Sort translatedSort = DynamicFilterJpaRepositoryImpl.updateSortFilterPath(sort, List.of(filter("person.name", "nameParam")));

        Assertions.assertThat(translatedSort).isSameAs(sort);
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
