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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TestSpecificationLess {

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
    public void test_LessOperation_OnString() {
        when(root.getJavaType()).thenReturn(Person.class);
        when(root.get(anyString())).thenReturn(path);
        when(path.getJavaType()).thenReturn(String.class);
        when(builder.upper(any())).thenReturn(path);

        FilterData filterData = new FilterData("name", new String[]{"name"}, String.class,
                Between.class, false, new String[]{"TestValue"}, null, "");

        SpecificationLess<Person> specification = new SpecificationLess<>(filterData, dataConversionService);
        specification.toPredicate(root, query, builder);

        verify(builder, times(1)).lessThan(any(Expression.class), (String) argThat(x -> x.toString().equals("TestValue")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_LessOperation_OnString_IgnoringCase() {
        when(root.getJavaType()).thenReturn(Person.class);
        when(root.get(anyString())).thenReturn(path);
        when(path.getJavaType()).thenReturn(String.class);
        when(builder.upper(any())).thenReturn(path);

        FilterData filterData = new FilterData("name", new String[]{"name"}, String.class,
                Between.class, false, new String[]{"TestValue"}, List.of(ModIgnoreCase.class), "");

        SpecificationLess<Person> specification = new SpecificationLess<>(filterData, dataConversionService);
        specification.toPredicate(root, query, builder);

        verify(builder, times(1)).lessThan(any(Expression.class), (String) argThat(x -> x.toString().equals("TESTVALUE")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_LessOperation_OnNumber() {
        when(root.getJavaType()).thenReturn(Person.class);
        when(root.get(anyString())).thenReturn(path);
        when(path.getJavaType()).thenReturn(BigDecimal.class);
        when(builder.upper(any())).thenReturn(path);

        FilterData filterData = new FilterData("height", new String[]{"height"}, BigDecimal.class,
                Between.class, false, new Object[]{180}, null, "");

        SpecificationLess<Person> specification = new SpecificationLess<>(filterData, dataConversionService);
        specification.toPredicate(root, query, builder);

        verify(builder, times(1)).lt(any(Expression.class), eq(BigDecimal.valueOf(180)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_LessOperation_OnNumber_IgnoringCase() {
        when(root.getJavaType()).thenReturn(Person.class);
        when(root.get(anyString())).thenReturn(path);
        when(path.getJavaType()).thenReturn(BigDecimal.class);
        when(builder.upper(any())).thenReturn(path);

        FilterData filterData = new FilterData("height", new String[]{"height"}, BigDecimal.class,
                Between.class, false, new Object[]{180}, List.of(ModIgnoreCase.class), "");

        SpecificationLess<Person> specification = new SpecificationLess<>(filterData, dataConversionService);
        specification.toPredicate(root, query, builder);

        verify(builder, times(1)).lt(any(Expression.class), eq(BigDecimal.valueOf(180)));
    }
}
