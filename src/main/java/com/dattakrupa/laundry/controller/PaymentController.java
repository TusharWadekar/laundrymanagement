package com.dattakrupa.laundry.controller;

import com.dattakrupa.laundry.dto.ApiResponseDTO;
import com.dattakrupa.laundry.dto.PaymentRequestDTO;
import com.dattakrupa.laundry.dto.PaymentResponseDTO;
import com.dattakrupa.laundry.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    // ── POST /payment/create-order/{orderId} ──
    // Razorpay order banao + WhatsApp bill bhejo
    @PostMapping("/create-order/{orderId}")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> createOrder(
            @PathVariable Long orderId) {

        Map<String, Object> response = paymentService.createRazorpayOrder(orderId);
        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        "Razorpay order ready! 💳", response));
    }

    // ── POST /payment/verify ──
    // Customer ne pay kiya → verify karo
    @PostMapping("/verify")
    public ResponseEntity<ApiResponseDTO<PaymentResponseDTO>> verifyPayment(
            @Valid @RequestBody PaymentRequestDTO request) {

        PaymentResponseDTO response = paymentService.verifyPayment(request);
        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        "Payment successful! ✅ DattaKrupa Laundry", response));
    }

    // ── POST /payment/webhook ──
    // Razorpay webhook — automatic payment updates
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok("Webhook received ✅");
    }

    // ── GET /payment/order/{orderId} ──
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponseDTO<List<PaymentResponseDTO>>> getByOrder(
            @PathVariable Long orderId) {

        List<PaymentResponseDTO> payments =
                paymentService.getPaymentsByOrder(orderId);
        return ResponseEntity.ok(
                ApiResponseDTO.success("Order payments", payments));
    }

    // ── GET /payment/customer/{customerId} ──
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponseDTO<List<PaymentResponseDTO>>> getByCustomer(
            @PathVariable Long customerId) {

        List<PaymentResponseDTO> payments =
                paymentService.getPaymentsByCustomer(customerId);
        return ResponseEntity.ok(
                ApiResponseDTO.success("Customer payment history", payments));
    }
}