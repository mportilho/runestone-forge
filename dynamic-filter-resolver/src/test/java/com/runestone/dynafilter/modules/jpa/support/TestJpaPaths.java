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

package com.runestone.dynafilter.modules.jpa.support;

import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.FilterModifier;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.modules.jpa.operation.modifiers.ModJoinTypeLeft;
import com.runestone.dynafilter.modules.jpa.operation.modifiers.ModJoinTypeRight;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.Attribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestJpaPaths {

    @Mock
    @SuppressWarnings("rawtypes")
    private Root root;

    @Mock
    @SuppressWarnings("rawtypes")
    private CriteriaQuery query;

    @Mock
    @SuppressWarnings("rawtypes")
    private Path path;

    @Mock
    @SuppressWarnings("rawtypes")
    private Join join;

    @Mock
    @SuppressWarnings("rawtypes")
    private Join existingJoin;

    @Mock
    @SuppressWarnings("rawtypes")
    private Attribute attribute;

    @BeforeEach
    void setup() {
        JpaPaths.clearCaches();
    }

    @Test
    @DisplayName("resolveAttributePath resolves a simple path without marking the query distinct")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testResolveAttributePathResolvesSimplePathWithoutDistinct() {
        when(root.getJavaType()).thenReturn(Object.class);
        when(root.get("name")).thenReturn(path);

        JpaPaths.ResolvedJpaPath<?> resolved = JpaPaths.resolveAttributePath(" name ", filterData(), root, query);

        assertThat(resolved.expression()).isSameAs(path);
        assertThat(resolved.crossedPluralAssociation()).isFalse();
        verify(query, never()).distinct(true);
    }

    @Test
    @DisplayName("resolveAttributePath creates joins and marks distinct after crossing a plural association")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testResolveAttributePathCreatesJoinAndMarksDistinctForPluralAssociation() {
        when(root.getJavaType()).thenReturn(Object.class);
        when(root.getJoins()).thenReturn(Set.of());
        when(root.join("addresses", JoinType.INNER)).thenReturn(join);
        when(join.getAttribute()).thenReturn(attribute);
        when(attribute.isCollection()).thenReturn(true);
        when(join.get("street")).thenReturn(path);

        JpaPaths.ResolvedJpaPath<?> resolved = JpaPaths.resolveAttributePath("addresses.street", filterData(), root, query);

        assertThat(resolved.expression()).isSameAs(path);
        assertThat(resolved.crossedPluralAssociation()).isTrue();
        verify(root).join("addresses", JoinType.INNER);
        verify(query).distinct(true);
    }

    @Test
    @DisplayName("resolveAttributePath reuses an existing join with the requested join type")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testResolveAttributePathReusesExistingJoinWithRequestedJoinType() {
        when(root.getJavaType()).thenReturn(Object.class);
        when(root.getJoins()).thenReturn(Set.of(existingJoin));
        when(existingJoin.getAttribute()).thenReturn(attribute);
        when(attribute.getName()).thenReturn("address");
        when(attribute.isCollection()).thenReturn(false);
        when(existingJoin.getJoinType()).thenReturn(JoinType.LEFT);
        when(existingJoin.get("city")).thenReturn(path);

        JpaPaths.ResolvedJpaPath<?> resolved = JpaPaths.resolveAttributePath(
                "address.city",
                filterData(List.of(ModJoinTypeLeft.class)),
                root
        );

        assertThat(resolved.expression()).isSameAs(path);
        assertThat(resolved.crossedPluralAssociation()).isFalse();
        verify(root, never()).join(anyString(), any(JoinType.class));
    }

    @Test
    @DisplayName("resolveAttributePath gives right joins precedence when both join modifiers are present")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testResolveAttributePathGivesRightJoinPrecedence() {
        when(root.getJavaType()).thenReturn(Object.class);
        when(root.getJoins()).thenReturn(Set.of());
        when(root.join("address", JoinType.RIGHT)).thenReturn(join);
        when(join.getAttribute()).thenReturn(attribute);
        when(attribute.isCollection()).thenReturn(false);
        when(join.get("city")).thenReturn(path);

        JpaPaths.resolveAttributePath(
                "address.city",
                filterData(List.of(ModJoinTypeLeft.class, ModJoinTypeRight.class)),
                root
        );

        verify(root).join("address", JoinType.RIGHT);
    }

    @Test
    @DisplayName("resolveAttributeJoinPath joins the final path segment")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testResolveAttributeJoinPathJoinsFinalPathSegment() {
        when(root.getJavaType()).thenReturn(Object.class);
        when(root.getJoins()).thenReturn(Set.of());
        when(root.join("tags", JoinType.INNER)).thenReturn(join);
        when(join.getAttribute()).thenReturn(attribute);
        when(attribute.isCollection()).thenReturn(true);

        JpaPaths.ResolvedJpaPath<?> resolved = JpaPaths.resolveAttributeJoinPath("tags", filterData(), root, query);

        assertThat(resolved.expression()).isSameAs(join);
        assertThat(resolved.crossedPluralAssociation()).isTrue();
        verify(query).distinct(true);
    }

    @Test
    @DisplayName("resolveAttributePath rejects null and malformed paths")
    void testResolveAttributePathRejectsNullAndMalformedPaths() {
        when(root.getJavaType()).thenReturn(Object.class);

        assertThatNullPointerException()
                .isThrownBy(() -> JpaPaths.resolveAttributePath(null, filterData(), root))
                .withMessage("path cannot be null");
        assertThatThrownBy(() -> JpaPaths.resolveAttributePath("", filterData(), root))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Path cannot be empty");
        assertThatThrownBy(() -> JpaPaths.resolveAttributePath("address..city", filterData(), root))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid path segment on path 'address..city'");
    }

    private static FilterData filterData() {
        return filterData(null);
    }

    private static FilterData filterData(List<Class<? extends FilterModifier>> modifiers) {
        return new FilterData(new String[]{"path"}, new String[]{"parameter"}, Object.class,
                Equals.class, false, new Object[]{"value"}, modifiers, null);
    }

}
