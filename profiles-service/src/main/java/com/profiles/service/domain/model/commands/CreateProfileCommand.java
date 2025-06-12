package com.profiles.service.domain.model.commands;

/**
 * Create User Profile Command
 * @param userId ID of the user this profile belongs to
 * @param firstName First name
 * @param lastName Last name
 * @param phoneNumber Phone number
 */
public record CreateProfileCommand(Long userId, String firstName, String lastName, String phoneNumber) {}
