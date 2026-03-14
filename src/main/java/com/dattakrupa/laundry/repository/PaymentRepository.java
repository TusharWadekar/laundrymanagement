package com.dattakrupa.laundry.repository;

import com.dattakrupa.laundry.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Razorpay payment ID se dhundo
    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);

    // Duplicate payment check
    boolean existsByRazorpayPaymentId(String razorpayPaymentId);

    // Customer ki payment history
    List<Payment> findByCustomerIdOrderByPaymentDateDesc(Long customerId);

    // Order ki payments
    List<Payment> findByOrderId(Long orderId);

    // Aaj ki online payments
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.status = 'SUCCESS' " +
           "AND DATE(p.paymentDate) = CURRENT_DATE " +
           "AND p.paymentMode = 'ONLINE'")
    Double getTodayOnlinePayments();

    // Aaj ki cash payments
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.status = 'SUCCESS' " +
           "AND DATE(p.paymentDate) = CURRENT_DATE " +
           "AND p.paymentMode = 'CASH'")
    Double getTodayCashPayments();
}