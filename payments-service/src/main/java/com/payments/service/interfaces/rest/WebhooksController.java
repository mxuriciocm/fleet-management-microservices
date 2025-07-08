package com.payments.service.interfaces.rest;

import com.payments.service.domain.services.PaymentCommandService;
import com.payments.service.domain.services.SubscriptionCommandService;
import com.payments.service.infrastructure.paypal.PayPalService;
import com.payments.service.infrastructure.paypal.dto.PayPalWebhookEvent;
import com.payments.service.infrastructure.persistence.jpa.repositories.PaymentRepository;
import com.payments.service.infrastructure.persistence.jpa.repositories.SubscriptionRepository;
import com.payments.service.shared.interfaces.rest.resources.MessageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments/webhooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhooks", description = "PayPal webhook endpoints")
public class WebhooksController {

    private final PaymentCommandService paymentCommandService;
    private final SubscriptionCommandService subscriptionCommandService;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PayPalService payPalService;

    @Value("${paypal.webhook-id}")
    private String webhookId;

    @PostMapping("/paypal")
    @Operation(summary = "Handle PayPal webhooks")
    public ResponseEntity<?> handlePayPalWebhook(
            @RequestBody PayPalWebhookEvent event,
            @RequestHeader("PAYPAL-TRANSMISSION-ID") String transmissionId,
            @RequestHeader("PAYPAL-CERT-ID") String certId,
            @RequestHeader("PAYPAL-AUTH-ALGO") String authAlgo,
            @RequestHeader("PAYPAL-TRANSMISSION-SIG") String signature) {

        log.info("Received PayPal webhook event: {} with ID: {}", event.eventType(), event.id());


        try {
            boolean isValid = payPalService.verifyWebhookSignature(
                    event.toString(), signature, webhookId);

            if (!isValid) {
                log.warn("Invalid webhook signature for event: {}", event.id());
                return ResponseEntity.badRequest()
                        .body(new MessageResource("Invalid webhook signature"));
            }
        } catch (Exception e) {
            log.error("Error verifying webhook signature: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResource("Webhook verification failed"));
        }


        try {
            processWebhookEvent(event);
            return ResponseEntity.ok(new MessageResource("Webhook processed successfully"));
        } catch (Exception e) {
            log.error("Error processing webhook event {}: {}", event.id(), e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new MessageResource("Error processing webhook"));
        }
    }

    private void processWebhookEvent(PayPalWebhookEvent event) {
        switch (event.eventType()) {
            case "PAYMENT.CAPTURE.COMPLETED" -> handlePaymentCaptureCompleted(event);
            case "PAYMENT.CAPTURE.DENIED" -> handlePaymentCaptureDenied(event);
            case "BILLING.SUBSCRIPTION.ACTIVATED" -> handleSubscriptionActivated(event);
            case "BILLING.SUBSCRIPTION.CANCELLED" -> handleSubscriptionCancelled(event);
            case "BILLING.SUBSCRIPTION.SUSPENDED" -> handleSubscriptionSuspended(event);
            case "BILLING.SUBSCRIPTION.PAYMENT.FAILED" -> handleSubscriptionPaymentFailed(event);
            default -> log.info("Unhandled webhook event type: {}", event.eventType());
        }
    }

    private void handlePaymentCaptureCompleted(PayPalWebhookEvent event) {
        Map<String, Object> resource = event.resource();
        String paypalPaymentId = (String) resource.get("id");

        paymentRepository.findByPaypalOrderId(paypalPaymentId)
                .ifPresent(payment -> {
                    paymentCommandService.completePayment(payment.getId(), paypalPaymentId);

                    // If this is a subscription payment, handle subscription success
                    if (payment.getSubscriptionId() != null) {
                        subscriptionCommandService.handlePaymentSuccess(payment.getSubscriptionId());
                    }

                    log.info("Payment {} completed via webhook", payment.getId());
                });
    }

    private void handlePaymentCaptureDenied(PayPalWebhookEvent event) {
        Map<String, Object> resource = event.resource();
        String paypalPaymentId = (String) resource.get("id");
        String reason = (String) resource.get("reason_code");

        paymentRepository.findByPaypalOrderId(paypalPaymentId)
                .ifPresent(payment -> {
                    paymentCommandService.failPayment(payment.getId(), reason);


                    if (payment.getSubscriptionId() != null) {
                        subscriptionCommandService.handlePaymentFailure(payment.getSubscriptionId());
                    }

                    log.info("Payment {} failed via webhook: {}", payment.getId(), reason);
                });
    }

    private void handleSubscriptionActivated(PayPalWebhookEvent event) {
        Map<String, Object> resource = event.resource();
        String paypalSubscriptionId = (String) resource.get("id");

        subscriptionRepository.findByPaypalSubscriptionId(paypalSubscriptionId)
                .ifPresent(subscription -> {
                    subscriptionCommandService.activateSubscription(subscription.getId(), paypalSubscriptionId);
                    log.info("Subscription {} activated via webhook", subscription.getId());
                });
    }

    private void handleSubscriptionCancelled(PayPalWebhookEvent event) {
        Map<String, Object> resource = event.resource();
        String paypalSubscriptionId = (String) resource.get("id");

        subscriptionRepository.findByPaypalSubscriptionId(paypalSubscriptionId)
                .ifPresent(subscription -> {
                    subscriptionCommandService.handle(
                            new com.payments.service.domain.model.commands.CancelSubscriptionCommand(
                                    subscription.getId(), "Cancelled via PayPal webhook"));
                    log.info("Subscription {} cancelled via webhook", subscription.getId());
                });
    }

    private void handleSubscriptionSuspended(PayPalWebhookEvent event) {
        Map<String, Object> resource = event.resource();
        String paypalSubscriptionId = (String) resource.get("id");

        subscriptionRepository.findByPaypalSubscriptionId(paypalSubscriptionId)
                .ifPresent(subscription -> {
                    subscriptionCommandService.suspendSubscription(subscription.getId());
                    log.info("Subscription {} suspended via webhook", subscription.getId());
                });
    }

    private void handleSubscriptionPaymentFailed(PayPalWebhookEvent event) {
        Map<String, Object> resource = event.resource();
        String paypalSubscriptionId = (String) resource.get("id");

        subscriptionRepository.findByPaypalSubscriptionId(paypalSubscriptionId)
                .ifPresent(subscription -> {
                    subscriptionCommandService.handlePaymentFailure(subscription.getId());
                    log.info("Subscription {} payment failed via webhook", subscription.getId());
                });
    }
}
