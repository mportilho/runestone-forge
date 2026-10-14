package com.runestone.dynafilter.modules.jpa.resolver;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.model.statement.AbstractStatement;
import com.runestone.dynafilter.core.model.statement.CompoundStatement;
import com.runestone.dynafilter.core.model.statement.NegatedStatement;
import com.runestone.dynafilter.core.model.statement.NoOpStatement;
import com.runestone.dynafilter.core.operation.FilterOperationService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

/** Guards observable mandatory results at every evaluated native operand, before JPA can omit null predicates. */
final class MandatorySpecificationStatementAnalyser extends SpecificationStatementAnalyser {

    MandatorySpecificationStatementAnalyser(FilterOperationService<Specification<?>> filterOperationService) {
        super(filterOperationService);
    }

    @SuppressWarnings("unchecked")
    <T> Specification<T> resolve(AbstractStatement statement) {
        Specification<T> specification = (Specification<T>) statement.acceptAnalyser(this);
        if (specification == null) {
            throw new DynamicFilterConfigurationException("Mandatory restriction returned an absent executable filter: "
                    + statement.getClass().getName());
        }
        return (root, query, builder) -> {
            Predicate predicate = specification.toPredicate(root, query, builder);
            if (predicate == null) {
                throw new DynamicFilterConfigurationException("Mandatory restriction returned an absent predicate: "
                        + statement.getClass().getName());
            }
            return predicate;
        };
    }

    @Override
    public Specification<?> analyseCompoundStatement(CompoundStatement statement) {
        Specification<Object> left = resolve(statement.getLeftStatement());
        Specification<Object> right = resolve(statement.getRightStatement());
        return statement.getLogicOperator().isConjunction() ? left.and(right) : left.or(right);
    }

    @Override
    public Specification<?> analyseNegationStatement(NegatedStatement statement) {
        return Specification.not(resolve(statement.getStatement()));
    }

    @Override
    public Specification<?> analyseNoOpStatement(NoOpStatement statement) {
        throw new DynamicFilterConfigurationException("Mandatory restrictions cannot contain NoOpStatement nodes");
    }
}
