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

import com.runestone.converters.RuntimeDataConversionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestJpaValues {

    @Mock
    private RuntimeDataConversionService conversionService;

    @Test
    @DisplayName("asArray normalizes null, arrays, collections, and scalar values")
    void testAsArrayNormalizesSupportedValueShapes() {
        Object[] existingArray = new Object[]{"one", "two"};

        assertThat(JpaValues.asArray(null)).containsExactly((Object) null);
        assertThat(JpaValues.asArray(existingArray)).isSameAs(existingArray);
        assertThat(JpaValues.asArray(List.of("one", "two"))).containsExactly("one", "two");
        assertThat(JpaValues.asArray("one")).containsExactly("one");
    }

    @Test
    @DisplayName("convert converts only non-null array values")
    void testConvertConvertsOnlyNonNullValues() {
        when(conversionService.convert("1", Integer.class)).thenReturn(1);
        when(conversionService.convert("2", Integer.class)).thenReturn(2);

        Object[] converted = JpaValues.convert(new Object[]{"1", null, "2"}, Integer.class, conversionService);

        assertThat(converted).containsExactly(1, null, 2);
        verify(conversionService, never()).convert(null, Integer.class);
    }

    @Test
    @DisplayName("convert rejects null arguments")
    void testConvertRejectsNullArguments() {
        assertThatNullPointerException()
                .isThrownBy(() -> JpaValues.convert(null, Integer.class, conversionService))
                .withMessage("rawValues cannot be null");
        assertThatNullPointerException()
                .isThrownBy(() -> JpaValues.convert(new Object[]{"1"}, null, conversionService))
                .withMessage("targetType cannot be null");
        assertThatNullPointerException()
                .isThrownBy(() -> JpaValues.convert(new Object[]{"1"}, Integer.class, null))
                .withMessage("conversionService cannot be null");
    }

}
