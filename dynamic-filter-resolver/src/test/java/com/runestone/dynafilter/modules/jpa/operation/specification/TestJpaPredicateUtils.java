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
import com.runestone.dynafilter.core.operation.types.Like;
import com.runestone.dynafilter.modules.jpa.operation.modifiers.ModJoinTypeInner;
import com.runestone.dynafilter.modules.jpa.operation.modifiers.ModJoinTypeRight;
import com.runestone.dynafilter.modules.jpa.tools.app.database.AddressRepository;
import com.runestone.dynafilter.modules.jpa.tools.app.database.InMemoryDatabaseApplication;
import com.runestone.dynafilter.modules.jpa.tools.app.database.PersonRepository;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Address;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Location;
import com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels.Person;
import org.assertj.core.api.Assertions;
import org.hibernate.query.sqm.tree.domain.AbstractSqmPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.util.List;

@DataJpaTest
@ContextConfiguration(classes = InMemoryDatabaseApplication.class)
public class TestJpaPredicateUtils {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Test
    public void testSimplePath() {
        FilterData filterData = FilterData.of("name", new String[]{"name"}, String.class, Like.class, new Object[]{"John"});
        Specification<Person> specification = (root, query, builder) -> {
            var path = JpaPredicateUtils.computeAttributePath(filterData, root);
            Assertions.assertThat(path).isNotNull();
            if (path instanceof AbstractSqmPath<Object> abstractSqmPath) {
                Assertions.assertThat(abstractSqmPath.getLhs().getNodeType().getBindableJavaType()).isEqualTo(Person.class);
                Assertions.assertThat(abstractSqmPath.getExpressible().getBindableJavaType()).isEqualTo(String.class);
                Assertions.assertThat(abstractSqmPath.getExpressible().toString()).containsIgnoringWhitespaces("name:String");
            }
            return builder.equal(root.get("name"), "John");
        };
        personRepository.findAll(specification);
    }

    @Test
    public void testJoinPath() {
        FilterData filterData = new FilterData("person.height", new String[]{"personHeight"}, String.class, Like.class,
                false, new Object[]{BigDecimal.valueOf(180)}, List.of(ModJoinTypeRight.class), null);
        Specification<Address> specification = (root, query, builder) -> {
            var path = JpaPredicateUtils.computeAttributePath(filterData, root);
            Assertions.assertThat(path).isNotNull();
            if (path instanceof AbstractSqmPath<Object> abstractSqmPath) {
                Assertions.assertThat(abstractSqmPath.getLhs().getNodeType().getBindableJavaType()).isEqualTo(Person.class);
                Assertions.assertThat(abstractSqmPath.getExpressible().getBindableJavaType()).isEqualTo(BigDecimal.class);
                Assertions.assertThat(abstractSqmPath.getExpressible().toString()).containsIgnoringWhitespaces("height:BigDecimal");
            }
            return builder.equal(root.get("street"), "John");
        };
        addressRepository.findAll(specification);
    }

    @Test
    public void testMultipleJoinPaths() {
        FilterData filterDataHeight = new FilterData("person.height", new String[]{"personHeight"}, String.class, Like.class,
                false, new Object[]{BigDecimal.valueOf(180)}, null, null);
        FilterData filterDataWeight = new FilterData("person.weight", new String[]{"personWeight"}, String.class, Like.class,
                false, new Object[]{BigDecimal.valueOf(90)}, null, null);
        FilterData filterDataState = new FilterData("location.state", new String[]{"personState"}, String.class, Like.class,
                false, new Object[]{"CA"}, List.of(ModJoinTypeInner.class), null);

        Specification<Address> specification = (root, query, builder) -> {
            var pathHeight = JpaPredicateUtils.computeAttributePath(filterDataHeight, root);
            Assertions.assertThat(pathHeight).isNotNull();
            if (pathHeight instanceof AbstractSqmPath<Object> abstractSqmPath) {
                Assertions.assertThat(abstractSqmPath.getLhs().getNodeType().getBindableJavaType()).isEqualTo(Person.class);
                Assertions.assertThat(abstractSqmPath.getExpressible().getBindableJavaType()).isEqualTo(BigDecimal.class);
                Assertions.assertThat(abstractSqmPath.getExpressible().toString()).containsIgnoringWhitespaces("height:BigDecimal");
            }

            var pathWeight = JpaPredicateUtils.computeAttributePath(filterDataWeight, root);
            Assertions.assertThat(pathWeight).isNotNull();
            if (pathWeight instanceof AbstractSqmPath<Object> abstractSqmPath) {
                Assertions.assertThat(abstractSqmPath.getLhs().getNodeType().getBindableJavaType()).isEqualTo(Person.class);
                Assertions.assertThat(abstractSqmPath.getExpressible().getBindableJavaType()).isEqualTo(BigDecimal.class);
                Assertions.assertThat(abstractSqmPath.getExpressible().toString()).containsIgnoringWhitespaces("weight:BigDecimal");
            }

            var pathState = JpaPredicateUtils.computeAttributePath(filterDataState, root);
            Assertions.assertThat(pathState).isNotNull();
            if (pathState instanceof AbstractSqmPath<Object> abstractSqmPath) {
                Assertions.assertThat(abstractSqmPath.getLhs().getNodeType().getBindableJavaType()).isEqualTo(Location.class);
                Assertions.assertThat(abstractSqmPath.getExpressible().getBindableJavaType()).isEqualTo(String.class);
                Assertions.assertThat(abstractSqmPath.getExpressible().toString()).containsIgnoringWhitespaces("state:String");
            }

            return builder.equal(root.get("street"), "John");
        };
        addressRepository.findAll(specification);
    }

}
