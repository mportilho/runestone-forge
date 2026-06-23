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
import com.runestone.dynafilter.core.resolver.DynamicFilterResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.FluentQuery;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Interface to extend JPA repositories with dynamic filter capabilities.
 *
 * @param <T> Entity type
 * @param <I> Entity ID type
 */
@NoRepositoryBean
public interface DynamicFilterJpaRepository<T, I> extends JpaRepository<T, I>, JpaSpecificationExecutor<T> {

    /**
     * Find one entity by the given conditional statement.
     *
     * @param conditionalStatement Conditional statement
     * @return Optional of entity
     */
    Optional<T> findOne(ConditionalStatement conditionalStatement);

    /**
     * Find all entities by the given conditional statement.
     *
     * @param conditionalStatement Conditional statement
     * @return List of entities
     */
    List<T> findAll(ConditionalStatement conditionalStatement);

    /**
     * Find all entities by the given conditional statement and pageable.
     *
     * @param conditionalStatement Conditional statement
     * @param pageable             Pageable
     * @return Page of entities
     */
    Page<T> findAll(ConditionalStatement conditionalStatement, Pageable pageable);

    /**
     * Find all entities by the given conditional statement and sort.
     *
     * @param conditionalStatement Conditional statement
     * @param sort                 Sort
     * @return List of entities
     */
    List<T> findAll(ConditionalStatement conditionalStatement, Sort sort);

    /**
     * Find all entities by the given conditional statement and entity graph defined on the entity.
     *
     * @param conditionalStatement Conditional statement
     * @param entityGraphType      Entity graph type
     * @param entityGraphName      Entity graph name
     * @return List of entities
     */
    List<T> findAll(ConditionalStatement conditionalStatement, EntityGraph.EntityGraphType entityGraphType, String entityGraphName);

    /**
     * Find all entities by the given conditional statement, pageable and entity graph defined on the entity.
     *
     * @param conditionalStatement Conditional statement
     * @param pageable             Pageable
     * @param entityGraphType      Entity graph type
     * @param entityGraphName      Entity graph name
     * @return Page of entities
     */
    Page<T> findAll(ConditionalStatement conditionalStatement, Pageable pageable, EntityGraph.EntityGraphType entityGraphType, String entityGraphName);

    /**
     * Find all entities by the given conditional statement, pageable and entity graph defined on the entity. The default entityGraphType
     * used on this method is EntityGraphType.FETCH.
     *
     * @param conditionalStatement Conditional statement
     * @param pageable             Pageable
     * @param entityGraphName      Entity graph name
     * @return Page of entities
     */
    Page<T> findAll(ConditionalStatement conditionalStatement, Pageable pageable, String entityGraphName);

    /**
     * Find all entities by the given conditional statement, sort and entity graph defined on the entity. The default entityGraphType
     * used on this method is EntityGraphType.FETCH.
     *
     * @param conditionalStatement Conditional statement
     * @param sort                 Sort
     * @param entityGraphName      Entity graph name
     * @return List of entities
     */
    List<T> findAll(ConditionalStatement conditionalStatement, Sort sort, String entityGraphName);

    /**
     * Find all entities by the given conditional statement, sort and entity graph defined on the entity.
     *
     * @param conditionalStatement Conditional statement
     * @param sort                 Sort
     * @param entityGraphType      Entity graph type
     * @param entityGraphName      Entity graph name
     * @return List of entities
     */
    List<T> findAll(ConditionalStatement conditionalStatement, Sort sort, EntityGraph.EntityGraphType entityGraphType, String entityGraphName);

    /**
     * Find one entity by the given conditional statement and entity graph defined on the entity.
     *
     * @param conditionalStatement Conditional statement
     * @param entityGraphType      Entity graph type
     * @param entityGraphName      Entity graph name
     * @return Entity
     */
    T findOne(ConditionalStatement conditionalStatement, EntityGraph.EntityGraphType entityGraphType, String entityGraphName);

    /**
     * Count entities by the given conditional statement.
     *
     * @param conditionalStatement Conditional statement
     * @return Number of entities
     */
    long count(ConditionalStatement conditionalStatement);

    /**
     * Check if an entity exists by the given conditional statement.
     *
     * @param conditionalStatement Conditional statement
     * @return True if exists, false otherwise
     */
    boolean exists(ConditionalStatement conditionalStatement);

    /**
     * Find by the given conditional statement and query function.
     *
     * @param conditionalStatement Conditional statement
     * @param queryFunction        Query function
     * @param <S>                  Entity type
     * @param <R>                  Result type
     * @return Result of the query function
     */
    <S extends T, R> R findBy(ConditionalStatement conditionalStatement, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction);

    /**
     * Convert a conditional statement to a JPA specification of the current repository entity type.
     *
     * @param conditionalStatement Conditional statement
     * @return JPA specification
     */
    Specification<T> convertoToSpecification(ConditionalStatement conditionalStatement);

    /**
     * Set the dynamic filter resolver to be used by the repository.
     *
     * <p>For internal use only.
     *
     * @param dynamicFilterResolver Dynamic filter resolver
     */
    void setDynamicFilterResolver(DynamicFilterResolver<Specification<T>> dynamicFilterResolver);

}
