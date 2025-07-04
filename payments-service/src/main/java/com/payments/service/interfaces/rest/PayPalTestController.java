package com.payments.service.interfaces.rest;

import com.payments.service.infrastructure.paypal.PayPalService;
import com.payments.service.shared.interfaces.rest.resources.MessageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/paypal/test")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "PayPal Test", description = "PayPal integration testing endpoints")
public class PayPalTestController {

    private final PayPalService payPalService;

    @GetMapping("/connection")
    @Operation(summary = "Test PayPal connection")
    public ResponseEntity<?> testConnection() {
        try {
            boolean isConnected = payPalService.testConnection();

            Map<String, Object> response = new HashMap<>();
            response.put("connected", isConnected);
            response.put("message", isConnected ? "PayPal connection successful" : "PayPal connection failed");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error testing PayPal connection: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResource("Connection test failed: " + e.getMessage()));
        }
    }

    @PostMapping("/create-order")
    @Operation(summary = "Create a test PayPal order")
    public ResponseEntity<?> createTestOrder(
            @RequestParam(defaultValue = "10.00") String amount,
            @RequestParam(defaultValue = "USD") String currency,
            @RequestParam(defaultValue = "http://localhost:8086/api/v1/paypal/test/success") String returnUrl,
            @RequestParam(defaultValue = "http://localhost:8086/api/v1/paypal/test/cancel") String cancelUrl) {

        try {
            BigDecimal orderAmount = new BigDecimal(amount);
            var orderResponse = payPalService.createOrder(orderAmount, currency, returnUrl, cancelUrl);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", orderResponse.orderId());
            response.put("approvalUrl", orderResponse.approvalUrl());
            response.put("status", orderResponse.status());
            response.put("message", "Test order created successfully");
            response.put("instructions", "Use the approvalUrl to complete the payment in PayPal");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating test order: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResource("Failed to create test order: " + e.getMessage()));
        }
    }

    @PostMapping("/capture-order/{orderId}")
    @Operation(summary = "Capture a test PayPal order")
    public ResponseEntity<?> captureTestOrder(@PathVariable String orderId) {
        try {
            boolean captured = payPalService.captureOrder(orderId);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", orderId);
            response.put("captured", captured);
            response.put("message", captured ? "Order captured successfully" : "Order capture failed");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error capturing test order: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResource("Failed to capture order: " + e.getMessage()));
        }
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get PayPal order details")
    public ResponseEntity<?> getOrderDetails(@PathVariable String orderId) {
        try {
            var orderResponse = payPalService.getOrderDetails(orderId);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", orderResponse.orderId());
            response.put("approvalUrl", orderResponse.approvalUrl());
            response.put("status", orderResponse.status());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting order details: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResource("Failed to get order details: " + e.getMessage()));
        }
    }

    @GetMapping("/success")
    @Operation(summary = "PayPal payment success callback")
    public ResponseEntity<String> paymentSuccess(@RequestParam String token, @RequestParam String PayerID) {
        log.info("Payment success callback received. Token: {}, PayerID: {}", token, PayerID);
        return ResponseEntity.ok("""
            <html>
            <body>
                <h2>Payment Successful!</h2>
                <p>Token: %s</p>
                <p>Payer ID: %s</p>
                <p>You can now capture this payment using the capture endpoint.</p>
            </body>
            </html>
            """.formatted(token, PayerID));
    }

    @GetMapping("/cancel")
    @Operation(summary = "PayPal payment cancel callback")
    public ResponseEntity<String> paymentCancel(@RequestParam String token) {
        log.info("Payment cancel callback received. Token: {}", token);
        return ResponseEntity.ok("""
            <html>
            <body>
                <h2>Payment Cancelled</h2>
                <p>Token: %s</p>
                <p>The payment was cancelled by the user.</p>
            </body>
            </html>
            """.formatted(token));
    }
}
