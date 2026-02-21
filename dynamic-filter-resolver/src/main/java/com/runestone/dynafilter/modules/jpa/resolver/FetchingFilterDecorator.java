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

package com.runestone.dynafilter.modules.jpa.resolver;

import com.runestone.assertions.Asserts;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.resolver.FilterDecorator;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.FetchParent;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.mapping.PropertyPath;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class FetchingFilterDecorator implements FilterDecorator<Specification<?>> {

    private final Collection<Fetching> fetches;

    public FetchingFilterDecorator(Collection<Fetching> fetches) {
        if (Asserts.isEmpty(fetches)) {
            throw new IllegalArgumentException("fetches is required to be not empty");
        }
        this.fetches = fetches;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Specification<?> decorate(Specification<?> filter, StatementWrapper statementWrapper) {
        Specification<?> decorated = (root, query, criteriaBuilder) -> {
            Class<?> resultType = query.getResultType();
            if (resultType == null || (!Long.class.equals(resultType) && !long.class.equals(resultType))) {
                query.distinct(true);
            }
            Set<FetchPath> createdFetches = new HashSet<>(fetches.size() * 2);
            for (Fetching fetching : fetches) {
                createJoinClause(root, fetching, createdFetches);
            }
            return null;
        };
        return filter != null ? ((Specification) decorated).and(filter) : decorated;
    }

    private static void createJoinClause(Root<Object> root, Fetching fetching, Set<FetchPath> createdFetches) {
        for (String attributePath : fetching.value()) {
            FetchPath fetchPath = new FetchPath(attributePath, fetching.joinType());
            if (!createdFetches.add(fetchPath)) {
                continue;
            }
            FetchParent<?, ?> from = root;
            PropertyPath propertyPath = PropertyPath.from(attributePath, root.getJavaType());
            while (propertyPath != null && propertyPath.hasNext()) {
                from = getOrCreateFetch(from, propertyPath.getSegment(), fetching.joinType());
                propertyPath = propertyPath.next();
            }
            if (propertyPath != null) {
                getOrCreateFetch(from, propertyPath.getSegment(), fetching.joinType());
            } else {
                throw new IllegalStateException(
                        String.format("Expected parsing to yield a PropertyPath from %s but got null!", attributePath));
            }
        }
    }

    private static Fetch<?, ?> getOrCreateFetch(FetchParent<?, ?> from, String attributePath, JoinType joinType) {
        for (Fetch<?, ?> fetch : from.getFetches()) {
            boolean sameName = fetch.getAttribute().getName().equals(attributePath);
            if (sameName && fetch.getJoinType().equals(joinType)) {
                return fetch;
            }
        }
        return from.fetch(attributePath, joinType);
    }

    public Collection<Fetching> getFetches() {
        return fetches;
    }

    private record FetchPath(String value, JoinType joinType) {
    }
}
