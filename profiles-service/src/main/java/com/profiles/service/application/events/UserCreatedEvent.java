package com.profiles.service.application.events;

public record UserCreatedEvent(Long userId, String email) {}
