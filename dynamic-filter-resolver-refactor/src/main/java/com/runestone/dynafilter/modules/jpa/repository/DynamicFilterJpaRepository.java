package com.runestone.dynafilter.modules.jpa.repository;

import com.runestone.dynafilter.core.generator.ConditionalStatement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface DynamicFilterJpaRepository<T, ID> extends JpaRepository<T, ID> {

    List<T> findAll(ConditionalStatement conditionalStatement);

    List<T> findAll(ConditionalStatement conditionalStatement, Sort sort);

    Page<T> findAll(ConditionalStatement conditionalStatement, Pageable pageable);

    long count(ConditionalStatement conditionalStatement);

    boolean exists(ConditionalStatement conditionalStatement);
}
