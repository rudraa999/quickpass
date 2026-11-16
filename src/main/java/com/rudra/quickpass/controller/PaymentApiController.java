package com.rudra.quickpass.controller;

import com.rudra.quickpass.dto.BookPassRequest;
import com.rudra.quickpass.dto.CreateOrderResponse;
import com.rudra.quickpass.dto.VerifyPaymentRequest;
import com.rudra.quickpass.entity.Booking;
import com.rudra.quickpass.security.CustomUserDetails;
import com.rudra.quickpass.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentApiController {

    private final PaymentService paymentService;

    public PaymentApiController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Step 1: Create Order & Reserve Inventory
     */
    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponse> createOrder(
            @Valid @RequestBody BookPassRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CreateOrderResponse res = paymentService.createPaymentOrder(request, userDetails.getUser());
        return ResponseEntity.ok(res);
    }

    /**
     * Step 2: Cryptographically Verify Payment & Generate Passes
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Booking booking = paymentService.verifyPayment(request, userDetails.getUser());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Payment successfully verified! Your tickets are issued.",
                "bookingId", booking.getId(),
                "orderNumber", booking.getOrderNumber()
        ));
    }

    /**
     * Step 3: Handle Payment Cancellation (restore inventory)
     */
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelPayment(
            @RequestBody Map<String, String> body) {
        String razorpayOrderId = body.get("razorpayOrderId");
        if (razorpayOrderId != null && !razorpayOrderId.isBlank()) {
            paymentService.cancelPayment(razorpayOrderId);
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Payment hold released."
        ));
    }
}

