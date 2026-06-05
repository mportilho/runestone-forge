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

package com.runestone.dynafilter.modules.jpa.support;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.modules.jpa.operation.modifiers.ModJoinTypeLeft;
import com.runestone.dynafilter.modules.jpa.operation.modifiers.ModJoinTypeRight;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public final class JpaPaths {

    private static final String[] EMPTY_SEGMENTS = {};
    private static final Map<String, ParsedPath> PARSED_PATH_CACHE = Caffeine.newBuilder()
            .maximumSize(4096)
            .executor(Runnable::run)
            .<String, ParsedPath>build().asMap();

    private JpaPaths() {
    }

    public static void applyDistinctIfNeeded(ResolvedJpaPath<?> path, CriteriaQuery<?> query) {
        if (path.crossedPluralAssociation()) {
            query.distinct(true);
        }
    }

    public static <T> ResolvedJpaPath<T> resolveAttributePath(String path, FilterData filterData, Root<?> root) {
        Objects.requireNonNull(path, "path cannot be null");
        String key = root.getJavaType().getCanonicalName() + "." + path;
        ParsedPath parsedPath = PARSED_PATH_CACHE.computeIfAbsent(key, k -> parsePath(path));
        JoinType joinType = getJoinType(filterData);
        From<?, ?> from = root;
        boolean crossedPluralAssociation = false;

        for (String associationSegment : parsedPath.associationSegments()) {
            Join<?, ?> join = getOrCreateJoin(from, associationSegment, joinType);
            crossedPluralAssociation = crossedPluralAssociation || isPluralAttribute(join);
            from = join;
        }
        return new ResolvedJpaPath<>(from.get(parsedPath.attributeSegment()), crossedPluralAssociation);
    }

    @SuppressWarnings("unchecked")
    public static <T> ResolvedJpaPath<T> resolveAttributeJoinPath(String path, FilterData filterData, Root<?> root) {
        Objects.requireNonNull(path, "path cannot be null");
        String key = root.getJavaType().getCanonicalName() + "." + path;
        ParsedPath parsedPath = PARSED_PATH_CACHE.computeIfAbsent(key, k -> parsePath(path));
        JoinType joinType = getJoinType(filterData);
        From<?, ?> from = root;
        boolean crossedPluralAssociation = false;

        for (String associationSegment : parsedPath.associationSegments()) {
            Join<?, ?> join = getOrCreateJoin(from, associationSegment, joinType);
            crossedPluralAssociation = crossedPluralAssociation || isPluralAttribute(join);
            from = join;
        }
        Join<?, ?> join = getOrCreateJoin(from, parsedPath.attributeSegment(), joinType);
        crossedPluralAssociation = crossedPluralAssociation || isPluralAttribute(join);
        return new ResolvedJpaPath<>((Path<T>) join, crossedPluralAssociation);
    }

    static void clearCaches() {
        PARSED_PATH_CACHE.clear();
    }

    private static JoinType getJoinType(FilterData filterData) {
        if (filterData.hasModifier(ModJoinTypeRight.class)) {
            return JoinType.RIGHT;
        } else if (filterData.hasModifier(ModJoinTypeLeft.class)) {
            return JoinType.LEFT;
        }
        return JoinType.INNER;
    }

    private static Join<?, ?> getOrCreateJoin(From<?, ?> from, String attribute, JoinType joinType) {
        for (Join<?, ?> join : from.getJoins()) {
            boolean sameName = join.getAttribute() != null && join.getAttribute().getName().equals(attribute);
            if (sameName && join.getJoinType().equals(joinType)) {
                return join;
            }
        }
        return from.join(attribute, joinType);
    }

    private static boolean isPluralAttribute(Join<?, ?> join) {
        return join.getAttribute() != null && join.getAttribute().isCollection();
    }

    private static ParsedPath parsePath(String path) {
        String[] segments = splitPath(path);
        if (segments.length == 0) {
            throw new IllegalStateException("Path cannot be empty");
        } else if (segments.length == 1) {
            return new ParsedPath(EMPTY_SEGMENTS, segments[0]);
        }
        return new ParsedPath(Arrays.copyOf(segments, segments.length - 1), segments[segments.length - 1]);
    }

    private static String[] splitPath(String path) {
        String nonNullPath = Objects.requireNonNull(path, "path cannot be null").trim();
        if (nonNullPath.isEmpty()) {
            return EMPTY_SEGMENTS;
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
                    throw new IllegalStateException("Invalid path segment on path '%s'".formatted(path));
                }
                segments[segmentIndex++] = nonNullPath.substring(start, i);
                start = i + 1;
            }
        }
        if (start == nonNullPath.length()) {
            throw new IllegalStateException("Invalid path segment on path '%s'".formatted(path));
        }
        segments[segmentIndex] = nonNullPath.substring(start);
        return segments;
    }

    private record ParsedPath(String[] associationSegments, String attributeSegment) {
    }

    public record ResolvedJpaPath<T>(Path<T> expression, boolean crossedPluralAssociation) {
    }

}
