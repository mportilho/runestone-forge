package com.runestone.dynafilter.core.generator;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.model.statement.AbstractStatement;
import com.runestone.dynafilter.core.model.statement.CompoundStatement;
import com.runestone.dynafilter.core.model.statement.LogicalStatement;
import com.runestone.dynafilter.core.model.statement.NegatedStatement;
import com.runestone.dynafilter.core.model.statement.NoOpStatement;
import com.runestone.dynafilter.core.resolver.FilterDecorator;

import java.util.List;

/**
 * A complete filter: ordinary conditions and decorators, followed by mandatory expressions intersected by AND.
 * The restriction list is structurally isolated; its expressions, filter data, values and the wrapper remain shared.
 * Callers must not mutate shared trees, metadata, arrays, values or stateful collaborators while a derived statement
 * is in use, including between page-content and count queries.
 *
 * @param statementWrapper ordinary conditions and request/decorator metadata
 * @param filterDecorator ordinary decorators, never applied to mandatory expressions
 * @param restrictions ordered mandatory expressions, preserving each expression's grouping and negation
 */
public record ConditionalStatement(StatementWrapper statementWrapper, FilterDecorator<?> filterDecorator,
                                   List<AbstractStatement> restrictions) {

    public ConditionalStatement(StatementWrapper statementWrapper, FilterDecorator<?> filterDecorator) {
        this(statementWrapper, filterDecorator, List.of());
    }

    public ConditionalStatement {
        if (statementWrapper == null) {
            throw new IllegalArgumentException("Statement Wrapper cannot be null");
        }
        if (restrictions == null) {
            throw new DynamicFilterConfigurationException("Mandatory restrictions cannot be null");
        }
        for (AbstractStatement restriction : restrictions) {
            validateRestriction(restriction);
        }
        restrictions = List.copyOf(restrictions);
    }

    private static void validateRestriction(AbstractStatement statement) {
        if (statement == null || statement instanceof NoOpStatement) {
            throw new DynamicFilterConfigurationException("Mandatory restrictions cannot contain null or NoOpStatement nodes");
        }
        if (statement instanceof CompoundStatement compound) {
            if (compound.getLogicOperator() == null) {
                throw new DynamicFilterConfigurationException("Mandatory compound restrictions require a logic operator");
            }
            validateRestriction(compound.getLeftStatement());
            validateRestriction(compound.getRightStatement());
        } else if (statement instanceof NegatedStatement negated) {
            validateRestriction(negated.getStatement());
        } else if (statement instanceof LogicalStatement logical && logical.getFilterData() == null) {
            throw new DynamicFilterConfigurationException("Mandatory logical restrictions require filter data");
        }
    }
}
