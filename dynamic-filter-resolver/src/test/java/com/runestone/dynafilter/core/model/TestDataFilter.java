package com.runestone.dynafilter.core.model;

import com.runestone.dynafilter.core.exceptions.MultipleFilterDataValuesException;
import com.runestone.dynafilter.core.model.modifiers.ModIgnoreCase;
import com.runestone.dynafilter.core.operation.types.Between;
import com.runestone.dynafilter.core.operation.types.Like;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

public class TestDataFilter {

    @Test
    public void testNoValue() {
        Assertions.assertThatThrownBy(() -> FilterData.of("name", new String[]{"clientName"}, String.class, Like.class, new Object[]{}))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testNoParameter() {
        Assertions.assertThatThrownBy(() -> FilterData.of("name", null, String.class, Like.class, new Object[]{"John"}))
                .isInstanceOf(IllegalArgumentException.class);
        Assertions.assertThatThrownBy(() -> FilterData.of("name", new String[]{}, String.class, Like.class, new Object[]{"John"}))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testOneParameterOneScalarValue() {
        var filterData = FilterData.of("name", new String[]{"clientName"}, String.class, Like.class, new Object[]{"John"});
        Assertions.assertThat(filterData.<String>findOneValue()).isEqualTo("John");
        Assertions.assertThat(filterData.findValueOnIndex(0)).isEqualTo("John");
        Assertions.assertThat(filterData.hasModifier(UnnamedInterface.class)).isFalse();
    }

    @Test
    public void testMultipleParameterOneValueEach() {
        var filterData = FilterData.of("creationDate", new String[]{"firstDate", "lastDate"}, LocalDate.class,
                Between.class, new Object[]{"2022-01-01", "2022-12-31"});
        Assertions.assertThatThrownBy(filterData::findOneValue).isInstanceOf(MultipleFilterDataValuesException.class);
        Assertions.assertThat(filterData.findValueOnIndex(0)).isEqualTo("2022-01-01");
        Assertions.assertThat(filterData.findValueOnIndex(1)).isEqualTo("2022-12-31");
        Assertions.assertThatThrownBy(() -> filterData.findValueOnIndex(2))
                .isInstanceOf(ArrayIndexOutOfBoundsException.class)
                .hasMessageStartingWith("Accessing nonexistent index ");
        Assertions.assertThat(filterData.hasModifier(UnnamedInterface.class)).isFalse();
    }

    @Test
    public void testThrowExOneParameterOneMultipleValues() {
        Assertions.assertThatThrownBy(() ->
                        FilterData.of("name", new String[]{"clientName"}, String.class, Like.class, new Object[]{"John", "Doe"}))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testMultipleParameterMultipleValuesEach() {
        var filterData = FilterData.of("creationDateInterval", new String[]{"firstInterval", "lastInterval"}, LocalDate.class,
                Between.class, new Object[]{new Object[]{"2021-01-01", "2021-01-01"}, new Object[]{"2022-12-31", "2022-01-01"}});
        Assertions.assertThatThrownBy(filterData::findOneValue).isInstanceOf(MultipleFilterDataValuesException.class);
        Assertions.assertThat(filterData.findValueOnIndex(0)).isEqualTo(new Object[]{"2021-01-01", "2021-01-01"});
        Assertions.assertThat(filterData.findValueOnIndex(1)).isEqualTo(new Object[]{"2022-12-31", "2022-01-01"});
        Assertions.assertThat(filterData.hasModifier(UnnamedInterface.class)).isFalse();
    }

    @Test
    public void testFilterDataWithModifier() {
        var filterDataName = new FilterData("name", new String[]{"clientName"}, String.class, Like.class, true,
                new Object[]{"John"}, List.of(ModIgnoreCase.class), "");
        Assertions.assertThat(filterDataName.hasModifier(ModIgnoreCase.class)).isTrue();
        Assertions.assertThat(filterDataName.hasModifier(UnnamedInterface.class)).isFalse();
    }

    private interface UnnamedInterface extends FilterModifier {
    }

}
