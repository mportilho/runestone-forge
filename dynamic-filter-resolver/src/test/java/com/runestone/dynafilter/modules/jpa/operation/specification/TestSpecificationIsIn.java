package com.runestone.dynafilter.modules.jpa.operation.specification;

import com.runestone.converters.DataConversionService;
import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.modifiers.ModIgnoreCase;
import com.runestone.dynafilter.core.operation.types.Between;
import com.runestone.dynafilter.core.operation.types.IsIn;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TestSpecificationIsIn {

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
    public void test_InOperation_OnString() {
        when(root.getJavaType()).thenReturn(Person.class);
        when(root.get(anyString())).thenReturn(path);
        when(path.getJavaType()).thenReturn(String.class);
        when(builder.upper(any())).thenReturn(path);

        FilterData filterData = new FilterData("name", new String[]{"name"}, String.class,
                IsIn.class, false, new Object[]{new String[]{"v1", "v2", "v3"}}, null, "");

        SpecificationIsIn<Person> specification = new SpecificationIsIn<>(filterData, dataConversionService);
        specification.toPredicate(root, query, builder);

        verify(path, times(1)).in("v1", "v2", "v3");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_InOperation_OnString_IgnoringCase() {
        when(root.getJavaType()).thenReturn(Person.class);
        when(root.get(anyString())).thenReturn(path);
        when(path.getJavaType()).thenReturn(String.class);
        when(builder.upper(any())).thenReturn(path);

        FilterData filterData = new FilterData("name", new String[]{"name"}, String.class,
                IsIn.class, false, new Object[]{new String[]{"v1", "v2", "v3"}}, List.of(ModIgnoreCase.class), "");

        SpecificationIsIn<Person> specification = new SpecificationIsIn<>(filterData, dataConversionService);
        specification.toPredicate(root, query, builder);

        verify(path, times(1)).in("V1", "V2", "V3");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_InOperation_OnNumber() {
        when(root.getJavaType()).thenReturn(Person.class);
        when(root.get(anyString())).thenReturn(path);
        when(path.getJavaType()).thenReturn(BigDecimal.class);
        when(builder.upper(any())).thenReturn(path);

        FilterData filterData = new FilterData("height", new String[]{"height"}, BigDecimal.class,
                Between.class, false, new Object[]{new Object[]{180, 200}}, null, "");

        SpecificationIsIn<Person> specification = new SpecificationIsIn<>(filterData, dataConversionService);
        specification.toPredicate(root, query, builder);

        verify(path, times(1)).in(BigDecimal.valueOf(180), BigDecimal.valueOf(200));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_InOperation_OnNumber_IgnoringCase() {
        when(root.getJavaType()).thenReturn(Person.class);
        when(root.get(anyString())).thenReturn(path);
        when(path.getJavaType()).thenReturn(BigDecimal.class);
        when(builder.upper(any())).thenReturn(path);

        FilterData filterData = new FilterData("height", new String[]{"height"}, BigDecimal.class,
                Between.class, false, new Object[]{new Object[]{180, 200}}, List.of(ModIgnoreCase.class), "");

        SpecificationIsIn<Person> specification = new SpecificationIsIn<>(filterData, dataConversionService);
        specification.toPredicate(root, query, builder);

        verify(path, times(1)).in(BigDecimal.valueOf(180), BigDecimal.valueOf(200));
    }
}
