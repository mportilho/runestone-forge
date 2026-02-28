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

package com.runestone.dynafilter.modules.jpa.resolver;

import com.runestone.dynafilter.modules.jpa.tools.app.database.InMemoryDatabaseApplication;
import com.runestone.dynafilter.modules.jpa.tools.app.database.PersonRepository;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Address;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Location;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Phone;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;
import jakarta.persistence.criteria.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@DataJpaTest
@ContextConfiguration(classes = InMemoryDatabaseApplication.class)
public class TestFetchingFilterDecoratorIntegration {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    public void setup() {
        personRepository.deleteAll();
        entityManager.flush();

        Person john = instantiate(Person.class);
        ReflectionTestUtils.setField(john, "name", "John Doe");
        ReflectionTestUtils.setField(john, "height", BigDecimal.valueOf(180));
        ReflectionTestUtils.setField(john, "weight", BigDecimal.valueOf(82));
        ReflectionTestUtils.setField(john, "birthday", LocalDate.of(1990, 1, 1));
        ReflectionTestUtils.setField(john, "registerDate", LocalDateTime.of(2024, 1, 1, 10, 30));

        Location sp = createLocation("Sao Paulo", "SP");
        Location rj = createLocation("Rio de Janeiro", "RJ");
        Address johnAddressOne = createAddress("Paulista", "1000", john, sp);
        Address johnAddressTwo = createAddress("Atlantica", "220", john, rj);
        Phone johnPhone = createPhone("11999990000", john);

        ReflectionTestUtils.setField(john, "addresses", Set.of(johnAddressOne, johnAddressTwo));
        ReflectionTestUtils.setField(john, "phones", Set.of(johnPhone));
        entityManager.persist(john);

        Person mary = instantiate(Person.class);
        ReflectionTestUtils.setField(mary, "name", "Mary Major");
        ReflectionTestUtils.setField(mary, "height", BigDecimal.valueOf(168));
        ReflectionTestUtils.setField(mary, "weight", BigDecimal.valueOf(64));
        ReflectionTestUtils.setField(mary, "birthday", LocalDate.of(1994, 3, 12));
        ReflectionTestUtils.setField(mary, "registerDate", LocalDateTime.of(2024, 2, 20, 9, 15));

        Location mg = createLocation("Belo Horizonte", "MG");
        Address maryAddress = createAddress("Afonso Pena", "85", mary, mg);
        ReflectionTestUtils.setField(mary, "addresses", Set.of(maryAddress));
        ReflectionTestUtils.setField(mary, "phones", Set.of());
        entityManager.persist(mary);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void testFetchDecoratorLoadsRequestedAssociation() {
        Specification<Person> baseFilter = nameStartsWith("John");
        Person withoutFetch = personRepository.findAll(baseFilter).getFirst();
        Assertions.assertThat(Persistence.getPersistenceUtil().isLoaded(withoutFetch, "addresses")).isFalse();

        entityManager.clear();

        Fetching fetching = FetchingAddressesOnly.class.getAnnotation(Fetching.class);
        Specification<Person> withFetchFilter = decorate(baseFilter, fetching);
        Person withFetch = personRepository.findAll(withFetchFilter).getFirst();

        Assertions.assertThat(Persistence.getPersistenceUtil().isLoaded(withFetch, "addresses")).isTrue();
    }

    @Test
    public void testNestedFetchPathLoadsCollectionAndNestedAssociation() {
        Fetching fetching = NestedAddressLocationFetch.class.getAnnotation(Fetching.class);
        Specification<Person> specification = decorate(nameStartsWith("John"), fetching);
        Person person = personRepository.findAll(specification).getFirst();

        Assertions.assertThat(Persistence.getPersistenceUtil().isLoaded(person, "addresses")).isTrue();
        Assertions.assertThat(Persistence.getPersistenceUtil().isLoaded(person, "phones")).isFalse();
        Assertions.assertThat(person.getAddresses()).allSatisfy(address ->
                Assertions.assertThat(Persistence.getPersistenceUtil().isLoaded(address, "location")).isTrue());
    }

    @Test
    public void testCountQueryDoesNotForceDistinct() {
        Fetching fetching = FetchingAddressesOnly.class.getAnnotation(Fetching.class);
        Specification<Person> specification = decorate(nameStartsWith("J"), fetching);
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<Person> root = query.from(Person.class);

        Predicate predicate = specification.toPredicate(root, query, criteriaBuilder);
        query.select(criteriaBuilder.count(root));
        if (predicate != null) {
            query.where(predicate);
        }

        Assertions.assertThat(query.isDistinct()).isFalse();
        Assertions.assertThat(root.getFetches()).isEmpty();
        Assertions.assertThat(entityManager.createQuery(query).getSingleResult()).isEqualTo(1L);
    }

    @Test
    public void testDuplicateFetchPathIsDeduplicated() {
        Fetching[] duplicateFetches = DuplicateAddressFetch.class.getAnnotationsByType(Fetching.class);
        Specification<Person> specification = decorate(nameStartsWith("John"), duplicateFetches);
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Person> query = criteriaBuilder.createQuery(Person.class);
        Root<Person> root = query.from(Person.class);

        Predicate predicate = specification.toPredicate(root, query, criteriaBuilder);
        if (predicate != null) {
            query.where(predicate);
        }

        Assertions.assertThat(root.getFetches()).hasSize(1);
        Fetch<?, ?> fetch = root.getFetches().iterator().next();
        Assertions.assertThat(fetch.getAttribute().getName()).isEqualTo("addresses");
        Assertions.assertThat(query.isDistinct()).isTrue();
    }

    @Test
    public void testInvalidPathFailsFast() {
        Fetching fetching = InvalidPathFetch.class.getAnnotation(Fetching.class);
        Specification<Person> specification = decorate(nameStartsWith("John"), fetching);
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Person> query = criteriaBuilder.createQuery(Person.class);
        Root<Person> root = query.from(Person.class);

        Assertions.assertThatThrownBy(() -> specification.toPredicate(root, query, criteriaBuilder))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("invalidPathField");
    }

    @SuppressWarnings("unchecked")
    private static Specification<Person> decorate(Specification<Person> specification, Fetching... fetchings) {
        return (Specification<Person>) new FetchingFilterDecorator(Arrays.asList(fetchings)).decorate(specification, null);
    }

    private static Specification<Person> nameStartsWith(String prefix) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("name"), prefix + "%");
    }

    private Location createLocation(String city, String state) {
        Location location = new Location();
        ReflectionTestUtils.setField(location, "city", city);
        ReflectionTestUtils.setField(location, "state", state);
        entityManager.persist(location);
        return location;
    }

    private static Address createAddress(String street, String number, Person person, Location location) {
        Address address = instantiate(Address.class);
        ReflectionTestUtils.setField(address, "street", street);
        ReflectionTestUtils.setField(address, "number", number);
        ReflectionTestUtils.setField(address, "person", person);
        ReflectionTestUtils.setField(address, "location", location);
        return address;
    }

    private static Phone createPhone(String number, Person person) {
        Phone phone = new Phone();
        ReflectionTestUtils.setField(phone, "number", number);
        ReflectionTestUtils.setField(phone, "person", person);
        return phone;
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

    @Fetching("addresses")
    @Target(TYPE)
    @Retention(RUNTIME)
    private @interface FetchingAddressesOnly {
    }

    @Fetching("addresses.location")
    @Target(TYPE)
    @Retention(RUNTIME)
    private @interface NestedAddressLocationFetch {
    }

    @Fetching("addresses")
    @Fetching("addresses")
    @Target(TYPE)
    @Retention(RUNTIME)
    private @interface DuplicateAddressFetch {
    }

    @Fetching("addresses.invalidPathField")
    @Target(TYPE)
    @Retention(RUNTIME)
    private @interface InvalidPathFetch {
    }
}
