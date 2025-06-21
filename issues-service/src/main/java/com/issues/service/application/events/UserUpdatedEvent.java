package com.issues.service.application.events;

import java.util.List;

/**
 * Event received when a user is updated in the IAM service
 * @param userId The ID of the updated user
 * @param email The updated email (or null if not changed)
 * @param roles Updated list of roles (or null if not changed)
 */
public record UserUpdatedEvent(Long userId, String email, List<String> roles) {}
