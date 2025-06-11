package com.iam.service.interfaces.rest.resources;

/**
 * Resource for changing user email.
 */
public record ChangeEmailResource(
    String password,
    String newEmail
) {}
