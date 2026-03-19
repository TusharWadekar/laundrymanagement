package com.dattakrupa.laundry.dto;

import com.dattakrupa.laundry.enums.OrderStatus;
import com.dattakrupa.laundry.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponseDTO {

    private Long id;

    // Customer ki basic info
    private Long customerId;
    private String customerName;
    private String customerPhone;

    private OrderStatus status;
    private PaymentStatus paymentStatus;

    private Double totalAmount;
    private Double paidAmount;
    private Double dueAmount; // totalAmount - paidAmount

    private List<OrderItemResponseDTO> items;

    private String notes;
    private LocalDate expectedDelivery;
    private LocalDateTime createdAt;
    private Long daysSinceCreated;
    // Razorpay ke liye
    private String razorpayOrderId;
}