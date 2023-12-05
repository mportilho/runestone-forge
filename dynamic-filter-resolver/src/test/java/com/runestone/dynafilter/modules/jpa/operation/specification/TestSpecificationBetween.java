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

package com.runestone.dynafilter.modules.jpa.operation.specification;

import com.runestone.converters.DataConversionService;
import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.modifiers.ModIgnoreCase;
import com.runestone.dynafilter.core.operation.types.Between;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TestSpecificationBetween {

    private static final DataConversionService dataConversionService = new DefaultDataConversionService();

    @Mock
    private CriteriaBuilder builder;

    @Mock
    @SuppressWarnings("rawtypes")
    private CriteriaQuery query;

    @Mock
    @SuppressWarnings("rawtypes")
    private Root root;

    @Mock
    @SuppressWarnings("rawtypes")
    private Path path;

    @Test
    @SuppressWarnings("unchecked")
    public void testBetweenOperation_WithOneValue_ThrowingException() {
        when(root.getJavaType()).thenReturn(Person.class);
        when(root.get(anyString())).thenReturn(path);
        when(path.getJavaType()).thenReturn(String.class);
        when(builder.upper(any())).thenReturn(path);
        FilterData filterData = new FilterData("name", new String[]{"name"}, String.class,
                Between.class, false, new String[]{"TestValue"}, null, "");

        SpecificationBetween<Person> specification = new SpecificationBetween<>(filterData, dataConversionService);
        assertThatThrownBy(() -> specification.toPredicate(root, query, builder)).isInstanceOf(ArrayIndexOutOfBoundsException.class);
        verify(builder, times(0)).between(any(Expression.class), anyString(), anyString());
    }

    @Test
    @SuppressWarnings({"unchecked"})
    public void testBetweenOperation_WithTwoNullValues() {
        when(root.getJavaType()).thenReturn(Person.class);
        when(root.get(anyString())).thenReturn(path);
        when(path.getJavaType()).thenReturn(BigDecimal.class);
        when(builder.upper(any())).thenReturn(path);

        FilterData filterData = new FilterData("height", new String[]{"minHeight", "maxHeight"}, BigDecimal.class,
                Between.class, false, new String[]{null, null}, null, "");

        SpecificationBetween<Person> specification = new SpecificationBetween<>(filterData, dataConversionService);
        specification.toPredicate(root, query, builder);

        verify(builder, times(1)).between(any(), eq(null), (String) eq(null));
    }

    @Test
    @SuppressWarnings({"unchecked"})
    public void testBetweenOperation_WithTwoValues() {
        when(root.getJavaType()).thenReturn(Person.class);
        when(root.get(anyString())).thenReturn(path);
        when(path.getJavaType()).thenReturn(BigDecimal.class);
        when(builder.upper(any())).thenReturn(path);
        FilterData filterData = new FilterData("height", new String[]{"minHeight", "maxHeight"}, BigDecimal.class,
                Between.class, false, new Object[]{140, 200}, null, "");

        SpecificationBetween<Person> specification = new SpecificationBetween<>(filterData, dataConversionService);
        specification.toPredicate(root, query, builder);

        verify(builder, times(1)).between(any(), eq(BigDecimal.valueOf(140)), eq(BigDecimal.valueOf(200)));
        verify(builder, times(0)).upper(any());
    }

    @Test
    @SuppressWarnings({"unchecked"})
    public void testBetweenOperation_WithTwoString_IgnoringCase() {
        when(root.getJavaType()).thenReturn(Person.class);
        when(root.get(anyString())).thenReturn(path);
        when(path.getJavaType()).thenReturn(String.class);
        when(builder.upper(any())).thenReturn(path);

        FilterData filterData = new FilterData("name", new String[]{"minName", "maxName"}, String.class,
                Between.class, false, new String[]{"a", "c"}, List.of(ModIgnoreCase.class), "");

        SpecificationBetween<Person> specification = new SpecificationBetween<>(filterData, dataConversionService);
        specification.toPredicate(root, query, builder);

        verify(builder, times(1)).between(any(), eq("A"), eq("C"));
        verify(builder, times(1)).upper(any());
    }
}
