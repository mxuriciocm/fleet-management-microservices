package com.shipments.service.application.events;

/**
 * Event received when a profile is created in the Profiles service
 * @param userId ID of the user this profile belongs to
 * @param firstName First name from the profile
 * @param lastName Last name from the profile
 * @param phoneNumber Phone number from the profile
 */
public record ProfileCreatedEvent(Long userId, String firstName, String lastName, String phoneNumber) {}
