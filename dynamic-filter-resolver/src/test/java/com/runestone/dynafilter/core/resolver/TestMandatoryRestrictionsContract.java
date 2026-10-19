package com.runestone.dynafilter.core.resolver;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.statement.AbstractStatement;
import com.runestone.dynafilter.core.model.statement.CompoundStatement;
import com.runestone.dynafilter.core.model.statement.LogicOperator;
import com.runestone.dynafilter.core.model.statement.LogicalStatement;
import com.runestone.dynafilter.core.model.statement.NegatedStatement;
import com.runestone.dynafilter.core.model.statement.NoOpStatement;
import com.runestone.dynafilter.core.model.statement.StatementAnalyser;
import com.runestone.dynafilter.core.model.statement.StatementVisitor;
import com.runestone.dynafilter.core.operation.types.Equals;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestMandatoryRestrictionsContract {

    private final StatementWrapper wrapper = new StatementWrapper(new NoOpStatement(), null, null, null);

    @Test
    void unsupportedBackendRejectsScopeButPreservesOldConstructionAndUnrestrictedResolution() {
        DynamicFilterResolver<String> resolver = new DynamicFilterResolver<>() {
            @Override
            @SuppressWarnings("unchecked")
            public <R extends String> R createFilter(StatementWrapper statementWrapper, FilterDecorator<String> decorator) {
                return (R) "ordinary";
            }
        };
        var unrestricted = new ConditionalStatement(wrapper, null);
        assertThat(unrestricted.restrictions()).isEmpty();
        assertThat(resolver.<String>createFilter(unrestricted)).isEqualTo("ordinary");

        var owner = new LogicalStatement(FilterData.of("owner", new String[]{"owner"}, String.class,
                Equals.class, new Object[]{"alice"}));
        var restricted = new ConditionalStatement(wrapper, null, List.of(owner));
        assertThatThrownBy(() -> resolver.createFilter(restricted))
                .isInstanceOf(DynamicFilterConfigurationException.class)
                .hasMessageContaining("restrictions");
    }

    @Test
    void isolatesRestrictionContainerWithoutCloningExpressionsOrChangingOrdinaryMetadata() {
        var owner = owner();
        var negated = new NegatedStatement(owner);
        List<AbstractStatement> supplied = new ArrayList<>(List.of(owner, negated));
        FilterDecorator<String> decorator = (filter, statement) -> filter;
        var statement = new ConditionalStatement(wrapper, decorator, supplied);
        supplied.clear();

        assertThat(statement.statementWrapper()).isSameAs(wrapper);
        assertThat(statement.filterDecorator()).isSameAs(decorator);
        assertThat(statement.restrictions()).containsExactly(owner, negated);
        assertThat(statement.restrictions().getFirst()).isSameAs(owner);
        assertThatThrownBy(() -> statement.restrictions().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThat(wrapper.decoratedFilters()).isEmpty();
        assertThat(wrapper.allFilters()).isEmpty();
    }

    @Test
    void directConstructionRejectsNoOpAtRootAndAnywhereInNativeMandatoryTrees() {
        AbstractStatement noOp = new NoOpStatement();
        for (var invalid : List.of(noOp, new NegatedStatement(noOp),
                new CompoundStatement(owner(), noOp, LogicOperator.CONJUNCTION),
                new CompoundStatement(noOp, owner(), LogicOperator.DISJUNCTION),
                new NegatedStatement(new CompoundStatement(owner(), noOp, LogicOperator.DISJUNCTION)))) {
            assertThatThrownBy(() -> new ConditionalStatement(wrapper, null, List.of(invalid)))
                    .isInstanceOf(DynamicFilterConfigurationException.class)
                    .hasMessageContaining("NoOpStatement");
        }
    }

    @Test
    void directConstructionRejectsMissingNativeOperandsAndRestrictionArguments() {
        List<AbstractStatement> withNull = new ArrayList<>();
        withNull.add(null);
        assertThatThrownBy(() -> new ConditionalStatement(wrapper, null, withNull))
                .isInstanceOf(DynamicFilterConfigurationException.class);
        assertThatThrownBy(() -> new ConditionalStatement(wrapper, null, null))
                .isInstanceOf(DynamicFilterConfigurationException.class);
        for (var invalid : List.of(new NegatedStatement(null), new LogicalStatement(null),
                new CompoundStatement(owner(), null, LogicOperator.CONJUNCTION),
                new CompoundStatement(owner(), owner(), null))) {
            assertThatThrownBy(() -> new ConditionalStatement(wrapper, null, List.of(invalid)))
                    .isInstanceOf(DynamicFilterConfigurationException.class);
        }
    }

    @Test
    void genericResolutionAppliesRestrictionsToTheDecoratedResultAndAcceptsOpaqueCustomStatements() {
        AbstractStatement custom = new AbstractStatement() {
            @Override
            public <R> R acceptAnalyser(StatementAnalyser<R> analyser) {
                return owner().acceptAnalyser(analyser);
            }

            @Override
            public void acceptVisitor(StatementVisitor visitor) {
                owner().acceptVisitor(visitor);
            }
        };
        var grouped = new CompoundStatement(owner(), new NegatedStatement(custom), LogicOperator.DISJUNCTION);
        DynamicFilterResolver<String> resolver = new DynamicFilterResolver<>() {
            @Override
            @SuppressWarnings("unchecked")
            public <R extends String> R createFilter(StatementWrapper wrapper, FilterDecorator<String> decorator) {
                return (R) decorator.decorate("ordinary", wrapper);
            }

            @Override
            @SuppressWarnings("unchecked")
            public <R extends String> R applyRestrictions(R filter, List<AbstractStatement> restrictions) {
                assertThat(restrictions).containsExactly(grouped, custom);
                return (R) (filter + " AND scope");
            }
        };
        FilterDecorator<String> decorator = (filter, ignored) -> "replacement";
        var statement = new ConditionalStatement(wrapper, decorator, List.of(grouped, custom));

        assertThat(resolver.<String>createFilter(statement)).isEqualTo("replacement AND scope");
    }

    private static LogicalStatement owner() {
        return new LogicalStatement(FilterData.of("owner", new String[]{"owner"}, String.class,
                Equals.class, new Object[]{"alice"}));
    }
}
