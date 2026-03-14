package com.dattakrupa.laundry.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Kapde ka naam — Shirt, Pant, Saree, etc.
    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double pricePerItem;

    // Auto calculate — quantity * pricePerItem
    @Column(nullable = false)
    private Double subtotal;

    // Pre-save mein subtotal calculate karo
    @PrePersist
    @PreUpdate
    public void calculateSubtotal() {
        this.subtotal = this.quantity * this.pricePerItem;
    }
}