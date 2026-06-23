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

package com.runestone.converters.impl;

import com.runestone.converters.DataConverter;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDataConverterSpiLoader {

    @Test
    void verifyAllConvertersAreRegisteredInSpi() {
        // 1. Discover all classes implementing DataConverter using ClassGraph
        List<Class<?>> allConverterClasses;
        try (ScanResult scanResult = new ClassGraph()
                .acceptPackages("com.runestone.converters.impl")
                .enableClassInfo()
                .scan()) {

            allConverterClasses = scanResult.getClassesImplementing(DataConverter.class.getName())
                    .stream()
                    .filter(classInfo -> !classInfo.isAbstract() && !classInfo.isInterface())
                    .filter(classInfo -> !Modifier.isAbstract(classInfo.loadClass().getModifiers()))
                    .map(ClassInfo::loadClass)
                    .collect(Collectors.toList());
        }

        assertThat(allConverterClasses).isNotEmpty();

        // 2. Discover all classes actually loaded by ServiceLoader
        Set<Class<?>> spiLoadedClasses = ServiceLoader.load(DataConverter.class)
                .stream()
                .map(ServiceLoader.Provider::type)
                .collect(Collectors.toSet());

        // 3. Verify exactly matching configurations
        List<Class<?>> missingFromSpi = allConverterClasses.stream()
                .filter(clazz -> !spiLoadedClasses.contains(clazz))
                .toList();

        assertThat(missingFromSpi)
                .withFailMessage("The following DataConverter implementations are missing from " +
                        "META-INF/services/com.runestone.converters.DataConverter : \n%s", missingFromSpi)
                .isEmpty();
    }
}
