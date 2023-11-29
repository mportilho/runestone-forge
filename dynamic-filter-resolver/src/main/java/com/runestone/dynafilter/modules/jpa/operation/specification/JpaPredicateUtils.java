package com.runestone.dynafilter.modules.jpa.operation.specification;

import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.modules.jpa.operation.modifiers.ModJoinTypeInner;
import com.runestone.dynafilter.modules.jpa.operation.modifiers.ModJoinTypeRight;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.Attribute;
import org.springframework.data.mapping.PropertyPath;

import java.util.function.BiFunction;

/**
 * Helper class for manipulating JPA Criteria objects
 *
 * @author Marcelo Portilho
 */
class JpaPredicateUtils {

    /**
     * Calls comparison methods of {@link CriteriaBuilder} on generic type objects
     */
    @SuppressWarnings({"unchecked"})
    public static Predicate toComparablePredicate(
            Expression<?> path, Object value,
            BiFunction<Expression<? extends Comparable<Object>>, Comparable<Object>, Predicate> comparablePredicateFunction,
            BiFunction<Expression<Number>, Number, Predicate> numberPredicateFunction) {
        if (value instanceof Number) {
            return numberPredicateFunction.apply((Expression<Number>) path, (Number) value);
        }
        return comparablePredicateFunction.apply((Expression<? extends Comparable<Object>>) path, (Comparable<Object>) value);
    }

    /**
     * Obtains the actual JPA Criteria {@link Path} object to the desired path
     * defined on {@link FilterData} object. Joins automatically when navigating through entities.
     */
    public static <T> Path<T> computeAttributePath(FilterData filterData, Root<?> root) {
        PropertyPath propertyPath = PropertyPath.from(filterData.path(), root.getJavaType());
        From<?, ?> from = root;

        while (propertyPath != null && propertyPath.hasNext()) {
            from = getOrCreateJoin(from, propertyPath.getSegment(), getJoinType(filterData));
            propertyPath = propertyPath.next();
        }
        if (propertyPath == null) {
            throw new IllegalStateException(String.format("No path '%s' found no type '%s'", filterData.path(), root.getJavaType().getCanonicalName()));
        }
        return from.get(propertyPath.getSegment());
    }

    /**
     * Obtains the actual JPA JoinType object to the desired path defined on {@link FilterData} object.
     *
     * @param filterData the {@link FilterData} object to get the join type from
     * @return will never be {@literal null}. Defaults to {@link JoinType#LEFT} if no join type is defined
     */
    private static JoinType getJoinType(FilterData filterData) {
        if (filterData.hasModifier(ModJoinTypeRight.class)) {
            return JoinType.RIGHT;
        } else if (filterData.hasModifier(ModJoinTypeInner.class)) {
            return JoinType.INNER;
        }
        return JoinType.LEFT;
    }

    /**
     * Returns an existing join for the given attribute if one already exists or
     * creates a new one if not.
     *
     * @param from      the {@link From} to get the current joins from.
     * @param attribute the {@link Attribute} to look for in the current joins.
     * @return will never be {@literal null}.
     */
    private static Join<?, ?> getOrCreateJoin(From<?, ?> from, String attribute, JoinType joinType) {
        for (Join<?, ?> join : from.getJoins()) {
            boolean sameName = join.getAttribute().getName().equals(attribute);
            if (sameName && join.getJoinType().equals(joinType)) {
                return join;
            }
        }
        return from.join(attribute, joinType);
    }

}