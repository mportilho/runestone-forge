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

import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.IsIn;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Address;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.Attribute;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Set;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TestJpaPredicateUtilsUnit {

    @Mock
    @SuppressWarnings("rawtypes")
    private Root root;

    @Mock
    @SuppressWarnings("rawtypes")
    private Join join;

    @Mock
    @SuppressWarnings("rawtypes")
    private Path path;

    @Mock
    @SuppressWarnings("rawtypes")
    private Attribute attribute;

    @Test
    public void testResolveAttributePathReportsIntermediatePluralAssociation() {
        String fieldPath = "addresses.street";
        FilterData filterData = FilterData.of(fieldPath, new String[]{"street"}, String.class,
                Equals.class, new Object[]{"Main"});
        when(root.getJavaType()).thenReturn(Person.class);
        when(root.getJoins()).thenReturn(Set.of());
        when(root.join("addresses", JoinType.INNER)).thenReturn(join);
        when(join.getAttribute()).thenReturn(attribute);
        when(attribute.isCollection()).thenReturn(true);
        when(join.get("street")).thenReturn(path);

        JpaPredicateUtils.PathResolution<String> resolution = JpaPredicateUtils.resolveAttributePath(fieldPath, filterData, root);

        Assertions.assertThat(resolution.expression()).isSameAs(path);
        Assertions.assertThat(resolution.crossedPluralAssociation()).isTrue();
    }

    @Test
    public void testResolveAttributePathKeepsSingularAssociationNonDistinct() {
        String fieldPath = "person.name";
        FilterData filterData = FilterData.of(fieldPath, new String[]{"personName"}, String.class,
                Equals.class, new Object[]{"John"});
        when(root.getJavaType()).thenReturn(Address.class);
        when(root.getJoins()).thenReturn(Set.of());
        when(root.join("person", JoinType.INNER)).thenReturn(join);
        when(join.getAttribute()).thenReturn(attribute);
        when(attribute.isCollection()).thenReturn(false);
        when(join.get("name")).thenReturn(path);

        JpaPredicateUtils.PathResolution<String> resolution = JpaPredicateUtils.resolveAttributePath(fieldPath, filterData, root);

        Assertions.assertThat(resolution.expression()).isSameAs(path);
        Assertions.assertThat(resolution.crossedPluralAssociation()).isFalse();
    }

    @Test
    public void testResolveAttributeJoinPathReportsFinalPluralAttribute() {
        String fieldPath = "statuses";
        FilterData filterData = FilterData.of(fieldPath, new String[]{"statuses"}, Object.class,
                IsIn.class, new Object[]{new Object[]{"ACTIVE"}});
        when(root.getJavaType()).thenReturn(Person.class);
        when(root.getJoins()).thenReturn(Set.of());
        when(root.join("statuses", JoinType.INNER)).thenReturn(join);
        when(join.getAttribute()).thenReturn(attribute);
        when(attribute.isCollection()).thenReturn(true);

        JpaPredicateUtils.PathResolution<Object> resolution = JpaPredicateUtils.resolveAttributeJoinPath(fieldPath, filterData, root);

        Assertions.assertThat(resolution.expression()).isSameAs(join);
        Assertions.assertThat(resolution.crossedPluralAssociation()).isTrue();
    }

}
