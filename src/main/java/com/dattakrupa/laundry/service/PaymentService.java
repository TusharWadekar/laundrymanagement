package com.dattakrupa.laundry.service;

import com.dattakrupa.laundry.dto.PaymentRequestDTO;
import com.dattakrupa.laundry.dto.PaymentResponseDTO;

import java.util.List;
import java.util.Map;

public interface PaymentService {

    // Razorpay order banao
    Map<String, Object> createRazorpayOrder(Long orderId);

    // Payment verify karo
    PaymentResponseDTO verifyPayment(PaymentRequestDTO request);

    // Webhook handle karo
    void handleWebhook(String payload, String signature);

    // Order ki payment history
    List<PaymentResponseDTO> getPaymentsByOrder(Long orderId);

    // Customer ki payment history
    List<PaymentResponseDTO> getPaymentsByCustomer(Long customerId);
}