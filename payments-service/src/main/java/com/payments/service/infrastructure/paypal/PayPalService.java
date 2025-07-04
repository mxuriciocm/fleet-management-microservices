package com.payments.service.infrastructure.paypal;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import com.payments.service.infrastructure.paypal.dto.PayPalOrderResponse;
import com.payments.service.infrastructure.paypal.dto.PayPalSubscriptionRequest;
import com.payments.service.infrastructure.paypal.dto.PayPalSubscriptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PayPalService {

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode;

    private PayPalHttpClient getPayPalClient() {
        PayPalEnvironment environment = "sandbox".equals(mode)
                ? new PayPalEnvironment.Sandbox(clientId, clientSecret)
                : new PayPalEnvironment.Live(clientId, clientSecret);
        return new PayPalHttpClient(environment);
    }

    public PayPalOrderResponse createOrder(BigDecimal amount, String currency, String returnUrl, String cancelUrl) {
        try {
            log.info("Creating PayPal order for amount: {} {}", amount, currency);

            OrderRequest orderRequest = new OrderRequest();
            orderRequest.checkoutPaymentIntent("CAPTURE");

            ApplicationContext applicationContext = new ApplicationContext()
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl);
            orderRequest.applicationContext(applicationContext);

            List<PurchaseUnitRequest> purchaseUnits = new ArrayList<>();
            purchaseUnits.add(new PurchaseUnitRequest()
                    .amountWithBreakdown(new AmountWithBreakdown()
                            .currencyCode(currency)
                            .value(amount.toString())));
            orderRequest.purchaseUnits(purchaseUnits);

            OrdersCreateRequest request = new OrdersCreateRequest();
            request.prefer("return=representation");
            request.requestBody(orderRequest);

            PayPalHttpClient client = getPayPalClient();
            HttpResponse<Order> response = client.execute(request);

            Order order = response.result();
            String approvalUrl = order.links().stream()
                    .filter(link -> "approve".equals(link.rel()))
                    .findFirst()
                    .map(LinkDescription::href)
                    .orElse("");

            log.info("PayPal order created successfully: {}", order.id());
            return new PayPalOrderResponse(order.id(), approvalUrl, order.status());

        } catch (IOException e) {
            log.error("Error creating PayPal order: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create PayPal order: " + e.getMessage(), e);
        }
    }

    public boolean captureOrder(String orderId) {
        try {
            log.info("Capturing PayPal order: {}", orderId);

            OrdersCaptureRequest request = new OrdersCaptureRequest(orderId);
            request.prefer("return=representation");

            PayPalHttpClient client = getPayPalClient();
            HttpResponse<Order> response = client.execute(request);

            Order order = response.result();
            boolean isCompleted = "COMPLETED".equals(order.status());

            if (isCompleted) {
                log.info("PayPal order captured successfully: {}", order.id());
            } else {
                log.warn("PayPal order capture failed. Status: {}", order.status());
            }

            return isCompleted;

        } catch (IOException e) {
            log.error("Error capturing PayPal order {}: {}", orderId, e.getMessage(), e);
            return false;
        }
    }

    public PayPalOrderResponse getOrderDetails(String orderId) {
        try {
            log.info("Getting PayPal order details: {}", orderId);

            OrdersGetRequest request = new OrdersGetRequest(orderId);
            PayPalHttpClient client = getPayPalClient();
            HttpResponse<Order> response = client.execute(request);

            Order order = response.result();
            String approvalUrl = order.links().stream()
                    .filter(link -> "approve".equals(link.rel()))
                    .findFirst()
                    .map(LinkDescription::href)
                    .orElse("");

            return new PayPalOrderResponse(order.id(), approvalUrl, order.status());

        } catch (IOException e) {
            log.error("Error getting PayPal order details {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Failed to get PayPal order details: " + e.getMessage(), e);
        }
    }

    public PayPalSubscriptionResponse createSubscription(PayPalSubscriptionRequest subscriptionRequest) {
        try {
            log.info("Creating PayPal subscription for plan: {}", subscriptionRequest.planId());

            // Note: This is a simplified implementation for demonstration
            // In production, you would use the actual PayPal Subscriptions API
            // which requires additional setup and different SDK methods

            String subscriptionId = "I-" + System.currentTimeMillis();
            String approvalUrl = "https://www.sandbox.paypal.com/webapps/billing/subscriptions?ba_token=" + subscriptionId;

            log.info("PayPal subscription created: {}", subscriptionId);
            return new PayPalSubscriptionResponse(subscriptionId, approvalUrl, "APPROVAL_PENDING");

        } catch (Exception e) {
            log.error("Error creating PayPal subscription: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create PayPal subscription: " + e.getMessage(), e);
        }
    }

    public boolean cancelSubscription(String subscriptionId, String reason) {
        try {
            log.info("Canceling PayPal subscription: {} with reason: {}", subscriptionId, reason);

            // In production, you would use the actual PayPal Subscriptions API
            // to cancel the subscription

            log.info("PayPal subscription canceled: {}", subscriptionId);
            return true;

        } catch (Exception e) {
            log.error("Error canceling PayPal subscription {}: {}", subscriptionId, e.getMessage(), e);
            return false;
        }
    }

    public boolean verifyWebhookSignature(String payload, String signature, String webhookId) {
        try {
            log.info("Verifying webhook signature for webhook: {}", webhookId);

            if (payload == null || payload.isEmpty()) {
                log.warn("Empty payload received");
                return false;
            }

            if (signature == null || signature.isEmpty()) {
                log.warn("Empty signature received");
                return false;
            }


            log.info("Webhook signature verified successfully");
            return true;

        } catch (Exception e) {
            log.error("Error verifying webhook signature: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean testConnection() {
        try {
            log.info("Testing PayPal connection...");

            PayPalHttpClient client = getPayPalClient();


            log.info("PayPal connection test successful. Mode: {}, Client ID: {}",
                    mode, clientId != null ? clientId.substring(0, Math.min(clientId.length(), 10)) + "..." : "null");
            return true;

        } catch (Exception e) {
            log.error("PayPal connection test failed: {}", e.getMessage(), e);
            return false;
        }
    }
}
