package com.payments.service.application.events;

import java.time.LocalDateTime;

public record UserCreatedEvent(
        Long userId,
        String email,
        String role,
        LocalDateTime createdAt
) {}