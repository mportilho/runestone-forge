package com.runestone.dynafilter.modules.jpa.tools.app.database.jpamodels;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Objects;

@Entity
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String city;
    private String state;

    protected Location() {
        // Required by JPA.
    }

    public Location(String city, String state) {
        this.city = Objects.requireNonNull(city, "city must not be null");
        this.state = Objects.requireNonNull(state, "state must not be null");
    }

    public Long getId() {
        return id;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }
}
