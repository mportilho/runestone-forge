package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.generator.SortContract;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.Between;
import com.runestone.dynafilter.core.operation.types.Decorated;
import com.runestone.dynafilter.core.operation.types.Dynamic;
import com.runestone.dynafilter.core.operation.types.extensions.PeriodOverlapsClosed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestDeclaredSortContract {
    @Test
    void policyCompositionUsesTheSameInterfacesMetaAnnotationsAndFromSourcesAsTheFilters() throws Exception {
        var generator = new AnnotationStatementGenerator();
        var parameter = PolicySources.class.getDeclaredMethod("search", Object.class).getParameters()[0];
        var input = new AnnotationStatementInput(PolicyReusedSearch.class, parameter.getAnnotations());
        var contract = generator.sortContract(input);

        assertThat(contract.candidates()).contains(
                new SortContract.Candidate("typeExtra", List.of(), true),
                new SortContract.Candidate("metaExtra", List.of(), true),
                new SortContract.Candidate("fromExtra", List.of(), true),
                new SortContract.Candidate("nestedExtra", List.of("nestedAlias"), true),
                new SortContract.Candidate("parameterExtra", List.of(), true));
        assertThat(contract.exclusions()).containsExactlyInAnyOrder("typeExcluded", "fromExcluded", "nestedExcluded", "parameterExcluded");
        assertThat(contract.candidates()).extracting(SortContract.Candidate::path).doesNotContain("unusedExtra");
        assertThat(contract).isSameAs(generator.generateStatements(input, Map.of()).sortContract());

        var disjunction = PolicySources.class.getDeclaredMethod("disjunction", Object.class).getParameters()[0];
        var alternate = generator.sortContract(new AnnotationStatementInput(null, disjunction.getAnnotations()));
        assertThat(alternate.candidates()).contains(new SortContract.Candidate("nestedExtra", List.of("nestedAlias"), true),
                new SortContract.Candidate("fromExtra", List.of(), true));
        assertThat(alternate.exclusions()).containsExactlyInAnyOrder("fromExcluded", "nestedExcluded");
    }

    @Test
    void intervalAndDynamicAliasesComeOnlyFromTheirDeclarations() {
        var generator = new AnnotationStatementGenerator();
        var input = new AnnotationStatementInput(IntervalSearch.class, null);
        var absent = generator.generateStatements(input, Map.of());
        var active = generator.generateStatements(input, Map.of("dynamic", new String[]{"BT", "A", "Z"}));

        assertThat(active.sortContract()).isSameAs(absent.sortContract());
        assertThat(absent.sortContract().candidates()).containsExactly(
                new SortContract.Candidate("height", List.of("heightMin", "heightMax")),
                new SortContract.Candidate("name", List.of("dynamic")));
    }

    @Test
    void discoveryReusesInterfacesMetaAnnotationsAndFromSourcesIncludingNestedStatements() throws Exception {
        var generator = new AnnotationStatementGenerator();
        var parameter = Sources.class.getDeclaredMethod("search", Object.class).getParameters()[0];
        var input = new AnnotationStatementInput(ReusedSearch.class, parameter.getAnnotations());

        assertThat(generator.generateStatements(input, Map.of()).sortContract().candidates()).containsExactlyInAnyOrder(
                new SortContract.Candidate("name", List.of("query")),
                new SortContract.Candidate("height", List.of("heightMin", "heightMax")),
                new SortContract.Candidate("weight", List.of("weight")),
                new SortContract.Candidate("birthday", List.of("birthday")),
                new SortContract.Candidate("registerDate", List.of("registered")));
    }

    @Test
    void omittedDeclarationsRemainInFilterMetadataAndDoNotSuppressPublicContributions() {
        var result = new AnnotationStatementGenerator().generateStatements(new AnnotationStatementInput(OmittedSearch.class, null), Map.of());

        assertThat(result.allFilters()).hasSize(4);
        assertThat(result.sortContract().candidates()).containsExactly(new SortContract.Candidate("name", List.of("publicName")));
    }

    @Test
    void compoundDeclarationsPublishEachPathWithoutZippingOrInferringAliases() {
        var generator = new AnnotationStatementGenerator();
        var input = new AnnotationStatementInput(CompoundSearch.class, null);

        var absent = generator.generateStatements(input, Map.of());
        var active = generator.generateStatements(input, Map.of("term", "2020-01-01", "second", "2020-12-31"));

        assertThat(absent.sortContract()).isSameAs(active.sortContract());
        assertThat(absent.sortContract().candidates()).containsExactly(
                new SortContract.Candidate("name", List.of()),
                new SortContract.Candidate("otherName", List.of()));
    }

    @Test
    void declarationsAreCompiledOnceWithoutDependingOnReceivedValuesAndCannotBeMutated() {
        var generator = new AnnotationStatementGenerator();
        var input = new AnnotationStatementInput(Search.class, null);
        var absent = generator.generateStatements(input, Map.of());
        var active = generator.generateStatements(input, Map.of("query", "Alice"));

        assertThat(absent.sortContract()).isSameAs(active.sortContract()).isSameAs(generator.sortContract(input));
        assertThat(absent.sortContract().candidates()).containsExactly(new SortContract.Candidate("name", List.of("query")));
        assertThatThrownBy(() -> absent.sortContract().candidates().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> absent.sortContract().candidates().getFirst().aliases().clear()).isInstanceOf(UnsupportedOperationException.class);
        active.allFilters().getFirst().path()[0] = "other";
        assertThat(absent.sortContract().candidates().getFirst().path()).isEqualTo("name");
    }

    @Test
    void declaredRootComesFromUsedSourcesAndSurvivesRebuildWithoutChangingRootlessContracts() throws Exception {
        var generator = new AnnotationStatementGenerator();
        var method = RootSources.class.getDeclaredMethod("search", ConditionalStatement.class);
        var input = new AnnotationStatementInput(ConditionalStatement.class, method.getParameters()[0].getAnnotations());
        var source = new ConditionalStatement(generator.generateStatements(input, Map.of()), null);

        assertThat(source.statementWrapper().sortContract().declaredRoot()).isEqualTo(Root.class);
        assertThat(ConditionalStatement.builder(source).build().statementWrapper().sortContract())
                .isSameAs(source.statementWrapper().sortContract());
        var candidates = source.statementWrapper().sortContract().candidates();
        assertThat(new SortContract(candidates).declaredRoot()).isNull();
        assertThat(new SortContract(candidates, Root.class)).isNotEqualTo(new SortContract(candidates));
        assertThat(generator.sortContract(new AnnotationStatementInput(DeclaredSearch.class, null)).declaredRoot()).isEqualTo(Root.class);
    }

    @Test
    void recordEqualityIncludesTheContractAndCopiesMutableCollectionsAtItsBoundary() {
        var aliases = new ArrayList<>(List.of("query"));
        var candidates = new ArrayList<>(List.of(new SortContract.Candidate("name", aliases)));
        var contract = new SortContract(candidates);
        aliases.clear();
        candidates.clear();
        var original = new AnnotationStatementGenerator().generateStatements(new AnnotationStatementInput(Search.class, null), Map.of());
        var same = new StatementWrapper(original.statement(), original.decoratedFilters(), original.allFilters(), contract);
        var different = new StatementWrapper(original.statement(), original.decoratedFilters(), original.allFilters(), SortContract.EMPTY);

        assertThat(original).isEqualTo(same).isNotEqualTo(different);
    }

    @Conjunction(@Filter(path = "name", parameters = "query", operation = Equals.class))
    interface Search {
    }

    @Conjunction(@Filter(path = {"name", "otherName"}, parameters = {"term", "second"}, operation = PeriodOverlapsClosed.class))
    interface CompoundSearch {
    }

    @Conjunction({
            @Filter(path = "height", parameters = {"heightMin", "heightMax"}, operation = Between.class),
            @Filter(path = "name", parameters = "dynamic", operation = Dynamic.class)
    })
    interface IntervalSearch {
    }

    @Conjunction({
            @Filter(path = "name", parameters = "privateName", operation = Equals.class, hidden = true),
            @Filter(path = "name", parameters = "constantName", operation = Equals.class, constantValues = "Alice"),
            @Filter(path = "name", parameters = "decorated", operation = Decorated.class),
            @Filter(path = "name", parameters = "publicName", operation = Equals.class)
    })
    interface OmittedSearch {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Conjunction(@Filter(path = "height", parameters = {"heightMin", "heightMax"}, operation = Between.class))
    @interface HeightRange {
    }

    @HeightRange
    interface ReusedSearch extends Search {
    }

    static class FromSearch {
        @Filter(path = "weight", parameters = "weight", operation = Equals.class)
        String weight;
    }

    static class NestedFromSearch {
        @Filter(path = "birthday", parameters = "birthday", operation = Equals.class)
        String birthday;
    }

    static class Sources {
        void search(@ConjunctionFrom(value = FromSearch.class, disjunctions = @StatementFrom(NestedFromSearch.class))
                    @Disjunction(conjunctions = @Statement(@Filter(path = "registerDate", parameters = "registered", operation = Equals.class)))
                    Object input) {
        }

    }

    @Retention(RetentionPolicy.RUNTIME)
    @SortPolicy(include = "metaExtra")
    @interface MetaPolicy {
    }

    @MetaPolicy
    @SortPolicy(include = "typeExtra", exclude = "typeExcluded")
    interface PolicyReusedSearch extends Search {
    }

    @SortPolicy(include = "fromExtra", exclude = "fromExcluded")
    static class PolicyFromSearch extends UnusedSource {
    }

    @SortPolicy(aliases = @SortAlias(name = "nestedAlias", path = "nestedExtra"), exclude = "nestedExcluded")
    static class PolicyNestedSearch {
    }

    static class PolicySources {
        void search(@SortPolicy(include = "parameterExtra", exclude = "parameterExcluded")
                    @ConjunctionFrom(value = PolicyFromSearch.class, disjunctions = @StatementFrom(PolicyNestedSearch.class)) Object input) {
        }

        void disjunction(@DisjunctionFrom(value = PolicyNestedSearch.class, conjunctions = @StatementFrom(PolicyFromSearch.class)) Object input) {
        }
    }

    @SortPolicy(include = "unusedExtra")
    static class UnusedSource {
    }

    static class Root {
    }

    @FilterTarget(Root.class)
    @Conjunction(@Filter(path = "name", parameters = "name", operation = Equals.class))
    interface DeclaredSearch {
    }

    @FilterTarget(Root.class)
    static class Source {
        @Filter(path = "name", parameters = "query", operation = Equals.class)
        private String query;
    }

    static class RootSources {
        void search(@ConjunctionFrom(Source.class) ConditionalStatement statement) {
        }
    }
}
