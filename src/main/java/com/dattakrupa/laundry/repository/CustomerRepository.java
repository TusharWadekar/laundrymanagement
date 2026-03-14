package com.dattakrupa.laundry.repository;

import com.dattakrupa.laundry.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Phone se customer dhundo
    Optional<Customer> findByPhoneNumber(String phoneNumber);

    // Phone already exist karta hai kya?
    boolean existsByPhoneNumber(String phoneNumber);

    // Name se search karo (case insensitive)
    List<Customer> findByNameContainingIgnoreCase(String name);

    // Jinki udhari baki hai
    List<Customer> findByTotalDueGreaterThan(Double amount);

    // Jinki udhari zero hai (clean customers)
    List<Customer> findByTotalDue(Double amount);
}