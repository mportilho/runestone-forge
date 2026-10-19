package com.runestone.dynafilter.core.generator.annotation;

import com.runestone.dynafilter.core.generator.SortContract;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.operation.types.Equals;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestDeclaredSortContract {
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
}
