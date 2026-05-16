package com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
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
    private final List<Address> addresses = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "person")
    private final List<Phone> phones = new ArrayList<>();

    protected Person() {
        // Required by JPA.
    }

    public Person(String name, BigDecimal height, BigDecimal weight, LocalDate birthday, LocalDateTime registerDate) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.height = Objects.requireNonNull(height, "height must not be null");
        this.weight = Objects.requireNonNull(weight, "weight must not be null");
        this.birthday = Objects.requireNonNull(birthday, "birthday must not be null");
        this.registerDate = Objects.requireNonNull(registerDate, "registerDate must not be null");
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

    public List<Address> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }

    public List<Phone> getPhones() {
        return Collections.unmodifiableList(phones);
    }

    public void addAddress(Address address) {
        Address requiredAddress = Objects.requireNonNull(address, "address must not be null");
        requiredAddress.assignPerson(this);
        addresses.add(requiredAddress);
    }

    public void addPhone(Phone phone) {
        Phone requiredPhone = Objects.requireNonNull(phone, "phone must not be null");
        requiredPhone.assignPerson(this);
        phones.add(requiredPhone);
    }
}
