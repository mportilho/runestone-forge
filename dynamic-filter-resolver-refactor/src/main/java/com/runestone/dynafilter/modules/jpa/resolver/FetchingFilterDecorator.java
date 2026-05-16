package com.runestone.dynafilter.modules.jpa.resolver;

import com.runestone.dynafilter.core.decorator.FilterDecorator;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.modules.jpa.operation.ModJoinLeft;
import com.runestone.dynafilter.modules.jpa.operation.ModJoinRight;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.FetchParent;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class FetchingFilterDecorator implements FilterDecorator<Specification<?>> {

    private final List<Fetching> fetches;

    public FetchingFilterDecorator(List<Fetching> fetches) {
        this.fetches = fetches == null ? List.of() : List.copyOf(fetches);
    }

    @Override
    public Specification<?> decorate(Specification<?> filter, StatementWrapper statementWrapper) {
        Objects.requireNonNull(filter, "filter must not be null");
        return (root, query, criteriaBuilder) -> {
            if (!isCountQuery(query.getResultType())) {
                Set<String> visited = new LinkedHashSet<>();
                for (Fetching fetching : fetches) {
                    if (visited.add(fetching.path())) {
                        fetch(root, fetching.path(), joinType(fetching));
                    }
                }
                if (!fetches.isEmpty()) {
                    query.distinct(true);
                }
            }
            return cast(filter).toPredicate(root, query, criteriaBuilder);
        };
    }

    @SuppressWarnings("unchecked")
    private static Specification<Object> cast(Specification<?> specification) {
        return (Specification<Object>) specification;
    }

    private static boolean isCountQuery(Class<?> resultType) {
        return resultType == Long.class || resultType == long.class;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void fetch(FetchParent parent, String path, JoinType joinType) {
        FetchParent current = parent;
        for (String segment : path.split("\\.")) {
            if (segment.isBlank()) {
                throw new IllegalArgumentException("Fetch path contains a blank segment: " + path);
            }
            Fetch existing = existingFetch(current, segment);
            current = existing == null ? current.fetch(segment, joinType) : existing;
        }
    }

    @SuppressWarnings("rawtypes")
    private static Fetch existingFetch(FetchParent parent, String attribute) {
        for (Object fetch : parent.getFetches()) {
            Fetch<?, ?> candidate = (Fetch<?, ?>) fetch;
            if (candidate.getAttribute().getName().equals(attribute)) {
                return candidate;
            }
        }
        return null;
    }

    private static JoinType joinType(Fetching fetching) {
        if (fetching.joinType().equals(ModJoinLeft.class)) {
            return JoinType.LEFT;
        }
        if (fetching.joinType().equals(ModJoinRight.class)) {
            return JoinType.RIGHT;
        }
        return JoinType.INNER;
    }
}
