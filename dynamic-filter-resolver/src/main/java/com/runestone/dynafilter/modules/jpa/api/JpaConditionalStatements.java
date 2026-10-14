package com.runestone.dynafilter.modules.jpa.api;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.modules.jpa.resolver.SpecificationStatementAnalyser;
import org.springframework.data.jpa.domain.Specification;

/** JPA-specific composition of complete conditional statements, without a Spring Data dependency in the core. */
public final class JpaConditionalStatements {

    private JpaConditionalStatements() {
    }

    /**
     * Appends an opaque JPA restriction by AND after all ordinary decorators.
     * Existing filters, metadata, decorators and restrictions are preserved without mutating {@code original}.
     * Each call creates a new complete statement through the core builder; existing collaborators remain shared.
     * Restrictions accumulate without replacement or deduplication. No filter or sorting aliases are registered.
     *
     * <p>The restriction is supported by {@link SpecificationStatementAnalyser} and its subclasses. When the
     * complete statement is resolved/executed, incompatible backends and null mandatory predicates raise
     * {@link DynamicFilterConfigurationException}. Explicit true and false predicates are valid. Only the returned
     * predicate is checked: null operands hidden inside a composed specification cannot be inspected.</p>
     *
     * <p>The specification must be re-executable, preserve root cardinality and not modify the outer query's
     * selection, WHERE, ordering, grouping, HAVING, distinctness or fetches. Predicate construction and correlated
     * subqueries are supported; EXISTS is recommended for plural relationships. These obligations are contractual,
     * not sandboxed or deeply validated. Fetching and query shaping belong outside the restriction.</p>
     *
     * <p>JPA invokes the specification with the current content root/query and again with separate count objects
     * when count is actually executed. Do not cache roots, predicates or subqueries, mutate captured identity data
     * between executions, or capture shared mutable request state. The library does not copy or freeze the supplied
     * specification, discover subresource ownership, or verify compatibility with the repository entity.</p>
     *
     * <p>Pass the returned complete statement to the repository. Wrapper-only resolution does not carry restrictions;
     * this opt-in helper is not an automatic repository authorization boundary. The opaque restriction exposes no
     * native filter paths or parameters to statement visitors.</p>
     *
     * @param original the complete statement to restrict
     * @param restriction the application-supplied scope for the repository entity
     * @param <T> the restriction's entity type; this does not establish compatibility with the repository
     * @return a new statement with the additional mandatory restriction
     * @throws NullPointerException if either argument is null
     */
    public static <T> ConditionalStatement restrict(ConditionalStatement original, Specification<T> restriction) {
        return ConditionalStatement.builder(original)
                .restrict(new JpaSpecificationRestriction(restriction))
                .build();
    }
}
