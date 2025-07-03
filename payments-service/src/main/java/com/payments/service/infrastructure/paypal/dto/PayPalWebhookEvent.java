package com.payments.service.infrastructure.paypal.dto;

import java.util.Map;

public record PayPalWebhookEvent(
        String id,
        String eventType,
        String createTime,
        String resourceType,
        Map<String, Object> resource,
        Map<String, Object> summary
) {}
