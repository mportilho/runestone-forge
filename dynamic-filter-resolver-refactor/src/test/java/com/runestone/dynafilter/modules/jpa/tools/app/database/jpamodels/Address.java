package com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

import java.util.Objects;

@Entity
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String street;
    private String number;

    @ManyToOne
    @JoinColumn
    private Person person;

    @OneToOne
    @JoinColumn
    private Location location;

    protected Address() {
        // Required by JPA.
    }

    public Address(String street, String number, Location location) {
        this.street = Objects.requireNonNull(street, "street must not be null");
        this.number = Objects.requireNonNull(number, "number must not be null");
        this.location = Objects.requireNonNull(location, "location must not be null");
    }

    public Long getId() {
        return id;
    }

    public String getStreet() {
        return street;
    }

    public String getNumber() {
        return number;
    }

    public Person getPerson() {
        return person;
    }

    public Location getLocation() {
        return location;
    }

    void assignPerson(Person person) {
        this.person = Objects.requireNonNull(person, "person must not be null");
    }
}
