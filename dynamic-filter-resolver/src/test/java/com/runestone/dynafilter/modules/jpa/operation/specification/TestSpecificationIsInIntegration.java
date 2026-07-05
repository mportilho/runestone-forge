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

import com.runestone.converters.impl.DefaultDataConversionService;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.operation.types.IsIn;
import com.runestone.dynafilter.modules.jpa.tools.app.database.InMemoryDatabaseApplication;
import com.runestone.dynafilter.modules.jpa.tools.app.database.ProdutoRepository;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Produto;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.TipoProduto;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Set;

@DataJpaTest
@ContextConfiguration(classes = InMemoryDatabaseApplication.class)
public class TestSpecificationIsInIntegration {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private EntityManager entityManager;

    private static final DefaultDataConversionService conversionService = new DefaultDataConversionService();

    @BeforeEach
    public void setup() {
        produtoRepository.deleteAll();
        entityManager.flush();

        produtoRepository.save(new Produto("Notebook", Set.of(TipoProduto.ELETRONICO)));
        produtoRepository.save(new Produto("Camiseta", Set.of(TipoProduto.VESTUARIO)));
        produtoRepository.save(new Produto("Arroz", Set.of(TipoProduto.ALIMENTICIO)));
        produtoRepository.save(new Produto("Consultoria TI", Set.of(TipoProduto.SERVICO, TipoProduto.ELETRONICO)));

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void test_IsIn_OnElementCollection_SingleEnumValue() {
        FilterData filterData = new FilterData(new String[]{"tipos"}, new String[]{"tipos"}, Object.class,
                IsIn.class, false, new Object[]{new Object[]{"ELETRONICO"}}, null, "");

        SpecificationIsIn<Produto> spec = new SpecificationIsIn<>(filterData, conversionService);
        List<Produto> result = produtoRepository.findAll(spec);

        Assertions.assertThat(result)
                .hasSize(2)
                .extracting(Produto::getNome)
                .containsExactlyInAnyOrder("Notebook", "Consultoria TI");
    }

    @Test
    public void test_IsIn_OnElementCollection_MultipleEnumValues() {
        FilterData filterData = new FilterData(new String[]{"tipos"}, new String[]{"tipos"}, Object.class,
                IsIn.class, false, new Object[]{new Object[]{"VESTUARIO", "ALIMENTICIO"}}, null, "");

        SpecificationIsIn<Produto> spec = new SpecificationIsIn<>(filterData, conversionService);
        List<Produto> result = produtoRepository.findAll(spec);

        Assertions.assertThat(result)
                .hasSize(2)
                .extracting(Produto::getNome)
                .containsExactlyInAnyOrder("Camiseta", "Arroz");
    }

    @Test
    public void test_IsIn_OnElementCollection_EnumValueNotPresent_ReturnsEmpty() {
        FilterData filterData = new FilterData(new String[]{"tipos"}, new String[]{"tipos"}, Object.class,
                IsIn.class, false, new Object[]{new Object[]{"SERVICO"}}, null, "");

        SpecificationIsIn<Produto> spec = new SpecificationIsIn<>(filterData, conversionService);
        List<Produto> result = produtoRepository.findAll(spec);

        Assertions.assertThat(result)
                .hasSize(1)
                .extracting(Produto::getNome)
                .containsExactly("Consultoria TI");
    }

    @Test
    public void test_IsIn_OnElementCollection_WithEnumDirectValue() {
        FilterData filterData = new FilterData(new String[]{"tipos"}, new String[]{"tipos"}, Object.class,
                IsIn.class, false, new Object[]{new Object[]{TipoProduto.ELETRONICO}}, null, "");

        SpecificationIsIn<Produto> spec = new SpecificationIsIn<>(filterData, conversionService);
        List<Produto> result = produtoRepository.findAll(spec);

        Assertions.assertThat(result)
                .hasSize(2)
                .extracting(Produto::getNome)
                .containsExactlyInAnyOrder("Notebook", "Consultoria TI");
    }

    @Test
    public void test_IsIn_OnElementCollection_MultiValueEntity_NoDuplicates() {
        // "Consultoria TI" has both SERVICO and ELETRONICO; filtering for both should return it once
        FilterData filterData = new FilterData(new String[]{"tipos"}, new String[]{"tipos"}, Object.class,
                IsIn.class, false, new Object[]{new Object[]{"SERVICO", "ELETRONICO"}}, null, "");

        SpecificationIsIn<Produto> spec = new SpecificationIsIn<>(filterData, conversionService);
        List<Produto> result = produtoRepository.findAll(spec);

        Assertions.assertThat(result)
                .hasSize(2)
                .extracting(Produto::getNome)
                .containsExactlyInAnyOrder("Notebook", "Consultoria TI");
    }

    @Test
    public void test_IsIn_OnElementCollection_MultiValueEntity_PageCountUsesDistinct() {
        FilterData filterData = new FilterData(new String[]{"tipos"}, new String[]{"tipos"}, Object.class,
                IsIn.class, false, new Object[]{new Object[]{"SERVICO", "ELETRONICO"}}, null, "");

        SpecificationIsIn<Produto> spec = new SpecificationIsIn<>(filterData, conversionService);
        Page<Produto> page = produtoRepository.findAll(spec, PageRequest.of(0, 1));

        Assertions.assertThat(page.getContent())
                .hasSize(1)
                .extracting(Produto::getNome)
                .containsAnyOf("Notebook", "Consultoria TI");
        Assertions.assertThat(page.getTotalElements()).isEqualTo(2L);
    }
}
