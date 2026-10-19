package com.runestone.dynafilter.modules.jpa.api;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.FilterRequestData;
import com.runestone.dynafilter.core.model.statement.LogicalStatement;
import com.runestone.dynafilter.core.model.statement.StatementAnalyser;
import com.runestone.dynafilter.core.model.statement.StatementVisitor;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.resolver.DynamicFilterResolver;
import com.runestone.dynafilter.core.resolver.FilterDecorator;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class TestJpaConditionalStatements {

    @Test
    void appendsRestrictionWithoutChangingSourceFiltersMetadataDecoratorsOrPreviousRestrictions() {
        var ordinary = equal("name", "search", "alice");
        var previous = equal("name", "existingScope", "alice");
        var decorated = FilterData.of("name", new String[]{"decoration"}, String.class, Equals.class, new Object[]{"alice"});
        var alias = new FilterRequestData(new String[]{"name"}, new String[]{"search"}, String.class,
                Equals.class, "false", null, null, null, false, false, List.of(), null);
        var wrapper = new StatementWrapper(ordinary, Map.of("name", decorated), List.of(alias), null);
        FilterDecorator<Specification<?>> decorator = (filter, metadata) -> filter;
        var source = new ConditionalStatement(wrapper, decorator, List.of(previous));
        Specification<Object> scope = (root, query, builder) -> builder.conjunction();

        var restricted = JpaConditionalStatements.restrict(source, scope);

        assertThat(restricted.statementWrapper().statement()).isEqualTo(ordinary);
        assertThat(restricted.statementWrapper().decoratedFilters()).containsExactlyEntriesOf(Map.of("name", decorated));
        assertThat(restricted.statementWrapper().allFilters()).containsExactly(alias);
        assertThat(restricted.filterDecorator()).isEqualTo(decorator);
        assertThat(restricted.restrictions()).hasSize(2).startsWith(previous);
        assertThat(source.restrictions()).containsExactly(previous);
        assertThat(source.statementWrapper()).isEqualTo(wrapper);
        assertThat(decorated.values()).containsExactly("alice");
    }

    @Test
    @SuppressWarnings("unchecked")
    void rejectsIndependentAnalysersEvenWhenTheirDeclaredResultTypeIsSpecification() {
        var source = ConditionalStatement.builder().build();
        var restricted = JpaConditionalStatements.restrict(source, Specification.<Object>unrestricted());
        StatementAnalyser<Specification<?>> incompatible = mock(StatementAnalyser.class);

        assertThatThrownBy(() -> restricted.restrictions().getLast().acceptAnalyser(incompatible))
                .isInstanceOf(DynamicFilterConfigurationException.class)
                .hasMessageContaining("SpecificationStatementAnalyser");
        verifyNoInteractions(incompatible);
    }

    @Test
    void unsupportedBackendRejectsTheCompleteHelperProducedStatementInsteadOfDroppingScope() {
        DynamicFilterResolver<String> backend = new DynamicFilterResolver<>() {
            @Override
            @SuppressWarnings("unchecked")
            public <R extends String> R createFilter(StatementWrapper wrapper, FilterDecorator<String> decorator) {
                return (R) "ordinary";
            }
        };
        var source = ConditionalStatement.builder().build();
        var restricted = JpaConditionalStatements.restrict(source,
                (root, query, builder) -> builder.conjunction());

        assertThat(backend.<String>createFilter(source)).isEqualTo("ordinary");
        assertThatThrownBy(() -> backend.createFilter(restricted))
                .isInstanceOf(DynamicFilterConfigurationException.class)
                .hasMessageContaining("does not support mandatory restrictions");
    }

    @Test
    void rejectsNullArgumentsInsteadOfTreatingThemAsOptionalScope() {
        var source = ConditionalStatement.builder().build();

        assertThatThrownBy(() -> JpaConditionalStatements.restrict(null, Specification.unrestricted()))
                .isInstanceOf(NullPointerException.class).hasMessageContaining("Source statement");
        assertThatThrownBy(() -> JpaConditionalStatements.restrict(source, null))
                .isInstanceOf(NullPointerException.class).hasMessageContaining("Restriction specification");
        assertThat(source.restrictions()).isEmpty();
    }

    @Test
    void opaqueRestrictionsDoNotExposeNativeFilterPathsOrParametersToVisitors() {
        var restricted = JpaConditionalStatements.restrict(ConditionalStatement.builder().build(),
                (root, query, builder) -> builder.conjunction());
        var visitor = mock(StatementVisitor.class);

        restricted.restrictions().getLast().acceptVisitor(visitor);

        verifyNoInteractions(visitor);
        assertThat(restricted.statementWrapper().allFilters()).isEmpty();
        assertThat(restricted.statementWrapper().decoratedFilters()).isEmpty();
    }

    private static LogicalStatement equal(String path, String parameter, String value) {
        return new LogicalStatement(FilterData.of(path, new String[]{parameter}, String.class, Equals.class, new Object[]{value}));
    }
}
