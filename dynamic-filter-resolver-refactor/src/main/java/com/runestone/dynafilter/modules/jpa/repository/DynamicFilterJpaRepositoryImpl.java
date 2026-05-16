package com.runestone.dynafilter.modules.jpa.repository;

import com.runestone.dynafilter.core.DynamicFilterResolver;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.model.FilterRequestData;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DynamicFilterJpaRepositoryImpl<T, ID> extends SimpleJpaRepository<T, ID>
        implements DynamicFilterJpaRepository<T, ID> {

    private DynamicFilterResolver<Specification<?>> dynamicFilterResolver;

    public DynamicFilterJpaRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }

    public DynamicFilterJpaRepositoryImpl(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
    }

    public void setDynamicFilterResolver(DynamicFilterResolver<Specification<?>> dynamicFilterResolver) {
        this.dynamicFilterResolver = Objects.requireNonNull(dynamicFilterResolver, "dynamicFilterResolver must not be null");
    }

    @Override
    public List<T> findAll(ConditionalStatement conditionalStatement) {
        return findAll(convertToSpecification(conditionalStatement));
    }

    @Override
    public List<T> findAll(ConditionalStatement conditionalStatement, Sort sort) {
        return findAll(convertToSpecification(conditionalStatement), updateSortFilterPath(sort,
                conditionalStatement.statementWrapper().allFilters()));
    }

    @Override
    public Page<T> findAll(ConditionalStatement conditionalStatement, Pageable pageable) {
        Pageable translatedPageable = translatePageable(pageable, conditionalStatement.statementWrapper().allFilters());
        return findAll(convertToSpecification(conditionalStatement), translatedPageable);
    }

    @Override
    public long count(ConditionalStatement conditionalStatement) {
        return count(convertToSpecification(conditionalStatement));
    }

    @Override
    public boolean exists(ConditionalStatement conditionalStatement) {
        return exists(convertToSpecification(conditionalStatement));
    }

    public Specification<T> convertoToSpecification(ConditionalStatement conditionalStatement) {
        return convertToSpecification(conditionalStatement);
    }

    @SuppressWarnings("unchecked")
    public Specification<T> convertToSpecification(ConditionalStatement conditionalStatement) {
        Objects.requireNonNull(conditionalStatement, "conditionalStatement must not be null");
        if (dynamicFilterResolver == null) {
            throw new IllegalStateException("DynamicFilterResolver was not injected into DynamicFilterJpaRepositoryImpl");
        }
        return (Specification<T>) dynamicFilterResolver.createFilter(conditionalStatement);
    }

    public static Sort updateSortFilterPath(Sort sort, List<FilterRequestData> filterRequestData) {
        if (sort == null || sort.isUnsorted()) {
            return Sort.unsorted();
        }
        List<Sort.Order> translatedOrders = new ArrayList<>();
        for (Sort.Order order : sort) {
            translatedOrders.add(order.withProperty(pathFor(order.getProperty(), filterRequestData)));
        }
        return Sort.by(translatedOrders);
    }

    private static String pathFor(String property, List<FilterRequestData> filterRequestData) {
        if (filterRequestData == null) {
            return property;
        }
        for (FilterRequestData filter : filterRequestData) {
            String firstParameter = filter.parameters()[0];
            if (firstParameter.equals(property)) {
                return filter.path();
            }
        }
        return property;
    }

    private static Pageable translatePageable(Pageable pageable, List<FilterRequestData> filterRequestData) {
        if (pageable == null || pageable.isUnpaged()) {
            return Pageable.unpaged();
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), updateSortFilterPath(pageable.getSort(), filterRequestData));
    }
}
