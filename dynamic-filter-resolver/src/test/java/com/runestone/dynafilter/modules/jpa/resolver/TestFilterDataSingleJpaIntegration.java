package com.runestone.dynafilter.modules.jpa.resolver;

import com.runestone.dynafilter.core.generator.ConditionalStatement;
import com.runestone.dynafilter.core.generator.StatementWrapper;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.model.statement.LogicalStatement;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.IsIn;
import com.runestone.dynafilter.modules.jpa.spring.EnableDynamicFilterServletConfiguration;
import com.runestone.dynafilter.modules.jpa.tools.app.database.InMemoryDatabaseApplication;
import com.runestone.dynafilter.modules.jpa.tools.app.database.ProdutoRepository;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Produto;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.TipoProduto;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = {
        InMemoryDatabaseApplication.class,
        TestFilterDataSingleJpaIntegration.FilterConfiguration.class
})
final class TestFilterDataSingleJpaIntegration {

    @Autowired
    private ProdutoRepository repository;

    @Autowired
    private EntityManager entityManager;

    private Long notebookId;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        var notebook = repository.save(new Produto("Notebook", Set.of(TipoProduto.ELETRONICO)));
        notebookId = notebook.getId();
        repository.save(new Produto("Camiseta", Set.of(TipoProduto.VESTUARIO)));
        repository.save(new Produto("Consultoria TI", Set.of(TipoProduto.SERVICO, TipoProduto.ELETRONICO)));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void selectsRecordsUsingTheSuppliedValueAndExplicitParameterMetadata() {
        var filter = FilterData.single("nome", "applicationName", String.class, Equals.class, "Notebook");

        assertThat(repository.findAll(statement(filter)))
                .extracting(Produto::getNome)
                .containsExactly("Notebook");
    }

    @Test
    void convertsAnAbbreviatedFactoryValueInTheExistingQueryFlow() {
        var filter = FilterData.single("id", Long.class, Equals.class, notebookId.toString());

        assertThat(repository.findAll(statement(filter)))
                .extracting(Produto::getNome)
                .containsExactly("Notebook");
    }

    @Test
    void resolvesAnArrayValuedFilterWithoutFlatteningItsParameterSlots() {
        var filter = FilterData.single("tipos", "selectedTypes", TipoProduto.class, IsIn.class,
                new String[]{"ELETRONICO", "SERVICO"});

        assertThat(repository.findAll(statement(filter)))
                .extracting(Produto::getNome)
                .containsExactlyInAnyOrder("Notebook", "Consultoria TI");
    }

    private static ConditionalStatement statement(FilterData filter) {
        return new ConditionalStatement(new StatementWrapper(new LogicalStatement(filter), null, null), null);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableDynamicFilterServletConfiguration
    static final class FilterConfiguration {
    }
}
