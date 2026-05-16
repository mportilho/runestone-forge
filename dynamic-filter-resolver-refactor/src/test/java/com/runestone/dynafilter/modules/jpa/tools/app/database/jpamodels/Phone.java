package com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.util.Objects;

@Entity
public class Phone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String number;

    @ManyToOne
    @JoinColumn
    private Person person;

    protected Phone() {
        // Required by JPA.
    }

    public Phone(String number) {
        this.number = Objects.requireNonNull(number, "number must not be null");
    }

    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public Person getPerson() {
        return person;
    }

    void assignPerson(Person person) {
        this.person = Objects.requireNonNull(person, "person must not be null");
    }
}
