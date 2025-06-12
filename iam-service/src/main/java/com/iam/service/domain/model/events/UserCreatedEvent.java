package com.iam.service.domain.model.events;

/**
 * Event published when a new user is created,
 * This event will be consumed by the Profile service to create a profile for the user
 *
 * @param userId The ID of the newly created user
 * @param email The email of the newly created user
 */
public record UserCreatedEvent(Long userId, String email) {}
