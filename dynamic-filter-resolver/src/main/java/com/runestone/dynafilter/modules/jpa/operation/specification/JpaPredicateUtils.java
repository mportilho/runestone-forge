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

package com.runestone.dynafilter.modules.jpa.operation.specification;

import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.modules.jpa.operation.modifiers.ModJoinTypeLeft;
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
     * @return will never be {@literal null}. Defaults to {@link JoinType#INNER} if no join type is defined
     */
    private static JoinType getJoinType(FilterData filterData) {
        if (filterData.hasModifier(ModJoinTypeRight.class)) {
            return JoinType.RIGHT;
        } else if (filterData.hasModifier(ModJoinTypeLeft.class)) {
            return JoinType.LEFT;
        }
        return JoinType.INNER;
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