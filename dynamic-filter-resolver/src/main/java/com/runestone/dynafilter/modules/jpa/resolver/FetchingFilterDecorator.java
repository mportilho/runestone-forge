package com.runestone.dynafilter.modules.jpa.resolver;

import com.runestone.assertions.Asserts;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.resolver.FilterDecorator;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.mapping.PropertyPath;

import java.util.Collection;

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
            query.distinct(true);
            for (Fetching fetching : fetches) {
                createJoinClause(root, fetching);
            }
            return null;
        };
        return filter != null ? ((Specification) decorated).and(filter) : decorated;
    }

    private static void createJoinClause(Root<Object> root, Fetching fetching) {
        for (String attributePath : fetching.value()) {
            From<?, ?> from = root;
            PropertyPath propertyPath = PropertyPath.from(attributePath, root.getJavaType());
            while (propertyPath != null && propertyPath.hasNext()) {
                from = (From<?, ?>) root.fetch(propertyPath.getSegment(), fetching.joinType());
                propertyPath = propertyPath.next();
            }
            if (propertyPath != null) {
                from.fetch(propertyPath.getSegment(), fetching.joinType());
            } else {
                throw new IllegalStateException(
                        String.format("Expected parsing to yield a PropertyPath from %s but got null!", attributePath));
            }
        }
    }

    public Collection<Fetching> getFetches() {
        return fetches;
    }
}
