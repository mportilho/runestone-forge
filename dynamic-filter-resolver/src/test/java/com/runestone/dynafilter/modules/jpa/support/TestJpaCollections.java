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

import com.runestone.converters.DataConversionService;
import com.runestone.dynafilter.core.exceptions.DynamicFilterConfigurationException;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.metamodel.Bindable;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.Type;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestJpaCollections {

    @Mock
    @SuppressWarnings("rawtypes")
    private Expression expression;

    @Mock
    private Path<?> path;

    @Mock
    private DataConversionService conversionService;

    @Test
    @DisplayName("requireCollectionExpression accepts collection expressions")
    void testRequireCollectionExpressionAcceptsCollectionExpression() {
        when(expression.getJavaType()).thenReturn(collectionType());

        Expression<?> collectionExpression = JpaCollections.requireCollectionExpression(expression, "ContainsAll");

        assertThat(collectionExpression).isSameAs(expression);
    }

    @Test
    @DisplayName("requireCollectionExpression rejects non-collection expressions")
    void testRequireCollectionExpressionRejectsNonCollectionExpression() {
        when(expression.getJavaType()).thenReturn(String.class);

        assertThatExceptionOfType(DynamicFilterConfigurationException.class)
                .isThrownBy(() -> JpaCollections.requireCollectionExpression(expression, "ContainsAll"))
                .withMessage("ContainsAll requires a collection path, but found java.lang.String");
    }

    @Test
    @DisplayName("findCollectionElementType reads plural path element type")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testFindCollectionElementTypeReadsPluralPathElementType() {
        PluralAttribute pluralAttribute = org.mockito.Mockito.mock(PluralAttribute.class);
        Type elementType = org.mockito.Mockito.mock(Type.class);
        when(path.getModel()).thenReturn((Bindable) pluralAttribute);
        when(pluralAttribute.getElementType()).thenReturn(elementType);
        when(elementType.getJavaType()).thenReturn(String.class);

        Class<?> elementJavaType = JpaCollections.findCollectionElementType(path);

        assertThat(elementJavaType).isEqualTo(String.class);
    }

    @Test
    @DisplayName("findCollectionElementType falls back to Object for non-plural expressions")
    void testFindCollectionElementTypeFallsBackToObjectForNonPluralExpressions() {
        Class<?> elementJavaType = JpaCollections.findCollectionElementType(expression);

        assertThat(elementJavaType).isEqualTo(Object.class);
    }

    @Test
    @DisplayName("requireNonNegativeSize converts non-negative integer values")
    void testRequireNonNegativeSizeConvertsNonNegativeIntegerValue() {
        when(conversionService.convert("3", Integer.class)).thenReturn(3);

        int size = JpaCollections.requireNonNegativeSize("3", conversionService, "CollectionSize", "size");

        assertThat(size).isEqualTo(3);
    }

    @Test
    @DisplayName("requireNonNegativeSize rejects null values before conversion")
    void testRequireNonNegativeSizeRejectsNullValue() {
        assertThatThrownBy(() -> JpaCollections.requireNonNegativeSize(null, conversionService, "CollectionSize", "size"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CollectionSize requires non-null size");
    }

    @Test
    @DisplayName("requireNonNegativeSize rejects converted negative or null sizes")
    void testRequireNonNegativeSizeRejectsNegativeOrNullConvertedSizes() {
        when(conversionService.convert("-1", Integer.class)).thenReturn(-1);
        when(conversionService.convert("invalid", Integer.class)).thenReturn(null);

        assertThatThrownBy(() -> JpaCollections.requireNonNegativeSize("-1", conversionService, "CollectionSize", "size"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CollectionSize requires a non-negative integer size");
        assertThatThrownBy(() -> JpaCollections.requireNonNegativeSize("invalid", conversionService, "CollectionSize", "size"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CollectionSize requires a non-negative integer size");
    }

    @SuppressWarnings("rawtypes")
    private static Class collectionType() {
        return List.class;
    }

}
