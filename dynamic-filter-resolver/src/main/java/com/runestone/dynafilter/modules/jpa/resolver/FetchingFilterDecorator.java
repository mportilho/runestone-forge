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
import org.springframework.data.core.PropertyPath;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FetchingFilterDecorator implements FilterDecorator<Specification<?>> {

    private final Collection<Fetching> fetches;
    private final List<ResolvedFetchPath> resolvedFetchPaths;

    public FetchingFilterDecorator(Collection<Fetching> fetches) {
        if (Asserts.isEmpty(fetches)) {
            throw new IllegalArgumentException("fetches is required to be not empty");
        }
        this.fetches = List.copyOf(fetches);
        this.resolvedFetchPaths = parseFetches(this.fetches);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Specification<?> decorate(Specification<?> filter, StatementWrapper statementWrapper) {
        Specification<?> decorated = (root, query, criteriaBuilder) -> {
            Class<?> resultType = query.getResultType();
            boolean countQuery = Long.class.equals(resultType) || long.class.equals(resultType);
            if (!countQuery) {
                query.distinct(true);
                Map<FetchParent<?, ?>, Map<FetchSegment, Fetch<?, ?>>> fetchLookup = new IdentityHashMap<>();
                for (ResolvedFetchPath resolvedFetchPath : resolvedFetchPaths) {
                    createJoinClause(root, resolvedFetchPath, fetchLookup);
                }
            }
            return null;
        };
        return filter != null ? ((Specification) decorated).and(filter) : decorated;
    }

    private static void createJoinClause(Root<Object> root, ResolvedFetchPath fetchPath,
                                         Map<FetchParent<?, ?>, Map<FetchSegment, Fetch<?, ?>>> fetchLookup) {
        FetchParent<?, ?> from = root;
        for (String segment : fetchPath.segments()) {
            from = getOrCreateFetch(from, segment, fetchPath.joinType(), fetchLookup);
        }
    }

    private static Fetch<?, ?> getOrCreateFetch(FetchParent<?, ?> from, String attributePath, JoinType joinType,
                                                Map<FetchParent<?, ?>, Map<FetchSegment, Fetch<?, ?>>> fetchLookup) {
        FetchSegment fetchSegment = new FetchSegment(attributePath, joinType);
        Map<FetchSegment, Fetch<?, ?>> perParentLookup = fetchLookup.computeIfAbsent(from, ignored -> new HashMap<>());
        Fetch<?, ?> cachedFetch = perParentLookup.get(fetchSegment);
        if (cachedFetch != null) {
            return cachedFetch;
        }

        for (Fetch<?, ?> fetch : from.getFetches()) {
            boolean sameName = fetch.getAttribute().getName().equals(attributePath);
            if (sameName && fetch.getJoinType().equals(joinType)) {
                perParentLookup.put(fetchSegment, fetch);
                return fetch;
            }
        }
        Fetch<?, ?> createdFetch = from.fetch(attributePath, joinType);
        perParentLookup.put(fetchSegment, createdFetch);
        return createdFetch;
    }

    private static List<ResolvedFetchPath> parseFetches(Collection<Fetching> fetches) {
        List<ResolvedFetchPath> resolvedPaths = new ArrayList<>();
        Map<FetchPath, ResolvedFetchPath> deduplicatedPaths = new LinkedHashMap<>();

        for (Fetching fetching : fetches) {
            for (String path : fetching.value()) {
                FetchPath fetchPath = new FetchPath(path, fetching.joinType());
                deduplicatedPaths.computeIfAbsent(fetchPath, key ->
                        new ResolvedFetchPath(splitPathSegments(path), fetching.joinType()));
            }
        }

        resolvedPaths.addAll(deduplicatedPaths.values());
        return List.copyOf(resolvedPaths);
    }

    private static String[] splitPathSegments(String path) {
        String nonNullPath = Objects.requireNonNull(path, "Fetching path cannot be null").trim();
        if (nonNullPath.isEmpty()) {
            throw new IllegalArgumentException("Fetching path cannot be blank");
        }

        int segmentCount = 1;
        for (int i = 0; i < nonNullPath.length(); i++) {
            if (nonNullPath.charAt(i) == '.') {
                segmentCount++;
            }
        }

        String[] segments = new String[segmentCount];
        int start = 0;
        int segmentIndex = 0;
        for (int i = 0; i < nonNullPath.length(); i++) {
            if (nonNullPath.charAt(i) == '.') {
                if (i == start) {
                    throw new IllegalArgumentException("Invalid fetching path segment on path '%s'".formatted(path));
                }
                segments[segmentIndex++] = nonNullPath.substring(start, i);
                start = i + 1;
            }
        }
        if (start == nonNullPath.length()) {
            throw new IllegalArgumentException("Invalid fetching path segment on path '%s'".formatted(path));
        }
        segments[segmentIndex] = nonNullPath.substring(start);
        return segments;
    }

    public Collection<Fetching> getFetches() {
        return fetches;
    }

    private record FetchPath(String value, JoinType joinType) {
    }

    private record FetchSegment(String segment, JoinType joinType) {
    }

    private record ResolvedFetchPath(String[] segments, JoinType joinType) {
    }
}
