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
        var filterDataName = new FilterData(new String[]{"name"}, new String[]{"clientName"}, String.class, Like.class, true,
                new Object[]{"John"}, List.of(ModIgnoreCase.class), "");
        Assertions.assertThat(filterDataName.hasModifier(ModIgnoreCase.class)).isTrue();
        Assertions.assertThat(filterDataName.hasModifier(UnnamedInterface.class)).isFalse();
    }

    @Test
    public void testGeneralFactoryNullArgumentValidationRemainsUnambiguous() {
        Assertions.assertThatThrownBy(() -> FilterData.of("name", null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("parameters cannot be null or empty");
        Assertions.assertThatThrownBy(() -> FilterData.of("name", new String[]{"clientName"}, String.class, Like.class, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("values cannot be null or empty");
    }

    @Test
    public void testGeneralFactoryKeepsItsExistingNameAndMetadataValidation() {
        var filter = FilterData.of(null, new String[]{null, " "}, null, null, new Object[]{null, "John"});

        Assertions.assertThat(filter.path()).containsExactly((String) null);
        Assertions.assertThat(filter.parameters()).containsExactly(null, " ");
        Assertions.assertThat(filter.targetType()).isNull();
        Assertions.assertThat(filter.operation()).isNull();
        Assertions.assertThat(filter.values()).containsExactly(null, "John");
        Assertions.assertThat(filter.negate()).isFalse();
        Assertions.assertThat(filter.modifiers()).isNull();
        Assertions.assertThat(filter.description()).isNull();
    }

    @Test
    public void testGeneralFactoryKeepsCallerOwnedArraysAndNullValueSlots() {
        String[] parameters = {"clientName"};
        String[] names = {"John", "Mary"};
        Object[] values = {names};
        var filter = FilterData.of("name", parameters, String.class, Like.class, values);

        Assertions.assertThat(filter.parameters()).isSameAs(parameters);
        Assertions.assertThat(filter.values()).isSameAs(values);
        Assertions.assertThat(filter.<Object>findOneValue()).isSameAs(names);

        var nullValue = FilterData.of("name", parameters, String.class, Like.class, new Object[]{null});
        Assertions.assertThat(nullValue.values()).hasSize(1);
        Assertions.assertThat(nullValue.<Object>findOneValue()).isNull();
    }

    private interface UnnamedInterface extends FilterModifier {
    }

}
