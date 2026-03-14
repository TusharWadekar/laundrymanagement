package com.dattakrupa.laundry.repository;

import com.dattakrupa.laundry.enums.OrderStatus;
import com.dattakrupa.laundry.enums.PaymentStatus;
import com.dattakrupa.laundry.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Customer ke saare orders
    List<Order> findByCustomerId(Long customerId);

    // Status se filter karo
    List<Order> findByStatus(OrderStatus status);

    // Payment status se filter
    List<Order> findByPaymentStatus(PaymentStatus paymentStatus);

    // Razorpay order ID se dhundo
    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);

    // Aaj ke orders
    List<Order> findByCreatedAtBetween(
        java.time.LocalDateTime start,
        java.time.LocalDateTime end
    );

    // Customer ke unpaid orders
    List<Order> findByCustomerIdAndPaymentStatus(
        Long customerId,
        PaymentStatus paymentStatus
    );

    // Today's total revenue
    @Query("SELECT COALESCE(SUM(o.paidAmount), 0) FROM Order o " +
           "WHERE DATE(o.createdAt) = CURRENT_DATE")
    Double getTodayRevenue();

    // Total pending amount
    @Query("SELECT COALESCE(SUM(o.totalAmount - o.paidAmount), 0) FROM Order o " +
           "WHERE o.paymentStatus != 'PAID'")
    Double getTotalPendingAmount();
}