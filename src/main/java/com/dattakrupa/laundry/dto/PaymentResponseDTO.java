package com.dattakrupa.laundry.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponseDTO {

    private Long id;
    private Long orderId;
    private String customerName;
    private Double amount;
    private String status;       // SUCCESS / FAILED
    private String paymentMode;  // ONLINE / CASH
    private LocalDateTime paymentDate;
    private String razorpayPaymentId;
}