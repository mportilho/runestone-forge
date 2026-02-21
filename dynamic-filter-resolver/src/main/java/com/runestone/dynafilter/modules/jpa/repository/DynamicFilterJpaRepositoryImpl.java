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
import com.runestone.dynafilter.core.model.FilterRequestData;
import com.runestone.dynafilter.core.resolver.DynamicFilterResolver;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

    public DynamicFilterJpaRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager em) {
        super(entityInformation, em);
        this.em = em;
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
        return findAll(dynamicFilterResolver.<Specification<T>>createFilter(conditionalStatement), updatePageableFilterPath(conditionalStatement, pageable));
    }

    @Override
    public List<T> findAll(ConditionalStatement conditionalStatement, Sort sort) {
        return findAll(dynamicFilterResolver.<Specification<T>>createFilter(conditionalStatement), updateSortFilterPath(conditionalStatement, sort));
    }

    @Override
    public List<T> findAll(ConditionalStatement conditionalStatement, EntityGraph.EntityGraphType entityGraphType, String entityGraphName) {
        return findAll(conditionalStatement, Sort.unsorted(), entityGraphType, entityGraphName);
    }

    @Override
    public Page<T> findAll(ConditionalStatement conditionalStatement, Pageable pageable, EntityGraph.EntityGraphType entityGraphType, String entityGraphName) {
        Pageable updatedPageable = updatePageableFilterPath(conditionalStatement, pageable);
        Specification<T> spec = dynamicFilterResolver.createFilter(conditionalStatement);
        TypedQuery<T> query = getQuery(spec, updatedPageable.getSort());
        query.setHint(entityGraphType.getKey(), em.getEntityGraph(entityGraphName));
        return readPage(query, getDomainClass(), updatedPageable, spec);
    }

    @Override
    public Page<T> findAll(ConditionalStatement conditionalStatement, Pageable pageable, String entityGraphName) {
        return findAll(conditionalStatement, updatePageableFilterPath(conditionalStatement, pageable), EntityGraph.EntityGraphType.FETCH, entityGraphName);
    }

    @Override
    public List<T> findAll(ConditionalStatement conditionalStatement, Sort sort, EntityGraph.EntityGraphType entityGraphType, String entityGraphName) {
        Specification<T> spec = dynamicFilterResolver.createFilter(conditionalStatement);
        TypedQuery<T> query = getQuery(spec, updateSortFilterPath(conditionalStatement, sort));
        query.setHint(entityGraphType.getKey(), em.getEntityGraph(entityGraphName));
        return query.getResultList();
    }

    @Override
    public List<T> findAll(ConditionalStatement conditionalStatement, Sort sort, String entityGraphName) {
        return findAll(conditionalStatement, updateSortFilterPath(conditionalStatement, sort), EntityGraph.EntityGraphType.FETCH, entityGraphName);
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
    public long delete(ConditionalStatement conditionalStatement) {
        return delete(dynamicFilterResolver.<Specification<T>>createFilter(conditionalStatement));
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

    private Pageable updatePageableFilterPath(ConditionalStatement conditionalStatement, Pageable pageable) {
        if (!pageable.getSort().isSorted()) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), updateSortFilterPath(conditionalStatement, pageable.getSort()));
    }

    private Sort updateSortFilterPath(ConditionalStatement conditionalStatement, Sort sort) {
        if (!sort.isSorted()) {
            return sort;
        }
        return updateSortFilterPath(sort, conditionalStatement.statementWrapper().allFilters());
    }

    static Sort updateSortFilterPath(Sort sort, List<FilterRequestData> filters) {
        if (!sort.isSorted() || filters == null || filters.isEmpty()) {
            return sort;
        }

        Map<String, String> parameterPathMap = new HashMap<>(Math.max(16, filters.size()));
        for (FilterRequestData filter : filters) {
            String[] parameters = filter.parameters();
            if (parameters == null || parameters.length == 0) {
                continue;
            }

            String parameter = parameters[0];
            if (parameter == null || parameterPathMap.containsKey(parameter)) {
                continue;
            }

            String path = filter.path();
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
