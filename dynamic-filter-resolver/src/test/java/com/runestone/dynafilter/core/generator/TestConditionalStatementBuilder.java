package com.runestone.dynafilter.core.generator;

import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.FilterRequestData;
import com.runestone.dynafilter.core.model.statement.AbstractStatement;
import com.runestone.dynafilter.core.model.statement.CompoundStatement;
import com.runestone.dynafilter.core.model.statement.LogicOperator;
import com.runestone.dynafilter.core.model.statement.LogicalStatement;
import com.runestone.dynafilter.core.model.statement.NegatedStatement;
import com.runestone.dynafilter.core.model.statement.NoOpStatement;
import com.runestone.dynafilter.core.model.statement.StatementAnalyser;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.resolver.DynamicFilterResolver;
import com.runestone.dynafilter.core.resolver.FilterDecorator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestConditionalStatementBuilder {

    @Test
    void buildsEmptyStatementsAndGroupsOrdinaryChainsFromLeftToRight() {
        var builder = ConditionalStatement.builder();
        var empty = builder.build();
        assertThat(empty.restrictions()).isEmpty();
        assertThat(empty.statementWrapper().allFilters()).isEmpty();
        assertThat(empty.statementWrapper().decoratedFilters()).isEmpty();
        assertThat(ordinaryMatches(empty, Map.of())).isTrue();

        var chained = builder.or(filter("a", true)).or(filter("b", true)).and(filter("c", false)).build();
        assertThat(ordinaryMatches(chained, Map.of("a", true, "b", false, "c", true))).isFalse();
        assertThat(ordinaryMatches(chained, Map.of("a", false, "b", true, "c", false))).isTrue();
        assertThat(ordinaryMatches(empty, Map.of("a", false, "b", false, "c", true))).isTrue();

        var grouped = new CompoundStatement(new LogicalStatement(filter("b", true)),
                new LogicalStatement(filter("c", false)), LogicOperator.CONJUNCTION);
        var explicit = ConditionalStatement.builder().and(filter("a", true)).or(grouped).build();
        assertThat(ordinaryMatches(explicit, Map.of("a", true, "b", false, "c", true))).isTrue();
        assertThat(ordinaryMatches(explicit, Map.of("a", false, "b", true, "c", true))).isFalse();
    }

    @Test
    void rebuildingPreservesSourceAndAccumulatesRestrictionsWithoutChangingPreviousBuilds() {
        var data = filter("a", true);
        var grouped = new CompoundStatement(new LogicalStatement(data), new LogicalStatement(filter("b", true)),
                LogicOperator.DISJUNCTION);
        var sourceRestriction = new LogicalStatement(filter("owner", true));
        var decorated = filter("input", true);
        var metadata = new FilterRequestData(new String[]{"a"}, new String[]{"declared"}, Boolean.class,
                Equals.class, "false", null, null, null, false, false, List.of(), null);
        var source = new ConditionalStatement(new StatementWrapper(grouped, Map.of("input", decorated), List.of(metadata), null),
                null, List.of(sourceRestriction));
        var builder = ConditionalStatement.builder(source);
        var unchanged = builder.build();
        assertThat(unchanged).isNotSameAs(source);
        assertThat(unchanged.statementWrapper().statement()).isSameAs(grouped);
        assertThat(unchanged.statementWrapper().allFilters()).isSameAs(source.statementWrapper().allFilters());
        assertThat(unchanged.statementWrapper().decoratedFilters()).isSameAs(source.statementWrapper().decoratedFilters());

        var negatedData = new FilterData(new String[]{"c"}, new String[]{"internal"}, Boolean.class, Equals.class,
                true, new Object[]{false}, null, null);
        var secondRestriction = new NegatedStatement(new LogicalStatement(filter("tenant", false)));
        var first = builder.restrict(secondRestriction).and(negatedData).build();
        var second = builder.or(filter("d", true)).restrict(filter("scope", true)).build();

        var sample = Map.<String, Object>of("a", true, "b", false, "c", false, "d", true);
        assertThat(ordinaryMatches(source, sample)).isTrue();
        assertThat(ordinaryMatches(first, sample)).isFalse();
        assertThat(ordinaryMatches(second, sample)).isTrue();
        assertThat(ordinaryMatches(first, Map.of("a", false, "b", true, "c", true, "d", false))).isTrue();
        assertThat(ordinaryMatches(first, Map.of("a", false, "b", false, "c", true, "d", false))).isFalse();
        assertThat(source.restrictions()).containsExactly(sourceRestriction);
        assertThat(first.restrictions()).containsExactly(sourceRestriction, secondRestriction);
        assertThat(second.restrictions()).hasSize(3).startsWith(sourceRestriction, secondRestriction);
        assertThat(first.statementWrapper().allFilters()).containsExactly(metadata);
        assertThat(first.statementWrapper().findDecoratedFilterByPath("input")).containsSame(decorated);
        assertThat(builder.build()).isNotSameAs(second);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidMandatoryTrees")
    void rejectsInvalidMandatoryTreesImmediatelyRatherThanWaitingForBuild(String scenario, AbstractStatement invalid) {
        var builder = ConditionalStatement.builder();
        assertThatThrownBy(() -> builder.restrict(invalid)).isInstanceOf(DynamicFilterConfigurationException.class);
        assertThat(builder.build().restrictions()).isEmpty();
    }

    private static Stream<Arguments> invalidMandatoryTrees() {
        var valid = new LogicalStatement(filter("owner", true));
        var noOp = new NoOpStatement();
        return Stream.of(Arguments.of("no-op root", noOp), Arguments.of("negated no-op", new NegatedStatement(noOp)),
                Arguments.of("AND no-op child", new CompoundStatement(valid, noOp, LogicOperator.CONJUNCTION)),
                Arguments.of("OR no-op child", new CompoundStatement(noOp, valid, LogicOperator.DISJUNCTION)),
                Arguments.of("negated group with no-op", new NegatedStatement(new CompoundStatement(valid, noOp, LogicOperator.DISJUNCTION))),
                Arguments.of("missing filter data", new LogicalStatement(null)), Arguments.of("missing negated operand", new NegatedStatement(null)),
                Arguments.of("missing compound operand", new CompoundStatement(valid, null, LogicOperator.CONJUNCTION)),
                Arguments.of("missing operator", new CompoundStatement(valid, valid, null)));
    }

    @Test
    void decoratorsRunSourceFirstAndInAdditionOrderWithDataButWithoutImplicitConditions() {
        var sourceData = filter("source", true);
        FilterDecorator<String> sourceDecorator = (result, wrapper) -> result + "/source:" +
                wrapper.findDecoratedFilterByPath("source").orElseThrow().findOneValue();
        var source = new ConditionalStatement(new StatementWrapper(new NoOpStatement(), Map.of("source", sourceData), null, null),
                sourceDecorator);
        var extra = new FilterData(new String[]{"extra", "alias"}, new String[]{"internal"}, Boolean.class, Equals.class,
                false, new Object[]{false}, null, null);
        FilterDecorator<String> added = (result, wrapper) -> result + "/added:" +
                wrapper.findDecoratedFilterByPath("extra").orElseThrow().findOneValue();
        FilterDecorator<String> last = (result, wrapper) -> "last(" + result + ")";
        var builder = ConditionalStatement.builder(source).decorate(added).decoratedFilter(extra);
        var first = builder.build();
        var second = builder.decorate(last).decoratedFilter(filter("later", true)).build();

        assertThat(resolve(source)).isEqualTo("ordinary/source:true");
        assertThat(resolve(first)).isEqualTo("ordinary/source:true/added:false");
        assertThat(resolve(second)).isEqualTo("last(ordinary/source:true/added:false)");
        assertThat(source.filterDecorator()).isSameAs(sourceDecorator);
        assertThat(first.statementWrapper().statement()).isSameAs(source.statementWrapper().statement());
        assertThat(first.statementWrapper().decoratedFilters()).containsOnlyKeys("source", "extra", "alias");
        assertThat(first.statementWrapper().findDecoratedFilterByPath("alias")).containsSame(extra);
        assertThat(second.statementWrapper().decoratedFilters()).containsOnlyKeys("source", "extra", "alias", "later");
        assertThat(source.statementWrapper().decoratedFilters()).containsOnlyKeys("source");
        assertThat(first.statementWrapper().allFilters()).isEmpty();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("decoratedPathCollisions")
    void decoratedPathCollisionsAreRejectedWithoutPartiallyChangingTheBuilder(String scenario, FilterData collision) {
        var source = new ConditionalStatement(new StatementWrapper(new NoOpStatement(), Map.of("source", filter("source", true)), null, null), null);
        var builder = ConditionalStatement.builder(source).decoratedFilter(filter("extra", true));
        assertThatThrownBy(() -> builder.decoratedFilter(collision)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("path");
        assertThat(builder.build().statementWrapper().decoratedFilters()).containsOnlyKeys("source", "extra");
    }

    private static Stream<Arguments> decoratedPathCollisions() {
        return Stream.of(Arguments.of("source path", filter("source", false)), Arguments.of("new path", filter("extra", false)),
                Arguments.of("collision after fresh path", new FilterData(new String[]{"fresh", "extra"}, new String[]{"input"},
                        Boolean.class, Equals.class, false, new Object[]{true}, null, null)),
                Arguments.of("duplicate supplied paths", new FilterData(new String[]{"duplicate", "duplicate"}, new String[]{"input"},
                        Boolean.class, Equals.class, false, new Object[]{true}, null, null)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("nullArguments")
    void rejectsNullArgumentsImmediately(String scenario, Consumer<ConditionalStatementBuilder> invocation) {
        var builder = ConditionalStatement.builder();
        assertThatThrownBy(() -> invocation.accept(builder)).isInstanceOf(NullPointerException.class);
        assertThat(builder.build().restrictions()).isEmpty();
        assertThat(builder.build().statementWrapper().decoratedFilters()).isEmpty();
    }

    private static Stream<Arguments> nullArguments() {
        return Stream.of(nullArgument("source", builder -> ConditionalStatement.builder(null)),
                nullArgument("AND filter", builder -> builder.and((FilterData) null)),
                nullArgument("AND expression", builder -> builder.and((AbstractStatement) null)),
                nullArgument("OR filter", builder -> builder.or((FilterData) null)),
                nullArgument("OR expression", builder -> builder.or((AbstractStatement) null)),
                nullArgument("restriction filter", builder -> builder.restrict((FilterData) null)),
                nullArgument("restriction expression", builder -> builder.restrict((AbstractStatement) null)),
                nullArgument("decorator", builder -> builder.decorate(null)),
                nullArgument("decorated filter", builder -> builder.decoratedFilter(null)));
    }

    private static Arguments nullArgument(String scenario, Consumer<ConditionalStatementBuilder> invocation) {
        return Arguments.of(scenario, invocation);
    }

    @Test
    void acceptsANullValueSlotWithoutOmittingTheFilter() {
        var builder = ConditionalStatement.builder();
        var nullValue = filter("nullable", null);
        var statement = builder.and(nullValue).restrict(nullValue).decoratedFilter(nullValue).build();
        assertThat(statement.restrictions()).hasSize(1);
        assertThat(statement.statementWrapper().findDecoratedFilterByPath("nullable")).containsSame(nullValue);
        assertThat(ordinaryMatches(statement, Map.of("nullable", true))).isFalse();
    }

    private static String resolve(ConditionalStatement statement) {
        DynamicFilterResolver<String> resolver = new DynamicFilterResolver<>() {
            @Override
            @SuppressWarnings("unchecked")
            public <R extends String> R createFilter(StatementWrapper wrapper, FilterDecorator<String> decorator) {
                return (R) (decorator == null ? "ordinary" : decorator.decorate("ordinary", wrapper));
            }
        };
        return resolver.createFilter(statement);
    }

    private static boolean ordinaryMatches(ConditionalStatement statement, Map<String, Object> sample) {
        var predicate = statement.statementWrapper().statement().acceptAnalyser(new EqualityPredicateAnalyser());
        return predicate == null || predicate.test(sample);
    }

    /** A backend extension evaluating equality behavior, without inspecting or asserting a tree representation. */
    private static final class EqualityPredicateAnalyser implements StatementAnalyser<Predicate<Map<String, Object>>> {

        @Override
        public Predicate<Map<String, Object>> analyseLogicalStatement(LogicalStatement statement) {
            var data = statement.getFilterData();
            return sample -> Objects.equals(sample.get(data.path()[0]), data.findOneValue());
        }

        @Override
        public Predicate<Map<String, Object>> analyseCompoundStatement(CompoundStatement statement) {
            var left = statement.getLeftStatement().acceptAnalyser(this);
            var right = statement.getRightStatement().acceptAnalyser(this);
            if (left == null) {
                return right;
            }
            if (right == null) {
                return left;
            }
            return statement.getLogicOperator().isConjunction() ? left.and(right) : left.or(right);
        }

        @Override
        public Predicate<Map<String, Object>> analyseNegationStatement(NegatedStatement statement) {
            var predicate = statement.getStatement().acceptAnalyser(this);
            return predicate == null ? null : predicate.negate();
        }

        @Override
        public Predicate<Map<String, Object>> analyseNoOpStatement(NoOpStatement statement) {
            return null;
        }
    }

    private static FilterData filter(String path, Object value) {
        return FilterData.of(path, new String[]{path}, Boolean.class, Equals.class, new Object[]{value});
    }
}
