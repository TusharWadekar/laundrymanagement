package com.dattakrupa.laundry.repository;

import com.dattakrupa.laundry.model.UdhariLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UdhariLedgerRepository extends JpaRepository<UdhariLedger, Long> {

    // Customer ki udhari history
    List<UdhariLedger> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    // Jinki udhari abhi baki hai
    List<UdhariLedger> findByBalanceGreaterThan(Double amount);

    // Total pending udhari
    @Query("SELECT COALESCE(SUM(u.balance), 0) FROM UdhariLedger u " +
           "WHERE u.balance > 0")
    Double getTotalPendingUdhari();

    // Customer ki total udhari
    @Query("SELECT COALESCE(SUM(u.balance), 0) FROM UdhariLedger u " +
           "WHERE u.customer.id = :customerId AND u.balance > 0")
    Double getCustomerTotalUdhari(Long customerId);
}