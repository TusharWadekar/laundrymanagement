package com.dattakrupa.laundry.repository;

import com.dattakrupa.laundry.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Phone already exist karta hai kya?

    // Name se search karo (case insensitive)
    List<Customer> findByNameContainingIgnoreCase(String name);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM Customer c WHERE TRIM(c.phoneNumber) = TRIM(:phoneNumber)")
    boolean existsByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @Query("SELECT c FROM Customer c WHERE TRIM(c.phoneNumber) = TRIM(:phoneNumber)")
    Optional<Customer> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);



    // Jinki udhari baki hai
    List<Customer> findByTotalDueGreaterThan(Double amount);

    // Jinki udhari zero hai (clean customers)
    List<Customer> findByTotalDue(Double amount);
}