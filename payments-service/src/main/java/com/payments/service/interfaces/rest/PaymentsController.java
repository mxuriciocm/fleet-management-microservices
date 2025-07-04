package com.payments.service.interfaces.rest;

import com.payments.service.domain.model.commands.CreatePaymentCommand;
import com.payments.service.domain.model.commands.ProcessPaymentCommand;
import com.payments.service.domain.model.queries.GetPaymentByIdQuery;
import com.payments.service.domain.model.queries.GetPaymentsBySubscriptionIdQuery;
import com.payments.service.domain.model.queries.GetPaymentsByUserIdQuery;
import com.payments.service.domain.services.PaymentCommandService;
import com.payments.service.domain.services.PaymentQueryService;
import com.payments.service.infrastructure.paypal.PayPalService;
import com.payments.service.interfaces.rest.resources.CreatePaymentResource;
import com.payments.service.interfaces.rest.resources.PaymentResource;
import com.payments.service.interfaces.rest.transform.CreatePaymentCommandFromResourceAssembler;
import com.payments.service.interfaces.rest.transform.PaymentResourceFromEntityAssembler;
import com.payments.service.shared.interfaces.rest.resources.MessageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment management endpoints")
public class PaymentsController {

    private final PaymentCommandService paymentCommandService;
    private final PaymentQueryService paymentQueryService;
    private final PayPalService payPalService;

    @PostMapping
    @Operation(summary = "Create a new payment")
    public ResponseEntity<?> createPayment(@RequestBody CreatePaymentResource resource) {
        var command = CreatePaymentCommandFromResourceAssembler.toCommandFromResource(resource);
        var payment = paymentCommandService.handle(command);

        if (payment.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResource("Failed to create payment"));
        }


        try {
            var paypalResponse = payPalService.createOrder(
                    payment.get().getAmount(),
                    payment.get().getCurrency(),
                    resource.returnUrl(),
                    resource.cancelUrl()
            );


            var processCommand = new ProcessPaymentCommand(payment.get().getId(), paypalResponse.orderId());
            paymentCommandService.handle(processCommand);

            var paymentResource = PaymentResourceFromEntityAssembler.toResourceFromEntity(payment.get());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new CreatePaymentResponse(paymentResource, paypalResponse.approvalUrl()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResource("Failed to create PayPal order: " + e.getMessage()));
        }
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<?> getPaymentById(@PathVariable Long paymentId) {
        var query = new GetPaymentByIdQuery(paymentId);
        var payment = paymentQueryService.handle(query);

        if (payment.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResource("Payment not found"));
        }

        var resource = PaymentResourceFromEntityAssembler.toResourceFromEntity(payment.get());
        return ResponseEntity.ok(resource);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get payments by user ID")
    public ResponseEntity<List<PaymentResource>> getPaymentsByUserId(@PathVariable Long userId) {
        var query = new GetPaymentsByUserIdQuery(userId);
        var payments = paymentQueryService.handle(query);

        var resources = payments.stream()
                .map(PaymentResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(resources);
    }

    @GetMapping("/subscription/{subscriptionId}")
    @Operation(summary = "Get payments by subscription ID")
    public ResponseEntity<List<PaymentResource>> getPaymentsBySubscriptionId(@PathVariable Long subscriptionId) {
        var query = new GetPaymentsBySubscriptionIdQuery(subscriptionId);
        var payments = paymentQueryService.handle(query);

        var resources = payments.stream()
                .map(PaymentResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(resources);
    }

    @PostMapping("/{paymentId}/capture")
    @Operation(summary = "Capture payment")
    public ResponseEntity<?> capturePayment(@PathVariable Long paymentId) {
        var query = new GetPaymentByIdQuery(paymentId);
        var payment = paymentQueryService.handle(query);

        if (payment.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResource("Payment not found"));
        }

        try {
            boolean captured = payPalService.captureOrder(payment.get().getPaypalOrderId());
            if (captured) {
                paymentCommandService.completePayment(paymentId, payment.get().getPaypalOrderId());
                return ResponseEntity.ok(new MessageResource("Payment captured successfully"));
            } else {
                paymentCommandService.failPayment(paymentId, "Failed to capture PayPal order");
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                        .body(new MessageResource("Failed to capture payment"));
            }
        } catch (Exception e) {
            paymentCommandService.failPayment(paymentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResource("Error capturing payment: " + e.getMessage()));
        }
    }

    public record CreatePaymentResponse(PaymentResource payment, String approvalUrl) {}
}
