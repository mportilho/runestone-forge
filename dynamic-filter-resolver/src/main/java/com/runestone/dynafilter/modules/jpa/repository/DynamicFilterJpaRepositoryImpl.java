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

import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.exceptions.InvalidSortException;
import com.runestone.dynafilter.modules.jpa.api.JpaSortPolicy;
import com.runestone.dynafilter.modules.jpa.support.JpaSortCatalog;
import com.runestone.dynafilter.core.model.FilterRequestData;
import com.runestone.dynafilter.core.resolver.DynamicFilterResolver;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Nulls;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.query.FluentQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Implementation of {@link DynamicFilterJpaRepository} that extends {@link SimpleJpaRepository}
 *
 * @param <T> Entity type
 * @param <I> Entity ID type
 */
public class DynamicFilterJpaRepositoryImpl<T, I> extends SimpleJpaRepository<T, I> implements DynamicFilterJpaRepository<T, I> {

    private final EntityManager em;
    private DynamicFilterResolver<Specification<T>> dynamicFilterResolver;
    private JpaSortPolicy sortPolicy = JpaSortPolicy.DECLARED;
    private final JpaSortCatalog sortCatalog;

    public DynamicFilterJpaRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager em) {
        super(entityInformation, em);
        this.em = em;
        this.sortCatalog = new JpaSortCatalog(em.getEntityManagerFactory());
        this.dynamicFilterResolver = null;
    }

    @Override
    public Optional<T> findOne(ConditionalStatement conditionalStatement) {
        return findOne(dynamicFilterResolver.<Specification<T>>createFilter(conditionalStatement));
    }

    @Override
    public List<T> findAll(ConditionalStatement conditionalStatement) {
        return findAll(dynamicFilterResolver.<Specification<T>>createFilter(conditionalStatement));
    }

    @Override
    public Page<T> findAll(ConditionalStatement conditionalStatement, Pageable pageable) {
        Pageable translated = updatePageableFilterPath(conditionalStatement, pageable);
        Specification<T> spec = dynamicFilterResolver.createFilter(conditionalStatement);
        if (translated.isUnpaged()) {
            List<T> content = getSortedQuery(spec, translated.getSort()).getResultList();
            return new PageImpl<>(content, translated, content.size());
        }
        return readPage(getSortedQuery(spec, translated.getSort()), getDomainClass(), translated, spec);
    }

    @Override
    public List<T> findAll(ConditionalStatement conditionalStatement, Sort sort) {
        Sort translated = updateSortFilterPath(conditionalStatement, sort);
        return getSortedQuery(dynamicFilterResolver.<Specification<T>>createFilter(conditionalStatement), translated).getResultList();
    }

    @Override
    public List<T> findAll(ConditionalStatement conditionalStatement, EntityGraph.EntityGraphType entityGraphType, String entityGraphName) {
        return findAll(conditionalStatement, Sort.unsorted(), entityGraphType, entityGraphName);
    }

    @Override
    public Page<T> findAll(ConditionalStatement conditionalStatement, Pageable pageable, EntityGraph.EntityGraphType entityGraphType, String entityGraphName) {
        Pageable updatedPageable = updatePageableFilterPath(conditionalStatement, pageable);
        Specification<T> spec = dynamicFilterResolver.createFilter(conditionalStatement);
        TypedQuery<T> query = getSortedQuery(spec, updatedPageable.getSort());
        query.setHint(entityGraphType.getKey(), em.getEntityGraph(entityGraphName));
        if (updatedPageable.isUnpaged()) {
            List<T> content = query.getResultList();
            return new PageImpl<>(content, updatedPageable, content.size());
        }
        return readPage(query, getDomainClass(), updatedPageable, spec);
    }

    @Override
    public Page<T> findAll(ConditionalStatement conditionalStatement, Pageable pageable, String entityGraphName) {
        return findAll(conditionalStatement, pageable, EntityGraph.EntityGraphType.FETCH, entityGraphName);
    }

    @Override
    public List<T> findAll(ConditionalStatement conditionalStatement, Sort sort, EntityGraph.EntityGraphType entityGraphType, String entityGraphName) {
        Sort translated = updateSortFilterPath(conditionalStatement, sort);
        Specification<T> spec = dynamicFilterResolver.createFilter(conditionalStatement);
        TypedQuery<T> query = getSortedQuery(spec, translated);
        query.setHint(entityGraphType.getKey(), em.getEntityGraph(entityGraphName));
        return query.getResultList();
    }

    @Override
    public List<T> findAll(ConditionalStatement conditionalStatement, Sort sort, String entityGraphName) {
        return findAll(conditionalStatement, sort, EntityGraph.EntityGraphType.FETCH, entityGraphName);
    }

    @Override
    public T findOne(ConditionalStatement conditionalStatement, EntityGraph.EntityGraphType entityGraphType, String entityGraphName) {
        Specification<T> spec = dynamicFilterResolver.createFilter(conditionalStatement);
        TypedQuery<T> query = getQuery(spec, Sort.unsorted());
        query.setHint(entityGraphType.getKey(), em.getEntityGraph(entityGraphName));
        return query.getSingleResult();
    }

    @Override
    public long count(ConditionalStatement conditionalStatement) {
        return count(dynamicFilterResolver.<Specification<T>>createFilter(conditionalStatement));
    }

    @Override
    public boolean exists(ConditionalStatement conditionalStatement) {
        return exists(dynamicFilterResolver.<Specification<T>>createFilter(conditionalStatement));
    }

    @Override
    public <S extends T, R> R findBy(ConditionalStatement conditionalStatement, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return findBy(dynamicFilterResolver.<Specification<T>>createFilter(conditionalStatement), queryFunction);
    }

    @Override
    public Specification<T> convertoToSpecification(ConditionalStatement conditionalStatement) {
        return dynamicFilterResolver.createFilter(conditionalStatement);
    }

    @Override
    public void setDynamicFilterResolver(DynamicFilterResolver<Specification<T>> dynamicFilterResolver) {
        this.dynamicFilterResolver = dynamicFilterResolver;
    }

    private TypedQuery<T> getSortedQuery(Specification<T> specification, Sort sort) {
        if (sortPolicy != JpaSortPolicy.DECLARED || sort.isUnsorted()) {
            return getQuery(specification, sort);
        }
        // Spring Data's PropertyPath follows Java backing fields. Validated JPA paths must
        // instead follow the persistent members, including getters with different field types.
        Specification<T> ordered = (root, query, builder) -> {
            var predicate = specification != null ? specification.toPredicate(root, query, builder) : null;
            List<jakarta.persistence.criteria.Order> orders = new ArrayList<>();
            for (Sort.Order order : sort) {
                Expression<?> expression = sortExpression(root, order.getProperty());
                if (order.isIgnoreCase() && expression.getJavaType() == String.class) {
                    expression = builder.lower(expression.as(String.class));
                }
                Nulls nulls = switch (order.getNullHandling()) {
                    case NATIVE -> Nulls.NONE;
                    case NULLS_FIRST -> Nulls.FIRST;
                    case NULLS_LAST -> Nulls.LAST;
                };
                orders.add(order.isAscending() ? builder.asc(expression, nulls) : builder.desc(expression, nulls));
            }
            query.orderBy(orders);
            return predicate;
        };
        // Only the content query uses this specification; count retains the original one.
        return getQuery(ordered, Sort.unsorted());
    }

    private static Expression<?> sortExpression(From<?, ?> root, String path) {
        String[] segments = path.split("\\.", -1);
        From<?, ?> current = root;
        for (int i = 0; i < segments.length - 1; i++) {
            String segment = segments[i];
            From<?, ?> next = null;
            for (var fetch : current.getFetches()) {
                if (fetch instanceof From<?, ?> fetched && fetch.getAttribute().getName().equals(segment)
                        && !(fetched instanceof Join<?, ?> joined && joined.getOn() != null)) {
                    next = fetched;
                    break;
                }
            }
            if (next == null) {
                for (var join : current.getJoins()) {
                    // An ON condition belongs to the predicate, not the persistent sort destination.
                    if (join.getAttribute().getName().equals(segment) && join.getOn() == null) {
                        next = join;
                        break;
                    }
                }
            }
            current = next != null ? next : current.join(segment, JoinType.LEFT);
        }
        return current.get(segments[segments.length - 1]);
    }

    private Pageable updatePageableFilterPath(ConditionalStatement conditionalStatement, Pageable pageable) {
        if (!pageable.getSort().isSorted()) {
            return pageable;
        }
        Sort translated = updateSortFilterPath(conditionalStatement, pageable.getSort());
        return pageable.isUnpaged() ? Pageable.unpaged(translated)
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), translated);
    }

    @Override
    public void setSortPolicy(JpaSortPolicy policy) {
        this.sortPolicy = java.util.Objects.requireNonNull(policy, "sort policy is required");
    }

    private Sort updateSortFilterPath(ConditionalStatement conditionalStatement, Sort sort) {
        if (!sort.isSorted()) {
            return sort;
        }
        if (sortPolicy == JpaSortPolicy.DECLARED) {
            Map<String, String> catalog = sortCatalog.resolve(conditionalStatement.statementWrapper().sortContract(), getDomainClass());
            List<String> rejected = new ArrayList<>();
            List<Sort.Order> translated = new ArrayList<>();
            for (Sort.Order order : sort) {
                String path = catalog.get(order.getProperty());
                if (path == null || order instanceof JpaSort.JpaOrder jpaOrder && jpaOrder.isUnsafe()) {
                    rejected.add(order.getProperty());
                } else {
                    translated.add(order.withProperty(path));
                }
            }
            if (!rejected.isEmpty()) {
                throw new InvalidSortException(rejected);
            }
            return Sort.by(translated);
        }
        return updateSortFilterPath(sort, conditionalStatement.statementWrapper().allFilters());
    }

    static Sort updateSortFilterPath(Sort sort, List<FilterRequestData> filters) {
        if (!sort.isSorted() || filters == null || filters.isEmpty()) {
            return sort;
        }

        Map<String, String> parameterPathMap = new HashMap<>(Math.max(16, filters.size()));
        for (FilterRequestData filter : filters) {
            if (filter.parameterCount() == 0) {
                continue;
            }

            String parameter = filter.parameterAt(0);
            if (parameter == null || parameterPathMap.containsKey(parameter)) {
                continue;
            }

            String path = filter.pathAt(0);
            if (path != null && !parameter.equals(path)) {
                parameterPathMap.put(parameter, path);
            }
        }

        if (parameterPathMap.isEmpty()) {
            return sort;
        }

        boolean changed = false;
        List<Sort.Order> translatedOrders = new ArrayList<>();
        for (Sort.Order order : sort) {
            String translatedPath = parameterPathMap.get(order.getProperty());
            if (translatedPath != null) {
                translatedOrders.add(order.withProperty(translatedPath));
                changed = true;
            } else {
                translatedOrders.add(order);
            }
        }

        return changed ? Sort.by(translatedOrders) : sort;
    }
}
