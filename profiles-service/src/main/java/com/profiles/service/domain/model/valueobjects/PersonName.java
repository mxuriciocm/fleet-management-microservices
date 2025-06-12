package com.profiles.service.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record PersonName(String firstName, String lastName) {

    public PersonName {
        // Convert null to empty string
        firstName = (firstName == null) ? "" : firstName.trim();
        lastName = (lastName == null) ? "" : lastName.trim();
    }

    public String getFullName() {
        return String.format("%s %s", firstName, lastName).trim();
    }
}
