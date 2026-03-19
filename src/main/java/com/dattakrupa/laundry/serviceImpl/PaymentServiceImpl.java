package com.dattakrupa.laundry.serviceImpl;

import com.dattakrupa.laundry.dto.PaymentRequestDTO;
import com.dattakrupa.laundry.dto.PaymentResponseDTO;
import com.dattakrupa.laundry.enums.PaymentStatus;
import com.dattakrupa.laundry.exception.BadRequestException;
import com.dattakrupa.laundry.exception.PaymentException;
import com.dattakrupa.laundry.exception.ResourceNotFoundException;
import com.dattakrupa.laundry.model.Customer;
import com.dattakrupa.laundry.model.Order;
import com.dattakrupa.laundry.model.Payment;
import com.dattakrupa.laundry.repository.CustomerRepository;
import com.dattakrupa.laundry.repository.OrderRepository;
import com.dattakrupa.laundry.repository.PaymentRepository;
import com.dattakrupa.laundry.service.PaymentService;
import com.dattakrupa.laundry.service.WhatsAppService;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final WhatsAppService whatsAppService;

    // ── Step 1: Razorpay Order Banao ──
    @Override
    public Map<String, Object> createRazorpayOrder(Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Order", orderId));

            if (order.getPaymentStatus() == PaymentStatus.PAID) {
                throw new BadRequestException("Yeh order already paid hai!");
            }

            RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);

            double dueAmount = order.getTotalAmount() - order.getPaidAmount();

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", (int)(dueAmount * 100));
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "dattakrupa_order_" + orderId);
            orderRequest.put("notes", new JSONObject()
                    .put("shop", "DattaKrupa Laundry")
                    .put("orderId", orderId)
                    .put("customerName", order.getCustomer().getName())
            );

            com.razorpay.Order razorpayOrder = razorpay.orders.create(orderRequest);

            order.setRazorpayOrderId(razorpayOrder.get("id"));
            orderRepository.save(order);

            // ✅ Clean payment link
            String paymentLink = frontendUrl + "/pay/" + orderId;

            // ✅ WhatsApp pe bill bhejo
            whatsAppService.sendBillMessage(order, paymentLink);

            // ✅ Amount — decimal remove
            int amount = (int) Math.round(dueAmount);

            Map<String, Object> response = new HashMap<>();
            response.put("razorpayOrderId", razorpayOrder.get("id"));
            response.put("amount", (int)(dueAmount * 100));
            response.put("currency", "INR");
            response.put("keyId", keyId);
            response.put("customerName", order.getCustomer().getName());
            response.put("customerPhone", order.getCustomer().getPhoneNumber());
            response.put("description", "DattaKrupa Laundry — Order #" + orderId);
            response.put("paymentLink", paymentLink); // ✅ Clean link
            response.put("dueAmount", amount);        // ✅ No decimal

            return response;

        } catch (BadRequestException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new PaymentException(
                    "Payment order create nahi hua: " + e.getMessage());
        }
    }

    // ── Step 2: Payment Verify Karo ──
    @Override
    @Transactional
    public PaymentResponseDTO verifyPayment(PaymentRequestDTO request) {
        try {
            // Duplicate payment check
            if (paymentRepository.existsByRazorpayPaymentId(
                    request.getRazorpayPaymentId())) {
                throw new RuntimeException("Yeh payment already process ho chuki hai!");
            }

            // Signature verify karo
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", request.getRazorpayOrderId());
            attributes.put("razorpay_payment_id", request.getRazorpayPaymentId());
            attributes.put("razorpay_signature", request.getRazorpaySignature());

            boolean isValid = Utils.verifyPaymentSignature(attributes, keySecret);

            if (!isValid) {
                throw new RuntimeException("Payment signature invalid hai! ❌");
            }

            // Order dhundo
            Order order = orderRepository
                    .findByRazorpayOrderId(request.getRazorpayOrderId())
                    .orElseThrow(() -> new RuntimeException("Order nahi mila!"));

            // Order paid mark karo
            order.setPaidAmount(order.getTotalAmount());
            order.setPaymentStatus(PaymentStatus.PAID);
            orderRepository.save(order);

            // Customer ki udhari update karo
            Customer customer = order.getCustomer();
            if (customer.getTotalDue() > 0) {
                customer.setTotalDue(Math.max(0,
                        customer.getTotalDue() - order.getTotalAmount()));
                customerRepository.save(customer);
            }

            // Payment record save karo
            Payment payment = Payment.builder()
                    .order(order)
                    .customer(customer)
                    .razorpayOrderId(request.getRazorpayOrderId())
                    .razorpayPaymentId(request.getRazorpayPaymentId())
                    .razorpaySignature(request.getRazorpaySignature())
                    .amount(order.getTotalAmount())
                    .status("SUCCESS")
                    .paymentMode("ONLINE")
                    .build();

            Payment saved = paymentRepository.save(payment);

            // ✅ WhatsApp confirmation bhejo
            whatsAppService.sendPaymentConfirmation(order);

            log.info("✅ Payment successful + WhatsApp sent! Order #{}",
                    order.getId());

            return mapToResponse(saved);


        } catch (Exception e) {
            log.error("Payment verify failed: {}", e.getMessage());
            throw new RuntimeException("Payment verify nahi hua: " + e.getMessage());
        }
    }

    // ── Step 3: Webhook Handle Karo ──
    @Override
    @Transactional
    public void handleWebhook(String payload, String signature) {
        try {
            // Webhook signature verify karo
            boolean isValid = Utils.verifyWebhookSignature(
                    payload, signature, webhookSecret);

            if (!isValid) {
                log.warn("Invalid webhook signature!");
                return;
            }

            JSONObject event = new JSONObject(payload);
            String eventType = event.getString("event");

            log.info("Webhook received: {}", eventType);

            switch (eventType) {
                case "payment.captured" -> {
                    log.info("✅ Payment captured via webhook");
                    handlePaymentCaptured(event);
                }
                case "payment.failed" -> {
                    log.warn("❌ Payment failed via webhook");
                    handlePaymentFailed(event);
                }
                default -> log.info("Unhandled webhook event: {}", eventType);
            }

        } catch (Exception e) {
            log.error("Webhook handle failed: {}", e.getMessage());
        }
    }

    // ── Payment History ──
    @Override
    public List<PaymentResponseDTO> getPaymentsByOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponseDTO> getPaymentsByCustomer(Long customerId) {
        return paymentRepository
                .findByCustomerIdOrderByPaymentDateDesc(customerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── Private Helpers ──

    private void handlePaymentCaptured(JSONObject event) {
        try {
            JSONObject paymentEntity = event
                    .getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");

            String razorpayOrderId = paymentEntity.getString("order_id");
            String razorpayPaymentId = paymentEntity.getString("id");

            // Duplicate check
            if (paymentRepository.existsByRazorpayPaymentId(razorpayPaymentId)) {
                log.info("Payment already processed: {}", razorpayPaymentId);
                return;
            }

            // Order update karo
            orderRepository.findByRazorpayOrderId(razorpayOrderId)
                    .ifPresent(order -> {
                        order.setPaymentStatus(PaymentStatus.PAID);
                        order.setPaidAmount(order.getTotalAmount());
                        orderRepository.save(order);
                        log.info("Order #{} paid via webhook", order.getId());
                    });

        } catch (Exception e) {
            log.error("handlePaymentCaptured error: {}", e.getMessage());
        }
    }

    private void handlePaymentFailed(JSONObject event) {
        try {
            JSONObject paymentEntity = event
                    .getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");

            String razorpayOrderId = paymentEntity.getString("order_id");
            log.warn("Payment failed for Razorpay Order: {}", razorpayOrderId);

        } catch (Exception e) {
            log.error("handlePaymentFailed error: {}", e.getMessage());
        }
    }

    // Entity → DTO
    private PaymentResponseDTO mapToResponse(Payment payment) {
        return PaymentResponseDTO.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .customerName(payment.getCustomer().getName())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentMode(payment.getPaymentMode())
                .paymentDate(payment.getPaymentDate())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .build();
    }
}