package com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Entity
@Table
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private BigDecimal height;

    private BigDecimal weight;

    private LocalDate birthday;

    private LocalDateTime registerDate;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "person")
    private Set<Address> addresses;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "person")
    private Set<Phone> phones;

    protected Person() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public LocalDateTime getRegisterDate() {
        return registerDate;
    }

    public Set<Address> getAddresses() {
        return addresses;
    }

    public Set<Phone> getPhones() {
        return phones;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Person other = (Person) obj;
        return getId() != null && Objects.equals(id, other.id);
    }

}
