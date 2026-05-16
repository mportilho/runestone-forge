package com.runestone.dynafilter.modules.jpa.operation.specification;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.modules.jpa.operation.ModJoinLeft;
import com.runestone.dynafilter.modules.jpa.operation.ModJoinRight;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class JpaPredicateUtils {

    private static final long DEFAULT_PATH_CACHE_SIZE = 4096L;
    private static final String PATH_CACHE_SIZE_PROPERTY = "runestone.dynafilter.jpa.path.cache.max-size";
    private static final Cache<String, List<String>> PATH_CACHE = Caffeine.newBuilder()
            .maximumSize(Long.getLong(PATH_CACHE_SIZE_PROPERTY, DEFAULT_PATH_CACHE_SIZE))
            .build();

    private JpaPredicateUtils() {
    }

    public static Path<?> computeAttributePath(FilterData filterData, Root<?> root) {
        Objects.requireNonNull(filterData, "filterData must not be null");
        Objects.requireNonNull(root, "root must not be null");
        List<String> segments = pathSegments(filterData.path());
        if (segments.size() == 1) {
            return root.get(segments.getFirst());
        }
        From<?, ?> current = root;
        JoinType joinType = joinType(filterData);
        for (int index = 0; index < segments.size() - 1; index++) {
            current = join(current, segments.get(index), joinType);
        }
        return current.get(segments.getLast());
    }

    public static Expression<?> computeAttributeJoinPath(FilterData filterData, Root<?> root) {
        Objects.requireNonNull(filterData, "filterData must not be null");
        Objects.requireNonNull(root, "root must not be null");
        List<String> segments = pathSegments(filterData.path());
        From<?, ?> current = root;
        JoinType joinType = joinType(filterData);
        for (String segment : segments) {
            current = join(current, segment, joinType);
        }
        return current;
    }

    public static JoinType joinType(FilterData filterData) {
        if (filterData.hasModifier(ModJoinLeft.class)) {
            return JoinType.LEFT;
        }
        if (filterData.hasModifier(ModJoinRight.class)) {
            return JoinType.RIGHT;
        }
        return JoinType.INNER;
    }

    private static List<String> pathSegments(String path) {
        return PATH_CACHE.get(path, key -> Arrays.stream(key.split("\\."))
                .filter(segment -> !segment.isBlank())
                .toList());
    }

    @SuppressWarnings("unchecked")
    private static <Z, X> Join<Z, X> join(From<?, ?> from, String attribute, JoinType joinType) {
        for (Join<?, ?> join : from.getJoins()) {
            if (join.getAttribute().getName().equals(attribute) && join.getJoinType().equals(joinType)) {
                return (Join<Z, X>) join;
            }
        }
        return ((From<Z, ?>) from).join(attribute, joinType);
    }
}
