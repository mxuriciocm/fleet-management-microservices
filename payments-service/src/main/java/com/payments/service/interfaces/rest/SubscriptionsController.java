package com.payments.service.interfaces.rest;

import com.payments.service.domain.model.commands.CancelSubscriptionCommand;
import com.payments.service.domain.model.commands.CreateSubscriptionCommand;
import com.payments.service.domain.model.queries.GetSubscriptionByIdQuery;
import com.payments.service.domain.model.queries.GetSubscriptionsByUserIdQuery;
import com.payments.service.domain.services.SubscriptionCommandService;
import com.payments.service.domain.services.SubscriptionQueryService;
import com.payments.service.infrastructure.paypal.PayPalService;
import com.payments.service.infrastructure.paypal.dto.PayPalSubscriptionRequest;
import com.payments.service.interfaces.rest.resources.CreateSubscriptionResource;
import com.payments.service.interfaces.rest.resources.SubscriptionResource;
import com.payments.service.interfaces.rest.transform.CreateSubscriptionCommandFromResourceAssembler;
import com.payments.service.interfaces.rest.transform.SubscriptionResourceFromEntityAssembler;
import com.payments.service.shared.interfaces.rest.resources.MessageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "Subscription management endpoints")
public class SubscriptionsController {

    private final SubscriptionCommandService subscriptionCommandService;
    private final SubscriptionQueryService subscriptionQueryService;
    private final PayPalService payPalService;

    @PostMapping
    @Operation(summary = "Create a new subscription")
    public ResponseEntity<?> createSubscription(@RequestBody CreateSubscriptionResource resource) {
        var command = CreateSubscriptionCommandFromResourceAssembler.toCommandFromResource(resource);
        var subscription = subscriptionCommandService.handle(command);

        if (subscription.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResource("Failed to create subscription"));
        }


        var paypalRequest = new PayPalSubscriptionRequest(
                subscription.get().getPlan().getPlanId(),
                subscription.get().getAmount(),
                subscription.get().getCurrency(),
                resource.returnUrl(),
                resource.cancelUrl(),
                subscription.get().getUserId()
        );

        try {
            var paypalResponse = payPalService.createSubscription(paypalRequest);
            var subscriptionResource = SubscriptionResourceFromEntityAssembler.toResourceFromEntity(subscription.get());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new CreateSubscriptionResponse(subscriptionResource, paypalResponse.approvalUrl()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResource("Failed to create PayPal subscription: " + e.getMessage()));
        }
    }

    @GetMapping("/{subscriptionId}")
    @Operation(summary = "Get subscription by ID")
    public ResponseEntity<?> getSubscriptionById(@PathVariable Long subscriptionId) {
        var query = new GetSubscriptionByIdQuery(subscriptionId);
        var subscription = subscriptionQueryService.handle(query);

        if (subscription.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResource("Subscription not found"));
        }

        var resource = SubscriptionResourceFromEntityAssembler.toResourceFromEntity(subscription.get());
        return ResponseEntity.ok(resource);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get subscriptions by user ID")
    public ResponseEntity<List<SubscriptionResource>> getSubscriptionsByUserId(@PathVariable Long userId) {
        var query = new GetSubscriptionsByUserIdQuery(userId);
        var subscriptions = subscriptionQueryService.handle(query);

        var resources = subscriptions.stream()
                .map(SubscriptionResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(resources);
    }

    @PostMapping("/{subscriptionId}/cancel")
    @Operation(summary = "Cancel subscription")
    public ResponseEntity<?> cancelSubscription(@PathVariable Long subscriptionId,
                                                @RequestParam(required = false) String reason) {
        var command = new CancelSubscriptionCommand(subscriptionId, reason != null ? reason : "User requested");
        var subscription = subscriptionCommandService.handle(command);

        if (subscription.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResource("Subscription not found"));
        }


        if (subscription.get().getPaypalSubscriptionId() != null) {
            try {
                payPalService.cancelSubscription(subscription.get().getPaypalSubscriptionId(), reason);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new MessageResource("Failed to cancel PayPal subscription: " + e.getMessage()));
            }
        }

        return ResponseEntity.ok(new MessageResource("Subscription canceled successfully"));
    }

    @PostMapping("/{subscriptionId}/activate")
    @Operation(summary = "Activate subscription")
    public ResponseEntity<?> activateSubscription(@PathVariable Long subscriptionId,
                                                  @RequestParam String paypalSubscriptionId) {
        try {
            subscriptionCommandService.activateSubscription(subscriptionId, paypalSubscriptionId);
            return ResponseEntity.ok(new MessageResource("Subscription activated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResource("Failed to activate subscription: " + e.getMessage()));
        }
    }

    public record CreateSubscriptionResponse(SubscriptionResource subscription, String approvalUrl) {}
}
