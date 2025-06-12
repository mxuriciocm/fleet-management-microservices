package com.profiles.service.interfaces.rest.resources;

public record CreateProfileResource(Long userId, String firstName, String lastName, String phoneNumber) {

    public CreateProfileResource {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }
    }
}
