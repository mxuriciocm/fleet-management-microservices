package com.shipments.service.application.events;

import java.util.List;

/**
 * Event received when a user is created in the IAM service
 * @param userId The ID of the newly created user
 * @param email The email of the newly created user
 * @param roles List of roles assigned to the user
 */
public record UserCreatedEvent(Long userId, String email, List<String> roles) {}
