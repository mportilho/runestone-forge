package com.runestone.dynafilter.modules.jpa.tools.app.database;

import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Address;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Location;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Phone;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Produto;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.TipoProduto;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaFixtureMappingTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("maps person fixture with addresses, phones and location")
    void mapsPersonFixtureWithRelationships() {
        Location location = new Location("Belem", "PA");
        entityManager.persist(location);

        Person person = new Person(
                "Ada Lovelace",
                new BigDecimal("1.70"),
                new BigDecimal("62.50"),
                LocalDate.of(1815, 12, 10),
                LocalDateTime.of(2026, 5, 16, 10, 30)
        );
        person.addAddress(new Address("Rua das Mangueiras", "42", location));
        person.addPhone(new Phone("9133334444"));

        entityManager.persist(person);
        entityManager.flush();
        entityManager.clear();

        Person persisted = entityManager.find(Person.class, person.getId());

        assertThat(persisted.getName()).isEqualTo("Ada Lovelace");
        assertThat(persisted.getAddresses())
                .singleElement()
                .satisfies(address -> {
                    assertThat(address.getStreet()).isEqualTo("Rua das Mangueiras");
                    assertThat(address.getLocation().getCity()).isEqualTo("Belem");
                });
        assertThat(persisted.getPhones())
                .singleElement()
                .extracting(Phone::getNumber)
                .isEqualTo("9133334444");
    }

    @Test
    @DisplayName("maps produto fixture with lazy enum element collection")
    void mapsProdutoFixtureWithElementCollection() {
        Produto produto = new Produto("Notebook", EnumSet.of(TipoProduto.ELETRONICO, TipoProduto.SERVICO));

        entityManager.persist(produto);
        entityManager.flush();
        entityManager.clear();

        Produto persisted = entityManager.find(Produto.class, produto.getId());

        assertThat(persisted.getNome()).isEqualTo("Notebook");
        assertThat(persisted.getTipos()).containsExactlyInAnyOrder(TipoProduto.ELETRONICO, TipoProduto.SERVICO);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = Person.class)
    static class TestApplication {
    }
}
