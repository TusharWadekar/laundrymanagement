package com.dattakrupa.laundry.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "udhari_ledger")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UdhariLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false)
    private Double amountDue;

    @Builder.Default
    private Double amountPaid = 0.0;

    // Baaki bacha hua amount
    @Column(nullable = false)
    private Double balance;

    // e.g. "Cash payment", "Online via Razorpay"
    private String note;

    @CreationTimestamp
    private LocalDateTime createdAt;
}