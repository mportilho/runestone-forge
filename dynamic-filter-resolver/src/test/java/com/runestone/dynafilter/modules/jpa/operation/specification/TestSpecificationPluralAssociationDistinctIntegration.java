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

import com.runestone.converters.RuntimeDataConversionService;
import com.runestone.converters.impl.runtime.DefaultRuntimeDataConversionService;
import com.runestone.dynafilter.core.model.FilterData;
import com.runestone.dynafilter.core.operation.types.Equals;
import com.runestone.dynafilter.core.operation.types.IsIn;
import com.runestone.dynafilter.core.operation.types.Like;
import com.runestone.dynafilter.modules.jpa.tools.app.database.InMemoryDatabaseApplication;
import com.runestone.dynafilter.modules.jpa.tools.app.database.PersonRepository;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Address;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Location;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@DataJpaTest
@ContextConfiguration(classes = InMemoryDatabaseApplication.class)
public class TestSpecificationPluralAssociationDistinctIntegration {

    private static final RuntimeDataConversionService CONVERSION_SERVICE = DefaultRuntimeDataConversionService.standard();

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    public void setup() {
        personRepository.deleteAll();
        entityManager.flush();

        Person john = createPerson("John Doe", BigDecimal.valueOf(180));
        Location saoPaulo = createLocation("Sao Paulo", "SP");
        Location campinas = createLocation("Campinas", "SP");
        Address johnAddressOne = createAddress("Main Avenue", "100", john, saoPaulo);
        Address johnAddressTwo = createAddress("Main Street", "200", john, campinas);
        ReflectionTestUtils.setField(john, "addresses", Set.of(johnAddressOne, johnAddressTwo));
        ReflectionTestUtils.setField(john, "phones", Set.of());
        entityManager.persist(john);

        Person mary = createPerson("Mary Major", BigDecimal.valueOf(168));
        Location beloHorizonte = createLocation("Belo Horizonte", "MG");
        Address maryAddress = createAddress("Afonso Pena", "85", mary, beloHorizonte);
        ReflectionTestUtils.setField(mary, "addresses", Set.of(maryAddress));
        ReflectionTestUtils.setField(mary, "phones", Set.of());
        entityManager.persist(mary);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Equals across OneToMany returns distinct root rows and count")
    public void testEqualsAcrossOneToManyReturnsDistinctRowsAndCount() {
        FilterData filterData = new FilterData(new String[]{"addresses.location.state"}, new String[]{"state"}, String.class,
                Equals.class, false, new Object[]{"SP"}, null, "");
        SpecificationEquals<Person> specification = new SpecificationEquals<>(filterData, CONVERSION_SERVICE);

        Page<Person> page = personRepository.findAll(specification, PageRequest.of(0, 1, Sort.by("name")));

        Assertions.assertThat(page.getContent())
                .hasSize(1)
                .extracting(Person::getName)
                .containsExactly("John Doe");
        Assertions.assertThat(page.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Like across OneToMany returns each root only once")
    public void testLikeAcrossOneToManyReturnsDistinctRows() {
        FilterData filterData = new FilterData(new String[]{"addresses.street"}, new String[]{"street"}, String.class,
                Like.class, false, new Object[]{"Main"}, null, "");
        SpecificationLike<Person> specification = new SpecificationLike<>(filterData, CONVERSION_SERVICE);

        var result = personRepository.findAll(specification, Sort.by("name"));

        Assertions.assertThat(result)
                .hasSize(1)
                .extracting(Person::getName)
                .containsExactly("John Doe");
    }

    @Test
    @DisplayName("IsIn across OneToMany and final simple attribute returns distinct root rows")
    public void testIsInAcrossOneToManyFinalSimpleAttributeReturnsDistinctRows() {
        FilterData filterData = new FilterData(new String[]{"addresses.location.state"}, new String[]{"states"}, String.class,
                IsIn.class, false, new Object[]{new Object[]{"SP"}}, null, "");
        SpecificationIsIn<Person> specification = new SpecificationIsIn<>(filterData, CONVERSION_SERVICE);

        var result = personRepository.findAll(specification, Sort.by("name"));

        Assertions.assertThat(result)
                .hasSize(1)
                .extracting(Person::getName)
                .containsExactly("John Doe");
    }

    @Test
    @DisplayName("ManyToOne path does not mark the CriteriaQuery as distinct")
    public void testManyToOnePathDoesNotMarkQueryAsDistinct() {
        FilterData filterData = new FilterData(new String[]{"person.name"}, new String[]{"personName"}, String.class,
                Equals.class, false, new Object[]{"John Doe"}, null, "");
        SpecificationEquals<Address> specification = new SpecificationEquals<>(filterData, CONVERSION_SERVICE);
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Address> query = criteriaBuilder.createQuery(Address.class);
        Root<Address> root = query.from(Address.class);

        specification.toPredicate(root, query, criteriaBuilder);

        Assertions.assertThat(query.isDistinct()).isFalse();
    }

    private Location createLocation(String city, String state) {
        Location location = new Location();
        ReflectionTestUtils.setField(location, "city", city);
        ReflectionTestUtils.setField(location, "state", state);
        entityManager.persist(location);
        return location;
    }

    private static Person createPerson(String name, BigDecimal height) {
        Person person = instantiate(Person.class);
        ReflectionTestUtils.setField(person, "name", name);
        ReflectionTestUtils.setField(person, "height", height);
        ReflectionTestUtils.setField(person, "weight", BigDecimal.valueOf(70));
        ReflectionTestUtils.setField(person, "birthday", LocalDate.of(1990, 1, 1));
        ReflectionTestUtils.setField(person, "registerDate", LocalDateTime.of(2024, 1, 1, 10, 30));
        return person;
    }

    private static Address createAddress(String street, String number, Person person, Location location) {
        Address address = instantiate(Address.class);
        ReflectionTestUtils.setField(address, "street", street);
        ReflectionTestUtils.setField(address, "number", number);
        ReflectionTestUtils.setField(address, "person", person);
        ReflectionTestUtils.setField(address, "location", location);
        return address;
    }

    private static <T> T instantiate(Class<T> type) {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not instantiate " + type.getName(), e);
        }
    }

}
