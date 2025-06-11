package com.iam.service.interfaces.rest.dto;

import java.util.List;

/**
 * Data Transfer Object for User.
 * Used for REST API communication between services.
 *
 * @param userId User identifier
 * @param email User email
 * @param roles List of role names assigned to the user
 */
public record UserDto(
        Long userId,
        String email,
        List<String> roles
) {}