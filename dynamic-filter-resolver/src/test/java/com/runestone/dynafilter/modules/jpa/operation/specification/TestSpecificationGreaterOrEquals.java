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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TestSpecificationGreaterOrEquals {

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
    public void test_GreaterOrEqualsOperation_OnString() {
        when(root.getJavaType()).thenReturn(Person.class);
        when(root.get(anyString())).thenReturn(path);
        when(path.getJavaType()).thenReturn(String.class);
        when(builder.upper(any())).thenReturn(path);

        FilterData filterData = new FilterData("name", new String[]{"name"}, String.class,
                Between.class, false, new String[]{"TestValue"}, null, "");

        SpecificationGreaterOrEquals<Person> specification = new SpecificationGreaterOrEquals<>(filterData, dataConversionService);
        specification.toPredicate(root, query, builder);

        verify(builder, times(1)).greaterThanOrEqualTo(any(Expression.class), (String) argThat(x -> x.toString().equals("TestValue")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_GreaterOrEqualsOperation_OnString_IgnoringCase() {
        when(root.getJavaType()).thenReturn(Person.class);
        when(root.get(anyString())).thenReturn(path);
        when(path.getJavaType()).thenReturn(String.class);
        when(builder.upper(any())).thenReturn(path);

        FilterData filterData = new FilterData("name", new String[]{"name"}, String.class,
                Between.class, false, new String[]{"TestValue"}, List.of(ModIgnoreCase.class), "");

        SpecificationGreaterOrEquals<Person> specification = new SpecificationGreaterOrEquals<>(filterData, dataConversionService);
        specification.toPredicate(root, query, builder);

        verify(builder, times(1)).greaterThanOrEqualTo(any(Expression.class), (String) argThat(x -> x.toString().equals("TESTVALUE")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_GreaterOrEqualsOperation_OnNumber() {
        when(root.getJavaType()).thenReturn(Person.class);
        when(root.get(anyString())).thenReturn(path);
        when(path.getJavaType()).thenReturn(BigDecimal.class);
        when(builder.upper(any())).thenReturn(path);

        FilterData filterData = new FilterData("height", new String[]{"height"}, BigDecimal.class,
                Between.class, false, new Object[]{180}, null, "");

        SpecificationGreaterOrEquals<Person> specification = new SpecificationGreaterOrEquals<>(filterData, dataConversionService);
        specification.toPredicate(root, query, builder);

        verify(builder, times(1)).ge(any(Expression.class), eq(BigDecimal.valueOf(180)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_GreaterOrEqualsOperation_OnNumber_IgnoringCase() {
        when(root.getJavaType()).thenReturn(Person.class);
        when(root.get(anyString())).thenReturn(path);
        when(path.getJavaType()).thenReturn(BigDecimal.class);
        when(builder.upper(any())).thenReturn(path);

        FilterData filterData = new FilterData("height", new String[]{"height"}, BigDecimal.class,
                Between.class, false, new Object[]{180}, List.of(ModIgnoreCase.class), "");

        SpecificationGreaterOrEquals<Person> specification = new SpecificationGreaterOrEquals<>(filterData, dataConversionService);
        specification.toPredicate(root, query, builder);

        verify(builder, times(1)).ge(any(Expression.class), eq(BigDecimal.valueOf(180)));
    }
}
