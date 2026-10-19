package com.runestone.dynafilter.modules.jpa.tools.sortmodel;

import jakarta.persistence.Embeddable;

@Embeddable
public class SortDetails {
    private String city;

    protected SortDetails() {
    }

    public SortDetails(String city) {
        this.city = city;
    }
}
